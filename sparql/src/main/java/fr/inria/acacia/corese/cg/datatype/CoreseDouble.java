package fr.inria.acacia.corese.cg.datatype;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:double datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseDouble extends CoreseNumber{
	static final CoreseURI datatype=new CoreseURI(RDF.xsddouble);
	static final int code = DOUBLE;
	protected double dvalue = 0;

	public CoreseDouble(String label){
            setLabel(label);
            dvalue = Double.parseDouble(label);
	}
	
	public CoreseDouble(double val){
		dvalue = val;
                setLabel(getNormalizedLabel());
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
		return dvalue != 0.0;
	}
	
        @Override
	public double doubleValue(){
		return  dvalue;
	}
	
        @Override
	public float floatValue(){
		return  (float) dvalue;
	}
	
        @Override
	public long longValue(){
		return (long) dvalue;
	}
	
        @Override
	public int intValue(){
		return (int) dvalue;
	}
	
	
	
	
        @Override
	public double getdValue(){
		return dvalue;
	}
	
        @Override
	public long getlValue(){
		return (long) dvalue;
	}
	


	
        @Override
	public int compare(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG:   
		case INTEGER:   
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: 
			double d1 = iod.doubleValue();
			return (dvalue < d1) ? -1 : (d1 == dvalue ? 0 : 1);
		default: throw failure();
		}	
	}
	

        @Override
	public boolean less(IDatatype iod)  throws  CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG:   
		case INTEGER: 
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return dvalue < iod.doubleValue();
		default: throw failure();
		}	
	}
	
        @Override
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG:   
		case INTEGER: 
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return dvalue <= iod.doubleValue();
		default: throw failure();
		}	
	}
	
        @Override
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG:   
		case INTEGER: 
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return dvalue > iod.doubleValue();
		default: throw failure();
		}	
	}
	
        @Override
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case LONG:   
		case INTEGER: 
		case DECIMAL: 
		case FLOAT: 
		case DOUBLE: return dvalue >= iod.doubleValue();
		default: throw failure();
		}	
	}
	
        @Override
	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case LONG:   
		case INTEGER:   
		case DECIMAL:
		case FLOAT:
		case DOUBLE: return dvalue == iod.doubleValue();
		
		//case UNDEF:
                case URI:
		case BLANK: return false;
                         		
		default: throw failure();
		}	
	}
	
	
        @Override
        public String getNormalizedLabel(){
		return getNormalizedLabel(Double.toString(dvalue));		
	}
        
	public String getNormalizedLabel2(){
		String label = Double.toString(dvalue);
		String str = infinity(label);
		return (str==null) ? label : str;
	}
        
        public String getNormalizedLabel(String label){
		String str = infinity(label);
		return (str==null) ? label : str;
	}
	
	static String infinity(String label){
		if (label.equals("Infinity") || label.equals("+Infinity")) {
			return  "INF";
		} else if (label.equals("-Infinity")) {
			return "-INF";
		}
		return null;
	}
	
//	public static String getNormalizedLabel(String label){
//		String str = infinity(label);
//		if (str!=null) return str;
//		double v = Double.parseDouble(label);
//		double floor = Math.floor(v);
//		if(! DatatypeMap.SEVERAL_NUMBER_SPACE &&
//			 floor == v && v <= Long.MAX_VALUE && v >= Long.MIN_VALUE){
//			return Long.toString((long)floor);
//		}
//		else{
//			return Double.toString(v);
//		}
//	}
	
        @Override
	public String getLowerCaseLabel(){
		return Double.toString(dvalue);
	}

	
}