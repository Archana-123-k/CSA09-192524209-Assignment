package com.campus.parking.api;

import com.campus.parking.domain.PassType;
import com.campus.parking.domain.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ApiModels {
    private ApiModels() { }

    public record EntryRequest(String registrationNumber, VehicleType vehicleType, Long userId) { }
    public record ExitRequest(String ticketId, String registrationNumber, BigDecimal kwh, String paymentMethod) { }
    public record UserRequest(String fullName, String email, String category) { }
    public record PassRequest(Long userId, PassType passType, LocalDateTime validFrom, LocalDateTime validTo) { }
    public record VehicleView(String registrationNumber, VehicleType vehicleType, Long userId) { }
    public record SlotView(long id, int floor, int number, String type, boolean occupied, String registrationNumber) { }
    public record TicketView(String ticketId, String registrationNumber, VehicleType vehicleType,
                             long slotId, int floor, int slotNumber, LocalDateTime entryTime,
                             LocalDateTime exitTime, Long durationMinutes, BigDecimal parkingFee,
                             BigDecimal chargingFee, BigDecimal totalFee, boolean passApplied, String status) { }
    public record UserView(long id, String fullName, String email, String category) { }
    public record PassView(long id, long userId, PassType passType, LocalDateTime validFrom,
                           LocalDateTime validTo, boolean active) { }
    public record Dashboard(long totalSlots, long availableSlots, long occupiedSlots, long activeTickets,
                            BigDecimal revenueToday, long totalVehicles) { }
    public record Report(long ticketsToday, long exitsToday, BigDecimal revenueToday,
                         long averageDurationMinutes, long activeTickets) { }
    public record ApiError(String message) { }
}