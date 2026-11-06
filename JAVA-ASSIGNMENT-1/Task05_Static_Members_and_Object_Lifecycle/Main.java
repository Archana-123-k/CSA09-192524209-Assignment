import java.util.concurrent.atomic.AtomicLong;

public class Main {
    static class Ticket {
        private static final AtomicLong NEXT = new AtomicLong(1);
        static { System.out.println("Ticket counter initialized"); }
        private final long id = NEXT.getAndIncrement();
        static long issuedCount() { return NEXT.get() - 1; }
        @Override public String toString() { return "Ticket-" + id; }
    }
    static class Facility {
        private int occupancy;
        Ticket enter() { occupancy++; return new Ticket(); }
        int occupancy() { return occupancy; }
    }
    public static void main(String[] args) {
        Facility north = new Facility();
        Facility south = new Facility();
        Ticket ticket = north.enter();
        south.enter();
        System.out.println("Returned object: " + ticket);
        System.out.println("Occupancy north/south: " + north.occupancy() + "/" + south.occupancy());
        System.out.println("Tickets issued: " + Ticket.issuedCount());
        ticket = null;
        System.out.println("Ticket reference cleared; eligible only if no other references remain");
    }
}
