package fr.inria.edelweiss.kgraph.query;

import java.util.HashMap;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Match
 *
 * Draft subsumption for xxx rdf:type c:Person Exploits graph subClassOf
 * properties, use a cache
 *
 * TODO: Remove dichotomy on constant class in EdgeIndex if entailment does not
 * process rdfs:subClassOf TODO: ?x rdf:type ?c ?y rdf:type ?c
 *
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class MatcherImpl implements Matcher {

    Graph graph;
    Entailment entail;
    Cache table;
    int mode = SUBSUME;

    class BTable extends HashMap<Node, Boolean> {
    }

    class STable extends HashMap<Node, BTable> {
    }

    /**
     *
     * Cache: store subsumption between query and target node
     */
    class Cache {

        STable table;

        Cache(Query q) {
            table = new STable();
        }

        BTable getTable(Node q) {
            BTable bt = table.get(q);
            if (bt == null) {
                bt = new BTable();
                table.put(q, bt);
            }
            return bt;
        }

        Boolean get(Node q, Node t) {
            BTable bt = getTable(q);
            return bt.get(t);
        }

        void put(Node q, Node t, Boolean b) {
            BTable bt = getTable(q);
            bt.put(t, b);
        }
    }

    MatcherImpl() {
        //table = new Cache();
    }

    public static MatcherImpl create() {
        return new MatcherImpl();
    }

    public static MatcherImpl create(Graph g) {
        MatcherImpl m = new MatcherImpl();
        m.graph = g;
        m.entail = g.getEntailment();
        return m;
    }

    @Override
    public boolean match(Edge q, Edge r, Environment env) {

        if (graph.getProxy().isType(q)) { //(q.getLabel().equals(RDF.TYPE)){
            return matchType(q, r, env);
        }

        if (env.getQuery() != null && env.getQuery().isRelax(q)) {
            return matchType(q, r, env);
        }

        if (!q.getLabel().equals(Graph.TOPREL)
                && !q.getLabel().equals(r.getLabel())) {
            return false;
        }

        int max = q.nbNode();
        if (max > r.nbNode()) {
            return false;
        }
        for (int i = 0; i < max; i++) {
            Node qNode = q.getNode(i);
            Node node = r.getNode(i);
            if (!match(qNode, node, env)) {
                return false;
            }
        }
        return true;
    }

    boolean matchType(Edge q, Edge r, Environment env) {

        if (!match(q.getNode(0), r.getNode(0), env)) {
            return false;
        }

        Query query = env.getQuery();

        if (query != null && query.isRelax()) {
            return true;
        }

        int localMode = mode;
        if (query != null && query.getMode() != UNDEF) {
            localMode = query.getMode();
        }

        Node qnode = q.getNode(1);

        switch (localMode) {
            case STRICT:
                return match(qnode, r.getNode(1), env);
            case RELAX:
                return true;
        }


        if (qnode.isConstant() && entail != null) {

            if (match(qnode, r.getNode(1), env)) {
                return true;
            }
            if (entail.isTopClass(qnode)) {
                // ?x rdf:type rdfs:Resource
                return true;
            }

            Node gqnode = graph.getNode(qnode);
            if (gqnode == null) {
                return false;
            }
            switch (localMode) {

                case SUBSUME:
                case MIX:

//				if (entail.isTopClass(qnode)){
//					// ?x rdf:type rdfs:Resource
//					return true;
//				}


                    // if rdf:type is completed by subClassOf, skip this and perform std match
                    // if rdf:type is not completed by subClassOf, check whether r <: q
                    boolean b = false;

                    if (entail.isSubClassOfInference()) {
                        b = match(qnode, r.getNode(1), env);
                    } else {
                        b = isSubClassOf(r.getNode(1), gqnode, env);
                    }

                    if (!b && localMode == MIX) {
                        b = isSubClassOf(gqnode, r.getNode(1), env);
                    }
                    return b;


                case GENERAL:
                    return isSubClassOf(gqnode, r.getNode(1), env);
            }
        }

        return match(qnode, r.getNode(1), env);
    }

    /**
     * Store subsumption test in a cache for each query type and each target
     * type, store whether target subClassOf query the cache is bound to current
     * query (via the environment)
     */
    public boolean isSubClassOf(Node t, Node q, Environment env) {
        Cache table = getTable(env);
        Boolean b = table.get(q, t);
        if (b == null) {
            // PRAGMA: use graph because entail may be null (cf PluginImpl)			
            b = graph.isSubClassOf(t, q);
            table.put(q, t, b);
        }
        return b;
    }

    Cache getTable(Environment env) {
        Cache table = (Cache) env.getObject();
        if (table == null) {
            table = new Cache(env.getQuery());
            env.setObject(table);
        }
        return table;
    }

    boolean isSubClassOf2(Node t, Node q, Environment env) {
        boolean b = graph.isSubClassOf(t, q);
        return b;
    }

    @Override
    public boolean same(Node node, Node n1, Node n2, Environment env) {
        boolean b = n1.same(n2);
        if (b) {
            return true;
        }
        Query q = env.getQuery();
        if (q != null && q.isMatchBlank()
                && n1.isBlank() && n2.isBlank()) {
            b = match(graph, n1, n2, env, new TreeNode(), 0);            
            return b;
        }
        return false;
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public boolean match(Node q, Node t, Environment env) {
        if (q.isVariable() || q.isBlank()) {
            return true;
        }

        IDatatype qdt = (IDatatype) q.getValue();
        IDatatype tdt = (IDatatype) t.getValue();

        return qdt.sameTerm(tdt);
    }

    /**
     * *********************************************************
     *
     *
     **********************************************************
     */
    class TreeNode extends TreeMap<IDatatype, IDatatype> {

        TreeNode() {
            super(new Compare());
        }
    }

    /**
     * This Comparator enables to retrieve an occurrence of a given Literal
     * already existing in graph in such a way that two occurrences of same
     * Literal be represented by same Node in graph It (may) represent (1
     * integer) and (1.0 float) as two different Nodes Current implementation of
     * EdgeIndex sorted by values ensure join (by dichotomy ...)
     */
    class Compare implements Comparator<IDatatype> {

        public int compare(IDatatype dt1, IDatatype dt2) {

            // xsd:integer differ from xsd:decimal 
            // same node for same datatype 
            if (dt1.getDatatypeURI() != null && dt2.getDatatypeURI() != null) {
                int cmp = dt1.getDatatypeURI().compareTo(dt2.getDatatypeURI());
                if (cmp != 0) {
                    return cmp;
                }
            }

            int res = dt1.compareTo(dt2);
            return res;
        }
    }

    IDatatype getValue(Node n) {
        return (IDatatype) n.getValue();
    }

    /**
     * Two different blank nodes match if they have the same edges and their
     * target nodes recursively match (same term or blank match) Use case: two
     * OWL expressions are the same but use different blank nodes PRAGMA: does
     * not compare named graph when compare edges
     */
    boolean match(Graph g, Node n1, Node n2, Environment env, TreeNode t, int n) {
        if (n1.same(n2)) {
            return true;
        }

        IDatatype dt = t.get(getValue(n1));
        if (dt != null) {
            // we forbid to match another blank node
            // in some case it may happen
            // TODO:  manage a list of IDatatype
            boolean b = dt.same(getValue(n2));
            return b;
        } else {
            t.put(getValue(n1), getValue(n2));
        }

        List<Entity> l1 = g.getEdgeListSimple(n1);
        List<Entity> l2 = g.getEdgeListSimple(n2);

        if (l1.size() != l2.size()) {

            if (n == 0) {
                
               if (! clean(l1, l2)){
                   // one of them may have one additional edge: remove it
                   return false;
               }

            } else {
                return false;
            }
        }

        for (int i = 0; i < l1.size(); i++) {

            Edge e1 = l1.get(i).getEdge();
            Edge e2 = l2.get(i).getEdge();
            boolean b = match(g, e1, e2, env, t, n + 1);
            if (!b) {
                return false;
            }
        }

        return true;
    }

    boolean match(Graph g, Edge e1, Edge e2, Environment env, TreeNode t, int n) {

        if (!match(e1, e2, env)) {
            // TODO: rdf:type ???
            // URI/Literal vs Blank ???
            return false;
        }

        if (e1.getNode(1).isBlank() && e2.getNode(1).isBlank()) {
            boolean b = match(g, e1.getNode(1), e2.getNode(1), env, t, n);
            if (!b) {
                return false;
            }
        }

        return true;
    }

    boolean clean(List<Entity> l1, List<Entity> l2) {

        if (l1.size() < l2.size()) {
            List<Entity> tmp = l1;
            l1 = l2;
            l2 = tmp;
        }

        if (l1.size() - l2.size() > 1) {
           return false;
        }

        boolean found = false;
        for (int i = 0; i < l2.size(); i++) {

            Edge e1 = l1.get(i).getEdge();
            Edge e2 = l2.get(i).getEdge();

            if (!e1.getEdgeNode().equals(e2.getEdgeNode())) {
                l1.remove(l1.get(i));
                found = true;
                break;
            }
        }

        if (!found) {
            l1.remove(l1.get(l1.size() - 1));
        }

        return true;
    }
}
