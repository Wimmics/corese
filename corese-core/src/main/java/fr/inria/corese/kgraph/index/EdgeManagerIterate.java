package fr.inria.corese.kgraph.index;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.edge.EdgeGeneric;
import java.util.Iterator;

/**
 * Iterate internal Edge Index 
 * fill buffer Edge with property Node from internal Index
 * return the buffer
 * buffer is the same object during iteration
 * hence if someone need to record edge, it MUST be copied
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
class EdgeManagerIterate implements Iterable<Entity>, Iterator<Entity> {

    EdgeManager list;
    int focusNodeIndex;
    int ind, start = 0;
    boolean isList = true;
    EdgeGeneric buffer;

    EdgeManagerIterate(EdgeManager l) {
        list = l;
        buffer = new EdgeGeneric(list.getPredicate());
    }

    EdgeManagerIterate(EdgeManager l, int n) {
       this(l);
       start = n;
       focusNodeIndex = getNodeIndex(n);
       isList = false;
    }

    @Override
    public Iterator<Entity> iterator() {
        ind = start;
        return this;
    }
    
    int getNodeIndex(int n) {
        return list.get(n).getNode(list.getIndex()).getIndex();
    }

    @Override
    public boolean hasNext() {
        boolean b = ind < list.size()
                && (isList || getNodeIndex(ind) == focusNodeIndex);        
        return b;
    }

    @Override
    public Entity next() {
        Entity ent = list.get(ind++);
        fill(buffer, ent);
        return buffer;
    }
    
    /**
     * Fill buffer Edge from internal ent
     */
    void fill(EdgeGeneric buf, Entity ent){
        buf.setGraph(ent.getGraph());
        buf.replicate(ent);
    }

    @Override
    public void remove() {
    }

   
}
