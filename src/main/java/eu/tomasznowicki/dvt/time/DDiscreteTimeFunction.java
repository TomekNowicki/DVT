package eu.tomasznowicki.dvt.time;


import eu.tomasznowicki.dvt.raport.ToStrings;
import java.util.Arrays;



/**
 * Rozłożone w czasie wydażenie takie jak posilek, czy bolus. Zaczyna się w
 * chwili time. I trwa span minut.
 * <p>
 * Funkcja dyskretna minta po minucie
 * <p>
 * Pacjent przyjmuje posiłki oraz insulię jako takie eventy.
 *
 * @author www.tomasznowicki.eu
 */

public final class DDiscreteTimeFunction {

    public final DTimeStamp openTime, closeTime;
    public final double[] values; //Wartość dla każdej minuty
    public final double sum; //Suma wartości

    public DDiscreteTimeFunction(DTimeStamp dTime, double... dValues) {

        openTime = dTime;
        closeTime = openTime.shift(dValues.length);
        values = new double[dValues.length];
        System.arraycopy(dValues, 0, values, 0, dValues.length);
        sum = Arrays.stream(values).sum();
    }

    public boolean activeAtTimeStamp(DTimeStamp dTimeStamp) {

        return dTimeStamp.compareTo(openTime) > 0 && dTimeStamp.compareTo(closeTime) <= 0;
    }

    public boolean drainedAtTimeStamp(DTimeStamp dTimeStamp) {

        return dTimeStamp.compareTo(closeTime) > 0;
    }

    public double valueAtTimeStamp(DTimeStamp dTimeStamp) {

        if (!activeAtTimeStamp(dTimeStamp)) {

            return 0;

        } else {

            var minute = (int) (dTimeStamp.total - openTime.total);
            return values[minute - 1];
        }
    }

    public static DDiscreteTimeFunction join(DDiscreteTimeFunction... dTimeFunctions) {

        //Znalezienie rozpiętości nowego eventu
        var minTime = dTimeFunctions[0].openTime;
        var maxTime = dTimeFunctions[0].closeTime;

        for (var function : dTimeFunctions) {

            if (function.openTime.compareTo(minTime) < 0) {
                minTime = function.openTime;
            }

            if (function.closeTime.compareTo(maxTime) > 0) {
                maxTime = function.closeTime;
            }

        }

        //Składanie eventow
        var joinValues = new double[(int) (maxTime.total - minTime.total)];
        Arrays.fill(joinValues, 0);

        var localClock = new DClock(minTime);
        //DTimeStamp localTime = null;

        for (int i = 0; i < joinValues.length; i++) {

            try {

                localClock.tictoc();
                
                var localTime = localClock.time();
                
                for (DDiscreteTimeFunction function : dTimeFunctions) {
                    
                    joinValues[i] += function.valueAtTimeStamp(localTime);
                }

            } catch (DClockException ex) {
                
                //Will not occure becouse maxTime exists
            }

        }

        return new DDiscreteTimeFunction(minTime, joinValues);
    }


    @Override
    public String toString() {
        return ToStrings.discreteTimeFunctionToString(this, true);
    }
}


/*

    public double valueAtTimeRange(DTimeStamp dTime0, DTimeStamp dTime1) {

        
        int m0 = (int) (dTime0.total - openTime.total);
        int m1 = (int) (dTime1.total - openTime.total);

        if (m0 >= m1) {
            return 0; //Brak przedziału do sumowania        
        }
        if (m0 >= span || m1 <= 0) {
            return 0; //Przedział sumowania nie zaczepia o event
        }
        double c = 0;
        for (int i = (m0 >= 0 ? m0 : 0); i < (m1 < span ? m1 : span); i++) {
            c += values[i];
        }
        return c;
        

    }
 */
