package eu.tomasznowicki.dvt.therapy;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.time.DClock;
import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.kit.DCGM;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DControlBox;
import eu.tomasznowicki.dvt.algorithm.DController;
import eu.tomasznowicki.dvt.raport.DLogger;
import eu.tomasznowicki.dvt.djson.*;
import eu.tomasznowicki.dvt.patient.DPatient;
import eu.tomasznowicki.dvt.raport.ToStrings;
import java.nio.file.Files;
import java.nio.file.Paths;

// "I never had to choose a subject - my subject rather chose me." ~EH
//
abstract public class DTherapy {

    public static final String //
            FILE_NAME_CONTROLS = "controls.json",
            FILE_NAME_MEALS = "meals.json",
            FILE_NAME_KIT = "kit.json",
            FILE_NAME_PATIENT = "patient.json";

    protected DClock clock = null;
    protected DPatient patient = null;
    protected DTherapyKit kit = null;
    protected DMealBox meals = null;
    protected DControlBox controls = null;
    protected DCGM cgm = null;
    protected DController controller = null;
    protected DLogger logger = null;

    public boolean ready() {
        return clock != null && patient != null && kit != null && meals != null
                && controls != null && cgm != null && controller != null && logger != null;
    }

    public boolean open(String path, DTimeStamp dTimeStamp, DController.DType dTypeController) {

        if (!Files.isDirectory(Paths.get(path))) {
            return false;
        }
        
        patient = JSONPatient.readFromFile(path + "/" + FILE_NAME_PATIENT);
        kit = JSONKit.readFromFile(path + "/" + FILE_NAME_KIT);
        meals = JSONMealBox.readFromFile(path + "/" + FILE_NAME_MEALS);
        controls = JSONControlTable.readFromFile(path + "/" + FILE_NAME_CONTROLS);
        cgm = new DCGM();        

        if (patient == null || kit == null || meals == null || controls == null || cgm == null) {
            return false;
        }
        
        logger = new DLogger(path);

        if (!logger.open()) {
            return false;
        }

        kit.setPatient(patient);
        meals.setPatient(patient);
        controls.setPatient(patient);
        cgm.setPatient(patient);

        controller = DController.get(dTypeController, kit, cgm, meals, controls); //Ważna kolejność, kontroler na końcu

        clock = new DClock(dTimeStamp);
        patient.setClock(clock);
        kit.setClock(clock);
        meals.setClock(clock);
        controls.setClock(clock);
        cgm.setClock(clock);
        controller.setClock(clock);

        patient.setLogger(logger.getLoggerPatient());
        patient.setLoggerInput(logger.getLoggerInput());
        patient.setLoggerOutput(logger.getLoggerOutput());
        kit.setLogger(logger.getLoggerTherapyKit());
        meals.setLogger(logger.getLoggerMealBox());
        cgm.setLogger(logger.getLoggerCGM());
        controller.setLogger(logger.getLoggerController());

        patient.log_yourself();
        kit.log_yourself();
        meals.log_yourself();
        cgm.log_yourself();
        controller.log_yourself();

        return true;
    }

    public boolean close() {

        if (ready()) {

            patient.log_yourself();
            kit.log_yourself();
            cgm.log_yourself();
            controller.log_yourself();
            meals.log_yourself();
            logger.close();

            clock = null;
            patient = null;
            kit = null;
            meals = null;
            controls = null;
            cgm = null;
            controller = null;
            logger = null;

            return true;

        } else {
            return false;
        }
    }

    abstract public boolean nextMinute();
    
    public DTimeStamp stepTime(){
        return clock.time();
    }
    
    public String infoString(){
        String info = ""; 
        info += ToStrings.BGtoString(patient.getBG());
        return info;
    }
}
