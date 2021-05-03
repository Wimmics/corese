/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.gui.core;

import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.rule.RuleError;
import fr.inria.corese.gui.query.GraphEngine;
import fr.inria.corese.kgram.api.core.Edge;
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
    boolean doit = false;
    int owl = RuleEngine.OWL_RL;
    GraphEngine engine;
    private boolean trace = false;
    
    Entailment(GraphEngine e){
        engine = e;
    }
    
    @Override
    public void run(){
        try {
            engine.setOWLRL(doit, owl, trace);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
            MainFrame.display(ex.getMessage());
            for (RuleError err : engine.getOwlEngine().getErrorList()) {
                MainFrame.display(err.toString());
            }
            MainFrame.getSingleton().focusMessagePanel();
        }
    }

    void setOWLRL(boolean selected, int owl) {
        doit = selected;
        this.owl = owl;
    }
    
    void process(){
        if (doit){
            // in a // thread
            start();
        }       
    }

    void setTrace(boolean b) {
        trace = b;
    }

}
