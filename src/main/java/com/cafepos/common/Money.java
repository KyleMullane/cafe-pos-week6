package com.cafepos.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private final BigDecimal amount;

    public static Money of(double value) { return new Money(BigDecimal.valueOf(value)); }
    public static Money of(BigDecimal value) { return new Money(value); }
    public static Money zero() { return new Money(BigDecimal.ZERO); }

    private Money(BigDecimal a) {
        if (a == null) throw new IllegalArgumentException("amount required");
        this.amount = a.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        if (other == null) throw new IllegalArgumentException("other required");
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        return new Money(this.amount.multiply(BigDecimal.valueOf(qty)));
    }

    public BigDecimal asBigDecimal() { return amount; }

    @Override public int compareTo(Money o) { return this.amount.compareTo(o.amount); }
    @Override public boolean equals(Object o) { return (o instanceof Money m) && amount.equals(m.amount); }
    @Override public int hashCode() { return Objects.hash(amount); }
    @Override public String toString() { DecimalFormat df = new DecimalFormat("0.00");
        return df.format(amount); }
}
