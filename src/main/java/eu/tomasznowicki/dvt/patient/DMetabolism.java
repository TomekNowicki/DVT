package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DUtils;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.raport.DLoggerPatient;
import eu.tomasznowicki.dvt.raport.ToStrings;
import java.util.Arrays;

/**
 * Skrzynka metaboliczna, symulacja minutowa
 *
 * Wrzucamy: wymienniki pochodzące z ww, wt, wb oraz insulinę Jest to
 * zamienieniane na pozom cukry we krwi bloodGlucose [mg/dL] oraz pozoiom
 * insuliny we krwi bi [j]
 *
 * Skrzynka posługuje się minutowymi współczynnikami metabolicznymi Skrzynka
 * metaboliczna, symulacja minutowa
 *
 * Wrzucamy: wymienniki pochodzące z ww, wt, wb oraz insulinę Jest to
 * zamienieniane na pozom cukry we krwi bloodGlucose [mg/dL] oraz pozoiom
 * insuliny we krwi bi [j]
 *
 * Skrzynka posługuje się minutowymi współczynnikami metabolicznymi Wszystkie
 * współczynniki muszą być większe od zera
 *
 * @author tomasznowicki.eu
 */
public class DMetabolism {

    public final String name;

    /**
     * Wrażliwość na insulinę. Ile glikemii obniża 1j insuliny. Nie przyjmuje
     * ujemnych.
     */
    private final double[] sensitivityInsulin;

    /**
     * Wrażliwość na wymienniki. Ile glikemi podnosic 1 wymiennik. Tak samo
     * traktuje się wymiennik węglowodanowy, tłuszczowy i białkowy. Nie
     * przyjmuje ujemnych.
     */
    private final double[] sensitivityW;

    private DLoggerPatient dlogger = null;

    public DMetabolism(String dName) {

        name = dName;
        sensitivityInsulin = new double[DTimeStamp.MINUTES_IN_DAY];
        sensitivityW = new double[DTimeStamp.MINUTES_IN_DAY];

        Arrays.fill(sensitivityInsulin, 0);
        Arrays.fill(sensitivityW, 0);
    }

    public void setLogger(DLoggerPatient dLogger) {

        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    public void setSensitivityInsulin(double... dSettings) {

        DUtils.setDoubleDAY(sensitivityInsulin, dSettings);

    }

    public void setSensitivityW(double... dSettings) {

        DUtils.setDoubleDAY(sensitivityW, dSettings);
    }

    public double[] getSensitivityInsulin() {

        return sensitivityInsulin;
    }

    public double[] getSensitivityW() {

        return sensitivityW;
    }


    /*
     * Wykonuje obliczenia dla bieżącej minuty, czyli w przedziale [snapshot-1,
     * snapshot]
     * tutaj bloodGlucose może być ujemne
     */
    public double reaction(DTimeStamp dTimeStamp, double w, double insulin) {

        // "The shorter answer is doing the thing." E.H.
        //
        double sW = sensitivityW[dTimeStamp.point];
        double sI = sensitivityInsulin[dTimeStamp.point];

        double bloodGlucoseChange = sW * w - sI * insulin; //Bilans 

        if (dlogger != null) {
            dlogger.log_metabolism_reaction(dTimeStamp, name, w, insulin, sW, sI, bloodGlucoseChange);
        }

        return bloodGlucoseChange;
    }

    @Override
    public String toString() {
        return ToStrings.metabolismToString(this);
    }

}


/*
public void reaction(DTimeStamp dTimeStamp, double w, double insulin) {

        double sW = sensitivityW[dTimeStamp.point];
        double sI = sensitivityInsulin[dTimeStamp.point];
        
        if(dlogger!=null) dlogger.log_before(dTimeStamp, sW, sI, bloodGlucose, bi);
        
        glycemiaIncrease = sW * w; // wzrost
        glycemiaDecrease = -sI * (bi + insulin); // spadek 
        bi = 0; //Jeżeli coś było to zostało teraz wykorzystane

        glycemiaBilans = bloodGlucose + glycemiaIncrease + glycemiaDecrease;

        if (glycemiaBilans < 0 && sI > 0) //Do tego nie powinno dochodzić
        {
            //Część insuliny nie została wykorzystana, zostaje w systemie
            bi = -glycemiaBilans / sI;
            glycemiaBilans = 0;
        }

        bloodGlucose = glycemiaBilans;

        
        if(dlogger!=null) dlogger.log_after(bloodGlucose, bi);

    }
 */
