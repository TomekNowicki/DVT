package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.biocyb.DGlycemia;
import eu.tomasznowicki.dvt.kit.DCGM;
import java.io.FileWriter;
import static eu.tomasznowicki.dvt.raport.ToStrings.*;

public class DLoggerCGM extends DLogger.DLoggerAttendee {

    public DLoggerCGM(FileWriter fileWriter) {
        super(fileWriter);
    }
    
    public boolean log_cgm(DCGM cgm){
        return log_insert(ENDL+ DStrings.CGM + SEP + cgmToString(cgm) + ENDL);
    }
    
    public boolean log_scan(DGlycemia glycemia, boolean alarm){
        var log =  glycemiaToString(glycemia) + " " + (alarm ? DStrings.CGM_ALARM : ""); 
        return log_insert(log + ENDL);
    }
}
