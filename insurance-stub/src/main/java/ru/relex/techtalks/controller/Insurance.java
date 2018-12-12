package ru.relex.techtalks.controller;

public class Insurance {

  private String insuranceId;
  private Long issueDate;
  private Long expirationDate;
  private String status;
  private String insuranceCategory;

  public String getInsuranceId() {
    return insuranceId;
  }

  public void setInsuranceId(final String insuranceId) {
    this.insuranceId = insuranceId;
  }

  public Long getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(final Long issueDate) {
    this.issueDate = issueDate;
  }

  public Long getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(final Long expirationDate) {
    this.expirationDate = expirationDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public String getInsuranceCategory() {
    return insuranceCategory;
  }

  public void setInsuranceCategory(String insuranceCategory) {
    this.insuranceCategory = insuranceCategory;
  }
}
