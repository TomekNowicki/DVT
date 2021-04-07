package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.patient.DAssimilationMeal;
import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;
import eu.tomasznowicki.dvt.time.DTimeStamp;

/**
 * Informacje o posiłku.
 *
 * @author www.tomasznowicki.eu
 */
public class DMeal implements DTimeFunctionable, Comparable<DMeal> {

    public final DTimeStamp time; //Kiedy przyjęty

    public final double carbGrams, fatGrams, protGrams, totalGrams;
    public final double carbKcal, fatKcal, protKcal, totalKcal;
    public final double ww, wb, wt, wbt;

    public final int glycemnicIndex; //Wypadkowy indeks glikemiczny
    public static boolean CARB_ONLY = false; //To tylko do przewidywań, Do pacjenta zawsze idzie całość

    /**
     * Matematyczne modele absorbji węglowodanów, tłuszcu i białka.
     */
    public final DDistribution carbDist, fatDist, protDist;

    /**
     * Należy podać czas "wprowadzenia posiłku" oraz w gramach: węglowodany,
     * tłuszcze, białka. Dodatkowo podać należy wypadkowy indeks glikemiczny.
     * Reszta sie przelicza.
     *
     * @param dTime Czas posiłku.
     * @param carb Ile węglowodanów w gramach.
     * @param fat Ile tłuszczu w gramach.
     * @param prot Ile białek w gramach.
     * @param index Indeks glikemiczny.
     */
    public DMeal(DTimeStamp dTime, double carb, double fat, double prot, int index) {

        
        time = dTime;
        carbGrams = carb;
        fatGrams = fat;
        protGrams = prot;
        glycemnicIndex = index;

        totalGrams = carbGrams + fatGrams + protGrams;
        carbKcal = DNutrition.CARB_TO_KCAL * carbGrams;
        fatKcal = DNutrition.FAT_TO_KCAL * fatGrams;
        protKcal = DNutrition.PROT_TO_KCAL * protGrams;
        totalKcal = carbKcal + fatKcal + protKcal;
        ww = carbKcal / DNutrition.CARB_KCAL_TO_1WW;
        wt = fatKcal / DNutrition.FAT_KCAL_TO_1WT;
        wb = protGrams / DNutrition.PROT_KCAL_TO_1WB;
        wbt = wb + wt;
        carbDist = DNutrition.getDistributionForCarb(ww, wt, wb, glycemnicIndex);
        fatDist = DNutrition.getDistributionForFat(ww, wt, wb, glycemnicIndex);
        protDist = DNutrition.getDistributionForProt(ww, wt, wb, glycemnicIndex);

    }

    /**
     * Służy do zmiany czasu posiłku. Jest używane, gdy zmieniamy dayTime na
     * totalTime przy wyjmowaniu posiłku z planu dnia.
     *
     * @param dTime Ten czas zostanie podstawiony.
     * @param dMeal Z tego bierze, węglowoadny, tłuszcz, białko i indeks
     */
    public DMeal(DTimeStamp dTime, DMeal dMeal) {

        this(dTime, dMeal.carbGrams, dMeal.fatGrams, dMeal.protGrams, dMeal.glycemnicIndex);
    }

    public DMeal() {
        this(new DTimeStamp(), 0, 0, 0, 0);
    }

    /**
     * Służy z zmiany czasu posiłku
     *
     * @param minutes Ile mninut przesunąć. (Czy może być ujemne?)
     * @return Nowy obiekt posiłku
     */
    public DMeal shift(int minutes) {

        return new DMeal(time.shift(minutes), carbGrams, fatGrams, protGrams, glycemnicIndex);
    }

    public double w() {

        return CARB_ONLY ? ww : ww + wbt;
    }

    public static DMeal join(DMeal dMeal0, DMeal dMeal1) {

        if (dMeal0 != null && dMeal1 != null
                && dMeal0.time.equals(dMeal1.time)) {

            return new DMeal(dMeal0.time,
                    dMeal0.carbGrams + dMeal1.carbGrams,
                    dMeal0.fatGrams + dMeal1.fatGrams,
                    dMeal0.protGrams + dMeal1.protGrams,
                    DNutrition.resultantGlycemicIndex(dMeal0, dMeal1));
        } else {

            return null;
        }

    }

    /**
     * Sprawdzamy ile wymienników w danej (minucie) chwili wydzieliło sie z
     * posiłku do krwi.
     *
     * @param dTime Czas symulacji.
     * @return Ile wymienników z tego posiłu wydzialiło się w danej chwili. Tzn.
     * w przedzale [ta_minuta-1, ta_minuta]. Suma ww, wt, wb.
     */
    @Override
    public final double valueAt(DTimeStamp dTime) {

        int m = DTimeFunctionable.eventMinute(time, dTime); //Czas w wielomianie
        double w = carbDist.valueAtMinute(m);
        if (!CARB_ONLY) {
            w += fatDist.valueAtMinute(m) + protDist.valueAtMinute(m);
        }
        return w;
    }

    @Override
    public final double pastAt(DTimeStamp dTime) {

        int m = DTimeFunctionable.eventMinute(time, dTime);
        double w = carbDist.pastAtMinute(m);
        if (!CARB_ONLY) {
            w += fatDist.pastAtMinute(m) + protDist.pastAtMinute(m);
        }
        return w;
    }

    @Override
    public double pastAt(DTimeStamp dTime, int horizont) {

        int from = DTimeFunctionable.eventMinute(time, dTime.shift(-horizont));
        int to = DTimeFunctionable.eventMinute(time, dTime);
        double w = carbDist.valueAtMinuteRange(from, to);
        if (!CARB_ONLY) {
            w += fatDist.valueAtMinuteRange(from, to) + protDist.valueAtMinuteRange(from, to);
        }
        return w;
    }

    @Override
    public final double futureAt(DTimeStamp dTime) {

        int m = DTimeFunctionable.eventMinute(time, dTime);
        double w = carbDist.futureAtMinute(m);
        if (!CARB_ONLY) {
            w += fatDist.futureAtMinute(m) + protDist.futureAtMinute(m);
        }
        return w;
    }

    @Override
    public final double futureAt(DTimeStamp dTime, int horizont) {

        int from = DTimeFunctionable.eventMinute(time, dTime);
        int to = DTimeFunctionable.eventMinute(time, dTime.shift(horizont));
        double w = carbDist.valueAtMinuteRange(from, to);
        if (!CARB_ONLY) {
            w += fatDist.valueAtMinuteRange(from, to) + protDist.valueAtMinuteRange(from, to);
        }
        return w;
    }

    @Override
    public DDiscreteTimeFunction toTimeFunction() {
        
        return DAssimilationMeal.makeDiscreteTimeFunction(this);
    }


    @Override
    public String toString() {

        return ToStrings.mealToString(this, true);
    }



    @Override
    public int compareTo(DMeal dMeal) {

        return this.time.compareTo(dMeal.time);
    }

}


/*
      Po jakim czasie będzie efekt we krwi. Do planowania kontroli glikemi po
      korekcie.
     
    public final int effectHorizont;
effectHorizont = DNutrition.getEffectHorizont(carbGrams, fatGrams, protGrams, glycemnicIndex);

    public static int getEffectHorizont(double carb, double fat, double prot, int index) {
        //Po jakim czasie od spożycia podnosi się cukier
        //Do zaplanowania kontroli

        return 15; //--------------------------------> Do zrobienia
    }
Indywidualnie do każdego posiłku

 */
