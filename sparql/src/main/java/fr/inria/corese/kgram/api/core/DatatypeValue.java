
package fr.inria.corese.kgram.api.core;

import java.math.BigDecimal;
import java.util.List;

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

    BigDecimal decimalValue();
    
    Object objectValue();
    
    String getDatatypeURI();
    
    String getLang();
    
    List getValueList();
    
    boolean isURI();
    
    boolean isBlank();
    
    boolean isLiteral();
    
    boolean isNumber();
    
    boolean isBoolean();
    
    boolean isUndefined();
    
    boolean isExtension();
    
    Object getNodeObject();
}
