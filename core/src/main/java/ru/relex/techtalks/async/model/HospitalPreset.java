package ru.relex.techtalks.async.model;

import java.math.BigDecimal;
import java.util.Random;

/**
 * @author Nikita Skornyakov
 * @date 04.12.2018
 */
public enum HospitalPreset {

  HOSPITAL1("CAT1", "CAT2", "CAT3"),
  DR_REF_H2("CAT2", "CAT3"),
  EX_HOSP("CAT2"),
  REEE("CAT3", "CAT1"),
  BACK_S_H("CAT1", "CAT4");

  private final String[] categories;

  HospitalPreset(String... categories) {
    this.categories = categories;
  }

  public String[] getCategories() {
    return categories;
  }

  public BigDecimal nextPrice() {
    return BigDecimal.valueOf(new Random().nextInt(2500) + 500);
  }
}
