package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import static fr.inria.corese.kgram.core.Eval.STOP;

/**
 *
 * @author corby
 */
public class EvalOptional {

    /**
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }
    
    private boolean stop = false;
    Eval eval;
    
    EvalOptional(Eval e) {
        eval = e;
    }
    
    
    /**
     *
     * A optional B Filter F map1 = eval(A) ; map2 = eval(values Vb {
     * distinct(map1/Vb) } B) Vb = variables in-subscope in B, ie in-scope
     * except in right arg of an optional in B for m1 in map1: for m2 in map2:
     * if m1.compatible(m2): merge = m1.merge(m2) if eval(F(merge)) result +=
     * merge ...
     */
    int eval(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = eval.getMemory();
        Node queryNode = null;
       
        Mappings map1 = eval.subEval(p, gNode, queryNode, exp.first(), exp, data);
        if (isStop()) {
            return STOP;
        }
        if (map1.isEmpty()) {
            return backtrack;
        }

        MappingSet set1 = new MappingSet(map1);
        Exp rest = eval.prepareRest(exp, set1);
        /**
         * Push bindings from map1 into rest when there is at least one variable
         * in-subscope of rest that is always bound in map1 ?x p ?y optional {
         * ?y q ?z } -> values ?y { y1 yn } {?x p ?y optional { ?y q ?z }}
         * optional { ?z r ?t } -> if ?z is not bound in every map1, generate no
         * values.
         */
        Mappings map2 = eval.subEval(p, gNode, queryNode, rest, exp, set1.getJoinMappings());

        eval.getVisitor().optional(eval, eval.getGraphNode(gNode), exp, map1, map2);

        MappingSet set = new MappingSet(exp, set1, new MappingSet(map2));
        set.setDebug(env.getQuery().isDebug());
        set.start();

        for (Mapping m1 : map1) {
            if (isStop()) {
                return STOP;
            }
            boolean success = false;
            int nbsuc = 0;
            for (Mapping m2 : set.getCandidateMappings(m1)) {
                if (isStop()) {
                    return STOP;
                }
                Mapping merge = m1.merge(m2);
                if (merge != null) {
                    success = filter(env, queryNode, gNode, merge, exp);
                    if (success) {
                        nbsuc++;
                        if (env.push(merge, n)) {                                                    
                            backtrack = eval.eval(p, gNode, stack, n + 1);
                            env.pop(merge);
                            if (backtrack < n) {
                                return backtrack;
                            }
                        }
                    }
                }
            }

            if (nbsuc == 0) {
                if (env.push(m1, n)) {                   
                    backtrack = eval.eval(p, gNode, stack, n + 1);
                    env.pop(m1);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }

        return backtrack;
    }

    /**
     * proxyGraphNode is the fake graphNode ?_kgram_ that is a proxy for the
     * named graph ?g
     */
    boolean filter(Environment memory, Node queryNode, Node gNode, Mapping map, Exp exp) throws SparqlException {
        if (exp.isPostpone()) {
            // A optional B
            // filters of B must be evaluated now
            for (Exp f : exp.getPostpone()) {
                map.setQuery(memory.getQuery());
                map.setMap(memory.getMap());
                map.setBind(memory.getBind());
                //map.setGraphNode(queryNode);
                map.setGraphNode(gNode);
                map.setEval(eval);
                boolean b = eval.test(f.getFilter(), map);
                map.setGraphNode(null);
                if (eval.hasFilter) {
                    b = eval.getVisitor().filter(eval, gNode, f.getFilter().getExp(), b);
                }
                if (!b) {
                    return false;
                }
            }
        }
        return true;
    }

    
}
