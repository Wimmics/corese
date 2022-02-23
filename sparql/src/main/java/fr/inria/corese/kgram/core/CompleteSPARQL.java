/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute select expression, 
 * Compute value of group by, order by  
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class CompleteSPARQL {
    private static Logger logger = LoggerFactory.getLogger(CompleteSPARQL.class);
    
    Eval eval;
    Query query;
    
    CompleteSPARQL(Query q, Eval e){
        this.eval = e;
        this.query = q;
    }

      /**
     * Complete:
     * select (exp as var)
     * group by, order by
     * 
     * @param map 
     */
    void complete(Producer p, Mappings map) throws SparqlException{
        selectExpression(query, p, map);
        distinct(query, map);
        orderGroup(query, p, map);
    }
    
    void distinct(Query q, Mappings map){
        if (q.isAggregate() || ! map.isDistinct()){
            // do nothing
        }
        else {
            ArrayList<Mapping> list = new ArrayList<Mapping>(map.size());
            list.addAll(map.getList());
            map.getList().clear();
            for (Mapping m : list){
                map.submit(m);
            }
        }
    }
    
    Mappings selectExpression(Query q, Producer p, Mappings map) throws SparqlException {
        if (query.isSelectExpression()) {
            HashMap bnode = new HashMap();
            for (Mapping m : map) {
                bnode.clear();
                m.setMap(bnode);
                m.setQuery(q);
                Mapping res = selectExpression(q, p, m);
                if (res == null){
                    logger.warn("Select: exp != var value: " + m);
                }
            }
        }
        return map;
    }
    
    Mapping selectExpression(Query q, Producer p, Mapping m) throws SparqlException {
        ArrayList<Node> ql = new ArrayList<Node>();        
        ArrayList<Node> tl = new ArrayList<Node>();
        
        for (Exp e : q.getSelectFun()) {
            Filter f = e.getFilter();
            if (f != null) {
                // select (exp as ?y)
                if (e.isAggregate()){
                    // processed later, need place holder
                    if (m.getNodeValue(e.getNode()) == null){
                        ql.add(e.getNode());
                        tl.add(null);
                    }
                }
                else {
                    Node qnode = e.getNode();
                    Node tnode = eval.eval(null, f, m, p);
                    if (tnode != null) {
                        Node val = m.getNodeValue(qnode);
                        if (val == null) {
                            // bind e.getNode() = node
                            ql.add(qnode);
                            tl.add(tnode);
                            m.setNodeValue(qnode, tnode);                            
                        } else if (!val.equals(tnode)) {
                            // error: select var != bgp var
                            return null;
                        }
                    }
                }
            }
        }
        
        if (ql.size()>0){
            m.complete(ql, tl);
        }

        return m;
    }
    
    void orderGroup(Query q, Producer p, Mappings map) throws SparqlException{
        for (Mapping m : map){
            Node[] snode = new Node[q.getOrderBy().size()];
            Node[] gnode = new Node[q.getGroupBy().size()];
            orderGroup(q.getOrderBy(), snode, p, m);
            orderGroup(q.getGroupBy(), gnode, p, m);
            m.setOrderBy(snode);
            m.setGroupBy(gnode);
        }
    }
    
    void orderGroup(List<Exp> lExp, Node[] nodes, Producer p, Mapping m) throws SparqlException {
        int n = 0;
        for (Exp e : lExp) {
            Node qNode = e.getNode();
            if (qNode != null) {
                nodes[n] = m.getNodeValue(qNode);
            }
            if (nodes[n] == null) {
                Filter f = e.getFilter();
                if (f != null && !e.isAggregate()) {
                    nodes[n] = eval.eval(null, f, m, p);
                }

            }
            n++;
        }
    }
    
}
