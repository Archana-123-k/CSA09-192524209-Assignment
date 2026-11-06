package com.campus.parking.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PricingRule {
    BigDecimal calculate(LocalDateTime entry, LocalDateTime exit);
}