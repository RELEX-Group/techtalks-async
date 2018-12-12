package ru.relex.techtalks.async.kotlin.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.relex.techtalks.async.kotlin.service.PatientService
import ru.relex.techtalks.async.model.PatientDto

@RestController
@RequestMapping("/api")
class PatientController @Autowired() constructor(
  private val patientService: PatientService
) {

  private val logger = LoggerFactory.getLogger(PatientController::class.java)

  @GetMapping("/ping")
  suspend fun ping(): String {
    return "ok"
  }

  @GetMapping("/patient/{id}/data")
  suspend fun getPatientData(
    @PathVariable("id") id: Long,
    @RequestParam("category") category: String,
    @RequestParam("location") location: String
  ): PatientDto {
    try {
      return patientService.retrievePatientDataAsync(id, category, location)
    } catch (e: Exception) {
      logger.error("Error during patient data retrieving", e)
      throw e
    }
  }
}
