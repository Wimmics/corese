package fr.inria.edelweiss.kgraph.approximate.algorithm;

import fr.inria.edelweiss.kgraph.approximate.strategy.AlgType;

/**
 * Interface for implementing the similarity measurement algorithms
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 27 août 2015
 */
public interface ISimAlgorithm {

    public final static int NA = Integer.MIN_VALUE;//not calculated

    public final static double MIN = 0.0d;
    public final static double MAX = 1.0d;

    /**
     * Calculate the similarity between strings s1 and s2
     * 
     * @param s1
     * @param s2
     * @return 
     */
    double calculate(String s1, String s2);

    /**
     * Return the type of the algorithm
     * 
     * @return 
     */
    //AlgType getType();
}
