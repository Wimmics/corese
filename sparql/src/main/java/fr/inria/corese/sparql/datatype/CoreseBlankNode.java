package fr.inria.corese.sparql.datatype;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Pointerable;
import org.eclipse.rdf4j.model.BNode;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.rdf4j.CoreseDatatypeToRdf4jValue;


/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:anyURI datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseBlankNode extends CoreseResource {
	static int  code=BLANK;
        boolean variable = false;
        private boolean triple = false;
        private Pointerable pointerObject;
	
	
	public CoreseBlankNode(String value) {
		super(value);
	}
	
        @Override
	public int getCode() {
		return code;
	}
	
        @Override
	public boolean isBlank() {
		return true;
	}
	
        @Override
	public boolean isConstant() {
		return false;
	}
        
        @Override
	public boolean isVariable() {
            return variable;
	}
        
        @Override
        public void setVariable(boolean b){
            variable = b;
        }
	
        @Override
	public int compare(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel());
		}
		throw failure();
	}
		
        @Override
	public boolean less(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) < 0;
		}
		throw failure();
	}
	
        @Override
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) <= 0;
		}
		throw failure();
	}
	
        @Override
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) > 0;
		}
		throw failure();
	}
	
        @Override
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) >= 0;
		}
		throw failure();
	}
	
        @Override
	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case BLANK: return getLabel().equals(iod.getLabel()) ;
		}
		return false;
	}
	
	@Override 
	public int hashCode() {
		return getLabel().hashCode();
	}
        
        @Override
    public boolean sameTerm(IDatatype dt) {
        if (isTriple() && dt.isTriple()) {
            return sameTermTriple(dt) ;
        }
        return super.sameTerm(dt);
    }
    
    boolean sameTermTriple(IDatatype dt) {
        Pointerable obj1 = getPointerObject();
        Pointerable obj2 = dt.getPointerObject();
        if (obj1 != null && obj2 !=null) {
            Edge e1 = obj1.getEdge();
            Edge e2 = obj2.getEdge();
            return e1.sameTerm(e2);
        }
        return super.sameTerm(dt);
    }
    

        
    @Override
    public IDatatype eq(IDatatype dt) {
        if (isTriple() && dt.isTriple()) {
            return eqTriple(dt) ? TRUE : FALSE;
        }
        try {
            return (this.equalsWE(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }
        
    @Override
    public IDatatype ne(IDatatype dt) {
        IDatatype res = eq(dt);
        if (res == null) {
            return null;
        }
        return res.booleanValue() ? FALSE : TRUE;
    }
    
    boolean eqTriple(IDatatype dt) {
        Pointerable obj1 = getPointerObject();
        Pointerable obj2 = dt.getPointerObject();
        if (obj1 != null && obj2 !=null) {
            Edge e1 = obj1.getEdge();
            Edge e2 = obj2.getEdge();
            return e1.equals(e2);
        }
        return false;
    }
        

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CoreseBlankNode other = (CoreseBlankNode) obj;
		return getLabel().equals(other.getLabel());
	}

	@Override
	public BNode getRdf4jValue() {
		return CoreseDatatypeToRdf4jValue.convertBNode(this);
	}

    public Pointerable getPointerObject() {
        return pointerObject;
    }

    public void setPointerObject(Pointerable pointerObject) {
        this.pointerObject = pointerObject;
    }

    public boolean isTriple() {
        return triple;
    }

    public void setTriple(boolean triple) {
        this.triple = triple;
    }
	
}
