package fr.inria.edelweiss.kgengine;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IPath;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.edelweiss.kgram.api.core.Node;

public class QueryValue implements IResultValue {
	
	Node node;
	IDatatype dt;
	
	QueryValue(Node n){
		node = n;
		dt = (IDatatype) node.getValue();
	}


	public String getDatatypeURI() {

		return dt.getDatatypeURI();
	}

	
	public IDatatype getDatatypeValue() {
		
		return dt;
	}

	
	public double getDoubleValue() {
		
		return dt.getDoubleValue();
	}

	
	public int getIntegerValue() {
		
		return dt.getIntegerValue();
	}

	
	public String getLang() {
		
		return dt.getLang();
	}

	
	public IPath getPath() {
		
		return null;
	}

	
	public String getStringValue() {
		
		return dt.getLabel();
	}

	
	public boolean isArray() {
		
		return false;
	}

	
	public boolean isBlank() {
		
		return dt.isBlank();
	}

	
	public boolean isLiteral() {
		
		return dt.isLiteral();
	}

	
	public boolean isNumber() {
		
		return dt.isNumber();
	}

	
	public boolean isPath() {
		
		return false;
	}

	
	public boolean isURI() {
		
		return dt.isURI();
	}

}
