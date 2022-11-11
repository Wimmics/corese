package fr.inria.corese.core.rule;

import fr.inria.corese.kgram.api.core.Node;
import java.util.HashMap;

/**
 * Record property cardinality of rule where clause
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Record  {

    private int timestamp, loop, size;
    private Node predicate;
    private Rule rule;
    private int count = 0;
    
    HashMap<Node, Integer> map;

    Record(Rule r, int timestamp, int loop) {
        rule = r;
        this.timestamp = timestamp;
        this.loop = loop;
        //size = s;
        map = new HashMap();
    }
    
    public String toRDF(){
        String str = "[a kg:Index"
                + " ; kg:rule " + rule.getIndex() 
                + " ; kg:loop " + loop   
                + " ; kg:time " + timestamp ;
               // + " ; kg:size " + size + " ; \n";
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
    boolean accept(Record oldRecord) {
        int n = 0;
        for (Node pred : rule.getPredicates()) {
            if (get(pred) > oldRecord.get(pred)) {
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

    
    public int getTimestamp() {
        return timestamp;
    }

   
    public void setTimestamp(int index) {
        this.timestamp = index;
    }

    
    public int nbNewPredicate() {
        return count;
    }

   
    public void setCount(int count) {
        this.count = count;
    }

    
    public Node getPredicate() {
        return predicate;
    }

   
    public void setPredicate(Node predicate) {
        this.predicate = predicate;
    }

   
    public Rule getRule() {
        return rule;
    }

   
    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
