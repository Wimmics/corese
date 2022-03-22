/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.transform.Transformer;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Given two bnodes representing OWL expressions
 * test if they represent the same expression
 * 
 * TODO:
 * two RDF List with same elements but in different order do not match yet
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class MatchBNode {

        TreeNode ttrue, tfalse;
        Graph graph;
        int count = 0;

    MatchBNode(Graph g) {
        ttrue  = new TreeNode();
        tfalse = new TreeNode();
        graph = g;
    }

    /**
     * Store the result in a table   
     */
    boolean same(Node n1, Node n2, Environment env, int n) {
        IDatatype dt1 = getValue(n1);
        IDatatype dt2 = getValue(n2);
        
        IDatatype dt = ttrue.get(dt1);
        if (dt != null && dt.same(dt2)){                     
            return true;
        }
        
        dt = tfalse.get(dt1);
        if (dt != null && dt.same(dt2)){
            return false;
        }
        
        boolean b = match(n1, n2, new TreeNode(), n); 
        count++;
        
//        if (b){
//            trace(n1, n2);
//        }             
        
        if  (b){
            ttrue.put(dt1, dt2);
            ttrue.put(dt2, dt1);
        }
        else {
            tfalse.put(dt1, dt2);           
            tfalse.put(dt2, dt1);           
       }
        return b;
    }
    
    public int getCount(){
        return count;
    }
    
    public TreeNode getTree(boolean b){
        if (b){
            return ttrue;
        }
        else {
            return tfalse;
        }
        
    }
    
    void trace(Node n1, Node n2) {
            try {
                Transformer t = Transformer.create(graph, Transformer.TURTLE);
                System.out.println(n1 + " " + t.process(n1).getLabel());
                System.out.println(n2 + " " + t.process(n2).getLabel());
//        System.out.println("MBN: " + graph.getList(n1));
//        System.out.println("MBN: " + graph.getList(n2));
System.out.println();


//        if (b){
//            List<Entity> l1 = g.getEdgeListSimple(n1);
//            List<Entity> l2 = g.getEdgeListSimple(n2);
//            for (Entity e :l1){
//                System.out.print(e.getEdge().getEdgeNode() + " ");
//            }
//            System.out.println();
//            for (Entity e :l2){
//                System.out.print(e.getEdge().getEdgeNode() + " ");
//            }
//            System.out.println();
//            System.out.println();        
//        }
            } catch (EngineException ex) {
                Logger.getLogger(MatchBNode.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    

    /**
     * Two different blank nodes match if they have the same edges and their
     * target nodes recursively match (same term or blank match)
     * TreeNode tree ensure that same bnode is compared with same bnode during recursion
     * in case of loop
     * PRAGMA: n1 n2 are bnodes
     */
    boolean match(Node n1, Node n2, TreeNode tree, int n) {
        if (n1.same(n2)) {
            return true;
        }

        IDatatype dt1 = getValue(n1);
        IDatatype dt2 = getValue(n2);
        IDatatype dt = tree.get(dt1);
        if (dt != null) {
            // we forbid to match another blank node
            // TODO:  check this
            boolean b = dt.same(dt2);
            return b;
        } else {
            tree.put(dt1, dt2);
        }

        boolean suc = false;

        List<Node> ln1 = graph.getList(n1);
        
        if (ln1.isEmpty()) {
            List<Edge> l1 = graph.getEdgeListSimple(n1);
            List<Edge> l2 = graph.getEdgeListSimple(n2);

            if (size(l1, l2, n)
                    && match(l1, l2)
                    && match(l1, l2, tree, n + 1)) {
                suc = true;
            } 
        }
        else {
           List<Node> ln2 = graph.getList(n2); 
           if (ln1.size() == ln2.size()
                   && matchList(ln1, ln2, tree, n)){
               suc = true;             
           }
        }
        
        tree.remove(dt1);
        return suc;
  }
            
   boolean match(List<Edge> l1, List<Edge> l2, TreeNode tree, int n) {
        for (int i = 0; i < l1.size(); i++) {

            Edge e1 = l1.get(i);
            Edge e2 = l2.get(i);
            boolean b = match(e1, e2, tree, n);
            if (!b) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Match l1 elements to l2 elements, in any order
     */
   
    boolean matchList(List<Node> l1, List<Node> l2, TreeNode tree, int n){
        for (Node n1 : l1){
            
            int i = 0;
            boolean suc = false;
            
            for (Node n2 : l2){
                if (compare(n1, n2, tree, n)){
                    suc = true;
                    break;
                }
                else {
                    i++;
                }
            }
            
            if (suc){
                l2.remove(i);
            }
            else {
                return false;
            }
        }
        
        return true;
    }
    
    
    
    boolean size(List<Edge> l1, List<Edge> l2, int n) {
        return l1.size() == l2.size();
    }

    boolean clean2(List<Edge> l1, List<Edge> l2, int n){
        if (l1.size() == l2.size()) {
            return true;
        }      
        if (n == 0 && clean(l1, l2)) {
                // one of them may have an additional edge: skip it
                // use case: 
                // xx a _:b1
                // _:b2 subClassOf _:b3
                // _:b1 _:b2 may match if we skip the subClassOf edge
                // TODO: clean this
                return true;               
        }
        return false;
    }
    
    boolean match(List<Edge> l1, List<Edge> l2) {
        
        for (int i = 0; i < l1.size(); i++) {
            Edge e1 = l1.get(i);
            Edge e2 = l2.get(i);
            if (!e1.getEdgeNode().equals(e2.getEdgeNode())) {
                return false;
            }
        }
        
        return true;
    }

    IDatatype getValue(Node n) {
        return  n.getValue();
    }

    boolean match(Edge e1, Edge e2, TreeNode t, int n) {
        
        return compare(e1.getNode(1), e2.getNode(1), t, n);
    }

    boolean compare(Node x, Node y, TreeNode t, int n) {
        if (x.same(y)) {
            return true;
        } else if (x.isBlank() && y.isBlank()) {
            return match(x, y, t, n);
        }
        return false;
    }

    
    /**
     * l1 l2 are list of edges (of two bnodes)
     * if one of the list has one more edge than the other
     * remove the additional edge
     * Use case:
     * compare PAT and PAT subClassOf EXP
     * The second occurrence of PAT has subClassOf edge
     */
    boolean clean(List<Edge> l1, List<Edge> l2) {

        if (l1.size() < l2.size()) {
            List<Edge> tmp = l1;
            l1 = l2;
            l2 = tmp;
        }

        if (l1.size() - l2.size() > 1) {
            return false;
        }

        boolean found = false;
        Edge rem = null;
        for (int i = 0; i < l2.size(); i++) {

            Edge e1 = l1.get(i);
            Edge e2 = l2.get(i);

            if (!e1.getEdgeNode().equals(e2.getEdgeNode())) {
                rem = l1.get(i);
                l1.remove(l1.get(i));
                found = true;
                break;
            }
        }

        if (!found) {
            rem = l1.get(l1.size() - 1);
            l1.remove(l1.get(l1.size() - 1));
        }
               
        return true;
    }
    
      
      public class TreeNode extends TreeMap<IDatatype, IDatatype> {

         TreeNode(){
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
    
    
    
    
}