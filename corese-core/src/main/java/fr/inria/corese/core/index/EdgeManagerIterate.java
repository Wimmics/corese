package fr.inria.corese.core.index;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.core.edge.EdgeGeneric;
import fr.inria.corese.core.edge.EdgeTop;
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
    EdgeTop buffer;

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
    void fill(EdgeTop buf, Entity ent) {
        if (ent.nbNode() == 2) {
            fillTriple(buf, ent);
        }
        else {
            fillTuple(buf, ent);
        }
    }
    
    void fillTriple(EdgeTop buf, Entity ent){
        buf.setGraph(ent.getGraph());
        buf.replicate(ent);
    }
    
    void fillTuple(EdgeTop buf, Entity ent){
        if (buf.nbNode() == 2) {
            buffer = list.getGraph().getEdgeFactory().createDuplicate(ent); //new EdgeImpl();
            buf = buffer;
            buf.setEdgeNode(list.getPredicate());
        }
        buf.setGraph(ent.getGraph());
        buf.replicate(ent);
    }

    @Override
    public void remove() {
    }

   
}
