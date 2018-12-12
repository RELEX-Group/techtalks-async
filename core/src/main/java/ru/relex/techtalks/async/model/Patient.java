package ru.relex.techtalks.async.model;

import java.util.List;

public class Patient {

  private long id;
  private String firstName;
  private String lastName;
  private char gender;
  private String dateOfBirth;
  private String extId;
  private Insurance insurance;
  private List<History> requests;

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public char getGender() {
    return gender;
  }

  public void setGender(final char gender) {
    this.gender = gender;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(final String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(final String extId) {
    this.extId = extId;
  }

  public Insurance getInsurance() {
    return insurance;
  }

  public void setInsurance(final Insurance insurance) {
    this.insurance = insurance;
  }

  public List<History> getRequests() {
    return requests;
  }

  public void setRequests(final List<History> requests) {
    this.requests = requests;
  }

  @Override
  public String toString() {
    return "Patient{" +
      "id=" + id +
      ", firstName='" + firstName + '\'' +
      ", lastName='" + lastName + '\'' +
    ", gender=" + gender +
      ", dateOfBirth='" + dateOfBirth + '\'' +
      ", extId='" + extId + '\'' +
      ", insurance=" + insurance +
      ", requests=" + requests +
      '}';
  }
}
