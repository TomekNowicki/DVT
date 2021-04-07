package eu.tomasznowicki.dvt.time;

import eu.tomasznowicki.dvt.biocyb.DBolus;
import eu.tomasznowicki.dvt.biocyb.DMeal;
import eu.tomasznowicki.dvt.raport.ToStrings;

/**
 * Time Stamp specifies a snapshot in minute resolution time.
 *
 * @author www.tomasznowicki.eu
 */
public final class DTimeStamp implements Comparable<DTimeStamp> {

    public final static short HOURS_IN_DAY = 24;

    /**
     * How many minutes is in an hour.
     */
    public final static short MINUTES_IN_HOUR = 60;

    /**
     * How many minutes is in a day.
     */
    public final static short MINUTES_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOUR;

    /**
     * The therapy is counted down by the minute. Total minutes of time snapshot
     * are formated to day:hour:minute.
     *
     * @see total
     */
    public final long day;//, hour, minute;

    public final short hour, minute;

    /**
     * Which minute of the therapy within current day of the therapy without
     * taking into account hours.
     */
    public final short point;

    /**
     * Which minute of the therapy without taking into account days and hours.
     * It can be negative. It is also formatted to day:hour:minute.
     *
     * @see day
     * @see hour
     * @see minute
     */
    public final long total;

    /**
     * Constructa a new DTimeStamp object decoding the total minutes into
     * day:hour:minute
     *
     * @param minutes Total minutes of the therapy.
     * @see day
     * @see hour
     * @see minute
     */
    public DTimeStamp(long minutes) {

        total = minutes;

        if (total < 0) {

            day = total / MINUTES_IN_DAY - 1;
            point = (short) (MINUTES_IN_DAY + total % MINUTES_IN_DAY);

        } else {

            day = total / MINUTES_IN_DAY;
            point = (short) (total % MINUTES_IN_DAY);
        }

        hour = (byte) (point / MINUTES_IN_HOUR);
        minute = (byte) (point % MINUTES_IN_HOUR);
    }

    /**
     * Initializes a newly created DTimeStamp object so that it represented
     * day==0, hour==0, minute==0
     */
    public DTimeStamp() {

        this(0L);
    }

    /**
     * Constructs a new TimeStamp object. Decodes the day, hour, minute
     * parameters to total minutes of the therapy.
     *
     * @param day The day of the therapy.
     * @param hour The hour of the therapy.
     * @param minute The minute of the therapy.
     * @see total
     * @see point
     */
    public DTimeStamp(long day, int hour, int minute) {

        //Brakuje kontroli
        this(MINUTES_IN_DAY * day + MINUTES_IN_HOUR * hour + minute);
    }

    /**
     * Realize time shift.
     *
     * @param minutes Minutes shift. May be negative.
     * @return new DTimeStamp object time shifted to the orginal.
     */
    public final DTimeStamp shift(long minutes) {

        return new DTimeStamp(total + minutes);
    }

    /**
     * Converts a total time stamp to a day time stamp. It means that hour and
     * minute of the original time stamp is kept but the day is reduce to 0.
     *
     * @return DTimeStamp object whre day == 0. is reduced to 0;
     */
    public final DTimeStamp dayTimeStamp() {
        return day == 0 ? this : new DTimeStamp(point);
    }

    /**
     * *
     * abc
     *
     * @param time0
     * @param time1
     * @return Czas wcześniejszy
     */
    public static final DTimeStamp prior(DTimeStamp time0, DTimeStamp time1) {

        if (time0 != null && time1 != null) {
            return time0.total < time1.total ? time0 : time1;
        } else if (time0 != null && time1 == null) {
            return time0;
        } else if (time0 == null && time1 != null) {
            return time1;
        } else {
            return null;
        }
    }
    
    public static final long gap(DTimeStamp time0, DTimeStamp time1) {
        
        return time1.total - time0.total;
    }
    
    
    
    
    

    /**
     * Unique value within int type range. Since total minutes of the therapy is
     * long type the hash code values repeat after overdrawing the int range.
     *
     * @return Total minutes of the therapy within the int type range. Can be
     * negative.
     * @see total
     */
    @Override
    public int hashCode() {

        if (total > 0) {
            return (int) (total % ((long) Integer.MAX_VALUE));
        }
        if (total < 0) {
            return (int) (total % (long) Math.abs(Integer.MIN_VALUE));
        }

        return 0;
    }

    /**
     * Compares this DTimeStamp object. The result is true if and only if the
     * argument is not null and is a DTimeStamp object that has the same value
     * of total minutes of the therapy.
     *
     * @param obj The object to compare this DTimeStamp object
     * @return true if the object indicates the same time snapshot of the
     * therapy, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof DTimeStamp)) {
            return false;
        }

        return total == ((DTimeStamp) obj).total;
    }

    public boolean equalsDayTime(Object obj) {
        
        if (obj instanceof DTimeStamp) {
            return point == ((DTimeStamp) obj).point;
        } else if (obj instanceof DMeal) {
            return point == ((DMeal) obj).time.point;
        } else if (obj instanceof DBolus) {
            return point == ((DBolus) obj).time.point;
        } else {
            return false;
        }
    }

    /**
     * Compares two time stamps in timewise order. The comparision is based on
     * the total minutes of the therapy.
     *
     * @param dTimeStamp the time stamp to be compared
     * @return <br>value 0 if this.equals(argument)<br>
     * value 1 this.total jest większy argument.total<br>
     * value -1 if this.total jest mniejszy argument.total
     */
    @Override
    public int compareTo(DTimeStamp dTimeStamp) {

        if (this.equals(dTimeStamp)) {
            return 0;
        } else if (this.total > dTimeStamp.total) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Converts time stamp to a string.
     *
     * @return String at format day@hour:minute
     */
    @Override
    public String toString() {

        //return String.format(FORMAT, day, hour, minute);
        return ToStrings.timeStampToString(this);
    }

    public long[] toArray() {

        return new long[]{day, hour, minute};
    }

}
