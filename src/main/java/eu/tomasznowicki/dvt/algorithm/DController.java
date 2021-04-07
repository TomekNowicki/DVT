package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.therapy.DTherapyAttendee;
import eu.tomasznowicki.dvt.time.DClockEventAction;

import eu.tomasznowicki.dvt.kit.DControlBox;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.raport.DLoggerController;
import eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin;
import eu.tomasznowicki.dvt.biocyb.DNutrition.DFunctionMeal;
import static eu.tomasznowicki.dvt.raport.DStrings.*;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;

/*
    Tylko podaje posiłki i bolusy
 */
public abstract class DController extends DTherapyAttendee {

    public static enum DType {
        HARDWARE, //Bazuje na algorytmach sprzętu
        
        BRUTE, //Nie robi korekt, nie reaguje na alarmy
        
        PUMP0, //aktywna insulina
        PUMP1, //+aktywne wymienniki
        
        
        SMART0, //Robi korekty, reaguje na alermy pacjeta i CGM, nie monitoruje
        
        //Monitorowanie korekt
        //Branie pod uwagę aktywnej insuliny i aktywnych wymienników
        
        
        INTELLIGENT, //Monitoruje
        INTELLIGENT_CGM, //Robi i monitoruje korekty, korzysta z CGM
        AUTOPILOT, // Ustawia pompe/kit bez CGM
        AUTOPILOT_CGM, //Ustawia pompe/kit z CGM
        ARTIFICIAL_PANCREAS, //Na bieżąco reguluje bazą i ustawia kit
        PID, // //Proportional-integral-derivative control
        MP //Model predictive
    };

    public static enum DProcedure {
        MEAL, CONTROL, ALARM_PATIENT, ALARM_CGM
    };

    public static final int //
            DEFAULT_PRICK_VALIDITY = 15,
            DEFAULT_ALARM_SUSPENSION = 30,
            DEFAULT_CORRECTION_SUSPENSION = 30,
            DEFAULT_HORIZONT = 30,
            DEFAULT_NEXT_CHECK = 30;


    public final DType type;
    private boolean manual = false; //Stop, propozycja, decyzja, zatwierdzenie

    protected final DControlBox controls;
    protected final DMealBox meals;
    protected final DTherapyKit kit;
    protected final DCGM cgm;
    protected final Object patient = null; // !Intentional. No access here.

    protected double minBG, maxBG;

    
    protected boolean carbOverInsulin = false;
    protected int horizont = DEFAULT_HORIZONT;
    protected int nextCheck = DEFAULT_NEXT_CHECK;
    
    
    protected boolean mealRetiming = false, bolusRetiming = false;
    

    protected int //
            pricksOff = 0,
            controlValidity = DEFAULT_PRICK_VALIDITY,
            alarmsOff = 0,
            alarmsSuspension = DEFAULT_ALARM_SUSPENSION,
            correctionOff = 0,
            correctionSuspension = DEFAULT_CORRECTION_SUSPENSION;

    protected DLoggerController dlogger;

    protected DController(DType dTypeController, DTherapyKit dTherapyKit,
            DCGM dCGM, DMealBox dMealBox, DControlBox dControlBox) {

        type = dTypeController;
        controls = dControlBox;
        meals = dMealBox;
        kit = dTherapyKit;
        cgm = dCGM;

        var norm = kit.getNormoglycemia();
        minBG = norm[0];
        maxBG = norm[1];
    }

    static final public DController get(DType dTypeController,
            DTherapyKit dTherapyKit, DCGM dCGM, DMealBox dMealBox, DControlBox dControlTable) {

        switch (dTypeController) {
            case HARDWARE:
                return new DControllerHardware(dTherapyKit, dCGM, dMealBox, dControlTable);
            
            case BRUTE:
                return new DControllerBrute(dTherapyKit, dCGM, dMealBox, dControlTable);
            case PUMP0:
                return new DControllerPump0(dTherapyKit, dCGM, dMealBox, dControlTable);
            case SMART0:
                return new DControllerSmart0(dTherapyKit, dCGM, dMealBox, dControlTable);
            case INTELLIGENT:
                return null;
            case INTELLIGENT_CGM:
                return null;
            case AUTOPILOT:
                return null;
            case AUTOPILOT_CGM:
                return null;
            case ARTIFICIAL_PANCREAS:
                return null;
            case PID:
                return new DControllerPID(dTherapyKit, dCGM, dMealBox, dControlTable);
            case MP:
                return null;
        }
        return null;
    }

    public final void setLogger(DLoggerController dLogger) {
        if (dlogger == null) {
            dlogger = dLogger;
        }
    }

    public final void log_yourself() {
        if (dlogger != null) {
            dlogger.log_controller(this);
        }
    }

    public final void setManula(boolean dManual) {
        manual = dManual;
    }

    public final double currentGlycemia() {
        double BG;
        String info;
        if (pricksOff == 0) {
            BG = controls.prick().value;
            pricksOff = controlValidity;
            info = CONTROLLER_PRICK;
        } else {
            BG = controls.lastPrick().value;
            info = CONTROLLER_LAST + SEP + timeStampToString(controls.lastPrick().time);
        }
        info += SEP + BGtoString(BG);
        if (dlogger != null) {
            dlogger.log_info(info);
        }
        return BG;
    }
    
    

    protected abstract DTherapyDecision decisionForMeal();

    protected abstract DTherapyDecision decisionForControl();

    protected abstract DTherapyDecision decisionForAlarmPatient();

    protected abstract DTherapyDecision decisionForAlarmCGM();

    private DTherapyDecision prepareDecision(DProcedure dProcedure) {
        switch (dProcedure) {
            case MEAL:
                return decisionForMeal();
            case CONTROL:
                return decisionForControl();
            case ALARM_PATIENT:
                return decisionForAlarmPatient();
            case ALARM_CGM:
                return decisionForAlarmCGM();
        }
        return null; //Never happens
    }

    protected void approveDecision(DTherapyDecision decision) {
        if (manual) {
            //
            // Możliwość zmiany
            //
        }
    }

    private void executeDecision(DTherapyDecision decision) {

        String info = CONTROLLER_EXECUTING + ENDL;

        if (decision.empty()) {

            info += CONTROLLER_EMPTY_DECISION + ENDL;

        } else {

            if (decision.mealCorrection != null) {
                meals.scheduleMeal(decision.mealCorrection, DFunctionMeal.CORRECTION);
                info += mealToString(decision.mealCorrection) + SEP
                        + DFunctionMeal.CORRECTION.toString() + ENDL;
            }

            if (decision.mealRegular != null) {
                meals.scheduleMeal(decision.mealRegular, DFunctionMeal.REGULAR);
                info += mealToString(decision.mealRegular) + SEP
                        + DFunctionMeal.REGULAR.toString() + ENDL;
            }

            if (decision.bolusCorrection != null) {
                kit.scheduleInjection(decision.bolusCorrection, DFunctionInsulin.CORRECTION);
                info += bolusToString(decision.bolusCorrection) + SEP
                        + DFunctionInsulin.CORRECTION.toString() + ENDL;
            }

            if (decision.bolusRegular != null) {
                kit.scheduleInjection(decision.bolusRegular, DFunctionInsulin.REGULAR);
                info += bolusToString(decision.bolusRegular) + SEP
                        + DFunctionInsulin.REGULAR.toString() + ENDL;
            }
        }

        if (dlogger != null) {
            //info = info.substring(0, info.length() - ENDL.length());
            dlogger.log_info(info);
        }
    }

    private void therapy_procedure(DProcedure dProcedure) {
        DTherapyDecision decision = prepareDecision(dProcedure);
        approveDecision(decision);
        executeDecision(decision);
    }

    @Override
    public final void tickController(DClockEventAction event) {

        if (event.getSource() != clock) {
            return;
        }

        // !Mogą się nakładać akcje. Poszczególne kontrolery to załatwiają.
        //
        if (meals.doItNow()) {

            if (dlogger != null) {
                dlogger.log_procedure(clockTime, DProcedure.MEAL);
            }

            therapy_procedure(DProcedure.MEAL); // -> loggs
        }

        if (controls.doItNow()) {

            if (dlogger != null) {
                dlogger.log_procedure(clockTime, DProcedure.CONTROL);
            }

            therapy_procedure(DProcedure.CONTROL); // -> loggs
        }

        if (controls.alarm() && alarmsOff == 0) {

            if (dlogger != null) {
                dlogger.log_procedure(clockTime, DProcedure.ALARM_PATIENT);
            }

            therapy_procedure(DProcedure.ALARM_PATIENT); //-> loggs
        }

        if (cgm.alarm() && alarmsOff == 0) {

            if (dlogger != null) {
                dlogger.log_procedure(clockTime, DProcedure.ALARM_CGM);
            }

            therapy_procedure(DProcedure.ALARM_CGM); // -> loggs

        }

        if (alarmsOff > 0) {
            alarmsOff--;
        }

        if (pricksOff > 0) {
            pricksOff--;
        }

        if (correctionOff > 0) {
            correctionOff--;
        }
    }

    @Override
    protected void clean() {
        //Override if necessary
    }

    @Override
    public String toString() {
        return controllerToString(this);
    }

}

//
//Kontrole nie częściej niż
//Korekty insulinowe  nie częściej niż
//Korety węglowodanowe nie częściej niż
//Czy przekładać posiłek
//Czy przekladać bolus
//private DLoggerInput inLogger = null; //albo przenieść do terapii
// private DLoggerOutput outLogger = null; //albo przenieść do terapii
//logger samego kontrolera
//import eu.tomasznowicki.dvt.raport.DLoggerInput;
//import eu.tomasznowicki.dvt.raport.DLoggerOutput;
//Przeprowadza symulacje i kalibruje parametry
//na podstawie dHistory <- tutaj sa insight-y
//kalibracja możliwa między isight-ami
//? Co wychodzi wg moich ustawień <---
//? Co wyszło                     <---
//? Jak zmienić ustawienia, żeby wyszło dobrze <---
//Na bieżąco odgaduje ustawienia pacjenta
//!Potrzebne sa wartości startowe
//Potrzebuje tworzyć liste bolusów, liste posiłków  
//godzina, ile wstecz, tzn ile parametrów bede kalibrował
//biore wszystko co się wydarzyło i co ma znaczenie do kalibracji
//Czy on będzie miał swojego pacjenta?
//    private final Deque<DGlycemia> glycemia = new ArrayDeque<>();
//    private final Deque<DMeal> meal = new ArrayDeque<>();;
//    private final Deque<DBolus> bolus = new ArrayDeque<>();;
//    private final Deque<DBolus> base = new ArrayDeque<>();;
//Model pacjenta, taki wewnętrzny autoPilota do testów
//Wstępnie ustawiamy pacjeta odczytując kit
//Raczej całą terapie ma wewnętrznie
//Może ostatni dzień przeprowadzić wiele razy//Model pacjenta, taki wewnętrzny autoPilota do testów
//Wstępnie ustawiamy pacjeta odczytując kit
//Raczej całą terapie ma wewnętrznie
//Może ostatni dzień przeprowadzić wiele razy
//    public void setLogger(DLoggerController dLogger){
//        
//    }
//    public void noteMeal(DMeal dMeal){
//        // tutaj posiłki regularne i korektu
//    }
//    public void noteBolus(DBolus dBolus){
//        // bolusy do posiłków i korektu
//    }
//    public void noteBase(DBolus dBolus){
//        //bolusy bazowe
//    }
//    public void noteBloodGlucose(double bloodGlucose){
//        //z pomiarów penami
//    }
//    public void readCGM(DCGM dCGM){
//        //to raz na jakiś czas, może tak
//        
//    }    
//    public void noteBloodGlucose()
//    {         
////        if(!glycemia.offerLast(new DGlycemia(therapy.clockTime(),therapy.patientBG())))
////            therapy.logger.error("DController.noteBloodGlucose: offerLast error");
//    }
//    public void noteMeal(TimeStamp timeStamp, DMeal dMeal)
//    {
//        if(dMeal==null) return;
//        if(timeStamp.total==dMeal.clockTime.total)
//        {
//            if(!meal.offerLast(dMeal))
//                therapy.logger.error("AutoPilot.noteMeal: meal.offerLast error");
//        }
//        else
//            therapy.logger.error("AutoPilot.noteMeal: timeStamp error");
//    }
//    public void noteBolus(TimeStamp timeStamp, DBolus dBolus)
//    {
//        if(dBolus == null) return;
//        if(timeStamp.total == dBolus.clockTime.total)
//        {
//            if(!bolus.offerLast(dBolus))
//                therapy.logger.error("AutoPilot.noteBolus: bolus.offerLast error");
//        }
//        else 
//            therapy.logger.error("AutoPilot.noteBolus: timeStamp error");
//    }
//    public void noteBase(TimeStamp timeStamp, DBolus dBolus)
//    {   
//        if(dBolus == null) return;
//        if(timeStamp.total == dBolus.clockTime.total)
//        {
//            if(!base.offerLast(dBolus))
//                therapy.logger.error("AutoPilot.noteBase: base.offerLast error");
//        }
//        else 
//            therapy.logger.error("AutoPilot.noteBase: timeStamp error");
//    }
//    public void lookAtcgm(Dcgm cgm)
//    {
//        //
//        
//        /*
//        
//        if(!usingCGM) return;
//        DGlycemia g = cgm.lastGlycemia();
//        if(g==null) return;
//        noteBloodGlucose(g.timeStamp,g.BG);
//        
//        */
//    }
//    public void calculate(TimeStamp timeStamp){
//        //
//        
//  
//        
//    }
//    
//Nowe ustawienia dla kitu

/*

        if(BG<DDoctor.ALARM_MIN_BG)
            therapy.logger.error("AutoPilot.noteBloodGlucose: BG<0");
 */
//dHistory = new ArrayList<>(); 
//sensInsul0 = new double[sensResolution];
//sensWW0    = new double[sensResolution];
//sensLiver0 = new double[sensResolution];
//sensInsul  = new double[sensResolution];
//sensWW     = new double[sensResolution];
//sensLiver  = new double[sensResolution];
//Tutaj jakieś sensowne wartośc poczatkowe uniwrsalne
//for(int i=0; i<sensResolution; i++)
//    sensInsul0[i]=sensWW0[i]=sensLiver0[i]=0;
/*
    public boolean readPump(DPump dPump){
        //
        //!Wazna kolejnosc uruchamiania
        initialSensInsul(dPump);
        initialSensWW(dPump);
        initialSensLiver(dPump);
        
                //Przepisanie
        for(int i=0; i<sensResolution; i++){
            sensInsul[i]=sensInsul0[i];
            sensWW[i]=sensWW0[i];
            sensLiver[i]=sensLiver0[i];
        }
        
        return true;
        
    }
 */
 /*
    private void initialSensInsul(DPump dPump){
        //
        Clock clock = new Clock(new TimeStamp(0,0,0));
        for(int i=0; i<sensResolution; i++){
            clock.pushOneMinute();
            sensInsul0[i] = dPump.sensAtTime(clock.clockTime());         
        }
    }
 */
 /*
    private final List<DInsight> dHistory; 
    
    public final int sensResolution = 1440; // =24*60
    public final int hoursBack = 6;
    public final int minutesBack = hoursBack*60;
    
    private final double[] sensInsul0; //ile glikemii obniża 1j insuliny
    private final double[] sensWW0;    //ile glikemii podnosi 1WW
    private final double[] sensLiver0; //ile WW podrzuca wątroby
    private double[] sensInsul;
    private double[] sensWW;
    private double[] sensLiver;
    
    private class BolusMeal{
        public final DEvent bolus;
        public final DEvent meal;
        public BolusMeal(DEvent _bolus, DEvent _meal){bolus=_bolus; meal=_meal;}
        public double minuteBolusAtTime(TimeStamp timeStamp){ //Ile jednostek na WW w danej minucie
            double insulin = bolus.activateAtMinute(timeStamp); 
            double ww = meal.activateAtMinute(timeStamp);
            return ww>0 ? insulin/ww : 0; //Nie da się określić
        }
    }
 */
 /*
    public void test(){
        //
        System.out.println("AutoPilot - testy");
        
        
        calibrate24();
      
        
        try{
            
            File file1 = new File("sensInsul");
            File file2 = new File("sensWW");
            File file3 = new File("sensLiver");
            FileWriter fileWriter1 = new FileWriter(file1);
            FileWriter fileWriter2 = new FileWriter(file2);
            FileWriter fileWriter3 = new FileWriter(file3);       
            
            for(double x: sensInsul) fileWriter1.write(Double.toString(x)+"\n");    
            for(double x: sensWW   ) fileWriter2.write(Double.toString(x)+"\n");
            for(double x: sensLiver) fileWriter3.write(Double.toString(x)+"\n");
              
            fileWriter1.flush(); fileWriter2.flush(); fileWriter3.flush();
            fileWriter1.close(); fileWriter2.close(); fileWriter3.close();
        }
        catch(IOException e) { e.printStackTrace();};
        
    }//public void test()
 */
 /*
    public void calibrate24(){
        //
        // [ts,BG,ww,i]---[ts,BG,ww,i]---[ts,BG,ww,i]---[ts,BG,ww,i]
        //Kalibrujemy majac wstecz hourBack zapisow
        //To ma mieć możliwość przeporwadzenia symulacji
        
        //Teraz robi od poczatku, ale to bedzie ileś godzin wstecz
        
        //Lista bolusów
        List<NovorapidBolus> boluses = new ArrayList<>();
        List<SimpleMeal> meals = new ArrayList<>();
            
        for(DInsight i : dHistory){
            boluses.add(new NovorapidBolus(i.ts,i.ib));
            meals.add(new SimpleMeal(i.ts,i.ww));
        }
        
       DMetabolizm_to_dell metaBox = new DMetabolizm_to_dell(100,0); //!Na sztywno 100
       
       int steps = dHistory.get(dHistory.size()-1).ts.total - dHistory.get(0).ts.total;
       
       System.out.println(steps);
       
       for(int i=0; i<steps; i++){
           
           
       }
       
       //dHistory.get(0);
       
       System.out.println("--->"+dHistory.get(dHistory.size()-1));
        
    }
 */

 /*
    private void initialSensLiver(DPump dPump){
        //
        Clock clock = new Clock(new TimeStamp(0,24-hoursBack,0));
        TimeStamp timeStamp;
        Deque<NovorapidBolus> baseBolus = new ArrayDeque<>();
        for(int i=0; i<minutesBack+1; i++){
            timeStamp = clock.clockTime();
            double insulin = dPump.baseAtMinute_j(timeStamp);
            NovorapidBolus bolus = new NovorapidBolus(timeStamp,insulin);
            baseBolus.addLast(bolus);
            clock.pushOneMinute();
        }
        //! tutaj: TimeStamp[1 0:1]
        for(int i=0; i<sensResolution; i++){
            timeStamp = clock.clockTime();
            for(NovorapidBolus b : baseBolus)
                sensLiver0[i] += b.activateAtMinute(timeStamp);
            baseBolus.removeFirst();
            double insulin = dPump.baseAtMinute_j(timeStamp);
            NovorapidBolus bolus = new NovorapidBolus(timeStamp,insulin);
            baseBolus.addLast(bolus);
            clock.pushOneMinute();
        }
        //Ile insuliny w każdej minucie, przeliczam na WW
        for(int i=0; i<24; i++)
            sensLiver0[i] = sensLiver0[i]*sensInsul0[i]/sensWW0[i];
    }
 */
 /*
    private void initialSensWW(DPump dPump){
        //        
        Clock clock = new Clock(new TimeStamp(0,24-hoursBack,0));
        TimeStamp timeStamp;
        Deque<BolusMeal> bolusMeal = new ArrayDeque<>();
        for(int i=0; i<minutesBack+1; i++){  
            timeStamp = clock.clockTime();
            NovorapidBolus bolus = new NovorapidBolus(timeStamp, dPump.bolusAtMinute_j(timeStamp,1));
            SimpleMeal meal = new SimpleMeal(timeStamp,1);
            bolusMeal.addLast(new BolusMeal(bolus,meal));
            clock.pushOneMinute();
        }
        //! tutaj: TimeStamp[1 0:1]
        for(int i=0; i<sensResolution; i++){
            //            
            timeStamp = clock.clockTime();
            for(BolusMeal bm : bolusMeal){
                double s = bm.minuteBolusAtTime(timeStamp);
                if(sensWW0[i]<s) sensWW0[i]=s;
            }
            bolusMeal.removeFirst();
            NovorapidBolus bolus = new NovorapidBolus(timeStamp, dPump.bolusAtMinute_j(timeStamp,1));
            SimpleMeal meal = new SimpleMeal(timeStamp,1);
            bolusMeal.addLast(new BolusMeal(bolus,meal));      
            clock.pushOneMinute();
        }
        for(int i=0; i<sensResolution; i++) sensWW0[i] *= sensInsul0[i];
    }
 */
