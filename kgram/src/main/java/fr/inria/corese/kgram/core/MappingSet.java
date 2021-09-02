package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import static fr.inria.corese.kgram.tool.Message.NL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Utilitary class for Join Minus Optional
 * Compute common variables in left and right expression 
 * and whether common variables are always bound
 * Select subset of Mappings from left exp relevant for right expression
 * optional(s p o, minus(o q t, t r s)) 
 * variable o can be used in minus expression, variable s cannot 
 * special case for union:
 * join(s p o, union(s q t, o q t))
 * there is no common variable bound in branches of union
 * return complete Mappings, then union() will process it in the branches: left s and right o
 * In addition, in case of successful subset of Mappings, return also the original Mappings in case there is 
 * an union in right expression
 * If union is in subquery, the subquery may skip the original Mappings depending on its select
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class MappingSet {
    private Query query;
    Mappings map;
    private Mappings targetMapping;
    Exp exp;
    MappingSet set1, set2;
    HashMap<String, String> 
            union,        // union of variables
            intersection; // intersection of variables    
    List<String> varList;
    boolean isBound = false;
    private boolean debug= false;
    
  
    MappingSet(Query q, Mappings map1, Mappings map2) {
        set1 = new MappingSet(q, map1);
        set2 = new MappingSet(q, map2);
        setQuery(q);
    }
    
    
    MappingSet(Query q, Exp exp, MappingSet s1, MappingSet s2) {
        this.exp = exp;
        this.set1 = s1;
        this.set2 = s2;
        setQuery(q);
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
        sb.append("inter: ").append(getIntersection()).append(NL);
        sb.append("union: ").append(getUnion()).append(NL);
        if (set1 != null) {
            sb.append("first var: ").append(set1.getUnion()).append(NL);
        }
        if (set2 != null) {
            sb.append("rest var:  ").append(set2.getUnion()).append(NL);
        }
        if (varList!=null) {
            sb.append("common var:  ").append(varList).append(" always bound: ").append(isBound);
        }
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
    
    MappingSet(Query q, Mappings map) {
        this.map = map;
        union = new HashMap<>();
        intersection = new HashMap<>();
        setQuery(q);
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
        return inSet(varList, getIntersection());
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
    
     /**
     * @return the targetMapping
     */
    public Mappings getJoinMappings() {
        return targetMapping;
    }

    /**
     * @param targetMapping the targetMapping to set
     */
    public void setJoinMappings(Mappings targetMapping) {
        this.targetMapping = targetMapping;
    }
    
    
     /**
     * exp a Join, Minus, Optional, Union
     */
    Exp prepareRest(Exp exp) {
        Mappings map = prepareMappings(exp);
        return getRest(exp, map);
    }
    
    Exp getRest(Exp exp, Mappings map) {
        return (map == null) ? exp.rest(): setMappings(exp, map);
    }
    
    /**
     * Process intermediate Mappings
     * either as parameter of eval(rest, map)
     * or as values clause inserted in rest
     * exp is A optional B | A minus B | A join B
     */
    Exp setMappings(Exp exp, Mappings map) {
        setJoinMappings(map);
        return exp.rest();
    }
    
    /**
     * Process intermediate Mappings
     * either as parameter of eval(rest, map)
     * or as values clause inserted in rest
     * exp is A optional B | A minus B | A join B
     */
    Exp setMappings2(Exp exp, Mappings map) {
        Exp rest = exp.rest();
        if (exp.isJoin()){ 
            // join is generated to enable us to pass 
            // Mappings map as parameter to right argument
            // join (A, bgp(B))
            // join (A, union(B, C))
            // join (A, graph(B))
            // join (A, service)
            setJoinMappings(map);
        }
        else if (isFederate() || rest.isEvaluableWithMappings()) {
            // user case: 
            // A optional rest | A minus rest
            // and 1) rest recursively starts with service clause 
            // or  2) rest recursively starts with edge/path 
            // --2) special case taken into account in eval and()
            // eval(rest, map) may take Mappings map argument into account
            //System.out.println("parameter");
            setJoinMappings(map);
        } else {
            // inject Mappings map in copy of rest as a values clause
            // eval(values+rest)
            // use case: exp = optional, minus
            rest = rest.complete(map);
        }
        return rest;
    }
    
    boolean isFederate() {
        return getQuery().getGlobalQuery().isFederate();        
    }
    
    Mappings prepareMappings(Exp exp) {
        return prepareMappingsRest(exp.rest());
    }
    
    /**
     *
     * in-scope variables in exp except bind except those that are only in
     * right arg of an optional in exp and skip statements after first
     * union/minus/optional/graph in exp
     *
     */
    Mappings prepareMappingsRest(Exp exp) {
        List<Node> nodeListInScope = exp.getRecordInScopeNodesWithoutBind();
        if (!nodeListInScope.isEmpty() && hasIntersection(nodeListInScope)) {
            // generate values when at least one variable in-subscope is always 
            // bound in map1, otherwise it would generate duplicates in map2
            // or impose irrelevant bindings 
            // map = select distinct map1 wrt exp inscope nodes 
            Mappings map = getMappings().distinct(nodeListInScope);
            map.setNodeList(nodeListInScope);
            // record original Mappings because union in exp may process it
            // more precisely. see Eval unionData()
            // s p o {s q r} union {o q r}
            // map node list = {r}
            // whereas we can get s for first branch and o for second branch
            // this is why we record original Mappings for union in exp if any 
            map.setJoinMappings(getMappings());
            return map;           
        }
        // there is no in-scope variable.
        // return original Mappings in case of union in exp (see comment above)
        return getMappings(); // return null
    }
      
    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
    

}
