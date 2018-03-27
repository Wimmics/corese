package fr.inria.corese.core;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.index.NodeManager;
import fr.inria.corese.core.query.QueryProcess;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EventLogger {

    private static Logger logger = LoggerFactory.getLogger(EventLogger.class);
    EventManager mgr;
    QueryProcess exec;
    HashMap<Event, Event> show, hide;
    private boolean method = false;

    EventLogger(EventManager ev) {
        mgr = ev;       
    }
    
    public void show(Event e, boolean b) {
        if (b) {
            show().put(e, e);
        }
        else {
            show().remove(e);
        }
    }
    
    public void hide(Event e, boolean b) {
        if (b) {
            hide().put(e, e);
        } else {
            hide().remove(e);
        }
    }

    public void focus() {
       show().clear();
       hide().clear();
    }
    
    HashMap show() {
        if (show == null) {show = new HashMap<>();}
        return show;
    }
    
    HashMap hide() {
        if (hide == null) {hide = new HashMap<>();}
        return hide;
    }
    
    boolean accept(Event e) {
       return (show().isEmpty() || show().containsKey(e)) &&
             (hide().isEmpty() || ! hide().containsKey(e))  ;
    }

    
    void trace(Event type, Event e, Object o) {
        if (accept(e)) {
            if (isMethod()){
                method(type, e, o);
            }
            else {
                message(type, e, o);
            }
        }
    }
       
    void message(Event type, Event e, Object o) {
        switch (type) {
            case Start:
            case Process:
                switch (e) {
                    case LoadStep: logLoadStep(); break;
                    
                    case Service: 
                        logger.info(type + " " + e + serviceSeparator(o) + o);
                        break;
                    
                    default: logger.info(type + " " + e + pretty(o));
                }
                break;
            case Finish:
                logger.info(type + " " + e);
                break;
        }
        log(type, e, o);
    }
    
    String serviceSeparator(Object o) {
        return (o instanceof String) ? "\n" : " ";
    }
    
    void method(Event type, Event e, Object o) {
        try {
            getQueryProcess().event(type, e, o);
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(EventLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    QueryProcess getQueryProcess() {
        if (exec == null){
            exec = QueryProcess.create(getEventManager().getGraph());
        }
        return exec;
    }

    void log(Event type, Event e, Object o) {
        switch (type) {
            case Start:
                break;
                
            case Finish:
                switch(e) {
                    case Rule: logRule(e);
                    break;
                    
                    case IndexNodeManager: 
                    case IndexNodeManagerReduce: 
                        logNodeManager((o == null) ? getNodeMgr() : (NodeManager) o);
                    break;
                }
                break;
                
            case Process:
                break;
        }
                
        switch (e) {
            case InferenceEngine:
                logInference(type);
                break;
        }
    }

    String pretty(Object o) {
        return (o == null) ? "" : " " + o;
    }
         
    void logLoadStep() {
        logger.info(String.format("Loading: %s : %s", new Date(), mgr.getGraph().size()));
    }

    void logInference(Event e) {
        switch (e) {
            case Start: logger.info("Graph size before: " + mgr.getGraph().size()); break;
            case Finish:logger.info("Graph size after: "  + mgr.getGraph().size()); break;
        }
    }
    
    void logNodeManager(NodeManager nm) {
        logger.info(String.format("index: %s ; subject: %s ; predicate: %s ; ratio: %s ; graph: %s" , 
        nm.getIndex(), nm.size() , nm.count(), (nm.size() > 0) ? ((float) nm.count()) /  nm.size() : 0, 
        mgr.getGraph().size()));
    }
    
    NodeManager getNodeMgr() {
        return mgr.getGraph().getNodeManager();
    }
    
    EventManager getEventManager() {
        return mgr;
    }
    
    void logRule(Event e) {
        logger.info("Graph size after rule: "  + mgr.getGraph().size());
    }

    /**
     * @return the method
     */
    public boolean isMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(boolean method) {
        this.method = method;
    }

}
