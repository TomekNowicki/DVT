package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.therapy.DTherapyAttendee;
import eu.tomasznowicki.dvt.time.DClockEventAction;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.raport.DLoggerPatient;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin.*;
import eu.tomasznowicki.dvt.raport.DLoggerInput;
import eu.tomasznowicki.dvt.raport.DLoggerOutput;
import eu.tomasznowicki.dvt.raport.DStrings;

public class DPatientCore extends DTherapyAttendee {

    public final static int //
            BG_FATAL_MIN = 30,
            BG_ALARM_MIN = 50,
            BG_KIDNEY_THRESHOLD = 160,
            BG_ALARM_MAX = 260,
            BG_FATAL_MAX = 400;

    private double BG; // [mg/dL]
    private boolean alive;

    private final DLiver liver;
    private final DIndependentIntake intake;
    private final DKidneys kidneys;
    private final DMetabolism metabolizm;

    private final DDelivery deliveryMealsRegular;
    private final DDelivery deliveryMealsCorrection;
    private final DDelivery deliveryInsulinBase;
    private final DDelivery deliveryInsulinRegular;
    private final DDelivery deliveryInsulinCorrection;

    //TO DO: Reakcja na hiper i hipo
    protected DLoggerPatient dlogger = null;
    private DLoggerInput dlogger_in = null;
    private DLoggerOutput dlogger_out = null;

    public DPatientCore(double dBloodGlucose) {

        BG = dBloodGlucose;

        liver = new DLiver(DStrings.PATIENT_LIVER);
        intake = new DIndependentIntake(DStrings.PATINET_INTAKE);
        kidneys = new DKidneys(DStrings.PATIENT_KIDNEYS);
        metabolizm = new DMetabolism(DStrings.PATIENT_METABOLISM);

        deliveryMealsRegular = new DDelivery(DStrings.PATIENT_DELIVERY_MEALS_REGULAR);
        deliveryMealsCorrection = new DDelivery(DStrings.PATIENT_DELIVERY_MEALS_CORRECTION);
        deliveryInsulinBase = new DDelivery(DStrings.PATIENT_DELIVERY_INSULIN_BASE);
        deliveryInsulinRegular = new DDelivery(DStrings.PATIENT_DELIVERY_INSULIN_BOLUS);
        deliveryInsulinCorrection = new DDelivery(DStrings.PATIENT_DELIVERY_INSULIN_CORRECTION);

        liveControl();
    }

    public void setLogger(DLoggerPatient dLogger) {

        if (dlogger == null) {
            dlogger = dLogger;
            liver.setLogger(dLogger);
            kidneys.setLogger(dLogger);
            metabolizm.setLogger(dLogger);
            deliveryMealsRegular.setLogger(dLogger);
            deliveryMealsCorrection.setLogger(dLogger);
            deliveryInsulinBase.setLogger(dLogger);
            deliveryInsulinRegular.setLogger(dLogger);
            deliveryInsulinCorrection.setLogger(dLogger);
        }
    }

    public void setLoggerInput(DLoggerInput dLogger) {
        if (dlogger_in == null) {
            dlogger_in = dLogger;
        }
    }

    public void setLoggerOutput(DLoggerOutput dLogger) {
        if (dlogger_out == null) {
            dlogger_out = dLogger;
        }

    }

    public void log_yourself() {
        if (dlogger != null) {
            dlogger.log_patient(this);
        }
    }

    private void liveControl() {
        alive = (BG >= BG_FATAL_MIN && BG <= BG_FATAL_MAX);
    }

    public boolean alarm() {
        return BG < BG_ALARM_MIN || BG > BG_ALARM_MAX;
    }

    public boolean isAlive() {
        return alive;
    }

    public double getBG() {
        return BG;
    }

    public void setActionLiver(double... dSettings) {
        liver.setAction(dSettings);
    }

    public double[] getActionLiver() {
        return liver.getAction();
    }

    public void setActionKidneys(double... dSettings) {
        kidneys.setAction(dSettings);
    }
    
    public void setActionIntake(double ... dSettings){
        intake.setAction(dSettings);
    }
    
    public double [] getActionIntake(){
        return intake.getAction();
    }

    public double[] getActionKidneys() {
        return kidneys.getAction();
    }

    public void setSensitivityInsulin(double... dSettings) {
        metabolizm.setSensitivityInsulin(dSettings);
    }

    public double[] getSensitivityInsulin() {
        return metabolizm.getSensitivityInsulin();
    }

    public void setSensitivityW(double... dSettings) {
        metabolizm.setSensitivityW(dSettings);
    }

    public double[] getSensitivityW() {
        return metabolizm.getSensitivityW();
    }

    private void takeMeal(DMeal dMeal, String info, DDelivery dDelivery) {
        if (dlogger != null) {
            dlogger.log_function(clockTime, DStrings.PATIENT_TAKING_MEAL + SEP + info);
            dlogger.log_patient_adding_meal(clockTime, dMeal);
        }
        if (dMeal != null && dMeal.time.equals(clockTime)) {
            var success = dDelivery.add(dMeal); // -> log
            if(success && dlogger_in != null){
                dlogger_in.log_patient_input(dMeal);
            }
        } else {
            if (dlogger != null) {
                dlogger.log_failed();
            }
        }
    }

    private void takeBolus(DBolus dBolus, String info, DDelivery dDelivery) {
        if (dlogger != null) {
            dlogger.log_function(clockTime, DStrings.PATIENT_TAKING_INSULIN + SEP + info);
            dlogger.log_patient_adding_bolus(clockTime, dBolus);
        }
        if (dBolus != null && dBolus.time.equals(clockTime)
                && dBolus.typeBolus == DBolus.DTypeBolus.STANDARD) {
            var success = dDelivery.add(dBolus); // -> log
            if(success && dlogger_in != null){
                dlogger_in.log_patient_input(dBolus);
            }
        } else {
            if (dlogger != null) {
                dlogger.log_failed();
            }
        }
    }

    public void takeMealRegular(DMeal dMeal) {
        if (clockTime != null) {
            takeMeal(dMeal, REGULAR.toString(), deliveryMealsRegular); // -> log
        }
    }

    public void takeMealCorrection(DMeal dMeal) {
        if (clockTime != null) {
            takeMeal(dMeal, CORRECTION.toString(), deliveryMealsCorrection); // -> log
        }
    }

    public void takeBolusRegular(DBolus dBolus) {
        if (clockTime != null) {
            takeBolus(dBolus, REGULAR.toString(), deliveryInsulinRegular); // -> log
        }
    }

    public void takeBolusBase(DBolus dBolus) {
        if (clockTime != null) {
            takeBolus(dBolus, BASE.toString(), deliveryInsulinBase); // -> log
        }
    }

    public void takeBolusCorrection(DBolus dBolus) {
        if (clockTime != null) {
            takeBolus(dBolus, CORRECTION.toString(), deliveryInsulinCorrection); // -> log
        }
    }

    @Override
    public void tickPatient(DClockEventAction event) {
        if (event.getSource() == clock) {
            if (alive) {
                if (dlogger_out != null) {
                    dlogger_out.log_patient_output(clockTime.shift(-1), BG); // -> Stary cukier, nowy czas
                }
                liveOn(); // -> Nowy cukier
                liveControl();
            }
        }
    }

    private void liveOn() {
        //"But man is not made for defeat. A man can be destroyed but not defeted." ~EH
        //
        if (dlogger != null) {
            dlogger.log_function(clockTime, DStrings.PATIENT_PROCESSING);
            dlogger.log_patient_BG(clockTime, BG);
        }
        //Wymienniki uaktywnione w tej minucie
        var w0 = liver.action(clockTime, BG); // -> log
        var w1 = deliveryMealsRegular.activated(clockTime); // -> log
        var w2 = deliveryMealsCorrection.activated(clockTime); // -> log
        var w3 = intake.action(clockTime, BG);
        var w = w0 + w1 + w2 - w3;
        //Insulina uaktywniona w tej minucie
        var i0 = deliveryInsulinBase.activated(clockTime); // -> log
        var i1 = deliveryInsulinRegular.activated(clockTime); // -> log
        var i2 = deliveryInsulinCorrection.activated(clockTime); // -> log
        var insulin = i0 + i1 + i2;
        //Zmiany glikemi
        double dBGMetabolic = metabolizm.reaction(clockTime, w, insulin); //->log
        double dBGKidneys = kidneys.action(clockTime, BG); // -> log
        //Bilans
        BG = BG + dBGMetabolic - dBGKidneys;
        if (dlogger != null) {
            dlogger.log_patient_BG(clockTime, BG);
        }
    }

    @Override
    protected void clean() {
        if (dlogger != null) {
            dlogger.log_function(clockTime, DStrings.PATIENT_CLEANING);
        }
        deliveryMealsRegular.clean(clockTime); // -> logging
        deliveryMealsCorrection.clean(clockTime); // -> logging
        deliveryInsulinBase.clean(clockTime); // -> logging
        deliveryInsulinRegular.clean(clockTime); // -> logging
        deliveryInsulinCorrection.clean(clockTime); // -> logging
    }

    @Override
    public String toString() {
        return timeStampToString(clockTime) + SEP + BGtoString(BG) + ENDL
                + actionToString(liver, FORMAT_W) + ENDL
                + actionToString(intake, FORMAT_W) + ENDL
                + actionToString(kidneys, FORMAT_DBG) + ENDL
                + metabolismToString(metabolizm) + ENDL
                + deliveryToString(deliveryMealsRegular) + ENDL
                + deliveryToString(deliveryMealsCorrection) + ENDL
                + deliveryToString(deliveryInsulinBase) + ENDL
                + deliveryToString(deliveryInsulinRegular) + ENDL
                + deliveryToString(deliveryInsulinCorrection);
    }
}

/*
    public double futureW(int horizont) {

        DTimeStamp startTime = clock.clockTime();
        DTimeStamp endTime = startTime.shift(horizont);
        return delivery.wAtRangeTime(startTime, endTime);
    }



    public double futureLiver(int horizont) {

        DTimeStamp startTime = clock.clockTime();
        DTimeStamp endTime = startTime.shift(horizont);
        return delivery.liverAtRangeTime(startTime, endTime);
    }



    public double futureInsulinBolus(int horizont) {

        DTimeStamp startTime = clock.clockTime();
        DTimeStamp endTime = startTime.shift(horizont);
        return delivery.bolusAtRangeTime(startTime, endTime);
    }


        public double futureInsulinBase(int horizont) {

        DTimeStamp startTime = clock.clockTime();
        return delivery.baseAtRangeTime(startTime, startTime.shift(horizont));
    }

 */
