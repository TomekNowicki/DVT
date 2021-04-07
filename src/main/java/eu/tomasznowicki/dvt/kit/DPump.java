package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.algorithm.DNormalization;
import eu.tomasznowicki.dvt.time.DTimeStamp;

import eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.kit.DTherapyKit.DTypeKit;

import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin;

public class DPump extends DTherapyKitAlgorithms {

    public static final double RESOLUTION = 0.025;
    
    public static final int INTERVAL = 5;
    public final static int WAVE_STEP = 6 * INTERVAL; //Ustawiem na wielokrotność tego
    public final static int WAVE_MAX = 12 * WAVE_STEP; //Maksymaly czas

    protected DPump(DTypeInsulin dInsulinType) {

        super(DTypeKit.PUMP, dInsulinType, dInsulinType,
                RESOLUTION, RESOLUTION, WAVE_STEP, WAVE_MAX, INTERVAL);
    }



    @Override
    protected final void scheduleInjectionBase() {

        clearPlan(DFunctionInsulin.BASE);

        double baseAcc = 0;

        boolean success = true;

        //Przebiega dobe minuta po minucie
        for (var dayTime = new DTimeStamp(1); dayTime.total <= DTimeStamp.MINUTES_IN_DAY; dayTime = dayTime.shift(1)) {

            baseAcc += configurationBase.valueAtTimeStampStep(dayTime) / 60;//Ustawienie w [j/h]

            if (dayTime.total % INTERVAL == 0) {

                var dose = DNormalization.getRecomendation(baseAcc, resolutionBase, RECOMENDATION);

                if (dose > 0) {

                    var bolus = DBolus.buildStandard(dayTime, dose, typeInsulinBase);
                    /*success = */addToPlan(bolus, DFunctionInsulin.BASE);

                    baseAcc -= dose;
                }
            }
        }
    }

}
