package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.patient.DAbsorptionInsulin;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;

import java.util.ArrayList;

import static eu.tomasznowicki.dvt.biocyb.DBolus.DTypeBolus.*;


import static eu.tomasznowicki.dvt.biocyb.DInsulin.getDistributionFor;
import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;

/**
 * Klasa abstrakcyjna do obsługi bolusów z różnych typów insulin. Żeby uzyskać
 * obiekt bolusa należy korzystac z metod build... . W metodach build podaje się
 * jaka to insulina i jaki typ bolusa.
 * <p>
 * Podobnie jak z posiłkiem. Zrobienie bolusa jeszcze nie oznacza podania go
 * pacjentowi.
 *
 * @author www.tomasznowicki.eu
 */
public class DBolus implements DTimeFunctionable {

    public static enum DTypeBolus {
        STANDARD, SQUARE_WAVE, DUAL_WAVE
    };

    /**
     * Co ile minut bolus z tych tworzących fale
     */
    final public static int SPLIT_INTERVAL = 5;

    /**
     * Długość fali bolusa standardowego
     */
    final private static int STANDARD_WAVE = 0;

    /**
     * Część standardowa bolusa standardowego. Cała dawka w tym bolusie
     */
    final private static double STANDARD_PART = 1;

    /**
     * Część standardowa bolusa prostokatnego
     */
    final public static double SQUARE_PART = 0;

    /**
     * Czas bolusa. Minuta symulacji. Jest to czas zaplanowania bolusa i czas
     * przyjęcia bolusa.
     */
    public final DTimeStamp time;

    /**
     * Dawka insuliny. Całkowita insulina w bolusie.
     */
    public final double dose;

    /**
     * Typ bolusa jest życzeniem. Obliczny jest bolus teoretyczny. Natomiast kit
     * realizuje go wg własnych możliwości.
     *
     * @see SPLIT_INTERVAL
     */
    public final DTypeBolus typeBolus;

    /**
     * Z jakiej insuliny jest zrobiony bolus
     */
    public final DTypeInsulin typeInsulin;

    /**
     * Część przedłużona w minutach >0
     */
    public final int waveSpan;

    /**
     * Ile insuliny puścić od razu [0,1]
     */
    public final double standardPart;

    /**
     * Dystrybucja bolusa typu STANDARD. Jeżeli to nie jest bolus prosty to jest
     * to null.
     */
    public final DDistribution insulinDist;

    /**
     * Seria bolusów prostych, która realizuje bolus z falą. Jest to model
     * teoretyczny. Kit i tak to sobie zrobi po swojemu.
     */
    public final DBolus[] series;

    //---
    private DBolus(DTimeStamp dTime, double dDose,
            DTypeBolus dBolusType, DTypeInsulin dInsulinType,
            int dWaveSpan, double dStandardPart) {

        time = dTime;
        dose = dDose;
        typeBolus = dBolusType;
        typeInsulin = dInsulinType;
        waveSpan = dWaveSpan;
        standardPart = dStandardPart;
        insulinDist = typeBolus == STANDARD ? getDistributionFor(typeInsulin, dose) : null;
        series = typeBolus != STANDARD ? split() : null;
    }

    public static final DBolus buildStandard(DTimeStamp dTime, double dDose, DTypeInsulin dInsulinType) {

        if (dDose > 0) {

            return new DBolus(dTime, dDose, STANDARD, dInsulinType, STANDARD_WAVE, STANDARD_PART);

        } else {

            return null;
        }
    }

    public static final DBolus buildSquareWave(DTimeStamp dTime, double dDose,
            DTypeInsulin dInsulinType, int dWaveSpan) {

        if (dDose > 0 && dWaveSpan > 0) {

            return new DBolus(dTime, dDose, SQUARE_WAVE, dInsulinType, dWaveSpan, SQUARE_PART);

        } else if (dDose > 0) {

            return new DBolus(dTime, dDose, STANDARD, dInsulinType, STANDARD_WAVE, STANDARD_PART);

        } else {

            return null;
        }
    }

    public static final DBolus buildDualWave(DTimeStamp dTime, double dDose,
            DTypeInsulin dInsulinType, int dWaveSpan, double standardPart) {

        if (dDose > 0 && dWaveSpan > 0 && standardPart > 0 && standardPart < 1) {

            return new DBolus(dTime, dDose, DUAL_WAVE, dInsulinType, dWaveSpan, standardPart);

        } else if (dDose > 0 && dWaveSpan > 0 && standardPart == SQUARE_PART) {

            return new DBolus(dTime, dDose, SQUARE_WAVE, dInsulinType, dWaveSpan, SQUARE_PART);

        } else if (dDose > 0 && standardPart == STANDARD_PART) {

            return new DBolus(dTime, dDose, STANDARD, dInsulinType, STANDARD_WAVE, STANDARD_PART);
        } else {
            return null;
        }

    }

    public DBolus[] getInjects() {

        return series != null ? series : new DBolus[]{this};
    }

    //Horyzont działania bolusa, aż insulina z niego sie wyzeruje
    public int getHorizont() {

        return DInsulin.getSpanFor(typeInsulin) + waveSpan;
    }

    //Przesunięcie czasu bolusa o zadaną liczbę minut
    public DBolus shift(long minutes) {

        return new DBolus(time.shift(minutes), dose, typeBolus, typeInsulin, waveSpan, standardPart);
    }

    public static DBolus join(DBolus dBolus0, DBolus dBolus1) {

        if (dBolus0 != null && dBolus1 != null
                && dBolus0.time.equals(dBolus1.time)
                && dBolus0.typeBolus == dBolus1.typeBolus
                && dBolus0.typeInsulin == dBolus1.typeInsulin
                && dBolus0.waveSpan == dBolus1.waveSpan) {

            var resultantDose = dBolus0.dose + dBolus1.dose;

            switch (dBolus0.typeBolus) {

                case STANDARD:
                    return DBolus.buildStandard(dBolus0.time, resultantDose, dBolus0.typeInsulin);
                case SQUARE_WAVE:
                    return DBolus.buildSquareWave(dBolus0.time, resultantDose, dBolus0.typeInsulin, dBolus0.waveSpan);
                case DUAL_WAVE:
                    var standard0 = dBolus0.dose * dBolus0.standardPart;
                    var standard1 = dBolus1.dose * dBolus1.standardPart;
                    var resultantStandardPart = (standard0 + standard1) / resultantDose;
                    return DBolus.buildDualWave(dBolus0.time, resultantDose, dBolus0.typeInsulin, dBolus0.waveSpan, resultantStandardPart);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    private DBolus[] split() {

        ArrayList<DBolus> s = new ArrayList<>(); //Here: series.size == 0

        if (typeBolus == STANDARD) { //Happens for: STANDARD

            s.add(this); //Terminates recursion. Here: series.size == 1

        } else { //Happens for: DUAL_WAVE and SQUARE_WAVE

            if (standardPart > 0) { //Happens for: DUAL_WAVE
                s.add(buildStandard(time, standardPart * dose, typeInsulin));
                //Here: series.size == 1
            }

            if (waveSpan > 0) { //Happens for: SQUARE_WAVE and DUAL_WAVE

                int n = waveSpan / SPLIT_INTERVAL; //Number of wave boluses

                DBolus b = buildStandard(time, (1 - standardPart) * dose / (double) n, typeInsulin);

                for (int i = 0; i < n; i++) {

                    if (i == 0 && s.size() == 1) { //Happens for DUAL_WAVE
                        s.set(0, join(s.get(0), b));
                    } else {
                        s.add(b);
                    }

                    b = b.shift(SPLIT_INTERVAL);
                }
            }

        }

        return s.toArray(new DBolus[s.size()]);
    }

    //--------------------------------------------------------------------------
    @Override
    public final double valueAt(DTimeStamp dTime) {

        if (typeBolus == STANDARD) {
            int m = DTimeFunctionable.eventMinute(time, dTime);
            return insulinDist.valueAtMinute(m);
        } else {
            double v = 0;
            for (DBolus b : series) {
                v += b.valueAt(dTime);
            }
            return v;
        }
    }

    @Override
    public final double pastAt(DTimeStamp dTime) {

        if (typeBolus == STANDARD) {
            int m = DTimeFunctionable.eventMinute(time, dTime);
            return insulinDist.pastAtMinute(m);
        } else {
            double v = 0;
            for (DBolus b : series) {
                v += b.pastAt(dTime);
            }
            return v;
        }
    }

    @Override
    public double pastAt(DTimeStamp dTime, int horizont) {

        if (typeBolus == STANDARD) {
            int from = DTimeFunctionable.eventMinute(time, dTime.shift(-horizont));
            int to = DTimeFunctionable.eventMinute(time, dTime);
            return insulinDist.valueAtMinuteRange(from, to);
        } else {
            double v = 0;
            for (DBolus b : series) {
                v += b.pastAt(dTime, horizont);
            }
            return v;
        }

    }

    @Override
    public final double futureAt(DTimeStamp dTime) {

        if (typeBolus == STANDARD) {
            int m = DTimeFunctionable.eventMinute(time, dTime);
            return insulinDist.futureAtMinute(m);
        } else {
            double v = 0;
            for (DBolus b : series) {
                v += b.futureAt(dTime);
            }
            return v;
        }
    }

    @Override
    public final double futureAt(DTimeStamp dTime, int horizont) {

        if (typeBolus == STANDARD) {
            int from = DTimeFunctionable.eventMinute(time, dTime);
            int to = DTimeFunctionable.eventMinute(time, dTime.shift(horizont));
            return insulinDist.valueAtMinuteRange(from, to);
        } else {
            double v = 0;
            for (DBolus b : series) {
                v += b.futureAt(dTime, horizont);
            }
            return v;
        }
    }

    @Override
    public DDiscreteTimeFunction toTimeFunction() {
        
        return DAbsorptionInsulin.makeDiscreteTimeFunction(this);
    }

    @Override
    public String toString() {
        
        return ToStrings.bolusToString(this,true);

       

    }

}

/*
     * Z bolusa projektowanego robi event. To jest to co przyjmuje pacjent.
     * <p>
     * Bolus standardowy to oczywista sprawa. Trudniej jest zrobić dla
     * prostokątnego i złożonego.
     * <p>
     * Docelowo robi to kit, bo kit ma takie a nie inne możliwości. Ale zawsze
     * jest to seria bolusów prostych. Tutaj tak jakby teoretycznnie. Może
     * później będzie jakieś planowanie.
     *
     * @return Pojedyńczy event.
     
    @Override
    public final DEvent getEvent() {

        if (typeBolus == STANDARD) {

            return new DEvent(time, insulinDist, DInsulin.RESOLUTION);

        } else {

            DEvent events[] = new DEvent[series.length];

            for (int i = 0; i < series.length; i++) {
                events[i] = series[i].getEvent();
            }

            return DEvent.join(events);
        }

    }
    
 */
