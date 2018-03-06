package fr.inria.corese.kgraph.index;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Event;
import fr.inria.corese.kgraph.core.Graph;
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
public class NodeManager {

    private Graph graph;
    private HashMap<Node, PredicateList> ptable;
    // in some case content is obsolete
    private boolean active = true;
    // safety to switch off
    private boolean available = true;
    private int count = 0;
    private int index =0;
    private boolean debug = false;
    // record position of node in edge list
    boolean isPosition = true;
    private int i;
    private static final String NL = System.getProperty("line.separator");
    
    NodeManager(Graph g, int index) {
        graph = g;
        ptable = new HashMap<>();
        this.index = index;
        //setAvailable(index > 0);
    }

    public int size() {
        return ptable.size();
    }

    public int count() {
        return count;
    }

    void clear() {
        ptable.clear();
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
    
    void add(Node node, Node predicate, int n) {
        if (isEffective()) {
            PredicateList list = ptable.get(node);
            if (list == null) {
                list = new PredicateList(isPosition);
                ptable.put(node, list);
            }
            list.add(node, predicate, n);
            count++;
        }
    }
      
    PredicateList getPredicates(Node node) {
        if (isEffective()) {
           return getPredicateList(node);
        } 
        else if (isAvailable() && ! graph.isIndex()) {
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
        PredicateList list = ptable.get(node);
        if (list == null) {
            list = new PredicateList(isPosition, 0);
            ptable.put(node, list);
        }
        return list;
    }
    
    int getPosition(Node node, Node predicate) {
        PredicateList list = ptable.get(node);
        if (list == null) {
            return -2;
        }
        return list.getPosition(predicate);
    } 

    public boolean isEffective() {
        return active && available;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active ;
    }
    
     public boolean isConsultable() {
        return active && size()>0;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

     /**
     * @return the available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * @param available the available to set
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }


    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public String display() {
        StringBuilder sb = new StringBuilder();
        
        for (Node n : ptable.keySet()) {
            sb.append(n).append(" :");
            PredicateList l = getPredicateList(n);
            int i = 0;
            for (Node p : l) {
                sb.append(" ").append(p).append(" ").append(l.getPosition(i++)).append("; ");
            }
            sb.append(NL);
        }
        
        return sb.toString();
    }

}
