package eu.tomasznowicki.dvt.djson;

import eu.tomasznowicki.dvt.patient.DPatientCore;
import eu.tomasznowicki.dvt.patient.DPatient;
import eu.tomasznowicki.dvt.time.DTimeSetting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static eu.tomasznowicki.dvt.time.DTimeStamp.MINUTES_IN_HOUR;
import static eu.tomasznowicki.dvt.time.DTimeStamp.MINUTES_IN_DAY;


public class JSONPatient extends JSONio {
    
    public static enum DTypePatient {
        MINUTES_VALUES, DAY_SETTINGS, NON
    };
    
    private static final String //
            NAME = "Diabetes 1 patient",
            NAME_OF_TYPE = "Data type",
            NAME_OF_BLOOD_GLUCOSE = "Blood glucose level",
            NAME_OF_SENS_INSULIN = "Insulin sensetivity [mg/dl per 1j]",
            NAME_OF_SENS_W = "W sensetivity [mg/dl per 1W]",
            NAME_OF_ACTION_LIVER = "Liver activity [W per minute]",
            NAME_OF_ACTION_INTAKE = "Independent intake [?]",
            NAME_OF_ACTION_KIDNEYS = "Kidneys activity [ mg/dl per minute]";
    
    private static final String //
            SPLIT = ":",
            WRITE_FORMAT = "%d" + SPLIT + "%d" + SPLIT + "%.3f",
            READ_REGEX = "^([0-9]|1[0-9]|2[0-3])" + SPLIT + "([0-9]|[1-5][0-9])" + SPLIT + "[0-9]+.[0-9]{3}$";
    
    public static boolean writeToFile(String fileName, double bg,
            double[] sesitivityInsulin, double[] sensetivityW,
            double[] actionLiver, double[] actionIntake, double[] actionKidneys) {
        var jsonPatient = makeJSONObject(bg, sesitivityInsulin, sensetivityW, actionLiver, actionIntake, actionKidneys);
        return jsonPatient != null ? writeToFile(jsonPatient, fileName) : false;
    }
    
    public static boolean writeToFile(String fileName, double bg,
            DTimeSetting[] sensI, DTimeSetting[] sensW,
            DTimeSetting[] actionL, DTimeSetting[] actionI, DTimeSetting[] actionK) {
        var jsonPatient = makeJSONObject(bg, sensI, sensW, actionL, actionI, actionK);
        return jsonPatient != null ? writeToFile(jsonPatient, fileName) : false;
        
    }
    
    public static boolean writeToFileCore(String fileName, DPatient dPatient) {
        return writeToFileCore(fileName, (DPatientCore)dPatient);
    }
    
    
    public static boolean writeToFileCore(String fileName, DPatientCore dPatient) {
        return writeToFile(fileName, dPatient.getBG(),
                dPatient.getSensitivityInsulin(), dPatient.getSensitivityW(),
                dPatient.getActionLiver(), dPatient.getActionIntake(), dPatient.getActionKidneys());
    }
    
    public static boolean wirteToFile(String fileName, DPatient dPatient) {
        return writeToFile(fileName, dPatient.getBG(),
                dPatient.getSensitivityInsulinSettigns(), dPatient.getSensitivityWSettings(),
                dPatient.getActionLiverSettings(), dPatient.getActionIntakeSettings(), dPatient.getActionKidneysSettings());
    }
    
    public static DTypePatient readPatientType(String fileName) {
        JSONObject jsonPatient = readJSONObjectFromFile(fileName);
        if (jsonPatient == null) {
            return DTypePatient.NON;
        } else {
            var jsonType = (String) jsonPatient.get(NAME_OF_TYPE);
            if (jsonType == null) {
                return DTypePatient.NON;
            } else if (jsonType.equals(DTypePatient.DAY_SETTINGS.toString())) {
                return DTypePatient.DAY_SETTINGS;
            } else if (jsonType.equals(DTypePatient.MINUTES_VALUES.toString())) {
                return DTypePatient.MINUTES_VALUES;
            } else {
                return DTypePatient.NON;
            }
        }
    }
    
    public static DPatient readFromFile(String fileName) {
        JSONObject jsonPatient = readJSONObjectFromFile(fileName);
        return jsonPatient != null ? readJSONObject(jsonPatient) : null;
    }
    
    public static DPatientCore readFormFileForCore(String fileName) {
        JSONObject jsonPatient = readJSONObjectFromFile(fileName);
        return jsonPatient != null ? readJSONObjectForPatientCore(jsonPatient) : null;
    }
    
    private static JSONObject makeJSONObject(double bg, DTimeSetting[] sensI, DTimeSetting[] sensW,
            DTimeSetting[] actionL, DTimeSetting[] actionI, DTimeSetting[] actionK) {
        //
        if (correctBG(bg) && correctSettings(sensI, sensW, actionL, actionI, actionK)) {
            var jsonSensI = TimeSettingsArrayToJSONArray(sensI);
            var jsonSensW = TimeSettingsArrayToJSONArray(sensW);
            var jsonActionL = TimeSettingsArrayToJSONArray(actionL);
            var jsonActionI = TimeSettingsArrayToJSONArray(actionI);
            var jsonActionK = TimeSettingsArrayToJSONArray(actionK);
            var jsonPatient = new JSONObject();
            jsonPatient.put(NAME_OF_ATTENDEE, NAME);
            jsonPatient.put(NAME_OF_TYPE, DTypePatient.DAY_SETTINGS.toString());
            jsonPatient.put(NAME_OF_BLOOD_GLUCOSE, String.valueOf(bg));
            jsonPatient.put(NAME_OF_SENS_INSULIN, jsonSensI);
            jsonPatient.put(NAME_OF_SENS_W, jsonSensW);
            jsonPatient.put(NAME_OF_ACTION_LIVER, jsonActionL);
            jsonPatient.put(NAME_OF_ACTION_INTAKE, jsonActionI);
            jsonPatient.put(NAME_OF_ACTION_KIDNEYS, jsonActionK);
            return jsonPatient;
        } else {
            
            return null;
        }
    }
    
    private static JSONObject makeJSONObject(double bg, double[] sensI, double[] sensW,
            double[] actionL, double [] actionI, double[] actionK) {
        
        if (correctBG(bg) && correctSettings(sensI, sensW, actionL, actionI, actionK)) {
            var jsonSensI = new JSONArray();
            var jsonSensW = new JSONArray();
            var jsonActionL = new JSONArray();
            var jsonActionI = new JSONArray();
            var jsonActionK = new JSONArray();
            for (int i = 0; i < sensI.length; i++) {
                jsonSensI.add(sensI[i]);
                jsonSensW.add(sensW[i]);
                jsonActionL.add(actionL[i]);
                jsonActionI.add(actionI[i]);
                jsonActionK.add(actionK[i]);
            }
            var jsonPatient = new JSONObject();
            jsonPatient.put(NAME_OF_ATTENDEE, NAME);
            jsonPatient.put(NAME_OF_TYPE, DTypePatient.MINUTES_VALUES.toString());
            jsonPatient.put(NAME_OF_BLOOD_GLUCOSE, String.valueOf(bg));
            jsonPatient.put(NAME_OF_SENS_INSULIN, jsonSensI);
            jsonPatient.put(NAME_OF_SENS_W, jsonSensW);
            jsonPatient.put(NAME_OF_ACTION_LIVER, jsonActionL);
            jsonPatient.put(NAME_OF_ACTION_INTAKE, jsonActionI);
            jsonPatient.put(NAME_OF_ACTION_KIDNEYS, jsonActionK);
            return jsonPatient;
        } else {
            return null;
        }
    }
    
    private static DPatientCore readJSONObjectForPatientCore(JSONObject jsonPatient) {
        
        var jsonAttendee = (String) jsonPatient.get(NAME_OF_ATTENDEE);
        var jsonType = (String) jsonPatient.get(NAME_OF_TYPE);
        var jsonBg = (String) jsonPatient.get(NAME_OF_BLOOD_GLUCOSE);
        var jsonSensI = (JSONArray) jsonPatient.get(NAME_OF_SENS_INSULIN);
        var jsonSensW = (JSONArray) jsonPatient.get(NAME_OF_SENS_W);
        var jsonActionL = (JSONArray) jsonPatient.get(NAME_OF_ACTION_LIVER);
        var jsonActionI = (JSONArray) jsonPatient.get(NAME_OF_ACTION_INTAKE);
        var jsonActionK = (JSONArray) jsonPatient.get(NAME_OF_ACTION_KIDNEYS);
        
        DPatientCore dPatientCore = null;
        
        if (jsonAttendee != null
                && jsonAttendee.equals(NAME)
                && jsonType.equals(DTypePatient.MINUTES_VALUES.toString())
                && jsonBg != null
                && jsonSensI != null && jsonSensW != null
                && jsonActionL != null && jsonActionI != null && jsonActionK != null) {
            
            double bg = 0;
            
            try {
                bg = Double.parseDouble(jsonBg);
            } catch (NumberFormatException ex) {
            }
            
            double[] sensI = JSONArrayToDoubleArray(jsonSensI);
            double[] sensW = JSONArrayToDoubleArray(jsonSensW);
            double[] actionL = JSONArrayToDoubleArray(jsonActionL);
            double[] actionI = JSONArrayToDoubleArray(jsonActionI);
            double[] actionK = JSONArrayToDoubleArray(jsonActionK);
            
            if (correctBG(bg) && correctSettings(sensI, sensW, actionL, actionI, actionK)) {
                
                dPatientCore = new DPatientCore(bg);
                
                if (sensI.length == MINUTES_IN_DAY) { //1440
                    dPatientCore.setSensitivityInsulin(sensI);
                    dPatientCore.setSensitivityW(sensW);
                    dPatientCore.setActionLiver(actionL);
                    dPatientCore.setActionIntake(actionI);
                    dPatientCore.setActionKidneys(actionK);
                } else { // 24 -> 1440
                    double[] daySensI = new double[MINUTES_IN_DAY];
                    double[] daySensW = new double[MINUTES_IN_DAY];
                    double[] dayActionL = new double[MINUTES_IN_DAY];
                    double[] dayActionI = new double[MINUTES_IN_DAY];
                    double[] dayActionK = new double[MINUTES_IN_DAY];
                    int j = 0;
                    for (int i = 0; i < MINUTES_IN_DAY; i++) {
                        if (i != 0 && i % MINUTES_IN_HOUR == 0) { // co 60 min
                            j++;
                        }
                        daySensI[i] = sensI[j];
                        daySensW[i] = sensW[j];
                        dayActionL[i] = actionL[j] / (double) MINUTES_IN_HOUR;
                        dayActionI[i] = actionI[j] / (double) MINUTES_IN_HOUR;
                        dayActionK[i] = actionK[j] / (double) MINUTES_IN_HOUR;
                    }
                    dPatientCore.setSensitivityInsulin(daySensI);
                    dPatientCore.setSensitivityW(daySensW);
                    dPatientCore.setActionLiver(dayActionL);
                    dPatientCore.setActionIntake(dayActionI);
                    dPatientCore.setActionKidneys(dayActionK);
                }
            }
        }
        return dPatientCore;
    }
    
    static DPatient readJSONObject(JSONObject jsonPatient) {
        
        var jsonAttendee = (String) jsonPatient.get(NAME_OF_ATTENDEE);
        var jsonType = (String) jsonPatient.get(NAME_OF_TYPE);
        var jsonBg = (String) jsonPatient.get(NAME_OF_BLOOD_GLUCOSE);
        var jsonSensI = (JSONArray) jsonPatient.get(NAME_OF_SENS_INSULIN);
        var jsonSensW = (JSONArray) jsonPatient.get(NAME_OF_SENS_W);
        var jsonActionL = (JSONArray) jsonPatient.get(NAME_OF_ACTION_LIVER);
        var jsonActionI = (JSONArray) jsonPatient.get(NAME_OF_ACTION_INTAKE);
        var jsonActionK = (JSONArray) jsonPatient.get(NAME_OF_ACTION_KIDNEYS);
        
        DPatient dPatient = null;
        
        if (jsonAttendee != null
                && jsonAttendee.equals(NAME)
                && jsonType.equals(DTypePatient.DAY_SETTINGS.toString())
                && jsonBg != null
                && jsonSensI != null && jsonSensW != null
                && jsonActionL != null && jsonActionI != null && jsonActionK != null) {
            //
            double bg = 0;
            try {
                bg = Double.parseDouble(jsonBg);
            } catch (NumberFormatException ex) {
            }
            DTimeSetting[] sensI = JSONArrayToTimeSettingsArray(jsonSensI);
            DTimeSetting[] sensW = JSONArrayToTimeSettingsArray(jsonSensW);
            DTimeSetting[] actionL = JSONArrayToTimeSettingsArray(jsonActionL);
            DTimeSetting[] actionI = JSONArrayToTimeSettingsArray(jsonActionI);
            DTimeSetting[] actionK = JSONArrayToTimeSettingsArray(jsonActionK);
            
            if (correctBG(bg) && correctSettings(sensI, sensW, actionL, actionI, actionK)) {
                dPatient = new DPatient(bg);
                dPatient.setSensitivityInsulin(sensI);
                dPatient.setSensitivityW(sensW);
                dPatient.setActionLiver(actionL);
                dPatient.setActionIntake(actionI);
                dPatient.setActionKidneys(actionK);
            }
        }
        
        return dPatient;
    }
    
}
