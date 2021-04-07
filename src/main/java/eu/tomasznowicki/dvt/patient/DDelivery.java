package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.biocyb.DTimeFunctionable;
import eu.tomasznowicki.dvt.raport.DLoggerPatient;
import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.raport.ToStrings;
import java.util.ArrayList;

public class DDelivery {

    public final String name;

    private final ArrayList<DDiscreteTimeFunction> function;

    private DLoggerPatient dlogger = null;

    public DDelivery(String dName) {

        name = dName;
        function = new ArrayList<>();
    }

    public void setLogger(DLoggerPatient dLogger) {

        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    public boolean add(DTimeFunctionable dTimeFunctionable) {

        boolean success = true;
        DDiscreteTimeFunction fun = null;

        if (dTimeFunctionable != null) {
            fun = dTimeFunctionable.toTimeFunction();
        } else {
            success = false;
        }

        if (fun != null) {
            success = function.add(fun);
        } else {
            success = false;
        }

        if (dlogger != null) {

            if (success) {
                dlogger.log_delivery_adding(name, fun);
            } else {
                dlogger.log_failed();
            }
        }

        return success;
    }

    public int size(){
        return function.size();
    }
    
    public DDiscreteTimeFunction[] getFuntions() {
        

        return function.toArray(new DDiscreteTimeFunction[function.size()]);
    }

    public double activated(DTimeStamp dTimeStamp) {

        double acc = 0;

        for (DDiscreteTimeFunction fun : function) {
            acc += fun.valueAtTimeStamp(dTimeStamp);
        }

        if (dlogger != null) {
            dlogger.log_delivery_activation(dTimeStamp, name, acc);
        }

        return acc;
    }

    public void clean(DTimeStamp dTimeStamp) {

        if (!function.isEmpty()) {

            var iterator = function.iterator();

            while (iterator.hasNext()) {

                var fun = (DDiscreteTimeFunction) iterator.next();

                if (fun.drainedAtTimeStamp(dTimeStamp)) {

                    iterator.remove();

                    if (dlogger != null) {
                        dlogger.log_delivery_cleaning(name, fun);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return ToStrings.deliveryToString(this);
    }

}


/*
To będzie potrzebne do algorytmów przewidywania


    public double liverAtRangeTime(DTimeStamp startTime, DTimeStamp endTime) {

        if (startTime.total >= endTime.total) {
            return 0; //Brak przedziału
        }
        short span = (short) (endTime.total - startTime.total); //Całkowity span
        short startPoint0 = startTime.point, startPoint1 = 0; //Zawsze
        short endPoint0 = (short) (startPoint0 + span), endPoint1 = 0;

        if (endPoint0 > DTimeStamp.DAY) //Przekracza granice doby
        {
            endPoint1 = (short) (endPoint0 - DTimeStamp.DAY);
            endPoint0 = DTimeStamp.DAY;
        }

        double counter = 0;
        for (short i = startPoint0; i < endPoint0; i++) {
            counter += actionLiver[i];
        }
        for (short i = startPoint1; i < endPoint1; i++) {
            counter += actionLiver[i];
        }
        return counter;
    }




    public double wAtRangeTime(DTimeStamp startTime, DTimeStamp endTime) {

        return eventAtRangeTime(functionMeal, startTime, endTime);
    }



    private double eventAtRangeTime(Deque<DEvent> eventDeque, DTimeStamp startTime, DTimeStamp endTime) {

        double counter = 0;

        for (DDiscreteTimeFunction e : eventDeque) {
            counter += e.valueAtTimeRange(startTime, endTime);
        }

        return counter;
    }


    public double baseAtRangeTime(DTimeStamp startTime, DTimeStamp endTime) {

        return eventAtRangeTime(functionBase, startTime, endTime);
    }


    public double bolusAtRangeTime(DTimeStamp startTime, DTimeStamp endTime) {

        return eventAtRangeTime(functionBolus, startTime, endTime);
    }


 */
