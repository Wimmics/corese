package fr.inria.corese.gui.core;

import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.rule.RuleError;
import fr.inria.corese.gui.query.GraphEngine;
import fr.inria.corese.sparql.exceptions.EngineException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Entailment extends Thread {
    private static final Logger logger = LogManager.getLogger(MainFrame.class.getName());
    boolean inThread = true;
    int owl = RuleEngine.OWL_RL;
    private String path;
    GraphEngine engine;
    private boolean trace = false;
    
    Entailment(GraphEngine e){
        engine = e;
    }
    
    Entailment(GraphEngine e, boolean b){
        this(e);
        inThread = b;
    }
    
    @Override
    public void run(){
        try {
            if (getPath()==null) {
                engine.setOWLRL(owl, trace);
            }
            else {
                engine.runRule(getPath());
            }
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
            MainFrame.newline();
            MainFrame.display(ex.getMessage());
            MainFrame.newline();
            for (RuleError err : engine.getRuleEngine(getPath()).getErrorList()) {
                MainFrame.display(err.toString());
            }
            MainFrame.getSingleton().focusMessagePanel();
        } catch (LoadException ex) {
            logger.error(ex);
        }
    }

    void setOWLRL(int owl) {
        this.owl = owl;
    }   
    
    void process(){
        if (inThread){
            // in a // thread
            start();
        }  
        else {
            run();
        }
    }

    void setTrace(boolean b) {
        trace = b;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
