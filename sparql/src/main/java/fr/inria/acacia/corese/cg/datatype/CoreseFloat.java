package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:float datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseFloat extends CoreseDouble{
	static final CoreseURI datatype=new CoreseURI(RDF.xsdfloat);
	static final int code = FLOAT;
	
	public CoreseFloat(String value) {
		super(value);
	}
	
	public CoreseFloat(float value) {
		super(value);
	}
	
	public CoreseFloat(double value) {
		super(value);
	}
	
	public IDatatype getDatatype(){
		return datatype;
	}
	
	 public int getCode(){
			return code;
		}
	
	public static String getNormalizedLabel(String label){
		String str = infinity(label);
		if (str!=null) return str;
		
		float v = Float.parseFloat(label);
		double floor = Math.floor(v);
		
		if(! DatatypeMap.SEVERAL_NUMBER_SPACE &&
			floor == v && v <= Long.MAX_VALUE && v >= Long.MIN_VALUE){
			return Long.toString((long)floor);
		}
		else{
			return Float.toString(v);
		}
	}
	
	public boolean isFloat(){
		return true;
	}

}