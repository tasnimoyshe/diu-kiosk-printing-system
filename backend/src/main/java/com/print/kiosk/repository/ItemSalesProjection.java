package com.print.kiosk.repository;

import java.math.BigDecimal;

public interface ItemSalesProjection {

    String getLabel();

    Long getNetQuantity();

    BigDecimal getNetAmount();
}