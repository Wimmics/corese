package fr.inria.edelweiss.kgraph.approximate.result;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structure: Key -> (node -> Value)
 *
 * Key: (?var, <uri>) Value: (node, similarity, algs)
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 29 oct. 2015
 */
public class SimilarityResults {

    private final Map<Key, Map<Node, Value>> all;
    private static SimilarityResults sr = new SimilarityResults();

    public SimilarityResults() {
        this.all = new HashMap<Key, Map<Node, Value>>();
    }

    //todo
    //should not be static, need to be changed!!!
    public static SimilarityResults getInstance() {
        return sr;
    }

    public void add(Key var, Node node, String alg, double sim) {
        if (all.containsKey(var)) {
            Map<Node, Value> mr = all.get(var);
            if (mr.containsKey(node)) {
                mr.get(node).merge(node, alg, sim);
            } else {
                Value r = new Value(node, alg, sim);
                mr.put(node, r);
            }
        } else {
            Map<Node, Value> m = new HashMap<Node, Value>();
            Value r = new Value(node, alg, sim);
            m.put(node, r);
            all.put(var, m);
        }
    }

    public void add(Key var, Value r) {
        add(var, r.getNode(), r.getAlgorithms(), r.getSimilarity());
    }

    public Double getSimilarity(Key key, Node dt, String algs) {
        Value r = this.get(key, dt);
        return (r != null && r.getAlgorithms().equalsIgnoreCase(algs)) ? r.getSimilarity() : null;
    }

    public Double getSimilarity(Key key, IDatatype dt) {
        Value r = this.get(key, dt);
        return (r == null) ? null : r.getSimilarity();
    }

    public Double getSimilarity(String var, IDatatype dt) {
        return this.getSimilarity(Key.create(var), dt);
    }

    public Value get(Key key, Node dt) {
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
        for (Map.Entry<Key, Map<Node, Value>> entrySet : all.entrySet()) {
            Key key = entrySet.getKey();
            Map<Node, Value> value = entrySet.getValue();
            sb.append(key).append("\n");
            for (Value v : value.values()) {
                sb.append("\t" + v.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static Double aggregate(Mapping map, List<Node> selectNodes) {
        return aggregate(map, selectNodes, null);
    }

    public static Double aggregate(Mapping map, List<Node> selectNodes, StringBuilder sb) {
        double sim = 1;

        for (Node qNode : selectNodes) {
            Node node = map.getNode(qNode);
            if (node == null) {
                continue;
            }
            if (sb != null) {
                sb.append(qNode).append(" = ").append(node).append("; ");
            }
            Double s = SimilarityResults.getInstance().getSimilarity(qNode.getLabel(), (IDatatype) node.getValue());
            if (s != null) {
                sim *= s;
            }
        }

        if (sb != null) {
            sb.append("sim = ").append(Utils.format(sim)).append("; ").append("\n");
        }
        return sim;
    }

    public static void aggregate(Mappings mappings) {
        StringBuilder sb = new StringBuilder("********** Mapping Similarity ************\n");
        List<Node> select = mappings.getQuery().getSelect();
        boolean isSelect = mappings.getQuery().getSelect() != null;
        for (Mapping map : mappings) {
            List<Node> nodes = isSelect ? select : Arrays.asList(map.getQueryNodes());
            aggregate(map, nodes, sb);
        }

        System.out.println(sb);
    }
}
