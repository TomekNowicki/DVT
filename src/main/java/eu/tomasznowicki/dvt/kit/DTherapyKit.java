package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.time.DTimeSetting;
import eu.tomasznowicki.dvt.time.DDaySettings;
import eu.tomasznowicki.dvt.time.DClockEventAction;
import eu.tomasznowicki.dvt.therapy.DTherapyAttendee;
import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DInsulin;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin.*;
import eu.tomasznowicki.dvt.raport.DLoggerKit;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import eu.tomasznowicki.dvt.raport.DStrings;
import static eu.tomasznowicki.dvt.raport.DStrings.KIT_CLEANING;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class DTherapyKit extends DTherapyAttendee implements TherapyAdvising {

    public static enum DTypeKit {
        PUMP, PENBOX
    };

    public static final double DEFAULT_BLOOD_GLOOCOSE_MIN = 70;
    public static final double DEFAULT_BLOOD_GLOOCOSE_MAX = 130;

    public static final double RECOMENDATION = 0.75;

    public final DTypeKit typeKit;
    public final DTypeInsulin typeInsulinBolus, typeInsulinBase;
    public final double resolutionBase, resolutionBolus;
    public final int resolutionWaveSpan, maxWaveSpan, intervalWaveSpan;

    protected int insulinHorizont;

    protected double minBG = DEFAULT_BLOOD_GLOOCOSE_MIN;
    protected double maxBG = DEFAULT_BLOOD_GLOOCOSE_MAX;

    protected final DDaySettings configurationSensitivity; //Ile glikemii obniża 1 jednostka. Wrażliwość na insulinę.
    protected final DDaySettings configurationBolus; //Ile jednostek insuliny na 1 wymiennik ( WW, WT, WB). Ustawienie bolusa.
    protected final DDaySettings configurationBase; //Baza dobowa. Inaczej interpretowana w pompie, a inaczej w penach.

    private double baseSum = 0;

    private final ArrayList<DBolus> planBase;
    private final ArrayList<DBolus> planRegular;
    private final ArrayList<DBolus> planCorrection;

    private final ArrayList<DBolus> historyBase;
    private final ArrayList<DBolus> historyRegular;
    private final ArrayList<DBolus> historyCorrection;

    protected DLoggerKit dlogger = null;

    public abstract int normalizeWaveSpan(int dSpan);

    public abstract DBolus normalizeInjection(DBolus dBolus);

    protected abstract void scheduleInjectionBase();

    public abstract void scheduleInjection(DBolus dBolus, DInsulin.DFunctionInsulin dFunctionInsulin);

    protected DTherapyKit(DTypeKit dTypeKit,
            DTypeInsulin dTypeInsulinBase, DTypeInsulin dTypeInsulinBolus,
            double dResolutionBase, double dResolutionBolus,
            int dResolutionWaveSpan, int dMaxWaveSpan, int dIntervalWaveSpan) {

        typeKit = dTypeKit;
        typeInsulinBase = dTypeInsulinBase;
        typeInsulinBolus = dTypeInsulinBolus;
        resolutionBase = dResolutionBase;
        resolutionBolus = dResolutionBolus;
        resolutionWaveSpan = dResolutionWaveSpan;
        intervalWaveSpan = dIntervalWaveSpan;
        maxWaveSpan = dMaxWaveSpan;
        insulinHorizont = DInsulin.getHorizontFor(typeInsulinBolus);
        configurationSensitivity = new DDaySettings();
        configurationBolus = new DDaySettings();
        configurationBase = new DDaySettings();
        planBase = new ArrayList<>();
        planRegular = new ArrayList<>();
        planCorrection = new ArrayList<>();
        historyBase = new ArrayList<>();
        historyRegular = new ArrayList<>();
        historyCorrection = new ArrayList<>();
    }

    public static final DTherapyKit getPump(DTypeInsulin dInsulinType) {

        return new DPump(dInsulinType);
    }

    public static final DTherapyKit getPenBox(DTypeInsulin dInsulinTypeBase, DTypeInsulin dInsulinTypeBolus) {

        return new DPenBox(dInsulinTypeBase, dInsulinTypeBolus);
    }

    public final void setLogger(DLoggerKit dLogger) {

        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    public void log_yourself() {
        if (dlogger != null) {
            dlogger.log_kit(this);
        }
    }

    protected final ArrayList<DBolus> takeHistory(DFunctionInsulin dFunctionInsulin) {

        switch (dFunctionInsulin) {
            case BASE:
                return historyBase;
            case REGULAR:
                return historyRegular;
            case CORRECTION:
                return historyCorrection;
            default:
                return null;
        }
    }

    protected final ArrayList<DBolus> takePlan(DFunctionInsulin dFunctionInsulin) {

        switch (dFunctionInsulin) {
            case BASE:
                return planBase;
            case REGULAR:
                return planRegular;
            case CORRECTION:
                return planCorrection;
            default:
                return null;
        }
    }

    protected final DTypeInsulin takeInsulinType(DFunctionInsulin dFunctionInsulin) {

        switch (dFunctionInsulin) {
            case BASE:
                return typeInsulinBase;
            case REGULAR:
                return typeInsulinBolus;
            case CORRECTION:
                return typeInsulinBolus;
            default:
                return null;
        }
    }

    public final void setInsulinHorizont(int dInsulinHorizont) {

        insulinHorizont = dInsulinHorizont >= 0 ? dInsulinHorizont : 0;
    }

    public final int getInsulinHorizont() {

        return insulinHorizont;
    }

    public final void setNormoglycemia(double dBloodGlucoseMin, double dBloodGlucoseMax) {

        minBG = dBloodGlucoseMin > 0 ? dBloodGlucoseMin : 0;
        maxBG = dBloodGlucoseMax > dBloodGlucoseMin ? dBloodGlucoseMax : dBloodGlucoseMin;
    }

    public final double[] getNormoglycemia() {

        return new double[]{minBG, maxBG};
    }

    private void setConfiguration(DDaySettings dDaySettings, DTimeSetting... dSettings) {

        if (dSettings != null) {

            for (var setting : dSettings) {

                if (setting.value >= 0) {

                    dDaySettings.addSetting(setting);

                } else {

                    dDaySettings.addSetting(new DTimeSetting(setting.time, 0));
                }
            }
        }

    }

    public final void setConfigurationSensitivity(DTimeSetting... dSettings) {

        setConfiguration(configurationSensitivity, dSettings);
    }

    public final DTimeSetting[] getConfigurationSensitivity() {

        return configurationSensitivity.getSettings();
    }
    
    public final double getSensitivity(DTimeStamp dTimeStamp){
        return configurationSensitivity.valueAtTimeStampStep(dTimeStamp);
    }

    public final void clearConfigurationSensitivity() {
        configurationSensitivity.clear();
    }

    public final void setConfigurationBolus(DTimeSetting... dSettings) {

        setConfiguration(configurationBolus, dSettings);
    }

    public final DTimeSetting[] getConfiguratinBolus() {

        return configurationBolus.getSettings();
    }
    
    public final double getBolusSetting(DTimeStamp dTimeStamp) {
        return configurationBolus.valueAtTimeStampStep(dTimeStamp);
    }

    public final void clearConfigurationBolus() {

        configurationBolus.clear();
    }

    public final void setConfiguratinBase(DTimeSetting... dSettings) {

        setConfiguration(configurationBase, dSettings);
        scheduleInjectionBase();
    }

    public final void clearConfigurationBase() {

        configurationBase.clear();
        scheduleInjectionBase();
    }

    public final DTimeSetting[] getConfigurationBase() {

        return configurationBase.getSettings();
    }

    public final double getBaseSum() {

        return baseSum;
    }

    private DBolus[] getBoluses(ArrayList<DBolus> dBoluses) {

        if (dBoluses.isEmpty()) {
            return null;
        } else {
            return dBoluses.toArray(new DBolus[dBoluses.size()]);
        }
    }

    public final DBolus[] getPlannedBoluses(DFunctionInsulin dInsulinFunction) {

        return getBoluses(takePlan(dInsulinFunction));
    }

    public final DBolus[] getHistoryBoluses(DFunctionInsulin dInsulinFunction) {

        return getBoluses(takeHistory(dInsulinFunction));
    }

    private void addToHistory(DBolus dBolus, DFunctionInsulin dFunctionInsulin) {

        var ok = (takeHistory(dFunctionInsulin)).add(dBolus);

        if (dlogger != null) {
            dlogger.log_adding_to_history(clockTime, dBolus, dFunctionInsulin.toString(), ok);
        }
    }

    protected final void addToPlan(DBolus dBolus, DFunctionInsulin dFunctionInsulin) {

        boolean ok = false;

        if (dBolus != null
                && dBolus.typeBolus == DBolus.DTypeBolus.STANDARD
                && dBolus.typeInsulin == takeInsulinType(dFunctionInsulin)
                && dBolus.dose > 0) {

            var plan = takePlan(dFunctionInsulin);

            if (dFunctionInsulin == BASE && dBolus.time.day > 0) {
                dBolus = DBolus.buildStandard(dBolus.time.dayTimeStamp(), dBolus.dose, dBolus.typeInsulin);
            }

            //DBolus bolusExisting = null; //Ewentulany bolus na tą samą chwilę czasową
            Iterator iterator = plan.iterator();
            while (iterator.hasNext()) {
                var bolus = (DBolus) iterator.next();
                if (bolus.time.equals(dBolus.time)) {
                    var bolusResultant = DBolus.join(bolus, dBolus);

                    if (dlogger != null) {
                        dlogger.log_joining_boluses(clockTime, bolus, dBolus, bolusResultant, dFunctionInsulin.toString());
                    }

                    dBolus = bolusResultant;
                    iterator.remove();
                    if (dFunctionInsulin == BASE) {
                        baseSum -= bolus.dose;
                    }
                }
            }

            ok = plan.add(dBolus);

            if (ok && dFunctionInsulin == BASE) {
                baseSum += dBolus.dose;
            }
        }

        if (dlogger != null) {
            dlogger.log_adding_to_plan(clockTime, dBolus, dFunctionInsulin.toString(), ok);
        }
    }

    protected final void clearPlan(DFunctionInsulin dFunctionInsulin) {

        (takePlan(dFunctionInsulin)).clear();
        if (dFunctionInsulin == BASE) {
            baseSum = 0;
        }
        if (dlogger != null) {
            dlogger.log_clearing_plan(clockTime, dFunctionInsulin.toString());
        }
    }

    private void cleanHistory(DFunctionInsulin dFunctionInsulin) {

        Iterator iterator = takeHistory(dFunctionInsulin).iterator();

        while (iterator.hasNext()) {

            var bolus = (DBolus) iterator.next();

            if (bolus.futureAt(clockTime) < DInsulin.THRESHOLD) {
                iterator.remove();
                if (dlogger != null) {
                    dlogger.log_cleaning_from_history(clockTime, bolus, dFunctionInsulin.toString());
                }
            }
        }
    }

    @Override
    protected void clean() { // Tylko histora, bo plany czyści przy podawaniu

        if (dlogger != null) {
            dlogger.log_function(clockTime, KIT_CLEANING + ENDL);
        }

        cleanHistory(DFunctionInsulin.REGULAR); // -> log
        cleanHistory(DFunctionInsulin.BASE); // -> log
        cleanHistory(DFunctionInsulin.CORRECTION); // -> log
    }

    public final double activeInsulin(DFunctionInsulin dFunctionInsulin) {

        var history = takeHistory(dFunctionInsulin);
        double accInsulin = 0;

        if (history != null) {

            for (DBolus bolus : history) {
                accInsulin += bolus.futureAt(clockTime);
            }
        }

        return accInsulin;
    }

    public final double activeInsulin(DFunctionInsulin dFunctionInsulin, int horizont) {

        var history = takeHistory(dFunctionInsulin);
        double accInsulin = 0;

        if (history != null) {

            for (DBolus bolus : history) {
                accInsulin += bolus.futureAt(clockTime, horizont);
            }
        }

        return accInsulin;
    }

    private DBolus injectionNow(DFunctionInsulin dFunctionInsulin) {

        var plan = takePlan(dFunctionInsulin);
        var searchTime = (dFunctionInsulin.equals(BASE)) ? clockTime.dayTimeStamp() : clockTime;
        DBolus toAdminister = null;
        var iterator = plan.iterator();
        while (iterator.hasNext()) {
            var bolus = (DBolus) iterator.next();
            if (bolus.time.equals(searchTime)) {
                if (dFunctionInsulin.equals(BASE)) {
                    toAdminister = DBolus.buildStandard(clockTime, bolus.dose, bolus.typeInsulin);
                    break;
                } else {
                    toAdminister = bolus;
                    iterator.remove(); //---> Dlatego planów się nie czyści
                    break;
                }
            }
        }
        return toAdminister;
    }

    @Override
    public void tickOutfit(DClockEventAction event) {

        if (event.getSource() != clock) {
            return;
        }

        var bolusBase = injectionNow(BASE);
        var bolusRegular = injectionNow(REGULAR);
        var bolusCorrection = injectionNow(CORRECTION);

        if (bolusBase != null) {

            if (patient != null) {
                patient.takeBolusBase(bolusBase);
            }

            if (dlogger != null) {
                dlogger.log_administering(clockTime, bolusBase, BASE.toString());
            }

            addToHistory(bolusBase, BASE); //->log
        }

        if (bolusRegular != null) {
            if (patient != null) {
                patient.takeBolusRegular(bolusRegular);
            }
            if (dlogger != null) {
                dlogger.log_administering(clockTime, bolusRegular, REGULAR.toString());
            }
            addToHistory(bolusRegular, REGULAR);//->log
        }

        if (bolusCorrection != null) {
            if (patient != null) {
                patient.takeBolusCorrection(bolusCorrection);
            }
            if (dlogger != null) {
                dlogger.log_administering(clockTime, bolusCorrection, DStrings.W_CORRECTION.toString());
            }
            addToHistory(bolusCorrection, CORRECTION);
        }
    }

    @Override
    public String toString() {

        String string = timeStampToString(clockTime) + SEP + typeKit + SEP;
        if (typeKit.equals(DTypeKit.PENBOX)) {
            string += DInsulin.DFunctionInsulin.BASE + SEP + typeInsulinBase
                    + DInsulin.DFunctionInsulin.REGULAR;
        }
        string += typeInsulinBolus + SEP + DStrings.INSULIN_HORIZONT + EQ + insulinHorizont + ENDL
                + DStrings.KIT_SETTINGS_SENSITIVITY + ENDL + daySettingsToString(configurationSensitivity) + ENDL
                + DStrings.KIT_SETTINGS_BOLUS + ENDL + daySettingsToString(configurationBolus) + ENDL
                + DStrings.KIT_SETTINGS_BASE + ENDL + daySettingsToString(configurationBase) + ENDL
                + TOTAL + SEP + insulinToString(baseSum) + ENDL
                + DStrings.KIT_PLAN + SEP + DFunctionInsulin.BASE + ENDL + arrayListBolusToString(planBase) + ENDL
                + DStrings.KIT_PLAN + SEP + DFunctionInsulin.REGULAR + ENDL + arrayListBolusToString(planRegular) + ENDL
                + DStrings.KIT_PLAN + SEP + DFunctionInsulin.CORRECTION + ENDL + arrayListBolusToString(planCorrection) + ENDL
                + DStrings.KIT_HISTORY + SEP + DFunctionInsulin.BASE + ENDL + arrayListBolusToString(historyBase) + ENDL
                + DStrings.KIT_HISTORY + SEP + DFunctionInsulin.REGULAR + ENDL + arrayListBolusToString(historyRegular) + ENDL
                + DStrings.KIT_HISTORY + SEP + DFunctionInsulin.CORRECTION + ENDL + arrayListBolusToString(historyCorrection);
        return string;
    }
}


/*
    public final void setConfBolus6(double... settings) {
        configurationBolus.clear();

        if (settings.length >= 6) {

            configurationBolus.addSettings(
                    new DTimeSetting(new DTimeStamp(0, 0, 0), settings[5]),
                    new DTimeSetting(new DTimeStamp(0, 5, 0), settings[0]),
                    new DTimeSetting(new DTimeStamp(0, 9, 30), settings[1]),
                    new DTimeSetting(new DTimeStamp(0, 12, 30), settings[2]),
                    new DTimeSetting(new DTimeStamp(0, 15, 30), settings[3]),
                    new DTimeSetting(new DTimeStamp(0, 18, 30), settings[4]),
                    new DTimeSetting(new DTimeStamp(0, 21, 30), settings[5])
            );
        }
    }
 */
 /* Może to przenieśc do pudełka statycznego
    //Ustawia co godzinę, chyba, że się skończą
    public final void setConfBase24(double... settings) {

        configurationBase.clear();

        for (int i = 0; i < 24; i++) {
            if (i < settings.length) {
                configurationBase.addSetting(new DTimeSetting(new DTimeStamp(0, i, 0), settings[i]));
            } else {
                break;
            }
        }

        scheduleInjectionBase();
    }
 */
