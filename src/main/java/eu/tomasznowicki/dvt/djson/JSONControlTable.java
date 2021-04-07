package eu.tomasznowicki.dvt.djson;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.kit.DControlBox;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.Locale;

public class JSONControlTable extends JSONio {

    public static final String //
            NAME = "Diabetes control table",
            NAME_OF_TIMES = "Times of day";

    private static final String 
            SPLIT = ":",
            WRITE_FORMAT = "%d" + SPLIT + "%d",
            READ_REGEX = "^([0-9]|1[0-9]|2[0-3])" + SPLIT + "([0-9]|[1-5][0-9])$";

    public static boolean writeToFile(String fileName, DTimeStamp... dTimeStamps) {
        var jsonControls = makeJSONObject(dTimeStamps);
        return jsonControls != null ? writeToFile(jsonControls, fileName) : false;
    }

    public static boolean writeToFile(String fileName, DControlBox dControlTable) {
        return writeToFile(fileName, dControlTable.getControls());
    }

    public static DControlBox readFromFile(String fileName) {
        JSONObject jsonControls = readJSONObjectFromFile(fileName);
        return jsonControls != null ? readJSONObject(jsonControls) : null;
    }

    private static JSONObject makeJSONObject(DTimeStamp... dTimeStamps) {
        SortedSet<DTimeStamp> setDayTimes = new TreeSet<>();
        for (var timeStamp : dTimeStamps) {
            setDayTimes.add(timeStamp.dayTimeStamp());
        }
        var jsonDayTimes = new JSONArray();
        setDayTimes.forEach((timeStamp) -> {
            jsonDayTimes.add(String.format(Locale.US, WRITE_FORMAT, timeStamp.hour, timeStamp.minute));
        });
        var jsonControl = new JSONObject();
        jsonControl.put(NAME_OF_ATTENDEE, NAME);
        jsonControl.put(NAME_OF_TIMES, jsonDayTimes);
        return jsonControl;
    }

    private static DControlBox readJSONObject(JSONObject jsonControls) {
        var jsonAttendee = (String) jsonControls.get(NAME_OF_ATTENDEE);
        var jsonDayTimes = (JSONArray) jsonControls.get(NAME_OF_TIMES);
        if (jsonAttendee !=null && jsonAttendee.equals(NAME) && jsonDayTimes != null) {    
            var dControl = new DControlBox();
            var pattern = Pattern.compile(READ_REGEX);
            String s0, s1[];
            int h, m;
            var iterator = jsonDayTimes.iterator();
            while (iterator.hasNext()) {
                s0 = (String) iterator.next();
                if (pattern.matcher(s0).matches()) {
                    s1 = s0.split(SPLIT);
                    h = Integer.parseInt(s1[0]);
                    m = Integer.parseInt(s1[1]);
                    dControl.addControl(new DTimeStamp(0, h, m));
                }
            }
            return dControl;
        } else {
            return null;
        }
    }
    
}
