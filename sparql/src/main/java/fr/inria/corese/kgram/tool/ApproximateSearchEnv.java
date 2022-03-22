package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import java.util.ArrayList;
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
public class ApproximateSearchEnv {

    private static int code = 0;
    private int id;
    private final Map<Key, Map<Node, Value>> all;
    
    public ApproximateSearchEnv() {
        this.id = code++;
        this.all = new HashMap<Key, Map<Node, Value>>();
    }

    public void add(Expr var, Node uri, Node node, String alg, double sim) {
        Key key = new Key(var, uri);

        if (all.containsKey(key)) {
            Map<Node, Value> value = all.get(key);
            if (!value.containsKey(node)) {
                Value r = new Value(node, alg, sim);
                value.put(node, r);
            }
        } else {
            Map<Node, Value> m = new HashMap<Node, Value>();
            Value r = new Value(node, alg, sim);
            m.put(node, r);
            all.put(key, m);
        }
    }

    public Double getSimilarity(Expr var, Node node, String algs) {
        Key key = new Key(var);
        Value r = this.get(key, node);
        return (r != null && r.getAlgorithms().equalsIgnoreCase(algs)) ? r.getSimilarity() : null;
    }

    public Double getSimilarity(Expr var, Node node) {
        Key key = new Key(var);
        Value r = this.get(key, node);
        return (r == null) ? null : r.getSimilarity();
    }

    private Value get(Key key, Node node) {
        if (this.all.containsKey(key)) {
            if (this.all.get(key).containsKey(node)) {
                return this.all.get(key).get(node);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Appx search [" + this.id + "]\n");
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

    /**
     * Aggregate and get value of similarity using all existing variables
     * 
     * @param env
     * @return 
     */
    public Double aggregate(Environment env) {
        List<Expr> lv = this.getVariables();
        return aggregate(env, lv);
    }

    /**
     * Aggreate with existing variables (excpet for the given variable)
     * @param env
     * @param var
     * @param sim
     * @return 
     */
    public Double aggregate(Environment env, Expr var, double sim) {
        List<Expr> lv = this.getVariables();
        if (lv.contains(var)) {
            lv.remove(var);
        }

        Double cb = (lv.isEmpty()) ? 1 : aggregate(env, lv);
        return cb * sim;
    }

    private Double aggregate(Environment env, List<Expr> lv) {
        if (lv.isEmpty()) {
            return 1.0;
        }

        double sim = 1;

        //calculate similarity
        for (Expr var : lv) {
            Node node = env.getNode(var);
            if (node == null) {
                continue;
            }

            Double s = this.getSimilarity(var, node.getValue());
            if (s != null) {
                sim *= s;
            }
        }
        return sim;
    }

    public List<Expr> getVariables() {
        List lv = new ArrayList<Expr>();
        for (Key k : this.all.keySet()) {
            lv.add(k.getVar());
        }

        return lv;
    }

    class Key {

        private final Expr var;
        private Node uri;

        public Key(Expr var, Node uri) {
            this(var);
            this.uri = uri;
        }

        public Key(Expr var) {
            this.var = var;
        }

        public Expr getVar() {
            return var;
        }

        public Node getUri() {
            return uri;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.var != other.var && (this.var == null || !this.var.equals(other.var))) {
                return false;
            }
            //only that var is equal is ok
            return true;
        }

        @Override
        public String toString() {
            return "Key{" + "var=" + var + ", uri=" + uri + '}';
        }
    }

    class Value {

        private final Node node;
        private double similarity = -1;
        private String algorithms = "";

        public Value(Node node, String algorithms, double sim) {
            this(node, algorithms);
            this.similarity = sim;
        }

        public Value(Node node, String algorithms) {
            this.node = node;
            this.algorithms = algorithms;
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
}
