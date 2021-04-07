package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.time.DTimeStamp;

class DKidneys extends DAction {

    public DKidneys(String dName) {
        super(dName); 
    }

    @Override
    public double action(DTimeStamp dTimeStamp, double bloodGlucose) {

        double bloodGlucoseChange = 0;

        if (bloodGlucose > DPatientCore.BG_KIDNEY_THRESHOLD) {

            bloodGlucoseChange = action[dTimeStamp.point];
        }

        if (dlogger != null) {
            dlogger.log_kidneys_action(dTimeStamp, name, bloodGlucoseChange);
        }

        return bloodGlucoseChange;
    }
    
       @Override
    public String toString(){
        return ToStrings.actionToString(this, ToStrings.FORMAT_DBG);
    }

}
