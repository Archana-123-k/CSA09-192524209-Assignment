import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Main {
    interface PricingRule { BigDecimal fee(LocalDateTime in, LocalDateTime out); }
    interface Pass { boolean covers(LocalDateTime in, LocalDateTime out); }
    record OneDayPass(LocalDateTime from, LocalDateTime to) implements Pass {
        OneDayPass { if (!to.isAfter(from)||Duration.between(from,to).compareTo(Duration.ofHours(24))>0) throw new IllegalArgumentException("one-day pass must be <=24 hours"); }
        public boolean covers(LocalDateTime in,LocalDateTime out){return !in.isBefore(from)&&!out.isAfter(to);}
    }
    record MonthlyPass(LocalDateTime from, LocalDateTime to) implements Pass {
        public boolean covers(LocalDateTime in,LocalDateTime out){return !in.isBefore(from)&&!out.isAfter(to);}
    }
    record Visitor(String name, Pass pass) { }
    static class HourlyRule implements PricingRule {
        final BigDecimal rate; HourlyRule(BigDecimal rate){this.rate=rate;}
        public BigDecimal fee(LocalDateTime in,LocalDateTime out){long sec=Duration.between(in,out).getSeconds();if(sec<0)throw new IllegalArgumentException();return rate.multiply(BigDecimal.valueOf((sec+3599)/3600));}
    }
    static class EBikeRule implements PricingRule {final BigDecimal flat;EBikeRule(BigDecimal flat){this.flat=flat;}public BigDecimal fee(LocalDateTime in,LocalDateTime out){return flat;}}
    static class VanRule implements PricingRule {
        final BigDecimal carRate;VanRule(BigDecimal rate){carRate=rate;}
        public BigDecimal fee(LocalDateTime in,LocalDateTime out){long sec=Duration.between(in,out).getSeconds();if(sec<0)throw new IllegalArgumentException();long hours=(sec+3599)/3600;BigDecimal total=BigDecimal.ZERO;for(long i=0;i<hours;i++){int h=in.plusHours(i).getHour();total=total.add(carRate.multiply(BigDecimal.valueOf(h>=9&&h<17?2:1)));}return total;}
    }
    static class Vehicle {final String registration;final PricingRule pricing;Vehicle(String r,PricingRule p){registration=r;pricing=p;}BigDecimal parking(LocalDateTime in,LocalDateTime out,Pass pass){return pass!=null&&pass.covers(in,out)?BigDecimal.ZERO:pricing.fee(in,out);}}
    static class ElectricCar extends Vehicle {ElectricCar(String r){super(r,new HourlyRule(BigDecimal.valueOf(30)));}BigDecimal charging(int kWh){if(kWh<0)throw new IllegalArgumentException();return BigDecimal.valueOf(kWh*12L);}}
    static class EBike extends Vehicle {EBike(String r,BigDecimal fee){super(r,new EBikeRule(fee));}}
    static class DeliveryVan extends Vehicle {DeliveryVan(String r){super(r,new VanRule(BigDecimal.valueOf(30)));}}
    static class ParkingFacility {final String[][] slots;ParkingFacility(int[] counts){slots=new String[counts.length][];for(int f=0;f<counts.length;f++)slots[f]=new String[counts[f]];}int[] allocate(String reg){for(int f=0;f<slots.length;f++)for(int s=0;s<slots[f].length;s++)if(slots[f][s]==null){slots[f][s]=reg;return new int[]{f,s};}return null;}int total(){return Arrays.stream(slots).mapToInt(row->row.length).sum();}}
    static int checks=0;
    static void check(String label,String expected,String actual){boolean pass=expected.equals(actual);System.out.printf("%-32s expected=%-8s actual=%-8s %s%n",label,expected,actual,pass?"PASS":"FAIL");if(!pass)throw new AssertionError(label);checks++;}
    static String money(BigDecimal value){return value.setScale(2).toPlainString();}
    public static void main(String[] args){
        LocalDateTime start=LocalDateTime.of(2026,11,6,8,0);
        ParkingFacility f=new ParkingFacility(new int[]{2,3,1,2,4});
        check("Five unequal floors", "12",Integer.toString(f.total()));
        check("Jagged slot allocation", "[0, 0]",Arrays.toString(f.allocate("E1")));
        check("E-bike flat fee", "15.00",money(new EBike("E1",BigDecimal.valueOf(15)).parking(start,start.plusHours(2),null)));
        DeliveryVan van=new DeliveryVan("V1");
        check("Van 08:00 to 10:00", "90.00",money(van.parking(start,start.plusHours(2),null)));
        check("Van 17:00 off-peak", "30.00",money(van.parking(start.withHour(17),start.withHour(18),null)));
        check("Van 5 PM boundary", "90.00",money(van.parking(start.withHour(16),start.withHour(18),null)));
        Visitor visitor=new Visitor("Campus visitor",new OneDayPass(start,start.plusHours(24)));
        check("Visitor one-day pass", "0.00",money(new Vehicle("VIS",new HourlyRule(BigDecimal.valueOf(30))).parking(start.plusHours(1),start.plusHours(3),visitor.pass())));
        Pass monthly=new MonthlyPass(start.minusDays(10),start.plusDays(20));
        check("Monthly pass regression", "0.00",money(new Vehicle("STAFF",new HourlyRule(BigDecimal.valueOf(30))).parking(start,start.plusHours(2),monthly)));
        check("EV parking plus 2 kWh", "54.00",money(new ElectricCar("EV").parking(start,start.plusHours(1),null).add(new ElectricCar("EV").charging(2))));
        check("All design challenge checks", "9",Integer.toString(checks));
    }
}
