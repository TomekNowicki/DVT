package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DControlBox;

public class DControllerPump0 extends DController {

    protected DControllerPump0(DTherapyKit dTherapyKit, DCGM dCGM,
            DMealBox dMealBox, DControlBox dControlBox) {
        super(DType.PUMP0, dTherapyKit, dCGM, dMealBox, dControlBox);
    }

    @Override
    protected DTherapyDecision decisionForMeal() {

        var BG = currentGlycemia(); // -> loggs
        var meal = meals.planedNow();

        var advice = new DTherapyAdvice(clockTime, BG, horizont);
        advice.writeMeal(meal);
        kit.writeAdvice(advice);

        var decision = new DTherapyDecision(clockTime, dlogger);
        decision.insertMealRegular(meal);
        decision.insertBolusRegular(advice.insulinForPlannedW, kit.typeInsulinBolus);

        if (BG < minBG) {

            if (carbOverInsulin) {
                decision.insertMealCorrection(advice.wForCorrection);
            } else {
                decision.redoseBolusRegular(advice.insulinForCorrection);
            }

            controls.addExtraControl(clockTime.shift(nextCheck));

        } else if (BG > maxBG) {

            decision.insertBolusCorrection(advice.insulinForCorrection, kit.typeInsulinBolus);
            controls.addExtraControl(clockTime.shift(nextCheck));

        }

        var activeInsulin = advice.futureInsulinRegular + advice.futureInsulinCorrection;
        decision.regardActiveInsulin(activeInsulin);

        return decision;
    }

    @Override
    protected DTherapyDecision decisionForControl() {

        var BG = currentGlycemia(); // -> loggs

        var advice = new DTherapyAdvice(clockTime, BG, horizont);
        kit.writeAdvice(advice);

        var decision = new DTherapyDecision(clockTime, dlogger);

        if (BG < minBG) {
            
            decision.insertMealCorrection(advice.wForCorrection);
            controls.addExtraControl(clockTime.shift(nextCheck));
        
        } else if (BG > maxBG) {
            
            decision.insertBolusCorrection(advice.insulinForCorrection, kit.typeInsulinBolus);
            var activeInsulin = advice.futureInsulinRegular + advice.futureInsulinCorrection;
            decision.regardActiveInsulin(activeInsulin);
            controls.addExtraControl(clockTime.shift(nextCheck));
        }

        return decision;
    }

    @Override
    protected DTherapyDecision decisionForAlarmPatient() {
        pricksOff = 0;
        alarmsOff = alarmsSuspension;
        return decisionForControl();
    }

    @Override
    protected DTherapyDecision decisionForAlarmCGM() {

        return DTherapyDecision.ignoring(clockTime, dlogger);
    }

}
