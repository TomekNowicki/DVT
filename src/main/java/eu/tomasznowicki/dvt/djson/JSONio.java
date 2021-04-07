package eu.tomasznowicki.dvt.djson;

import eu.tomasznowicki.dvt.patient.DPatientCore;
import eu.tomasznowicki.dvt.time.DTimeSetting;
import eu.tomasznowicki.dvt.time.DTimeStamp;
import static eu.tomasznowicki.dvt.time.DTimeStamp.HOURS_IN_DAY;
import static eu.tomasznowicki.dvt.time.DTimeStamp.MINUTES_IN_DAY;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class JSONio {

    public static final String NAME_OF_ATTENDEE = "Therapy attendee";

    private static final String // Dotyczy Time Settings
            SPLIT = ":",
            WRITE_FORMAT = "%d" + SPLIT + "%d" + SPLIT + "%.3f",
            READ_REGEX = "^([0-9]|1[0-9]|2[0-3])" + SPLIT + "([0-9]|[1-5][0-9])" + SPLIT + "[0-9]+.[0-9]{3}$";

    protected static boolean writeToFile(JSONObject jsonObject, String fileName) {
        try (var jsonFile = new FileWriter(fileName)) {
            jsonFile.write(jsonObject.toJSONString());
            jsonFile.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    protected static JSONObject readJSONObjectFromFile(String fileName) {
        try (var jsonFile = new FileReader(fileName)) {
            return (JSONObject) (new JSONParser()).parse(jsonFile);
        } catch (IOException | ParseException ex) {
            return null;
        }
    }

    protected static JSONArray TimeSettingsArrayToJSONArray(DTimeSetting[] dArray) {
        if (dArray != null) {
            JSONArray jArray = new JSONArray();
            for (var a : dArray) {
                jArray.add(String.format(Locale.US, WRITE_FORMAT, a.time.hour, a.time.minute, a.value));
            }
            return jArray;
        }
        return null;
    }

    protected static DTimeSetting[] JSONArrayToTimeSettingsArray(JSONArray jArray) {
        if (jArray != null && !jArray.isEmpty()) {
            SortedSet<DTimeSetting> dTimeSetting = new TreeSet<>();
            Pattern pattern = Pattern.compile(READ_REGEX);
            String s0;
            String[] s1;
            int h;
            int m;
            double value;
            Iterator iterator = jArray.iterator();
            while (iterator.hasNext()) {
                s0 = (String) iterator.next();
                if (pattern.matcher(s0).matches()) {
                    s1 = s0.split(SPLIT);
                    h = Integer.parseInt(s1[0]);
                    m = Integer.parseInt(s1[1]);
                    value = Double.parseDouble(s1[2]);
                    dTimeSetting.add(new DTimeSetting(new DTimeStamp(0, h, m), value));
                }
            }
            return dTimeSetting.toArray(new DTimeSetting[dTimeSetting.size()]);
        } else {
            return null;
        }
    }

    protected static double[] JSONArrayToDoubleArray(JSONArray jsonArray) {
        if (jsonArray != null && !jsonArray.isEmpty()) {
            double[] doubleArray = new double[jsonArray.size()];
            int i = 0;
            double value;
            var iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                try {
                    value = (Double) iterator.next();
                } catch (ClassCastException ex) {
                    return null;
                }
                doubleArray[i++] = value;
            }
            return doubleArray;
        } else {
            return null;
        }
    }

    protected static boolean correctBG(double BG) {
        return BG > DPatientCore.BG_FATAL_MIN && BG < DPatientCore.BG_FATAL_MAX;
    }

    //sensI, sensW, actionL, actionI actionK
    
    protected static boolean correctSettings(DTimeSetting[] sensI, DTimeSetting[] sensW,
            DTimeSetting[] actionL, DTimeSetting [] actionI, DTimeSetting[] actionK) {

        if (sensI == null || sensW == null || actionL == null || actionI == null || actionK == null) {
            return false;
        } else {
            if (sensI.length == 0 || sensW.length == 0 || actionL.length == 0 || actionI.length == 0 || actionK.length == 0) {
                return false;
            } else {
                //
                // Tutaj sprawdzenie czy dane są z sensem
                // sensI, sensW, actionL, actionI actionK
                //
                return true;
            }
        }
    }

    //sensI, sensW, actionL, actionI, actionK
    protected static boolean correctSettings(double[] s0, double[] s1, double[] s2, double[] s3, double[] s4) {

        if (s0 == null || s1 == null || s2 == null || s3 == null || s4 == null) {
            return false;
        } else {

            if (s0.length == 0 || s1.length == 0 || s2.length == 0 || s3.length == 0 || s4.length == 0) {
                return false;
            } else {

                if ((s0.length == HOURS_IN_DAY && s1.length == HOURS_IN_DAY
                        && s2.length == HOURS_IN_DAY && s3.length == HOURS_IN_DAY && s4.length == HOURS_IN_DAY)
                        || (s0.length == MINUTES_IN_DAY && s1.length == MINUTES_IN_DAY
                        && s2.length == MINUTES_IN_DAY && s3.length == MINUTES_IN_DAY && s4.length == MINUTES_IN_DAY)) {

                    for (int i = 0; i < s0.length; i++) {
                        if (s0[i] < 0 || s1[i] < 0 || s2[i] < 0 || s3[i] < 0 || s4[i] < 0) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }

            }
        }
    }

}
