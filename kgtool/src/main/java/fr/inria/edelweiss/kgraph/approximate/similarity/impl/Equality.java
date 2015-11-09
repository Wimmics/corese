package fr.inria.edelweiss.kgraph.approximate.similarity.impl;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;

/**
 * Equality.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 7 oct. 2015
 */
public class Equality extends BaseAlgorithm {

    public Equality(AlgType type) {
        super(type);
    }

    @Override
    public double calculate(String s1, String s2) {
        Utils.show("Eq", s1, s2, SIM_MAX);
        return SIM_MAX;
    }

}
