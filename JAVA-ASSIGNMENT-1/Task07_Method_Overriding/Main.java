import java.util.Objects;

abstract class Vehicle {
    private final String registration;
    Vehicle(String registration) { this.registration = Objects.requireNonNull(registration); }
    public final String getRegistration() { return registration; }
    protected abstract double rate();
    public double calculateFee(int minutes) { return Math.max(0, (minutes + 59) / 60) * rate(); }
    @Override public String toString() { return getClass().getSimpleName() + "[" + registration + "]"; }
    @Override public boolean equals(Object other) { return other instanceof Vehicle v && registration.equals(v.registration); }
    @Override public int hashCode() { return registration.hashCode(); }
}
class Car extends Vehicle {
    Car(String registration) { super(registration); }
    @Override protected double rate() { return 30; }
}
public class Main {
    public static void main(String[] args) {
        Vehicle a = new Car("TN02B2");
        Vehicle b = new Car("TN02B2");
        System.out.println(a + " -> Rs " + a.calculateFee(61));
        System.out.println("Equal: " + a.equals(b) + ", same hash: " + (a.hashCode() == b.hashCode()));
    }
}
