package fr.inria.corese.core.index;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Reduce Edges with Metadata (rdf star)
 */
public class EdgeManagerMetadata {
    static final int IGRAPH = Graph.IGRAPH;
    
    private EdgeManager manager;
    
    EdgeManagerMetadata(EdgeManager man) {
        manager = man;
    }
    
    Graph getGraph() {
        return getManager().getGraph();
    }
    
    Node getPredicate() {
        return getManager().getPredicate();
    }
    
    List<Edge> getEdgeList() {
        return getManager().getEdgeList();
    }
    
    int getIndex() {
        return getManager().getIndex();
    }
    
    /**
     * Context: RDF*
     * Merge duplicate triples, keep one metadata node
     * PRAGMA: index = 0, list is sorted and reduced
     * rdf* triple with same g s p o  and with or without ref id 
     * are not yet reduced
     * because they are considered equal by compare()
     * we remove these duplicate here and keep triple with ref id if any
     */
    void metadata() {
        ArrayList<Edge> l = new ArrayList<>();
        Edge e1 = null;
        int count = 0;
        int i = 0;
        
        for (Edge e2 : getEdgeList()) { 
            if (e1 == null) {
                e1 = e2;
                l.add(e2);
            }
            else if (compare3(e1, e2) == 0){
                //  g s p o t1 vs g s p o t2
                //  keep g s p o t1
                if (e1.getReferenceNode() == null) { 
                    // e2 replace e1
                    share(e2, e1);
                    e1 = e2;
                    l.set(l.size()-1, e2);
                    count ++;
                }  
                else {
                    share(e1, e2);
                    merge(e1, e2);
                    // e1 replace e2
                    count ++;
                }
            }
            else if (Graph.TRIPLE_UNIQUE_NAME && compare2(e1, e2) == 0) {
                // g1 s p o t1 vs g2 s p o t2
                // replace by 
                // g1 s p o t1 vs g2 s p o t1
                name(e1, e2, l, i);
                e1 = l.get(l.size()-1);
            }
            else {
                e1 = e2;
                l.add(e2);
            }
            i++;
        }
        
        getManager().setEdgeList(l);
        if (count > 0) {
            getGraph().setSize(getGraph().size() - count);
        }       
    }
    
    // e1 replace e2
    void share(Edge e1, Edge e2) {
       if (e2.isAsserted()) {
            e1.setAsserted(true);
        }
    }
      

     
    // keep only one metadata node (e1)
    // replace e2 reference by e1 reference
    void merge(Edge e1, Edge e2) {
        if (e1.getReferenceNode() != e2.getReferenceNode()) {
            getManager().getIndexer().replace(e2.getReferenceNode(), e1.getReferenceNode());
        }
    }
    
    /** 
     *  e1 = g1 s p o t1 ; e2 = g2 s p o t2
     *  merge reference nodes and replace by: 
     *  g1 s p o t1 ; g2 s p o t1
     *  e1 is in list, e2 is candidate
     *  int i is index of e2 in Index main list
     */
    void name(Edge e1, Edge e2, List<Edge> edgeList, int i) {
        if (e1.getReferenceNode() != null) {
            if (e2.getReferenceNode() != null) {
                // replace reference node t2 of e2 with t1
                merge(e1, e2);
                e2.setReferenceNode(e1.getReferenceNode());
            } else {
                // set/add reference node of e2 to t1
                e2 = getGraph().getEdgeFactory().name(e2, getPredicate(), e1.getReferenceNode());
            }
        } else if (e2.getReferenceNode() != null) {
            // set/add reference node of e1 to t2
            e1 = getGraph().getEdgeFactory().name(e1, getPredicate(), e2.getReferenceNode());
            edgeList.set(edgeList.size() - 1, e1);
        } else {
            Node ref = getSameEdgeReferenceNode(e2, i);
            // e1, e2 have no reference node
            // add reference node when there is a third one with reference node
            // e1 = g1 s p o -> g1 s p o t
            // e2 = g2 s p o -> g2 s p o t
            if (ref != null) {
                e1 = getGraph().getEdgeFactory().name(e1, getPredicate(), ref);
                e2 = getGraph().getEdgeFactory().name(e2, getPredicate(), ref);
                edgeList.set(edgeList.size() - 1, e1);
            }
        }
        edgeList.add(e2);
    }
    
    /**
     * Is there an edge similar to e in Index list after index n
     * with a reference node
     * similar: same subject/object
     */
    Node getSameEdgeReferenceNode(Edge e, int n) {
        for (int i=n+1; i<getEdgeList().size(); i++) {
            Edge e2 = getEdgeList().get(i);
            if (compare2(e, e2) == 0) {
                if (e2.hasReferenceNode()) {
                    return e2.getReferenceNode();
                }
            }
            else {
                return null;
            }
        }
        return null;
    } 
    
     // compare subject object graph (not compare reference node if any)
    int compare3(Edge e1, Edge e2) {
        return getManager().compare3(e1, e2);
    }
    
    // compare subject object (not compare graph)
    int compare2(Edge e1, Edge e2) {
        return getManager().compare2(e1, e2);
    }

    public EdgeManager getManager() {
        return manager;
    }

    public void setManager(EdgeManager manager) {
        this.manager = manager;
    }
    
    
    /**
     * Draft:
     * reduce + metadata at once
     * @pragma: index() == 0
     */
    int reduceMetadata(NodeManager mgr) {
        ArrayList<Edge> l = new ArrayList<>();
        Edge pred = null;
        int count = 0, ind = 0;
        
        for (Edge edge : getEdgeList()) {
            if (pred == null) {
                l.add(edge);
                mgr.add(edge.getNode(getIndex()), getPredicate(), ind);
                ind++;
            } 
            else if (getGraph().isMetadataNode()) {
                if (compare2(pred, edge) == 0) {
                    // same s p o
                    if (getManager().compareNodeTerm(pred.getGraphNode(), edge.getGraphNode()) == 0) {
                        // same g s p o
                        
                    }
                    else {
                        // same s p o different g
                    }
                }
                else {
                    // different s p o
                }
                
                count++;
            } 
            else if (getManager().equalWithoutConsideringMetadata(pred, edge)) {
                // skip edge because it is redundant with pred
                count++;
            }
            
            if (false) {
                l.add(edge);
                if (edge.getNode(getIndex()) != pred.getNode(getIndex())) {
                    mgr.add(edge.getNode(getIndex()), getPredicate(), ind);
                }
                ind++;
            }
            
            pred = edge;
        }
        
        //setEdgeList(l);
        if (count > 0) {
            getGraph().setSize(getGraph().size() - count);
        }
        //System.out.println("after reduce: " + list);
        return count;
    }
      
    
}
