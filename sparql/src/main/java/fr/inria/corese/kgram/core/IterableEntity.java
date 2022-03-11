package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import java.util.Iterator;
import fr.inria.corese.kgram.api.core.Edge;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
 class IterableEntity implements Iterable<Edge>, Iterator<Edge> {
        
        Iterable loop;
        Iterator it;
        
        IterableEntity(Iterable loop){
            this.loop = loop;
            it = loop.iterator();
        }

        @Override
        public Iterator<Edge> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Edge next() {
            Object obj = it.next();
           if (obj instanceof Node) {
                Node n = (Node) obj;               
                return (Edge) n.getNodeObject();
            }

            return (Edge) obj;


        }

        @Override
        public void remove() {
        }
    
    }
