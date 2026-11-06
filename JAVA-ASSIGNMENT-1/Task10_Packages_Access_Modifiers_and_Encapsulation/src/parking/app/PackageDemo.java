package parking.app;

import parking.model.Vehicle;

final class DemoVehicle extends Vehicle {
    DemoVehicle(String registration, String category) { super(registration, category); }
    String categoryForSubclass() { return getCategory(); }
}

public class PackageDemo {
    public static void main(String[] args) {
        DemoVehicle vehicle = new DemoVehicle("TN10PK1000", "CAR");
        System.out.println("Public getter: " + vehicle.getRegistration());
        System.out.println("Protected access through subclass: " + vehicle.categoryForSubclass());
        System.out.println("Default access: same-package only; not accessible from parking.app");
    }
}
