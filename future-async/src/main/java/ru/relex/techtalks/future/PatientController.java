package ru.relex.techtalks.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.relex.techtalks.async.model.PatientDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Nikita Skornyakov
 * @date 05.12.2018
 */
@RestController
@RequestMapping("/api")
public class PatientController {

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  private Logger logger = LoggerFactory.getLogger(PatientController.class);

  private final PatientService patientService;

  @GetMapping("/ping")
  public Future<String> ping() {
    return CompletableFuture.completedFuture("ok");
  }

  @GetMapping("/patient/{id}/data")
  public Future<PatientDto> findDto(
    @RequestParam("location") String location,
    @PathVariable("id") long userId,
    @RequestParam("category") String category
  ) {
    logger.info("Request");
    return patientService.retrievePatientDataAsync(userId, category, location).thenApply(data -> {
      logger.info("Received data");
      return data;
    });
  }
}
