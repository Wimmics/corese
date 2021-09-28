package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class ProcessVisitorDefault implements ProcessVisitor {
    
    public static int SLICE_DEFAULT_VALUE = ProcessVisitor.SLICE_DEFAULT;
    
    int slice = SLICE_DEFAULT_VALUE;
    IDatatype defaultValue;

    @Override
    public int slice() {
        return slice;
    }
    
    @Override
    public int slice(Node serv, Mappings map) {
        return slice;
    }

    @Override
    public int setSlice(int n) {
        slice = n;
        return n;
    }
    
    @Override
    public void setDefaultValue(IDatatype val) {
        defaultValue = val;
    }
    
    @Override
    public IDatatype defaultValue() {
        return defaultValue;
    }

    
}
