/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.gui.core;

import fr.inria.corese.gui.query.GraphEngine;


/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Entailment extends Thread {
    boolean doit = false, lite=false;
    GraphEngine engine;
    private boolean trace = false;
    
    Entailment(GraphEngine e){
        engine = e;
    }
    
    public void run(){
        engine.setOWLRL(doit, lite, trace);
    }

    void setOWLRL(boolean selected, boolean b) {
        doit = selected;
        lite = b;
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
