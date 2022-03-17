package fr.inria.corese.core.index;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.sparql.api.IDatatype.NodeKind;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manage a table of list of properties for each node
 * Node ni -> (p1, .. pn) ; (i1, .. in)
 * where ij = position of ni in  edge list of property pj
 *
 * Use case:
 * ?x rdfs:label "Antibes"@fr . ?x ?q ?v
 * When we enumerate ?x ?q ?v we can focus on the properties of ?x=xi
 * instead of enumerating all properties.
 * DPbedia has more than 10.000 properties !
 *
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class NodeManagerBasic {

    private Graph graph;
    private HashMap<Node, PredicateList> nodePredicateTable;
    // in some case content is obsolete
    private boolean active = true;
    // safety to switch off
    private boolean available = true;
    private int count = 0;
    private int index =0;
    private boolean debug = false;
    // record position of node in edge list
    boolean isPosition = true;
    private static final String NL = System.getProperty("line.separator");
    
    NodeManagerBasic(Graph g, int index) {
        graph = g;
        nodePredicateTable = new HashMap<>();
        this.index = index;
    }

    public int size() {
        return getPredicateTable().size();
    }

    public int count() {
        return count;
    }

    void clear() {
        getPredicateTable().clear();
        count = 0;
    }
    
    public int getIndex() {
        return index;
    }

    public void desactivate() {
        clear();
        setActive(false);
    }

    public void activate() {
        clear();
        setActive(true);
    }
    
    // start indexing
    public void start() {
        activate();
        graph.getEventManager().start(Event.IndexNodeManager);
    }
    
    // finish indexing
    public void finish() {
        graph.getEventManager().finish(Event.IndexNodeManager, this);
    }
    
    PredicateList get(Node node) {
        return get1(node);
    }
    
    void put(Node node, PredicateList list) {
        put1(node, list);
    }
    
    PredicateList get1(Node node) {
        return getPredicateTable(node).get(node);
    }
    
    void put1(Node node, PredicateList list) {
        getPredicateTable(node).put(node, list);
    }
        
    void add(Node node, Node predicate, int begin, int end) {
        if (isEffective()) {
            PredicateList list = get(node);
            if (list == null) {
                list = new PredicateList(isPosition);
                put(node, list);
            }
            list.add(node, predicate, begin,end);
            count++;
        }
    }
      
    PredicateList getPredicates(Node node) {
        if (isEffective()) {
           return getPredicateList(node);
        } 
        else if (isAvailable() && ! graph.isIndexable()) {
            synchronized (graph) {
                graph.getIndex(index).indexNodeManager();
            }
            if (debug) {
                System.out.println("NMP create: " + index + " " + node + " " + getPredicateList(node));
            }
            return getPredicateList(node);
        } 
        else {
            return graph.getIndex().getSortedPredicates();
        }
    }

    PredicateList getPredicateList(Node node) {
        PredicateList list = get(node);
        if (list == null) {
            list = new PredicateList(isPosition, 0);
            put(node, list);
        }
        return list;
    }
    
    int getPosition(Node node, Node predicate) {
        PredicateList list = get(node);
        if (list == null) {
            return -2;
        }
        return list.getPosition(predicate);
    } 

    public boolean isEffective() {
        return active && available;
    }

   
    public boolean isActive() {
        return active ;
    }
    
     public boolean isConsultable() {
        return active && size()>0;
    }

    
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAvailable() {
        return available;
    }

    
    public void setAvailable(boolean available) {
        this.available = available;
    }

     
    public boolean isDebug() {
        return debug;
    }

   
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public String display() {
        StringBuilder sb = new StringBuilder();
        int num = 0;
        for (Node n : getPredicateTable().keySet()) {
            sb.append(String.format("%s %s: ", num++, n));
            PredicateList predicateList = getPredicateList(n);
            int i = 0;
            for (Node p : predicateList.getPredicateList()) {
                sb.append(String.format(" %s %s;", p, predicateList.getPosition(i++)));
            }
            sb.append(NL);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getPredicateTable().toString();
    }
    
    public HashMap<Node, PredicateList> getPredicateTable(Node node) {
        return getPredicateTable();
    }
    
    // @todo: one map per kind of Node
    public HashMap<Node, PredicateList> getPredicateTable2(Node node) {
        switch (node.getNodeKind()) {
            case URI:
            case BNODE:
            case TRIPLE:
            case LITERAL:
        }
        return getPredicateTable();
    }

    public HashMap<Node, PredicateList> getPredicateTable() {
        return nodePredicateTable;
    }

    public void setPredicateTable(HashMap<Node, PredicateList> ptable) {
        this.nodePredicateTable = ptable;
    }

}
