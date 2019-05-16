/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.gui.core;

import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.gui.query.GraphEngine;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Entailment extends Thread {
    boolean doit = false;
    int owl = RuleEngine.OWL_RL;
    GraphEngine engine;
    private boolean trace = false;
    
    Entailment(GraphEngine e){
        engine = e;
    }
    
    @Override
    public void run(){
        engine.setOWLRL(doit, owl, trace);
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
