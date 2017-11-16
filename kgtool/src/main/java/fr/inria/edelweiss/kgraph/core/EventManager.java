package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EventManager {

    private static Logger logger = LogManager.getLogger(EventManager.class);
    Graph graph;
    private boolean debug = false;

    EventManager(Graph g) {
        graph = g;
    }
    
    public void send(Event type, Event e) {
        send(type, e, null);
    }


    public void send(Event type, Event e, Object o) {
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
        switch (e) {
            case Query:
            case RuleEngine:
            case Workflow:
                graph.init();
                break;
                
            case Format:
            case Process:
                graph.doIndex();
                break;    

            case Update:
                graph.startUpdate();
                break;
                
            case LoadAPI:
                graph.setUpdate(true);
                break;  
                                
            case Insert:
            case Delete:             
            case Construct:     
                 break;
                
            case ActivateEntailment:
                graph.setEntail(true);
                break;
                
            case ActivateRDFSEntailment:
                setEntailment(true);
                graph.setEntail(true);
                break;
                
            case ActivateRuleEngine:    
                getWorkflow().setActivate(Engine.RULE_ENGINE, true);
                graph.setEntail(true); 
                break;
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

    public void finish(Event e) {
         switch (e) {
            case Insert:
            case Delete:             
            case Construct: 
            case LoadUpdate:    
                graph.prepare();            
                break;
                
            case LoadAPI: break;
                
            case ActivateRDFSEntailment:
                setEntailment(false);
                break;
                
            case ActivateRuleEngine:    
                getWorkflow().setActivate(Engine.RULE_ENGINE, false);
                break;
        }
    }

    public void process(Event e) {
        switch(e) {
            case Insert: graph.setUpdate(true);
            case Delete: graph.setDelete(true);
        }
    }

    void trace(Event type, Event e, Object o) {
        if (debug) {
            switch (type) {
                case Start:
                case Process:
                    logger.info("Event: " + type + " " + e + " " + o);
                    break;
                case Finish:
                    logger.info("Event: " + type + " " + e);
                    break;
            }
        }
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
