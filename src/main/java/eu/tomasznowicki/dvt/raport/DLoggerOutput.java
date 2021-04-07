package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;
import java.io.FileWriter;

public class DLoggerOutput extends DLogger.DLoggerAttendee {

    public static int INTERVAL = 5; //Co ile minut zapis
    private int counter = -1; //Ma zrobić wpis dla startu symulacji

    public DLoggerOutput(FileWriter fileWriter) {
        super(fileWriter);
    }

    public boolean log_patient_output(DTimeStamp time, double BG) {
        if (++counter == INTERVAL) {
            counter = 0;
        }
        if (counter == 0) {
            var log = timeStampToString(time) + SEP + String.format(LOC, FORMAT_BG, BG);
            return log_insert(log + ENDL);
        } else {
            return true;
        }
    }
    
}
