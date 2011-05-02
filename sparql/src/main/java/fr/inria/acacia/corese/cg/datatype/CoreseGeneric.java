package fr.inria.acacia.corese.cg.datatype;

import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * 
 * Generic datatype for other such as dayTimeDuration
 */
public class CoreseGeneric extends CoreseStringableImpl {
    static final Hashtable<String, CoreseURI> hdt = new Hashtable<String, CoreseURI>(); // datatype name -> CoreseURI datatype

    CoreseURI datatype;
	
	public CoreseGeneric(String label, String uri){
		super(label);
		setDatatype(uri);
	}
	
	public CoreseGeneric(String label){
		super(label);
	}
	
	public void setDatatype(String uri){
		CoreseURI dt =  hdt.get(uri);
	    if (dt == null){
	      dt = new CoreseURI(uri);
	      hdt.put(uri, dt);
	    }
	    datatype = dt;
	}

	public IDatatype getDatatype(){
		return datatype;
	}
	
	public boolean equals(IDatatype dt) throws CoreseDatatypeException {
		if (dt.getDatatype() == null) throw failure();
		if (! getDatatype().sameTerm(dt.getDatatype())) throw failure();
		return getLabel().equals(dt.getLabel());
	}
	
	
}
