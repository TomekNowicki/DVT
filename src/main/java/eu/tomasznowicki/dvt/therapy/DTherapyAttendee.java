package eu.tomasznowicki.dvt.therapy;

import eu.tomasznowicki.dvt.time.DClock;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.patient.DPatientCore;

import eu.tomasznowicki.dvt.time.DClockListener;
import eu.tomasznowicki.dvt.time.DClockEventSync;
import eu.tomasznowicki.dvt.time.DClockEventAction;

abstract public class DTherapyAttendee implements DClockListener {

    protected DClock clock = null;
    protected DTimeStamp clockTime = null;
    protected DPatientCore patient = null;

    private int cleanCounter = 0;
    private final int cleanInterval = DTimeStamp.MINUTES_IN_DAY / 3;
    
    final public void setClock(DClock dClock) {
        if (clockTime == null) {
            clock = dClock;
            dClock.addListener(this); // -> set clockTime
        }
    }

    final public void setPatient(DPatientCore dPatient) {
        if (patient == null && !(this instanceof DPatientCore)) {
            patient = dPatient;
        }
    }

    abstract protected void clean();

    @Override
    public void syncReceived(DClockEventSync event) {
        
        if (event.getSource() != clock) {
            return;
        }

        clockTime = event.time;
        
                
        if (++cleanCounter >= cleanInterval) {
            clean();
            cleanCounter = 0;
        }

    }

    
    @Override
    public void tickCGM(DClockEventAction event){
        
    }
    
    @Override
    public void tickController(DClockEventAction event) {

    }

    @Override
    public void tickOutfit(DClockEventAction event) {

    }

    @Override
    public void tickPatient(DClockEventAction event) {

    }

}
