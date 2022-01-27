package fr.inria.corese.sparql.datatype;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 *
 */
public class CoreseTriple extends CoreseBlankNode {

    private Edge edge;

    public CoreseTriple(String value) {
        super(value);
    }

    @Override
    public boolean isTriple() {
        return true;
    }
    
    @Override
    public boolean isTripleWithEdge() {
        return getEdge()!=null;
    }

    @Override
    public Edge getEdge() {
        return edge;
    }

    @Override
    public void setEdge(Edge e) {
        edge = e;
    }

    @Override
    public boolean sameTerm(IDatatype dt) {
        if (dt.isTriple()) {
            return sameTermTriple(dt);
        }
        return super.sameTerm(dt);
    }

    boolean sameTermTriple(IDatatype dt) {        
        if (getEdge() != null && dt.getEdge() != null) {            
            return getEdge().sameTermWithoutGraph(dt.getEdge());
        }
        return super.sameTerm(dt);
    }

    @Override
    public IDatatype eq(IDatatype dt) {
        try {
            if (dt.isTriple()) {
                return compareTriple(dt) == 0 ? TRUE : FALSE;
            }
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

  
    @Override
    public boolean less(IDatatype dt) throws CoreseDatatypeException {
        if (getEdge()!=null && dt.isTripleWithEdge()) {
            return getEdge().compareWithoutGraph(dt.getEdge()) < 0;
        }
        return super.less(dt);        
    }

    @Override
    public boolean lessOrEqual(IDatatype dt) throws CoreseDatatypeException {
        if (getEdge() != null && dt.isTripleWithEdge()) {
            return getEdge().compareWithoutGraph(dt.getEdge()) <= 0;
        }
        return super.lessOrEqual(dt);
    }

    @Override
    public boolean greater(IDatatype dt) throws CoreseDatatypeException {
        if (getEdge() != null && dt.isTripleWithEdge()) {
            return getEdge().compareWithoutGraph(dt.getEdge()) > 0;
        }
        return super.greater(dt);
    }

    @Override
    public boolean greaterOrEqual(IDatatype dt) throws CoreseDatatypeException {
        if (getEdge() != null && dt.isTripleWithEdge()) {
            return getEdge().compareWithoutGraph(dt.getEdge()) >= 0;
        }
        return super.greaterOrEqual(dt);
    }
    
    
    

    boolean eqTriple(IDatatype dt) {
        if (getEdge() != null && dt.getEdge() != null) {
            return getEdge().equalsWithoutGraph(dt.getEdge());
        }
        return false;
    }

    @Override
    public int compareTriple(IDatatype dt) throws CoreseDatatypeException {
        if (getEdge() != null && dt.getEdge() != null) {
            return getEdge().compareWithoutGraph(dt.getEdge());
        }
        throw failure;
    }
}
