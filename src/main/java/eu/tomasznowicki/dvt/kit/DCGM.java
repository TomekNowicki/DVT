package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.therapy.DTherapyAttendee;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.biocyb.DGlycemia;
import eu.tomasznowicki.dvt.raport.DLoggerCGM;
import static eu.tomasznowicki.dvt.raport.ToStrings.timeStampToString;

import eu.tomasznowicki.dvt.time.DClockEventAction;
import java.util.Deque;
import java.util.ArrayDeque;

public class DCGM extends DTherapyAttendee implements TherapyAdvising {

    public static final int // all in minutes
            DEFAULT_INTERVAL = 5,
            DEFAULT_DELAY = 10,
            DEFAULT_HORIZONT_TREND_SHORT = 15,
            DEFAULT_HORIZONT_TREND_LONG = 30,
            DEFAULT_HORIZONT_AVERAGE_SHORT = 30,
            DEFAULT_HORIZONT_AVERAGE_LONG = 45,
            DEFAULT_HORIZONT_HISTORY = 3 * DTimeStamp.MINUTES_IN_DAY;

    public static final double //
            DEFAULT_ALARM_MIN = 70,
            DEFAULT_ALARM_MAX = 130;
    public static final int DEFAULT_HORIZONT_ALARM = 15;

    private int //
            delay, interval, scanCounter,
            horizontTrendShort, horizontTrendLong,
            horizontAverageShort, horizonAaverageLong,
            horizontHistory;

    private double alarmBgMin, alarmBgMax;
    private int horizontAlarm;

    private boolean alarmsOn, alarm;

    private final Deque<DGlycemia> records;
    private DGlycemia current;

    DLoggerCGM dlogger = null;

    public DCGM() {
        delay = DEFAULT_DELAY;
        interval = scanCounter = DEFAULT_INTERVAL;
        horizontTrendShort = DEFAULT_HORIZONT_TREND_SHORT;
        horizontTrendLong = DEFAULT_HORIZONT_TREND_LONG;
        horizontAverageShort = DEFAULT_HORIZONT_AVERAGE_SHORT;
        horizonAaverageLong = DEFAULT_HORIZONT_AVERAGE_LONG;
        horizontHistory = DEFAULT_HORIZONT_HISTORY;

        alarmBgMin = DEFAULT_ALARM_MIN;
        alarmBgMax = DEFAULT_ALARM_MAX;
        horizontAlarm = DEFAULT_HORIZONT_ALARM;
        alarmsOn = true;
        alarm = false;
        records = new ArrayDeque<>();
        current = null;
    }

    public void setLogger(DLoggerCGM dLogger) {
        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    public void log_yourself() {
        if (dlogger != null) {
            dlogger.log_cgm(this);
        }
    }

    public void alarmsOn(boolean on) {
        alarmsOn = on;
    }

    public boolean alarm() {
        return alarmsOn ? alarm : false;
    }

    public DGlycemia getGlycemia() {
        return current;
    }

    private void scan() {

        if (patient != null) {
            //
            var bg = patient.getBG(); //Tyle ma pacient glikemii
            var rs = findRate(bg, horizontTrendShort); //Może być null
            var rl = findRate(bg, horizontTrendLong); //Może być null
            var as = findAverage(bg, horizontAverageShort); //Może być null
            var al = findAverage(bg, horizonAaverageLong); //Może być null
            var t = delay > 0 ? clockTime.shift(delay) : clockTime;
            var glycemia = new DGlycemia(t, bg, rs, rl, as, al);
            records.offerLast(glycemia);
        }
    }

    private DGlycemia findGlycemia() {
        var iter = records.descendingIterator();
        while (iter.hasNext()) {
            var g = iter.next();
            if (g.time.compareTo(clockTime) == 0) {
                return g;
            }
        }
        return null;
    }

    private Double findRate(double bg, int horizont) { //!bg to wartość jeszcze nie wpisana
        int n = horizont / interval; //Tyle jest potrzebnych wartości wstecz, aby objąć horyzont
        if (n > 0 && records.size() >= n) {
            double[] r = new double[n]; //Tutaj różnice skończone
            double bg0, bg1 = bg; //Wartość poprzednia i następna
            var iter = records.descendingIterator();
            for (int i = 0; i < n; i++) {
                bg0 = iter.next().value;
                r[i] = (bg1 - bg0) / ((double) interval);
                bg1 = bg0;//Poprzednia staje się następną
            }
            //!Tutaj można zrealizować średnią ważoną
            double sum = 0;
            for (double x : r) {
                sum += x;
            }
            return sum / (double) n;
        } else {
            return null; //Nie jest w stanie policzyć
        }
    }

    private Double findAverage(double bg, int horizont) {  //!bg to wartość jeszcze nie wpisana
        int n = horizont / interval; //Tyle jest potrzebnych wartości wstecz, aby objąć horyzont
        if (n > 0 && records.size() >= n) {
            double sum = bg;
            var iter = records.descendingIterator();
            for (int i = 0; i < n; i++) {
                sum += iter.next().value;
            }
            return sum / (double) (n + 1);
        } else {
            return null;
        }
    }

    private boolean checkAlarm() {
        var a = false; //Wynik testu
        if (current != null) {
            if (current.value < alarmBgMin || current.value > alarmBgMax) {
                a = true;
            } else if (current.rateShort != null) {
                var p = current.value + current.rateShort * horizontAlarm;
                if (p < alarmBgMin || p > alarmBgMax) {
                    a = true;
                }
            }
        }
        return a;
    }

    @Override
    public void clean() {
        DTimeStamp cleanTime = clockTime.shift(-horizontHistory);
        while (true) {
            var first = records.peekFirst();
            if (first == null || first.time.compareTo(cleanTime) >= 0) {
                break;
            } else {
                records.removeFirst();
            }
        }
    }

    @Override
    public String toString() {
        return timeStampToString(clockTime) + " "
                + "interval=" + interval + " "
                + "delay=" + delay + "\n"
                + "horizontTrendShort=" + horizontTrendShort + " "
                + "horizontTrendLong=" + horizontTrendLong + "\n"
                + "horizontAverageShort=" + horizontAverageShort + " "
                + "horizonAaverageLong=" + horizonAaverageLong + "\n";
    }

    

    
    
    
    
    @Override
    public void tickCGM(DClockEventAction event) {

        if (event.getSource() != clock) {
            return;
        }

        scanCounter--;

        if (scanCounter == 0) {
            scanCounter = interval;
            scan();
        }

        var g = findGlycemia();
        if (g != null) {
            current = g;
            alarm = checkAlarm();
            if (dlogger != null) {
                dlogger.log_scan(current, alarm);
            }
        }
    }

    @Override
    public void writeAdvice(DTherapyAdvice advice) {
        var bg = getGlycemia();
        advice.shortBG = bg.rateShort;
        advice.longBG = bg.rateLong;
    }

}

//
//    private void scan() {
//        if (patient != null) {
//var glycemia = new DGlycemia(clockTime.shift(delay), patient.getBG(), findTrend());
//records.offerLast(glycemia);
//            alarm = glycemia.value < bgMin
//                    || (glycemia.value < bgMinTrend && glycemia.trend == DECREASE)
//                    || (glycemia.value < bgMinQuick && glycemia.trend == DECREASE_QUICK)
//                    || glycemia.value > bgMax
//                    || (glycemia.value > bgMaxTrend && glycemia.trend == INCREASE)
//                    || (glycemia.value > bgMaxQuick && glycemia.trend == INCREASE_QUICK);
//    }
// }
/*
    public static final int // mg / dl
            BG_MIN_ALARM = 70,
            BG_MIN_ALARM_TREND = 75,
            BG_MIN_ALARM_QUICK = 80,
            BG_MAX_ALARM = 180,
            BG_MAX_ALARM_TREND = 150,
            BG_MAX_ALARM_QUICK = 140;

        bgMin = BG_MIN_ALARM;
        bgMinTrend = BG_MIN_ALARM_TREND;
        bgMinQuick = BG_MIN_ALARM_QUICK;
        bgMax = BG_MAX_ALARM;
        bgMaxTrend = BG_MAX_ALARM_TREND;
        bgMaxQuick = BG_MAX_ALARM_QUICK;

    protected int bgMin, bgMinTrend, bgMinQuick, bgMax, bgMaxTrend, bgMaxQuick;

 */
//                + DStrings.CGM_ALARMS + ARROW
//                + bgMin + SEP + bgMinTrend + SEP + bgMinQuick + SEP
//                + bgMaxQuick + SEP + bgMaxTrend + SEP + bgMax + ENDL;
