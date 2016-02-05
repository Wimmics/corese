package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
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
    public Entity getEntity() {
        return null;
    }
    
     @Override
    public Query getQuery() {
        return null;
    }
    
    @Override
    public Object getGraphStore(){
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
    
    public Iterable getLoop(){
        return empty;
    }

}
