package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Node;

/**
 * Sort KGRAM edges in connected order before query process
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Sorter {
   
   
    public void sort(Query q, Exp exp, List<String> lVar, List<Exp> lBind) {
        int hasService = 0;

        List<Node> lNode = new ArrayList<Node>();

        for (int i = 0; i < exp.size(); i++) {
            Exp e1 = exp.get(i);
           
            if (e1.isSortable()) {
                if (lNode.isEmpty() && lVar.isEmpty() && leaveFirst()) {
                    // let first edge at its place
                } else {
                    for (int j = i + 1; j < exp.size(); j++) {
                        Exp e2 = exp.get(j);
                        if (e2.isOption()) {
                            // cannot move option because it may bind free variables
                            // that may influence next exp
                            break;
                        } else if (e2.isSortable()) {

                            if (shift(q, e1, e2, lNode, lVar, lBind)) {
                                // ej<ei : put ej at i and shift
                                for (int k = j; k > i; k--) {
                                    // shift to the right
                                    exp.set(k, exp.get(k - 1));
                                }
                                exp.set(i, e2);
                                e1 = e2;
                                //break;
                            }
                        }
                    }
                }

                e1.bind(lNode);
            }
        }

    }

    void check(Query q, Exp exp, List<Node> list) {
        if (!(exp.isEdge() || exp.isPath())) {
            return;
        }

        boolean connect = false;

        for (int i = 0; i < exp.nbNode(); i++) {
            Node n = exp.getNode(i);
            if (list.contains(n)) {
                connect = true;
                break;
            }
        }

        if (!connect) {
            q.addInfo("Disconnected: ", exp);
            System.out.println("Disconnect: " + exp);
        }
    }

    public boolean leaveFirst() {
        return true;
    }

    /**
     * variable node bound count 2 constant node count 1 variable node not bound
     * count 0
     */
    boolean shift(Query q, Exp e1, Exp e2, List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        if (e1.isGraphPath(e2, e1)) {
            return true;
        }
        if (e1.isGraphPath(e1, e2)) {
            return false;
        }

        return before(q, e1, e2, lNode, lVar, lBind);
    }

    protected boolean before(Query q, Exp e1, Exp e2, List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        int n1 = e1.nBind(lNode, lVar, lBind);
        int n2 = e2.nBind(lNode, lVar, lBind);
        
        if (beforeBind(q, e2, e1)){
            n2 += 1;
        }

//        if (n1 == 0 && n2 == 0) {
//            if (beforeBind(q, e2, e1)) {
//                return true;
//            }
//        }
        return n2 > n1;
    }

    /**
     * Edge with node in bindings has advantage
     */
    protected boolean beforeBind(Query q, Exp e2, Exp e1) {
        Mappings list = q.getActualMappings();
        if (list != null && list.size() > 0) {
            Mapping map = list.get(0);
            if (e1.bind(map)) {
                return false;
            }
            if (e2.bind(map)) {
                return true;
            }
        }
        return false;
    }

  
}
