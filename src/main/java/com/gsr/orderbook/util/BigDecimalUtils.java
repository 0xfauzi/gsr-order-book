package com.gsr.orderbook.util;

import java.math.BigDecimal;

public class BigDecimalUtils {

    public static boolean greaterThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    public static boolean equal(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }

    public static boolean lessThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) < 0;

    }
}
