package eu.tomasznowicki.dvt.time;

import eu.tomasznowicki.dvt.raport.ToStrings;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * DTimeSetting objects used to describe the whole day. Example:<br><br>
 * DTimeSetting(00:00,value1) | from 0:00 to 2:30 : value1<br>
 * DTimeSetting(02:30,value2) | from 2:30 to 4:30 : value2<br>
 * DTimeSetting(04:40,value3) | from 4:40 to 0:00 : value3<br>
 *
 * @author tomek
 */
public final class DDaySettings {

    private final SortedSet<DTimeSetting> settings;

    /**
     * Creates empty DDaySettings object. Empty means that there is only one
     * DTimeSetting object: time 00:00, value 0. It means that for the whole day
     * the value is 0.
     */
    public DDaySettings() {
        settings = new TreeSet<>();
        addSetting(new DTimeSetting());

    }

    public DDaySettings(DTimeSetting... dSettings) {
        this();
        addSettings(dSettings); //Nigdy nie jest puste
    }

    /**
     * Alters settings of a day by adding new time setting object or replacing
     * an existing on if there is already time setting with the same time stamp.
     *
     * @param dSetting Time setting to be inserted into a day or replace
     * existing one.
     */
    public void addSetting(DTimeSetting dSetting) { //Ignoruje null-a

        if (dSetting != null) {

            if (settings.contains(dSetting)) {
                settings.remove(dSetting); //Bo porównanie dotycy tylko czasu nie wartości
            }

            settings.add(dSetting);
        }
    }

    /**
     * Calls addSetting method for every given time setting.
     *
     * @param dSettings Array of time settings.
     * @see addSetting
     */
    public void addSettings(DTimeSetting... dSettings) {

        for (DTimeSetting s : dSettings) {

            addSetting(s);
        }
    }

    /**
     * A way to examine current day settings.
     *
     * @return Array of time settings.
     */
    public DTimeSetting[] getSettings() {

        //Nigdy nie jest puste
        return settings.toArray(new DTimeSetting[settings.size()]);

    }

    public int size() {
        return settings.size();
    }

    /*
     * After calling clear the day settings is empty. It means that for the
     * whole day the value is 0. There is only on time setting inside time 00:00
     * value 0.
     */
    public void clear() {

        settings.clear();
        addSetting(new DTimeSetting());
    }

    public double[] dayValuesStep() {
        var minutes = new double[DTimeStamp.MINUTES_IN_DAY];
        var time = new DTimeStamp(0);
        for (int i = 0; i < minutes.length; i++) {
            minutes[i] = valueAtTimeStampStep(new DTimeStamp(i));
        }
        return minutes;
    }

    public double[] dayValuesPolyline() {
        var minutes = new double[DTimeStamp.MINUTES_IN_DAY];
        var time = new DTimeStamp(0);
        for (int i = 0; i < minutes.length; i++) {
            minutes[i] = valueAtTimeStampPolyline(new DTimeStamp(i));
        }
        return minutes;
    }

    public double valueAtTimeStampStep(DTimeStamp dTime) {

        var dayStamp = dTime.dayTimeStamp();

        var value = settings.last().value;

        for (DTimeSetting setting : settings) {

            if (dayStamp.compareTo(setting.time) > 0) {

                value = setting.value;

            } else {

                break;
            }
        }

        return value;
    }

    //Tutaj po łamanej
    public double valueAtTimeStampPolyline(DTimeStamp dTime) {

        var dayStamp = dTime.dayTimeStamp();
        double value = 0;

        if (settings.size() == 1) {
            value = settings.first().value;
        } else {

            DTimeStamp leftTime = null, rightTime = null;
            double leftValue = 0, rightValue = 0;

            //Punkt odniesienia z lewej
            for (DTimeSetting s : settings) {
                if (s.time.compareTo(dayStamp) <= 0) {
                    leftTime = s.time;
                    leftValue = s.value;
                } else {
                    break;
                }
            }

            if (leftTime == null) {
                DTimeSetting s = settings.last();
                leftTime = s.time.shift(-DTimeStamp.MINUTES_IN_DAY);
                leftValue = s.value;
            }

            //Punkt odniesienia z prawej
            for (DTimeSetting s : settings) {
                if (s.time.compareTo(dayStamp) > 0) {
                    rightTime = s.time;
                    rightValue = s.value;
                    break;
                }
            }

            if (rightTime == null) {
                DTimeSetting s = settings.first();
                rightTime = s.time.shift(DTimeStamp.MINUTES_IN_DAY);
                rightValue = s.value;
            }

            //Interpolacja
            double range = (double) (rightTime.total - leftTime.total);
            double point = (double) (dayStamp.total - leftTime.total);

            value = leftValue + (point / range) * (rightValue - leftValue);

        }

        return value;

    }

    @Override
    public String toString() {
        return ToStrings.daySettingsToString(this);
    }

}
