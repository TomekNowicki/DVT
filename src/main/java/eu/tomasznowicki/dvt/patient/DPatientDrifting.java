package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DTimeSetting;
import java.util.Random;

public class DPatientDrifting extends DPatient {

    public static final double //
            DEFAULT_UP = 0.3,
            DEFAULT_DOWN = 0.3,
            DEFAULT_CHANGE = 0.05,
            DEFAULT_ODDS = 0.0;

    private Random random = new Random();

    private double // -----------------> przydałoby się w prawo w lewo
            oddsLiver, upLiver, downLiver, changeLiver,
            upKidneys, downKidneys, changeKidneys, oddsKidneys,
            upSensI, downSensI, changeSensI, oddsSensI,
            upSensW, downSensW, changeSensW, oddsSensW;
    
    private int timeDriftLiver = 30;

    public DPatientDrifting(double dBloodGlucose) {
        super(dBloodGlucose);
        upLiver = upKidneys = upSensI = upSensW = DEFAULT_UP;
        downLiver = downKidneys = downSensI = downSensW = DEFAULT_DOWN;
        changeLiver = changeKidneys = changeSensI = changeSensW = DEFAULT_CHANGE;
        oddsLiver = oddsKidneys = oddsSensI = oddsSensW = DEFAULT_ODDS;
    }

    //Losowa zmiana punktów 
    //0 to jest pierwotne, 1 to jest bieżace, !Te same rozmiary
    private DTimeSetting[] driftSettings( DTimeSetting[] settingsInt, DTimeSetting[] settingsCurr,
            double up, double down, double change) {
        //
      
        int n = settingsInt.length;
        DTimeSetting[] settingsNew = new DTimeSetting[n];

        for (int i = 0; i < n; i++) {
            var value = settingsCurr[i].value; //Wartość kóra jest
            var time = settingsCurr[i].time;

            //Dryft wartości
            var min = (1 - down) * settingsInt[i].value; //Min tutaj
            if (min < 0) {
                min = 0;
            }
            var max = (1 + up) * settingsInt[i].value; //Max tutaj
            var dv = change * (max - min); //Krok zmiany

            if (value + dv > max) {
                value -= dv; //bo w górę już nie można
            } else if (value - dv < min) {
                value += dv; //bo w dół już nie można
            } else {
                //Można w górę lub w dół
                value = (random.nextDouble() > 0.5) ? value + dv : value - dv;
            }

            //Dryft czasu
            if(time.point != 0){
                

               int d = (int)(( (2.0*random.nextDouble())-1.0 ) * timeDriftLiver );
               time = settingsInt[i].time.shift(d);

                    //time = time.shift(10);
            }
            
            
            
            
            settingsNew[i] = new DTimeSetting(time, value);

        }

        return settingsNew;
    }

    @Override
    public boolean floatLiver() {
        //
        if (random.nextDouble() < oddsLiver) {
            return false;
        } else {
                        
            var newSettings = driftSettings(setLiver, settingsLiver.getSettings(),upLiver,downLiver,changeLiver);    
            settingsLiver.clear();
            settingsLiver.addSettings(newSettings);
            setActionLiver(settingsLiver.dayValuesPolyline());
            return true;
        }
    }

    @Override
    protected boolean floatKidneys() {
        //
        if (random.nextDouble() < oddsKidneys) {
            return false;
        } else {
            //Zmien
            return true;
        }
    }

    @Override
    protected boolean floatSensitivityInsulin() {
        //
        if (random.nextDouble() < oddsSensI) {
            return false;
        } else {
            //Zmien
            return true;
        }
    }

    @Override
    protected boolean floatSensitivityW() {
        //
        if (random.nextDouble() < oddsSensW) {
            return false;
        } else {
            //Zmien
            return true;
        }
    }

}

//    public void flowLiver() {
//        //Zmienia ustawienia wątroby
//
//        double up = 0.3, down = 0.2; //Tyle maksymalnie może pójśc w górę i w dół
//        double change = 0.2; //
//
//        if (random.nextDouble() > 0) { //Prawdopodobieństwo zmiany
//
//            var settings0 = settingsLiver0.getSettings(); //Te same rozmiary
//            var settings = settingsLiver.getSettings(); //Te same rozmiary
//
//            var newSettings = new ArrayList<DTimeSetting>(settings0.length);
//
//            for (int i = 0; i < settings0.length; i++) {
//                var max = settings0[i].value * (1 + up);
//                var min = settings0[i].value * (1 - down); //Aby nie spadło poniżej zera
//                var value = settings[i].value;
//                var dv = change * (max - min);
//                double newValue = value;
//
//                if (value + dv > max) {
//                    newValue -= dv;
//                } else if (value - dv < min) {
//                    newValue += dv;
//                } else {
//                    if (random.nextDouble() > 0.5) {
//                        newValue += dv;
//                    } else {
//                        newValue -= dv;
//                    }
//                }
//
//                newSettings.add(new DTimeSetting(settings[i].time, newValue));
//            }
//
//            settingsLiver.clear();
//            settingsLiver.addSettings(newSettings.toArray(new DTimeSetting[newSettings.size()]));
//            super.setActionLiver(settingsLiver.dayValuesPolyline()); //już nie super
//        }
//
//    }
