package fr.inria.corese.sparql.datatype;

import fr.inria.corese.kgram.api.core.Pointerable;

/**
 * Encapsulate an Object which is not a Pointerable
 * To be used in CoresePointer
 * Use case: DatatypeMap.createObject(object) on any Object
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class PointerObject implements Pointerable {
    
    Object object;
    
    public PointerObject() {}
    
    public PointerObject(Object obj) {
        object = obj; 
    }
    
    @Override
    public String getDatatypeLabel() {
        return String.format("[%s:%s]", object.getClass().getName(), object.toString());
    }
    
    @Override
    public String toString() {
        return getDatatypeLabel();
    }
    
    @Override
    public Object getPointerObject() {
        return object;
    }
    
    @Override
    public Iterable getLoop() {
        if (object instanceof Iterable) {
            return (Iterable) object;
        }
        return empty;
    }
    
}
