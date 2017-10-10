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
 * An implementation of the xsd:long datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */
@Deprecated
public  class CoreseLong extends CoreseNumber {

	/** logger from log4j */
	private static Logger logger = LogManager.getLogger(CoreseLong.class);

	static final CoreseURI datatype=new CoreseURI(RDF.xsdlong);
	static final int code = LONG;
	
	long lvalue;

	// TODO: fix it
	public CoreseLong(String value) {
            setLabel(value);
            lvalue = Long.parseLong(value.startsWith("+")?value.substring(1):value);
	}


	public CoreseLong(long value) {
		lvalue = value;
                setLabel(Long.toString(value));
	}

        @Override
	public int getCode(){
		return code;
	}
	
        @Override
	public IDatatype getDatatype(){
		return datatype;
	}


        @Override
	public boolean isTrue() {
		return lvalue != 0;
	}
        
        @Override
	public boolean booleanValue() {
		return lvalue != 0;
	}

        @Override
	public long longValue(){
		return lvalue;
	}

        @Override
	public int intValue(){
		return (int) lvalue;
	}
	
        @Override
	public double doubleValue(){
		return  (double) lvalue;
	}
	
        @Override
	public float floatValue(){
		return  (float) lvalue;
	}

	
        @Override
	public double getdValue(){
		return (double) lvalue;
	}
	
        @Override
	public int getiValue(){
		return (int)lvalue;
	}

        @Override
	public long getlValue(){
		return lvalue;
	}
	


        @Override
	public int compare(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG:   
		case INTEGER:   
			long l = iod.longValue();
			return (lvalue < l) ? -1 : (l == lvalue ? 0 : 1);
		
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: 
			double d1 = iod.doubleValue();
			return (doubleValue() < d1) ? -1 : (d1 == doubleValue() ? 0 : 1);
		default: throw failure();
		}	
	}


        @Override
	public boolean less(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER:   
		case LONG:   return longValue() < iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() < iod.doubleValue();
		default: throw failure();
		}	
	}

        @Override
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER:  
		case LONG:   return longValue() <= iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() <= iod.doubleValue();
		default: throw failure();
		}	
	}

        @Override
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER:    
		case LONG:   return longValue() > iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() > iod.doubleValue();
		default: throw failure();
		}	
	}

        @Override
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case INTEGER:     
		case LONG:   return longValue() >= iod.longValue();
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return doubleValue() >= iod.doubleValue();
		default: throw failure();
		}	
	}

        @Override
	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case INTEGER:   
		case LONG:   return longValue() == iod.longValue();
		
		case FLOAT: 
		case DECIMAL: 
		case DOUBLE: return doubleValue() == iod.doubleValue();
		
		//case UNDEF:
                case URI:
		case BLANK: return false;
                                        
		default: throw failure();
		}	
	}


//        @Override
//	public String getNormalizedLabel(){
//		return Long.toString(lvalue);
//	}

//	public static String getNormalizedLabel(String label){
//		if (label.startsWith("+")){
//			label = label.substring(1);
//		}
//		return new Long(label).toString();
//	}

        @Override
	public String getLowerCaseLabel(){
		return getLabel();
	}

}