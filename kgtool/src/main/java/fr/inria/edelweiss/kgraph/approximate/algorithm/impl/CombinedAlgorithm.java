package fr.inria.edelweiss.kgraph.approximate.algorithm.impl;

import fr.inria.edelweiss.kgraph.approximate.strategy.Priority;
import fr.inria.edelweiss.kgraph.approximate.strategy.AlgType;
import fr.inria.edelweiss.kgraph.approximate.algorithm.ISimAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.algorithm.impl.BaseAlgorithm;
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
     * Construct a combined algorithm using given list of algorithms and weights
     *
     * @param algs
     */
    public CombinedAlgorithm(List<ISimAlgorithm> algs) {
        this(algs, null);
    }

    public CombinedAlgorithm(List<ISimAlgorithm> algs, double[] weights2) {
        super(AlgType.mult);
        this.algs = algs;
        this.weights = weights2;
    }

    @Override
    public double calculate(String s1, String s2) {
        if (algs.isEmpty()) {
            return NA;
        }

        double[] similarity = new double[algs.size()];
        for (int i = 0; i < this.algs.size(); i++) {
            similarity[i] = this.algs.get(i).calculate(s1, s2);
        }

        return Priority.sum(similarity, this.weights);
    }
}
