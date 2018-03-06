package fr.inria.corese.kgraph.core.edge;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;

/**
 * Graph Edge for the defaultGraph
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public abstract class EdgeBinary extends EdgeTop 
    implements Edge {
    public static boolean displayGraph = true;
    protected Node subject, object;

    public EdgeBinary() {
    }

  
    EdgeBinary(Node subject, Node object) {
        this.subject = subject;
        this.object = object;
    }    

    @Override
    public String toString() {       
        String str = "";
        if (displayGraph) {
            str += getGraph() + " ";
        }
        str += getNode(0) + " " + getEdgeNode() + " " + getNode(1);
        return str;
    }
           

    @Override
    public boolean contains(Node node) {
        // TODO Auto-generated method stub
        return getNode(0).same(node) || getNode(1).same(node);
    }

 
    @Override
    public int getIndex() {
        return -1;
    }
    
      @Override
    public void setIndex(int n) {
        
    }


    @Override
    public String getLabel() {
        return getEdgeNode().getLabel();
    }

    @Override
    public Node getNode(int n) {
       switch(n){
           case Graph.IGRAPH: return getGraph();
           case 0: return subject;
           case 1: return object;
       }
       return null;
    }
    
    public void setNode(int i, Node n){
        switch (i){
            case 0: subject = n; break;
            case 1: object  = n; break;
        }
    }

    @Override
    public int nbNode() {
        return 2;
    }

    @Override
    public Edge getEdge() {
        return this;
    }


    @Override
    public Node getNode() {
        // TODO Auto-generated method stub
        return DatatypeMap.createObject(this.toString(), this);
    }

    @Override
    public Node getEdgeVariable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProvenance() {
        return null;
    }
    
    @Override    
    public void setProvenance(Object obj) {         
    }
    
     @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.DEFAULT_INDEX);
    }
    
}

