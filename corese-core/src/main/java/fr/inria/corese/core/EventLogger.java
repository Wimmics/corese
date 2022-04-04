package fr.inria.corese.core;

import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.index.NodeManager;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;
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
    public static boolean DEFAULT_METHOD = false;
    static final String INSERT_FUN = NSManager.USER + "insert";
    static final String DELETE_FUN = NSManager.USER + "delete";
    
    EventManager mgr;
    QueryProcess exec;
    HashMap<Event, Event> show, hide;
    private boolean method = DEFAULT_METHOD;

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
    
    void trace(Event type, Event e, Object o, Object o2) {
        if (accept(e)) {
            if (isMethod()){
                method(type, e, o, o2);
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
                switch (e) {
                    case IndexNodeManager:
                        logger.info(type + " " + e);
                        break;
                        
                    default: logger.info(type + " " + e + pretty(o));
                }
                break;
        }
        log(type, e, o);
    }
    
    String serviceSeparator(Object o) {
        return (o instanceof String) ? "\n" : " ";
    }
    
    void method(Event type, Event e, Object o, Object o2) {
        if (!true) {
            methodcall(type, e, o);
        }  
        else {
            funcall(type, e, o, o2);
        }
    }
    
    void methodcall(Event type, Event e, Object o) {
        try {
            getQueryProcess().event(type, e, o);
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(EventLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Manage update event with LDScript function
     * @public us:insert(edge)
     * @public us:delete(edge)
     * When insert, o = target edge, o2 = null
     * When delete, o = target edge, o2 = query edge
     * Call delete with query edge because target edge has null predicate in the graph Index
     */
    void funcall(Event type, Event e, Object o, Object o2) {
        boolean b = getEventManager().isVerbose();
        getEventManager().setVerbose(false);
        try {
            switch (type) {
                case Process:
                    switch (e) {
                        case Insert:
                            //funcall(INSERT_FUN, (Edge)o);
                            insert((Edge)o);
                            break;
                        case Delete:
                            //funcall(DELETE_FUN, (Edge) ((o2 == null) ? o : o2));
                            delete((Edge)((o2 == null) ? o : o2));
                            break;
                    }
            }
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(EventLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            getEventManager().setVerbose(b);
        }
    }
    
    IDatatype funcall(String name, Edge edge) throws EngineException {
        return getQueryProcess().funcall(name, DatatypeMap.createObject(edge));
    }
    
    DatatypeValue insert(Edge edge) throws EngineException {
        return DatatypeMap.TRUE;
    }
    
    DatatypeValue delete(Edge edge) throws EngineException {
        return getQueryProcess().getVisitor().delete(edge);
    }
    
    QueryProcess getQueryProcess() {
        if (exec == null){
            exec = QueryProcess.create(getEventManager().getGraph());
            try {
                exec.getCreateEval().setVisitor(new QuerySolverVisitor(exec.getCreateEval()));
            } catch (EngineException ex) {
                java.util.logging.Logger.getLogger(EventLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        logger.info(String.format("index: %s ; subject: %s ; predicate: %s ; ratio (p/s): %s ; graph: %s" , 
        nm.getIndex(), nm.size() , nm.count(), 
        (nm.size() > 0) ? ((float) nm.count()) /  nm.size() : 0, 
        mgr.getGraph().size()));
//        if (nm.getIndex()==-1) {
//            logger.info(nm.toString());
//        }
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
