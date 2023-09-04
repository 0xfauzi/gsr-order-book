package com.oxfauzi.orderbook.domain;

import java.math.BigDecimal;

public class Constants {

    public static final Order POISON_ORDER = new Order("POISON", BigDecimal.ZERO, BigDecimal.ZERO);
    public static final Level EMPTY_LEVEL = new Level(BigDecimal.ZERO);
}
