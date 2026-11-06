import java.util.Objects;

class ParkingSlot {
    private final int floor;
    private int number;
    private Integer occupantCount;
    ParkingSlot(int floor, int number) { if (floor < 0 || number < 0) throw new IllegalArgumentException(); this.floor = floor; this.number = number; this.occupantCount = 0; }
    public int getFloor() { return floor; }
    public int getNumber() { return number; }
    public void setNumber(int number) { if (number < 0) throw new IllegalArgumentException("invalid slot"); this.number = number; }
    protected int getOccupantCount() { return occupantCount; }
    String describe() { return "F" + floor + "-S" + number; }
}
public class Main {
    public static void main(String[] args) {
        ParkingSlot slot = new ParkingSlot(1, 4);
        Integer a = 1000, b = 1000;
        System.out.println(slot.describe());
        System.out.println("Integer ==: " + (a == b));
        System.out.println("Objects.equals: " + Objects.equals(a, b));
    }
}
