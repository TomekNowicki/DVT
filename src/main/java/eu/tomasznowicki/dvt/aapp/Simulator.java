package eu.tomasznowicki.dvt.aapp;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.therapy.DTherapy;
import eu.tomasznowicki.dvt.therapy.DAutoTherapy;
import static eu.tomasznowicki.dvt.algorithm.DController.DType.*;
import eu.tomasznowicki.dvt.biocyb.DMeal;


public class Simulator {

    public static final String PATH = "/home/tomek/Dokumenty/T1DM/DVTlab/sim/04";
    
    public static final DTimeStamp START_TIME = new DTimeStamp(0, 4, 0), STOP_TIME = new DTimeStamp(3, 4, 0);
    //public static final DTimeStamp START_TIME = new DTimeStamp(0, 4, 0), STOP_TIME = new DTimeStamp(3, 4, 0);
        
    private DTherapy therapy = null;

    Simulator() {
        //public boolean open(String path, DTimeStamp dTimeStamp) {
        therapy = new DAutoTherapy();
    }

    public void run() {

        System.out.println("Starting simulation...");
        therapy.open(PATH, START_TIME, PID);

        if (therapy.ready()) {
            
            boolean ok;
            
            for (long i = START_TIME.total; i <= STOP_TIME.total; i++) {
               
                System.out.print(therapy.stepTime() + " -> ");
                
                ok = therapy.nextMinute();
                
                System.out.println(therapy.infoString());
                
                if(!ok) break;
            }
            
            System.out.println("...done. Check files.");

        } else System.out.println("!Error: Therapy not ready");

        therapy.close();
    }
    
    
    public void test() {
        System.out.println("Testing...");
        
    }
    
    

    public static void main(String[] args) {
       //(new Simulator()).run();
        
        //(new Simulator()).test();
        //public DMeal(DTimeStamp dTime, double carb, double fat, double prot, int index) {
        DMeal m = new DMeal(new DTimeStamp(), 1, 1, 1 , 50);
        
        System.out.print(m);
    }

}
