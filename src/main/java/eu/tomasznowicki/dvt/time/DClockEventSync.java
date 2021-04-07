package eu.tomasznowicki.dvt.time;

import java.util.EventObject;

public class DClockEventSync extends EventObject {

    public final DTimeStamp time; //Tutaj czas

    public DClockEventSync(Object source, DTimeStamp dTimeStamp) {
        
        super(source);
        
        time = dTimeStamp;
    }

}
