import java.math.BigDecimal;

public class Main {
    enum Type { TWO_WHEELER, CAR, ELECTRIC_CAR }
    static final class Ticket {
        final String registration; final Type type; final int floor, slot; final boolean pass;
        Ticket(String registration, Type type, int floor, int slot, boolean pass) { this.registration=registration; this.type=type; this.floor=floor; this.slot=slot; this.pass=pass; }
    }
    static final class Facility {
        final String[][] slots; final Ticket[] active; BigDecimal collection=BigDecimal.ZERO;
        Facility(int... capacity) { slots=new String[capacity.length][]; int total=0; for(int f=0;f<capacity.length;f++){slots[f]=new String[capacity[f]];total+=capacity[f];} active=new Ticket[total]; }
        Ticket enter(String reg, String rawType, boolean pass) {
            Type type; try { type=Type.valueOf(rawType); } catch(RuntimeException e) { throw new IllegalArgumentException("unknown vehicle type"); }
            if(reg==null||reg.isBlank()) throw new IllegalArgumentException("registration required");
            if(find(reg)!=null) throw new IllegalArgumentException("duplicate registration");
            for(int f=0;f<slots.length;f++) for(int s=0;s<slots[f].length;s++) if(slots[f][s]==null) {
                slots[f][s]=reg; Ticket t=new Ticket(reg,type,f,s,pass); int i=emptyIndex(); if(i<0) throw new IllegalStateException("ticket capacity full"); active[i]=t; return t;
            }
            throw new IllegalStateException("no free slot");
        }
        BigDecimal exit(String reg,int minutes,int kWh) {
            if(minutes<0||kWh<0) throw new IllegalArgumentException("negative input");
            Ticket t=find(reg); if(t==null) throw new IllegalArgumentException("ticket not found");
            if(t.type!=Type.ELECTRIC_CAR&&kWh!=0) throw new IllegalArgumentException("charging only for EV");
            int hours=(minutes+59)/60;
            BigDecimal parking=t.pass?BigDecimal.ZERO:BigDecimal.valueOf((long)hours*(t.type==Type.TWO_WHEELER?10:30));
            BigDecimal charging=t.type==Type.ELECTRIC_CAR?BigDecimal.valueOf(kWh*12L):BigDecimal.ZERO;
            slots[t.floor][t.slot]=null; for(int i=0;i<active.length;i++) if(active[i]==t){active[i]=null;break;} BigDecimal total=parking.add(charging); collection=collection.add(total); return total;
        }
        Ticket find(String reg){for(Ticket t:active)if(t!=null&&t.registration.equals(reg))return t;return null;}
        int emptyIndex(){for(int i=0;i<active.length;i++)if(active[i]==null)return i;return -1;}
        int available(){int n=0;for(String[] row:slots)for(String cell:row)if(cell==null)n++;return n;}
        boolean release(int f,int s){if(f<0||f>=slots.length||s<0||s>=slots[f].length||slots[f][s]==null)return false;slots[f][s]=null;return true;}
    }
    static int passed=0;
    static void check(String name,String expected,String actual){boolean ok=expected.equals(actual);System.out.printf("%-28s | %-16s | %-16s | %s%n",name,expected,actual,ok?"PASS":"FAIL");if(!ok)throw new AssertionError(name);passed++;}
    static String fee(BigDecimal amount){return amount.setScale(2).toPlainString();}
    static void rejected(String name,String expected,Runnable action){try{action.run();check(name,expected,"accepted");}catch(RuntimeException e){check(name,expected,e.getMessage());}}
    public static void main(String[] args){
        System.out.println("TC | Scenario | Expected | Actual | Status");
        Facility daily=new Facility(4);
        daily.enter("BIKE", "TWO_WHEELER",false); check("Bike entry slot","0-0",daily.find("BIKE").floor+"-"+daily.find("BIKE").slot);
        check("Bike 60 minutes","10.00",fee(daily.exit("BIKE",60,0)));
        daily.enter("CAR","CAR",false); check("Car 61 minutes","60.00",fee(daily.exit("CAR",61,0)));
        daily.enter("EV","ELECTRIC_CAR",false); check("EV 130min + 10kWh","210.00",fee(daily.exit("EV",130,10)));
        daily.enter("PASS","CAR",true); check("Monthly pass","0.00",fee(daily.exit("PASS",120,0)));
        check("Daily collection","280.00",fee(daily.collection));
        daily.enter("ZERO","TWO_WHEELER",false); check("Zero minute","0.00",fee(daily.exit("ZERO",0,0)));
        daily.enter("EXACT","CAR",false); check("Exact 60-min boundary","30.00",fee(daily.exit("EXACT",60,0)));
        Facility last=new Facility(1);last.enter("LAST","CAR",false);check("Last slot consumed","0",Integer.toString(last.available()));
        rejected("No space","no free slot",()->last.enter("FULL","CAR",false));
        Facility dup=new Facility(2);dup.enter("DUP","CAR",false);rejected("Duplicate registration","duplicate registration",()->dup.enter("DUP","CAR",false));
        rejected("Unknown type","unknown vehicle type",()->dup.enter("BAD","AIRCRAFT",false));
        check("Invalid slot","false",Boolean.toString(last.release(0,1)));
        check("Total checks passed","13",Integer.toString(passed));
        System.out.println("Manual: 10 + 60 + (90 + 120) + 0 = Rs 280.00");
    }
}
