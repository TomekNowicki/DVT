package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DControlBox;
import eu.tomasznowicki.dvt.kit.DTherapyKit;

//Używa sprzętu i tyle
public class DControllerHardware extends DController {

    private boolean useCGM = true; //Odczyty glikemii oraz
    
    private int horizont = 30; //Jeżeli 0 to nie bierze pod uwagę aktywenj
    

    protected DControllerHardware(DTherapyKit dTherapyKit, DCGM dCGM,
            DMealBox dMealBox, DControlBox dControlBox) {
        super(DType.HARDWARE, dTherapyKit, dCGM, dMealBox, dControlBox);
    }

    @Override
    protected DTherapyDecision decisionForMeal() {
        
        //
        var BG = currentGlycemia(); // -> loggs
        var meal = meals.planedNow();
        
        var advice = new DTherapyAdvice(clockTime, BG, horizont);
        
        advice.writeMeal(meal);
        meals.writeAdvice(advice);
        kit.writeAdvice(advice);
        cgm.writeAdvice(advice);
        
        var decision = new DTherapyDecision(clockTime, dlogger);
        
        if(BG < minBG) {
            //
        } else if(BG > maxBG) {
            //
        } else {
            //
            //Tutaj też można odpytac cgm
            
            
            
            decision.insertMealRegular(meal);
            //decision.insertBolusRegular(advice., DInsulin.DTypeInsulin.FIASP);
        }
        
        
        
        
        return decision;
    }

    @Override
    protected DTherapyDecision decisionForControl() {
        
        var BG = currentGlycemia(); // -> loggs
        
        var advice = new DTherapyAdvice(clockTime, BG,horizont);
        
        kit.writeAdvice(advice);
        cgm.writeAdvice(advice);
        
        
        var decision = new DTherapyDecision(clockTime, dlogger);
        
        
        if (BG < minBG) {
            
            
            
            
        } else if (BG > maxBG) {
            
        } else {
            
            //Ryzyko spadku, wzrostu i już można podać
            
            
        }

        return decision;
    }

    @Override
    protected DTherapyDecision decisionForAlarmPatient() {
        //

        return DTherapyDecision.ignoring(clockTime, dlogger);
    }

    @Override
    protected DTherapyDecision decisionForAlarmCGM() {

        if (useCGM) {

            //Obsługa alarmu
            return DTherapyDecision.ignoring(clockTime, dlogger);
        } else {
            return DTherapyDecision.ignoring(clockTime, dlogger);
        }

    }

}
