package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.patient.DPatientCore;

import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import java.io.FileWriter;

import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import static eu.tomasznowicki.dvt.raport.DLogger.ALWAYS_TIME_STAMP;
import eu.tomasznowicki.dvt.time.DDaySettings;


public class DLoggerPatient extends DLogger.DLoggerAttendee {

    DLoggerPatient(FileWriter fileWriter) {
        super(fileWriter);
    }
    
    public boolean log_patient(DPatientCore patient){
        return log_insert(DStrings.PATIENT + SEP + patientToString(patient) + ENDL);
    }
    
    public boolean log_patient_BG(DTimeStamp time, double bg) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += BGtoString(bg);
        return log_insert(log + ENDL);
    }

    public boolean log_patient_adding_bolus(DTimeStamp time, DBolus bolus) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += bolusToString(bolus);
        return log_insert(log + ENDL);
    }

    public boolean log_patient_adding_meal(DTimeStamp time, DMeal meal) {
        var log = ALWAYS_TIME_STAMP ? timeStampToString(time) + SEP : "";
        log += mealToString(meal);
        return log_insert(log + ENDL);
    }

    public boolean log_liver_activation(DTimeStamp time, String name, double value) {

        var log = name + SEP;
        if (ALWAYS_TIME_STAMP) {
            log += timeStampToString(time) + SEP;
        }
        log += wToString(value);
        return log_insert(log + ENDL);
    }
    
    
        public boolean log_independet_intake_activation(DTimeStamp time, String name, double value) {

        var log = name + SEP;
        if (ALWAYS_TIME_STAMP) {
            log += timeStampToString(time) + SEP;
        }
        log += wToString(value);
        return log_insert(log + ENDL);
    }
    

    public boolean log_metabolism_reaction(DTimeStamp time, String name, double W, double I,
            double sensW, double sensI, double dBG) {

        var log = name + SEP;
        if (ALWAYS_TIME_STAMP) {
            log += timeStampToString(time) + SEP;
        }
        log += wToString(W) + SEP;
        log += insulinToString(I) + SEP;
        log += sensitivityToString(DStrings.SENSITIVITY_W, sensW) + SEP;
        log += sensitivityToString(DStrings.SENSITIVITY_INSULIN, sensI) + SEP;
        log += dBGtoString(dBG);
        return log_insert(log + ENDL);
    }

    public boolean log_kidneys_action(DTimeStamp time, String name, double dBG) {

        var log = name + SEP;
        if (ALWAYS_TIME_STAMP) {
            log += timeStampToString(time) + SEP;
        }
        log += dBGtoString(dBG);
        return log_insert(log + ENDL);
    }

    public boolean log_delivery_activation(DTimeStamp time, String name, double value) {

        var log = name + SEP;
        if (ALWAYS_TIME_STAMP) {
            log += timeStampToString(time) + SEP;
        }
        switch (name) {
            case DStrings.PATIENT_DELIVERY_MEALS_REGULAR:
            case DStrings.PATIENT_DELIVERY_MEALS_CORRECTION:
                log += wToString(value);
                break;
            case DStrings.PATIENT_DELIVERY_INSULIN_BASE:
            case DStrings.PATIENT_DELIVERY_INSULIN_BOLUS:
            case DStrings.PATIENT_DELIVERY_INSULIN_CORRECTION:
                log += insulinToString(value);
                break;
        }
        return log_insert(log  + ENDL);
    }
    
    
    

    public boolean log_delivery_adding(String name, DDiscreteTimeFunction dFunction) {
        var log = name + SEP + discreteTimeFunctionToString(dFunction, true);
        return log_insert(log + ENDL);
    }

    public boolean log_delivery_cleaning(String name, DDiscreteTimeFunction dFunction) {
        var log = name + SEP + discreteTimeFunctionToString(dFunction, false);
        return log_insert(log  + ENDL);
    }
    
    public boolean log_drift(DTimeStamp time, String name, DDaySettings settings) {
        var log = name + SEP;
        if (ALWAYS_TIME_STAMP) {
            log += timeStampToString(time) + SEP;
        }
        log += ENDL + daySettingsToString(settings);
        return log_insert(log + ENDL); 
    }
    
}
