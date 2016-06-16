
package fr.inria.edelweiss.kgraph.core.edge;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.api.core.TripleStore;
import fr.inria.edelweiss.kgraph.core.GraphObject;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public abstract class EdgeTop extends GraphObject implements Entity {
    
        public Entity copy() {
            return create(getGraph(), getNode(0), getEdgeNode(), getNode(1));
        }
        
        public static Entity create(Node source, Node subject, Node predicate, Node objet){
            return null;
        }
        
        public Node getEdgeNode(){
            return null;
        }
        
         public void setTag(Node node) {           
        }
         
         public void setGraph(Node node){
             
         }
         
        @Override
         public Object getProvenance(){
             return null;
         }
         
        @Override
         public void setProvenance(Object o){
             
         }               
        
     @Override
    public Iterable<IDatatype> getLoop() {
        return getNodeList();
    }
     
     public ArrayList<IDatatype> getNodeList() {
        ArrayList<IDatatype> list = new ArrayList();
        for (int i = 0; i < 4; i++) {
            list.add(getValue(null, i));
        }
        return list;
    }
        
        @Override
      public IDatatype getValue(String var, int n){     
        switch (n){
            case 0: return nodeValue(getNode(0));
            case 1: return nodeValue(getEdge().getEdgeNode());                 
            case 2: return nodeValue(getNode(1));
            case 3: return nodeValue(getGraph());
        }
        return null;
    }
      
      IDatatype nodeValue(Node n){
          return (IDatatype) n.getValue();
      }
      
        
        @Override
        public int pointerType(){
            return Pointerable.ENTITY_POINTER;
        }
        
        @Override
        public Entity getEntity(){
            return this;
        }
        
            @Override
        public TripleStore getTripleStore() {
            return getNode(0).getTripleStore();
        }



}
