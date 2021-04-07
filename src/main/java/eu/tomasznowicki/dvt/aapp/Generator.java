package eu.tomasznowicki.dvt.aapp;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.time.DTimeSetting;



import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.biocyb.DInsulin;
import eu.tomasznowicki.dvt.biocyb.DNutrition;
import eu.tomasznowicki.dvt.therapy.DTherapy;
import eu.tomasznowicki.dvt.djson.*;
import eu.tomasznowicki.dvt.patient.DPatient;


public class Generator {

    private final String controlsDest, mealsDest, kitDest, patientDest;

    Generator(String path) {
        controlsDest = path + "/" + DTherapy.FILE_NAME_CONTROLS;
        mealsDest = path + "/" + DTherapy.FILE_NAME_MEALS;
        kitDest = path + "/" + DTherapy.FILE_NAME_KIT;
        patientDest = path + "/" + DTherapy.FILE_NAME_PATIENT;
    }

        
    void generate() {
        
        //Plan kontroli
        JSONControlTable.writeToFile(controlsDest,
                new DTimeStamp(0, 1, 0),
                new DTimeStamp(0, 4, 0),
                //new DTimeStamp(0, 8, 0),
                new DTimeStamp(0, 9, 30),
                //new DTimeStamp(0, 11, 0),
                new DTimeStamp(0, 12, 30),
                //new DTimeStamp(0, 14, 0),
                new DTimeStamp(0, 15, 30),
                //new DTimeStamp(0, 17, 0),
                new DTimeStamp(0, 18, 30),
                //new DTimeStamp(0, 20, 0),
                new DTimeStamp(0, 22, 0));

        //Plan posiłków
        JSONMealBox.writeToFile(mealsDest, 15,
                DNutrition.getSimpleMeal(new DTimeStamp(0, 8, 0), 4),
                DNutrition.getSimpleMeal(new DTimeStamp(0, 11, 0), 3),
                DNutrition.getSimpleMeal(new DTimeStamp(0, 14, 0), 3.5),
                DNutrition.getSimpleMeal(new DTimeStamp(0, 17, 0), 2),
                DNutrition.getSimpleMeal(new DTimeStamp(0, 20, 0), 3));

        //Kit
        DTherapyKit kit = DTherapyKit.getPump(DInsulin.DTypeInsulin.NOVORAPID);
        kit.setConfiguratinBase(new DTimeSetting(new DTimeStamp(0, 0, 0), 0.6));
        kit.setConfigurationBolus(new DTimeSetting(new DTimeStamp(0, 0, 0), 1));
        kit.setConfigurationSensitivity(new DTimeSetting(new DTimeStamp(0, 0, 0), 100));
        JSONKit.writeToFile(kitDest, kit);


        DPatient patient = new DPatient(100);
        
        patient.setSensitivityInsulin(new DTimeSetting(new DTimeStamp(0, 0, 0), 100));
        patient.setSensitivityW(new DTimeSetting(new DTimeStamp(0, 0, 0), 100));
        patient.setActionLiver( new DTimeSetting(new DTimeStamp(0, 0, 0), 0.6) );
        patient.setActionIntake(new DTimeSetting(new DTimeStamp(0,0,0),0.3));
        patient.setActionKidneys(new DTimeSetting(new DTimeStamp(0, 0, 0), 6));  
        
//        patient.setSensitivityInsulin(
//                new DTimeSetting(new DTimeStamp(0,  0, 0), 120),
//                new DTimeSetting(new DTimeStamp(0,  4, 0), 120),
//                new DTimeSetting(new DTimeStamp(0,  6, 0), 100),
//                new DTimeSetting(new DTimeStamp(0, 20, 0), 100),
//                new DTimeSetting(new DTimeStamp(0, 22, 0), 120));
//        
//        patient.setSensitivityW(
//                new DTimeSetting(new DTimeStamp(0,  0, 0), 100),
//                new DTimeSetting(new DTimeStamp(0, 12, 0), 100),
//                new DTimeSetting(new DTimeStamp(0, 13, 0),  90),
//                new DTimeSetting(new DTimeStamp(0, 15, 0),  90),
//                new DTimeSetting(new DTimeStamp(0, 17, 0), 100));
//        
//        patient.setActionLiver(
//                new DTimeSetting(new DTimeStamp(0,  0, 0), 1  ),
//                new DTimeSetting(new DTimeStamp(0,  4, 0), 1  ),
//                new DTimeSetting(new DTimeStamp(0,  6, 0), 1.2),
//                new DTimeSetting(new DTimeStamp(0,  9, 0), 1.2),
//                new DTimeSetting(new DTimeStamp(0, 11, 0), 1 ));
//        !Tutaj jeszcze intake
//        
//        patient.setActionKidneys(
//                new DTimeSetting(new DTimeStamp(0, 0, 0), 10)); 
        
        
        JSONPatient.wirteToFile(patientDest, patient);          
                  
    }
    
    void readGenerated(){
        System.out.println("\nControls\n" + JSONControlTable.readFromFile(controlsDest));
        System.out.println("\nMeals\n" + JSONMealBox.readFromFile(mealsDest));
        System.out.println("\nKit\n" + JSONKit.readFromFile(kitDest));
        System.out.println("\nPatient\n" + JSONPatient.readFromFile(patientDest));
    }

    public static void main(String[] args) {
        System.out.print("Generating in ");
        System.out.println(Simulator.PATH);
        Generator gen = new Generator(Simulator.PATH);
        gen.generate();
        gen.readGenerated();
    }
    
}


/*
        //Pacjent
        DPatientCore patient = new DPatientCore(100);
        double[] settings = new double[1440];
        Arrays.fill(settings, 0, DTimeStamp.MINUTES_IN_DAY, 100);
        patient.setSensitivityInsulin(settings);
        patient.setSensitivityW(settings);
        Arrays.fill(settings, 0, DTimeStamp.MINUTES_IN_DAY, 0.3 / 60.0);
        patient.setActionLiver(settings);
        Arrays.fill(settings, 0, DTimeStamp.MINUTES_IN_DAY, 10.0 / 60.0);
        patient.setActionKidneys(settings);
        JSONPatient.writeToFile(patientDest, patient);
*/