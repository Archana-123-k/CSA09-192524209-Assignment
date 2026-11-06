import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    static class ParkingTicket {
        private static final AtomicLong NEXT = new AtomicLong(1);
        private final long id;
        private final String registration;
        private final LocalDateTime entry;
        ParkingTicket() { this("UNKNOWN"); }
        ParkingTicket(String registration) { this(registration, LocalDateTime.of(2026, 11, 6, 9, 0)); }
        ParkingTicket(String registration, LocalDateTime entry) {
            this.id = NEXT.getAndIncrement();
            this.registration = registration;
            this.entry = entry;
        }
        ParkingTicket(ParkingTicket source) { this(source.registration, source.entry); }
        @Override public String toString() { return "Ticket " + id + " " + registration + " " + entry; }
    }
    public static void main(String[] args) {
        ParkingTicket original = new ParkingTicket("TN01AB1111");
        ParkingTicket copy = new ParkingTicket(original);
        System.out.println(original);
        System.out.println(copy);
        System.out.println("Fresh ID on copy: " + !original.toString().equals(copy.toString()));
    }
}
