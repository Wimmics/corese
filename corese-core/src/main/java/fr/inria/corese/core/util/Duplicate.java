package fr.inria.corese.core.util;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Distinct;
import java.util.HashMap;

/**
 * Remove duplicate candidate edge in Construct
 * Use case: Rule Engine Optimization
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Duplicate {
    
    HashMap<String, Distinct> table;
    
    
    Duplicate(){
        table = new HashMap();
    }
    
    public static Duplicate create(){
        return new Duplicate();
    }
    
    /**
     * Is triple n1 p n2 already stored ?
     */
    public boolean exist(Node p, Node n1, Node n2){
        Distinct d = getDistinct(p.getLabel());
        return ! d.isDistinct(n1, n2);
    }
    
    /**
     * Each property has it's own distinct
     */
    Distinct getDistinct(String name){
        Distinct d = table.get(name);
        if (d == null){
            d =  Distinct.create();
            table.put(name, d);
        }
        return d;
    }
    

}
