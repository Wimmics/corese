package fr.inria.corese.kgram.api.core;

import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author corby
 */
public interface TripleStore {
    
    Node getNode(int n);
    IDatatype set(IDatatype key, IDatatype value);
    int size();
    
}
