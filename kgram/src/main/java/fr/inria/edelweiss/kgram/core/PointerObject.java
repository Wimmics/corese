/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Pointerable;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public abstract class PointerObject implements Pointerable {

    @Override
    public int pointerType() {
        return UNDEF;
    }

    @Override
    public Mappings getMappings() {
        return null;    
    }

    @Override
    public Mapping getMapping() {
        return null;
    }

    @Override
    public Entity getEntity() {
        return null;
    }
    
    @Override
    public int size(){
        return 0;
    }
    
    @Override
    public Object getValue(String var, int n){
        return null;
    }

}
