package fr.inria.corese.core.approximate.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Strategy types
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 7 oct. 2015
 */
public enum StrategyType {

    //URI
    URI_LEX, 
    URI_WN,
    URI_EQUALITY,
    PROPERTY_EQUALITY, 
    
    //class hierarchy
    CLASS_HIERARCHY,
    
    //literal
    LITERAL_WN, 
    LITERAL_LEX; 
        
    /**
     * Return all values
     * @return 
     */
    public static List<StrategyType> allValues() {
        return Arrays.asList(StrategyType.values());
    }   
}
