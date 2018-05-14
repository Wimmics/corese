/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.core.producer;

import fr.inria.corese.kgram.api.core.Edge;

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
    boolean eval(Edge ent){
        for (DataFilter f : list){
           if (f.eval(ent)){
               return false;
           }
        }
        return true;
    }

}
