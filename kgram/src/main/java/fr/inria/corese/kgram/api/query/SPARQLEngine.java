package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;

/**
 *
 * @author corby
 */
public interface SPARQLEngine {
        
    Mappings eval(Query q, Mapping m, Producer p) throws SparqlException;
    
    Mappings eval(Node gNode, Query q, Mapping m, Producer p) throws SparqlException;
        
    void getLinkedFunction(String uri) throws SparqlException;
    
    // within a lock where query processing has ended and where we can process another query
    boolean isSynchronized();
    
    void setSynchronized(boolean b);
    
}
