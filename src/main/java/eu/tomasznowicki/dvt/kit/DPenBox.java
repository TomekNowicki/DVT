package eu.tomasznowicki.dvt.kit;

import eu.tomasznowicki.dvt.algorithm.DNormalization;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin;

import eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.kit.DTherapyKit.DTypeKit;

// Tutaj będą ustawienia
// Peny to będzie na śniadanie, 2 śniadanie, obiad, podwieczorek, kolacja
// Jakaś interpolacja ?
//Bo baza inaczej podawane przez peny, a inaczej przez pompe 
public class DPenBox extends DTherapyKitAlgorithms {

    public final static double RESOLUTION_BASE = 1.0;
    public final static double RESOLUTION_BOLUS = 0.5;

    public static final int INTERVAL = 60;
    public final static int WAVE_STEP = 1 * INTERVAL; //Ustawiem na wielokrotność tego
    public final static int WAVE_MAX = 3 * WAVE_STEP; //Maksymaly czas

    protected DPenBox(DTypeInsulin dInsulinTypeBase, DTypeInsulin dInsulinTypeBolus) {
        super(DTypeKit.PUMP, dInsulinTypeBase, dInsulinTypeBolus,
                RESOLUTION_BASE, RESOLUTION_BOLUS, WAVE_STEP, WAVE_MAX, INTERVAL);
    }

    @Override
    protected void scheduleInjectionBase() {
        
        clearPlan(DFunctionInsulin.BASE);

        boolean success = true;
        
        //Czyta ustawienia dosłownie
        
        for (var dTimeSetting : configurationBase.getSettings()){
            
            var dose = DNormalization.getRecomendation(dTimeSetting.value, resolutionBase, RECOMENDATION);
            
            var bolus = DBolus.buildStandard(dTimeSetting.time, dose, typeInsulinBase);
            
            /*success = */addToPlan(bolus, DFunctionInsulin.BASE);
            
        }
        
    }

}


