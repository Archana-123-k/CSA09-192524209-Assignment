interface Chargeable {
    double chargingFee(int kWh);
    default String summary(int kWh) { return kWh + " kWh"; }
    static boolean valid(int kWh) { return kWh >= 0; }
}
interface Payable { double totalDue(); }
abstract class Vehicle {
    private final String registration;
    Vehicle(String registration) { this.registration = registration; }
    String registration() { return registration; }
    abstract double calculateFee(int minutes);
}
class ElectricCar extends Vehicle implements Chargeable, Payable {
    private final int kWh;
    ElectricCar(String registration, int kWh) { super(registration); if (!Chargeable.valid(kWh)) throw new IllegalArgumentException(); this.kWh = kWh; }
    @Override double calculateFee(int minutes) { return Math.max(1, (minutes + 59) / 60) * 30.0; }
    @Override public double chargingFee(int units) { return units * 12.0; }
    @Override public double totalDue() { return calculateFee(61) + chargingFee(kWh); }
}
public class Main {
    public static void main(String[] args) {
        ElectricCar ev = new ElectricCar("TN03C3", 2);
        System.out.println("Vehicle: " + ev.registration());
        System.out.println("Energy: " + ev.summary(2));
        System.out.println("Total: Rs " + ev.totalDue());
    }
}
