package ru.relex.techtalks.async.rx.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.relex.techtalks.async.mapper.HospitalMapper;
import ru.relex.techtalks.async.mapper.IPatientMapper;
import ru.relex.techtalks.async.model.*;
import ru.relex.techtalks.async.service.impl.AbstractPatientService;

@Service
public class PatientService extends AbstractPatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

  private Scheduler jdbcPool;
  private Scheduler asyncPool;
  private WebClient hospitalClient = WebClient.create(hospitalLookupUri);
  private WebClient insuranceClient = WebClient.create(insuranceUri);

  @Autowired
  public PatientService(IPatientMapper mapper,
                        ObjectMapper objectMapper,
                        @Qualifier("JDBC_POOL") Scheduler jdbcPool,
                        @Qualifier("ASYNC_POOL") Scheduler asyncPool,
                        HospitalMapper hospitalMapper) {
    super(mapper, objectMapper, hospitalMapper);
    this.jdbcPool = jdbcPool;
    this.asyncPool = asyncPool;
  }

  @Override
  public PatientDto retrievePatientData(long id, String requestCat, String loc) {
    return null;
  }

  public Mono<PatientDto> retrievePatientDataAsync(long id, String requestCat, String loc) {
    logger.info("Retrieve patient data");

    return findPatientMono(id)
      .flatMap(patient -> Mono.zip(
        findPatientInsuranceMono(patient.getExtId()),
        hospitalMapper.requestCountMono(patient.getId()),
        findNearestHospitalsFlux(loc).collectList()
      ).flatMap(elements -> {
        Insurance insurance = elements.getT1();
        Long historyCount = elements.getT2();
        List<Hospital> hospitals = elements.getT3();

        Discount discount = calculateDiscount(historyCount, insurance);
        Hospital hospital = getWeightedHospitalPricing(hospitals, discount);

        return hospitalMapper.getCategoryAvgPriceMono(requestCat, hospital.getTitle())
          .map(price -> {
            BigDecimal finalPrice = calculatePrice(price, discount);
            return aggregate(patient, insurance, hospital, finalPrice);
          });
      }));
  }

  private Mono<Patient> findPatientMono(long id) {
    logger.info("Request patient");
    return Mono.fromCallable(() -> {
      logger.info("Request patient");
      return patientMapper.getById(id);
    })
      .subscribeOn(jdbcPool)
      .publishOn(asyncPool);
  }

  private Flux<Hospital> findNearestHospitalsFlux(String loc) {
    logger.info("Hospital request");
    return hospitalClient
      .get()
      .uri(hospitalLookupUri + "?loc=" + loc)
      .retrieve()
      .bodyToFlux(Hospital.class);
  }

  private Mono<Insurance> findPatientInsuranceMono(String insuranceId) {
    logger.info("Insurance request");
    return insuranceClient
      .get()
      .uri(insuranceUri + "?iid=" + insuranceId)
      .retrieve()
      .bodyToMono(Insurance.class);
  }
}
