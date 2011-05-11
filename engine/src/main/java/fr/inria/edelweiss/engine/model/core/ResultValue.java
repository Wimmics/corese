package fr.inria.edelweiss.engine.model.core;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IPath;
import fr.inria.acacia.corese.api.IResultValue;

public class ResultValue implements IResultValue {
	
	IDatatype dt;
	
	ResultValue(IDatatype dt){
		this.dt = dt;
	}
	
	public static ResultValue create(IDatatype dt){
		return new ResultValue(dt);
	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return dt.getLabel();
	}

	@Override
	public String getDatatypeURI() {
		// TODO Auto-generated method stub
		return dt.getDatatypeURI();
	}

	@Override
	public IDatatype getDatatypeValue() {
		// TODO Auto-generated method stub
		return dt;
	}

	@Override
	public String getLang() {
		// TODO Auto-generated method stub
		return dt.getLang();
	}

	@Override
	public boolean isBlank() {
		// TODO Auto-generated method stub
		return dt.isBlank();
	}

	@Override
	public boolean isURI() {
		// TODO Auto-generated method stub
		return dt.isURI();
	}

	@Override
	public boolean isLiteral() {
		// TODO Auto-generated method stub
		return dt.isLiteral();
	}

	@Override
	public boolean isNumber() {
		// TODO Auto-generated method stub
		return dt.isNumber();
	}

	@Override
	public int getIntegerValue() {
		// TODO Auto-generated method stub
		return dt.getIntegerValue();
	}

	@Override
	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return dt.getDoubleValue();
	}

	@Override
	public IPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPath() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isArray() {
		// TODO Auto-generated method stub
		return dt.isArray();
	}

}
