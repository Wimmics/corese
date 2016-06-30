/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgraph.core.producer;

import fr.inria.edelweiss.kgram.api.core.Entity;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataFilterOr extends DataFilterBoolean {
        
    DataFilterOr(){
    }
       
    @Override
    boolean eval(Entity ent){
        for (DataFilter f : list){
            if (f.eval(ent)){
                return true;
            }
        }
        return false;
    }

}
