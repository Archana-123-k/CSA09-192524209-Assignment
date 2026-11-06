package com.campus.parking.service;

import com.campus.parking.api.ApiModels.*;
import com.campus.parking.domain.PassType;
import com.campus.parking.domain.Pass;
import com.campus.parking.domain.MonthlyPass;
import com.campus.parking.domain.OneDayPass;
import com.campus.parking.domain.Vehicle;
import com.campus.parking.domain.VehicleFactory;
import com.campus.parking.domain.VehicleType;
import com.campus.parking.pricing.PricingRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ParkingService implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    private final Map<VehicleType, PricingRule> pricingRules;
    private final int[] floorSlots;

    public ParkingService(JdbcTemplate jdbc, Map<VehicleType, PricingRule> pricingRules,
                          @Value("${parking.floor-slots:8,10,12,10,8}") String configuredFloorSlots) {
        this.jdbc = jdbc;
        this.pricingRules = pricingRules;
        this.floorSlots = parseFloorSlots(configuredFloorSlots);
    }

    @Override
    public void run(String... args) {
        int[][] slotLayout = new int[floorSlots.length][];
        for (int floorIndex = 0; floorIndex < floorSlots.length; floorIndex++) {
            slotLayout[floorIndex] = new int[floorSlots[floorIndex]];
            for (int slotIndex = 0; slotIndex < slotLayout[floorIndex].length; slotIndex++) {
                slotLayout[floorIndex][slotIndex] = slotIndex + 1;
            }
        }
        for (int floor = 1; floor <= floorSlots.length; floor++) {
            final int floorNumber = floor;
            int existing = jdbc.queryForObject("SELECT COUNT(*) FROM parking_slot WHERE floor_number = ?", Integer.class, floorNumber);
            if (existing == 0) {
                for (int number : slotLayout[floor - 1]) {
                    String type = number <= Math.max(1, floorSlots[floor - 1] / 3) ? "TWO_WHEELER" : "STANDARD";
                    jdbc.update("INSERT INTO parking_slot(floor_number, slot_number, slot_type, occupied) VALUES (?, ?, ?, FALSE)", floorNumber, number, type);
                }
            }
        }
    }

    private int[] parseFloorSlots(String values) {
        String[] parts = values.split(",");
        if (parts.length != 5) throw new IllegalArgumentException("Exactly five floor slot counts are required.");
        int[] counts = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            counts[i] = Integer.parseInt(parts[i].trim());
            if (counts[i] < 1) throw new IllegalArgumentException("Every floor must have at least one slot.");
        }
        return counts;
    }

    @Transactional
    public TicketView enter(EntryRequest request) {
        String registration = normalizeRegistration(request.registrationNumber());
        if (request.vehicleType() == null) throw new ParkingException("Select a valid vehicle type.");
        Vehicle vehicle = VehicleFactory.create(request.vehicleType(), registration);
        VehicleType vehicleType = vehicle.getVehicleType();
        if (request.userId() != null && !exists("SELECT COUNT(*) FROM parking_user WHERE id = ?", request.userId())) {
            throw new ParkingException("The selected user does not exist.");
        }
        if (exists("SELECT COUNT(*) FROM parking_ticket WHERE registration_number = ? AND status = 'ACTIVE'", registration)) {
            throw new ParkingException("This vehicle already has an active parking ticket.");
        }

        Integer vehicleExists = jdbc.queryForObject("SELECT COUNT(*) FROM vehicle WHERE registration_number = ?", Integer.class, registration);
        if (vehicleExists != null && vehicleExists > 0) {
            jdbc.update("UPDATE vehicle SET vehicle_type = ?, user_id = ? WHERE registration_number = ?",
                    vehicleType.name(), request.userId(), registration);
        } else {
            jdbc.update("INSERT INTO vehicle(registration_number, vehicle_type, user_id) VALUES (?, ?, ?)",
                    registration, vehicleType.name(), request.userId());
        }

        String preferredSlotType = vehicleType == VehicleType.TWO_WHEELER || vehicleType == VehicleType.E_BIKE
                ? "TWO_WHEELER" : "STANDARD";
        List<Long> slots = jdbc.query("SELECT id FROM parking_slot WHERE occupied = FALSE AND slot_type = ? ORDER BY floor_number, slot_number LIMIT 1 FOR UPDATE",
                (rs, row) -> rs.getLong("id"), preferredSlotType);
        if (slots.isEmpty()) throw new ParkingException("No available " + preferredSlotType.toLowerCase().replace('_', ' ') + " parking slot.");

        long slotId = slots.get(0);
        int changed = jdbc.update("UPDATE parking_slot SET occupied = TRUE, vehicle_registration = ? WHERE id = ? AND occupied = FALSE", registration, slotId);
        if (changed != 1) throw new ParkingException("The selected slot was just taken. Please try again.");
        String ticketId = UUID.randomUUID().toString();
        LocalDateTime entry = LocalDateTime.now();
        jdbc.update("INSERT INTO parking_ticket(ticket_id, registration_number, slot_id, user_id, entry_time, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')",
                ticketId, registration, slotId, request.userId(), Timestamp.valueOf(entry));
        return ticket(ticketId);
    }

    @Transactional
    public TicketView exit(ExitRequest request) {
        if (request == null || ((request.ticketId() == null || request.ticketId().isBlank())
                && (request.registrationNumber() == null || request.registrationNumber().isBlank()))) {
            throw new ParkingException("Enter a ticket ID or vehicle registration number.");
        }
        BigDecimal kwh = request.kwh() == null ? BigDecimal.ZERO : request.kwh();
        if (kwh.signum() < 0) throw new ParkingException("Charging units cannot be negative.");
        String where = request.ticketId() != null && !request.ticketId().isBlank() ? "t.ticket_id = ?" : "t.registration_number = ?";
        String key = request.ticketId() != null && !request.ticketId().isBlank()
                ? request.ticketId().trim() : normalizeRegistration(request.registrationNumber());
        List<TicketRow> rows = jdbc.query("SELECT t.*, v.vehicle_type, s.floor_number, s.slot_number FROM parking_ticket t " +
                        "JOIN vehicle v ON v.registration_number = t.registration_number JOIN parking_slot s ON s.id = t.slot_id " +
                        "WHERE " + where + " AND t.status = 'ACTIVE' FOR UPDATE", ticketRowMapper(), key);
        if (rows.isEmpty()) throw new ParkingException("No active parking ticket was found.");
        TicketRow current = rows.get(0);
        VehicleType vehicleType = VehicleType.valueOf(current.vehicleType);
        LocalDateTime exit = LocalDateTime.now();
        long minutes = Math.max(0, Duration.between(current.entryTime, exit).toMinutes());
        boolean passApplied = hasCoveringPass(current.userId, current.entryTime, exit);
        BigDecimal parkingFee = passApplied ? BigDecimal.ZERO : pricingRules.get(vehicleType).calculate(current.entryTime, exit);
        BigDecimal chargingFee = vehicleType == VehicleType.ELECTRIC_CAR
                ? kwh.multiply(BigDecimal.valueOf(12)) : BigDecimal.ZERO;
        if (vehicleType != VehicleType.ELECTRIC_CAR && kwh.signum() > 0) {
            throw new ParkingException("Charging units can only be entered for an electric car.");
        }
        BigDecimal total = parkingFee.add(chargingFee);
        jdbc.update("UPDATE parking_ticket SET exit_time = ?, duration_minutes = ?, parking_fee = ?, charging_fee = ?, total_fee = ?, pass_applied = ?, status = 'CLOSED' WHERE ticket_id = ?",
                Timestamp.valueOf(exit), minutes, parkingFee, chargingFee, total, passApplied, current.ticketId);
        jdbc.update("UPDATE parking_slot SET occupied = FALSE, vehicle_registration = NULL WHERE id = ?", current.slotId);
        String method = request.paymentMethod() == null || request.paymentMethod().isBlank() ? "CASH" : request.paymentMethod().trim().toUpperCase();
        jdbc.update("INSERT INTO payment_record(ticket_id, amount, payment_method) VALUES (?, ?, ?)", current.ticketId, total, method);
        return ticket(current.ticketId);
    }

    private boolean hasCoveringPass(Long userId, LocalDateTime entry, LocalDateTime exit) {
        if (userId == null) return false;
        List<Pass> passes = jdbc.query("SELECT pass_type, valid_from, valid_to FROM parking_pass WHERE user_id = ? AND active = TRUE",
            (rs, row) -> {
                LocalDateTime validFrom = rs.getTimestamp("valid_from").toLocalDateTime();
                LocalDateTime validTo = rs.getTimestamp("valid_to").toLocalDateTime();
                PassType type = PassType.valueOf(rs.getString("pass_type"));
                return type == PassType.ONE_DAY ? new OneDayPass(validFrom, validTo) : new MonthlyPass(validFrom, validTo);
            }, userId);
        return passes.stream().anyMatch(pass -> pass.covers(entry, exit));
    }

    @Transactional(readOnly = true)
    public Dashboard dashboard() {
        return new Dashboard(count("SELECT COUNT(*) FROM parking_slot"), count("SELECT COUNT(*) FROM parking_slot WHERE occupied = FALSE"),
                count("SELECT COUNT(*) FROM parking_slot WHERE occupied = TRUE"), count("SELECT COUNT(*) FROM parking_ticket WHERE status = 'ACTIVE'"),
                amount("SELECT COALESCE(SUM(amount), 0) FROM payment_record WHERE DATE(paid_at) = CURRENT_DATE"),
                count("SELECT COUNT(*) FROM vehicle"));
    }

    @Transactional(readOnly = true)
    public List<SlotView> slots() {
        return jdbc.query("SELECT * FROM parking_slot ORDER BY floor_number, slot_number", (rs, row) -> new SlotView(
                rs.getLong("id"), rs.getInt("floor_number"), rs.getInt("slot_number"), rs.getString("slot_type"),
                rs.getBoolean("occupied"), rs.getString("vehicle_registration")));
    }

    @Transactional(readOnly = true)
    public List<TicketView> history() {
        return jdbc.query("SELECT t.*, v.vehicle_type, s.floor_number, s.slot_number FROM parking_ticket t " +
                "JOIN vehicle v ON v.registration_number = t.registration_number JOIN parking_slot s ON s.id = t.slot_id ORDER BY t.entry_time DESC",
                ticketViewMapper());
    }

    @Transactional(readOnly = true)
    public Report report() {
        BigDecimal average = jdbc.queryForObject("SELECT COALESCE(AVG(duration_minutes), 0) FROM parking_ticket WHERE duration_minutes IS NOT NULL", BigDecimal.class);
        return new Report(count("SELECT COUNT(*) FROM parking_ticket WHERE DATE(entry_time) = CURRENT_DATE"),
                count("SELECT COUNT(*) FROM parking_ticket WHERE DATE(exit_time) = CURRENT_DATE"),
                amount("SELECT COALESCE(SUM(amount), 0) FROM payment_record WHERE DATE(paid_at) = CURRENT_DATE"),
            average == null ? 0 : average.longValue(),
                count("SELECT COUNT(*) FROM parking_ticket WHERE status = 'ACTIVE'"));
    }

    public List<VehicleView> vehicles() {
        return jdbc.query("SELECT * FROM vehicle ORDER BY registration_number", (rs, row) -> new VehicleView(
                rs.getString("registration_number"), VehicleType.valueOf(rs.getString("vehicle_type")), nullableLong(rs, "user_id")));
    }

    @Transactional
    public VehicleView saveVehicle(String registrationNumber, VehicleType type, Long userId) {
        String registration = normalizeRegistration(registrationNumber);
        if (type == null) throw new ParkingException("Select a valid vehicle type.");
        if (exists("SELECT COUNT(*) FROM parking_ticket WHERE registration_number = ? AND status = 'ACTIVE'", registration)) {
            throw new ParkingException("Exit the vehicle before updating its details.");
        }
        VehicleType resolvedType = VehicleFactory.create(type, registration).getVehicleType();
        if (userId != null && !exists("SELECT COUNT(*) FROM parking_user WHERE id = ?", userId)) throw new ParkingException("The selected user does not exist.");
        jdbc.update("INSERT INTO vehicle(registration_number, vehicle_type, user_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE vehicle_type = VALUES(vehicle_type), user_id = VALUES(user_id)",
                registration, resolvedType.name(), userId);
        return jdbc.queryForObject("SELECT * FROM vehicle WHERE registration_number = ?", (rs, row) -> new VehicleView(
                rs.getString("registration_number"), VehicleType.valueOf(rs.getString("vehicle_type")), nullableLong(rs, "user_id")), registration);
    }

    @Transactional
    public void deleteVehicle(String registrationNumber) {
        String registration = normalizeRegistration(registrationNumber);
        if (exists("SELECT COUNT(*) FROM parking_ticket WHERE registration_number = ? AND status = 'ACTIVE'", registration)) {
            throw new ParkingException("Exit the vehicle before deleting its record.");
        }
        jdbc.update("DELETE FROM vehicle WHERE registration_number = ?", registration);
    }

    public List<UserView> users() {
        return jdbc.query("SELECT * FROM parking_user ORDER BY full_name", (rs, row) -> new UserView(
                rs.getLong("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("category")));
    }

    @Transactional
    public UserView createUser(UserRequest request) {
        if (request == null || blank(request.fullName()) || blank(request.email()) || blank(request.category())) {
            throw new ParkingException("Name, email, and category are required.");
        }
        String category = request.category().trim().toUpperCase();
        if (!List.of("STUDENT", "STAFF", "VISITOR").contains(category)) throw new ParkingException("Category must be STUDENT, STAFF, or VISITOR.");
        jdbc.update("INSERT INTO parking_user(full_name, email, category) VALUES (?, ?, ?)", request.fullName().trim(), request.email().trim().toLowerCase(), category);
        return jdbc.queryForObject("SELECT * FROM parking_user WHERE email = ?", (rs, row) -> new UserView(
                rs.getLong("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("category")), request.email().trim().toLowerCase());
    }

    @Transactional
    public UserView updateUser(long id, UserRequest request) {
        String category = request.category() == null ? "" : request.category().trim().toUpperCase();
        if (blank(request.fullName()) || blank(request.email()) || !List.of("STUDENT", "STAFF", "VISITOR").contains(category)) {
            throw new ParkingException("Provide a name, email, and valid user category.");
        }
        if (jdbc.update("UPDATE parking_user SET full_name = ?, email = ?, category = ? WHERE id = ?",
                request.fullName().trim(), request.email().trim().toLowerCase(), category, id) == 0) throw new ParkingException("User not found.");
        return jdbc.queryForObject("SELECT * FROM parking_user WHERE id = ?", (rs, row) -> new UserView(
                rs.getLong("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("category")), id);
    }

    @Transactional
    public void deleteUser(long id) {
        jdbc.update("DELETE FROM parking_user WHERE id = ?", id);
    }

    public List<PassView> passes() {
        return jdbc.query("SELECT * FROM parking_pass ORDER BY valid_to DESC", passMapper());
    }

    @Transactional
    public PassView createPass(PassRequest request) {
        if (request == null || request.userId() == null || request.passType() == null || request.validFrom() == null || request.validTo() == null
                || !request.validTo().isAfter(request.validFrom())) throw new ParkingException("Provide a user, pass type, and valid start/end dates.");
        if (!exists("SELECT COUNT(*) FROM parking_user WHERE id = ?", request.userId())) throw new ParkingException("The selected user does not exist.");
        if (request.passType() == PassType.ONE_DAY && Duration.between(request.validFrom(), request.validTo()).compareTo(Duration.ofHours(24)) > 0) {
            throw new ParkingException("A one-day pass cannot be longer than 24 hours.");
        }
        jdbc.update("INSERT INTO parking_pass(user_id, pass_type, valid_from, valid_to, active) VALUES (?, ?, ?, ?, TRUE)",
                request.userId(), request.passType().name(), Timestamp.valueOf(request.validFrom()), Timestamp.valueOf(request.validTo()));
        Long id = jdbc.queryForObject("SELECT MAX(id) FROM parking_pass", Long.class);
        return jdbc.queryForObject("SELECT * FROM parking_pass WHERE id = ?", passMapper(), id);
    }

    @Transactional
    public void deletePass(long id) {
        jdbc.update("DELETE FROM parking_pass WHERE id = ?", id);
    }

    @Transactional
    public PassView updatePass(long id, PassRequest request) {
        if (request == null || request.userId() == null || request.passType() == null || request.validFrom() == null || request.validTo() == null
                || !request.validTo().isAfter(request.validFrom())) throw new ParkingException("Provide a user, pass type, and valid start/end dates.");
        if (!exists("SELECT COUNT(*) FROM parking_user WHERE id = ?", request.userId())) throw new ParkingException("The selected user does not exist.");
        if (request.passType() == PassType.ONE_DAY && Duration.between(request.validFrom(), request.validTo()).compareTo(Duration.ofHours(24)) > 0) {
            throw new ParkingException("A one-day pass cannot be longer than 24 hours.");
        }
        if (jdbc.update("UPDATE parking_pass SET user_id = ?, pass_type = ?, valid_from = ?, valid_to = ?, active = TRUE WHERE id = ?",
                request.userId(), request.passType().name(), Timestamp.valueOf(request.validFrom()), Timestamp.valueOf(request.validTo()), id) == 0) {
            throw new ParkingException("Pass not found.");
        }
        return jdbc.queryForObject("SELECT * FROM parking_pass WHERE id = ?", passMapper(), id);
    }

    @Transactional
    public SlotView createSlot(int floor, int number, String type) {
        if (floor < 1 || floor > floorSlots.length || number < 1 || blank(type)) throw new ParkingException("Provide a valid floor, slot number, and slot type.");
        String normalizedType = type.trim().toUpperCase();
        if (!List.of("STANDARD", "TWO_WHEELER").contains(normalizedType)) throw new ParkingException("Slot type must be STANDARD or TWO_WHEELER.");
        jdbc.update("INSERT INTO parking_slot(floor_number, slot_number, slot_type, occupied) VALUES (?, ?, ?, FALSE)", floor, number, normalizedType);
        return jdbc.queryForObject("SELECT * FROM parking_slot WHERE floor_number = ? AND slot_number = ?", slotMapper(), floor, number);
    }

    @Transactional
    public void deleteSlot(long id) {
        if (exists("SELECT COUNT(*) FROM parking_slot WHERE id = ? AND occupied = TRUE", id)) throw new ParkingException("An occupied slot cannot be deleted.");
        jdbc.update("DELETE FROM parking_slot WHERE id = ?", id);
    }

    @Transactional
    public SlotView updateSlot(long id, int floor, int number, String type) {
        if (floor < 1 || floor > floorSlots.length || number < 1 || blank(type)) throw new ParkingException("Provide a valid floor, slot number, and slot type.");
        String normalizedType = type.trim().toUpperCase();
        if (!List.of("STANDARD", "TWO_WHEELER").contains(normalizedType)) throw new ParkingException("Slot type must be STANDARD or TWO_WHEELER.");
        if (exists("SELECT COUNT(*) FROM parking_slot WHERE id = ? AND occupied = TRUE", id)) throw new ParkingException("An occupied slot cannot be changed.");
        if (jdbc.update("UPDATE parking_slot SET floor_number = ?, slot_number = ?, slot_type = ? WHERE id = ?", floor, number, normalizedType, id) == 0) {
            throw new ParkingException("Parking slot not found.");
        }
        return jdbc.queryForObject("SELECT * FROM parking_slot WHERE id = ?", slotMapper(), id);
    }

    public List<Map<String, Object>> payments() {
        return jdbc.queryForList("SELECT * FROM payment_record ORDER BY paid_at DESC");
    }

    @Transactional
    public TicketView ticket(String ticketId) {
        List<TicketView> result = jdbc.query("SELECT t.*, v.vehicle_type, s.floor_number, s.slot_number FROM parking_ticket t " +
                "JOIN vehicle v ON v.registration_number = t.registration_number JOIN parking_slot s ON s.id = t.slot_id WHERE t.ticket_id = ?",
                ticketViewMapper(), ticketId);
        if (result.isEmpty()) throw new ParkingException("Ticket not found.");
        return result.get(0);
    }

    private RowMapper<TicketRow> ticketRowMapper() {
        return (rs, row) -> new TicketRow(rs.getString("ticket_id"), rs.getString("vehicle_type"), rs.getLong("slot_id"),
                nullableLong(rs, "user_id"), rs.getTimestamp("entry_time").toLocalDateTime());
    }

    private RowMapper<TicketView> ticketViewMapper() {
        return (rs, row) -> new TicketView(rs.getString("ticket_id"), rs.getString("registration_number"),
                VehicleType.valueOf(rs.getString("vehicle_type")), rs.getLong("slot_id"), rs.getInt("floor_number"), rs.getInt("slot_number"),
                localDateTime(rs, "entry_time"), localDateTime(rs, "exit_time"), nullableLong(rs, "duration_minutes"),
                rs.getBigDecimal("parking_fee"), rs.getBigDecimal("charging_fee"), rs.getBigDecimal("total_fee"),
                rs.getBoolean("pass_applied"), rs.getString("status"));
    }

    private RowMapper<PassView> passMapper() {
        return (rs, row) -> new PassView(rs.getLong("id"), rs.getLong("user_id"), PassType.valueOf(rs.getString("pass_type")),
                localDateTime(rs, "valid_from"), localDateTime(rs, "valid_to"), rs.getBoolean("active"));
    }

    private RowMapper<SlotView> slotMapper() {
        return (rs, row) -> new SlotView(rs.getLong("id"), rs.getInt("floor_number"), rs.getInt("slot_number"),
                rs.getString("slot_type"), rs.getBoolean("occupied"), rs.getString("vehicle_registration"));
    }

    private LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0 : value;
    }

    private BigDecimal amount(String sql) {
        BigDecimal value = jdbc.queryForObject(sql, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean exists(String sql, Object... args) {
        return count(sql, args) > 0;
    }

    private String normalizeRegistration(String registration) {
        if (blank(registration)) throw new ParkingException("Vehicle registration number is required.");
        return registration.trim().toUpperCase();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private record TicketRow(String ticketId, String vehicleType, long slotId, Long userId, LocalDateTime entryTime) { }
}