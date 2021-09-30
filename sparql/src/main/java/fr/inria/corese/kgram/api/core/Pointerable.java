package fr.inria.corese.kgram.api.core;

import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.path.Path;
import java.util.ArrayList;

/**
 * Interface for objects that can be object of a CoresePointer
 * @author Olivier Corby - Wimmics Inria I3S - 2015
 */
public interface Pointerable extends Loopable {
    static final ArrayList empty = new ArrayList(0);
          
    default PointerType pointerType() {
        return PointerType.UNDEF;
    }
    
    default Object getPointerObject() {
        return this;
    }
    
    default Mappings getMappings() {
        return null;    
    }

    
    default Mapping getMapping() {
        return null;
    }
   
    default Edge getEdge() {
        return null;
    }
    
    default Node getNode() {
        return null;
    }
        
    default Query getQuery() {
        return null;
    }
    
    default Exp getStatement() {
        return null;
    }
    
    default Path getPathObject() {
        return null;
    }
       
    default TripleStore getTripleStore(){
        return null;
    }
       
    default int size(){
        return 0;
    }
       
    default Object getValue(String var, int n){
        return null;
    }
    
    
    @Override
    default Iterable getLoop(){
        return empty;
    }
    
    default String getDatatypeLabel() {
        return Integer.toString(hashCode());
    }
    
    default int compare(Pointerable obj) {
        return Integer.compare(hashCode(), obj.hashCode());
    }
   
}
