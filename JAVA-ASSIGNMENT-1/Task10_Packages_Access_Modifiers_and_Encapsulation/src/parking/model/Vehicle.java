package parking.model;

public class Vehicle {
    private final String registration;
    protected final String category;
    int internalCode;

    public Vehicle(String registration, String category) {
        if (registration == null || registration.isBlank()) throw new IllegalArgumentException("registration is required");
        this.registration = registration;
        this.category = category;
    }

    public String getRegistration() { return registration; }
    protected String getCategory() { return category; }
    String internalLabel() { return "vehicle:" + internalCode; }
}
