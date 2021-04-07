package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.algorithm.DController;
import static eu.tomasznowicki.dvt.algorithm.DController.DProcedure;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import static eu.tomasznowicki.dvt.raport.DLogger.ALWAYS_TIME_STAMP;
import static eu.tomasznowicki.dvt.raport.DStrings.CONTROLLER_INSULIN_CALCULATED;
import java.io.FileWriter;

import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import eu.tomasznowicki.dvt.time.DTimeStamp;

public class DLoggerController extends DLogger.DLoggerAttendee {

    public DLoggerController(FileWriter fileWriter) {
        super(fileWriter);
    }

    public boolean log_controller(DController controller) {
        return log_insert(DStrings.CONTROLLER + SEP + controllerToString(controller) + ENDL2);
    }

    public boolean log_procedure(DTimeStamp time, DProcedure procedure) {
        var log = timeStampToString(time) + SEP + procedure.toString();
        return log_insert(log + ENDL);
    }
    
    

    //Plan podania insuliny, wyliczona -> zaplanowane
    public boolean log_insulin(DTimeStamp time, double insulin, DBolus bolus, String info) {

        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";

        

        return log_insert(log + ENDL2);

    }
    
    
    public boolean log_meal(DTimeStamp time, double ww, DMeal meal, String info) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";

        log += info + SEP + wToString(ww)
                + ARROW + mealToString(meal);

        return log_insert(log + ENDL2);
    }
    
    
    public boolean log_tep(String s){
        
        return log_insert(ENDL + s + ENDL2);
    }


}
