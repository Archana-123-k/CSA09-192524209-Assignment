package com.campus.parking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ParkingFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clearActiveParking() {
        jdbc.update("UPDATE parking_slot SET occupied = FALSE, vehicle_registration = NULL");
        jdbc.update("DELETE FROM payment_record");
        jdbc.update("DELETE FROM parking_ticket");
        jdbc.update("DELETE FROM parking_pass");
        jdbc.update("DELETE FROM parking_user");
        jdbc.update("DELETE FROM vehicle");
    }

    @Test
    void entryAndExitCreateTicketCalculateFeeAndFreeSlot() throws Exception {
        String registration = registration();
        JsonNode entry = json.readTree(mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registrationNumber\":\"%s\",\"vehicleType\":\"CAR\"}".formatted(registration)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        assertEquals("ACTIVE", entry.get("status").asText());
        assertTrue(entry.get("ticketId").asText().length() > 10);

        JsonNode exit = json.readTree(mvc.perform(post("/api/exit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registrationNumber\":\"%s\",\"kwh\":0,\"paymentMethod\":\"UPI\"}".formatted(registration)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("CLOSED", exit.get("status").asText());
        assertEquals("30.00", exit.get("totalFee").decimalValue().setScale(2).toPlainString());
        assertEquals(15, jdbc.queryForObject("SELECT COUNT(*) FROM parking_slot WHERE occupied = FALSE", Integer.class));
        System.out.printf("Entry/exit expected CLOSED, ₹30.00, slot released; actual %s, ₹%s%n",
                exit.get("status").asText(), exit.get("totalFee").asText());
    }

    @Test
    void duplicateActiveVehicleIsRejected() throws Exception {
        String registration = registration();
        String payload = "{\"registrationNumber\":\"%s\",\"vehicleType\":\"TWO_WHEELER\"}".formatted(registration);
        mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated());
        mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("This vehicle already has an active parking ticket."));
    }

    @Test
    void fullFacilityRejectsEntry() throws Exception {
        jdbc.update("UPDATE parking_slot SET occupied = TRUE WHERE slot_type = 'STANDARD'");
        mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registrationNumber\":\"%s\",\"vehicleType\":\"CAR\"}".formatted(registration())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("No available standard parking slot."));
    }

    @Test
    void invalidSlotNumberIsRejected() throws Exception {
        mvc.perform(post("/api/slots").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"floor\":1,\"number\":0,\"type\":\"STANDARD\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void monthlyAndOneDayPassesWaiveParkingFee() throws Exception {
        long userId = createUser();
        for (String type : new String[]{"MONTHLY", "ONE_DAY"}) {
            LocalDateTime now = LocalDateTime.now();
            String pass = "{\"userId\":%d,\"passType\":\"%s\",\"validFrom\":\"%s\",\"validTo\":\"%s\"}"
                    .formatted(userId, type, now.minusHours(1), now.plusHours(type.equals("ONE_DAY") ? 23 : 24));
            mvc.perform(post("/api/passes").contentType(MediaType.APPLICATION_JSON).content(pass)).andExpect(status().isCreated());
            String registration = registration();
            mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"registrationNumber\":\"%s\",\"vehicleType\":\"CAR\",\"userId\":%d}".formatted(registration, userId)))
                    .andExpect(status().isCreated());
            JsonNode exit = json.readTree(mvc.perform(post("/api/exit").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"registrationNumber\":\"%s\"}".formatted(registration)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            assertTrue(exit.get("passApplied").asBoolean());
            assertEquals("0.00", exit.get("totalFee").decimalValue().setScale(2).toPlainString());
            jdbc.update("DELETE FROM parking_pass");
        }
    }

    @Test
    void electricCarIncludesChargingFeeAndEbikeUsesFlatFee() throws Exception {
        String evRegistration = registration();
        mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON)
                .content("{\"registrationNumber\":\"%s\",\"vehicleType\":\"ELECTRIC_CAR\"}".formatted(evRegistration))).andExpect(status().isCreated());
        JsonNode evExit = json.readTree(mvc.perform(post("/api/exit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registrationNumber\":\"%s\",\"kwh\":2}".formatted(evRegistration)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("54.00", evExit.get("totalFee").decimalValue().setScale(2).toPlainString());

        String bikeRegistration = registration();
        mvc.perform(post("/api/entry").contentType(MediaType.APPLICATION_JSON)
                .content("{\"registrationNumber\":\"%s\",\"vehicleType\":\"E_BIKE\"}".formatted(bikeRegistration))).andExpect(status().isCreated());
        JsonNode bikeExit = json.readTree(mvc.perform(post("/api/exit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registrationNumber\":\"%s\"}".formatted(bikeRegistration)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("15.00", bikeExit.get("totalFee").decimalValue().setScale(2).toPlainString());
    }

    private long createUser() throws Exception {
        JsonNode user = json.readTree(mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test Campus User\",\"email\":\"%s@campus.test\",\"category\":\"STUDENT\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        return user.get("id").asLong();
    }

    private String registration() {
        return "TEST" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}