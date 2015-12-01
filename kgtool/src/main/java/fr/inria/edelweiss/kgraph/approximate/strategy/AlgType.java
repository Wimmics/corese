package fr.inria.edelweiss.kgraph.approximate.strategy;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration of types of similarity measurement algorithms
 *
 * @author fsong
 */
public enum AlgType {

    empty,//no algorithm implemented
    ng, jw, ch, wn, eq,
    // dr, eq,
    mult; //combiend algorithm //combiend algorithm 

    /**
     * Return the list of all types of single algorithms
     *
     * @return
     */
    public static List<AlgType> allValues() {
        return Arrays.asList(new AlgType[]{ng, jw, ch, wn, eq});
    }
}
