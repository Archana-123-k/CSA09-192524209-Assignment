package com.campus.parking.pricing;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class DeliveryVanPricingRule implements PricingRule {
    private final BigDecimal carRate;

    public DeliveryVanPricingRule(BigDecimal carRate) {
        this.carRate = carRate;
    }

    @Override
    public BigDecimal calculate(LocalDateTime entry, LocalDateTime exit) {
        long seconds = Math.max(0, Duration.between(entry, exit).getSeconds());
        long startedHours = Math.max(1, (seconds + 3599) / 3600);
        BigDecimal total = BigDecimal.ZERO;
        for (long hour = 0; hour < startedHours; hour++) {
            LocalDateTime segmentStart = entry.plusHours(hour);
            int time = segmentStart.getHour();
            boolean peak = time >= 9 && time < 17;
            total = total.add(carRate.multiply(BigDecimal.valueOf(peak ? 2 : 1)));
        }
        return total;
    }
}