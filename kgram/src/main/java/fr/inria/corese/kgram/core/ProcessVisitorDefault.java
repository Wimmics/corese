package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.ProcessVisitor;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class ProcessVisitorDefault implements ProcessVisitor {
    
    public static int SLICE_DEFAULT_VALUE = ProcessVisitor.SLICE_DEFAULT;
    
    int slice = SLICE_DEFAULT_VALUE;

    @Override
    public int slice(Node serv, Mappings map) {
        return slice;
    }

    @Override
    public void setSlice(int n) {
        slice = n;
    }
    
}
