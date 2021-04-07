package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.biocyb.DGlycemia;
import eu.tomasznowicki.dvt.therapy.DTherapyAttendee;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.time.DClockEventSync;
import static eu.tomasznowicki.dvt.raport.ToStrings.controlTableToString;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

public class DControlBox extends DTherapyAttendee {

    protected final Set<DTimeStamp> plan; //Plan dobowy; dobowy znacznik czasowy, bez powtórek
    protected final Set<DTimeStamp> extra; //Do wykonania; znacznik czasowy symulacji, bez powtórek
    private boolean controlNow = false;     //ale mogą się powtórzyć między plan i ekstra

    private DGlycemia lastGlycemia = null;

    public DControlBox() {
        plan = new TreeSet<>();
        extra = new TreeSet<>();
    }

    private DTimeStamp dayControlToSimControl(DTimeStamp dTimeStamp) {
        var day = clockTime.point > dTimeStamp.point ? clockTime.day + 1 : clockTime.day;
        return new DTimeStamp(day, dTimeStamp.hour, dTimeStamp.minute);
    }

    public final boolean addControl(DTimeStamp dTimeStamp) {
        if (dTimeStamp != null) {
            return plan.add(dTimeStamp.dayTimeStamp());
        } else {
            return false;
        }
    }

    public final boolean addControls(DTimeStamp... dTimeStamps) {
        boolean success = true;
        for (DTimeStamp control : dTimeStamps) {
            success = addControl(control);
        }
        return success;
    }

    public final boolean addExtraControl(DTimeStamp dTimeStamp) {
        if (dTimeStamp != null && dTimeStamp.compareTo(clockTime) > 0) { //Tylko w przyszłości
            return extra.add(dTimeStamp);
        } else {
            return false;
        }
    }

    public final DTimeStamp[] getControls() {
        return plan.toArray(new DTimeStamp[plan.size()]);
    }

    public final DTimeStamp[] getExtraControls() {
        return extra.toArray(new DTimeStamp[extra.size()]);
    }

    public final void clearAllControls() {
        plan.clear();
        extra.clear();
    }

    public final boolean doItNow() {
        return controlNow;
    }

    public final DGlycemia prick() {
        if (patient != null) {
            lastGlycemia = new DGlycemia(clockTime, patient.getBG());
        }
        return lastGlycemia;
    }

    public final DGlycemia lastPrick() {
        if (lastGlycemia == null) {
            prick();
        }
        return lastGlycemia;
    }

    public final boolean alarm() {
        return patient.alarm();
    }

    public final DTimeStamp nextControlFrom(DTimeStamp dTimeStamp) {
        DTimeStamp next = null;
        //
        // Do zrobienia.
        // Nie łączyć tego z check_this_minute()
        //
        return next;
    }

    private void check_this_minute() { //Nadpisanie nie jest istotnoe
        controlNow = false;
        for (DTimeStamp dayControl : plan) {
            if (dayControl.point == clockTime.point) {
                controlNow = true;
                break;
            }
        }
        for (DTimeStamp simControl : extra) {
            if (simControl.equals(clockTime)) {
                controlNow = true;
                break;
            }
        }
    }

    @Override
    public void syncReceived(DClockEventSync event) {
        if (event.getSource() != clock) {
            return;
        }
        super.syncReceived(event); // -> clockTime
        check_this_minute(); // -> controlNow
    }

    @Override
    protected void clean() {
        Iterator iter = extra.iterator();
        while (iter.hasNext()) {
            if (((DTimeStamp) iter.next()).compareTo(clockTime) < 0) {
                iter.remove();
            }
        }
    }

    @Override
    public String toString() {
        return controlTableToString(this);
    }

}
