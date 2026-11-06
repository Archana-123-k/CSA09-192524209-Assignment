package com.campus.parking.domain;

public class Car extends Vehicle {
    public Car() { super(); }
    public Car(String registrationNumber) { super(registrationNumber); }
    @Override public VehicleType getVehicleType() { return VehicleType.CAR; }
}