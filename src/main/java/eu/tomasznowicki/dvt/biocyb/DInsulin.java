package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;

/**
 * Modele absorbcji insuliny. Tutaj wielomiany dotyczą pojedyńczego
 * (standardowego) bolusa.
 * <p>
 * Dodanie nowej insuliny -> W paru miejscach, ale tylko w tym pliku.
 *
 * @author www.tomasznowicki.eu
 */
public abstract class DInsulin {

    /**
     * Gdzy dzieli na minuty. Jeżeli wypada mniej, to przesuwa na kolejną
     * minutę.
     */
    public static final double RESOLUTION = 0.00001;
    
    public static final double THRESHOLD = 3 * RESOLUTION;
    
    public static enum DFunctionInsulin {
        BASE, REGULAR, CORRECTION
    };
    
    /**
     * Obsługiwane typy insuliny.
     */
    public static enum DTypeInsulin {
        NOVORAPID,
        FIASP,
        APIDRA
    };

    //NovoRapid
    public final static int NOVORAPID_SPAN = 6 * 60;
    public final static int NOVORAPID_EFFECT = 30; //<------ ??
    public final static int NOVORAPID_HORIZONT = 60;
    public final static double[] NOVORAPID_POLYNOMIAL = {
        0,
        24.373324143940813, //*x
        -86.47026726059873, //*x^2
        86.27149586382814, //*x^3
        36.85145951810967, //*x^4
        -108.50295830501261, //*x^5
        47.476946039732795 //*x^6
    };

    //Fiasp
    public final static int FIASP_SPAN = 6 * 60;
    public final static int FIASP_EFFECT = 30; //<---------------?? TO NIE MA SENSU
    public final static int FIASP_HORIZONT = 60;
    public final static double[] FIASP_POLYNOMIAL = {
        0,
        41.24640009796711, //*x
        -235.48965246655732, //*x^2
        561.1519721268699, //*x^3
        -675.5833732593659, //*x^4
        403.4374395145161, //*x^5
        -94.76278601342982 //x^6
    };

    //Apidra
    public final static int APIDRA_SPAN = 8 * 60;
    public final static int APIDRA_EFFECT = 30; //<-----------------??
    public final static double[] APIDRA_POLYNOMIAL = {
        0,
        51.754070342658416, //*x
        -325.1572892502022, //*x^2
        830.9609832612845, //*x^3
        -1053.3825532942649, //*x^4
        655.7409620921948, //*x^5
        -159.91617315167 //x^6
    };

    public final static DTypeInsulin stringToType(String name) {

        if (name.equals(DTypeInsulin.NOVORAPID.toString())) {
            return DTypeInsulin.NOVORAPID;
        } else if (name.equals(DTypeInsulin.FIASP.toString())) {
            return DTypeInsulin.FIASP;
        } else if (name.equals(DTypeInsulin.APIDRA.toString())) {
            return DTypeInsulin.APIDRA;
        } else {
            return null;
        }
    }

    public final static DDistribution getDistributionFor(DTypeInsulin dInsulinType, double dTotal) {

        switch (dInsulinType) {

            case NOVORAPID:
                return new DDistributionPolynomial(NOVORAPID_POLYNOMIAL, NOVORAPID_SPAN, dTotal);
            case FIASP:
                return new DDistributionPolynomial(FIASP_POLYNOMIAL, FIASP_SPAN, dTotal);
            case APIDRA:
                return new DDistributionPolynomial(APIDRA_POLYNOMIAL, APIDRA_SPAN, dTotal);

            default:
                return null; //Oznacza błąd
        }
    }

    public final static int getSpanFor(DTypeInsulin dInsulinType) {

        switch (dInsulinType) {
            case NOVORAPID:
                return NOVORAPID_SPAN;
            case FIASP:
                return FIASP_SPAN;
            default:
                return -1; //Oznacza błąd
        }

    }

    public final static int getEffectFor(DTypeInsulin dInsulinType) {

        switch (dInsulinType) {
            case NOVORAPID:
                return NOVORAPID_EFFECT;
            case FIASP:
                return FIASP_EFFECT;
            default:
                return -1; //Oznacza błąd
        }
    }

    public final static int getHorizontFor(DTypeInsulin dInsulinType) {

        switch (dInsulinType) {
            case NOVORAPID:
                return NOVORAPID_HORIZONT;
            case FIASP:
                return FIASP_HORIZONT;
            default:
                return -1; //Oznacza błąd
        }
    }

}
