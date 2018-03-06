package fr.inria.corese.kgraph.rule;

import fr.inria.corese.kgram.api.core.Node;
import java.util.HashMap;

/**
 * Record property cardinality of rule where clause
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Record  {

    private int time, loop, size;
    private Node predicate;
    private Rule rule;
    private int count = 0;
    
    HashMap<Node, Integer> map;

    Record(Rule r, int n, int l, int s) {
        rule = r;
        time = n;
        loop = l;
        size = s;
        map = new HashMap();
    }
    
    public String toRDF(){
        String str = "[a kg:Index"
                + " ; kg:rule " + rule.getIndex() 
                + " ; kg:loop " + loop   
                + " ; kg:time " + time 
                + " ; kg:size " + size + " ; \n";
        for (Node p : map.keySet()){
            Integer n = map.get(p);
            str += "kg:item [ rdf:predicate " + p + " ; rdf:value " + n + " ] ;\n";
        }
        str += "] .\n";
        return str;
    }

    /**
     * Accept a rule if there are new triples
     */
    boolean accept(Record told) {
        if (rule.getQuery().hasFunctional()){
            return true;
        }
        
        int n = 0;
        for (Node pred : rule.getPredicates()) {
            if (get(pred) > told.get(pred)) {
                n++;
                setPredicate(pred);
            }
        }
        setCount(n);
        return n > 0 ;
    }
    
     void trace(Record tnew){
        Record told = this;
        for (Node pred : rule.getPredicates()) {
            if (tnew.get(pred) > told.get(pred)) {
                double dd = ((double)(tnew.get(pred) - told.get(pred))) / (double)tnew.get(pred);
                System.out.println(pred + " : " + (tnew.get(pred) - told.get(pred)) + "/" + tnew.get(pred) + " = " + dd);
            }
        }
    }
    
    Integer get(Node n){
        return map.get(n);
    }
    
    void put(Node n, Integer i){
        map.put(n, i);
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return time;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.time = index;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the predicate
     */
    public Node getPredicate() {
        return predicate;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(Node predicate) {
        this.predicate = predicate;
    }

    /**
     * @return the rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
