package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

public class CoreseDateTimeOld extends CoreseDateOld {
        static int code = DATETIME;
	
	static final CoreseURI datatype=new CoreseURI(RDF.xsddateTime);
	
	public CoreseDateTimeOld(String label) throws CoreseDatatypeException{
		super(label);
	}
	
	public CoreseDateTimeOld()throws CoreseDatatypeException{
		super();
	}
	
        @Override
	public IDatatype getDatatype(){
		return datatype;
	}
        
    @Override
    public int getCode() {
        return code;
    }

}
