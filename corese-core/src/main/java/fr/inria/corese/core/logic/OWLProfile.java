package fr.inria.corese.core.logic;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.transform.DefaultVisitor;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import java.util.Map;

/**
 * OWL Profile type checker
 */
public class OWLProfile {
    public static final String OWL_RL = Transformer.OWL_RL;
    public static final String OWL_EL = Transformer.OWL_EL;
    public static final String OWL_QL = Transformer.OWL_QL;
    public static final String OWL_TC = Transformer.OWL_TC;
    
    private Graph graph;
    private Map<IDatatype, IDatatype> error;
    private String message;

              
    public OWLProfile (Graph g) {
        setGraph(g);
    }
       
    // default is OWL Checker
    public boolean process() throws EngineException {
        return process(OWL_TC);
    }
        
    public boolean process(String type) throws EngineException {
        Transformer checker = Transformer.create(getGraph(), type);
        TemplateVisitor vis = new DefaultVisitor();
        Binding b = Binding.create();
        b.setTransformerVisitor(vis);
        checker.process(b);
        
        Transformer printer = Transformer.create(getGraph(), Transformer.PP_ERROR); 
        IDatatype errors = vis.errors();
        IDatatype dt = printer.process(Transformer.PP_ERROR_DISPLAY, b, 
                DatatypeMap.newInstance(getTitle(type)), errors);
        if (dt !=null) {
            setMessage(dt.stringValue());
        }
        
        setError(errors.getMap());
        return getError().isEmpty();
    }
    
    String getTitle(String type) {
        switch (type) {
            case OWL_QL: return "OWL QL";
            case OWL_EL: return "OWL EL";
            case OWL_RL: return "OWL RL"; 
            case OWL_TC:  
            default:     return "OWL";
        }
    }
        
    public IDatatype pretty(IDatatype dt) throws EngineException {
        if (dt.isBlank()) {
            Transformer t = Transformer.create(getGraph(), Transformer.TURTLE);
            return t.process(dt);
        }
        else {
            return dt;
        }
    }
    

    
  
    

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Map<IDatatype, IDatatype> getError() {
        return error;
    }

    public void setError(Map<IDatatype, IDatatype> error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
    
}
