package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import java.util.List;

/**
 *
 * @author corby
 */
public interface Hierarchy {
        
    List<String> getSuperTypes(DatatypeValue object, DatatypeValue type);
    
    void defSuperType(DatatypeValue type, DatatypeValue sup);
        
}
