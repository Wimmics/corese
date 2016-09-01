package fr.inria.acacia.corese.cg.datatype;

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
 * An implementation of the xsd:integer datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseInteger extends CoreseNumber {

	/** logger from log4j */
	private static Logger logger = LogManager.getLogger(CoreseLong.class);
	public static final CoreseInteger ZERO = new CoreseInteger(0);
	public static final CoreseInteger ONE =  new CoreseInteger(1);
	public static final CoreseInteger TWO =  new CoreseInteger(2);
	public static final CoreseInteger THREE= new CoreseInteger(3);
	public static final CoreseInteger FOUR = new CoreseInteger(4);
	public static final CoreseInteger FIVE = new CoreseInteger(5);

	static final CoreseURI datatype=new CoreseURI(RDF.xsdinteger);
	static final int code = INTEGER;
	
	int ivalue;

	public CoreseInteger(String value) {
		if (value.startsWith("+")){
			value = value.substring(1);
		}
		ivalue = new Integer(value).intValue();
	}


	public CoreseInteger(int value) {
		ivalue = value;
	}

	public int getCode(){
		return code;
	}
	
	public IDatatype getDatatype(){
		return datatype;
	}


	public boolean isTrue() {
		return ivalue != 0;
	}

	public long longValue(){
		return ivalue;
	}

	public int intValue(){
		return  ivalue;
	}
	
	public double doubleValue(){
		return  (double) ivalue;
	}
	
	public float floatValue(){
		return  (float) ivalue;
	}

	
	public double getdValue(){
		return (double) ivalue;
	}
	
	public int getiValue(){
		return ivalue;
	}

	public long getlValue(){
		return (long)ivalue;
	}
	


	public int compare(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG: 
			long l = iod.longValue();
			return (ivalue < l) ? -1 : (l == ivalue ? 0 : 1);
		case INTEGER:   
			long i = iod.intValue();
			return (ivalue < i) ? -1 : (i == ivalue ? 0 : 1);
		
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: 
			double d1 = iod.doubleValue();
			return (doubleValue() < d1) ? -1 : (d1 == doubleValue() ? 0 : 1);
		default: throw failure();
		}	
	}


	public boolean less(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER: return ivalue < iod.intValue(); 
		case LONG:    return ivalue < iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() < iod.doubleValue();
		default: throw failure();
		}	
	}

	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER:  return ivalue <= iod.intValue(); 
		case LONG:     return ivalue <= iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() <= iod.doubleValue();
		default: throw failure();
		}	
	}

	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER:  return ivalue > iod.intValue(); 
		case LONG:     return ivalue > iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() > iod.doubleValue();
		default: throw failure();
		}	
	}

	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER: return ivalue >= iod.intValue(); 
		case LONG:    return ivalue >= iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() >= iod.doubleValue();
		default: throw failure();
		}	
	}

	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case INTEGER: return ivalue == iod.intValue(); 
		case LONG:    return ivalue == iod.longValue();
		
		case FLOAT: 
		case DECIMAL: 
		case DOUBLE: return doubleValue() == iod.doubleValue();
		
		case URI:
		case BLANK: return false;
		default: throw failure();
		}	
	}




	public String getNormalizedLabel(){
		return Integer.toString(ivalue);
	}

	public static String getNormalizedLabel(String label){
		if (label.startsWith("+")){
			label = label.substring(1);
		}
		return new Integer(label).toString();
	}

	public String getLowerCaseLabel(){
		return Integer.toString(ivalue);
	}
	
	

}