package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An abstract class, super-class of all CoreseNumber datatype; it defines static methods
 * <br>
 * @author Olivier Savoie
 */

public abstract class CoreseNumber extends CoreseDatatype {
	static int code=NUMBER;
	protected double dvalue = 0;
	protected String normalizedLabel = "";
	
	protected CoreseNumber(String normalizedLabel){
		this.normalizedLabel = normalizedLabel;
		dvalue = Double.parseDouble(normalizedLabel); //.doubleValue();
	}
	
	CoreseNumber(double val){
		dvalue = val;
	}
	
	/**
	 * Cast a number to integer means take the integer part,
	 * not just parsing the string label<br />
	 * Cast a number to a boolean is always allowed and should always give a result
	 */
	public IDatatype cast(IDatatype target, IDatatype javaType) {
		if (target.getLabel().equals(RDF.xsdinteger)){
			return new CoreseInteger(getlValue());
		}
		else if (target.getLabel().equals(RDF.xsdboolean)){
			//return new CoreseInteger(getlValue());
			if (getlValue() == 0)      return CoreseBoolean.FALSE;
			else if (getlValue() == 1) return CoreseBoolean.TRUE;
			else return null;
		}
		else return super.cast(target, javaType);
	}
	
	
	
	public boolean isNumber(){
		return true;
	}
	
	public boolean isTrue() {
		return dvalue != 0;
	}
	
	public boolean isTrueAble() {
		return true;
	}
	
	
	public  int getCode(){
		return code;
	}
	
	public IDatatype getDatatype() {
		return datatype;
	}
	
	public double getdValue(){
		return dvalue;
	}
	
	public long getlValue(){
		return (long) dvalue;
	}
	
	public static String getNormalizedLabel(String label){
		return "";
	}
	
	
	
	
}