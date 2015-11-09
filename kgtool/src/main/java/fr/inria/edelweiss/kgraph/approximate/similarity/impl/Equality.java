package fr.inria.edelweiss.kgraph.approximate.similarity.impl;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;

/**
 * Equality
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 7 oct. 2015
 */
public class Equality extends BaseAlgorithm {

    public Equality() {
        super(AlgType.eq);
    }

    @Override
    public double calculate(String s1, String s2) {
        Utils.show("Eq", s1, s2, MAX);
        return MAX;
    }
}
