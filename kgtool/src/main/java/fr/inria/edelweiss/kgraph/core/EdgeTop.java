
package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public abstract class EdgeTop implements Entity {
    
        public Entity copy() {
            return this;
        }
        
         public void setTag(Node node) {           
        }
         
         public void setGraph(Node node){
             
         }


}
