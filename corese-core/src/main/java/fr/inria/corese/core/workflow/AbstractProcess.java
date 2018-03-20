package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public interface AbstractProcess {
    
    static final String PREF = NSManager.SWL;
    static final String GRAPH = PREF + "graph";
    static final String PROBE = PREF + "probe";
            
   
    void subscribe(SemanticWorkflow w);
    
    String stringValue(Data data);
        
    void setContext(Context c);
    
    void inherit(Context c);

    void setDataset(Dataset ds);

    void inherit(Dataset ds);
    
    void setDebug(boolean b);
    
    boolean isDisplay();
    
    boolean isDebug();
    
    boolean isTransformation();
    
    void setProbe(boolean b);

    void setDisplay(boolean b);
    
    void setResult(String r);
    
    void setURI(String uri);
    
    void setName(String name);
}
