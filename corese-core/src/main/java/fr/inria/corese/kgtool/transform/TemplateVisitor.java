package fr.inria.corese.kgtool.transform;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgraph.core.Graph;
import java.util.Collection;

/**
 * Visitor that can be associated to a transformation
 * use case:
 * filter(st:visit(st:exp, ?x, true))
 * @author Olivier Corby - INRIA I3S -2015
 */
public interface TemplateVisitor {
    
    void visit(IDatatype name, IDatatype object, IDatatype arg);
    
    IDatatype set(IDatatype obj, IDatatype prop, IDatatype val);
    
    IDatatype get(IDatatype obj, IDatatype prop);
   
    void setGraph(Graph g);
        
    Collection<IDatatype> visited();
    
    boolean isVisited(IDatatype dt);

    public IDatatype visitedGraphNode();
    
    public Graph visitedGraph();
    
    Collection<IDatatype> getErrors(IDatatype dt);
    
}
