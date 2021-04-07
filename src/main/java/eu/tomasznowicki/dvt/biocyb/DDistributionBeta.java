/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.raport.ToStrings;

/**
 * public final static double[] DIST_CARB_LOW = {2, 4, 20};
    public final static double[] DIST_CARB_MED = {2, 6, 42};
    public final static double[] DIST_CARB_HIGH = {2.8, 10, 23234637426.0 / 48828125.0};
 * @author tomek
 */
public class DDistributionBeta extends DDistribution {

    public final double a, b, c; //Znormalizowana

    public DDistributionBeta(double[] dCoeff, int dSpan, double dTotal) {
        //double alpha, double beta, double fc, int dSpan, double dTotal
        super(dSpan, dTotal);
        
        int l = dCoeff.length;
        
        a = l > 0 ? dCoeff[0] : 1;
        b = l > 1 ? dCoeff[1] : 1;
        c = l > 2 ? dCoeff[2] : 0;
        
        check();
    }

    @Override
    protected double value(double x) {

        return c * Math.pow(x, a - 1) * Math.pow(1 - x, b - 1);

    }

    @Override
    public String toString() {

        return ToStrings.distributionBetaToString(this);
    }

}

/*
    @Override
    protected double value1(double x) {

        if (x < 0 || x > span) {
            return 0;
        } else {
            x = x / (double) span;
            return f * value0(x);
        }
    }


    //public final double a1, b1, c1; //Rzeczysta
    private final double f;

        //Współczynnik dla dystrybucji rzeczywistej
        f = total / span;


 */
