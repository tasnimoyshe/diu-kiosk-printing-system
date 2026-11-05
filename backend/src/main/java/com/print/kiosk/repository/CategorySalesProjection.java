package com.print.kiosk.repository;

import java.math.BigDecimal;

public interface CategorySalesProjection {

    String getLabel();

    Long getNetQuantity();

    BigDecimal getNetAmount();
}