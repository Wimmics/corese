package fr.inria.corese.kgraph.logic;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Distinct;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.rule.Rule;
import java.util.ArrayList;

/**
 * Transitive Closure of one property, e.g. rdfs:subClassOf
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Closure {
static int count = 0;

    Graph graph;
    Distinct dist;
    Query query;
    Node pred1, pred2, src;
    boolean[][] connect;
    boolean isDistinct = true;
    private boolean isConnect = false;
    boolean isMessage = true;
    private boolean isTrace = false;

    public Closure(Graph g, Distinct d) {
        graph = g;
        dist = d;
        init();
    }

    void init() {
    }
    
   
    /**
     * if isConnect() generate a connection matrix for  this predicate
     * Transitive edge will not be created in kg:rule 
     * for nodes that are already connected 
     */
    
        public void init(Node p) {
            init(p, p);
        }
        
    public void init(Node p1, Node p2) {
        // named graph where entailments are stored
        src = graph.addRuleGraphNode();
        // predicate of transitive property
        pred1 = graph.getPropertyNode(p1); 
        pred2 = graph.getPropertyNode(p2); 
        if (isConnect()) {
            int i = graph.getNodeIndex();            
            try {
                connect = new boolean[i][];
                for (Entity ent : graph.getEdges(pred1)) {
                    connect(ent.getNode(0), ent.getNode(1));
                }
            } catch (OutOfMemoryError E) {
                setConnect(false);
                System.out.println("Skip Cache Out Of Memory:  " + pred1);
            }
        }
    }
    
    public void setQuery(Query q){
        query = q;
    }

    public boolean isConnected(Node p, Node n1, Node n2) {
        if (isConnect()){
            int i1 = n1.getIndex();       
            if (i1 >= 0 && n2.getIndex() >= 0) {
                if (connect[i1] == null) {
                    try {
                        connect[i1] = new boolean[connect.length];                   
                    } catch (OutOfMemoryError E) {  
                        if (isMessage){
                            isMessage = false;
                            System.out.println("Skip Cache Out Of Memory:  "  + p);
                            return graph.exist(p, n1, n2);
                        }
                    }                   
                }
                return connect[i1][n2.getIndex()];
            }
        }
        return graph.exist(p, n1, n2);
    }

    public void connect(Node n1, Node n2) {
        if (isConnect()) {
            int i1 = n1.getIndex();
            if (i1 >= 0 && n2.getIndex() >= 0) {
                if (connect[i1] == null) {
                    try {
                        connect[i1] = new boolean[connect.length];
                    } catch (OutOfMemoryError E) {
                        if (isMessage) {
                            isMessage = false;
                            System.out.println("Skip Cache Out Of Memory:  " + pred1);
                        }        
                        return;
                    }
                }
                connect[i1][n2.getIndex()] = true;
            }
        }
    }

    /**
     * Transitive closure in a rule base This code emulates a transitive closure
     * rule, it is called at each loop in the rule engine When loop == 0
     * consider all edges When loop > 0, one of the two edges (e1, e2) must have
     * been created at previous loop First step n == 0: all edges Following
     * steps: new edges from previous step loopIndex: index of rule current loop
     * prevIndex: index of rule previous loop created edges are tagged with
     * current loop index. TODO: Producer vs Graph
     */
    public void closure(int loop, int loopIndex, int prevIndex) {
        if (pred1 == null || pred2 == null){
            return;
        }
        boolean same = pred1 == pred2;
        boolean go = true, isFirst = loop == 0;
        int n = 0;

        ArrayList<Entity> lnew = new ArrayList<Entity>(),
                ltmp = new ArrayList<Entity>();
        if (isTrace){
            System.out.println("Cl: 0 "  + graph.size(pred1));
        }
        while (go) {

            Iterable<Entity> it1 = lnew;
            if (n == 0) {
                it1 = graph.getEdges(pred1);
            }
            else if (isTrace){
                System.out.println("Cl: "  +n + " " + + lnew.size());               
            }
            n++;

            for (Entity e1 : it1) {
                // after step 0, consider only edges created at previous step
                Node node = e1.getNode(1);
                Node n1   = e1.getNode(0);

                if (same && n1 == node) {
                    continue;
                }
                boolean ok1 = isFirst || e1.getEdge().getIndex() >= prevIndex;

               Iterable<Entity> it2 = graph.getEdges(pred2, node, 0);

                if (it2 != null) {

                    for (Entity e2 : it2) {
                        // join e2 on edge e1
                        if (e2 != null) {

                            boolean ok2 = ok1 || e2.getEdge().getIndex() >= prevIndex;
                            if (!ok2) {
                                // need at least one new edge
                                continue;
                            }

                            Node n2 = e2.getNode(1);
                            if (e2.getNode(0) == n2) {
                                continue;
                            }

                            if (!isConnected(pred1, n1, n2) && isDistinct(n1, n2)) {
                                Entity ent = create(pred1, n1, n2);
                                ent.getEdge().setIndex(loopIndex);
                                ltmp.add(ent);
                                connect(n1, n2);
                            }
                        }
                    }
                }
            }
            Node p = pred1;
            if (p.getLabel().equals(Graph.TOPREL)){
                p = null;
            }
            if (isTrace){
                System.out.println("Cl: new " + ltmp.size());
            }
           graph.addOpt(p, ltmp);
           lnew = ltmp;
           ltmp = new ArrayList<Entity>();
           go = ! lnew.isEmpty();
        }
    }

    boolean isDistinct(Node n1, Node n2) {
        if (isDistinct) {
            return dist.isDistinct(n1, n2);
        }
        return true;
    }

    Entity create(Node p, Node n1, Node n2) {
        Entity ent = graph.create(src, n1, p, n2);
        ent.setProvenance(query.getProvenance());
        return ent;
    }

    /**
     * @return the Connect
     */
    public boolean isConnect() {
        return isConnect;
    }

    /**
     * @param Connect the Connect to set
     */
    public void setConnect(boolean Connect) {
        this.isConnect = Connect;
    }

    /**
     * @return the isTrace
     */
    public boolean isTrace() {
        return isTrace;
    }

    /**
     * @param isTrace the isTrace to set
     */
    public void setTrace(boolean isTrace) {
        this.isTrace = isTrace;
    }
}
