package ru.relex.techtalks.async.model;

import java.math.BigDecimal;

/**
 * @author Nikita Skornyakov
 * @date 03.12.2018
 */
public enum InsuranceType {

  BASIC(BigDecimal.valueOf(100)),
  EXTENDED(BigDecimal.valueOf(250)),
  FULL(BigDecimal.valueOf(25_000));

  private final BigDecimal value;

  InsuranceType(BigDecimal value) {
    this.value = value;
  }

  public BigDecimal getValue() {
    return value;
  }
}


