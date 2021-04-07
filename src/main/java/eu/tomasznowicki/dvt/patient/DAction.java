package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.raport.DLoggerPatient;
import java.util.Arrays;
import eu.tomasznowicki.dvt.time.DUtils;

abstract public class DAction {

    public final String name;
    
    protected final double[] action;

    protected DLoggerPatient dlogger = null;

    public DAction(String dName) {

        name = dName;

        action = new double[DTimeStamp.MINUTES_IN_DAY];
        Arrays.fill(action, 0);
    }

    public void setLogger(DLoggerPatient dLogger) {

        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    //DoubleDAY to tablica double[1440]
    public void setAction(double... dSettings) {

        DUtils.setDoubleDAY(action, dSettings);
    }

    public double[] getAction() {

        return action;
    }
    
    public abstract double action(DTimeStamp dTimeStamp, double bloodGlucose);
    
}
