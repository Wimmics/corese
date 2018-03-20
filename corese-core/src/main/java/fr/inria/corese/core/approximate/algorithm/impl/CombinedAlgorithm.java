package fr.inria.corese.core.approximate.algorithm.impl;

import fr.inria.corese.core.approximate.strategy.Priority;
import fr.inria.corese.core.approximate.strategy.AlgType;
import fr.inria.corese.core.approximate.algorithm.ISimAlgorithm;
import java.util.List;

/**
 * Combined algorithm (composed of several algorithms)
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 27 août 2015
 */
public class CombinedAlgorithm extends BaseAlgorithm {

    private final List<ISimAlgorithm> algs;
    private final double[] weights;

    /**
     * Construct a combined algorithm using given list of algorithms and default weights
     *
     * @param algs
     */
    public CombinedAlgorithm(List<ISimAlgorithm> algs) {
        this(algs, null);
    }

    /**
     * Construct a combined algorithm using given list of algorithms and weights
     * @param algs
     * @param weights2 
     */
    public CombinedAlgorithm(List<ISimAlgorithm> algs, double[] weights2) {
        super(AlgType.mult);
        this.algs = algs;
        this.weights = weights2;
    }

    @Override
    public double calculate(String s1, String s2, String parameter) {
        if (algs.isEmpty()) {
            return NA;
        }

        //normally, the parameter needs to be processed beofre passing to each 
        //specific algorithm
        double[] similarity = new double[algs.size()];
        for (int i = 0; i < this.algs.size(); i++) {
            similarity[i] = this.algs.get(i).calculate(s1, s2, parameter);
        }

        return Priority.sum(similarity, this.weights);
    }
}
