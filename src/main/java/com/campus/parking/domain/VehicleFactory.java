package com.campus.parking.domain;

public final class VehicleFactory {
    private VehicleFactory() { }

    public static Vehicle create(VehicleType type, String registrationNumber) {
        return switch (type) {
            case TWO_WHEELER -> new TwoWheeler(registrationNumber);
            case CAR -> new Car(registrationNumber);
            case ELECTRIC_CAR -> new ElectricCar(registrationNumber);
            case E_BIKE -> new EBike(registrationNumber);
            case DELIVERY_VAN -> new DeliveryVan(registrationNumber);
        };
    }
}