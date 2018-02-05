package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class MappingSet {
    
    Mappings map;
    HashMap<String, String> 
            union,        // union of variables
            intersection; // intersction of variables
    
    MappingSet(Mappings map) {
        this.map = map;
        union = new HashMap<>();
        intersection = new HashMap<>();
        process();
    }
    
    List<String> intersectionOfUnion(MappingSet model) {
        ArrayList<String> varList = new ArrayList<>();
        for (String var : getUnion().keySet()) {
            if (model.getUnion().containsKey(var)) {
                varList.add(var);
            }
        }
        return varList;
    }
    
    boolean inIntersection(List<String> varList) {
        return inSet(varList, intersection);
    }
    
    boolean inSet(List<String> varList, HashMap<String, String> table) {
        for (String var : varList) {
            if (! table.containsKey(var)) {
                return false;
            }
        }
        return true;
    }
    
    
    
    HashMap<String, String> getUnion() {
        return union;
    }
    
    HashMap<String, String> getIntersection() {
        return intersection;
    }
      
    /**
     * compute union and intersection of variables in Mappings map
     */
    void process() {
        if ( ! map.isEmpty()) {
            Mapping m = map.get(0);
            for (String var : m.getVariableNames()) {
                intersection.put(var, var);
            }
        }
        List<String> remove = new ArrayList<>();
        for (Mapping m : map) {
            for (String var : m.getVariableNames()) {
                union.put(var, var);
            }
            remove.clear();
            for (String var : intersection.keySet()) {
                if (m.getNodeValue(var) == null) {
                    remove.add(var);
                }
            }
            for (String var : remove) {
                intersection.remove(var);
            }
        }
    }

}
