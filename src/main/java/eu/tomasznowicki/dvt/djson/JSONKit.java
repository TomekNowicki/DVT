package eu.tomasznowicki.dvt.djson;

import eu.tomasznowicki.dvt.time.DTimeSetting;
import eu.tomasznowicki.dvt.biocyb.DInsulin;
import eu.tomasznowicki.dvt.biocyb.DInsulin.DTypeInsulin;
import eu.tomasznowicki.dvt.kit.DTherapyKit;
import eu.tomasznowicki.dvt.kit.DTherapyKit.DTypeKit;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class JSONKit extends JSONio {

    public static final String //
            NAME = "Diabetes therapy kit",
            NAME_OF_KIT_TYPE = "Type of kit",
            NAME_OF_INSULIN_TYPE_BASE = "Type of base insulin",
            NAME_OF_INSULIN_TYPE_BOLUS = "Type of bolus insulin",
            NAME_OF_SETTINGS_BASE = "Base settings",
            NAME_OF_SETTINGS_BOLUS = "Bolus settings",
            NAME_OF_SETTINGS_SENSETIVITY = "Insulin sensetivity settings";

    public static boolean writeToFile(String fileName, DTypeKit typeKit,
            DTypeInsulin insulinBase, DTypeInsulin insulinBolus,
            DTimeSetting[] base, DTimeSetting[] bolus, DTimeSetting[] sensetivity) {

        if (typeKit == DTypeKit.PUMP) {
            insulinBase = insulinBolus;
        }
        var kit = makeJSONObject(typeKit, insulinBase, insulinBolus, base, bolus, sensetivity);
        return kit != null ? writeToFile(kit, fileName) : false;
    }

    public static boolean writeToFile(String fileName, DTherapyKit dTherapyKit) {
        return writeToFile(fileName, dTherapyKit.typeKit,
                dTherapyKit.typeInsulinBase, dTherapyKit.typeInsulinBolus,
                dTherapyKit.getConfigurationBase(),
                dTherapyKit.getConfiguratinBolus(),
                dTherapyKit.getConfigurationSensitivity());
    }

    public static DTherapyKit readFromFile(String fileName) {
        JSONObject jsonKit = readJSONObjectFromFile(fileName);
        return jsonKit != null ? readJSONObject(jsonKit) : null;
    }

    private static JSONObject makeJSONObject(DTypeKit typeKit,
            DTypeInsulin insulinBase, DTypeInsulin insulinBolus,
            DTimeSetting[] base, DTimeSetting[] bolus, DTimeSetting[] sensetivity) {

        var jsonBase = TimeSettingsArrayToJSONArray(base);
        var jsonBolus = TimeSettingsArrayToJSONArray(bolus);
        var jsonSensetivity = TimeSettingsArrayToJSONArray(sensetivity);
        var jsonKit = new JSONObject();
        jsonKit.put(NAME_OF_ATTENDEE, NAME);
        jsonKit.put(NAME_OF_KIT_TYPE, typeKit.toString());
        jsonKit.put(NAME_OF_INSULIN_TYPE_BASE, insulinBase.toString());
        jsonKit.put(NAME_OF_INSULIN_TYPE_BOLUS, insulinBolus.toString());
        jsonKit.put(NAME_OF_SETTINGS_BASE, jsonBase);
        jsonKit.put(NAME_OF_SETTINGS_BOLUS, jsonBolus);
        jsonKit.put(NAME_OF_SETTINGS_SENSETIVITY, jsonSensetivity);
        return jsonKit;
    }

    private static DTherapyKit readJSONObject(JSONObject jsonKit) {
        var jsonAttendee = (String) jsonKit.get(NAME_OF_ATTENDEE);
        var jsonKitType = (String) jsonKit.get(NAME_OF_KIT_TYPE);
        var jsonInsulinBase = (String) jsonKit.get(NAME_OF_INSULIN_TYPE_BASE);
        var jsonInsulinBolus = (String) jsonKit.get(NAME_OF_INSULIN_TYPE_BOLUS);
        var jsonBase = (JSONArray) jsonKit.get(NAME_OF_SETTINGS_BASE);
        var jsonBolus = (JSONArray) jsonKit.get(NAME_OF_SETTINGS_BOLUS);
        var jsonSensetivity = (JSONArray) jsonKit.get(NAME_OF_SETTINGS_SENSETIVITY);
        DTherapyKit dTherapyKit = null;
        
        
        if (jsonAttendee != null && jsonAttendee.equals(NAME)
                && jsonKitType != null && jsonInsulinBase != null && jsonInsulinBolus != null) {
        
            DInsulin.DTypeInsulin insulinBase = DInsulin.stringToType(jsonInsulinBase);
            DInsulin.DTypeInsulin insulinBolus = DInsulin.stringToType(jsonInsulinBolus);
            
            if (jsonKitType.equals(DTypeKit.PUMP.toString())
                    && insulinBase != null && insulinBolus != null && insulinBase.equals(insulinBolus)) {
                dTherapyKit = DTherapyKit.getPump(insulinBolus);
            
            } else if (jsonKitType.equals(DTypeKit.PENBOX.toString())
                    && insulinBase != null && insulinBolus != null) {
                dTherapyKit = DTherapyKit.getPenBox(insulinBase, insulinBolus);
            }
        }
        if (dTherapyKit != null) {
            dTherapyKit.setConfiguratinBase(JSONArrayToTimeSettingsArray(jsonBase)); //Kit sprawdza null
            dTherapyKit.setConfigurationBolus(JSONArrayToTimeSettingsArray(jsonBolus)); //Kit sprawdza null
            dTherapyKit.setConfigurationSensitivity(JSONArrayToTimeSettingsArray(jsonSensetivity)); //Kit sprawdza null
        }
        return dTherapyKit;
    }

}
