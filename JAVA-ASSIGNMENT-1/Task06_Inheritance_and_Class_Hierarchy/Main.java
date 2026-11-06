abstract class Vehicle {
    private final String registration;
    Vehicle(String registration) { this.registration = registration; System.out.println("Vehicle constructor"); }
    String registration() { return registration; }
}
class Car extends Vehicle {
    Car(String registration) { super(registration); System.out.println("Car constructor"); }
}
class ElectricCar extends Car {
    ElectricCar(String registration) { super(registration); System.out.println("ElectricCar constructor"); }
}
public class Main {
    public static void main(String[] args) {
        ElectricCar ev = new ElectricCar("TN03C3");
        System.out.println("Registration: " + ev.registration());
    }
}
