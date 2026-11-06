package com.campus.parking.domain;

import java.time.LocalDateTime;

public final class MonthlyPass implements Pass {
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;

    public MonthlyPass(LocalDateTime validFrom, LocalDateTime validTo) {
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @Override
    public boolean covers(LocalDateTime visitStart, LocalDateTime visitEnd) {
        return !visitStart.isBefore(validFrom) && !visitEnd.isAfter(validTo);
    }
}