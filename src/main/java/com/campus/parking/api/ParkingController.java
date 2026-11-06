package com.campus.parking.api;

import com.campus.parking.api.ApiModels.*;
import com.campus.parking.domain.VehicleType;
import com.campus.parking.service.ParkingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ParkingController {
    private final ParkingService parking;

    public ParkingController(ParkingService parking) {
        this.parking = parking;
    }

    @GetMapping("/dashboard")
    public Dashboard dashboard() { return parking.dashboard(); }

    @GetMapping("/slots")
    public List<SlotView> slots() { return parking.slots(); }

    @PostMapping("/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotView createSlot(@RequestBody Map<String, Object> input) {
        return parking.createSlot(number(input.get("floor")), number(input.get("number")), String.valueOf(input.get("type")));
    }

    @PutMapping("/slots/{id}")
    public SlotView updateSlot(@PathVariable long id, @RequestBody Map<String, Object> input) {
        return parking.updateSlot(id, number(input.get("floor")), number(input.get("number")), String.valueOf(input.get("type")));
    }

    @DeleteMapping("/slots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(@PathVariable long id) { parking.deleteSlot(id); }

    @PostMapping("/entry")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketView entry(@RequestBody EntryRequest request) { return parking.enter(request); }

    @PostMapping("/exit")
    public TicketView exit(@RequestBody ExitRequest request) { return parking.exit(request); }

    @GetMapping("/tickets")
    public List<TicketView> history() { return parking.history(); }

    @GetMapping("/tickets/{ticketId}")
    public TicketView ticket(@PathVariable String ticketId) { return parking.ticket(ticketId); }

    @GetMapping("/vehicles")
    public List<VehicleView> vehicles() { return parking.vehicles(); }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleView createVehicle(@RequestBody VehicleView request) {
        return parking.saveVehicle(request.registrationNumber(), request.vehicleType(), request.userId());
    }

    @PutMapping("/vehicles/{registration}")
    public VehicleView updateVehicle(@PathVariable String registration, @RequestBody VehicleView request) {
        return parking.saveVehicle(registration, request.vehicleType(), request.userId());
    }

    @DeleteMapping("/vehicles/{registration}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable String registration) { parking.deleteVehicle(registration); }

    @GetMapping("/users")
    public List<UserView> users() { return parking.users(); }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView createUser(@RequestBody UserRequest request) { return parking.createUser(request); }

    @PutMapping("/users/{id}")
    public UserView updateUser(@PathVariable long id, @RequestBody UserRequest request) { return parking.updateUser(id, request); }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) { parking.deleteUser(id); }

    @GetMapping("/passes")
    public List<PassView> passes() { return parking.passes(); }

    @PostMapping("/passes")
    @ResponseStatus(HttpStatus.CREATED)
    public PassView createPass(@RequestBody PassRequest request) { return parking.createPass(request); }

    @PutMapping("/passes/{id}")
    public PassView updatePass(@PathVariable long id, @RequestBody PassRequest request) { return parking.updatePass(id, request); }

    @DeleteMapping("/passes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePass(@PathVariable long id) { parking.deletePass(id); }

    @GetMapping("/payments")
    public List<Map<String, Object>> payments() { return parking.payments(); }

    @GetMapping("/reports")
    public Report report() { return parking.report(); }

    private int number(Object value) {
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException exception) { throw new com.campus.parking.service.ParkingException("Floor and slot number must be whole numbers."); }
    }
}