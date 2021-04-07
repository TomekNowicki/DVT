package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.kit.DTherapyKit;
import static eu.tomasznowicki.dvt.raport.DLogger.ALWAYS_TIME_STAMP;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import java.io.FileWriter;

import static eu.tomasznowicki.dvt.raport.ToStrings.*;

public class DLoggerKit extends DLogger.DLoggerAttendee {

    DLoggerKit(FileWriter fileWriter) {
        super(fileWriter);
    }
    
    public boolean log_kit(DTherapyKit kit){
        return log_insert(DStrings.KIT + ENDL + therapyKitToString(kit) + ENDL2);
    }

    public boolean log_joining_boluses(DTimeStamp time, DBolus bolus0, DBolus bolus1, DBolus resultant, String info) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += DStrings.KIT_JOINING_BOLUSES + SEP + info + SEP + bolusToString(bolus0) + SEP
                + bolusToString(bolus1) + ARROW + bolusToString(resultant);
        return log_insert(log + ENDL2);
    }

    public boolean log_adding_to_plan(DTimeStamp time, DBolus bolus, String info, boolean ok) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += DStrings.KIT_ADDING_TO_PLAN + SEP + info + SEP + bolusToString(bolus);
        if (!ok) {
            log += SEP + FAILED;
        }
        return log_insert(log + ENDL2);
    }

    public boolean log_clearing_plan(DTimeStamp time, String info) { //Czyszczenie planu bazy
        var log = timeStampToString(time) + SEP + DStrings.KIT_CLEARING + SEP + info;
        return log_insert(log + ENDL2);
    }

    public boolean log_adding_to_history(DTimeStamp time, DBolus bolus, String info, boolean ok) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += DStrings.KIT_ADDING_TO_HISTORY + SEP + info + SEP + bolusToString(bolus);
        if (!ok) {
            log += SEP + FAILED;
        }
        return log_insert(log + ENDL2);
    }

    public boolean log_cleaning_from_history(DTimeStamp time, DBolus bolus, String info) {
        var log = timeStampToString(time) + SEP + DStrings.KIT_CLEANING + SEP + info + SEP + bolusToString(bolus);
        return log_insert(log + ENDL2);
    }

    public boolean log_administering(DTimeStamp time, DBolus bolus, String info) {
        var log = timeStampToString(time) + SEP + DStrings.KIT_ADMINISTERING + SEP + info + SEP + bolusToString(bolus);
        return log_insert(log + ENDL2);
    }
    
    public boolean log_calculation_advice(DTimeStamp time, DTherapyAdvice advice){
        var log = timeStampToString(time) + SEP + DStrings.KIT_ADVISING + ENDL + therapyAdviceToString(advice);
        return log_insert(log + ENDL2); 
    }

}
