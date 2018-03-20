/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.core.util;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class ValueCache {
    
    HashMap<IDatatype, Node> map;
    ArrayList<IDatatype> list;  
    
    int max = 100;
    
    public ValueCache(int n){
        max = n;
        map = new HashMap();
        list = new ArrayList();
    }
    
     public ValueCache(){
        this(100);
    }
    
    public int size(){
        return map.size();
    }
    
    public List<IDatatype> getList(){
        return list;
    }
    
    public Node get(IDatatype dt){
        return map.get(dt);
    }
    
    public void put(IDatatype dt, Node n){
        clean();
        map.put(dt, n);
        list.add(dt);
    }

    private void clean() {
        if (list.size() == max){
            IDatatype dt = list.get(0);
            map.remove(dt);
            list.remove(0);
        }
    }
    

}
