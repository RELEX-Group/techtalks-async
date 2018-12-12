package ru.relex.techtalks.simple.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.relex.techtalks.async.model.PatientDto;
import ru.relex.techtalks.simple.service.PatientService;

import java.util.concurrent.ExecutionException;

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
  public String ping() {
    return "ok";
  }

  @GetMapping("/patient/{id}/data")
  public PatientDto getPatientData(
    @PathVariable("id") Long id,
    @RequestParam("category") String category,
    @RequestParam("location") String location
  ) {
    try {
      return patientService.retrievePatientData(id, category, location);
    } catch (Exception e) {
      logger.error("Exception in sync", e);
      throw e;
    }
  }
}
