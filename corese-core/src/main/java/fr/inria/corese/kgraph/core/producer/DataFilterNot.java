/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgraph.core.producer;

import fr.inria.edelweiss.kgram.api.core.Entity;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataFilterNot extends DataFilterBoolean {
        
    DataFilterNot(){
    }
    
    public DataFilterNot(DataFilter f1){
         add(f1);
    }
       
    @Override
    boolean eval(Entity ent){
        for (DataFilter f : list){
           if (f.eval(ent)){
               return false;
           }
        }
        return true;
    }

}
