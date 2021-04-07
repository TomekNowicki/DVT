package eu.tomasznowicki.dvt.therapy;

import eu.tomasznowicki.dvt.time.DClockException;

public class DAutoTherapy extends DTherapy {

    @Override
    public boolean nextMinute() {
        try {
            clock.tictoc();
            return patient.isAlive();
        } catch (DClockException ex) {
            return false; // -> Przekroczenie zakresu long
        }
    }
}
