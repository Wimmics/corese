package fr.inria.corese.core.index;

import fr.inria.corese.core.edge.EdgeGeneric;
import java.util.Iterator;
import fr.inria.corese.kgram.api.core.Edge;

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
class EdgeManagerIterate implements Iterable<Edge>, Iterator<Edge> {

    EdgeManager list;
    int focusNodeIndex;
    // possibly Edge object Node index
    // use case: subject and object are known
    int objectNodeIndex=-1;
    int ind, start = 0;
    boolean isList = true;
    EdgeGeneric buffer;

    EdgeManagerIterate(EdgeManager l) {
        list = l;
        buffer = new EdgeGeneric(list.getPredicate());
    }

    EdgeManagerIterate(EdgeManager l, int begin) {
       this(l);
       start = begin;
       focusNodeIndex = getFocusNodeIndex(begin);
       isList = false;
    }
    
    EdgeManagerIterate(EdgeManager l, int begin, int objectNodeIndex) {
       this(l, begin);
       this.objectNodeIndex = objectNodeIndex;
    }

    @Override
    public Iterator<Edge> iterator() {
        ind = start;
        return this;
    }
    
    // return node index of focus node at nth position in edge list
    int getFocusNodeIndex(int n) {
        return list.get(n).getNode(list.getIndex()).getIndex();
    }
    
    // return node index of object node at nth position in edge list
    int getObjectNodeIndex(int n) {
        return list.get(n).getNode(1).getIndex();
    }

    @Override
    public boolean hasNext() {
        boolean b = ind < list.size()
                && (isList || getFocusNodeIndex(ind) == focusNodeIndex);
        if (b && objectNodeIndex!=-1) {
            b &= getObjectNodeIndex(ind) == objectNodeIndex;
        }       
        return b;
    }

    @Override
    public Edge next() {
        Edge ent = list.get(ind++);
        if (ent.isInternal()) { 
            fill(buffer, ent);
            return buffer;
        }
        return ent;
    }
    
    /**
     * Fill buffer Edge from internal ent
     */
    void fill(EdgeGeneric buf, Edge ent) {
        buf.setGraph(ent.getGraph());
        buf.replicate(ent);
    }


    @Override
    public void remove() {
    }

   
}
