package eu.tomasznowicki.dvt.patient;

import eu.tomasznowicki.dvt.time.DTimeStamp;
import static eu.tomasznowicki.dvt.time.DTimeStamp.MINUTES_IN_HOUR;
import eu.tomasznowicki.dvt.time.DTimeSetting;
import eu.tomasznowicki.dvt.time.DDaySettings;
import eu.tomasznowicki.dvt.time.DClockEventSync;
import eu.tomasznowicki.dvt.raport.DStrings;

public class DPatient extends DPatientCore {

    public static final DTimeStamp DEFAULT_FLOAT_TIME = new DTimeStamp(0, 4, 0);
    public static final int DEFAULT_FLOAT_FREQUENCY = 1; //Every day

    protected DTimeSetting[] // Położenie równowagi albo odniesienie
            setLiver, setIntake, setKidneys, setSensI, setSensW; // 
    protected DDaySettings // Aktualne
            settingsLiver, settingsIntake, settingsKidneys, settingsSensI, settingsSensW;

    private DTimeStamp driftTime;
    private int driftFrequency, driftCounter;

    public DPatient(double dBloodGlucose) {
        super(dBloodGlucose);
        setLiver = setIntake = setKidneys = setSensI = setSensW = null;
        settingsLiver = new DDaySettings();
        settingsIntake = new DDaySettings();
        settingsKidneys = new DDaySettings();
        settingsSensI = new DDaySettings();
        settingsSensW = new DDaySettings();
        driftTime = DEFAULT_FLOAT_TIME;
        driftFrequency = DEFAULT_FLOAT_FREQUENCY;
        driftCounter = driftFrequency;
    }

    public final void setActionLiver(DTimeSetting... dSettings) {
        settingsLiver.clear();
        settingsLiver.addSettings(dSettings);
        setLiver = settingsLiver.getSettings();
        var action = settingsLiver.dayValuesPolyline();
        for (int i = 0; i < action.length; i++) {
            action[i] /= (double)MINUTES_IN_HOUR;
        }
        super.setActionLiver(action);
    }

    public DTimeSetting[] getActionLiverSettings() {
        return settingsLiver.getSettings();
    }
    
    public final void setActionIntake(DTimeSetting... dSettings){
        settingsIntake.clear();
        settingsIntake.addSettings(dSettings);
        setIntake = settingsIntake.getSettings();
        
        var action = settingsIntake.dayValuesPolyline();
        for(int i = 0; i < action.length; i++) {
            action[i] /= (double) MINUTES_IN_HOUR;
        }
        super.setActionIntake(action);
    }
    
    public DTimeSetting [] getActionIntakeSettings() {
        return settingsIntake.getSettings();
    }
    
    

    public final void setActionKidneys(DTimeSetting... dSettings) {
        settingsKidneys.clear();
        settingsKidneys.addSettings(dSettings);
        setKidneys = settingsKidneys.getSettings();
        var action = settingsKidneys.dayValuesPolyline();
        for (int i = 0; i < action.length; i++) {
            action[i] /= (double)MINUTES_IN_HOUR;
        }
        super.setActionKidneys(action);
    }

    public DTimeSetting[] getActionKidneysSettings() {
        return settingsKidneys.getSettings();
    }

    public final void setSensitivityInsulin(DTimeSetting... dSettings) {
        settingsSensI.clear();
        settingsSensI.addSettings(dSettings);
        setSensI = settingsSensI.getSettings();
        super.setSensitivityInsulin(settingsSensI.dayValuesPolyline());
    }

    public DTimeSetting[] getSensitivityInsulinSettigns() {
        return settingsSensI.getSettings();
    }

    public final void setSensitivityW(DTimeSetting... dSettings) {
        settingsSensW.clear();
        settingsSensW.addSettings(dSettings);
        setSensW = settingsSensW.getSettings();
        super.setSensitivityW(settingsSensW.dayValuesPolyline());
    }

    public DTimeSetting[] getSensitivityWSettings() {
        return settingsSensW.getSettings();
    }

    void setFloatTime(DTimeStamp dTimeStamp) {
        driftTime = dTimeStamp.dayTimeStamp();
    }

    DTimeStamp getFloatTime() {
        return driftTime;
    }

    public boolean floatLiver() {
        return false;
    }

    protected boolean floatKidneys() {
        return false;
    }

    protected boolean floatSensitivityInsulin() {
        return false;
    }

    protected boolean floatSensitivityW() {
        return false;
    }

    @Override
    public void syncReceived(DClockEventSync event) {

        if (event.getSource() != clock) {
            return;
        }

        super.syncReceived(event);

        if (clockTime.equalsDayTime(driftTime)) {

            if (--driftCounter == 0) {

                if (dlogger != null) {
                    dlogger.log_function(clockTime, DStrings.PATIET_DRIFTING);
                }

                var driftL = floatLiver();
                var driftK = floatKidneys();
                var driftI = floatSensitivityInsulin();
                var driftW = floatSensitivityW();

                driftCounter = driftFrequency;

                if (dlogger != null) {

                    if (driftL) {
                        dlogger.log_drift(clockTime, DStrings.PATIENT_LIVER, settingsLiver);
                    }
                    if (driftK) {
                        dlogger.log_drift(clockTime, DStrings.PATIENT_KIDNEYS, settingsKidneys);
                    }
                    if (driftI) {
                        dlogger.log_drift(clockTime, DStrings.PATIENT_SENSITIVITY_INSULIN, settingsSensI);
                    }
                    if (driftW) {
                        dlogger.log_drift(clockTime, DStrings.PATIENT_SENSITIVITY_W, settingsSensW);
                    }
                }
            }
        }
    }

}
