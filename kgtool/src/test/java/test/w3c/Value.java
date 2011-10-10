package test.w3c;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

class Value {
	String uri, value, datatype, lang;
	boolean isBlank=false, isURI=true;
	IDatatype dt;
	
	static Value createLiteral(String val, String dt, String lang){
		Value v = new Value();
		v.value = val;
		v.datatype= dt;
		v.lang = lang;
		v.isURI = false;
		return v;
	}
	
	static Value createURI(String uri){ 
		Value v = new Value();
		v.uri = uri;
		return v;
	}
	
	static Value createBlank(String uri){ 
		Value v = new Value();
		v.uri = uri;
		v.isBlank = true;
		v.isURI = false;
		return v;
	}
	
	public String toString(){
		if (isLiteral()){
			String str = value;
			if (datatype != null){
				str += "^^" + datatype;
			} 
			else if (lang != null){
				str += "@" + lang;
			}
			return str;
		}
		else return uri;
	}
	
	IDatatype getDatatypeValue(){
		return dt;
	}
	
	void setURI(String uri){
		this.uri = uri;
		createDatatype();
	}
	
	void setValue(String val){
		if (value == null){
			value = val;
		}
		else {
			value += val;
		}
		createDatatype();
	}
	
	void createDatatype(){
		if (isURI){
			dt = DatatypeMap.createResource(uri);
		}
		else if (isBlank){
			dt = DatatypeMap.createBlank(uri);
		}
		else {
			dt = DatatypeMap.createLiteral(value, datatype, lang);
		}
	}
	
	boolean isURI(){
		return isURI;
	}
	
	boolean isLiteral(){
		return ! isURI && ! isBlank;
	}
	
	boolean isBlank(){
		return isBlank;
	}
	
	String getDatatype(){
		return datatype ;
	}
	
	String getLang(){
		return lang;
	}
	
	String getURI(){
		return uri;
	}
	
	String getValue(){
		return value;
	}
	
}