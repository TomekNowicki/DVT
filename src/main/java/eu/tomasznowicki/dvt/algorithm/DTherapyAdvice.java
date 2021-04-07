package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.time.DTimeStamp;

/*
 * Porada kitu jest prosta
 * Porada asystenta będzie bardziej dokladana
* Tutaj wszędzie na początku nule
* Algortymy wypełnią to co do nich należy

Pomap potrafi tyko podawać insuline
Będzie rozbudowywane



 */
public class DTherapyAdvice {

    final public DTimeStamp time;
    final public double BG;

    //Dane glikemia
    public Double corrBG = null; //Ile do korekty
    public Double shortBG = null; //Jak szybko się zmienia
    public Double longBG = null;

    //Dane wrażliwość
    public Double sensW = null;
    public Double sensI = null;

    //
    public final int horizont; // Jeżeli jest 0 to nie obliczaj

    //Dane insulina
    public Double futureInsulinBase = null;
    public Double futureInsulinRegular = null;
    public Double futureInsulinCorrection = null;

    //Dane W
    public Double futureWRegular = null;
    public Double futureWCorrection = null;

    public Double futureWLiver = null;
    public Double futureBGKidneys = null; //Ile odfiltrują nerki Będzie jeszcze ile się zmieni na hipo i hiper

    //public Double planedMeal = null;
    public Double planedW = null; // Ile wymiennikow zaplanowano
    public Double planedB = null;
    public Double planedT = null;

    //Wyniki
    public Double insulinForPlannedW = null;
    public Double insulinForPlannedB = null;
    public Double insulinForPlannedT = null;
    
    public Double insulinForCorrection = null;
    public Double wForCorrection = null;

    public String info = "";

    public DTherapyAdvice(DTimeStamp dTimeStamp, double bg, int horizont) {

        time = dTimeStamp;
        BG = bg > 0 ? bg : bg;
        this.horizont = horizont > 0 ? horizont : 0;
    }

    public DTherapyAdvice(DTimeStamp dTimeStamp, double bg) {

        this(dTimeStamp, bg, 0);
    }

    public void writeMeal(DMeal dMeal) {

        if (dMeal != null) {
            planedW = dMeal.ww;
            planedB = dMeal.wb;
            planedT = dMeal.wt;
        }

    }

    @Override
    public String toString() {
        return ToStrings.therapyAdviceToString(this);
    }

}
