package ru.relex.techtalks.async.quasar.service;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.fibers.futures.AsyncCompletionStage;
import co.paralleluniverse.fibers.futures.AsyncListenableFuture;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.relex.techtalks.async.mapper.HospitalMapper;
import ru.relex.techtalks.async.mapper.IPatientMapper;
import ru.relex.techtalks.async.model.*;
import ru.relex.techtalks.async.service.impl.AbstractPatientService;

@Service
public class PatientService extends AbstractPatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

  private ExecutorService jdbcPool;
  private HttpClient CLIENT = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_1_1)
    .build();

  @Autowired
  public PatientService(IPatientMapper mapper,
                        ObjectMapper objectMapper,
                        @Qualifier("JDBC_POOL") ExecutorService jdbcPool,
                        HospitalMapper hospitalMapper) {
    super(mapper, objectMapper, hospitalMapper);
    this.jdbcPool = jdbcPool;
  }

  @Suspendable
  public String ping1() throws SuspendExecution, InterruptedException {
    logger.info("ping1 - service");
    Fiber.sleep(1000);
    logger.info("ping1 complete - service");
    return "sleep1";
  }

  @Suspendable
  public String ping2() throws SuspendExecution, InterruptedException {
    logger.info("ping2 - service");
    Fiber.sleep(2000);
    logger.info("ping2 complete - service");
    return "sleep2";
  }

  @Override
  public PatientDto retrievePatientData(long id, String requestCat, String loc) {
    try {
      logger.info("Retrieve patient data");
      Fiber<Patient> patientTask = findPatientSuspendable(id).start();
      Fiber<List<Hospital>> hospitalsTask = findNearestHospitalsSuspend(loc).start();

      logger.info("Data requested");

      Patient patient = patientTask.get();

      logger.info("Patient received {}", patient);

      Fiber<Insurance> insuranceTask = findPatientInsuranceSuspend(patient.getExtId()).start();
      Fiber<Long> historyTask = findPatientHistorySuspend(patient.getId()).start();

      Insurance insurance = insuranceTask.get();
      Discount discount = calculateDiscount(historyTask.get(), insurance);
      Hospital hospital = getWeightedHospitalPricing(hospitalsTask.get(), discount);

      Fiber<BigDecimal> priceTask = getCategoryAvgPriceSuspend(requestCat, hospital.getTitle()).start();

      logger.info("External data recieved");

      BigDecimal finalPrice = calculatePrice(priceTask.get(), discount);

      PatientDto aggregate = aggregate(patient, insurance, hospital, finalPrice);
      logger.info("Produced data: {}", aggregate);
      return aggregate;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Suspendable
  private Fiber<Patient> findPatientSuspendable(long id) {
    return new Fiber<>() {
      @Override
      protected Patient run() throws SuspendExecution, InterruptedException {
        CompletableFuture<Patient> future = CompletableFuture
          .supplyAsync(() -> {
            logger.info("Request patient");
            return patientMapper.getById(id);
          }, jdbcPool);

        try {
          return AsyncCompletionStage.get(future);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Suspendable
  private Fiber<List<Hospital>> findNearestHospitalsSuspend(String loc) {
    return new Fiber<>() {
      @Override
      protected List<Hospital> run() throws SuspendExecution, InterruptedException {
        CompletableFuture<List<Hospital>> future = findNearestHospitalsAsync(loc);
        try {
          return AsyncCompletionStage.get(future);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Suspendable
  private Fiber<Insurance> findPatientInsuranceSuspend(String insuranceId) {
    return new Fiber<>() {
      @Override
      protected Insurance run() throws SuspendExecution, InterruptedException {
        CompletableFuture<Insurance> future = findPatientInsuranceAsync(insuranceId);

        try {
          return AsyncCompletionStage.get(future);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Suspendable
  private Fiber<Long> findPatientHistorySuspend(long userId) {
    return new Fiber<>() {
      @Override
      protected Long run() throws SuspendExecution, InterruptedException {
        CompletableFuture<Long> future = hospitalMapper.requestCountFuture(userId);

        try {
          return AsyncCompletionStage.get(future);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Suspendable
  private Fiber<BigDecimal> getCategoryAvgPriceSuspend(String cat, String hospitalName) {
    return new Fiber<>() {
      @Override
      protected BigDecimal run() throws SuspendExecution, InterruptedException {
        CompletableFuture<BigDecimal> future = hospitalMapper.getCategoryAvgPriceFuture(cat, hospitalName);

        try {
          return AsyncCompletionStage.get(future);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private List<Hospital> parseHospitals(String response) throws IOException {
    return objectMapper.readValue(response, new TypeReference<List<Hospital>>() {
    });
  }
}
