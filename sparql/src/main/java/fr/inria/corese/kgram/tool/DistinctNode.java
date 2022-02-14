package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.Node;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 */
public class DistinctNode<T extends Node> implements Iterator<T>, Iterable<T> {
    
    Iterable<T> mit;
    Iterator<T> it;
    HashMap<Node, Node> map;
    T current;
    
    
    public DistinctNode(Iterable<T> mit) {
        this.mit = mit;
        map = new HashMap<>();
    }

    @Override
    public boolean hasNext() {
        while (it.hasNext()) {
            current = it.next();
            if (current == null) {
                return false;
            }
            if (map.containsKey(current.getNode())) {
                // continue loop
            }
            else {
                map.put(current.getNode(), current.getNode());
                return true;
            }
            
        }
        return false;
    }

    @Override
    public T next() {
        return current;
    }

    @Override
    public Iterator<T> iterator() {
        current = null;
        it = mit.iterator();
        return this;
    }
    
}
