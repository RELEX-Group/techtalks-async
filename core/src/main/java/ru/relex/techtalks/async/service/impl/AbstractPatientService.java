package ru.relex.techtalks.async.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import ru.relex.techtalks.async.mapper.HospitalMapper;
import ru.relex.techtalks.async.mapper.IPatientMapper;
import ru.relex.techtalks.async.model.*;
import ru.relex.techtalks.async.service.IPatientService;

/**
 * @author Nikita Skornyakov
 * @date 03.12.2018
 */
@PropertySource("classpath:/properties.properties")
public abstract class AbstractPatientService implements IPatientService {


  protected static final BigDecimal HUNDRED_PCT = BigDecimal.valueOf(100);
  protected final IPatientMapper patientMapper;
  protected final ObjectMapper objectMapper;
  protected final HospitalMapper hospitalMapper;

  private static final HttpClient CLIENT = HttpClient
    .newBuilder()
    .version(Version.HTTP_1_1)
    .build();

  @Value("${insurance.uri}")
  protected String insuranceUri;

  @Value("${hospital.lookup.uri}")
  protected String hospitalLookupUri;

  public AbstractPatientService(IPatientMapper mapper, ObjectMapper objectMapper,
                                HospitalMapper hospitalMapper) {
    this.patientMapper = mapper;
    this.objectMapper = objectMapper;
    this.hospitalMapper = hospitalMapper;
  }

  @Override
  public abstract PatientDto retrievePatientData(long id, String requestCat, String loc);

  @Override
  public Patient findPatientById(long id) {
    return patientMapper.getById(id);
  }

  @Override
  public Insurance findPatientInsurance(String insuranceId) {
    try {
      return objectMapper.readValue(
        CLIENT.send(HttpRequest
            .newBuilder()
            .GET()
            .uri(URI.create(insuranceUri + "?iid=" + insuranceId))
            .build(),
          BodyHandlers.ofString(StandardCharsets.UTF_8))
          .body(), Insurance.class);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

  protected CompletableFuture<Insurance> findPatientInsuranceAsync(String insuranceId) {
    var handler = BodyHandlers.ofString(StandardCharsets.UTF_8);
    var request = HttpRequest
      .newBuilder()
      .GET()
      .uri(URI.create(insuranceUri + "?iid=" + insuranceId))
      .build();
    return CLIENT.sendAsync(request, handler)
      .thenApply(response -> {
        try {
          return objectMapper.readValue(response.body(), Insurance.class);
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      });
  }

  @Override
  public List<History> findPatientHistory(String userId) {
    return Collections.emptyList(); //TODO: implement method
  }

  protected CompletableFuture<List<Hospital>> findNearestHospitalsAsync(String loc) {
    var request = HttpRequest
      .newBuilder()
      .GET()
      .uri(URI.create(hospitalLookupUri + "?loc=" + loc))
      .build();
    var handler = BodyHandlers.ofString(StandardCharsets.UTF_8);
    return CLIENT.sendAsync(request, handler)
      .thenApply(response -> {
        try {
          return objectMapper.readValue(response.body(), new TypeReference<List<Hospital>>() {});
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      });
  }

  public List<Hospital> findNearestHospitals(String loc) {
    try {
      String hospitals = requestHospitals(loc).body();
      return objectMapper.readValue(hospitals, new TypeReference<List<Hospital>>() {
      });
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  protected HttpResponse<String> requestHospitals(String loc) {
    try {
      return CLIENT.send(hospitalRequest(loc),
        BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected final HttpRequest hospitalRequest(String loc) {
    return HttpRequest
      .newBuilder()
      .GET()
      .uri(URI.create(hospitalLookupUri + "?loc=" + loc))
      .build();
  }

  @Override
  public Discount calculateDiscount(long requestCount, Insurance insurance) {
    var discount = new Discount();
    discount.setDiscountPercent(BigDecimal.valueOf(Math.min(requestCount / 3, 10)));
    boolean isExpired = Instant.ofEpochSecond(insurance.getExpirationDate()).isBefore(Instant.now());
    if (!isExpired) {
      discount.setDiscountDollars(InsuranceType.valueOf(insurance.getInsuranceCategory()).getValue());
    }
    return discount;
  }

  @Override
  public Hospital getWeightedHospitalPricing(List<Hospital> hospitals, Discount discount) {
    if (hospitals.isEmpty()) {
      return null;
    }
    return hospitals.get(0);
  }

  @Override
  public PatientDto aggregate(Patient patient, Insurance insurance, Hospital hospital, BigDecimal estPrice) {

    return new PatientDto(patient, insurance, hospital, estPrice);
  }


  public BigDecimal calculatePrice(BigDecimal avgPrice, Discount discount) {
    return avgPrice.subtract(discount.getDiscountDollars())
      .multiply(HUNDRED_PCT.subtract(discount.getDiscountPercent()))
      .divide(HUNDRED_PCT, RoundingMode.HALF_EVEN)
      .max(BigDecimal.ZERO)
      .setScale(4, RoundingMode.UP);
  }
}
