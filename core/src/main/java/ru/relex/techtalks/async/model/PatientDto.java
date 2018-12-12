package ru.relex.techtalks.async.model;

import java.math.BigDecimal;

public class PatientDto {

  private Patient patient;
  private Insurance insurance;
  private Hospital hospital;
  private BigDecimal pricing;

  public PatientDto() {
  }

  public PatientDto(
    Patient patient, Insurance insurance, Hospital hospital, BigDecimal pricing) {
    this.patient = patient;
    this.insurance = insurance;
    this.hospital = hospital;
    this.pricing = pricing;
  }

  public PatientDto(Patient patient) {
    this.patient = patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public void setInsurance(Insurance insurance) {
    this.insurance = insurance;
  }

  public PatientDto withInsurance(Insurance insurance) {
    this.insurance = insurance;
    return this;
  }

  public void setHospital(Hospital hospital) {
    this.hospital = hospital;
  }

  public void setPricing(BigDecimal pricing) {
    this.pricing = pricing;
  }

  public Patient getPatient() {
    return patient;
  }

  public Insurance getInsurance() {
    return insurance;
  }

  public Hospital getHospital() {
    return hospital;
  }

  public BigDecimal getPricing() {
    return pricing;
  }
}
