package fr.inria.edelweiss.kgraph.approximate.strategy;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy types
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 7 oct. 2015
 */
public enum StrategyType {

    URI_LEX, 
    URI_WN,
    URI_EQUALITY,
    PROPERTY_EQUALITY, 
    CLASS_HIERARCHY,
    LITERAL_WN, LITERAL_LEX; //literal

    //1 URI: lexical level (ng-jw)
    //       semantic level (ssw-eq)
    //2 Property: dr, eq
    //3 hierarchy: ch
    //4 literals: lexical level(ng-jw)
    //            semantic level (sst)
    //***** strategy group ****
    public static List<StrategyType> allValues() {
        return Arrays.asList(StrategyType.values());
    }
}
