package fr.inria.corese.sparql.api;

import java.util.Collection;

/**
 * Visitor that can be associated to a transformation
 * use case:
 * filter(st:visit(st:exp, ?x, true))
 * @author Olivier Corby - INRIA - 2018
 */
public interface TransformVisitor {
    
    void visit(IDatatype name, IDatatype object, IDatatype arg);
    
    IDatatype set(IDatatype obj, IDatatype prop, IDatatype val);
    
    IDatatype get(IDatatype obj, IDatatype prop);
           
    Collection<IDatatype> visited();
    
    boolean isVisited(IDatatype dt);

    public IDatatype visitedGraphNode();
        
    Collection<IDatatype> getErrors(IDatatype dt);
    
    IDatatype errors();
    
}