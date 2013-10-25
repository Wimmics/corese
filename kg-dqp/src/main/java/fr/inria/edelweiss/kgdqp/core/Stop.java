/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 *
 * @author gaignard
 */
public class Stop implements Entity {

    @Override
    public Edge getEdge() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node getNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node getNode(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node getGraph() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return "STOP";
    }

    @Override
    public int nbNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Object getProvenance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProvenance(Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
