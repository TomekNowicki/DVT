package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DTimeStamp;

public class DIndependentIntake extends DAction {

    public DIndependentIntake(String dName) {
        super(dName);
    }

    //Ile węglowodanów pobierze organizm bez insuliny
    @Override
    public double action(DTimeStamp dTimeStamp, double bloodGlucose) {
        //

        var value = action[dTimeStamp.point];

        //W zależności od glikemii zmniejszyć lub zwiększyć
        if (dlogger != null) {
            dlogger.log_independet_intake_activation(dTimeStamp, name, value);
        }

        return value;
    }

}
