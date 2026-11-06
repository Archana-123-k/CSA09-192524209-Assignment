package com.campus.parking.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingRuleTest {
    @Test
    void hourlyRuleChargesEachStartedHourAndHasOneHourMinimum() {
        PricingRule rule = new HourlyPricingRule(BigDecimal.TEN);
        LocalDateTime start = LocalDateTime.of(2026, 11, 6, 8, 0);
        assertFee(rule, start, start, "10.00");
        assertFee(rule, start, start.plusMinutes(59), "10.00");
        assertFee(rule, start, start.plusMinutes(60), "10.00");
        assertFee(rule, start, start.plusMinutes(60).plusSeconds(1), "20.00");
        assertFee(rule, start, start.plusMinutes(61), "20.00");
        assertFee(rule, start, start.plusMinutes(120), "20.00");
        assertFee(rule, start, start.plusMinutes(121), "30.00");
    }

    @Test
    void deliveryVanDoublesCarRateOnlyDuringBusinessHours() {
        PricingRule rule = new DeliveryVanPricingRule(BigDecimal.valueOf(30));
        LocalDateTime beforeBusinessHours = LocalDateTime.of(2026, 11, 6, 8, 0);
        LocalDateTime peakHour = LocalDateTime.of(2026, 11, 6, 9, 0);
        LocalDateTime afterBusinessHours = LocalDateTime.of(2026, 11, 6, 17, 0);
        assertFee(rule, beforeBusinessHours, beforeBusinessHours.plusHours(1), "30.00");
        assertFee(rule, peakHour, peakHour.plusHours(1), "60.00");
        assertFee(rule, afterBusinessHours, afterBusinessHours.plusHours(1), "30.00");
    }

    private void assertFee(PricingRule rule, LocalDateTime start, LocalDateTime end, String expected) {
        BigDecimal actual = rule.calculate(start, end).setScale(2);
        System.out.printf("Expected ₹%s; actual ₹%s%n", expected, actual);
        assertEquals(new BigDecimal(expected), actual);
    }
}