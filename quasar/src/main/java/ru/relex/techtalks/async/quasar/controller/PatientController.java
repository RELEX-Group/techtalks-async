package ru.relex.techtalks.async.quasar.controller;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.relex.techtalks.async.model.PatientDto;
import ru.relex.techtalks.async.quasar.service.PatientService;

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

    @Suspendable
    @GetMapping("/ping")
    public String ping() throws Exception {
        return "ok";
    }

    @GetMapping("/patient/{id}/data")
    @Suspendable
    public PatientDto getPatientData(
            @PathVariable("id") Long id,
            @RequestParam("category") String category,
            @RequestParam("location") String location
    ) throws ExecutionException, InterruptedException {
        try {
            return patientService.retrievePatientData(id, category, location);
        } catch (Exception e) {
            logger.error("Error during patient data retrieving", e);
            throw e;
        }
    }
}
