package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Metadata;

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
    
    
    void visit(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {
        if (eval.getQuery().getGlobalAST().hasMetadata(Metadata.REPORT)) {
            eval.getBind().visit(e, g, m1, m2);
        }
    }
    
    @Override
    public IDatatype graph(Eval eval, Node g, Exp e, Mappings m1) {         
        visit(eval, g, e, m1, null);
        return defaultValue(); 
    } 
    
    @Override
    public IDatatype query(Eval eval, Node g, Exp e, Mappings m1) {         
        visit(eval, g, e, m1, null);
        return defaultValue(); 
    } 
    
    @Override
    public IDatatype service(Eval eval, Node g, Exp e, Mappings m1) {         
        visit(eval, g, e, m1, null);
        return defaultValue(); 
    } 
    
    @Override
    public IDatatype optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {         
        visit(eval, g, e, m1, m2);
        return defaultValue(); 
    } 

    @Override
    public IDatatype minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {         
        visit(eval, g, e, m1, m2);
        return defaultValue(); 
    } 
    
    @Override
    public IDatatype union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {         
        visit(eval, g, e, m1, m2);
        return defaultValue(); 
    } 
    
    @Override
    public IDatatype join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {         
        visit(eval, g, e, m1, m2);
        return defaultValue(); 
    } 
}
