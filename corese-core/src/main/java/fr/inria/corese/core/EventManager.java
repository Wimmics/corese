package fr.inria.corese.core;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.parser.Transformer;
import fr.inria.corese.core.api.Engine;
import static fr.inria.corese.core.Event.Finish;
import static fr.inria.corese.core.Event.Process;
import static fr.inria.corese.core.Event.Start;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.sparql.triple.update.Update;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Event Manager for consistency and trace
 * Update and Inference are tracked
 * Debug mode traces events
 * Show and hide events.
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EventManager {

    private static Logger logger = LoggerFactory.getLogger(EventManager.class);
    public static boolean DEFAULT_VERBOSE = false;
    Graph graph;
    private boolean verbose    = DEFAULT_VERBOSE;
    private boolean isEntail = true;
    private boolean isUpdate = false;
    private boolean isDelete = false;
    private boolean isDeletion = false;
    
    EventLogger log;

    EventManager(Graph g) {
        graph = g;
    }
    
    public void setMethod(boolean b){
        getLog().setMethod(b);
    }
    
    public void show(Event e) {
        getLog().show(e, true);
    }
    public void show(Event e, boolean status) {
        getLog().show(e, status);
    }
    public void hide(Event e) {
        getLog().hide(e, true);
    }
    public void hide(Event e, boolean status) {
        getLog().hide(e, status);
    }
    public void focus() {
       getLog().focus();
    }
    
    
    Graph getGraph() {
        return graph;
    }

    void send(Event type, Event e) {
        send(type, e, null);
    }
    
    void send(Event type, Event e, Object o) {
        trace(type, e, o);
        switch (type) {
            case Start:
                start(e);
                break;
            case Finish:
                finish(e);
                break;
            case Process:
                process(e);
                break;
        }
    }
    
    public void start(Event e) {
        start(e, null);
    }
    
    public void finish(Event e) {
        finish(e, null);
    }
    
    public void process(Event e) {
        process(e, null);
    }
     
    public void start(Event e, Object o) {
        trace(Start, e, o);
        switch (e) {
            case Query:
            case RuleEngine:
            case WorkflowParser:
                graph.init();
                break;
                
            case Format:
            case Process:
                graph.indexGraph();
                break;    

            case Update:
                graph.startUpdate();
                break;
                
            case UpdateStep:                
                break;    
                
            case LoadUpdate:
            case LoadAPI:
                startLoad();
                break;
                
//            case LoadAPI:
//                setUpdate(true);
//                break;  
                                
            case Insert:
            case Delete:             
            case Construct:     
                 break;
                
            case ActivateEntailment:
                setEntail(true);
                break;
             
                // Update create/drop kg:rule/kg:entailment
            case ActivateRDFSEntailment:
                setEntailment(true);
                setEntail(true);
                break;
                
            case ActivateRuleEngine:    
                getWorkflow().setActivate(Engine.RULE_ENGINE, true);
                setEntail(true); 
                break;
                
            case InferenceEngine:
                break;
                
            case CleanOntology:
                break;
       }
    }
    
    void startLoad() {
        if (graph.size() == 0) {
            // graph is empty, optimize loading as if the graph is to be indexed
            // because in this case, edges are added directly
            graph.setIndex(true);
        }
    }
    
    Workflow getWorkflow() {
        return graph.getWorkflow();
    }
    
    Entailment getEntailment() {
        return getWorkflow().getEntailment();
    }
    
    void setEntailment(boolean b) {
        if (getEntailment() != null) {
            getEntailment().setActivate(b);
        }
    }

    public void finish(Event e, Object o) {
        trace(Finish, e, o);
         switch (e) {
            case Insert:
            case Delete:             
            case LoadUpdate:    
                graph.indexGraph();            
                break; 
                
            case Construct: 
                graph.indexGraph();
                // continue
                
            case LoadAPI: 
            case UpdateStep:                
                graph.finishUpdate();
                break;            
                
            case Rule:
                break;
                                
            case ActivateRDFSEntailment:
                setEntailment(false);
                break;
                
            case ActivateRuleEngine:    
                getWorkflow().setActivate(Engine.RULE_ENGINE, false);
                break;
                
            case InferenceEngine:
                break;
                
            case CleanOntology:
                break;
                
            case IndexMetadata:
                break;
        }
    }

    public void process(Event e, Object o) {
        process(e, o, null);
    }
    
    public void process(Event e, Object o1, Object o2) {
        trace(Process, e, o1, o2);        
        switch(e) {
            case Insert: 
                setUpdate(true); break;
            case Delete: setDelete(true); break;
            case Finish: finish(); break;
        }
    }
    
    /**
     * GUI reset Corese
     * Clean old environment
     */
    public void finish() {
        if (Interpreter.getExtension() != null) {
            Interpreter.getExtension().setHierarchy(null);
        }
        Transformer.removeLinkedFunction();
    }
        
    
    void initStatus() {
         isEntail = true;
         isUpdate = false;
         isDelete = false;
         isDeletion = false;
    }
    
    void setUpdate(boolean b) {
        isUpdate = b;
        if (isUpdate) {
            setEntail(true);
            graph.eventUpdate();
        }
    }
    
    public boolean isUpdate() {
        return isUpdate;
    }
    
    void setDelete(boolean b) {
        setUpdate(b);
        isDelete = b;
        if (b) {
            isDeletion = true;
        }
    }
    
    public boolean isDelete() {
        return isDelete;
    }
    
    boolean isDeletion() {
        return isDeletion;
    }
    
    void setEntail(boolean b) {
        isEntail = b;
    }

    public boolean isEntail() {
        return isEntail;
    }
    
    
    void trace(Event type, Event e) {
        trace(type, e, null);
    }

    void trace(Event type, Event e, Object o) {
        trace(type, e, o, null);
    }
    void trace(Event type, Event e, Object o, Object o2) {
        if (verbose) {
            getLog().trace(type, e, o, o2);
        }
    }
    
    EventLogger getLog() {
        if (log == null) {
            log = new EventLogger(this);
        }
        return log;
    }
    
    /**
     * @return the debug
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param debug the debug to set
     */
    public void setVerbose(boolean debug) {
        this.verbose = debug;
    }
    
    public void setTrackUpdate(boolean track) {
        setVerbose(track);
        getLog().setMethod(track);
    }
    
    
}
