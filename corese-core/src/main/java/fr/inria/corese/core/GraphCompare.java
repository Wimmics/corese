package fr.inria.corese.core;

import fr.inria.corese.kgram.api.core.Node;
import java.util.HashMap;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.kgram.api.core.Edge;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class GraphCompare {
    private static Logger logger = LoggerFactory.getLogger(GraphCompare.class);
    
    Graph g1, g2;
    
    GraphCompare(Graph g1, Graph g2){
        this.g1 = g1;
        this.g2 = g2;
    }
    
    
       public boolean compare(boolean isGraph, boolean detail, boolean isDebug) {
        g1.prepare();
        g2.prepare();

        boolean ok = true;
        
        if (g1.size() != g2.size()) {
            logger.error("** Graph Size: " + g1.size() + " != " + g2.size());
            ok = false;
        }

        for (Node pred1 : g1.getProperties()) {
            Node pred2 = g2.getPropertyNode(pred1.getLabel());
            if (pred2 == null){
               ok = false;
               logger.error("** Graph Pred: " + pred1 + " undefined in G2 " );
            }
            else {
                int s1 = g1.size(pred1);
                int s2 = g2.size(pred2);
                if (s1 != s2) {
                    ok = false;
                    logger.error("** Graph Pred: " + pred1 + ": " + s1 + " != " + s2);
                }
            }
        }

        if (!ok && !detail) {
            return false;
        }

        TBN t = new TBN();

        for (Node pred1 : g1.getProperties()) {

            Iterable<Edge> l1 = g1.getEdges(pred1);

            Node pred2 = g2.getPropertyNode(pred1.getLabel());

            if (pred2 == null) {
                if (l1.iterator().hasNext()) {
                    if (isDebug) {
                        logger.error("Missing in g2: " + pred1);
                    }
                    return false;
                }
            } else {
                Iterable<Edge> l2 = g2.getEdges(pred2);

                Iterator<Edge> it = l2.iterator();

                for (Edge ent1 : l1) {

                    if (g1.isByIndex()) {
                        // node index
                        boolean b = compare(g2, pred2, t, ent1, isGraph);
                        if (!b) {
                            if (isDebug) {
                                logger.error("Missing in g2: " + ent1);
                            }
                            return false;
                        }
                    } else {
                        // node value 
                        if (!it.hasNext()) {
                            return false;
                        }

                        Edge ent2 = it.next();
                        if (!compare(ent1, ent2, t, isGraph)) {
                            if (isDebug) {
                                logger.error(ent1.toString());
                                logger.error(ent2.toString());
                            }
                            return false;
                        }
                    }
                }
            }

        }
        return true;
    }

       /**
        * TODO: may return false negative because it does not backtrack
        * It should be a projection ...
        */
    boolean compare(Graph g2, Node pred2, TBN t, Edge ent1, boolean isGraph) {
        Iterable<Edge> l2 = g2.getEdges(pred2);
        Iterator<Edge> it = l2.iterator();
        for (Edge ent2 : l2) {
            if (compare(ent1, ent2, t, isGraph)) {
                return true;
            }
        }
        return false;
    }

    boolean compare(Edge ent1, Edge ent2, TBN t, boolean isGraph) {

        for (int j = 0; j < ent1.nbGraphNode(); j++) {

            Node n1 = ent1.getNode(j);
            Node n2 = ent2.getNode(j);

            if (!compare(n1, n2, t)) {
                for (int k = 0; k < j; k++) {
                    t.pop(ent1.getNode(k));
                }
                return false;
            }
        }
        
        if (isGraph){
            return ent1.getGraph().equals(ent2.getGraph());
        }
        return true;
    }

    /**
     * Blanks may have different label but should be mapped to same blank
     */
    boolean compare(Node n1, Node n2, TBN t) {
        boolean ok = false;
        if (n1.isBlank()) {
            if (n2.isBlank()) {
                // blanks may not have same ID but 
                // if repeated they should  both be the same
                ok = t.same(n1, n2);
            }
        } else if (n2.isBlank()) {
        } else {
            ok = n1.equals(n2);
        }

        return ok;
    }

    class TBN extends HashMap<Node, Node> {
        
        HashMap<Node, Integer> count ;
        
        TBN(){
            count = new HashMap<Node, Integer>();
        }

        boolean same(Node n1, Node n2) {
            if (containsKey(n1)) {
                boolean b = get(n1).equals(n2);
                if (b){
                    count.put(n1, count.get(n1) + 1);
                }
                return b;
            } else {
                put(n1, n2);
                count.put(n1, 1);
               return true;
            }
        }
        
        void pop(Node n){
            if (n.isBlank()){
                if (count.containsKey(n)) {
                    count.put(n, count.get(n) - 1); 
                    if (count.get(n) == 0){
                        remove(n);
                    }
                }
            }
            
        }
    }

}
