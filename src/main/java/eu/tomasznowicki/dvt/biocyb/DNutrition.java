package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.time.DTimeStamp;

/**
 * Class containit information about numerical nutrition. Tutaj nie używa się
 * DMeal. Tutaj tylko węglowodany, tłuszcze i białka. <-Wymienniki
 *
 * @author www.tomasznowicki.eu
 */
public class DNutrition {

    public static enum DFunctionMeal {
        REGULAR, CORRECTION
    };

    public final static double[] DIST_CARB_LOW = {2, 4, 20};
    public final static double[] DIST_CARB_MED = {2, 6, 42};
    public final static double[] DIST_CARB_HIGH = {2.8, 10, 23234637426.0 / 48828125.0};

    public final static double[] DIST_FAT = {1.5, 2, 15.0 / 4.0};
    public final static double[] DIST_PROT = {1.5, 2, 15.0 / 4.0};

    /**
     * Used in distributing events into minutes
     */
    public static final double RESOLUTION = 0.00001;

    public static final double THRESHOLD = 3 * RESOLUTION;

    //public static final double EVENT_THRESHOLD = RESOLUTION;
    public static final int GI_LOW = 55, GI_HIGH = 70;

    public static final int EFFECT_HORIZONT_CARB = 15;

    /**
     * 1g of carbohydrate gives 4 kcal of energy
     */
    public static final double CARB_TO_KCAL = 4;

    /**
     * 1g of fat gives 9 kcal of energy
     */
    public static final double FAT_TO_KCAL = 9;

    /**
     * 1g of protein gives 4 kcal of energy
     */
    public static final double PROT_TO_KCAL = 4;

    /**
     * 40 kcal from carbohydrate is 1 Polish carbohydrate exchange ( called WW)
     * Note: 10g of carbohydrate not 15g makes here an exchange.
     */
    public static final double CARB_KCAL_TO_1WW = 40;

    /**
     * 100 kcal from fat is 1 Polish fat exchange (called WT)
     */
    public static final double FAT_KCAL_TO_1WT = 100;

    /**
     * 100g from protein is 1 Polish protein exchange (called WB)
     */
    public static final double PROT_KCAL_TO_1WB = 100;

    /**
     * Time span in minutes to digest carbohydrate
     */
    public static final int SPAN_CARB = 5 * 60;

    /**
     * Time span in minutes to digest fat
     */
    public static final int SPAN_FAT = 6 * 60;

    /**
     * Time span in minutes to digest protein.
     */
    public static final int SPAN_PROT = 6 * 60;

    /**
     * Glycemic index of correction meal.
     */
    public static final int CORR_CARB_GLYCEMIC_INDEX = 90;

    /**
     * How many grams of fat is in 1 WW of a correction meal.
     */
    public static final double CORR_CARB_FAT_GRAMS_PER_WW = 0.1;

    /**
     * How many grams of protein is in 1 WW of a correction meal.
     */
    public static final double CORR_CARB_PROT_GRAMS_PER_WW = 0.05;

    //Poniżej dla standarowego posiłku, jak terapia prosta
    public static final double SIMPLE_CARB_FAT_GRAMS_PER_WW = 0.3;
    public static final double SIMPLE_CARB_PROT_GRAMS_PER_WW = 0.2;
    public static final int SIMPLE_CARB_GLYCEMIC_INDEX = 50;

    /**
     * Resolution of a correction meal in WW.
     */
    public static final double CORR_CARB_RESOLUTION = 0.25;

    /*
     * Normalizes the dose of carb meal correction to the resolution.
     *
     */
    public static double normCarbCorr(double ww) {

        if (ww <= 0) {
            return 0;
        }
        double w = CORR_CARB_RESOLUTION;

        while (w < ww) {
            w += CORR_CARB_RESOLUTION;
        }

        return w;
    }

    /**
     * Tells how many WW it is.
     *
     * @param carbGrams Grams of carbohydrate.
     * @return How many WW it is.
     */
    public static double carbGramsToWW(double carbGrams) {

        return carbGrams * CARB_TO_KCAL / CARB_KCAL_TO_1WW;
    }

    public static DDistribution getDistributionForCarb(double ww, double wt, double wb, int index) {

        if (index >= GI_HIGH) {
            return new DDistributionBeta(DIST_CARB_HIGH, SPAN_CARB, ww);
        } else if (index >= GI_LOW) {
            return new DDistributionBeta(DIST_CARB_MED, SPAN_CARB, ww);
        } else {
            return new DDistributionBeta(DIST_CARB_LOW, SPAN_CARB, ww);
        }

    }

    public static DDistribution getDistributionForFat(double ww, double wt, double wb, int index) {

        return new DDistributionBeta(DIST_FAT, SPAN_FAT, wt);
    }

    public static DDistribution getDistributionForProt(double ww, double wt, double wb, int index) {

        return new DDistributionBeta(DIST_PROT, SPAN_PROT, wb);
    }

    public static int resultantGlycemicIndex(DMeal dMeal0, DMeal dMeal1) {
        //Wypadkowy index glikemiczny, tutuaj wg masy posiłku
        double g01 = dMeal0.totalGrams + dMeal1.totalGrams;
        double f0 = dMeal0.totalGrams / g01;
        double f1 = dMeal1.totalGrams / g01;
        return (int) (f0 * dMeal0.glycemnicIndex + f1 * dMeal1.glycemnicIndex);
    }

    /*
     * Robi posiłek korekcyjny na podstawie wyliczonych wymienników, które mają
     * być korektą.
     */
    public static DMeal getCorrectionMeal(DTimeStamp dTime, double w) {

        w = normCarbCorr(w);

        if (w > 0) {
            double carb = (w * DNutrition.CARB_KCAL_TO_1WW) / DNutrition.CARB_TO_KCAL;
            double fat = w * DNutrition.CORR_CARB_FAT_GRAMS_PER_WW;
            double prot = w * DNutrition.CORR_CARB_PROT_GRAMS_PER_WW;
            return new DMeal(dTime, carb, fat, prot, DNutrition.CORR_CARB_GLYCEMIC_INDEX);

        } else return null;


    }

    public static DMeal getSimpleMeal(DTimeStamp dTime, double w) {
        if (w <= 0) {
            return null;
        }

        double carb = (w * DNutrition.CARB_KCAL_TO_1WW) / DNutrition.CARB_TO_KCAL;
        double fat = w * DNutrition.SIMPLE_CARB_FAT_GRAMS_PER_WW;
        double prot = w * DNutrition.SIMPLE_CARB_PROT_GRAMS_PER_WW;
        return new DMeal(dTime, carb, fat, prot, DNutrition.SIMPLE_CARB_GLYCEMIC_INDEX);

    }

}

/*
     * Jeżeli część fali jest mniejsza niż ten próg to się jej nie stosuje
 */
//public static final double THRESHOLD_WAVE = 0.2;

/*
     * Jeżeli część standardowa jest mniejsza niż ten próg to nie stosuje się
     * części standardowej.
 */
//public static double MIN_STANDARD_PART = 0.1;

/*
     * Jeżeli część standardowa jest większa niż ten próg to się nie stosuje
     * części prostokątnej
 */
//public static double MAX_STANDARD_PART = 0.8;
/*
     * Sugeruje rodzaj bolusa dla posiłku. Rodzaje bolusa to prosty,
     * prostokątny, złożony. Tutaj nie oblicza insuliny tylko rodzaj bolusa:
     * PROSTY, PROSTOKĄT, ZŁOŻÓNY Wynik to: typ, czas_fali, ile_na_prosty.
     * <p>
     * Teraz przelicza proporcjonalnie do ww / wbt.
     *
     * @param dMeal Posiłek, tu wszystko wyliczone. Może być null.
     * @return Porada dotycząca bolusa
 */
//    public static DBolusAdvice bolusAdvice(DMeal dMeal) {
//
//        if (dMeal == null) {
//            return null;
//        }
//
//        //Domyślnie bolus jest złożony
//        double standardPart = dMeal.ww / dMeal.wbt;
//        int waveSpan = SPAN_PROT > SPAN_FAT ? SPAN_PROT : SPAN_FAT;
//        DBolus.DTypeBolus typeBolus = DBolus.DTypeBolus.DUAL_WAVE;
//
//        String info = "";
//
//        //Jeżeli część standardowa jest duża, a fala mała, to rezygnujemy z fali.
//        if (standardPart >= MAX_STANDARD_PART) {
//            standardPart = 1;
//            waveSpan = 0;
//            typeBolus = DBolus.DTypeBolus.STANDARD;
//        }
//
//        //Jeżeli część standardowa jest mała, a fala duża, to rezygnujemy z części standarodowej
//        if (standardPart < MIN_STANDARD_PART) {
//            standardPart = 0;
//            typeBolus = DBolus.DTypeBolus.SQUARE_WAVE;
//        }
//
//        return new DBolusAdvice(typeBolus, waveSpan, standardPart);
//    }
//    public static DBolusAdvice bolusAdviceSimple() {
//        return new DBolusAdvice(DBolus.DTypeBolus.STANDARD, 0, 1);
//    }

/*

   
     Opisuje jak powinien wyglądać bolus do posiłku. To nie wylicza dawki
     insuliny. Sugeruje typ bolusa.
     
    public static class DBolusAdvice {

        public final DBolus.DTypeBolus typeBolus;
        public final int span;
        public final double standardPart;

        public DBolusAdvice(DBolus.DTypeBolus type, int waveSpan, double standardPart) {

            typeBolus = type;
            span = waveSpan;
            this.standardPart = standardPart;
        }

//        @Override
//        public String toString() {
//            String s = "[" + typeBolus;
//
//            if (typeBolus != DBolus.DTypeBolus.STANDARD) {
//                s += " span=" + span;
//            }
//            if (typeBolus == DBolus.DTypeBolus.DUAL_WAVE) {
//                s += " standardPart=" + standardPart;
//            }
//            return s + "]";
//        }
    }


 */
