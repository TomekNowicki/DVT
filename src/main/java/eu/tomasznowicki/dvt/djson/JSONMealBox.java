package eu.tomasznowicki.dvt.djson;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.kit.DMealBox;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.Locale;

public class JSONMealBox extends JSONio {

    private static final String //
            NAME = "Diabetes menu",
            NAME_OF_MEALS = "Planned meals",
            NAME_OF_AHEAD = "Notify in advance of minutes";

    private static final String //
            SPLIT = ":",
            WRITE_FORMAT = "%d" + SPLIT + "%d" + SPLIT + "%.3f" + SPLIT + "%.3f" + SPLIT + "%.3f" + SPLIT + "%d",
            F_REGEX = "[0-9]+.[0-9]{3}",
            READ_REGEX = "^([0-9]|1[0-9]|2[0-3])" + SPLIT + "([0-9]|[1-5][0-9])" + F_REGEX + SPLIT
            + F_REGEX + SPLIT + F_REGEX + SPLIT + "([1-9]?[0-9])$";

    public static boolean writeToFile(String fileName, int ahead, DMeal... dMeals) {
        var jsonMeals = makeJSONObject(ahead, dMeals);
        return jsonMeals != null ? writeToFile(jsonMeals, fileName) : false;
    }

    public static boolean writeToFile(String fileName, int ahead, DMealBox dMealBox) {
        return writeToFile(fileName, ahead, dMealBox.getMeals());
    }

    public static DMealBox readFromFile(String fileName) {
        JSONObject jsonMeals = readJSONObjectFromFile(fileName);
        return jsonMeals != null ? readJSONObject(jsonMeals) : null;
    }

    private static JSONObject makeJSONObject(int ahead, DMeal... dMeals) {
        SortedSet<DMeal> dayMeals = new TreeSet<>();
        for (var meal : dMeals) {
            dayMeals.add(new DMeal(meal.time.dayTimeStamp(), meal));
        }
        var jsonDayMeals = new JSONArray();
        dayMeals.forEach((dayMeal) -> {
            jsonDayMeals.add(String.format(Locale.US, WRITE_FORMAT, dayMeal.time.hour, dayMeal.time.minute,
                    dayMeal.carbGrams, dayMeal.fatGrams, dayMeal.protGrams, dayMeal.glycemnicIndex));
        });
        var jsonMealBox = new JSONObject();
        jsonMealBox.put(NAME_OF_ATTENDEE, NAME);
        jsonMealBox.put(NAME_OF_AHEAD, Integer.toString(ahead));
        jsonMealBox.put(NAME_OF_MEALS, jsonDayMeals);
        return jsonMealBox;
    }

    private static DMealBox readJSONObject(JSONObject jsonMeals) {
        var jsonAttendee = (String) jsonMeals.get(NAME_OF_ATTENDEE);
        var jsonAhead = (String) jsonMeals.get(NAME_OF_AHEAD);
        var jsonDayMeals = (JSONArray) jsonMeals.get(NAME_OF_MEALS);

        if (jsonAttendee != null && jsonAttendee.equals(NAME)
                && jsonAhead != null && jsonDayMeals != null) {

            int ahead = DMealBox.AHEAD_MIN_VALUE;
            try {
                ahead = Integer.parseInt(jsonAhead);
            } catch (NumberFormatException ex) {
            }

            var dMealBox = new DMealBox();
            dMealBox.setAhead(ahead);
            var pattern = Pattern.compile(READ_REGEX);
            String s0, s1[];
            int h, m, gi;
            double cg, fg, pg;
            var iterator = jsonDayMeals.iterator();
            while (iterator.hasNext()) {
                s0 = (String) iterator.next();
                if (pattern.matcher(s0).matches() || true) {
                    s1 = s0.split(SPLIT);
                    h = Integer.parseInt(s1[0]);
                    m = Integer.parseInt(s1[1]);
                    cg = Double.parseDouble(s1[2]);
                    fg = Double.parseDouble(s1[3]);
                    pg = Double.parseDouble(s1[4]);
                    gi = Integer.parseInt(s1[5]);
                    dMealBox.addMeal(new DMeal(new DTimeStamp(0, h, m), cg, fg, pg, gi));
                }
            }
            return dMealBox;
        } else {
            return null;
        }
    }

}
