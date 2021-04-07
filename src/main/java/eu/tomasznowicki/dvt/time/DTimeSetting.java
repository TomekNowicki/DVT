package eu.tomasznowicki.dvt.time;

import eu.tomasznowicki.dvt.raport.ToStrings;

/**
 * Time setting is a single value assigned to time of a day. It repeats every
 * day at the same time.
 *
 * @author www.tomasznowicki.eu
 */
public class DTimeSetting implements Comparable<DTimeSetting> {

    /**
     * The time of a day. Here always day==0.
     */
    public final DTimeStamp time;

    /**
     * Any kind of value. It is not counted while time settings are compared.
     */
    public final double value; //Tutaj to może być ujemne


    /**
     * Constructs a new TimeSetting object.
     *
     * @param dTimeStamp Time stamp converted to a day time stamp, i.e. day==0;
     * @param dValue Real number
     */
    public DTimeSetting(DTimeStamp dTimeStamp, double dValue) {

        time = dTimeStamp.day == 0 ? dTimeStamp : dTimeStamp.dayTimeStamp();
        value = dValue;
    }

    /**
     * Initializes a newly created DTimeSetting object so that it represents
     * value 0 at time 00:00
     */
    public DTimeSetting() {

        this(new DTimeStamp(), 0);
    }

    /**
     * Compares this DTimeStting object. The result is true if and only if the
     * argument is not null and is a DTimeStting object that has the same time
     * stamp of a day. The value is not taken into account.
     *
     * @param obj The object to compare this DTimeSetting object
     * @return true if the object indicates the same time snapshot of the a day,
     * false otherwise.
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof DTimeSetting)) {
            
            return false;
            
        } else {
            
            return time.point == ((DTimeSetting) obj).time.point;
        }

        //return time.point == ((DTimeStamp) obj).point;  Był błąd
        //nie można return time.equals(( (DTimeSetting) obj).time) , bo robi total
    }

    /**
     * Compares two time settings in timewise order. The comparision is based on
     * the day minutes. The value is not taken into account.
     *
     * @param dTimeSetting
     * @return the value 0 if the argument time setting indicates the same day
     * time, a value 1 if the argument time setting indicates previous day time,
     * a value -1 if the argument time setting indicates posterior day time
     */
    @Override
    public int compareTo(DTimeSetting dTimeSetting) {

        return time.compareTo(dTimeSetting.time);
    }

    /**
     * Unique time. The value is not taken into account.
     *
     * @return Point of a day, i.e. minutes from midnight 00:00
     */
    @Override
    public int hashCode() {

        return time.point;
    }


    @Override
    public String toString() {

        return ToStrings.timeSettingToString(this);
    }

}
