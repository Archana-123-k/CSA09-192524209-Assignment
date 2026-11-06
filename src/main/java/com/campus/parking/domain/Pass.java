package com.campus.parking.domain;

import java.time.LocalDateTime;

public interface Pass {
    boolean covers(LocalDateTime visitStart, LocalDateTime visitEnd);
}