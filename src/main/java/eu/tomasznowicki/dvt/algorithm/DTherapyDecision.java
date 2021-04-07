package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DInsulin;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.biocyb.DNutrition;
import eu.tomasznowicki.dvt.raport.DLoggerController;
import eu.tomasznowicki.dvt.raport.ToStrings;
import static eu.tomasznowicki.dvt.raport.ToStrings.ARROW;
import static eu.tomasznowicki.dvt.raport.ToStrings.SEP;
import static eu.tomasznowicki.dvt.raport.ToStrings.bolusToString;
import static eu.tomasznowicki.dvt.raport.ToStrings.insulinToString;
import static eu.tomasznowicki.dvt.raport.ToStrings.mealToString;
import static eu.tomasznowicki.dvt.raport.ToStrings.wToString;
import eu.tomasznowicki.dvt.time.DTimeStamp;

public class DTherapyDecision {

    final public DTimeStamp time;

    public DMeal mealRegular, mealCorrection;
    public DBolus bolusRegular, bolusCorrection;

    private final DLoggerController dlogger;

    public DTherapyDecision(DTimeStamp dTimeStamp, DLoggerController dLogger) {

        mealRegular = mealCorrection = null;
        bolusRegular = bolusCorrection = null;
        time = dTimeStamp;
        dlogger = dLogger;
    }

    public DTherapyDecision(DTimeStamp dTimeStamp) {
        this(dTimeStamp, null);
    }

    public boolean empty() {
        return bolusRegular == null && bolusCorrection == null
                && mealRegular == null && mealCorrection == null;
    }

    public static DTherapyDecision ignoring(DTimeStamp dTimeStamp, DLoggerController dLogger) {

        if (dLogger != null) {
            dLogger.log_info("Ignoring:");
        }
        return new DTherapyDecision(dTimeStamp, null);
    }

    public final void insertMealRegular(DMeal dMeal) {

        mealRegular = dMeal;

        if (dlogger != null) {
            var info = "Meal regular:" + SEP + mealToString(mealRegular);
            dlogger.log_info(info);
        }

    }

    public final void insertMealCorrection(double ww) {

        mealCorrection = DNutrition.getCorrectionMeal(time, ww);

        if (dlogger != null) {
            var info = "Meal correction: " + SEP + wToString(ww) + SEP + ARROW + SEP
                    + mealToString(mealCorrection);
            dlogger.log_info(info);
        }
    }

    public final void insertBolusRegular(double dose, DInsulin.DTypeInsulin type) {

        bolusRegular = DBolus.buildStandard(time, dose, type);

        if (dlogger != null) {
            var info = "Bolus regular:" + SEP + insulinToString(dose)
                    + SEP + ARROW + SEP + bolusToString(bolusRegular);
            dlogger.log_info(info);
        }
    }

    public final void insertBolusCorrection(double dose, DInsulin.DTypeInsulin type) {

        bolusCorrection = DBolus.buildStandard(time, dose, type);

        if (dlogger != null) {
            var info = "Bolus correction:" + SEP + insulinToString(dose)
                    + SEP + ARROW + SEP + bolusToString(bolusCorrection);
            dlogger.log_info(info);
        }
    }
    
    public final void regardActiveInsulin(double dose){
        
        double regard = dose > 0 ? dose : 0;
        
        var info = "Regarding active insulin " + insulinToString(regard) + SEP;
                
        if(bolusCorrection != null && regard > 0){
            
            info += "Bolus correction " + ToStrings.bolusToString(bolusCorrection) + " -> ";
            
            var newDose = bolusCorrection.dose - regard;
        
            if(newDose > 0) {
                
                var bolus = DBolus.buildStandard(bolusCorrection.time, newDose, bolusCorrection.typeInsulin);
                regard = 0; 
                bolusCorrection = bolus;
                
            } else {
                
                bolusCorrection = null;
                regard = - newDose; //Jeszce ot
            }
            
            info += bolusCorrection!=null ? ToStrings.bolusToString(bolusCorrection) : "0";
            
            
        }
        
        
        if(bolusRegular !=null && regard > 0) {
            
            info += "Bolus regular " + ToStrings.bolusToString(bolusRegular) + " -> ";
            
            var newDose = bolusCorrection.dose - regard;
            
            if(newDose > 0) {
                var bolus = DBolus.buildStandard(bolusCorrection.time, newDose, bolusCorrection.typeInsulin);
                regard = 0;
                bolusRegular = bolus;
            } else {
                bolusRegular  = null;
                regard = - newDose;
            }
            
            info += bolusRegular != null ? ToStrings.bolusToString(bolusRegular) : "0";
            
        }
        
        
        
        if(regard > 0){
            info += "!Still left " + insulinToString(regard);
        }
        
        if (dlogger != null) {
            dlogger.log_info(info);
        }
        
        
    }
    
    
    

    public final void redoseBolusRegular(double change) {

        var info = "Bolus regular edit dose:" + SEP;

        if (bolusRegular != null) {
            var dose = bolusRegular.dose + change;
            var bolus = DBolus.buildStandard(time, dose, bolusRegular.typeInsulin);
            bolusRegular = bolus;

            info += insulinToString(change) + SEP + ARROW + SEP
                    + insulinToString(dose) + SEP + ARROW + SEP + bolusToString(bolus);

        } else {
            info += "Impossible!";
        }

        if (dlogger != null) {
            dlogger.log_info(info);
        }
    }
    
    
    
    

    public void retimeMealRegular(int ahead) {

        var info = "Meal regular retiming: " + ahead;

        if (ahead > 0) {
            mealRegular.shift(ahead);
            info += " done.";
        } else {
            info += "Impossible!";
        }

        if (dlogger != null) {
            dlogger.log_info(info);
        }

    }

    public void retimeBolusRegular(int ahead) {

        var info = "Bolus regular retiming " + ahead;

        if (ahead > 0) {
            bolusRegular.shift(ahead);
            info += " done.";
        } else {
            info += "Impossible!";
        }

        if (dlogger != null) {
            dlogger.log_info(info);
        }
    }

}
