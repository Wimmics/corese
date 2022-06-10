package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import static fr.inria.corese.kgram.core.Eval.STOP;
import java.util.Date;
import static fr.inria.corese.kgram.core.Eval.DISPLAY_RESULT_MAX;

/**
 *
 * @author corby
 */
public class EvalJoin {
    public static boolean SORT_OVERLOAD = true;
    public static boolean DEBUG_JOIN = false;

    Eval eval;
    boolean stop = false;
    boolean debug = DEBUG_JOIN;
    
    EvalJoin(Eval eval) {
        this.eval = eval;
    }
    
    void setStop(boolean b) {
        stop = b;
    }
           
    Query getQuery() {
        return eval.getMemory().getQuery();
    }
    
      /**
     * JOIN(e1, e2) Eval e1, eval e2, generate all joins that are compatible in
     * cartesian product
     */
    int eval(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        if (debug) {
            if (data!=null) {
                System.out.println("join 1st with data:\n" + data);
            }
        }
        int backtrack = n - 1;
        Memory env = eval.getMemory();
        Mappings map1 = eval.subEval(p, graphNode, graphNode, exp.first(), exp, data);
        if (map1.size() == 0) {
            eval.getVisitor().join(eval, eval.getGraphNode(graphNode), exp, map1, map1);
            return backtrack;
        }
        Date d1 = new Date();
        Mappings map1Extended = map1; 
        if (data != null && isFirstWithValuesOnly(exp)) {
            // use case: join(values ?s { <uri> }, service ?s { })
            // where values specify endpoint URL for service and 
            // previous data contains relevant bindings for service in rest
            // let's give a chance to pass relevant data to rest (service)
            // although there is values in between
            map1Extended = map1.join(data);
        }

        Date d2 = new Date();
        if (stop) {
            return STOP;
        }
        
        MappingSet set1 = new MappingSet(getQuery(), map1Extended);
        Mappings joinMappings = null;
        if (eval.isJoinMappings()) {
            joinMappings = set1.prepareMappingsRest(exp.rest());
            if (debug) {
                System.out.println("join 2nd with data:\n" + joinMappings);
            }
        }
        Mappings map2 = eval.subEval(p, graphNode, graphNode, exp.rest(), exp, joinMappings);

        eval.getVisitor().join(eval, eval.getGraphNode(graphNode), exp, map1, map2);

        if (map2.size() == 0) {
            return backtrack;
        }
        if (eval.isDebug()) {
            System.out.println("join map1:\n" + 
                    map1.toString(false, false, DISPLAY_RESULT_MAX));
            System.out.println("join map2:\n" + 
                    map2.toString(false, false, DISPLAY_RESULT_MAX));
        }
        
        return join(p, graphNode, stack, env, map1, map2, n);
    }
    


    int join(Producer p, Node graphNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) throws SparqlException {
        Node commonVariable = map1.getCommonNode(map2);
        if (commonVariable == null) {
            return joinWithoutCommonVariable(p, graphNode, stack, env, map1, map2, n);
        } else {
            return joinWithCommonVariable(commonVariable, p, graphNode, stack, env, map1, map2, n);
        }
    }
    
    /**
     * Try to find common variable var in both mappings
     * if any: sort second map wrt var
     * iterate first map, find occurrence of same value of var by dichotomy in second map 
     * map1 and map2 share commonVariable
     * sort map2 on commonVariable
     * enumerate map1
     * retrieve the index of value of commonVariable in map2 by dichotomy
     * 
     */
    int joinWithCommonVariable(Node commonVariable, Producer p, Node graphNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) throws SparqlException {
        int backtrack = n - 1;
        if (map1.size() > map2.size()) {
            Mappings tmp = map1;
            map1 = map2;
            map2 = tmp;
        }
        if (debug) {
            System.out.println("join:");
            System.out.println(map1);
            System.out.println(map2);
        }        
        if (SORT_OVERLOAD) {
            // setEval enable node comparison overload by Visitor compare() for extended datatypes
            map2.setEval(eval);
        }
        map2.sort(commonVariable);
        
        for (Mapping m1 : map1) {
            if (stop) {
                return STOP;
            }

            Node n1 = m1.getNode(commonVariable);
            if (env.push(m1, n)) {

                if (n1 == null) {
                    // enumerate all map2
                    for (Mapping m2 : map2) {
                        if (stop) {
                            return STOP;
                        }
                        if (env.push(m2, n)) {
                            if (debug) {
                                System.out.println("join 1:\n" + m1 + "\n" + m2);
                            }
                            backtrack = eval.eval(p, graphNode, stack, n + 1);
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
                        Node n2 = m2.getNode(commonVariable);
                        if (n2 != null) {
                            break;
                        }
                        if (env.push(m2, n)) {
                            backtrack = eval.eval(p, graphNode, stack, n + 1);
                            env.pop(m2);
                            if (backtrack < n) {
                                return backtrack;
                            }
                        }
                    }

                    // second, try : n2 != null
                    int nn = map2.find(n1, commonVariable);
                    if (nn >= 0 && nn < map2.size()) {

                        for (int i = nn; i < map2.size(); i++) {
                            // get value of var in map2
                            Mapping m2 = map2.get(i);
                            Node n2 = m2.getNode(commonVariable);
                            
                            if (n2 == null || !n1.match(n2)) { 
                                // map2 is sorted, if n1 != n2 we can exit the loop
                                break;
                            } else if (env.push(m2, n)) {
                                if (debug) {
                                    System.out.println("join 2:\n" + m1 + "\n" + m2);
                                }
                                backtrack = eval.eval(p, graphNode, stack, n + 1);
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
        return backtrack;
    }

    /**
     * No variable in common: cartesian product of mappings
     */
    int joinWithoutCommonVariable(Producer p, Node graphNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) throws SparqlException {
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
                        backtrack = eval.eval(p, graphNode, stack, n + 1);
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
    
    
        
    // use case: join(and(values ?s { <uri> }), service ?s { })
    boolean isFirstWithValuesOnly(Exp exp) {
        Exp fst = exp.first();
        return fst.isBGPAnd() && fst.size() == 1 && fst.get(0).isValues() ;
    }
    
    //    int eval2(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
//        int backtrack = n - 1;
//        Memory env = eval.getMemory();
//        Mappings map1 = eval.subEval(p, gNode, gNode, exp.first(), exp, data);
//        if (map1.size() == 0) {
//            eval.getVisitor().join(eval, eval.getGraphNode(gNode), exp, map1, map1);
//            return backtrack;
//        }
//        Date d1 = new Date();
//        Mappings map1Extended = map1; 
//        if (data != null && isFirstWithValuesOnly(exp)) {
//            // use case: join(values ?s { <uri> }, service ?s { })
//            // where values specify endpoint URL for service and 
//            // previous data contains relevant bindings for service in rest
//            // let's give a chance to pass relevant data to rest (service)
//            // although there is values in between
//            map1Extended = map1.join(data);
//        }
//
//        Date d2 = new Date();
//        if (stop) {
//            return STOP;
//        }
//        
//        MappingSet set1 = new MappingSet(getQuery(), map1Extended);
//        Exp rest = set1.prepareRest(exp);
//        Mappings map2 = eval.subEval(p, gNode, gNode, rest, exp, set1.getJoinMappings());
//
//        eval.getVisitor().join(eval, eval.getGraphNode(gNode), exp, map1, map2);
//
//        if (map2.size() == 0) {
//            return backtrack;
//        }
//       
//        return join(p, gNode, stack, env, map1, map2, n);
//    }
    
    @Deprecated
    private int join(Producer p, Node graphNode, Exp exp, Mappings map1, Mappings map2, Stack stack, int n) throws SparqlException {
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
                                if (n2 == null || !n1.match(n2)) { 
                                    // as map2 is sorted, if ?x != ?y we can exit the loop
                                    break;
                                } else if (env.push(m2, n)) {
                                    backtrack = eval.eval(p, graphNode, stack, n + 1);
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
        } 
//        else {
//            backtrack = joinWithCommonVariable(p, graphNode, stack, env, map1, map2, n);
//        }
        return backtrack;
    }
    

    
}
