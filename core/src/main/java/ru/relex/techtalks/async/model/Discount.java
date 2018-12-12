package ru.relex.techtalks.async.model;

import java.math.BigDecimal;

/**
 * @author Nikita Skornyakov
 */
public class Discount {

  private BigDecimal discountPercent;
  private BigDecimal discountDollars;

  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }

  public BigDecimal getDiscountDollars() {
    return discountDollars;
  }

  public void setDiscountDollars(BigDecimal discountDollars) {
    this.discountDollars = discountDollars;
  }
}
