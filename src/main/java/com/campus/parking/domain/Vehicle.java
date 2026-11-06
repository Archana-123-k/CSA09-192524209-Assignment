package com.campus.parking.domain;

public abstract class Vehicle {
    private static int createdCount;
    private final String registrationNumber;

    protected Vehicle() {
        this("UNREGISTERED");
    }

    protected Vehicle(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        createdCount++;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public abstract VehicleType getVehicleType();

    public String describe() {
        return describe("Vehicle");
    }

    public String describe(String label) {
        return label + ": " + registrationNumber + " (" + getVehicleType() + ")";
    }

    public static int getCreatedCount() {
        return createdCount;
    }
}