package fr.inria.edelweiss.kgraph.approximate.result;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;
import static fr.inria.edelweiss.kgraph.approximate.similarity.Utils.msg;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.BaseAlgorithm;
import java.util.ArrayList;
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
    private List<Variable> vars = new ArrayList<Variable>();

    private int counter = 1;

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

    public void remove(Key var) {
        if (this.all.containsKey(var)) {
            this.all.remove(var);
        }
    }

    public void remove(Key var, Node node) {
        if (this.all.containsKey(var)) {
            Map<Node, Value> mr = all.get(var);
            if (mr.containsKey(node)) {
                mr.remove(node);
            }
        }
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

    public Double aggregate(Mapping map) {
        return aggregate(map, null, null);
    }

    public Double aggregate(Mapping map, List<Node> selectNodes, StringBuilder sb) {
        if (this.vars.size() < 1) {
            return Double.NaN;
        }

        double sim = 1;
        StringBuilder ssb = (sb == null) ? null : new StringBuilder(counter++ + ":\t");

        if (selectNodes != null) {
            //get the query nodes
            for (Node qNode : selectNodes) {
                Node node = map.getNode(qNode);
                if (node == null) {
                    return Double.NaN;
                }
                if (ssb != null) {
                    ssb.append(qNode).append(" = ").append(node).append("; ");
                }
            }
        }

        //calculate similarity
        for (Variable var : this.vars) {
            Node node = map.getNode(var);
            if (node == null) {
                return Double.NaN;
            }

            Double s = SimilarityResults.getInstance().getSimilarity(var.getLabel(), (IDatatype) node.getValue());
            if (s != null) {
                sim *= s;
            } else {
                return Double.NaN;
            }
        }

        if (ssb != null) {
            ssb.append("sim = ").append(Utils.format(sim)).append("; ").append("\n");
        }

        if (sim >= BaseAlgorithm.THRESHOLD && sb != null) {
            sb.append(ssb);
        }
        return sim;
    }

    public void aggregate(Mappings mappings) {
        StringBuilder sb = new StringBuilder("********** Mapping Similarity ************\n");
        List<Node> select = mappings.getQuery().getSelect();
        boolean isSelect = mappings.getQuery().getSelect() != null;
        for (Mapping map : mappings) {
            List<Node> nodes = isSelect ? select : Arrays.asList(map.getQueryNodes());
            aggregate(map, nodes, sb);
        }

        msg(sb.toString(), true);
    }

    public void addVariable(Variable v) {
        this.vars.add(v);
    }

    public List<Variable> getVariables() {
        return this.vars;
    }
}
