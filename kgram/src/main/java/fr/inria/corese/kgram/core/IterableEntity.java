package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import java.util.Iterator;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
 class IterableEntity implements Iterable<Entity>, Iterator<Entity> {
        
        Iterable loop;
        Iterator it;
        
        IterableEntity(Iterable loop){
            this.loop = loop;
            it = loop.iterator();
        }

        @Override
        public Iterator<Entity> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entity next() {
            Object obj = it.next();
           if (obj instanceof Node) {
                Node n = (Node) obj;               
                return (Entity) n.getObject();
            }

            return (Entity) obj;


        }

        @Override
        public void remove() {
        }
    
    }
