package com.campus.parking.domain;

public class EBike extends Vehicle {
    public EBike() { super(); }
    public EBike(String registrationNumber) { super(registrationNumber); }
    @Override public VehicleType getVehicleType() { return VehicleType.E_BIKE; }
}