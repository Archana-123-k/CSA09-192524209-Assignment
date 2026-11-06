package com.campus.parking.domain;

public class ElectricCar extends Car {
    public ElectricCar() { super(); }
    public ElectricCar(String registrationNumber) { super(registrationNumber); }
    @Override public VehicleType getVehicleType() { return VehicleType.ELECTRIC_CAR; }
}