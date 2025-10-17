package com.cafepos.order;

import java.util.concurrent.atomic.AtomicLong;
public final class OrderIds {
    private static final AtomicLong SEQ = new AtomicLong(2000);
    public static long next() { return SEQ.incrementAndGet(); }
}
