package fr.inria.corese.core.logic;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Distinct;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.RULE_DATAMANAGER_FILTER_INDEX;
import java.util.ArrayList;
import fr.inria.corese.kgram.api.core.Edge;
import java.util.List;

/**
 * Transitive Closure of one property, e.g. rdfs:subClassOf
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Closure {

    static int count = 0;

    Graph graph;
    Distinct distinct;
    Query query;
    Node predicate1, predicate2, graphName;
    boolean[][] connect;
    boolean isDistinct = true;
    private boolean isConnect = false;
    boolean isMessage = true;
    private boolean isTrace = false;
    // send index to data manager edge iterator 
    private boolean filterEdgeIndex = true;

    public Closure(Graph g, Distinct d) {
        graph = g;
        distinct = d;
        init();
    }

    void init() {
    }

    /**
     * if isConnect() generate a connection matrix for this predicate Transitive
     * edge will not be created in kg:rule for nodes that are already connected
     */
    public void init(Node p) {
        init(p, p);
    }

    public void init(Node p1, Node p2) {
        // named graph where entailments are stored
        graphName = ruleGraphNode();
        // predicate of transitive property
        predicate1 = propertyNode(p1);
        predicate2 = propertyNode(p2);
        if (Property.hasValue(RULE_DATAMANAGER_FILTER_INDEX, false)){
            setFilterEdgeIndex(false);
        }
        if (isConnect()) {
            int i = graph.getNodeIndex();
            try {
                connect = new boolean[i][];
                for (Edge ent : graph.getEdges(predicate1)) {
                    connect(ent.getNode(0), ent.getNode(1));
                }
            } catch (OutOfMemoryError E) {
                setConnect(false);
                System.out.println("Skip Cache Out Of Memory:  " + predicate1);
            }
        }
    }

    public void setQuery(Query q) {
        query = q;
    }

    public boolean isConnected(Node p, Node n1, Node n2) {
        if (isConnect()) {
            int i1 = n1.getIndex();
            if (i1 >= 0 && n2.getIndex() >= 0) {
                if (connect[i1] == null) {
                    try {
                        connect[i1] = new boolean[connect.length];
                    } catch (OutOfMemoryError E) {
                        if (isMessage) {
                            isMessage = false;
                            System.out.println("Skip Cache Out Of Memory:  " + p);
                            return graph.exist(p, n1, n2);
                        }
                    }
                }
                return connect[i1][n2.getIndex()];
            }
        }
        return exist(p, n1, n2);
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
                            System.out.println("Skip Cache Out Of Memory:  " + predicate1);
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
        if (predicate1 == null || predicate2 == null) {
            return;
        }
        //boolean same = pred1 == pred2;
        boolean same = predicate1.equals(predicate2);
        boolean go = true, isFirst = loop == 0;
        int n = 0;
       
        ArrayList<Edge> edgeListNew = new ArrayList<>(),
                edgeListTemp = new ArrayList<>();
        
        if (isTrace) {
            System.out.println("Closure: 0 " + graph.size(predicate1));
        }
        while (go) {

            Iterable<Edge> it1 = edgeListNew;
            if (n == 0) {
                it1 = getEdges(predicate1);
            } else if (isTrace) {
                System.out.println("Closure: " + n + " " + +edgeListNew.size());
            }
            n++;

            for (Edge e1 : it1) {
                // after step 0, consider only edges created at previous step
                Node node = e1.getNode(1);
                Node n1 = e1.getNode(0);

                if (same && n1.equals(node)) {
                    continue;
                }
                boolean ok1 = isFirst || e1.getEdgeIndex() >= prevIndex;

                Iterable<Edge> it2 = 
                        ok1 ? getEdges(predicate2, node, 0) :
                        getEdges(predicate2, node, 0, prevIndex);

                if (it2 != null) {

                    for (Edge e2 : it2) {
                        // join e2 on edge e1
                        if (e2 != null) {

                            boolean ok2 = ok1 || e2.getEdgeIndex() >= prevIndex;
                            if (!ok2) {
                                // need at least one new edge
                                continue;
                            }

                            Node n2 = e2.getNode(1);
                            if (e2.getNode(0).equals(n2)) {
                                continue;
                            }

                            if (!isConnected(predicate1, n1, n2) && isDistinct(n1, n2)) {
                                Edge ent = create(predicate1, n1, n2);
                                ent.setEdgeIndex(loopIndex);
                                edgeListTemp.add(ent);
                                connect(n1, n2);
                            }
                        }
                    }
                }
            }
            Node p = predicate1;
            if (Graph.isTopRelation(p)) {
                p = null;
            }
            if (isTrace) {
                System.out.println("Closure: new " + edgeListTemp.size());
            }
            insert(p, edgeListTemp);
            edgeListNew = edgeListTemp;
            edgeListTemp = new ArrayList<>();
            go = !edgeListNew.isEmpty();
        }
    }

    boolean isDistinct(Node n1, Node n2) {
        if (isDistinct) {
            return distinct.isDistinct(n1, n2);
        }
        return true;
    }

    Edge create(Node p, Node n1, Node n2) {
        Edge ent = graph.create(graphName, n1, p, n2);
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

    Node ruleGraphNode() {
        return graph.addRuleGraphNode();
    }

    Node propertyNode(Node p) {
        return graph.getPropertyNode(p);
    }

    Iterable<Edge> getEdges(Node p) {
        return graph.getEdges(p);
    }

    Iterable<Edge> getEdges(Node p, Node n, int i) {
        return graph.getEdges(p, n, i);
    }
    
    Iterable<Edge> getEdges(Node p, Node n, int i, int index) {
        return graph.getEdges(p, n, i);
    }
 
    void insert(Node p, List<Edge> edgeList) {
        graph.addOpt(p, edgeList);
    }

    boolean exist(Node p, Node n1, Node n2) {
        return graph.exist(p, n1, n2);
    }

    public boolean isFilterEdgeIndex() {
        return filterEdgeIndex;
    }

    public void setFilterEdgeIndex(boolean filterEdgeIndex) {
        this.filterEdgeIndex = filterEdgeIndex;
    }

}
