package fr.inria.corese.sparql.datatype;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br> 
 * This class is used to create and manage a CoreseDate (xsd:DateTime)
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public class CoreseCalendar extends GregorianCalendar {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    boolean Z = false;
    private boolean bzone = false;
    String zone = "";

    CoreseCalendar() {
    }

    CoreseCalendar(int yy, int mm, int dd) {
        super(yy, mm, dd);
    }

    CoreseCalendar(int yy, int mm, int dd, int hh, int min, int ss) {
        super(yy, mm, dd, hh, min, ss);
    }

    public CoreseCalendar duplicate() {
        return new CoreseCalendar(get(YEAR), get(MONTH), get(DAY_OF_MONTH), get(HOUR_OF_DAY), get(MINUTE), get(SECOND));
        // return (CoreseCalendar) clone();
    }

    public CoreseCalendar duplicate(String tz) {
        CoreseCalendar cal = duplicate();
        cal.setTimeZone(TimeZone.getTimeZone("GMT" + tz));
        cal.setDZone(tz);
        cal.setZ(cal.getRawOffset() == 0);
        return cal;
    }

    public int getRawOffset() {
        return getTimeZone().getRawOffset();
    }

    boolean sameDate(CoreseCalendar cal) {
        return get(YEAR) == cal.get(YEAR) && get(MONTH) == cal.get(MONTH) && get(DAY_OF_MONTH) == cal.get(DAY_OF_MONTH);
    }

    void setZ(boolean z) {
        this.Z = z;
    }

    boolean getZ() {
        return Z;
    }

    void setDZone(String z) {
        this.zone = z;
    }

    String getDZone() {
        return zone;
    }

    float getSeconds() {
        int sec = this.get(Calendar.SECOND);
        int mill_sec = this.get(Calendar.MILLISECOND);
        return sec + (mill_sec / 1000f);
    }

    /**
     * 1 BC -> -1
     * 2 BC -> -2
     */
    int theYear() {
        int year = get(GregorianCalendar.YEAR);
        if (get(GregorianCalendar.ERA) == GregorianCalendar.BC) {
            year = -year;
        }
        return year;
    }

    /**
     * @return the bzone
     */
    public boolean isZone() {
        return bzone;
    }

    /**
     * @param bzone the bzone to set
     */
    public void setZone(boolean bzone) {
        this.bzone = bzone;
    }

}
