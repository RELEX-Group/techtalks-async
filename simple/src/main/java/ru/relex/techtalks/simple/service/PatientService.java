package ru.relex.techtalks.simple.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.relex.techtalks.async.mapper.HospitalMapper;
import ru.relex.techtalks.async.mapper.IPatientMapper;
import ru.relex.techtalks.async.model.*;
import ru.relex.techtalks.async.service.impl.AbstractPatientService;

@Service
public class PatientService extends AbstractPatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

  @Autowired
  public PatientService(IPatientMapper mapper,
      ObjectMapper objectMapper,
      HospitalMapper hospitalMapper) {
    super(mapper, objectMapper, hospitalMapper);
  }

  @Override
  public PatientDto retrievePatientData(long id, String requestCat, String loc) {
    logger.info("Retrieve patient data");

    Patient patient = patientMapper.getById(id);

    logger.info("Patient received");

    List<Hospital> hospitals = findNearestHospitals(loc);

    logger.info("Hospital found");

    long val = hospitalMapper.requestCount(id);

    logger.info("Request count calculated");

    Insurance insurance = findPatientInsurance(patient.getExtId());

    logger.info("Insurance found");

    Discount discount = calculateDiscount(val, insurance);

    logger.info("Discount calculated");

    Hospital hospital = getWeightedHospitalPricing(hospitals, discount);

    logger.info("Hospital selected");

    BigDecimal bd = hospitalMapper.getCategoryAvgPrice(requestCat, hospital.getTitle());

    logger.info("Average price calculated");

    var cost = calculatePrice(bd, discount);

    logger.info("Final price calculated");

    PatientDto aggregate = aggregate(patient, insurance, hospital, cost);
    logger.info("Produced data: {}", aggregate);
    return aggregate;
  }
}
