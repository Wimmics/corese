package fr.inria.corese.sparql.datatype;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * Edge reference datatype for edge reference node
 * <<s p o>> q v -> edge(s p o t)  t q v 
 * where t is reference node
 * e.referenceNode = t
 * t.edge = edge(s p o t)
 */
public class CoreseTriple extends CoreseResource {
    static int code = TRIPLE;
    
    private Edge edge;

    public CoreseTriple(String value) {
        super(value);
    }
    
    @Override
    public int getCode() {
        return code;
    }
    
    @Override
    public NodeKind getNodeKind() {
        return NodeKind.TRIPLE;
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
    public String getPrettyLabel() {
        if (getEdge() == null) {
            return getLabel();
        }
        return toStringTriple();
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
    public String toString() {
        if (getEdge() == null || ! DatatypeMap.DISPLAY_AS_TRIPLE) {
            return getLabel();
        }
        return toStringTriple();
    }
    
    public String toStringTriple() {        
        Edge e = getEdge();
        return String.format("<<%s %s %s>>", e.getSubjectValue(), e.getPredicateValue(), e.getObjectValue());
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
        return getLabel().equals(dt.getLabel());
    }
    
   
    @Override
    public int compare(IDatatype dt) throws CoreseDatatypeException {
       if (getEdge()!=null && dt.isTripleWithEdge()) {
            return getEdge().compareWithoutGraph(dt.getEdge());
        }
        throw failure();
    }
    
    
    @Override
    public boolean equalsWE(IDatatype dt) throws CoreseDatatypeException {
        if (dt.isTriple()) {
            return compare(dt) == 0;
        }
        return false;
    }
 
    @Override
    public boolean less(IDatatype dt) throws CoreseDatatypeException {
        return compare(dt) < 0 ;
    }

    @Override
    public boolean lessOrEqual(IDatatype dt) throws CoreseDatatypeException {
        return compare(dt) <= 0 ;
    }

    @Override
    public boolean greater(IDatatype dt) throws CoreseDatatypeException {
        return compare(dt) > 0 ;
    }

    @Override
    public boolean greaterOrEqual(IDatatype dt) throws CoreseDatatypeException {
        return compare(dt) >= 0 ;
    }
       
}
