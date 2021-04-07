package eu.tomasznowicki.dvt.raport;

import eu.tomasznowicki.dvt.algorithm.DController;
import java.util.Locale;
import java.util.ArrayList;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.time.DClock;
import eu.tomasznowicki.dvt.time.DTimeSetting;
import eu.tomasznowicki.dvt.time.DDaySettings;
import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;
import eu.tomasznowicki.dvt.biocyb.DGlycemia;
import eu.tomasznowicki.dvt.biocyb.DDistribution;
import eu.tomasznowicki.dvt.biocyb.DDistributionBeta;
import eu.tomasznowicki.dvt.biocyb.DDistributionPolynomial;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.biocyb.DBolus;
import static eu.tomasznowicki.dvt.biocyb.DBolus.DTypeBolus.*;
import static eu.tomasznowicki.dvt.biocyb.DInsulin.DFunctionInsulin.*;
import eu.tomasznowicki.dvt.patient.DAction;
import eu.tomasznowicki.dvt.patient.DDelivery;
import eu.tomasznowicki.dvt.patient.DMetabolism;
import eu.tomasznowicki.dvt.patient.DPatientCore;
import eu.tomasznowicki.dvt.kit.DControlBox;
import eu.tomasznowicki.dvt.kit.DMealBox;
import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.algorithm.DNormalization;
import eu.tomasznowicki.dvt.algorithm.DTherapyAdvice;
import eu.tomasznowicki.dvt.kit.DCGM;
import static eu.tomasznowicki.dvt.raport.DStrings.AHEAD;

public class ToStrings {
    
    public static final String //
            FORMAT_BG = "%.1f",
            FORMAT_DBG = "%.3f",
            FORMAT_W = "%.3f",
            FORMAT_I = "%.5f",
            FORMAT_SENS = "%.0f",
            FORMAT_TIME = "%02d@%02d:%02d",
            FORMAT_TIME_SETTING = "%5.3f",
            FORMAT_DISCRETE_FUNCTION = "%.5f",
            FORMAT_DISTRIBUTION = "%.3f",
            FORMAT_DISTRIBUTION_BETA = "%.3f*x^(%.3f-1)*(1-x)^(%.3f-1)",
            FORMAT_DOUBLE_DAY_INDEX = "%4d-%4d",
            FORMAT_DOUBLE_DAY_VALUE = "%.3f",
            FORMAT_NORMALIZATION = "%.5f",
            FORMAT_MEAL_WBT = "ww=%.3f wt=%.3f wb=%.3f index=%d",
            FORMAT_MEAL_GRAMS = "carb=%.3fg fat=%.3fg prot=%.3g",
            FORMAT_MEAL_CAL = "carb=%.3fkcal fat=%.3fkcal prot=%.3fkcal",
            FORMAT_BOLUS = "%.3fj";
    
    public static final Locale LOC = Locale.US;
    
    public static final String //
            NULL_VALUE = "!NULL VALUE",
            FAILED = "!FAILED",
            ERROR = "!ERROR",
            EMPTY = "Empty",
            OK = "OK";
    
    public static final String //
            TOTAL = "total",
            COUNT = "count",
            SPAN = "span",
            CLOCK = "CLK";
    
    public static final String //
            ENDL = "\n",
            ENDL2 = "\n\n",
            SEP = " ",
            EQ = "=",
            PLUS = "+",
            ARROW = "->",
            HYPHEN = "-",
            STRING_EMPTY = "";
    
    public static int COLUMNS = 15;
    
    public static String BGtoString(double BG) {
        return DStrings.BLOOD_GLUCOSE + EQ + String.format(LOC, FORMAT_BG, BG);
    }
    
    public static String dBGtoString(double BG) {
        return DStrings.BLOOD_GLUCOSE + EQ + String.format(LOC, FORMAT_DBG, BG);
    }
    
    public static String wToString(double W) {
        return DStrings.W + EQ + String.format(LOC, FORMAT_W, W);
    }
    
    public static String insulinToString(double insulin) {
        return DStrings.INSULIN + EQ + String.format(LOC, FORMAT_I, insulin);
    }
    
    public static String sensitivityToString(String name, double sensitivity) {
        return name + EQ + String.format(LOC, FORMAT_SENS, sensitivity);
    }
    
    public static String timeStampToString(DTimeStamp time) {
        var string = NULL_VALUE;
        if (time != null) {
            string = String.format(LOC, FORMAT_TIME, time.day, time.hour, time.minute);
        }
        return string;
    }
    
    public static String clockToString(DClock clock) {
        var string = NULL_VALUE;
        if (clock != null) {
            string += CLOCK + timeStampToString(clock.time()) + ARROW + clock.numberOfListeners();
        }
        return string;
    }
    
    public static String timeSettingToString(DTimeSetting setting) {
        var string = NULL_VALUE;
        if (setting != null) {
            string = timeStampToString(setting.time) + ARROW
                    + String.format(LOC, FORMAT_TIME_SETTING, setting.value);
        }
        return string;
    }
    
    public static String daySettingsToString(DDaySettings settings) {
        var string = NULL_VALUE;
        
        if (settings != null) {
            string = STRING_EMPTY;
            for (var setting : settings.getSettings()) {
                string += timeSettingToString(setting) + ENDL;
            }
            string = string.equals(STRING_EMPTY) ? EMPTY : string.substring(0, string.length() - ENDL.length());
        }
        return string;
    }
    
    public static String discreteTimeFunctionToString(DDiscreteTimeFunction function) {
        return discreteTimeFunctionToString(function, false);
    }
    
    public static String discreteTimeFunctionToString(DDiscreteTimeFunction function, boolean detailed) {
        var string = NULL_VALUE;
        if (function != null) {
            string = timeStampToString(function.openTime) + ARROW
                    + timeStampToString(function.closeTime) + SEP
                    + COUNT + EQ + function.values.length + SEP
                    + TOTAL + EQ + String.format(LOC, FORMAT_DISCRETE_FUNCTION, function.sum);
            
            if (detailed) {
                string += ENDL;
                int c = 0;
                for (double v : function.values) {
                    c++;
                    string += String.format(LOC, FORMAT_DISCRETE_FUNCTION, v);
                    if (c % COLUMNS == 0) {
                        string += ENDL;
                    } else {
                        string += SEP;
                    }
                }
            }
        }
        return string;
    }
    
    public static String glycemiaToString(DGlycemia glycemia) {
        var string = NULL_VALUE;
        if (glycemia != null) {
            string = timeStampToString(glycemia.time) + SEP + BGtoString(glycemia.value)
                    + " rateShort=" + glycemia.rateShort + " rateLong=" + glycemia.rateLong
                    + " averageShort=" + glycemia.averageShort + " averageLong=" + glycemia.averageLong;
                    
                    
                    
                   // + (glycemia.trend != DGlycemia.DTrend.UNKNOWN ? SEP + glycemia.trend.toString() : "");
        }
        return string;
    }
    
    private static String distributionToString(DDistribution dist) {
        var string = NULL_VALUE;
        if (dist != null) {
            string += TOTAL + EQ + dist.total + SEP
                    + SPAN + EQ + dist.span + SEP
                    + TOTAL + "/" + SPAN + dist.h + SEP;
            
            if (!dist.error()) {
                string += OK;
            } else {
                string += ERROR + SEP + dist.info();
            }
        }
        return string;
    }
    
    public static String distributionBetaToString(DDistributionBeta dist) {
        var string = NULL_VALUE;
        if (dist != null) {
            
            string += DStrings.DISTRIBUTION_BETA + ENDL
                    + String.format(LOC, FORMAT_DISTRIBUTION_BETA, dist.c, dist.a, dist.b) + ENDL
                    + distributionToString(dist);
        }
        return string;
    }
    
    public static String distributionPolynomialToString(DDistributionPolynomial dist) {
        var string = NULL_VALUE;
        if (dist != null) {
            string += DStrings.DISTRIBUTION_POLYNOMIAL + ENDL;
            for (int i = 0; i < dist.F.length; i++) {
                if (i > 0) {
                    string += SEP;
                }
                if (dist.F[i] > 0) {
                    string += PLUS;
                }
                string += String.format(LOC, FORMAT_DISTRIBUTION, dist.F[i]);
                if (i > 0) {
                    string += "*x";
                }
                if (i > 1) {
                    string += "^" + i;
                }
            }
            string += distributionToString(dist);
        }
        return string;
    }
    
    public static String mealToString(DMeal meal) {
        return mealToString(meal, false);
    }
    
    public static String mealToString(DMeal meal, boolean detailed) {
        var string = NULL_VALUE;
        if (meal != null) {
            string = timeStampToString(meal.time) + SEP
                    + String.format(LOC, FORMAT_MEAL_WBT, meal.ww, meal.wt, meal.wb, meal.glycemnicIndex);
            if (detailed) {
                string += String.format(LOC, FORMAT_MEAL_GRAMS, meal.carbGrams, meal.fatGrams, meal.protGrams) + SEP
                        + String.format(LOC, FORMAT_MEAL_CAL, meal.carbKcal, meal.fatKcal, meal.protKcal) + ENDL
                        + DStrings.CARBOHYDRATE + ARROW + meal.carbDist.toString() + ENDL
                        + DStrings.FAT + ARROW + meal.fatDist.toString() + ENDL
                        + DStrings.PROTEIN + ARROW + meal.protDist.toString();
            }
        }
        return string;
    }
    
    public static String bolusToString(DBolus bolus) {
        return bolusToString(bolus, false);
    }
    
    public static String bolusToString(DBolus bolus, boolean detailed) {
        var string = NULL_VALUE;
        if (bolus != null) {
            string = timeStampToString(bolus.time) + SEP
                    + String.format(Locale.US, FORMAT_BOLUS, bolus.dose) + SEP
                    + bolus.typeInsulin + SEP + bolus.typeBolus;
            if (bolus.typeBolus == STANDARD && detailed) {
                string += ENDL + bolus.insulinDist.toString();
            }
            if (bolus.typeBolus == DUAL_WAVE || bolus.typeBolus == SQUARE_WAVE) {
                string += SEP + SPAN + EQ + bolus.waveSpan + SEP + DStrings.BOLUS_STANDARD_PART + EQ + bolus.standardPart;
                
                for (DBolus b : bolus.series) {
                    string += ENDL + bolusToString(b);
                }
            }
        }
        return string;
    }
    
    public static String daySettingsToString(double[] table, String valueFormat) {
        //Bez powtarzania wartości
        String string = STRING_EMPTY;
        if (table == null || table.length != DTimeStamp.MINUTES_IN_DAY) {
            string = ERROR;
        } else {
            
            int j = 0, i = 1;
            var value = table[j];
            
            while (i < table.length) { //1440
                if (value != table[i++] || i == table.length - 1) {
                    string += String.format(LOC, FORMAT_DOUBLE_DAY_INDEX, j, i) + SEP
                            + timeStampToString(new DTimeStamp(j)) + HYPHEN
                            + timeStampToString(new DTimeStamp(i)) + SEP + ARROW + SEP
                            + String.format(LOC, valueFormat, value) + ENDL;
                    j = i;
                    value = table[j];
                }
            }
            
            string = string.substring(0, string.length() - ENDL.length());
        }
        
        return string;
    }
    
    public static String actionToString(DAction action, String valueFormat) {
        var string = NULL_VALUE;
        if (action != null) {
            string = action.name + ENDL + daySettingsToString(action.getAction(), valueFormat);
        }
        return string;
    }
    
    public static String deliveryToString(DDelivery delivery) {
        var string = NULL_VALUE;
        
        if (delivery != null) {
            string = delivery.name;
            
            if (delivery.size() == 0) {
                string += ENDL + EMPTY;
            } else {
                for (DDiscreteTimeFunction function : delivery.getFuntions()) {
                    string += ENDL + discreteTimeFunctionToString(function);
                }
            }
        }
        return string;
    }
    
    public static String metabolismToString(DMetabolism metabolism) {
        var string = NULL_VALUE;
        if (metabolism != null) {
            
            string = metabolism.name + ENDL
                    + DStrings.SENSITIVITY_INSULIN + ENDL
                    + daySettingsToString(metabolism.getSensitivityInsulin(), FORMAT_SENS) + ENDL
                    + DStrings.SENSITIVITY_W + ENDL
                    + daySettingsToString(metabolism.getSensitivityW(), FORMAT_SENS);
        }
        return string;
    }
    
    public static String patientToString(DPatientCore patient) {
        var string = NULL_VALUE;
        if (patient != null) {
            string = patient.toString();
        }
        return string;
    }
    
    public static String controlTableToString(DControlBox controlTable) {
        var string = NULL_VALUE;
        if (controlTable != null) {
            string = STRING_EMPTY;
            for (var timeStamp : controlTable.getControls()) {
                string += timeStampToString(timeStamp) + ENDL;
            }
            string = string.equals(STRING_EMPTY) ? EMPTY : string.substring(0, string.length() - ENDL.length());
        }
        return string;
    }
    
    public static String arrayMealsToString(DMeal[] meals) {
        String string = STRING_EMPTY;        
        for (var meal : meals) {
            string += mealToString(meal) + ENDL;
        }
        
        if(string.equals(STRING_EMPTY)) string = EMPTY + ENDL;
        
        
        return string;
    }
    
    public static String mealBoxToString(DMealBox mealbox) {
        var string = NULL_VALUE;
        
        if (mealbox != null) {
            string = AHEAD + EQ + mealbox.getAhead() + ENDL
                    + "Bill of fare:" + ENDL + arrayMealsToString(mealbox.getMeals());
            DMeal extra = mealbox.getExtraMeal();
            string += "Extra dish :" + ENDL + (extra != null ? mealToString(extra) : EMPTY ) + ENDL;             
            string += "Plan regular :" + ENDL + arrayMealsToString(mealbox.getPlanRegular())
                    + "Plan correction :" + ENDL + arrayMealsToString(mealbox.getPlanCorrection())
                    + "History regular :" + ENDL + arrayMealsToString(mealbox.getHistoryRegular())
                    + "History correction :" + ENDL + arrayMealsToString(mealbox.getHistoryCorrection());
            
            string = string.substring(0, string.length() - ENDL.length());
        }
        
        
        
        return string;
    }
    
    public static String arrayListBolusToString(ArrayList<DBolus> plan) {
        var string = STRING_EMPTY;
        for (DBolus bolus : plan) {
            string += bolusToString(bolus) + ENDL;
        }
        return string.equals(STRING_EMPTY) ? EMPTY : string.substring(0, string.length() - ENDL.length());
    }
    
    public static String therapyKitToString(DTherapyKit kit) {
        var string = NULL_VALUE;
        if (kit != null) {
            string = kit.toString();
        }
        return string;
    }
    
    public static String kitNormalizationToString(DNormalization norm) {
        var string = NULL_VALUE;
        if (norm != null) {
            string = String.format(LOC, FORMAT_NORMALIZATION, norm.raw)
                    + EQ + String.format(LOC, FORMAT_NORMALIZATION, norm.value)
                    + PLUS + String.format(LOC, FORMAT_NORMALIZATION, norm.rest)
                    + ARROW + String.format(LOC, FORMAT_NORMALIZATION, norm.recomendation);
        }
        return string;
    }
    
    public static String therapyAdviceToString(DTherapyAdvice advice) {
        var string = NULL_VALUE;
        if (advice != null) {
            //Znacznik czasu i horyzont
            string = timeStampToString(advice.time) + SEP + DStrings.INSULIN_HORIZONT + EQ
                    + advice.horizont;
                    //+ (advice.horizont != null ? advice.horizont : NULL_VALUE) + ENDL;
            //
            string += DStrings.W_PLANNED + SEP + (advice.planedW != null ? wToString(advice.planedW) : NULL_VALUE) + ENDL;
            //Glikemia
            string += DStrings.BLOOD_GLUCOSE + EQ
                    + String.format(LOC, FORMAT_BG, advice.BG)
                    //+ (advice.BG != null ? String.format(LOC, FORMAT_BG, advice.BG) : NULL_VALUE) + SEP
                    + DStrings.BLOOD_GLUCOSE_TO_CORRECT + EQ
                    + (advice.corrBG != null ? String.format(LOC, FORMAT_BG, advice.corrBG) : NULL_VALUE) + SEP
                    + DStrings.BLOOD_GLUCOSE_RATE + EQ
                    + (advice.shortBG != null ? String.format(LOC, FORMAT_DBG, advice.shortBG) : NULL_VALUE) + SEP
                    + (advice.longBG != null ? String.format(LOC, FORMAT_DBG, advice.longBG) : NULL_VALUE) + ENDL;
            //Wrażliwości
            string += (advice.sensI != null ? sensitivityToString(DStrings.SENSITIVITY_INSULIN, advice.sensI)
                    : DStrings.SENSITIVITY_INSULIN + EQ + NULL_VALUE) + SEP;
            string += (advice.sensW != null ? sensitivityToString(DStrings.SENSITIVITY_W, advice.sensW)
                    : DStrings.SENSITIVITY_W + EQ + NULL_VALUE) + ENDL;
            //Aktywna insulina
            string += DStrings.INSULIN_ACTIVE + SEP
                    + BASE + EQ
                    + (advice.futureInsulinBase != null ? insulinToString(advice.futureInsulinBase) : NULL_VALUE) + SEP
                    + REGULAR + EQ
                    + (advice.futureInsulinRegular != null ? insulinToString(advice.futureInsulinRegular) : NULL_VALUE) + SEP
                    + DStrings.W_CORRECTION + EQ
                    + (advice.futureInsulinCorrection != null ? insulinToString(advice.futureInsulinCorrection) : NULL_VALUE) + ENDL;
            //Aktywne wymienniki
            string += DStrings.W_ACTIVE + SEP
                    + DStrings.PATIENT_LIVER + EQ
                    + (advice.futureWLiver != null ? wToString(advice.futureWLiver) : NULL_VALUE) + SEP
                    + DStrings.PATIENT_DELIVERY_MEALS_REGULAR + EQ
                    + (advice.futureWRegular != null ? wToString(advice.futureWRegular) : NULL_VALUE) + SEP
                    + DStrings.PATIENT_DELIVERY_MEALS_CORRECTION + EQ
                    + (advice.futureInsulinCorrection != null ? wToString(advice.futureInsulinCorrection) : NULL_VALUE) + ENDL;
            //Aktywność nerek
            string += DStrings.PATIENT_KIDNEYS + SEP
                    + (advice.futureBGKidneys != null ? BGtoString(advice.futureBGKidneys) : NULL_VALUE) + ENDL;
            //Wyliczenia
            string += DStrings.INSULIN_FOR_PLANNED_W + SEP
                    + (advice.insulinForPlannedW != null ? insulinToString(advice.insulinForPlannedW) : NULL_VALUE) + SEP
                    + DStrings.INSULIN_FOR_CORRECTION + SEP
                    + (advice.insulinForCorrection != null ? insulinToString(advice.insulinForCorrection) : NULL_VALUE) + SEP
                    + DStrings.W_CORRECTION + SEP
                    + (advice.wForCorrection != null ? wToString(advice.wForCorrection) : NULL_VALUE);
            //Jezeli jest info
            if (advice.info != null) {
                string += ENDL + advice.info;
            }
        }
        return string;
    }
    
    public static String controllerToString(DController controller) {
        var string = NULL_VALUE;
        if (controller != null) {
            string = DStrings.CONTROLLER_TYPE + SEP + controller.type.toString();
        }
        return string;
    }
    
    public static String cgmToString(DCGM cgm) {
        var string = NULL_VALUE;
        if (cgm != null) {
            string = cgm.toString();
        }
        return string;
    }
    
}
