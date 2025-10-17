package com.cafepos.checkout;

import com.cafepos.common.Money;

public interface TaxPolicy { Money taxOn(Money amount); }