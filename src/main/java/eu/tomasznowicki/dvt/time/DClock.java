package eu.tomasznowicki.dvt.time;

import static eu.tomasznowicki.dvt.raport.ToStrings.clockToString;
import java.util.ArrayList;

public final class DClock {
    
    public static String MESSAGE_EXCEPTION_CLOCK = "!Time overflow";
    private long minute;
    private DTimeStamp time;
    private ArrayList<DClockListener> listeners;
    
    public DClock(DTimeStamp dTimeStamp) {
        minute = dTimeStamp.total;
        time = dTimeStamp;
        listeners = new ArrayList<>();
    }
    
    public DClock() {
        this(new DTimeStamp());
    }
    
    public DTimeStamp time() {
        return time;
    }
    
    public boolean addListener(DClockListener dClockListener) {
        if (!listeners.contains(dClockListener)) {
            if (listeners.add(dClockListener)) {
                dClockListener.syncReceived(new DClockEventSync(this, time));
                return true;
            }
        }
        return false;
    }
    
    public int removeListener(DClockListener dClockListener) {
        int c = 0;
        while (listeners.remove(dClockListener)) {
            c++;
        }
        return c;
    }
    
    public int numberOfListeners() {
        return listeners.size();
    }
    
    private void synchronizeListeners() {
        var event = new DClockEventSync(this, time);
        listeners.forEach((listener) -> {
            listener.syncReceived(event);
        });
    }
    
    private void activeteCGM() {
        var event = new DClockEventAction(this);
        listeners.forEach((listener) -> {
            listener.tickCGM(event);
        });
    }
    
    private void activateControllers() {
        var event = new DClockEventAction(this);
        listeners.forEach((listener) -> {
            listener.tickController(event);
        });
    }
    
    private void activateOutfit() {
        var event = new DClockEventAction(this);
        listeners.forEach((listener) -> {
            listener.tickOutfit(event);
        });
    }
    
    private void activatePatient() {
        var event = new DClockEventAction(this);
        listeners.forEach((listener) -> {
            listener.tickPatient(event);
        });
    }
    
    public void tictoc() throws DClockException {
        
        if (minute < Long.MAX_VALUE) {
            time = new DTimeStamp(++minute);
            synchronizeListeners();
            activeteCGM();
            activateControllers();
            activateOutfit();
            activatePatient();
        } else {
            throw new DClockException();
        }
    }
    
    @Override
    public String toString() {
        return clockToString(this);
    }
    
}


/*
 * nie da się cofnąć
 * The instance of the DClock class internally counts down minutes of the
 * therapy. Then the minutes are converted to the DTimeStamp object.
 * <p>
 * The virtual therapy is simulated in minute resolution. The participants of
 * the therapy need a common minutes counter. This is what an object of the
 * class Clock does.
 * <p>
 * Zrobić zmienną DTimeStamp i tu trzymać aktulany czas. i go podawać przy
 * próbie odczytu
 *
 * Dla aplikacji jednowątkowej
 * <p>
 * Zegar też jest używany do wewnętrznych przeliczeń. Ale wtedy nie ma lisnerów
 *
 */
