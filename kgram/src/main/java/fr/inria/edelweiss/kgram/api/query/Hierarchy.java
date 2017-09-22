package fr.inria.edelweiss.kgram.api.query;

import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import java.util.List;

/**
 *
 * @author corby
 */
public interface Hierarchy {
        
    List<String> getSuperTypes(DatatypeValue object, DatatypeValue type);
        
}
