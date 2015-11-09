package fr.inria.edelweiss.kgraph.approximate.similarity;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;

/**
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 27 août 2015
 */
public interface ISimAlgorithm {

    //public final static int SIM_NA = -1;//not applicable or error
    public final static int SIM_NC = -2;//not calculated


    public final static double SIM_MIN = 0.0d;
    public final static double SIM_MAX = 1.0d;

    double calculate(String s1, String s2);

    AlgType getType();
}
