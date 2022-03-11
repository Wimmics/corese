package fr.inria.corese.core;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Graphable;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.query.RDFizer;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.QueryLoad;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 * Graph Execution Context
 * Store History
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Context implements Graphable {

    private ArrayList<Node> queryNodes;
    private Node ruleEngineNode;
    RuleEngine re;
    private int max = 10;
    
    Graph graph;

    Context(Graph g) {
        this.graph = g;
        queryNodes = new ArrayList(max);
    }

    /**
     * @return the queryNode
     */
    public Node getQueryNode() {
        if (queryNodes.isEmpty()) {
            return null;
        }       
        return queryNodes.get(queryNodes.size() - 1);
    }
    
    Node getRE(){
        RuleEngine re = RuleEngine.create(Graph.create());
        for (Node n : queryNodes){
            Query q = (Query) n.getNodeObject();
            re.defRule(q);
        }
        Node res = DatatypeMap.createObject("RuleEngine", re);
        return res;
    }

    /**
     * -1 is all queries
     * 0 is last, 1 is before last ...
     */
    public Node getQueryNode(int n) {
        if (n == -1){
            return getRE();
        }
        int i = queryNodes.size() - 1 - n ;
        if (i >= 0 && i < queryNodes.size()){
            return queryNodes.get(i);
        }
        return null;
    }

    /**
     * @param queryNode the queryNode to set
     */
    public void setQuery(Query q) {
        while (queryNodes.size() >= max) {
            queryNodes.remove(0);
        }
        queryNodes.add(create("Query", q, IDatatype.QUERY));
    }

    /**
     * @return the ruleEngineNode
     */
    public Node getRuleEngineNode() {
        return ruleEngineNode;
    }
    
    public Node getRecordNode() {
        if (re == null){
            return null;
        }
        return create("Record", re.getRecord());
    }

    /**
     * @param ruleEngineNode the ruleEngineNode to set
     */
    public void setRuleEngine(RuleEngine re) {
        this.re = re;
        this.ruleEngineNode = create("RuleEngine", re);
    }
    
    Node create(String name, Object obj){
        return DatatypeMap.createObject(name, obj);
    }
    
    Node create(String name, Object obj, String dt){
        return DatatypeMap.createObject(name, obj, dt);
    }
    
    /**
     * @return the max
     */
    public int getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(int max) {
        this.max = max;
    }
    
    public void storeIndex(String name){
        Graph g = new RDFizer().getGraph(graph.describe());
        graph.setNamedGraph(name, g);
    }

    @Override
    public String toGraph() {
        StringBuilder sb = new StringBuilder();
        //sb.append(toRDF());
        sb.append(graph.toRDF());
        return sb.toString();
    }

    @Override
    public void setGraph(Object obj) {
    }

    @Override
    public Object getGraph() {
        return null;
    }
    
    String toRDF(){
        QueryLoad ql = QueryLoad.create();
         String str = "";
        try {
            str = ql.getResource("/data/kgram.ttl");
        } catch (IOException ex) {
            LoggerFactory.getLogger(Context.class.getName()).error("", ex);
        }
        return str;
    }
}
