package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import java.io.FileWriter;

public class DLoggerInput extends DLogger.DLoggerAttendee {

    private DTimeStamp lastTime;
    private double insulin, ww, wt, wb, wbt; //Akumulatory

    public DLoggerInput(FileWriter fileWriter) {
        super(fileWriter);
        lastTime = null;
        insulin = ww = wt = wb = wbt = 0;
    }

    @Override
    protected boolean before_close() {

        if (lastTime != null && (insulin > 0 || ww > 0 || wt > 0 || wb > 0 || wbt > 0)) {
            return flush();
        } else {
            return true;
        }
    }

    private boolean flush() {
        var log = timeStampToString(lastTime) + SEP
                + String.format(LOC, FORMAT_I, insulin) + SEP
                + String.format(LOC, FORMAT_W, ww) + SEP
                + String.format(LOC, FORMAT_W, wt) + SEP
                + String.format(LOC, FORMAT_W, wb) + SEP
                + String.format(LOC, FORMAT_W, wbt) + SEP;
        insulin = ww = wt = wb = wbt = 0;
        return log_insert(log + ENDL);
    }

    public boolean log_patient_input(DBolus bolus) {
        if (lastTime == null) {
            lastTime = bolus.time;
            insulin += bolus.dose;
            return true;
        } else if (lastTime.compareTo(bolus.time) == 0) {
            insulin += bolus.dose;
            return true;
        } else if (lastTime.compareTo(bolus.time) < 0) {
            var success = flush();
            lastTime = bolus.time;
            insulin += bolus.dose;
            return success;
        } else { //lastTime.compareTo(bolus.time) > 0 -> Spóźniony log
            return log_insert(ERROR + ENDL);
        }
    }

    public boolean log_patient_input(DMeal meal) {
        if (lastTime == null) {
            lastTime = meal.time;
            ww += meal.ww;
            wt += meal.wt;
            wb += meal.wb;
            wbt += meal.wbt;
            return true;
        } else if (lastTime.compareTo(meal.time) == 0) {
            ww += meal.ww;
            wt += meal.wt;
            wb += meal.wb;
            wbt += meal.wbt;
            return true;
        } else if (lastTime.compareTo(meal.time) < 0) {
            var success = flush();
            lastTime = meal.time;
            ww += meal.ww;
            wt += meal.wt;
            wb += meal.wb;
            wbt += meal.wbt;
            return success;
        } else { //lastTime.compareTo(bolus.time) > 0 -> Spóźniony log
            return log_insert(ERROR + ENDL);
        }
    }

}
