package ru.relex.techtalks.async.rx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.relex.techtalks.async.model.PatientDto;
import ru.relex.techtalks.async.rx.service.PatientService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequestMapping("/api")
@RestController
public class PatientController {
  private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

  private final PatientService patientService;

  @Autowired
  public PatientController(PatientService documentService) {
    this.patientService = documentService;
  }

  @GetMapping("/ping")
  public Mono<String> ping() {
    return Mono.just("ok");
  }

  @GetMapping("/patient/{id}/data")
  public Mono<PatientDto> getPatientData(
    @PathVariable("id") Long id,
    @RequestParam("category") String category,
    @RequestParam("location") String location
  ) throws ExecutionException, InterruptedException {
    try {
      return patientService.retrievePatientDataAsync(id, category, location)
        .doOnError(e -> logger.error("Exception", e));
    } catch (Exception e) {
      logger.error("Exception in sync", e);
      throw e;
    }
  }
}
