package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.time.DTimeStamp;

/**
 * Struktura typu czas - wartość glikemii
 *
 * @author www.tomasznowicki.eu
 */
public final class DGlycemia implements Comparable<DGlycemia> {

//    public enum DTrend {
//        DECREASE_QUICK, DECREASE, STABLE, INCREASE, INCREASE_QUICK, UNKNOWN
//    };


    public final DTimeStamp time; //Znacznik czasowy
    public final double value; //Wartość glikemii
    public final Double rateShort, rateLong; //Zmiana
    public final Double averageShort, averageLong; //Wartość średnia
    


//public final DTrend trend;

    public DGlycemia(DTimeStamp dTimeStamp, double dValue,
            Double dRateShort, Double dRateLong,
            Double dAverageShort, Double dAverageLong) {

        time = dTimeStamp;
        value = dValue >= 0 ? dValue : 0;
        rateShort = dRateShort;
        rateLong = dRateLong;
        averageShort = dAverageShort;
        averageLong = dAverageLong;
        
    }
    
    public DGlycemia(DTimeStamp dTimeStamp, double dValue) {
        this(dTimeStamp, dValue, null, null, null, null);
    }
    
    

    public static double bloodGlucoseToCorrect(double bloodGlucose,
            double bloodGlucoseMin, double bloodGlucoseMax) {

        if (bloodGlucose > 0 && bloodGlucoseMin > 0 && bloodGlucoseMax > 0
                && bloodGlucoseMax > bloodGlucoseMin) {

            //Poprawne dane do obliczeń
            if (bloodGlucose > bloodGlucoseMax) {

                return bloodGlucose - bloodGlucoseMax; // > 0

            } else if (bloodGlucose < bloodGlucoseMin) {

                return bloodGlucose - bloodGlucoseMin; // < 0

            } else {

                return 0; //Nie ma potrzeby robić korekty
            }

        } else {

            //Niepoprawne dane do obliczeń
            return 0;
        }

    }
    
    
    @Override
    public int compareTo(DGlycemia dGlycemia) {
        return time.compareTo(dGlycemia.time);
    }
    
     
    
    @Override
    public String toString() {

        return ToStrings.glycemiaToString(this);
    }

}
