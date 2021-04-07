package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DInsulin;
import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;

/**
 * Jak wchłania się inuslina. To jest to co dzieje się w pacjecie.
 * To jest / będzie cechą pacjeta
 * 
 *
 * @author tomek
 */
public class DAbsorptionInsulin {

    public static DDiscreteTimeFunction makeDiscreteTimeFunction(DBolus dBolus) {

        //Tutaj po prostu korzystamy z dystrybucji teoretycznej
        if (dBolus.typeBolus != DBolus.DTypeBolus.STANDARD) {

            return null;

        } else {

            double[] values = dBolus.insulinDist.toArray(DInsulin.RESOLUTION);
            
            return new DDiscreteTimeFunction(dBolus.time, values);
        }

    }

}
