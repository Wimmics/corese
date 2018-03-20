/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.core.producer;

import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataFilterBoolean extends DataFilter {
    
    ArrayList<DataFilter> list;
    
    DataFilterBoolean(){
        list = new ArrayList<DataFilter>();
    }
    
    @Override
    ArrayList<DataFilter> getList(){
        return list;
    }
    
    @Override
    boolean isBoolean(){
        return true;
    }
    
    @Override
    DataFilter add(DataFilter f){
        list.add(f);
        return this;
    } 
    
    @Override
    boolean setFilter(DataFilter f){
        switch (list.size()){
            case 0: add(f); return true;
                
            case 1:
                DataFilter df = list.get(0);
                if (df.isBoolean()){
                    if (df.setFilter(f)){
                        return true;
                    }
                }
                add(f); return true;

                
            case 2: df = list.get(1);
                if (df.isBoolean()){
                    if (df.setFilter(f)){
                       return true;
                    }
                }
                return false;
        }
        
        return true;
    }
}
