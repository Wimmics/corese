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
 * each kind of node has a table (uri, bnode, triple, literal)
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
public class NodeManager {

    private Graph graph;
    private ArrayList<PredicateTable> predicateTableList;
    // in some case content is obsolete
    private boolean active = true;
    // safety to switch off
    private boolean available = true;
    private int count = 0;
    private int index =0;
    private boolean debug = false;
    // record position of node in edge list
    private boolean position = true;
    private static final String NL = System.getProperty("line.separator");
    
    public class PredicateTable extends HashMap<Node, PredicateList> {}
    
    NodeManager(Graph g, int index) {
        graph = g;
        predicateTableList = new ArrayList<>(NodeKind.size());
        this.index = index;
        init();
    }

    void init() {        
        for (int i = 0; i<NodeKind.size(); i++) {
            getPredicateTableList().add(new PredicateTable());
        }
    }
    
    public int size() {
        int size = 0;
        for (PredicateTable t : getPredicateTableList()) {
            size += t.size();
        }
        return size;
    }

    // nb property
    public int count() {
        return count;
    }

    void clear() {
        for (PredicateTable t : getPredicateTableList()) {
            t.clear();
        }
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
        getGraph().getEventManager().start(Event.IndexNodeManager);
    }
    
    // finish indexing
    public void finish() {
        getGraph().getEventManager().finish(Event.IndexNodeManager, this);
    }
     
    void put(Node node, PredicateList list) {
        getPredicateTable(node).put(node, list);
    }
    
    PredicateList get(Node node) {
        return getPredicateTable(node).get(node);
    }
        
    void add(Node node, Node predicate, int n) {
        if (isEffective()) {
            PredicateList list = get(node);
            if (list == null) {
                list = new PredicateList(isPosition());
                put(node, list);
            }
            list.add(node, predicate, n);
            count++;
        }
    }
      
    PredicateList getPredicates(Node node) {
        if (isEffective()) {
           return getPredicateList(node);
        } 
        else if (isAvailable() && ! graph.isIndexable()) {
            synchronized (getGraph()) {
                getGraph().getIndex(getIndex()).indexNodeManager();
            }
            if (debug) {
                System.out.println("NMP create: " + getIndex() + " " + node + " " + getPredicateList(node));
            }
            return getPredicateList(node);
        } 
        else {
            return getGraph().getIndex().getSortedPredicates();
        }
    }

    PredicateList getPredicateList(Node node) {
        PredicateList list = get(node);
        if (list == null) {
            list = new PredicateList(isPosition(), 0);
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
        for (PredicateTable t : getPredicateTableList()) {
            for (Node n : t.keySet()) {
                sb.append(String.format("%s %s: ", num++, n));
                PredicateList predicateList = getPredicateList(n);
                int i = 0;
                for (Node p : predicateList) {
                    sb.append(String.format(" %s %s;", p, predicateList.getPosition(i++)));
                }
                sb.append(NL);
            }
        }

        return sb.toString();
    }
    
    @Override
    public String toString() {
        return display();
    }
    
    // one map per kind of Node
    public HashMap<Node, PredicateList> getPredicateTable(Node node) {        
        return getPredicateTableList().get(node.getNodeKind().getIndex());
    }

    public ArrayList<PredicateTable> getPredicateTableList() {
        return predicateTableList;
    }

    public void setPredicateTableList(ArrayList<PredicateTable> predicateTableList) {
        this.predicateTableList = predicateTableList;
    }

    public boolean isPosition() {
        return position;
    }

    public void setPosition(boolean position) {
        this.position = position;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
