package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.algorithm.DNormalization;
import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DGlycemia;
import eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin.*;
import eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;

import static eu.tomasznowicki.dvt.raport.ToStrings.SEP;
import static eu.tomasznowicki.dvt.raport.ToStrings.bolusToString;
import static eu.tomasznowicki.dvt.raport.DStrings.KIT_SCHEDULING;
import eu.tomasznowicki.dvt.raport.DStrings;

public abstract class DTherapyKitAlgorithms extends DTherapyKit {

    public static final int SCHEDULE_DELAY = 0; //Minutes

    protected DTherapyKitAlgorithms(DTypeKit dTypeKit,
            DTypeInsulin dTypeInsulinBase, DTypeInsulin dTypeInsulinBolus,
            double dResolutionBase, double dResolutionBolus,
            int dResolutionWaveSpan, int dMaxWaveSpan, int dIntervalWaveSpan) {

        super(dTypeKit, dTypeInsulinBase, dTypeInsulinBolus, dResolutionBase, dResolutionBolus,
                dResolutionWaveSpan, dMaxWaveSpan, dIntervalWaveSpan);

    }

    @Override
    public final void scheduleInjection(DBolus dBolus, DFunctionInsulin dFunctionInsulin) {

        if (dlogger != null) {
            dlogger.log_function(clockTime, DStrings.KIT_SCHEDULING + SEP
                    + dFunctionInsulin.toString() + SEP
                    + bolusToString(dBolus));
        }

        if (dBolus != null
                && dBolus.typeInsulin == typeInsulinBolus
                && dFunctionInsulin != DFunctionInsulin.BASE
                && dBolus.time.equals(clockTime)
                && dBolus.dose > 0) {

            dBolus = normalizeInjection(dBolus);
            dBolus = dBolus.shift(SCHEDULE_DELAY);

            switch (dBolus.typeBolus) {
                case STANDARD:
                    addToPlan(dBolus, dFunctionInsulin); //->log
                    break;
                case SQUARE_WAVE:
                    scheduleBolusSquareWave(dBolus, dFunctionInsulin); //->log
                    break;
                case DUAL_WAVE:
                    var peak = DBolus.buildStandard(dBolus.time, dBolus.standardPart * dBolus.dose, dBolus.typeInsulin);
                    var wave = DBolus.buildSquareWave(dBolus.time, (1 - dBolus.standardPart) * dBolus.dose, dBolus.typeInsulin, dBolus.waveSpan);
                    addToPlan(peak, dFunctionInsulin); //->log
                    scheduleBolusSquareWave(wave, dFunctionInsulin); //->log
                    break;
                default:
                    break;
            }

        } else {

            if (dlogger != null) {
                dlogger.log_failed();
            }
        }
    }

    private boolean scheduleBolusSquareWave(DBolus dBolus, DFunctionInsulin dFunctionInsulin) {

        //Tutaj bolus jest znormalizowany
        int interval = 0;
        int number;
        double rawDose;

        do {
            interval += intervalWaveSpan;
            number = dBolus.waveSpan / interval;
            rawDose = dBolus.dose / (double) number;

        } while (rawDose < resolutionBolus && interval < dBolus.waveSpan);

        //Jest number bolusów z dawką startową dose każdy
        var componentTime = dBolus.time;
        double componentDose = rawDose;
        boolean success = false; //musi być przynajniej 1

        for (int i = 0; i < number; i++) {

            var dKitNormalization = new DNormalization(componentDose, resolutionBolus);

            DBolus component = DBolus.buildStandard(componentTime, dKitNormalization.value, dBolus.typeInsulin);

            addToPlan(component, dFunctionInsulin); //-> log

            componentDose = rawDose + dKitNormalization.rest;
            componentTime = componentTime.shift(interval);
        }

        return success;
    }

    @Override
    final public int normalizeWaveSpan(int dSpan) {

        var span = (int) DNormalization.getRecomendation(dSpan, resolutionWaveSpan);

        return span > maxWaveSpan ? maxWaveSpan : span;
    }

    @Override
    public final DBolus normalizeInjection(DBolus dBolus) {

        if (dBolus == null) {

            return null;

        } else if (dBolus.typeBolus == DBolus.DTypeBolus.STANDARD) {

            var normalizedDose = DNormalization.getRecomendation(dBolus.dose, resolutionBolus, RECOMENDATION);
            return DBolus.buildStandard(dBolus.time, normalizedDose, dBolus.typeInsulin);

        } else if (dBolus.typeBolus == DBolus.DTypeBolus.SQUARE_WAVE) {

            var dKitNormalization = new DNormalization(dBolus.dose, resolutionBolus, RECOMENDATION);
            var normalizedDose = dKitNormalization.recomendation;
            var normalizedSpan = normalizeWaveSpan(dBolus.waveSpan);

            //Muszą być możliwe przynajmniej 2 dawki
            if (dKitNormalization.multiple >= 2) {
                return DBolus.buildSquareWave(dBolus.time, normalizedDose, dBolus.typeInsulin, normalizedSpan);
            } else {
                return DBolus.buildStandard(dBolus.time, normalizedDose, dBolus.typeInsulin);
            }

        } else if (dBolus.typeBolus == DBolus.DTypeBolus.DUAL_WAVE) {

            var dKitNormalizationStandard = new DNormalization(dBolus.dose * dBolus.standardPart, resolutionBolus, RECOMENDATION);
            var dKitNormalizationWave = new DNormalization(dBolus.dose * (1 - dBolus.standardPart), resolutionBolus, RECOMENDATION);

            var normalizedDoseStandard = dKitNormalizationStandard.recomendation;
            var normalizedDoseWave = dKitNormalizationWave.recomendation;
            var normalizedSpan = normalizeWaveSpan(dBolus.waveSpan);

            if (normalizedSpan == 0 || dKitNormalizationWave.multiple < 2) {
                //Brak fali, wszystko przerzucam do standardu
                normalizedDoseStandard = DNormalization.getRecomendation(dBolus.dose, resolutionBolus, RECOMENDATION);
                normalizedDoseWave = 0;
                normalizedSpan = 0;
            } else if (dKitNormalizationStandard.multiple == 0 && normalizedSpan > 0) {
                //Jest fala, ale nie ma standardu, wszystko przezucam do fali
                normalizedDoseStandard = 0;
                normalizedDoseWave = DNormalization.getRecomendation(dBolus.dose, resolutionBolus, RECOMENDATION);
            }

            var normalizedDose = normalizedDoseStandard + normalizedDoseWave;

            if (normalizedDose > 0) {

                double normalizedStandardPart = normalizedDoseStandard / normalizedDose;
                return DBolus.buildDualWave(dBolus.time, normalizedDose, dBolus.typeInsulin, normalizedSpan, normalizedStandardPart);

            } else {
                //Nie ma ani fali, ani standardu
                return null;
            }

        } else {

            return null;
        }
    }

    //Wyniki bez normalizacji, oddzielnie: insulina na wymienniki, korekta insulinowa, korekta węglowodanowa
    //Korekta albo albo
    @Override
    public final void writeAdvice(DTherapyAdvice advice) { //Schodkowo
        
        advice.info += typeKit.toString();
        
        if(!advice.time.equals(clockTime)){
            advice.info = "Time Error!";
            return;
        }

        advice.corrBG = DGlycemia.bloodGlucoseToCorrect(advice.BG, minBG, maxBG);
        advice.sensI = configurationSensitivity.valueAtTimeStampStep(clockTime);
        advice.sensW = configurationBolus.valueAtTimeStampStep(clockTime);
        advice.futureInsulinBase = activeInsulin(BASE, insulinHorizont);
        advice.futureInsulinRegular = activeInsulin(REGULAR, insulinHorizont);
        advice.futureInsulinCorrection = activeInsulin(CORRECTION, insulinHorizont);

        //Insulina na wymienniki, jeżeli jest możliwość policzyć  
        if(advice.planedW != null && advice.planedW > 0) {
            advice.insulinForPlannedW = advice.planedW * advice.sensW;
        }
        
        //Korekta insulinowa, jeżeli jest możliwość policzyć
        if (Math.abs(advice.corrBG) > 0 && advice.sensI > 0) {
            advice.insulinForCorrection = advice.corrBG / advice.sensI;
        } else {
            advice.insulinForCorrection = 0.0;
        }

        //Korekta węglowodanowa, jeżeli jest możliwość policzyć
        if (Math.abs(advice.corrBG) > 0 && advice.sensI > 0 && advice.sensW > 0) {
            advice.wForCorrection = -advice.corrBG / (advice.sensI * advice.sensW);
        } else {
            advice.wForCorrection = 0.0;
        }

        if (dlogger != null) {
            dlogger.log_calculation_advice(clockTime, advice);
        }

        

    }

}
