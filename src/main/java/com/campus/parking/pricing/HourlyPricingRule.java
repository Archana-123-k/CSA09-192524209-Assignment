package com.campus.parking.pricing;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class HourlyPricingRule implements PricingRule {
    private final BigDecimal hourlyRate;

    public HourlyPricingRule(BigDecimal hourlyRate) {
        this.hourlyRate = Objects.requireNonNull(hourlyRate);
    }

    @Override
    public BigDecimal calculate(LocalDateTime entry, LocalDateTime exit) {
        long seconds = Math.max(0, Duration.between(entry, exit).getSeconds());
        long startedHours = Math.max(1, (seconds + 3599) / 3600);
        return hourlyRate.multiply(BigDecimal.valueOf(startedHours));
    }
}