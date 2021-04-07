package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.therapy.DTherapyAttendee;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.time.DClockEventSync;
import eu.tomasznowicki.dvt.time.DClockEventAction;
import eu.tomasznowicki.dvt.biocyb.DNutrition;
import eu.tomasznowicki.dvt.biocyb.DNutrition.DFunctionMeal;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import static eu.tomasznowicki.dvt.raport.ToStrings.mealBoxToString;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import eu.tomasznowicki.dvt.raport.DLoggerMealBox;

public class DMealBox extends DTherapyAttendee implements TherapyAdvising {

    public static final int //
            AHEAD_MIN_VALUE = 0,
            AHEAD_MAX_VALUE = 20;
    private int ahead = AHEAD_MIN_VALUE; //Ile minut wcześniej zgłosić
    private DMeal mealNow = null; //Komunikat -> Do obsługi: podanie insuliny, zaschedulowanie do podania
    protected final Set<DMeal> billOfFare; //Plan dobowy; dobowy znacznik czasowy, bez powtórek czasowych
    protected DMeal extraFare = null; //Tylko 1 może być odłożony, skasowany po wywołaniu
    private final ArrayList<DMeal> planRegular; //Do wykonania; znacznik czasowy symulacji, bez powtórek, bo sumowanie
    private final ArrayList<DMeal> planCorrection; //Do wykonania; znacznik czasowy symulacji, bez powtórek, bo sumowanie
    private final ArrayList<DMeal> historyRegular; //Historia podanych, bez powtórek
    private final ArrayList<DMeal> historyCorrection; //Histor podanych, bez powtórek

    DLoggerMealBox dlogger = null;

    public DMealBox() {
        billOfFare = new TreeSet<>();
        planRegular = new ArrayList<>();
        planCorrection = new ArrayList<>();
        historyRegular = new ArrayList<>();
        historyCorrection = new ArrayList<>();
    }

    public final void setLogger(DLoggerMealBox dLogger) {

        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    public void log_yourself() {
        if (dlogger != null) {
            dlogger.log_mealbox(this);
        }
    }

    private ArrayList<DMeal> takePlan(DFunctionMeal dFunctionMeal) {
        switch (dFunctionMeal) {
            case REGULAR:
                return planRegular;
            case CORRECTION:
                return planCorrection;
            default:
                return null;
        }
    }

    private ArrayList<DMeal> takeHistory(DFunctionMeal dFunctionMeal) {
        switch (dFunctionMeal) {
            case REGULAR:
                return historyRegular;
            case CORRECTION:
                return historyCorrection;
            default:
                return null;
        }
    }

    final public void setAhead(int minutes) {
        if (ahead < AHEAD_MIN_VALUE) {
            ahead = AHEAD_MIN_VALUE;
        } else if (ahead > AHEAD_MAX_VALUE) {
            ahead = AHEAD_MAX_VALUE;
        } else {
            ahead = minutes;
        }
    }

    final public int getAhead() {
        return ahead;
    }

    final protected DMeal dayMealToSimMeal(DMeal dMeal) {
        var day = clockTime.point > dMeal.time.point ? clockTime.day + 1 : clockTime.day;
        return new DMeal(new DTimeStamp(day, dMeal.time.hour, dMeal.time.minute), dMeal);
    }

    final public boolean addMeal(DMeal dMeal) {
        if (dMeal != null) {
            return billOfFare.add(new DMeal(dMeal.time.dayTimeStamp(), dMeal));
        } else {
            return false;
        }
    }

    final public boolean addMeals(DMeal... dMeals) {
        boolean success = true;
        for (DMeal meal : dMeals) {
            success = addMeal(meal);
        }
        return success;
    }

    final public boolean addExtraMeal(DMeal dMeal) {
        if (dMeal != null && dMeal.time.compareTo(clockTime) > 0) { // -> Only future
            extraFare = dMeal;
            return true;
        } else {
            return false;
        }
    }

    final public DMeal[] getMeals() {
        return billOfFare.toArray(new DMeal[billOfFare.size()]);
    }

    final public DMeal getExtraMeal() {
        return extraFare;
    }

    final public void clearMeals() {
        billOfFare.clear();
    }

    final public void clearExtraMeal() {
        extraFare = null;
    }

    final public DMeal[] getHistoryRegular() {
        return historyRegular.toArray(new DMeal[historyRegular.size()]);
    }

    final public DMeal[] getHistoryCorrection() {
        return historyCorrection.toArray(new DMeal[historyCorrection.size()]);
    }

    final public DMeal[] getPlanRegular() {
        return planRegular.toArray(new DMeal[planRegular.size()]);
    }

    final public DMeal[] getPlanCorrection() {
        return planCorrection.toArray(new DMeal[planCorrection.size()]);
    }

    private void addToHistory(DMeal dMeal, DFunctionMeal dFunctionMeal) {
        var ok = (takeHistory(dFunctionMeal)).add(dMeal);
        if (dlogger != null) {
            dlogger.log_adding_to_history(clockTime, dMeal, dFunctionMeal.toString(), ok);
        }

    }

    private void addToPlan(DMeal dMeal, DFunctionMeal dFunctionMeal) { //Gwarantuje brak powótrzeń

        boolean ok = false;

        if (dMeal != null && dMeal.wbt > 0) {

            var plan = takePlan(dFunctionMeal);

            Iterator iterator = plan.iterator();
            while (iterator.hasNext()) {
                var meal = (DMeal) iterator.next();
                if (meal.time.equals(dMeal.time)) {
                    var resultantMeal = DMeal.join(meal, dMeal); //!Przydałoby sie logowanie
                    if (dlogger != null) {
                        dlogger.log_joining_meals(clockTime, dMeal, meal, resultantMeal, dFunctionMeal.toString());
                    }
                    dMeal = resultantMeal;
                    iterator.remove();
                }
            }
            ok = plan.add(dMeal);
        }

        if (dlogger != null) {
            dlogger.log_adding_to_plan(clockTime, dMeal, dFunctionMeal.toString(), ok);
        }
    }

    public final void scheduleMeal(DMeal dMeal, DFunctionMeal dFunctionMeal) {

        if (dlogger != null) {
            dlogger.log_scheduling(clockTime, dMeal, dFunctionMeal.toString());
        }

        if (dMeal != null && dMeal.wbt > 0 && dMeal.time.compareTo(clockTime) >= 0) {
            addToPlan(dMeal, dFunctionMeal); //-> log
        }
    }

    private DMeal mealNow(DFunctionMeal dFunctionMeal) {

        var plan = takePlan(dFunctionMeal);
        DMeal toServe = null;
        var iterator = plan.iterator();
        while (iterator.hasNext()) {
            var meal = (DMeal) iterator.next();
            if (meal.time.equals(clockTime)) {
                toServe = meal;
                iterator.remove(); //Dlatego planów nie trzeba czyścić
                break;
            }
        }
        return toServe;
    }

    public final boolean doItNow() {
        return mealNow != null;
    }

    public final DMeal planedNow() {
        return mealNow;
    }

    public final DMeal nextMealFrom(DTimeStamp dTimeStamp) {
        //
        // Do zrobienia.
        // Nie łączyć tego z check_this_minute()
        //
        return null;
    }

    private void check_this_minute() { //Pierszeństwo ma extra, może przysłonić planowany
        mealNow = nextMealFrom(clockTime);
        DTimeStamp aheadTime = clockTime.shift(ahead);
        mealNow = null;
        if (extraFare != null && extraFare.time.compareTo(aheadTime) <= 0) {
            mealNow = extraFare;
            extraFare = null;
        } else {
            for (DMeal dayMeal : billOfFare) {
                var simMeal = dayMealToSimMeal(dayMeal);
                if (simMeal.time.equals(aheadTime)) {
                    mealNow = simMeal;
                    break;
                }
            }
        }
    }

    private void cleanHistory(DFunctionMeal dFunctionMeal) {

        Iterator iterator = (takeHistory(dFunctionMeal)).iterator();

        while (iterator.hasNext()) {

            var meal = (DMeal) iterator.next();

            if (meal.futureAt(clockTime) < DNutrition.THRESHOLD) {

                iterator.remove();
                if (dlogger != null) {
                    dlogger.log_cleaning_from_history(clockTime, meal, dFunctionMeal.toString());
                }
            }
        }
    }

    public final double activeW(DFunctionMeal dFunctionMeal) {
        var history = takeHistory(dFunctionMeal);
        double accW = 0;
        if (history != null) {
            for (DMeal meal : history) {
                accW += meal.futureAt(clockTime);
            }
        }
        return accW;
    }

    public final double activeW(DFunctionMeal dFunctionMeal, int horizont) {
        var history = takeHistory(dFunctionMeal);
        double accW = 0;
        if (history != null) {
            for (DMeal meal : history) {
                accW += meal.futureAt(clockTime, horizont);
            }
        }
        return accW;
    }

    @Override
    public void syncReceived(DClockEventSync event) {
        if (event.getSource() != clock) {
            return;
        }
        super.syncReceived(event); // -> clockTime
        check_this_minute(); // -> mealNow
    }

    @Override
    public void tickOutfit(DClockEventAction event) {
        if (event.getSource() != clock) {
            return;
        }
        var mealRegular = mealNow(DFunctionMeal.REGULAR);
        var mealCorrection = mealNow(DFunctionMeal.CORRECTION);

        if (mealRegular != null) {
            if (patient != null) {
                patient.takeMealRegular(mealRegular);
            }
            if (dlogger != null) {
                dlogger.log_serving(clockTime, mealRegular, DFunctionMeal.REGULAR.toString());
            }

            addToHistory(mealRegular, DFunctionMeal.REGULAR);
        }

        if (mealCorrection != null) {
            if (patient != null) {
                patient.takeMealCorrection(mealCorrection);
            }

            if (dlogger != null) {
                dlogger.log_serving(clockTime, mealCorrection, DFunctionMeal.CORRECTION.toString());
            }

            addToHistory(mealCorrection, DFunctionMeal.CORRECTION);
        }
    }

    @Override
    protected void clean() {

        if (dlogger != null) {
            dlogger.log_cleaning(clockTime);
            //dlogger.log_meals(clockTime, historyRegular.toArray(new DMeal[historyRegular.size()]), DFunctionMeal.REGULAR.toString());
            //dlogger.log_meals(clockTime, historyCorrection.toArray(new DMeal[historyCorrection.size()]), DFunctionMeal.CORRECTION.toString());
        }

        cleanHistory(DFunctionMeal.REGULAR);
        cleanHistory(DFunctionMeal.CORRECTION);
    }

    @Override
    public String toString() {
        return mealBoxToString(this);
    }

    @Override
    public void writeAdvice(DTherapyAdvice advice) {
        
        advice.info += "MealBox ";
        
        if(!advice.time.equals(clockTime)){
            advice.info = "Time Error!";
            return;
        }
        
        if(advice.horizont > 0) {
            advice.futureWRegular = activeW(DFunctionMeal.REGULAR, advice.horizont);
            advice.futureWCorrection = activeW(DFunctionMeal.REGULAR, advice.horizont);
        }
        
 
        
    }

}
