package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.kit.DMealBox;
import static eu.tomasznowicki.dvt.raport.DLogger.ALWAYS_TIME_STAMP;
import java.io.FileWriter;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import static eu.tomasznowicki.dvt.raport.DStrings.*;
import eu.tomasznowicki.dvt.time.DTimeStamp;

public class DLoggerMealBox extends DLogger.DLoggerAttendee {

    public DLoggerMealBox(FileWriter fileWriter) {
        super(fileWriter);
    }

    public boolean log_mealbox(DMealBox box) {
        return log_insert(ENDL + MEALBOX + ENDL + mealBoxToString(box) + ENDL);
    }

    //---
    public boolean log_joining_meals(DTimeStamp time, DMeal meal0, DMeal meal1, DMeal resultant, String info) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += MEALBOX_JOINING_MEALS + SEP + info + SEP + mealToString(meal0) + SEP
                + mealToString(meal1) + ARROW + mealToString(resultant);
        return log_insert(log + ENDL);
    }

    //---
    public boolean log_scheduling(DTimeStamp time, DMeal meal, String info) {
        var log = timeStampToString(time) + SEP + MEALBOX_SCHEDULING + SEP + info + SEP + mealToString(meal);
        return log_insert(ENDL + log + ENDL);
    }

    public boolean log_serving(DTimeStamp time, DMeal meal, String info) {
        var log = timeStampToString(time) + SEP + MEALBOX_SERVING + SEP + info + SEP + mealToString(meal);
        return log_insert(ENDL + log + ENDL);
    }

    public boolean log_cleaning(DTimeStamp time) {
        var log = timeStampToString(time) + SEP + MEALBOX_CLEANING;
        return log_insert(ENDL + log + ENDL);
    }

    //---
    //---
    public boolean log_adding_to_plan(DTimeStamp time, DMeal meal, String info, boolean ok) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += MEALBOX_ADDING_TO_PLAN + SEP + info + SEP + mealToString(meal);
        if (!ok) {
            log += SEP + FAILED;
        }
        return log_insert(log + ENDL);
    }

    public boolean log_adding_to_history(DTimeStamp time, DMeal meal, String info, boolean ok) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += MEALBOX_ADDING_TO_HISTORY + SEP + info + SEP + mealToString(meal);
        if (!ok) {
            log += SEP + FAILED;
        }
        return log_insert(log + ENDL);
    }

    public boolean log_cleaning_from_history(DTimeStamp time, DMeal meal, String info) {
        var log = timeStampToString(time) + SEP + info + SEP + mealToString(meal);
        return log_insert(log + ENDL);
    }

}
