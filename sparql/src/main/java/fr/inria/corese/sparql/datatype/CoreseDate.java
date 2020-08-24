package fr.inria.corese.sparql.datatype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import java.util.TimeZone;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:date datatype used by Corese
 * <br>
 *
 * @author Olivier Savoie
 */
public class CoreseDate extends CoreseDatatype {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(CoreseDate.class);
    private Marker fatal = MarkerFactory.getMarker("FATAL");
    static int code = DATE;
    static final String TODAY = "today";
    public static final String MINUS_BOUND = "-14:00";
    public static final String PLUS_BOUND  = "+14:00";
    protected CoreseCalendar cal = null;
    protected String normalizedLabel = "";
    static final CoreseURI datatype = new CoreseURI(RDF.xsddate);

    public CoreseDate() throws CoreseDatatypeException {
         this(new CoreseCalendar());
   }
    
    public CoreseDate(CoreseCalendar cal){
        this.cal = cal;
        normalizedLabel = toString(cal);
    }

    public CoreseDate(String label) throws CoreseDatatypeException {
        this(label.equals(TODAY) ? new CoreseCalendar() : parse(label));
    }
    
    @Override
    public int getCode() {
        return code;
    }
    
     @Override 
    public boolean isDate(){
        return true;
    }

    public IDatatype getYear() {
        return DatatypeMap.newInstance(cal.theYear());
    }

    public IDatatype getMonth() {
        return DatatypeMap.newInstance(cal.get(GregorianCalendar.MONTH) + 1);
    }

    public IDatatype getDay() {
        return DatatypeMap.newInstance(cal.get(GregorianCalendar.DAY_OF_MONTH));
    }

    public IDatatype getHour() {
        int hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
        return DatatypeMap.newInstance(hour);
    }

    public IDatatype getMinute() {
        int min = cal.get(GregorianCalendar.MINUTE);
        return DatatypeMap.newInstance(min);
    }

    public CoreseDecimal getSecond() {
        int sec = cal.get(GregorianCalendar.SECOND);
        return new CoreseDecimal(sec);
    }

    void test(String label, String format) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(format);
            Date date = df.parse(label);
            String buf = df.format(date);
            logger.debug("** date " + buf + " " + date);
        } catch (ParseException e) {
            logger.error(fatal, e.getMessage());
        } catch (Exception e) {
            logger.error(fatal, e.getMessage());
        }
    }

    @Override
    public IDatatype getDatatype() {
        return datatype;
    }

    /**
     * Negative years: "-1-10-12"^^xsd:date represents year -1 year 0 is not a
     * correct lexical date (http://www.w3.org/TR/xmlschema-2/#dateTime) in this
     * implementation it is considered as year +1
     */
    static CoreseCalendar parse(String date) throws CoreseDatatypeException {
        boolean Z = false;
        boolean isTime = false;
        String zone = null;

        if (date.endsWith("Z")) {
            Z = true;
            date = date.substring(0, date.length() - 1);
        }
        
        String[] items = date.split("T");
        if (items.length == 0) {
            throw new CoreseDatatypeException("xsd:dateTime", date);
        }
        String thedate = items[0];

        boolean bp = false;
        if (thedate.startsWith("-")) {
            // Negative year Before Present
            bp = true;
            thedate = thedate.substring(1);
        }

        String[] elem = thedate.split("-");
        if (elem.length != 3) {
            throw new CoreseDatatypeException("xsd:dateTime", date);
        }

        int year = Integer.parseInt(elem[0]);

        if (year == 0) {
            bp = true;
        }

        int month = Integer.parseInt(elem[1]) -1;

        int ind = elem[2].indexOf("+");
        if (ind != -1) {
            // "2006-08-23+00:00"  skip the numbers ...
            zone = elem[2].substring(ind);
            elem[2] = elem[2].substring(0, ind);
        }

        int day = Integer.parseInt(elem[2]);
        
        //cal.setNum(num);

        
        int[] atime = new int[3];
        int milli = 0;
        Float milliF = (float)0;
        
        if (items.length == 2) { // parse time : 12:34:05
            isTime = true;

            // [-]CCYY-MM-DD T hh:mm:ss[Z|(+|-)hh:mm]
            // time :  hh:mm:ss 
            // zone : [Z|(+|-)hh:mm]

            String strtime = items[1];

            int size = 8;

            if (strtime.length() > 9 && strtime.charAt(8) == '.') {
                // check milliseconds:  12:13:14.56
                // set size as length of time + ms
                for (int i = 9; i < strtime.length(); i++) {
                    char next = strtime.charAt(i);

                    if (!(next >= '0' && next <= '9')) {
                        break;
                    } else {
                        size = i + 1;
                    }
                }
            }

            String time = strtime.substring(0, size);
            String[] thetime = time.split(":");

            int[] fields = {
                GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE,
                GregorianCalendar.SECOND};
            int tt;
                      
            for (int i = 0; i < thetime.length; i++) {
                try {
                    tt = Integer.parseInt(thetime[i]);
                    //cal.set(fields[i], tt);
                    atime[i] = tt;
                } catch (Exception e) {
                    if (i == 2) {
                        // there may be a float number of seconds : 12:34:05.5
                        milliF = Float.parseFloat(thetime[i]);
                        // set integer number of sec
                        //cal.set(fields[i], f.intValue());
                        atime[2] = milliF.intValue();
                        Float dec = milliF - (float) milliF.intValue(); // dec = 0.5
                        float fmilli = dec * (float) 1000; //  milliseconds
                        if (fmilli >= 1) {
                            milli = (int) fmilli;
//                            cal.setSeconds(f);
//                            cal.setTimeInMillis(cal.getTimeInMillis() + milli); // add the millisec
                        }
                    } else {
                        throw new CoreseDatatypeException("xsd:dateTime", date);
                    }
                }
            }
            
            //System.out.println("hour1: " + cal.get(Calendar.HOUR_OF_DAY));
            zone = strtime.substring(size, strtime.length());
        }
        
        CoreseCalendar cal;
        if (isTime){
            cal = new CoreseCalendar(year, month, day, atime[0], atime[1], atime[2]);
        }
        else {
             cal = new CoreseCalendar(year, month, day);
        }
        
        if (bp) {
            cal.set(GregorianCalendar.ERA, GregorianCalendar.BC);
        }
        
        cal.setZ(Z);
        
        if (zone != null && zone.length() > 0) {
            // 2002-10-10T12:00:00-05:00 = 2002-10-10T17:00:00Z, five hours later that GMT+00:00
            cal.setDZone(zone);
            cal.setTimeZone(TimeZone.getTimeZone("GMT" + zone));
            cal.setZone(true);
        } 
        else if (Z) {
            cal.setDZone("Z");
            cal.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        }
        
        if (milli != 0){
            cal.setTimeInMillis(cal.getTimeInMillis() + milli);
            cal.setSeconds(milliF);
        }
        
        return cal;
    }

    public IDatatype getTZ() {
        return DatatypeMap.newLiteral(cal.getDZone());
    }

    public IDatatype getTimezone() {
        String str = cal.getDZone();
        if (str == "") {
            return null;
        }
        if (str.equals("Z")) {
            return DatatypeMap.newInstance("PT0S", RDF.xsddaytimeduration);
        }
        String[] zone = str.split(":");
        String item = zone[0];
        String sign = "+";
        if (item.startsWith("-")) {
            sign = "-";
        }
        item = item.substring(1);
        if (item.startsWith("0")) {
            item = item.substring(1);
        }
        String res = sign + "PT" + item + "H";
        return DatatypeMap.newInstance(res, RDF.xsddaytimeduration);
    }

    public CoreseCalendar getCalendar() {
        return cal;
    }
    
    public CoreseCalendar getCalendar(String zone) {
        return getCalendar().duplicate(zone);
    }

    public int getRawOffset() {
        return getCalendar().getTimeZone().getRawOffset();
    }
    
    public boolean isZone(){
        return getCalendar().isZone();
    }

    long getTimeInMillis() {
        return getCalendar().getTimeInMillis();
    }
    
    long getTimeInMillis(String zone) {
        return getCalendar(zone).getTimeInMillis();
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public int compare(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                CoreseDate dt = (CoreseDate) iod;
                long l1 = getCalendar().getTimeInMillis();
                long l2 = dt.getCalendar().getTimeInMillis();
                return Long.compare(l1, l2);
        }
        throw failure();
    }

    /**
     *
     * C.Otherwise, if P contains a time zone and Q does not, compare as
     * follows:
     *
     * P < Q if P < (Q with time zone +14:00) 
     * P > Q if P > (Q with time zone -14:00) 
     * P <> Q otherwise, that is, 
     * if (Q with time zone +14:00) < P < (Q with time zone -14:00)
     *
     * D. Otherwise, if P does not contain a time zone and Q does, compare as
     * follows:
     *
     * P < Q if (P with time zone -14:00) < Q. 
     * P > Q if (P with time zone +14:00) > Q. 
     * P <> Q otherwise, that is, 
     * if (P with time zone +14:00) < Q < (P with time zone -14:00)
     */
    @Override
    public boolean less(IDatatype iod) throws CoreseDatatypeException {
      switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                check(iod);
                CoreseDate dt = (CoreseDate) iod;
                
                if (isZone() == dt.isZone()){
                    return getTimeInMillis() < dt.getTimeInMillis();
                }
                else if (isZone()){
                    return getTimeInMillis() < dt.getTimeInMillis(PLUS_BOUND);                    
                }
                else {
                    return getTimeInMillis(MINUS_BOUND) < dt.getTimeInMillis();
                }
        }
        throw failure();
    }
    
    
    @Override
     public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
      switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                check(iod);
                CoreseDate dt = (CoreseDate) iod;
                
                if (isZone() == dt.isZone()){
                    return getTimeInMillis() <= dt.getTimeInMillis();
                }
                else if (isZone()){
                    return getTimeInMillis() <= dt.getTimeInMillis(PLUS_BOUND);                    
                }
                else {
                    return getTimeInMillis(MINUS_BOUND) <= dt.getTimeInMillis();
                }               
        }
        throw failure();
    }
    
        @Override
    public boolean greater(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                check(iod);
                CoreseDate dt = (CoreseDate) iod;
                
                if (isZone() == dt.isZone()){
                    return getTimeInMillis() > dt.getTimeInMillis();
                }
                else if (isZone()){
                    return getTimeInMillis() > dt.getTimeInMillis(MINUS_BOUND);
                }
                else {
                    return getTimeInMillis(PLUS_BOUND) > dt.getTimeInMillis();
                }
        }
        throw failure();
    }

    
    @Override
    public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                check(iod);
                CoreseDate dt = (CoreseDate) iod;
                
                if (isZone() == dt.isZone()){
                    return getTimeInMillis() >= dt.getTimeInMillis();
                }
                else if (isZone()){
                    return getTimeInMillis() >= dt.getTimeInMillis(MINUS_BOUND);
                }
                else {
                    return getTimeInMillis(PLUS_BOUND) >= dt.getTimeInMillis();
                }               
        }
        throw failure();
    }


    @Override
    public String getNormalizedLabel() {
        return getLabel();
    }
    
    @Override
    public String getLabel() {
        return normalizedLabel;
    }

    public static CoreseDate today() {
        try {
            return new CoreseDate(TODAY);
        } catch (CoreseDatatypeException e) {
            return null; // never happens
        }
    }

    public static String toString(CoreseCalendar cal) {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.theYear();
        String res = Integer.toString(year) + "-";
        if (month < 10) {
            res += "0";
        }
        res += Integer.toString(month) + "-";
        if (day < 10) {
            res += "0";
        }
        res += Integer.toString(day);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        String time = "";
        if (hour > 0 || min > 0 || sec > 0 || cal.getSeconds() > 0) {

            if (hour < 10) {
                time += "0";
            }
            time += hour + ":";

            if (min < 10) {
                time += "0";
            }
            time += min + ":";

            if (cal.getSeconds() > 0) {
                // float number of sec
                if (cal.getSeconds() < 10) {
                    time += "0";
                }
                time += cal.getSeconds();
            } else {
                if (sec < 10) {
                    time += "0";
                }
                time += sec;
            }

            res += "T" + time;
        }

        if (cal.getZ()) {
            res += "Z";
        } else if (cal.getDZone() != null) {
            res += cal.getDZone();
        }

        return res;
    }

    static String getNormalizedLabel(String label) {
        //if (true) return label;
        CoreseCalendar cal = null;
        try {
            cal = parse(label);
        } catch (CoreseDatatypeException e) {
            return label;
        }

        return toString(cal);
    }

    @Override
    public String getLowerCaseLabel() {
        return getLabel();
    }

    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
        switch (iod.getCode()) {
            case DATE:
            case DATETIME:
                //check(iod);
                CoreseDate dt = (CoreseDate) iod;
                boolean b = getTimeInMillis() == dt.getTimeInMillis();
                return b;

            case URI:
            case BLANK:
                return false;
        }
        throw failure();
    }
  
    void check(IDatatype icod) throws CoreseDatatypeException {
        if (DatatypeMap.SPARQLCompliant && getClass() != icod.getClass()) {
            throw failure();
        }
    }
}
