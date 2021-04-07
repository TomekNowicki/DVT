package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DGlycemia;
import eu.tomasznowicki.dvt.biocyb.DInsulin;
import eu.tomasznowicki.dvt.biocyb.DNutrition;
import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.kit.DControlBox;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.raport.ToStrings;
import eu.tomasznowicki.dvt.time.DTimeStamp;

public class DControllerPID extends DController {

    public static final int DEFAULT_INTERVAL = 15;//min
    public static final double DEFAULT_AIM = 100; //mg/dl

    private int interval = DEFAULT_INTERVAL; //Co tyle minut wykonuje kontrole
    private double minBG, maxBG, aimBG = DEFAULT_AIM; //Sygnał

    private DGlycemia lastGlycemia = null;

    double lastOverValue = 0, lastOverAverage = 0;

    protected DControllerPID(DTherapyKit dTherapyKit, DCGM dCGM,
            DMealBox dMealBox, DControlBox dControlBox) {
        super(DType.PID, dTherapyKit, dCGM, dMealBox, dControlBox);
        prepareOutfit();
    }

    private void prepareOutfit() {
        //Kontrole to momenty działania kontrolera
        controls.clearAllControls();
        DTimeStamp controlTime = new DTimeStamp();
        while (controlTime.day == 0) {
            controls.addControl(controlTime);
            controlTime = controlTime.shift(interval);
        }
        //Pompa
        kit.clearConfigurationBase();
        kit.log_yourself();
        var norm = kit.getNormoglycemia();
        minBG = norm[0];
        maxBG = norm[1];
        //CGM
        cgm.alarmsOn(false);
    }

    @Override
    protected DTherapyDecision decisionForMeal() {  //A co jeżeli pokryje się z kontrolą ??? !!!
        //Po prostu podaje posiłek bez bolusa
        var meal = meals.planedNow(); //Podany z wyprzedzeniem
        var decision = new DTherapyDecision(clockTime, dlogger);
        decision.insertMealRegular(meal);
        return decision;
    }

    private String proportional(DGlycemia dGlycemia, DTherapyDecision dDecision) {

        var bg = dGlycemia.value;
        double trend = dGlycemia.rateShort != null ? dGlycemia.rateShort : 0;
        double average = dGlycemia.averageLong !=null ? dGlycemia.averageLong : bg;
        var sensI = kit.getSensitivity(clockTime); //Wrażliwość na insuline
        var sensW = kit.getBolusSetting(clockTime); //Wrażliwość na węglowodany
        String info = "Proportional: BG=" + bg + " trend"+ trend + " sensI=" + sensI + " sensW=" + sensW + " ";

        double min=minBG, max=maxBG;
        
        if (average > max && trend > 0) {
            double cbg = bg - max; //Tyle do korekty insulinowej
            var insulin = cbg / sensI;
            var bolus = DBolus.buildStandard(clockTime, insulin, kit.typeInsulinBolus);
            dDecision.bolusCorrection = bolus;
            info += "insulin correction: " + insulin + " -> " + ToStrings.bolusToString(bolus);
        } else if (average < min && trend < 0) {
            double cbg = min - bg; //Tyle do korekty węglowodanowej
            var ww = cbg / (sensI * sensW);
            var meal = DNutrition.getCorrectionMeal(clockTime, ww);
            dDecision.mealCorrection = meal;
            info += "carb correction: " + ww + " -> " + ToStrings.mealToString(meal);
        } else {
            info += "no correction";
        }
        
        return info;
    }

    
    @Override
    protected DTherapyDecision decisionForControl() { //Decyzja na najbliższe interval minut
        //
        var decision = new DTherapyDecision(clockTime, dlogger);
        var glycemia = cgm.getGlycemia();

        String info = "";

        if (glycemia != null) {
            info += proportional(glycemia, decision); //Wypełnia decyzje
            
            System.out.println(info);
        }

        if (dlogger != null) {
            dlogger.log_tep(info);
        }

        alarmsOff += interval; //Ma nie być alarmów
        return decision;
    }

    @Override
    protected DTherapyDecision decisionForAlarmPatient() {

        return DTherapyDecision.ignoring(clockTime, dlogger);
    }

    @Override
    protected DTherapyDecision decisionForAlarmCGM() {
        //
        //Nie reaguje na alarmy
        return DTherapyDecision.ignoring(clockTime, dlogger);
    }

}

//Może byc null mogą składniki być null
//Realizuje algorytm PID, co "interval" minu
//P 
//var g = cgm.getGlycemia(); //Wartość sygnału
//var pe = g != null ? g.value - aim : 0; //Wartość błędu
//I
//var c = cgm.getCumulative(horizontI); //Pole sygnału w horyzonice
//var ie = c != null ? c /*- aim * horizont*/ : 0;
//D
//var r = cgm.getRate(horizontP);
//var de = r / aim;
//Wysterowuje albo insulinę albo w (czyli ujemną insulinę)
//double insulin = Kp * pe + Ki * ie + Kd * de;
//System.out.println("--------> pe="+pe+" ie="+ie+" de="+de);
//if (insulin > 0) {
//Podaj insulinę
//decision.bolusCorrection = DBolus.buildStandard(clockTime, insulin, kit.typeInsulinBolus);
//} else {
//Podaj węglowodany
//Przeliczenie ujemnej na węglowodany
//advice.wForCorrection = -advice.corrBG / (advice.sensI * advice.sensW);
//}
//        info += glycemia.toString() + "\n";
//
//         //Tylko korekty: insulina lub węglowodany
//
//        var sensI = kit.getSensitivity(clockTime); //Wrażliwość na insuline
//        var sensW = kit.getBolusSetting(clockTime); //Wrażliwość na węglowodany
//
//        info += "sensI=" + sensI + " sensW=" + sensW + "\n";
//
//        double insulin1 = 0, insulin2 = 0, insulin3 = 0;
//        double ww1=0, ww2=0;
//
//        //info += "lastOverValue:" + lastOverValue + "->";
//
//        //Reakcja na poziom glikemii
//        //Ważne, żeby nie podawać dwa razy na to samo
//        double bg = glycemia.value;
//        if (bg > maxBG) {
//            double cbg = bg - aimBG; //Tyle do korekty           
//            insulin1 = cbg / sensI;
//        } else if(bg<minBG){
//            double cbg = minBG - bg;
//            ww1 = cbg / (sensI*sensW);
//        }
//
//        //info += lastOverValue + "\n";
//
//        //Reakcja na zmianę glikemii, wyliczamy insulinę na przewidywaną zmianę
//        if (glycemia.rateShort != null) {
//            double dbg = glycemia.rateShort * interval; //Zmiana glikemii do następnego pomiaru
//            insulin2 = dbg / sensI; //Ile insuliny na to. !Może być ujemny
//
//        }
//
////        if (glycemia.averageShort != null) {
////            info += "lastOverAverage=" + lastOverAverage + "->";
////
////            double gg = glycemia.averageShort - aimBG;
////
////            if (gg > lastOverAverage) {
////                double toDo = gg - lastOverAverage;
////                lastOverAverage = gg;
////                insulin3 = toDo / sensI;
////            }
////
////            info += lastOverAverage + "\n";
////
////        }
//
//        info += "insulin1=" + insulin1 + " insulin2=" + insulin2 + " insulin3=" + insulin3 + "\n";
//        
//        
//        double insulin = insulin1 + insulin2 + insulin3;
//        
//        if(insulin > 0){
//        
//        decision.bolusCorrection = DBolus.buildStandard(clockTime, insulin1 + insulin2 + insulin3, kit.typeInsulinBolus);
//        }
//        
//        else {
//            
//            //advice.wForCorrection = -advice.corrBG / (advice.sensI * advice.sensW);
//            
//            double w = -insulin / sensW;
//            info += "w="+w;
//            
//            decision.mealCorrection = DNutrition.getCorrectionMeal(clockTime, w);
//            
//            System.out.println("--->"+w);
//            
//        }
//        
//
//        
//
