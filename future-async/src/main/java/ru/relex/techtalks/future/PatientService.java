package ru.relex.techtalks.future;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import ru.relex.techtalks.async.mapper.HospitalMapper;
import ru.relex.techtalks.async.mapper.IPatientMapper;
import ru.relex.techtalks.async.model.*;
import ru.relex.techtalks.async.service.impl.AbstractPatientService;

import static ru.relex.techtalks.async.CoreConfig.CONCURRENCY;

/**
 * @author Nikita Skornyakov
 * @date 05.12.2018
 */
@Service
public class PatientService extends AbstractPatientService {

  //EDIT THIS TO SEE how performance will change
  private final Executor executor = Executors.newWorkStealingPool(CONCURRENCY);

  class DiscountData {
    Patient patient;
    Insurance insurance;
    Discount discount;
    Hospital hospital;
    BigDecimal price;
  }

  public PatientService(IPatientMapper mapper,
                        ObjectMapper objectMapper,
                        HospitalMapper hospitalMapper) {
    super(mapper, objectMapper, hospitalMapper);
  }

  @Override
  public PatientDto retrievePatientData(long id, String requestCat, String loc) {
    return null;
  }

  public CompletableFuture<PatientDto> retrievePatientDataAsync(
    long id, String requestCat, String loc
  ) {
    CompletableFuture<DiscountData> discountFuture = calculateDiscount(id);
    return calculatePatientResponse(requestCat, loc, discountFuture);
  }

  private CompletableFuture<PatientDto> calculatePatientResponse(
    String requestCat,
    String loc,
    CompletableFuture<DiscountData> discountFuture
  ) {
    CompletableFuture<List<Hospital>> hospitalsFuture = findNearestHospitalsAsync(loc);

    return discountFuture
      .thenCompose(data ->
        hospitalsFuture.thenApply(hospitals -> {
          data.hospital = getWeightedHospitalPricing(hospitals, data.discount);
          return data;
        })
      ).thenCompose(data ->
        hospitalMapper.getCategoryAvgPriceFuture(requestCat, data.hospital.getTitle())
          .thenApply((price) -> {
            data.price = this.calculatePrice(price, data.discount);
            return data;
          })
      ).thenApply(data ->
        aggregate(data.patient, data.insurance, data.hospital, data.price)
      );
  }

  private CompletableFuture<DiscountData> calculateDiscount(long id) {
    CompletableFuture<Long> requestsFuture = hospitalMapper.requestCountFuture(id);

    return CompletableFuture.supplyAsync(() -> findPatientById(id), executor)
      .thenApply(patient -> {
        var data = new DiscountData();
        data.patient = patient;
        return data;
      }).thenCompose(data ->
        findPatientInsuranceAsync(data.patient.getExtId())
          .thenApply(insurance -> {
            data.insurance = insurance;
            return data;
          })
      ).thenCombine(requestsFuture, (data, requests) -> {
        data.discount = calculateDiscount(requests, data.insurance);
        return data;
      });
  }

  private PatientDto innerRetrievePatientDataSync(long id, String requestCat, String loc) {

    var requests = hospitalMapper.requestCount(id);
    var hospitals = findNearestHospitals(loc);

    PatientDto patientDto = new PatientDto(findPatientById(id));
    patientDto.setInsurance(findPatientInsurance(patientDto.getPatient().getExtId()));
    Discount d = calculateDiscount(requests, patientDto.getInsurance());
    patientDto.setHospital(getWeightedHospitalPricing(hospitals, d));
    patientDto
      .setPricing(
        calculatePrice(hospitalMapper
          .getCategoryAvgPrice(requestCat, patientDto.getHospital().getTitle()), d));

    return patientDto;
  }
}
