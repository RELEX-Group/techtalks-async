package ru.relex.techtalks.async.model;

import java.math.BigDecimal;
import org.bson.codecs.pojo.annotations.BsonId;

public class History {

  @BsonId
  private String recordId;

  private String hospitalName;
  private long patientId;
  private String category;
  private long requestDate;
  private BigDecimal cost;

  public History(String recordId, String hospitalName, long patientId, String category, long requestDate,
      BigDecimal cost) {
    this.recordId = recordId;
    this.hospitalName = hospitalName;
    this.patientId = patientId;
    this.category = category;
    this.requestDate = requestDate;
    this.cost = cost;
  }

  public History() {
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(final String recordId) {
    this.recordId = recordId;
  }

  public long getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(final long requestDate) {
    this.requestDate = requestDate;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(final BigDecimal cost) {
    this.cost = cost;
  }

  public String getHospitalName() {
    return hospitalName;
  }

  public void setHospitalName(String hospitalName) {
    this.hospitalName = hospitalName;
  }

  public long getPatientId() {
    return patientId;
  }

  public void setPatientId(long patientId) {
    this.patientId = patientId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
}
