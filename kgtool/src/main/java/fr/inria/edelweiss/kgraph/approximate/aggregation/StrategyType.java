package fr.inria.edelweiss.kgraph.approximate.aggregation;

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
    URI, URI_LABEL, URI_COMMENT,
    //PROPERTY_DR, 
    PROPERTY_EQUALITY, URI_EQUALITY,
    CLASS_HIERARCHY,
    LITERAL_SS, LITERAL_LEX; //literal

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
