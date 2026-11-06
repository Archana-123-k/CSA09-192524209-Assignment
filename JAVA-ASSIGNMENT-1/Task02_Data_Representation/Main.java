import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner("75\n")) {
            int minutes = scanner.nextInt();
            int truncated = minutes / 60;
            int billable = (minutes + 59) / 60;
            System.out.println("Integer division hours: " + truncated);
            System.out.println("Billable hours: " + billable);
            System.out.println("Car fee: Rs " + BigDecimal.valueOf(30L * billable));
        }
    }
}
