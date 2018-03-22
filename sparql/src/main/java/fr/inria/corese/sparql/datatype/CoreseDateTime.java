package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

public class CoreseDateTime extends CoreseDate {
        static int code = DATETIME;
	
	static final CoreseURI datatype=new CoreseURI(RDF.xsddateTime);
	
	public CoreseDateTime(String label) throws CoreseDatatypeException{
		super(label);
	}
	
	public CoreseDateTime()throws CoreseDatatypeException{
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
