package com.campus.parking.domain;

public class TwoWheeler extends Vehicle {
    public TwoWheeler() { super(); }
    public TwoWheeler(String registrationNumber) { super(registrationNumber); }
    @Override public VehicleType getVehicleType() { return VehicleType.TWO_WHEELER; }
}