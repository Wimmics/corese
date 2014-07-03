package fr.inria.edelweiss.kgraph.logic;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Distinct;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
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
    Node pred, src;
    boolean[][] connect;
    boolean isDistinct = true;
    private boolean isConnect = false;
    boolean isMessage = true;

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
        // named graph where entailments are stored
        src = graph.addGraph(Entailment.RULE);
        // predicate of transitive property
        pred = graph.getPropertyNode(p);
//            System.out.println("Cl: " + pred + " " + graph.size(pred));
//            System.out.println("Cl: Res " + graph.nbResources());
//           

        if (isConnect()) {
            int i = graph.nbResources();            
            try {
                connect = new boolean[i][];
                for (Entity ent : graph.getEdges(pred)) {
                    connect(ent.getNode(0), ent.getNode(1));
                }
            } catch (OutOfMemoryError E) {
                setConnect(false);
                System.out.println("Skip Cache Out Of Memory:  " + pred);
            }
        }
    }

    public boolean isConnected(Node n1, Node n2) {
        if (isConnect()){
            int i1 = n1.getIndex();       
            if (i1 >= 0 && n2.getIndex() >= 0) {
                if (connect[i1] == null) {
                    try {
                        connect[i1] = new boolean[connect.length];                   
                    } catch (OutOfMemoryError E) {  
                        if (isMessage){
                            isMessage = false;
                            System.out.println("Skip Cache Out Of Memory:  "  + pred);
                            return graph.exist(pred, n1, n2);
                        }
                    }                   
                }
                return connect[i1][n2.getIndex()];
            }
        }
        return graph.exist(pred, n1, n2);
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
                            System.out.println("Skip Cache Out Of Memory:  " + pred);
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
        if (pred == null){
            return;
        }
        boolean go = true, isFirst = loop == 0;
        int n = 0;

        ArrayList<Entity> lnew = new ArrayList<Entity>(),
                ltmp = new ArrayList<Entity>();

        while (go) {

            Iterable<Entity> it1 = lnew;
            if (n == 0) {
                it1 = graph.getEdges(pred);
            }
            n++;
            for (Entity e1 : it1) {
                // after step 0, consider only edges created at previous step
                Node node = e1.getNode(1);
                Node n1 = e1.getNode(0);
                if (n1 == node) {
                    continue;
                }
                boolean ok = isFirst || e1.getEdge().getIndex() >= prevIndex;

                Iterable<Entity> it2 = graph.getEdges(pred, node, 0);

                if (it2 != null) {

                    for (Entity e2 : it2) {
                        // join e2 on edge e1
                        if (e2 != null) {

                            ok = isFirst || ok || e2.getEdge().getIndex() >= prevIndex;
                            if (!ok) {
                                // need at least one new edge
                                continue;
                            }

                            Node n2 = e2.getNode(1);
                            if (e2.getNode(0) == n2) {
                                continue;
                            }

                            if (!isConnected(n1, n2) && isDistinct(n1, n2)) {
                                EdgeImpl ent = create(n1, n2);
                                ent.setIndex(loopIndex);
                                ltmp.add(ent);
                                connect(n1, n2);
                            }
                        }
                    }
                }
            }
            Node p = pred;
            if (p.getLabel().equals(Graph.TOPREL)){
                p = null;
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

    EdgeImpl create(Node n1, Node n2) {
        return graph.create(src, n1, pred, n2);
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
}
