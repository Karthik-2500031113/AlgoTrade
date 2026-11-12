
package com.algotrade.util;

import java.math.BigDecimal;

public final class Money {

    private Money() {}

    public static BigDecimal requireNonNegative(
            BigDecimal amount, String fieldName) {

        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                fieldName + " cannot be null or negative"
            );
        }

        return amount;
    }

    public static BigDecimal requirePositive(
            BigDecimal amount, String fieldName) {

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                fieldName + " must be positive"
            );
        }

        return amount;
    }
}
