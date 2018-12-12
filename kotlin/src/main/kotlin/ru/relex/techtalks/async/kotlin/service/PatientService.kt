package ru.relex.techtalks.async.kotlin.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kotlin.experimental.coroutine.web.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.web.coroutine.function.client.CoroutineWebClient
import org.springframework.web.coroutine.function.client.body
import org.springframework.web.reactive.function.client.WebClient
import ru.relex.techtalks.async.kotlin.repository.PatientRepository
import ru.relex.techtalks.async.mapper.HospitalMapper
import ru.relex.techtalks.async.mapper.IPatientMapper
import ru.relex.techtalks.async.model.Hospital
import ru.relex.techtalks.async.model.Insurance
import ru.relex.techtalks.async.model.PatientDto
import ru.relex.techtalks.async.service.impl.AbstractPatientService
import java.lang.IllegalStateException
import java.math.BigDecimal

@Service
class PatientService @Autowired() constructor(
  patientMapper: IPatientMapper,
  objectMapper: ObjectMapper,
  private val patientRepository: PatientRepository,
  hospitalMapper: HospitalMapper,
  @Value("\${insurance.uri}") private val insuranceUri2: String,
  @Value("\${hospital.lookup.uri}") private val hospitalLookupUri2: String
) : AbstractPatientService(patientMapper, objectMapper, hospitalMapper) {
  private val logger = LoggerFactory.getLogger(PatientService::class.java)

  private val coroutineWebClient = CoroutineWebClient.create(insuranceUri2)
  private val webClient = WebClient.create(hospitalLookupUri2)

  @Deprecated(
    "Deprecated, use async implementation instead",
    ReplaceWith("retrievePatientDataAsync(id, requestCat, loc)")
  )
  override fun retrievePatientData(id: Long, requestCat: String, loc: String): PatientDto {
    return runBlocking {
      retrievePatientDataAsync(id, requestCat, loc)
    }
  }

  suspend fun retrievePatientDataAsync(id: Long,
                                       requestCat: String,
                                       loc: String): PatientDto = coroutineScope {
    logger.info("Retrieve patient data")
    val patientTask = async { patientRepository.getById(id) }
    val hospitalsTask = async { findNearestHospitalsSuspend(loc) }

    logger.info("Data requested")

    val patient = patientTask.await()
    val insuranceTask = async { findPatientInsuranceSuspend(patient.extId) }
    val historyTask = async { findPatientHistorySuspend(patient.id) }

    val insurance = insuranceTask.await()
    val discount = calculateDiscount(historyTask.await(), insurance)
    val hospital = getWeightedHospitalPricing(hospitalsTask.await(), discount)

    val priceTask = async { findCategoryAvgPriceSuspend(requestCat, hospital.title) }
    val resultPrice = calculatePrice(priceTask.await(), discount)

    logger.info("External data recieved")

    aggregate(patient, insurance, hospital, resultPrice).also {
      logger.info("Produced data: {}", it)
    }
  }

  private suspend fun findPatientInsuranceSuspend(insuranceId: String): Insurance {
    logger.info("Insurance request")
    return coroutineWebClient
      .get()
      .uri("?iid=$insuranceId")
      .retrieve()
      .body() ?: throw IllegalArgumentException("Patient does not have insurance")
  }

  private suspend fun findNearestHospitalsSuspend(loc: String): List<Hospital>? {
    logger.info("Hospital request")
    return webClient
      .get()
      .uri("?loc=$loc")
      .retrieve()
      .bodyToFlux(Hospital::class.java)
      .collectList()
      .awaitFirstOrNull() ?: throw IllegalArgumentException("Patient does not have nearest hospitals")
  }

  private suspend fun findPatientHistorySuspend(userId: Long): Long {
    logger.info("History request")
    return hospitalMapper.requestCountAsync(userId)
      .awaitFirstOrDefault(0).also {
        logger.info("History response {}", 0)
      }
  }

  private suspend fun findCategoryAvgPriceSuspend(cat: String, hospitalName: String): BigDecimal? {
    logger.info("Price request")
    return hospitalMapper.getCategoryAvgPriceAsync(cat, hospitalName)
      .awaitFirstOrNull()
  }
}
