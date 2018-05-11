package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.core.TripleStore;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public abstract class PointerObject implements Pointerable {
    static final ArrayList empty = new ArrayList(0);

    @Override
    public int pointerType() {
        return UNDEF_POINTER;
    }

    @Override
    public Mappings getMappings() {
        return null;    
    }

    @Override
    public Mapping getMapping() {
        return null;
    }

    @Override
    public Edge getEdge() {
        return null;
    }
    
     @Override
    public Query getQuery() {
        return null;
    }
    
    @Override
    public TripleStore getTripleStore(){
        return null;
    }
    
    @Override
    public int size(){
        return 0;
    }
    
    @Override
    public Object getValue(String var, int n){
        return null;
    }
    
    @Override
    public Iterable getLoop(){
        return empty;
    }

}
