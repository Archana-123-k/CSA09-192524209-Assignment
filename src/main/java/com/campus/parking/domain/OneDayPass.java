package com.campus.parking.domain;

import java.time.Duration;
import java.time.LocalDateTime;

public final class OneDayPass implements Pass {
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;

    public OneDayPass(LocalDateTime validFrom, LocalDateTime validTo) {
        if (Duration.between(validFrom, validTo).compareTo(Duration.ofHours(24)) > 0) {
            throw new IllegalArgumentException("A one-day pass cannot be longer than 24 hours.");
        }
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @Override
    public boolean covers(LocalDateTime visitStart, LocalDateTime visitEnd) {
        return !visitStart.isBefore(validFrom) && !visitEnd.isAfter(validTo);
    }
}