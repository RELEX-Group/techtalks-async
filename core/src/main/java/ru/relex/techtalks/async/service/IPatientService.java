package ru.relex.techtalks.async.service;

import java.math.BigDecimal;
import java.util.List;
import ru.relex.techtalks.async.model.*;

public interface IPatientService {

  /**
   * Retrieves information about patient by its id. Process consists of the following steps.
   * <ul>
   * <li>Patient data is requested from internal system (to check whether information was presented or not)
   * <li>Patient insurance data requested from external service to check whether or not it covers his/her request
   * <li>Patient request history is queried to check whether there were previous requests to calculate possible
   * discount
   * <li>Patient location is queried to find Hospitals that can process the request
   * <li>Finally each hospital request history is queried to get estimate pricing of request
   * <li>Aggregated information passed to patient
   * </ul>
   *
   * @param id id of the user
   * @param requestCat type of request
   * @param loc location of user (used to Query external API)
   */
  PatientDto retrievePatientData(long id, String requestCat, String loc);

  /**
   * Finds patient information by his ID.
   */
  Patient findPatientById(long id);

  /**
   * Retrieves patient Insurance Data.
   */
  Insurance findPatientInsurance(String insuranceId);

  /**
   * Retrieves history of patients requests.
   */
  List<History> findPatientHistory(String userId);

  List<Hospital> findNearestHospitals(String loc);

  Discount calculateDiscount(long reqCount, Insurance insurance);

  Hospital getWeightedHospitalPricing(List<Hospital> historyList, Discount discount);

  PatientDto aggregate(Patient patient, Insurance insurance, Hospital hospital, BigDecimal estPrice);
}
