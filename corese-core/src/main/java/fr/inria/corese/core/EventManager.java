package fr.inria.corese.core;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.parser.Transformer;
import fr.inria.corese.core.api.Engine;
import static fr.inria.corese.core.Event.Finish;
import static fr.inria.corese.core.Event.Process;
import static fr.inria.corese.core.Event.Start;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.LOG_NODE_INDEX;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
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
public class EventManager implements EventHandler {

    private static Logger logger = LoggerFactory.getLogger(EventManager.class);
    public static boolean DEFAULT_VERBOSE = false;

    private Graph graph;
    private boolean verbose    = DEFAULT_VERBOSE;
    private boolean isEntail = true;
    private boolean isUpdate = false;
    private boolean isDelete = false;
    private boolean deletion = false;
    
    
    EventLogger log;
    // User defined Event Handler
    private EventHandler handler;

    EventManager(Graph g) {
        graph = g;
        setEventHandler(this);
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
    
    public void process(Event e, Object o1, Object o2) {
        //trace(Process, e, o1, o2);        
        switch(e) {
            case Insert: 
                setUpdate(true); break;
            case Delete: 
                setDelete(true); break;
            case Finish: 
                finish(); break;
        }
    }
    
    public void process(Event e, Edge o1) {
        switch (e) {
            case Insert:
                getEventHandler().insert(o1);
                setUpdate(true);
                break;
        }
    }
    
    public void process(Event e, Edge target, Edge query) {
        switch(e) {           
            case Delete: 
                getEventHandler().delete(query, target);
                setDelete(true); break;            
        }
    }
    
    /*****************
     * EventHandler
     ****************/
    @Override
    public void delete(Edge q, Edge t) {        
    }
    
    @Override
    public void insert(Edge e) {
    }  
    
    
     
    public void start(Event e, Object o) {
        trace(Start, e, o);
        switch (e) {
            case Query:
                // sparql query 
                // (empty) where part of delete/insert data/where
            case RuleEngine:
            case WorkflowParser:
                // index graph + entailment
                getGraph().init();
                break;
                
            case InitQuery:
                // fake select where init query exec on load for init Visitor
            case InitUpdateQuery:
                // fake select where init query exec on global update query for init Visitor
                break;
                
            case Format:
                // RDF/JSON Format require graph index
            case Process:
                // specific action require graph index
                // basic index graph
                getGraph().indexGraph();
                break;    

            case Update:
                // global update query
                getGraph().startUpdate();
                break;
                
            case UpdateStep: 
                // one step in update query
                break;  
                
            case BasicUpdate:
                // copy, move, etc.
                getGraph().init();
                break;
                
            case LoadUpdate:
            case LoadAPI:
                // sequence of load can be done without graph index
                // specific query which require graph index perform 
                // getGraph().init() or getGraph().indexGraph() here
                getGraph().startLoad();
                break;
                                                
            case Delete:             
            case Insert:
                 break;
                
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
                
            case IndexNodeManager:
                log(LOG_NODE_INDEX, Event.Start, e, o);
                break;
       }
    }
   

    public void finish(Event e, Object o) {
        trace(Finish, e, o);
         switch (e) {
             // insert after delete may require up to date NodeIndex
             // we must compute node index after delete
             case Delete:
             case Insert:
                 getGraph().indexGraph();
                 break; 
                                
            case LoadUpdate: 
                // SPARQL Update load                 
                getGraph().finishUpdate();
                break; 
                
            case LoadAPI:
                // Java function load()
                // there may be several load(), do not index graph yet
                // next sparql query will do it
                getGraph().finishUpdate();
                break;   
                
            case UpdateStep: 
                // one step in update query 
                getGraph().finishUpdate();
                break;  
                                                           
            case Construct: 
                getGraph().indexGraph();
                getGraph().finishUpdate();
                break;
                
            case Rule:
                break;
                                
            case ActivateRDFSEntailment:
                setEntailment(false);
                break;
                
            case ActivateRuleEngine:    
                getWorkflow().setActivate(Engine.RULE_ENGINE, false);
                break;
                
            case RuleEngine:
                getGraph().finishRuleEngine();
                break;
                
            case InferenceEngine:
                break;
                
            case CleanOntology:
                break;
                
            case IndexMetadata:
                break;
                
            case IndexNodeManager:
                log(LOG_NODE_INDEX, Event.Finish, e, o);
                break;
        }
    }
    
    
 
    
    Workflow getWorkflow() {
        return getGraph().getWorkflow();
    }
    
    Entailment getEntailment() {
        return getWorkflow().getEntailment();
    }
    
    void setEntailment(boolean b) {
        if (getEntailment() != null) {
            getEntailment().setActivate(b);
        }
    }
    
    void log(Property.Value prop, Event type, Event e, Object o) {
        if (Property.booleanValue(prop)) {
            getLog().log(type, e, o);
        }
    }

    public void process(Event e, Object o) {
        process(e, o, null);
    }
      
    
    /**
     * GUI reset Corese
     * Clean old environment
     */
    public void finish() {
        if (ASTExtension.getSingleton() != null) {
            ASTExtension.getSingleton().setHierarchy(null);
        }
        Transformer.removeLinkedFunction();
    }
        
    
    void initStatus() {
         isEntail = true;
         isUpdate = false;
         isDelete = false;
         setDeletion(false);
    }
    
    void setUpdate(boolean b) {
        isUpdate = b;
        if (isUpdate) {
            setEntail(true);
            getGraph().eventUpdate();
        }
    }
    
    public boolean isUpdate() {
        return isUpdate;
    }
    
    void setDelete(boolean b) {
        setUpdate(b);
        isDelete = b;
        if (b) {
            setDeletion(true);
        }
    }
    
    public boolean isDelete() {
        return isDelete;
    }
    
    boolean isDeletion() {
        return deletion;
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

    public void setDeletion(boolean deletion) {
        this.deletion = deletion;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public EventHandler getEventHandler() {
        return handler;
    }

    public void setEventHandler(EventHandler handler) {
        this.handler = handler;
    }
    
}
