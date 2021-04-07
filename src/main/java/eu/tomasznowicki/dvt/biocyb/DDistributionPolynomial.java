package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.raport.ToStrings;

public class DDistributionPolynomial extends DDistribution {

    
    public final double[] F;

    public DDistributionPolynomial(double[] dCoeff, int dSpan, double dTotal) {

        super(dSpan, dTotal);
        F = new double[dCoeff.length];
        System.arraycopy(dCoeff, 0, F, 0, dCoeff.length);
        check();
    }

    @Override
    protected final double value(double x) {

        double a = 1, v = F[0];

        for (int i = 1; i < F.length; i++) {
            a *= x;
            v += F[i] * a;
        }

        return v;
    }

    @Override
    public final String toString() {

        return ToStrings.distributionPolynomialToString(this);
    }
}

//    public final int degree;

/*
     * Array of coefficients for the polynomial given in the [0,span] range
     * (working polynomial):<br>
     * F1[0] + F1[1]*x + F1[2]*x^2 + F1[3]*x^3 + ... + F1[n]*x^n<br>
     * where n is the degree of the polynomial.<br>
     * These factors are calculated in the constructor.
    
    public final double[] F1;

 */

 /*
    
     * Array of coefficients for the polynomial given in the [0,1] range (base
     * polynomial):<br>
     * F0[0] + F0[1]*x + F0[2]*x^2 + F0[3]*x^3 + ... + F0[n]*x^n<br>
     * where n is the degree of the polynomial.<br>
     * These factors are required in the constructor.
     
 */
 /*
    public DDistributionPolynomial(double[] coeff, int dSpan, double dTotal) {

        super(dSpan, dTotal);

        degree = coeff.length - 1;
        F0 = new double[coeff.length];
        F1 = new double[coeff.length];

        //Dystrybucja znormalizowana
        System.arraycopy(coeff, 0, F0, 0, coeff.length);

        //Dystrubucja rzeczywista
        double q = (double) span;
        for (int i = 0; i < coeff.length; i++) {
            F1[i] = total * F0[i] / q;
            q *= (double) span;
        }

        //Test dystrybucji
        
    }
 */

 /*
 * Class to keep polynomial for describing time developmnet of a phenomenon. f_0
 * + f_1*x + f_2*x^2 + f_3*x^3 + ... + f_degree*x^degree<br>
 * <p>
 * The polynomial is given in the range form 0 to 1. In this range it must have
 * positive values and its integral must be 1. Values at 0 and 1 must be 0. This
 * polynomial is called the base polynomial.
 * <p>
 * Then the polynomial is recalculated for the new range from 0 to span in
 * minutes. In the new range its values are still positive and the integral is
 * still 1. Values at both ends are 0 as well. This polynomial is called the
 * working polynomial.
 * <p>
 * It is the role of a programmer to give proper coefficents in the range [0,1].
 * The polynomial is checked against its constraction and if they are not
 * satisfied the error flag is set to be true.
 *
 * @author www.tomasznowicki.eu
 */
 /*
    private double value(double[] Q, double arg) {

        double v = Q[0], x = arg;
        for (int i = 1; i <= degree; i++) {
            v += Q[i] * x;
            x *= arg;
        }
        return v;
    }

    @Override
    protected final double value0(double x) {

        if (x < 0 || x > 1) {
            return 0;
        } else {
            return value(F0, x);
        }
    }

    @Override
    protected final double value1(double x) {
        if (x < 0 || x > span) {
            return 0;
        } else {
            return value(F1, x);
        }
    }
 */
 /*
    
     * Crates text reprezentation of a polynomial.
     *
     * @param Q Array of coefficients of the polynomial.
     * @return Polynomian convertet to string.
     
    private String polynomialToString(double Q[]) {

        String s = "";

        for (int i = 0; i <= degree; i++) {
            if (i > 0) {
                s += " ";
            }
            if (Q[i] > 0) {
                s += "+";
            }
            s += Double.toString(Q[i]);
            if (i > 0) {
                s += "*x";
            }
            if (i > 1) {
                s += "^" + Integer.toString(i);
            }
        }

        return s;
    }

 */
