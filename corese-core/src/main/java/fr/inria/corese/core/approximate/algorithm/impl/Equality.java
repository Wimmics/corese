package fr.inria.corese.core.approximate.algorithm.impl;

import fr.inria.corese.core.approximate.algorithm.Utils;
import static fr.inria.corese.core.approximate.strategy.AlgType.eq;

/**
 * Equality.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 17 nov. 2015
 */
public class Equality extends BaseAlgorithm {

    public Equality() {
        super(eq);
    }

    @Override
    public double calculate(String s1, String s2, String paramter) {
        double sim = MAX;
        Utils.msg("Eq", s1, s2, paramter, sim);
        return sim;
    }
}
