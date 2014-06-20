package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.ExpType;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EMPTY;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.core.Expr;
import static fr.inria.edelweiss.kgram.api.core.ExprType.CONSTANT;
import static fr.inria.edelweiss.kgram.api.core.ExprType.EQ;
import static fr.inria.edelweiss.kgram.api.core.ExprType.IN;
import static fr.inria.edelweiss.kgram.api.core.ExprType.TERM;
import static fr.inria.edelweiss.kgram.api.core.ExprType.VARIABLE;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.filter.Matcher;
import fr.inria.edelweiss.kgram.filter.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic triple pattern graph, mainly contains a list of nodes and a map of
 * nodes and their connected nodes
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class BPGraph {

    // list of nodes
    private List<BPGNode> nodes = null;
    // data structure that used to represents the graph
    // map(node, list of connected nodes)
    private Map<BPGNode, List<BPGNode>> graph = null;
    private Map<String, List<String>> boundValues = null;

    //for the moment, we just consider the AND and atomic relation 
    public BPGraph(Exp exp) {
        if (exp.type() != ExpType.AND) {
            return;
        }

        nodes = new ArrayList<BPGNode>();
        boundValues = new HashMap<String, List<String>>();
        
        createNodeList(exp);
        createGraph();
    }

    // Encapsulate expression into BPGNode and add them to a list
    private void createNodeList(Exp exp) {
        for (Exp ee : exp) {
            //TODO other types
            if (ee.type() == Exp.FILTER || ee.type() == EDGE || ee.type() == Exp.VALUES) {
                nodes.add(new BPGNode(ee));
                checkBindings(ee);
            }
        }
    }

    //Check the special cases in filter and values to find the bound constants
    private void checkBindings(Exp ee) {
        if (ee.type() == EDGE) {
            return;
        }

        //values (?x ?y) {(1 2 ) (3 4)}
        if (ee.type() == VALUES) {
            Mappings mm = ee.getMappings();
            for (Node n : ee.getNodeList()) {
                List values = new ArrayList();

                for (Mapping m : mm) {
                    if (m.isBound(n)) {
                        values.add(m.getValue(n).toString());
                    }
                }
                this.addBindings(n.getLabel(), values);
            }
        }

        //2 cases
        //1); filter (?x == 2)
        //2): filter ?x IN (2, 4)
        if (ee.type() == FILTER) {
            Expr expr = ee.getFilter().getExp();

            Matcher matcher = new Matcher();
            // ********** ?x = 'constant' **************
            if (matcher.match(new Pattern(TERM, EQ, VARIABLE, CONSTANT), expr)) {
                List<Expr> ls = expr.getExpList();
                List values = new ArrayList();
                //not sure the order of variable and constant
                if (ls.size() == 2) {
                    if (ls.get(0).type() == VARIABLE) {
                        values.add(ls.get(1).getLabel());
                        this.addBindings(ls.get(0).getLabel(), values);
                    } else {
                        values.add(ls.get(0).getLabel());
                        this.addBindings(ls.get(1).getLabel(), values);
                    }
                }
            }

            // *********** ?x IN {1, 2, 3} **************
            if (expr.oper() == IN) {
                List<Expr> ls = expr.getExpList();
                if (ls.size() == 2) {
                    List values = getValues(ls.get(1).getExpList());
                    this.addBindings(ls.get(0).getLabel(), values);
                }
            }
        }
    }

    //Get the values from a list of Expr
    private List<String> getValues(List<Expr> list) {
        List l = new ArrayList();
        for (Expr e : list) {
            l.add(e.getLabel());
        }
        return l;
    }

    //Add the bound key/values to the list
    private void addBindings(String key, List<String> values) {
        if (this.boundValues.containsKey(key)) {
            this.boundValues.get(key).addAll(values);
        } else {
            this.boundValues.put(key, values);
        }
    }

    //Create graph structure by finding variables sharing between nodes
    private void createGraph() {
        // Graph Structure:
        // Edge 1: connected edge11, edge 12, ...
        // Edge 2: connected edge 21, edge 22, ...\
        // ...
        // Filter 1: Connected edge 111, edge 112, ...
        // Filter 2...
        // ...
        graph = new HashMap<BPGNode, List<BPGNode>>();
        for (BPGNode bpn : nodes) {
            List<BPGNode> lnodes = new ArrayList<BPGNode>();
            for (BPGNode bpn2 : nodes) {
                if (!bpn.equals(bpn2) && bpn.isShared(bpn2)) {
                    lnodes.add(bpn2);
                }
            }
            graph.put(bpn, lnodes);
        }
    }

    /**
     * Return the list of values for variable that binds to constants/list, etc..
     * 
     * @param n
     * @return null if not found, otherwise the list of values
     */
    public List<String> isBound(Node n) {
        return boundValues.get(n.getLabel());
    }

    /**
     * Return list of nodes contained in the triple pattern graph
     *
     * @return
     */
    public List<BPGNode> getNodeList() {
       return this.getNodeList(EMPTY);
    }

    /**
     * Return list of nodes that contain triple pattern expression (edge)
     *
     * @param type EDGE, VALUES, FILTER, otherwise return all
     * @return
     */
    public List<BPGNode> getNodeList(int type) {
        if(type != EDGE || type != VALUES || type!=FILTER){
            return this.nodes;
        }
        
        List<BPGNode> list = new ArrayList<BPGNode>();
        for (BPGNode node : this.nodes) {
            if (node.getType() == type) {
                list.add(node);
            }
        }
        return list;
    }
    
    public Map<BPGNode, List<BPGNode>> getGraph(){
        return this.graph;
    }
}
