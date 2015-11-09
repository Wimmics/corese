package fr.inria.edelweiss.kgraph.approximate.similarity.impl;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;
import fr.inria.edelweiss.kgraph.approximate.aggregation.Priority;
import fr.inria.edelweiss.kgraph.approximate.similarity.ISimAlgorithm;
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
        super(AlgType.mult);
        this.algs = algs;
        this.weights = Priority.getWeightByAlgorithm(algs);
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
