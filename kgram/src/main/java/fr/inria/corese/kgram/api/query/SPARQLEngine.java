package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

/**
 *
 * @author corby
 */
public interface SPARQLEngine {
        
    Mappings eval(Query q, Mapping m, Producer p);
    
    //Query load(String path);
    
    void getLinkedFunction(String uri);
    
}
