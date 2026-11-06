package com.campus.parking.domain;

public class DeliveryVan extends Vehicle {
    public DeliveryVan() { super(); }
    public DeliveryVan(String registrationNumber) { super(registrationNumber); }
    @Override public VehicleType getVehicleType() { return VehicleType.DELIVERY_VAN; }
}