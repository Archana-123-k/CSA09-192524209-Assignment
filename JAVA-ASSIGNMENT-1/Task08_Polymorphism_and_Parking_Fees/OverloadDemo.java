import java.time.Duration;
import java.time.LocalTime;

abstract class OverloadVehicle {
    abstract double ratePerHour();
    double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("minutes cannot be negative");
        return ((minutes + 59) / 60) * ratePerHour();
    }
    double calculateFee(LocalTime entry, LocalTime exit) {
        return calculateFee(Math.toIntExact(Duration.between(entry, exit).toMinutes()));
    }
}
final class OverloadCar extends OverloadVehicle {
    @Override double ratePerHour() { return 30; }
}
public class OverloadDemo {
    public static void main(String[] args) {
        OverloadVehicle car = new OverloadCar();
        System.out.println("int overload: Rs " + car.calculateFee(61));
        System.out.println("LocalTime overload: Rs " + car.calculateFee(LocalTime.of(9, 0), LocalTime.of(10, 1)));
    }
}
