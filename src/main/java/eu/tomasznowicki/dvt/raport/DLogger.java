package eu.tomasznowicki.dvt.raport;

import static eu.tomasznowicki.dvt.raport.ToStrings.ARROW;
import static eu.tomasznowicki.dvt.raport.ToStrings.ENDL;
import static eu.tomasznowicki.dvt.raport.ToStrings.timeStampToString;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DLogger {

    private static final String //
            LOG_DIR_NAME = "simulation_logs",
            LOG_DIR_EXT_FORMAT = "_yyyy-MM-dd_HH-mm-ss";

    private static final String //
            EXT = ".txt",
            LOG_FILE_NAME_PATIENT = "log_patient" + EXT,
            LOG_FILE_NAME_KIT = "log_kit" + EXT,
            LOG_FILE_NAME_MEALBOX = "log_mealbox" + EXT,
            LOG_FILE_NAME_CGM = "log_cgm" + EXT,
            LOG_FILE_NAME_CONTROLLER = "log_controller" + EXT,
            LOG_FILE_NAME_INPUT = "log_input" + EXT,
            LOG_FILE_NAME_OUTPUT = "log_output" + EXT;

    public static boolean ALWAYS_TIME_STAMP = true;

    private final Path pathSimulation; //Jeżeli jest null, to nie będzie logowania
    private Path pathLoggs = null;

    private DLoggerInput dLoggerInput = null;
    private DLoggerOutput dLoggerOutput = null;

    private DLoggerPatient dLoggerPatient = null;
    private DLoggerKit dLoggerTherapyKit = null;
    private DLoggerMealBox dLoggerMealBox = null;
    private DLoggerCGM dLoggerCGM = null;
    private DLoggerController dLoggerController = null;

    static class DLoggerAttendee {

        protected final FileWriter writer;

        DLoggerAttendee(FileWriter fileWriter) {
            writer = fileWriter;
        }

        protected final boolean log_insert(String log) {
            try {
                writer.write(log);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }

        public boolean log_info(String info) {
            return log_insert(info + ENDL);
        }

        public boolean log_function(DTimeStamp time, String name) {
            var log = ENDL + timeStampToString(time) + ARROW + name + ENDL;
            return log_insert(log);
        }

        protected boolean before_close() {
            return true;
        }

        protected final boolean close() {
            try {
                var success = before_close();
                writer.flush();
                writer.close();
                return true && success;
            } catch (IOException ex) {
                return false;
            }
        }

        public boolean log_failed() {
            return log_insert(ToStrings.FAILED + ToStrings.ENDL);
        }
    } //class DLoggerAttendee

    //
    private Path getPath(String pathToSimulationFolder) {
        try {
            return Paths.get(pathToSimulationFolder);
        } catch (InvalidPathException ex) {
            return null;
        }
    }

    public DLogger(String pathToSimulationFolder) {

        pathSimulation = getPath(pathToSimulationFolder);
        //Jeżeli jest null to nic nie rób

    }

    public boolean open() {

        if (pathSimulation != null) {
            Path pathProbe;
            do {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(LOG_DIR_EXT_FORMAT);
                var logDirExt = LOG_DIR_NAME + dtf.format(now);
                pathProbe = Paths.get(pathSimulation.toString(), logDirExt);
            } while (Files.exists(pathProbe) && Files.isDirectory(pathProbe));
            if ((new File(pathProbe.toString())).mkdir()) {
                pathLoggs = pathProbe;
                return true;
            } else {
                pathLoggs = null;
                return false;
            }
        } else {
            return false;
        }
    }

    private FileWriter getFileWriter(Path path) {

        try {
            return new FileWriter(path.toString());
        } catch (IOException ex) {
            return null;
        }
    }

    public DLoggerInput getLoggerInput() {
        if (pathLoggs != null && dLoggerInput == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_INPUT);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerInput = new DLoggerInput(fileWriter);
            }
        }
        return dLoggerInput;
    }

    public DLoggerOutput getLoggerOutput() {
        if (pathLoggs != null && dLoggerOutput == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_OUTPUT);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerOutput = new DLoggerOutput(fileWriter);
            }
        }
        return dLoggerOutput;
    }

    public DLoggerPatient getLoggerPatient() {
        if (pathLoggs != null && dLoggerPatient == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_PATIENT);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerPatient = new DLoggerPatient(fileWriter);
            }
        }
        return dLoggerPatient;
    }

    public DLoggerKit getLoggerTherapyKit() {
        if (pathLoggs != null && dLoggerTherapyKit == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_KIT);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerTherapyKit = new DLoggerKit(fileWriter);
            }
        }
        return dLoggerTherapyKit;
    }

    public DLoggerMealBox getLoggerMealBox() {
        if (pathLoggs != null && dLoggerMealBox == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_MEALBOX);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerMealBox = new DLoggerMealBox(fileWriter);
            }
        }
        return dLoggerMealBox;
    }

    public DLoggerCGM getLoggerCGM() {
        if (pathLoggs != null && dLoggerCGM == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_CGM);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerCGM = new DLoggerCGM(fileWriter);
            }
        }
        return dLoggerCGM;
    }

    public DLoggerController getLoggerController() {
        if (pathLoggs != null && dLoggerController == null) {
            var path = Paths.get(pathLoggs.toString(), LOG_FILE_NAME_CONTROLLER);
            var fileWriter = getFileWriter(path);
            if (fileWriter != null) {
                dLoggerController = new DLoggerController(fileWriter);
            }
        }
        return dLoggerController;
    }

    public boolean close() {
        boolean success = true;
        if (dLoggerPatient != null) {
            success = success && dLoggerPatient.close();
        }
        if (dLoggerTherapyKit != null) {
            success = success && dLoggerTherapyKit.close();
        }
        if (dLoggerMealBox != null) {
            success = success && dLoggerMealBox.close();
        }
        if (dLoggerCGM != null) {
            success = success && dLoggerCGM.close();
        }
        if (dLoggerController != null) {
            success = success && dLoggerController.close();
        }
        if (dLoggerInput != null) {
            success = success && dLoggerInput.close();
        }
        if (dLoggerOutput != null) {
            success = success && dLoggerOutput.close();
        }
        return success;
    }

}
