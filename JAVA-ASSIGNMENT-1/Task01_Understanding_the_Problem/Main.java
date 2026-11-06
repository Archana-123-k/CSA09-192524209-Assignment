import java.util.HashMap;
import java.util.Map;

public class Main {
    enum Type { BIKE, CAR }
    record Vehicle(String registration, Type type) { }
    public static void main(String[] args) {
        Map<String, Vehicle> active = new HashMap<>();
        active.put("TN01AB1111", new Vehicle("TN01AB1111", Type.CAR));
        System.out.println("Find registration: " + active.get("TN01AB1111"));
        System.out.println("Active count: " + active.size());
    }
}
