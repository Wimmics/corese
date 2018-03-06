package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import static fr.inria.corese.kgram.tool.Message.NL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Utilitary class for Minus and Optional
 * Compute common variables and whether common variables are always bound
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class MappingSet {

   
    Mappings map;
    Exp exp;
    MappingSet set1, set2;
    HashMap<String, String> 
            union,        // union of variables
            intersection; // intersection of variables    
    List<String> varList;
    boolean isBound = false;
    private boolean debug= false;
    
  
    MappingSet(Mappings map1, Mappings map2) {
        set1 = new MappingSet(map1);
        set2 = new MappingSet(map2);
    }
    
    
    MappingSet(Exp exp, MappingSet s1, MappingSet s2) {
        this.exp = exp;
        this.set1 = s1;
        this.set2 = s2;
    }
    
    Mappings getMappings() {
        return map;
    }
       
    List<String> getVarList() {
        return varList;
    }
    
    boolean isBound() {
        return isBound;
    }
  

    /**
     * Variables in common in map1 and map2
     */
    List<String> computeVarList() {
        return set1.intersectionOfUnion(set2);
    }
     
    /**
     * Common variables bound in every Mapping in map1 and map2 ?
     */
    boolean isBound(List<String> varList) {
        return set1.inIntersection(varList) && set2.inIntersection(varList);
    }
    
    boolean hasIntersection(List<Node> nodeList) {
        for (Node node : nodeList) {
            if (getIntersection().containsKey(node.getLabel())) {
                return true;
            }
        }
        return false;
    }
    
    MappingSet start() {
        varList = computeVarList();
        isBound = isBound(varList);
        if (isDebug()) {
            System.out.println(this);
        }
        if (isBound) {
            set2.getMappings().sort(varList);
        }
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("first: ").append(set1.getUnion()).append(NL);
        sb.append("rest:  ").append(set2.getUnion()).append(NL);
        sb.append("list:  ").append(isBound).append(" ").append(varList);
        return sb.toString();
    }
    
    /**
     * return Mapping in map2 potential compatible with m in map1 for optional
     * If common variables are bound in every Mapping, 
     * find potential compatible Mapping by dichotomy and iterate
     * else return all Mappings
     * PRAGMA: if isBound, map2 has been sorted by start() above
     */
    Iterable<Mapping> getCandidateMappings(Mapping m) {
        if (isBound) {
            return new Iterate(m);
        }
        return set2.getMappings();
    }
    
        
    class Iterate implements Iterable<Mapping>, Iterator<Mapping> {
        
        int n;
        Mapping m;
        Mappings map;
        
        Iterate(Mapping m){
            map = set2.getMappings();
            this.n = map.find(m, varList);
            this.m = m;
        }

        @Override
        public boolean hasNext() {
            return n >= 0 && n < map.size() && map.get(n).optionalCompatible(m, varList);
        }

        @Override
        public Mapping next() {
            return map.get(n++);
        }

        @Override
        public Iterator<Mapping> iterator() {
            return this;
        }
    }
    
    /**
     * Is there one Mapping in map2 minus compatible with map in map1
     */
    boolean minusCompatible(Mapping map) {
        if (varList.isEmpty()) {
            // no common variables
            return false;
        } else {
            if (isBound) {
                // check map compatible by dichotomy in map2
                return set2.getMappings().minusCompatible(map, varList);
            } else {
                for (Mapping minus : set2.getMappings()) {
                    // enumerate map2
                    if (map.minusCompatible(minus, varList)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    
    //------------------------------------------------------
    
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
    
    
     /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    

}
