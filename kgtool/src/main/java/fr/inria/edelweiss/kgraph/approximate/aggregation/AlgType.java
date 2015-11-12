package fr.inria.edelweiss.kgraph.approximate.aggregation;

import java.util.Arrays;
import java.util.List;

/**
 * Enueration of types of similarity measurement algorithms
 *
 * @author fsong
 */
public enum AlgType {

    empty,//no algorithm implemented
    ng, jw, ch, wn,
    // dr, eq,
    mult; //combiend algorithm //combiend algorithm 

    /**
     * Return the list of all types of single algorithms
     *
     * @return
     */
    public static List<AlgType> allValues() {
        return Arrays.asList(new AlgType[]{ng, jw, ch, wn});
    }
}
