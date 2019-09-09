package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import static fr.inria.corese.kgram.core.Eval.STOP;
import java.util.Date;

/**
 *
 * @author corby
 */
public class EvalJoin {
    
    Eval eval;
    boolean stop = false;
    
    EvalJoin(Eval eval) {
        this.eval = eval;
    }
    
    void setStop(boolean b) {
        stop = b;
    }
    
      /**
     * JOIN(e1, e2) Eval e1, eval e2, generate all joins that are compatible in
     * cartesian product
     */
    int eval(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = eval.getMemory();
        Mappings map1 = eval.subEval(p, gNode, gNode, exp.first(), exp, data);
        if (map1.size() == 0) {
            eval.getVisitor().join(eval, eval.getGraphNode(gNode), exp, map1, map1);
            return backtrack;
        }
        Date d1 = new Date();
        Mappings arg = map1; 
        if (data != null && relevant(exp)) {
            // use case: join(data, values ?s { <uri> }, service ?s { })
            // data may be relevant for service
            arg = map1.join(data);
        }
        MappingSet set1 = new MappingSet(arg);
        Exp rest = eval.prepareRest(exp, set1);
        Date d2 = new Date();
        if (stop) {
            return STOP;
        }
        
        Mappings map2 = eval.subEval(p, gNode, gNode, rest, exp, set1.getJoinMappings());

        eval.getVisitor().join(eval, eval.getGraphNode(gNode), exp, map1, map2);

        if (map2.size() == 0) {
            return backtrack;
        }
       
        return join(p, gNode, stack, env, map1, map2, n);
    }
    
    // use case: join(data, and(values ?s { <uri> }), service ?s { })
    boolean relevant(Exp exp) {
        Exp fst = exp.first();
        return fst.isBGPAnd() && fst.size() == 1 && fst.get(0).isValues() ;
    }

    private int join(Producer p, Node gNode, Exp exp, Mappings map1, Mappings map2, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = eval.getMemory();
        Node qn1 = null, qn2 = null;

        for (int j = n + 1; j < stack.size(); j++) {
            // check if next exp is filter (?x = ?y)
            // where ?x in map1 and ?y in map2
            Exp e = stack.get(j);

            if (!e.isFilter()) {
                break;
            } else if (e.size() == 1 && e.get(0).isBindVar()) {
                // b = BIND(?x, ?y)
                Exp b = e.get(0);
                qn1 = b.get(0).getNode();
                qn2 = b.get(1).getNode();

                // Do the mappings bind  variables ?x and ?y respectively ?
                if ((map1.get(0).getNode(qn1) != null && map2.get(0).getNode(qn2) != null)) {
                    // ok do nothing
                    break;
                } else if ((map1.get(0).getNode(qn2) != null && map2.get(0).getNode(qn1) != null)) {
                    // ok switch variables: qn1 for map1 etc.
                    Node tmp = qn2;
                    qn2 = qn1;
                    qn1 = tmp;
                    break;
                } else {
                    // Mappings do not bind variables
                    qn2 = null;
                }
            }
        }

        if (qn1 != null && qn2 != null) {
            // sort map2 Mappings according to value of ?y
            // in order to perform dichotomy with ?x = ?y
            // ?x in map1, ?y in map2
            map2.sort(eval, qn2);

            // exploit dichotomy for ?x = ?y
            for (Mapping m1 : map1) {
                if (stop) {
                    return STOP;
                }
                Node n1 = m1.getNode(qn1);
                if (n1 != null) {

                    if (env.push(m1, n)) {

                        // index of ?y in map2
                        int nn = map2.find(n1, qn2);

                        if (nn >= 0 && nn < map2.size()) {

                            for (int i = nn; i < map2.size(); i++) {
                                if (stop) {
                                    return STOP;
                                }
                                // enumerate occurrences of ?y in map2
                                Mapping m2 = map2.get(i);
                                Node n2 = m2.getNode(qn2);
                                if (n2 == null || !n1.match(n2)) { // was equals
                                    // as map2 is sorted, if ?x != ?y we can exit the loop
                                    break;
                                } else if (env.push(m2, n)) {
                                    backtrack = eval.eval(p, gNode, stack, n + 1);
                                    env.pop(m2);
                                    if (backtrack < n) {
                                        return backtrack;
                                    }
                                }
                            }

                        }

                        env.pop(m1);
                    } else {
                    }
                }
            }
        } else {
            backtrack = join(p, gNode, stack, env, map1, map2, n);
        }
        return backtrack;
    }

    int join(Producer p, Node gNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) {
        int backtrack = n - 1;

        Node q = map1.getCommonNode(map2);

        if (q == null) {
            // no variable in common : simple cartesian product
            backtrack = simpleJoin(p, gNode, stack, env, map1, map2, n);
        } else {
            // map1 and map2 share a variable q
            // sort map2 on q
            // enumerate map1
            // retrieve the index of value of q in map2 by dichotomy

            if (map1.size() > map2.size()) {
                Mappings tmp = map1;
                map1 = map2;
                map2 = tmp;
            }
            map2.sort(eval, q);
            for (Mapping m1 : map1) {
                if (stop) {
                    return STOP;
                }
                // value of q in map1
                Node n1 = m1.getNode(q);
                if (env.push(m1, n)) {

                    if (n1 == null) {
                        // enumerate all map2
                        for (Mapping m2 : map2) {
                            if (stop) {
                                return STOP;
                            }
                            if (env.push(m2, n)) {
                                backtrack = eval.eval(p, gNode, stack, n + 1);
                                env.pop(m2);
                                if (backtrack < n) {
                                    return backtrack;
                                }
                            }
                        }
                    } else {

                        // first, try : n2 == null
                        for (Mapping m2 : map2) {
                            if (stop) {
                                return STOP;
                            }
                            Node n2 = m2.getNode(q);
                            if (n2 != null) {
                                break;
                            }
                            if (env.push(m2, n)) {
                                backtrack = eval.eval(p, gNode, stack, n + 1);
                                env.pop(m2);
                                if (backtrack < n) {
                                    return backtrack;
                                }
                            }
                        }

                        // second, try : n2 != null
                        int nn = map2.find(n1, q);

                        if (nn >= 0 && nn < map2.size()) {

                            for (int i = nn; i < map2.size(); i++) {

                                // get value of q in map2
                                Mapping m2 = map2.get(i);
                                Node n2 = m2.getNode(q);
                                if (n2 == null || !n1.match(n2)) { // was equals
                                    // as map2 is sorted, if ?x != ?y we can exit the loop
                                    break;
                                } else if (env.push(m2, n)) {
                                    backtrack = eval.eval(p, gNode, stack, n + 1);
                                    env.pop(m2);
                                    if (backtrack < n) {
                                        return backtrack;
                                    }
                                }
                            }
                        }
                    }

                    env.pop(m1);
                }
            }
        }
        return backtrack;
    }

    int simpleJoin(Producer p, Node gNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) {
        int backtrack = n - 1;
        for (Mapping m1 : map1) {
            if (stop) {
                return STOP;
            }
            if (env.push(m1, n)) {

                for (Mapping m2 : map2) {
                    if (stop) {
                        return STOP;
                    }
                    if (env.push(m2, n)) {
                        backtrack = eval.eval(p, gNode, stack, n + 1);
                        env.pop(m2);
                        if (backtrack < n) {
                            return backtrack;
                        }
                    }
                }

                env.pop(m1);
            }
        }
        return backtrack;
    }

    
}
