
package fr.inria.edelweiss.kgram.api.core;

/**
 *
 * Olivier Corby - Wimmics, Inria, I3S, 2015
 */
public interface DatatypeValue {
    
    String stringValue();

    boolean booleanValue();
    
    int intValue();
    
    long longValue();

    double doubleValue();
    
    float floatValue();
    
    Object objectValue();
    
    String getDatatypeURI();
    
    String getLang();
    
    boolean isURI();
    
    boolean isBlank();
    
    boolean isLiteral();
}
