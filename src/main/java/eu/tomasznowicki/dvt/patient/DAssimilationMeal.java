/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.biocyb.DNutrition;


/**
 * To jest cecha pacjetna
 * 
 * @author tomek
 */
public class DAssimilationMeal {

    public static DDiscreteTimeFunction makeDiscreteTimeFunction(DMeal dMeal) {

        double[] carbValues = dMeal.carbDist.toArray(DNutrition.RESOLUTION);
        double[] fatValues = dMeal.fatDist.toArray(DNutrition.RESOLUTION);
        double[] protValues = dMeal.protDist.toArray(DNutrition.RESOLUTION);
        
        DDiscreteTimeFunction carbEvent = new DDiscreteTimeFunction(dMeal.time, carbValues);
        DDiscreteTimeFunction fatEvent = new DDiscreteTimeFunction(dMeal.time, fatValues);
        DDiscreteTimeFunction protEvent = new DDiscreteTimeFunction(dMeal.time, protValues);
        
        return DDiscreteTimeFunction.join(carbEvent, fatEvent, protEvent);
    }

}
