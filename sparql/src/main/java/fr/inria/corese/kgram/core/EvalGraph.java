package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.List;

/**
 *
 * @author corby
 */
public class EvalGraph {
    
    Eval eval;
    boolean stop = false;
    
    EvalGraph(Eval e) {
        eval = e;
    }
    
    void setStop(boolean b) {
        stop = b;
    }
    
    /**
     * gNode is possible named graphURI stemming from evaluation context
     * It is not the named graph at stake here
     */
    int eval(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        // current named graph URI/VAR
        Node graphNode = exp.getGraphName();
        // URI or value of VAR or null if unbound
        Node graph     = eval.getNode(p, graphNode);
        Mappings res;

        if (graph == null) {
            // named graph VAR is unbound
            // iterate named graph list, using from named if any
            res = graphNodes(p, exp, data, n);
        } else {
            res = graph(p, graph, exp, data, n);
        }

        if (res == null) {
            return backtrack;
        }
        Memory env = eval.getMemory();

        for (Mapping m : res) {
            if (stop) {
                return eval.STOP;
            }            
            
            Node namedGraph = null;
            if (graphNode.isVariable()) {
                namedGraph = m.getNode(graphNode);
                if (namedGraph != null && ! namedGraph.equals(m.getNamedGraph())) {
                    // graph ?g { s p o optional { o q ?g }}
                    // variable ?g bound by exp not equal to named graph variable ?g
                    continue;
                }
            }
            
            if (env.push(m, n)) {
                boolean pop = false;                               
                if (env.push(graphNode, m.getNamedGraph())) {
                    pop = true;
                } else {
                    env.pop(m);
                    continue;
                }

                backtrack = eval.eval(p, gNode, stack, n + 1);
                env.pop(m);
                if (pop) {
                    env.pop(graphNode);
                }
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;
    }

    /**
     * Iterate named graph pattern evaluation on named graph list 
     * named graph list may come from Mappings map  from previous statement
     * OR from the "from named" clause OR from dataset named graph list
     */
    private Mappings graphNodes(Producer p, Exp exp, Mappings map, int n) throws SparqlException {
        Memory env = eval.getMemory();
        Query qq = eval.getQuery();
        Matcher mm = eval.getMatcher();
        int backtrack = n - 1;
        Node name = exp.getGraphName();
        Mappings res = null;
        Iterable<Node> graphNodes = null;
        
        if (map != null && map.inScope(name)) {
            // named graph list may come from evaluation context
            List<Node> list = map.aggregate(name);
            if (!list.isEmpty()) {
                graphNodes = list;
            }
        }
        if (graphNodes == null) {
            // from named clause OR dataset named graph list
            graphNodes = p.getGraphNodes(name, qq.getFrom(name), env);
        }

        for (Node graph : graphNodes) {
            if (mm.match(name, graph, env)) {
                Mappings m = graph(p, graph, exp, map, n);
                if (res == null) {
                    res = m;
                } else {
                    res.add(m);
                }
                env.pop(name);
            }
        }
        return res;
    }

    /**
     * Node graph: graph URI or Node graph pointer or Node path pointer
     * Exp exp: graph name { BGP }
     */
    private Mappings graph(Producer p, Node graph, Exp exp, Mappings map, int n) throws SparqlException {
        int backtrack = n - 1;
        boolean external = false;
        Node graphNode = exp.getGraphName();
        Producer np = p;
        if (graph != null && p.isProducer(graph)) {
            // graph ?g { }
            // named graph in GraphStore 
            np = p.getProducer(graph, eval.getMemory());
            np.setGraphNode(graph);  // the new gNode
            external = true;
        }

        Exp main = exp;
        Exp body = exp.rest();
        Mappings res;
        Node var = null, target = null;
        
        if (external) {
            if (graphNode.isVariable() && graph.getDatatypeValue().isExtension()) {
                var    = graphNode;
                target = graph;
            }
        }
        else {
            target = graph;
        }
        
        if (eval.isFederate(exp)) {
            res = eval.subEval(np, target, var, body, main, map, null, false, external);
        } 
        else {
            Exp ee = body;
            Mappings data = null; 
            
            if (graph.getPath() == null) {
                // not a path pointer
                if (Eval.isParameterGraphMappings()) {
                    // eval graph body with parameter map
                    // pro: if body is optional, eval it with parameter map
                    data = map;
                }
                else {
                    // eval graph body with values(map)
                    ee = body.complete(map);
                }
            }
            
            res = eval.subEval(np, target, var, ee, main, data, null, false, external);
        }
        res.setNamedGraph(graph);

        eval.getVisitor().graph(eval, graph, exp, res);
        return res;
    }

    
}
