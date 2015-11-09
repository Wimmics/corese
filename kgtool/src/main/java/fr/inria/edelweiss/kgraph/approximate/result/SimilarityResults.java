package fr.inria.edelweiss.kgraph.approximate.result;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import java.util.HashMap;
import java.util.Map;

/**
 * Results.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 29 oct. 2015
 */
public class SimilarityResults {

    //private Map<Expr, Map<Node, Result>> all;
    private final Map<Key, Map<Node, Result>> all;
    //todo!!
    //need to be modified
    //becaseu there may exist several instances at the same time in the environment
    private static SimilarityResults sr = new SimilarityResults();

    public SimilarityResults() {
        this.all = new HashMap<Key, Map<Node, Result>>();
    }

    public static SimilarityResults getInstance() {
        return sr;
    }

    public void add(Key var, Node node, String alg, double sim) {
        if (all.containsKey(var)) {
            Map<Node, Result> mr = all.get(var);
            if (mr.containsKey(node)) {
                mr.get(node).merge(node, alg, sim);
            } else {
                Result r = new Result(node, alg, sim);
                mr.put(node, r);
            }
        } else {
            Map<Node, Result> m = new HashMap<Node, Result>();
            Result r = new Result(node, alg, sim);
            m.put(node, r);
            all.put(var, m);
        }
    }

    public void add(Key var, Result r) {
        add(var, r.getNode(), r.getAlgorithms(), r.getSimilarity());
    }

    public Double getSimilarity(Key key, Node dt, String algs) {
        Result r = this.get(key, dt);
        return (r != null && r.getAlgorithms().equalsIgnoreCase(algs)) ? r.getSimilarity() : null;
    }

    public Double getSimilarity(Key key, IDatatype dt) {
        Result r = this.get(key, dt);
        return (r == null) ? null : r.getSimilarity();
    }

    public Double getSimilarity(String var, IDatatype dt) {
        return this.getSimilarity(Key.create(var), dt);
    }

    public Result get(Key key, Node dt) {
        if (this.all.containsKey(key)) {
            if (this.all.get(key).containsKey(dt)) {
                return this.all.get(key).get(dt);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Key, Map<Node, Result>> entrySet : all.entrySet()) {
            Key key = entrySet.getKey();
            Map<Node, Result> value = entrySet.getValue();
            sb.append(key).append("\n");
            for (Result v : value.values()) {
                sb.append("\t" + v.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static Double aggregate(Mapping m) {
        return 0d;
    }

    public static Double aggregate(Mappings m) {
        return 0d;
    }
}
