package fr.inria.corese.core.approximate.strategy;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration of types of similarity measurement algorithms
 *
 * @author fsong
 */
public enum AlgType {

    empty,//no algorithm implemented
    ng, //n-gram
    jw, //jaro-winkler (edit distance)
    ch, //class hierarchy (empty now)
    wn, //wordnet
    eq, //equality
    mult; //combiend algorithm

    /**
     * Return the list of all types of single algorithms
     *
     * @return
     */
    public static List<AlgType> allValues() {
        return Arrays.asList(new AlgType[]{ng, jw, ch, wn, eq});
    }
}
