
package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.core.PointerObject;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public abstract class EdgeTop extends PointerObject implements Entity {
    
        public Entity copy() {
            return this;
        }
        
         public void setTag(Node node) {           
        }
         
         public void setGraph(Node node){
             
         }
         
         public Object getProvenance(){
             return null;
         }
         
         public void setProvenance(Object o){
             
         }
         
        @Override
       public Iterable<Object> getLoop() {
            ArrayList<Object> list = new ArrayList();
            list.add(getGraph().getValue());
            list.add(getNode(0).getValue());
            list.add(getEdge().getEdgeNode().getValue());
            list.add(getNode(1).getValue());
            return list;
      }
        
        @Override
        public int pointerType(){
            return Pointerable.ENTITY;
        }
        
        @Override
        public Entity getEntity(){
            return this;
        }


}
