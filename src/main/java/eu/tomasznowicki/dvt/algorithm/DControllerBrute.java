package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DControlBox;

class DControllerBrute extends DController {

    protected DControllerBrute(DTherapyKit dTherapyKit, DCGM dCGM,
            DMealBox dMealBox, DControlBox dControlBox) {

        super(DType.BRUTE, dTherapyKit, dCGM, dMealBox, dControlBox);
    }

    @Override
    protected final DTherapyDecision decisionForMeal() {
        
        var BG = currentGlycemia(); // -> loggs
        var meal = meals.planedNow();
        
        var advice = new DTherapyAdvice(clockTime, BG);
        advice.writeMeal(meal);
        kit.writeAdvice(advice);
         
        var decision = new DTherapyDecision(clockTime, dlogger);
        decision.insertMealRegular(meal);
        decision.insertBolusRegular(advice.insulinForPlannedW, kit.typeInsulinBolus);

        return decision;
    }


    @Override
    protected final DTherapyDecision decisionForControl() {

        var BG = currentGlycemia(); // -> loggs
        return DTherapyDecision.ignoring(clockTime, dlogger);
    }

    @Override
    protected final DTherapyDecision decisionForAlarmPatient() {

        alarmsOff = alarmsSuspension;
        return DTherapyDecision.ignoring(clockTime, dlogger);
    }

    @Override
    protected final DTherapyDecision decisionForAlarmCGM() {

        alarmsOff = alarmsSuspension;
        return DTherapyDecision.ignoring(clockTime, dlogger);
    }
}