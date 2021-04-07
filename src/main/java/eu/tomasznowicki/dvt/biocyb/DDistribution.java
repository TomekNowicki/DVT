package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.raport.DStrings;

/**
 * Dystrybucja zjawiska
 *
 * @author www.tomasznowicki.eu
 */
public abstract class DDistribution {

    /**
     * Span in minutes of the phenomenon that is describe by the distribution.
     */
    public final int span;

    /**
     * Total value of the phenomenon to disribute; h = total / span;
     *
     * @see span
     */
    public final double total, h;

    private boolean error = false;
    private String info = "";

    // public final boolean error;
    // public final String info;
    /**
     * Real tolerance while testing the distribution. Numeric zero.
     */
    public static final double TOLERANCE = 0.00001;
    private static String FORMAT = "%.6f"; //One more then TOLERANCE

    /**
     * For numeric integration: number of samples in the range of [0,1] or in
     * every minute.
     */
    public static final int SAMPLES = 1000;

    protected DDistribution(int dSpan, double dTotal) {

        span = dSpan > 0 ? dSpan : 0;
        total = span > 0 && dTotal > 0 ? dTotal : 0;
        h = span > 0 ? total / span : 0;
    }
    
    public final boolean error(){
        return error;
    }
    
    public final String info(){
        return info;
    }
    

    //Każda dystrybucja ma swój wzór w przedzile [0,1]
    //Tutaj nie ma kontroli
    abstract protected double value(double x);

    //Wartość w przedziale [0,1], tutaj kontrola
    public final double value0(double x) {

        if (x < 0 || x > 1) {
            return 0;
        } else {
            return value(x);
        }
    }

    //Wartość w przedziale [0,span], tutaj kontrola
    public final double value1(double x) {

        if (x < 0 || x > span) {
            return 0;
        } else {
            x = x / (double) span;
            return h * value0(x);
        }
    }

    //Pole przybliżone parabolą, bez kontroli bo prywatna
    private double area(double from, double to, int k) {

        double h = (to - from) / 2;
        double f0 = k == 0 ? value0(from) : value1(from);
        double f1 = k == 0 ? value0(from + h) : value1(from + h);
        double f2 = k == 0 ? value0(to) : value1(to);
        return h * (f0 + 4 * f1 + f2) / 3; //Parabola

    }

    private double integral(double from, double to, int n, int k) {

        double x = from, dx = (to - from) / (double) n, s = 0;
        for (int i = 0; i < n; i++) {
            s += area(x, x + dx, k);
            x += dx;
        }
        return s;

    }

    public final double[] min(int k) {

        int steps = k == 0 ? SAMPLES : span * SAMPLES;
        double x = 0, dx = 1.0 / steps, f = 0, xm = 0, fm = 0;

        for (int i = 0; i < steps; i++) {
            x += dx;
            f = k == 0 ? value0(x) : value1(x);
            if (f < fm) {
                fm = f;
                xm = x;
            }
        }

        return new double[]{xm, fm};
    }

    public final double[] max(int k) {

        int steps = k == 0 ? SAMPLES : span * SAMPLES;
        double x = 0, dx = 1.0 / steps, f = 0, xm = 0, fm = 0;

        for (int i = 0; i < steps; i++) {
            x += dx;
            f = k == 0 ? value0(x) : value1(x);
            if (f > fm) {
                fm = f;
                fm = x;
            }
        }

        return new double[]{xm, fm};
    }

    protected final void check() {

        var v = value0(0);
        info = DStrings.DISTRIBUTION_VALUE00 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (Math.abs(v) > TOLERANCE) {
            error = true;
        }

        v = value0(1);
        info += DStrings.DISTRIBUTION_VALUE01 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (Math.abs(v) > TOLERANCE) {
            error = true;
        }

        v = min(0)[1];
        info += DStrings.DISTRIBUTION_MIN0 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (v < -TOLERANCE) {
            error = true;
        }

        v = integral(0, 1, SAMPLES, 0);
        info += DStrings.DISTRIBUTION_INTEGRAL0 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (Math.abs(v - 1.0) > TOLERANCE) {
            error = true;
        }

        v = value1(0);
        info += DStrings.DISTRIBUTION_VALUE10 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (Math.abs(v) > TOLERANCE) {
            error = true;
        }

        v = value1(span);
        info += DStrings.DISTRIBUTION_VALUE11 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (Math.abs(v) > TOLERANCE) {
            error = true;
        }

        v = min(1)[1];
        info += DStrings.DISTRIBUTION_MIN1 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (v < -TOLERANCE) {
            error = true;
        }

        v = integral(0, span, SAMPLES * span, 1);
        info += DStrings.DISTRIBUTION_INTEGRAL1 + ToStrings.EQ + String.format(ToStrings.LOC, FORMAT, v) + ToStrings.SEP;
        if (Math.abs(v - total) > TOLERANCE) {
            error = true;
        }

    }

    //--------------------------------------------------------------------------
    final public double valueAtMinuteRange(int from, int to) {

        from = from < 0 ? 0 : from;
        to = to > span ? span : to;

        if (from >= to) {
            return 0;
        } else {
            return integral((double) from, (double) to, (to - from) * SAMPLES, 1);
        }

    }

    //To jest minuta w przedziale od 0 do span, To nie jest minuta symulacji
    final public double valueAtMinute(int minute) {

        return valueAtMinuteRange(minute - 1, minute);

    }

    final public double futureAtMinute(int minute) { //To nie jest minuta symulacji

        return valueAtMinuteRange(minute, span);

    }

    final public double pastAtMinute(int minute) { //To nie jest minuta symulacji

        return valueAtMinuteRange(0, minute);

    }

    final public double[] toArray(double valueResolution) {

        double[] val = new double[span];
        double curr = 0, rest = 0;
        long multiple = 0;

        for (int i = 0; i < span; i++) {

            curr = valueAtMinuteRange(i, i + 1) + rest;
            multiple = Math.round(curr / valueResolution);
            val[i] = multiple * valueResolution; //May be zero. 
            rest = curr - val[i];
        }

        return val;
    }

   



}

/*
        /*
        if (minute >= span) {
            return 0;
        } else if (minute <= 0) {
            return total;
        } else {
            return integral1((double) minute, (double) span, span - minute);
        }
 */
 /*
        if (minute >= span) {
            return total;
        } else if (minute <= 0) {
            return 0;
        } else {
            return integral1(0, (double) (minute), minute);
        }
 */
 /*
    protected final double[] min_max_0() { //Jednostkowa

        double x = 0, dx = 1.0 / (double) SAMPLES, f = value0(0);
        double min = f, max = f;

        for (int i = 0; i < SAMPLES; i++) {
            x += dx;
            f = value0(x);
            if (f < min) {
                min = f;
            }
            if (f > max) {
                max = f;
            }
        }

        return new double[]{min, max};
    }

    protected final double[] min_max_1() { //Rzeczywista

        double x = 0, dx = (double) span / (double) SAMPLES, f = value1(0);
        double min = f, max = f;

        for (int i = 0; i < SAMPLES; i++) {
            x += dx;
            f = value0(x);
            if (f < min) {
                min = f;
            }
            if (f > max) {
                max = f;
            }
        }

        return new double[]{min, max};
    }
 */
 /*
     * Checks the given polynomial against constraints:<br>
     * - Zero value at both ends.<br>
     * - Not negative values<br>
     * - Integral equals 1 <br>
     * Wartości muszą być policzone w klasie potomnej ! Wywoływać w
     * konstruktorze potomnych
 */
/**
 * valueAt0, valueAt1 - Values of the base polynomial at x=0 and x=1. Both
 * should be 0.<br>
 * minimal0 - Minimal value of the base polynomial.<br>
 * integral0 - Integral for the base polynomial. Should be 1. integral1 -
 * Integral for the working polynomial. Should be 1.
 */
/**
 * If true the polynomial is correct in the sens of being positive in the range
 * of [0,1] with integral 1 in the range ond zero values at both ends.
 */
/*
    private double area0(double from, double to) { //Jednostkowa

        if (from >= to) {
            return 0;
        } else {
            from = from < 0 ? 0 : from;
            to = to > 1 ? 1 : to;
            double h = (to - from) / 2;
            double f0 = value0(from);
            double f1 = value0(from + h);
            double f2 = value0(to);
            return h * (f0 + 4 * f1 + f2) / 3; //Parabola
        }
    }

    private double area1(double from, double to) { //Rzeczywista

        if (from >= to) {
            return 0;
        } else {
            from = from < 0 ? 0 : from;
            to = to > span ? 1 : to;
            double h = (to - from) / 2;
            double f0 = value1(from);
            double f1 = value1(from + h);
            double f2 = value1(to);
            return h * (f0 + 4 * f1 + f2) / 3; //Parabola
        }
    }
 */
 /*
     * Calculates area under the polynomial at the range [from, to]. Uses a
     * parabola to approximate the polynomial shape.
     *
     * @param Q Array of coefficients of the polynomial.
     * @param factor Factor to multiply the polynomial.
     * @param from Start point of the range.
     * @param to End point of the range.
     * @return Area under the polynomial at the range [from,to].
 */
 /*
     * Calculates integral of a given polynomial at the range [form, to]. Uses
     * area method while numerical integrating.
     *
     * @param Q Array of coefficients of the polynomial.
     * @param factor Factor to multiply the polynomial.
     * @param from Start point of the range.
     * @param to End point of the range.
     * @param n Numerical resolution.
     * @return Integral over the range [from,t0].
     * @see area
 */
 /*
     * Finds minimum value of the polynomial at given numerical resoluton over
     * the range [form,to].
     *
     * @param Q Array of coefficients of the polynomial.
     * @param factor Factor to multiply the polynomial.
     * @param from Start point of the range
     * @param to End point of the range.
     * @param n Numerical resolution.
     * @return Minimal value at range [form,to].
 */
 /*
     * Calculates the portion of total value of the phenomenon in the range of
     * [from,to] that is subrange of [0,span].
     *
     * @param from Start minute.
     * @param to End minute.
     * @param total Total dose over the range [0,span].
     * @return The portion of total corresponding to the range [from,to]
 */
 /*
     * Calculates the portion of total value of the phenomenon in the range of
     * [minutes-1, minutes] that is subrange of [0,span].
     *
     * @param minute Minute of the phenomenon.
     * @param total Total dose over the range [0,span].
     * @return The portion of the total corresponding to the range [minute-1,
     * minute].
     * @see span
 */

 /*
     * Calculates the portion of total value of the phenomenon in the range of
     * [minute,span] that is subrange of [0,span].
     *
     * @param minute Minute of the phenomenon.
     * @param total Total dose over the range [0,span].
     * @return The portion of total corresponding to the range [minute,span].
 */

 /*
     * Calculates the portion of total value of the phenomenon in the range of
     * [0,minute] that is subrange of [0,span].
     *
     * @param minute Minute of the phenomenon.
     * @param total Total dose over the range [0,span].
     * @return The portion of total corresponding to the range [0,minute].
 */
 /*
     * Calculates a portion of the total for every minute of the phenomenon
     * accrodring to the polynomial.
     *
     * @param total Total dose over the range [0,span].
     * @param valueResolution Finite resolution of the phenomenon.
     * @return Array of distribution of total over the range [0,span].
 */
