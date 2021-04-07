package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.raport.ToStrings;



class DLiver extends DAction {

    public DLiver(String dName) {
        super(dName);
    }


    @Override
    public double action(DTimeStamp dTimeStamp, double bloodGlucose) {

        var value = action[dTimeStamp.point];
        
        //W zależności od glikemii zmniejszyć lub zwiększyć

        if (dlogger != null) {
            dlogger.log_liver_activation(dTimeStamp, name, value);
        }

        return value;
    }
    
    @Override
    public String toString(){
        return ToStrings.actionToString(this, ToStrings.FORMAT_W);
    }

}
