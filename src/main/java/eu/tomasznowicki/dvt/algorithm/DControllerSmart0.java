package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DControlBox;

public class DControllerSmart0 extends DController {

    protected DControllerSmart0(DTherapyKit dTherapyKit, DCGM dCGM,
            DMealBox dMealBox, DControlBox dControlBox) {
        //
        super(DType.SMART0, dTherapyKit, dCGM, dMealBox, dControlBox);
    }

    @Override
    protected DTherapyDecision decisionForMeal() {
        return DTherapyDecision.ignoring(clockTime,dlogger);
    }

    @Override
    protected DTherapyDecision decisionForControl() {
        return DTherapyDecision.ignoring(clockTime,dlogger);
    }

    @Override
    protected final DTherapyDecision decisionForAlarmPatient() {
        return DTherapyDecision.ignoring(clockTime,dlogger);
    }

    @Override
    protected final DTherapyDecision decisionForAlarmCGM() {
        return DTherapyDecision.ignoring(clockTime,dlogger);
    }
}