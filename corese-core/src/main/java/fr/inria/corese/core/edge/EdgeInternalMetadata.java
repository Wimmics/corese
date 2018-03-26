package fr.inria.corese.core.edge;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Graph Edge for internal storage
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2018
 *
 */
public class EdgeInternalMetadata extends EdgeInternal {

    private Node meta;

    public EdgeInternalMetadata() {
    }

    EdgeInternalMetadata(Node graph, Node subject, Node object) {
        super(graph, subject, object);
    }

    public static EdgeInternalMetadata create(Entity ent) {
        EdgeInternalMetadata res = new EdgeInternalMetadata(ent.getGraph(), ent.getNode(0), ent.getNode(1));
        res.setMeta(ent.getNode(2));
        return res;
    }

   @Override
   public int nbNode() {
       return 3;
   }
   
   /**
    * Metadata node is not a vertex
    * Use case: 
    * 1. do not create same triple with different metadata
    * 2. two same triple with different metadata are considered as same triple
    */
   @Override
   public int nbGraphNode() {
       return 2;
   }
   
    @Override
    public Node getNode(int n) {
        switch (n) {           
            case 2:return getMeta();
        }
        return super.getNode(n);
    }

    /**
     * @return the meta
     */
    public Node getMeta() {
        return meta;
    }

    /**
     * @param meta the meta to set
     */
    public void setMeta(Node meta) {
        this.meta = meta;
    }
    
    @Override
    public String toString() {       
        String str = "";
        if (displayGraph) {
            str += getGraph() + " ";
        }
        str += getNode(0) + " " + getEdgeNode() + " " + getNode(1) + " " + getMeta();
        return str;
    }
    

}
