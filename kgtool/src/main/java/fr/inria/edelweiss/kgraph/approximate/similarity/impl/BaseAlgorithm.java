package fr.inria.edelweiss.kgraph.approximate.similarity.impl;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;
import fr.inria.edelweiss.kgraph.approximate.similarity.ISimAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;

/**
 * Base Algorithm
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 24 sept. 2015
 */
public class BaseAlgorithm implements ISimAlgorithm {

    public static double THRESHOLD = 0.0;
    private final AlgType type;

    public BaseAlgorithm() {
        this(AlgType.empty);
    }

    public BaseAlgorithm(AlgType type) {
        this.type = type;
    }

    public AlgType getType() {
        return this.type;
    }

    @Override
    public double calculate(String s1, String s2) {
        Utils.show("Base-" + this.getType().name(), s1, s2, MIN);
        return MIN;
    }
}
