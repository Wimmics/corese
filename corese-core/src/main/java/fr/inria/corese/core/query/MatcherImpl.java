package fr.inria.corese.core.query;

import java.util.HashMap;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;

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
    private static boolean byIndex = false;

    /**
     * @return the byIndex
     */
    public static boolean isByIndex() {
        return byIndex;
    }

    /**
     * @param aByIndex the byIndex to set
     */
    public static void setCompareIndex(boolean aByIndex) {
        byIndex = aByIndex;
    }
    Graph graph;
    Entailment entail;
    Cache table;
    MatchBNode bnode;
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

    MatcherImpl(Graph g) {
        graph = g;
        entail = g.getEntailment();        
        bnode = new MatchBNode(g);
    }

    public static MatcherImpl create() {
        return new MatcherImpl(Graph.create());
    }

    public static MatcherImpl create(Graph g) {
        return new MatcherImpl(g);
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
            if (max == r.nbNode() + 1 && q.getNode(q.nbNode() -1).isMatchNodeList()) {
                //ok
            }
            else {
                return false;
            }
        }
        for (int i = 0; i < max; i++) {
            Node qNode = q.getNode(i);
            if (qNode.isMatchNodeList()) {
                return true;
            }
            else {
                Node node  = r.getNode(i);
                if (!match(qNode, node, env)) {
                    return false;
                }
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
        return same(n1, n2);  
    }
    
    @Deprecated
    public boolean same2(Node node, Node n1, Node n2, Environment env) {        
        boolean b = same(n1, n2);        
        if (b) {
            return true;
        }
        Query q = env.getQuery();
        if (q != null && q.isMatchBlank()
                && n1.isBlank() && n2.isBlank()) {
            b = bnode.same(n1, n2, env, 0);             
            return b;
        }
        return false;
    }
    
    boolean same(Node n1, Node n2){        
        return  (n1.getIndex() == n2.getIndex() 
                && n1.getTripleStore() == n2.getTripleStore()
                && n1.getIndex() != -1) 
                || n1.match(n2); // was same
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public boolean match(Node q, Node t, Environment env) {
        if (q.isVariable() || q.isBlank()) {
            return true;
        }

        IDatatype qdt = (IDatatype) q.getValue();
        IDatatype tdt = (IDatatype) t.getValue();
        return (DatatypeMap.SPARQLCompliant) ? qdt.sameTerm(tdt) : qdt.match(tdt);
    }

  public MatchBNode getMatchBNode(){
      return bnode;
  }
}
