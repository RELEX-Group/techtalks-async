package ru.relex.techtalks.async.kotlin.repository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import ru.relex.techtalks.async.mapper.IPatientMapper
import ru.relex.techtalks.async.model.Patient

@Repository
@Coroutine("JDBC_COROUTINE")
class PatientRepository @Autowired() constructor(
  private val patientMapper: IPatientMapper
) {
  private val logger = LoggerFactory.getLogger(PatientRepository::class.java)

  suspend fun getById(id: Long): Patient {
    logger.info("Get patient by id {}", id)
    return patientMapper.getById(id).also {
      logger.info("Found patient {}", id)
    }
  }
}
