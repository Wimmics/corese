package fr.inria.edelweiss.kgraph.approximate.algorithm.impl;

import fr.inria.edelweiss.kgraph.approximate.strategy.AlgType;
import fr.inria.edelweiss.kgraph.approximate.algorithm.ISimAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.algorithm.Utils;

/**
 * Base Algorithm
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 24 sept. 2015
 */
public class BaseAlgorithm implements ISimAlgorithm {
    private final AlgType type;

    /**
     * Constructor with default type 'empty'
     */
    BaseAlgorithm() {
        this(AlgType.empty);
    }

    /**
     * Constructor with given type
     * @param type 
     */
    public BaseAlgorithm(AlgType type) {
        this.type = type;
    }

    /**
     * Return type of algorithm
     * 
     * @return 
     */
    public AlgType getType() {
        return this.type;
    }

    @Override
    public double calculate(String s1, String s2) {
        Utils.msg("Base-" + this.getType().name(), s1, s2, MIN);
        return MIN;
    }
}
