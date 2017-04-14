package fr.inria.acacia.corese.cg.datatype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:date datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseDate extends CoreseDatatype {
	
	/** logger from log4j */
	private static Logger logger = LogManager.getLogger(CoreseDate.class);
	
	static int code=DATE;
	static final String TODAY = "today";
	protected CoreseCalendar cal = null;
	protected String normalizedLabel = "";

	
	static final CoreseURI datatype=new CoreseURI(RDF.xsddate);
	
	public CoreseDate()throws CoreseDatatypeException{
		this(TODAY);
	}
	
	public CoreseDate(String label) throws CoreseDatatypeException{
		if (label.equals(TODAY))
			cal = new CoreseCalendar();
		else
			cal = parse(label);
		normalizedLabel = toString(cal);
	}
	
	
        @Override
	public  int getCode(){
		return code;
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
	
	
	public IDatatype getHour(){
		int hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
		return DatatypeMap.newInstance(hour);
	}
	
	public IDatatype getMinute(){
		int min = cal.get(GregorianCalendar.MINUTE);
		return DatatypeMap.newInstance(min);
	}
	
	public CoreseDecimal getSecond(){
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
			logger.fatal(e.getMessage());
		}
		catch (Exception e) {
			logger.fatal(e.getMessage());
		}
	}
	
	public IDatatype getDatatype(){
		return datatype;
	}
	
	
	/**
         * Negative years:
         * "-1-10-12"^^xsd:date represents year -1
         * year 0 is not a correct lexical date (http://www.w3.org/TR/xmlschema-2/#dateTime)
         * in this implementation it is considered as year +1
         */
	static CoreseCalendar parse(String date) throws CoreseDatatypeException {
		boolean Z = false;
		String num = null;
		CoreseCalendar cal;
		if (date.endsWith("Z")){
			Z = true;
			date = date.substring(0, date.length()-1);
		}
		String[] items = date.split("T");
		if (items.length == 0) {
			throw new CoreseDatatypeException("xsd:dateTime", date);
		}
		String thedate = items[0];
                
                boolean bp = false;
                if (thedate.startsWith("-")){
                    // Negative year Before Present
                    bp = true;
                    thedate = thedate.substring(1);
                }
                
		String[] elem = thedate.split("-");
		if (elem.length != 3) {
			throw new CoreseDatatypeException("xsd:dateTime", date);
		}
                
		int year =  Integer.parseInt(elem[0]);
                        
                if (year == 0){
                    bp = true;
                }
                                           
		int month = Integer.parseInt(elem[1]);
		
		int ind = elem[2].indexOf("+");
		if (ind != -1){
			// "2006-08-23+00:00"  skip the numbers ...
			num = elem[2].substring(ind);
			elem[2] = elem[2].substring(0, ind);
		}
		
		int day =   Integer.parseInt(elem[2]);
		cal = new CoreseCalendar(year, month-1, day);
                if (bp){
                    cal.set(GregorianCalendar.ERA, GregorianCalendar.BC);
                }
		cal.setZ(Z);
		cal.setNum(num);
		
		if (items.length == 2) { // parse time : 12:34:05
			
			// [-]CCYY-MM-DD T hh:mm:ss[Z|(+|-)hh:mm]
			// time :  hh:mm:ss 
			// time2 : [Z|(+|-)hh:mm]
						
			String strtime = items[1];
						
			int size = 8;
			
			if (strtime.length() > 9 && strtime.charAt(8) == '.'){
				// check milliseconds:  12:13:14.56
				// set size as length of time + ms
				for (int i=9; i<strtime.length(); i++){
					char next = strtime.charAt(i);

					if (! (next >= '0' && next <= '9')){
						break;
					}
					else {
						size = i+1;
					}
				}
			}
								
			String time = strtime.substring(0, size);
                        
			String zone = strtime.substring(size, strtime.length());	// not used yet			

			if (zone.length()>0){
				cal.setDZone(zone);
			}
			else if (Z){
				cal.setDZone("Z");
			}

			String[] thetime = time.split(":");
			
			int[] fields = {
					GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE,
					GregorianCalendar.SECOND};
			int tt;
			for (int i = 0; i < thetime.length; i++) {
				try {
					tt = Integer.parseInt(thetime[i]);
					cal.set(fields[i], tt);
				}
				catch (Exception e) {
					if (i == 2){
						// there may be a float number of seconds : 12:34:05.5
						Float f = Float.parseFloat(thetime[i]);
                                                // set integer number of sec
						cal.set(fields[i], f.intValue());
						Float dec = f -  (float) f.intValue(); // dec = 0.5
						float fmilli = dec * (float)1000; //  milliseconds
						if (fmilli >= 1){
							int milli = (int) fmilli;
							cal.setSeconds(f);
							cal.setTimeInMillis(cal.getTimeInMillis() + milli); // add the millisec
						}                                                                                                                                                                                                                                        
					}
					else throw new CoreseDatatypeException("xsd:dateTime", date);
				}
			}
		}
		return cal;
	}
	
	public IDatatype getTZ(){
		return DatatypeMap.createLiteral(cal.getDZone());
	}
	
	public IDatatype getTimezone(){
		String str = cal.getDZone();
		if (str == "") return null;
		if (str.equals("Z")) 
			return DatatypeMap.createLiteral("PT0S", RDF.xsddaytimeduration);
		String[] zone = str.split(":");
		String item = zone[0];
		String sign = "+";
		if (item.startsWith("-")){
			sign = "-";
		}
		item = item.substring(1);
		if (item.startsWith("0")){
			item = item.substring(1);
		}
		String res = sign + "PT" + item + "H";
		return DatatypeMap.createLiteral(res, RDF.xsddaytimeduration);
	}
	
	protected CoreseCalendar getDate(){
		return cal;
	}
	
        @Override
	public boolean isNumber(){
		return false;
	}
	
        @Override
	public int compare(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case DATE: 
			CoreseDate dt = (CoreseDate) iod;
			long l1 =    getDate().getTimeInMillis();
			long l2 = dt.getDate().getTimeInMillis();
			return (l1 < l2) ? -1 : (l1 == l2 ? 0 : 1);
		}
		throw failure();
	}
	

        @Override
	public boolean less(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case DATE:
			check(iod);
			CoreseDate dt = (CoreseDate) iod;
			return cal.getTimeInMillis() < dt.getDate().getTimeInMillis();
		}
		throw failure();
	}
	
        @Override
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case DATE:
			check(iod);
			CoreseDate dt = (CoreseDate) iod;
			return cal.getTimeInMillis() <= dt.getDate().getTimeInMillis();
		}
		throw failure();
	}
	
        @Override
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case DATE:
			check(iod);
			CoreseDate dt = (CoreseDate) iod;
			return cal.getTimeInMillis() > dt.getDate().getTimeInMillis();
		}
		throw failure();
	}
	
        @Override
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case DATE:
			check(iod);
			CoreseDate dt = (CoreseDate) iod;
			return cal.getTimeInMillis() >= dt.getDate().getTimeInMillis();
		}
		throw failure();
	}
	
	
	
        @Override
	public String getNormalizedLabel() {
		return normalizedLabel;
	}
	
	public static CoreseDate today()   {
		try {
			return new CoreseDate(TODAY);
		}
		catch (CoreseDatatypeException e) {
			return null; // never happens
		}
	}
	
	public static String toString(CoreseCalendar cal) {
		int day =   cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH)+1 ;
		int year=   cal.theYear();
		String res = Integer.toString(year) + "-";          
		if (month < 10)
			res += "0";
		res += Integer.toString(month) + "-";
		if (day < 10)
			res += "0";
		res += Integer.toString(day);
		int hour=cal.get(Calendar.HOUR_OF_DAY);
		int min= cal.get(Calendar.MINUTE);
		int sec= cal.get(Calendar.SECOND);
		String time="";
		if (hour > 0 || min > 0 || sec > 0 || cal.getSeconds() > 0) {
                    
			if (hour < 10) time += "0";
			time += hour + ":";
                        
			if (min < 10) time += "0";
			time += min + ":";
                        
                        if (cal.getSeconds() > 0){
                            // float number of sec
                            if (cal.getSeconds() < 10){
                                time += "0";
                            }
                            time += cal.getSeconds();
                        }
                        else {
                            if (sec < 10) time += "0";
                            time += sec;                            
                        }
                        
			res+= "T" + time;
		}
		if (cal.getZ()) res += "Z";
		if (cal.getNum()!=null) res += cal.getNum();
		//logger.debug("** date " + res);
		return res;
	}
	
//	public String toString() {
//		return toString(cal);
//	}
	
	public static String getNormalizedLabel(String label){
		//if (true) return label;
		CoreseCalendar cal = null;
		try{
			cal = parse(label);
		}
		catch(CoreseDatatypeException e){
			return label;
		}
		
		return toString(cal);
	}
	
        @Override
	public String getLowerCaseLabel() {
		return this.getNormalizedLabel();
	}
	
        @Override
	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case DATE:
			check(iod);
			CoreseDate dt = (CoreseDate) iod;
			boolean b =  cal.getTimeInMillis() == dt.getDate().getTimeInMillis();
			return b && cal.getZ() == dt.getDate().getZ() 
			&& eq(dt);
			
		case URI:
		case BLANK: return false;
		}
		throw failure();
	}
	

	boolean eq(CoreseDate icod){
		String num = icod.getDate().getNum();
		if (cal.getNum() == null){
			return num == null;
		}
		else if (num == null){
			return false;
		}
		else return cal.getNum().equals(num);
	}
	
	void check(IDatatype icod) throws CoreseDatatypeException {
		if (SPARQLCompliant && getClass() != icod.getClass()){
			throw failure();
		}
	}
	
}
