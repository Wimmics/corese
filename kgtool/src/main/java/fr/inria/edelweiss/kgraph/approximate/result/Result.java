package fr.inria.edelweiss.kgraph.approximate.result;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.approximate.aggregation.ApproximateStrategy;
import fr.inria.edelweiss.kgraph.approximate.aggregation.Priority;

/**
 * Result.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 oct. 2015
 */
public class Result {

    //private final IDatatype e1, e2;
    private final Node node;
    private double similarity = -1;
    private String algorithms = "";

    public Result(Node node, String algorithms, double sim) {
        this(node, algorithms);
        this.similarity = sim;
    }

        public Result(Node node, String algorithms) {
        this.node = node;
        this.algorithms = algorithms;
    }
        
    public void merge(Node node, String alg, double sim) {

        double[] w = Priority.getWeightByAlgorithm(this.algorithms, alg);

        this.similarity = (w[0] * this.similarity + w[1] * sim);
        this.algorithms = this.algorithms + ApproximateStrategy.SEPERATOR + alg;

    }

    public Node getNode() {
        return node;
    }

    public String getAlgorithms() {
        return algorithms;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return "[" + node + ", " + similarity + ", " + algorithms + "]";
    }
}
