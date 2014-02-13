package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Sort KGRAM edges in connected order before query process
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Sorter {

    /**
     * Ensure edges are connected lVar is the list of var that are already bound
     */
    public void sort(Query q, Exp exp, List<String> lVar, List<Exp> lBind) {
        int hasService = sorting(q, exp, lVar, lBind);
        if (hasService > 0) { // && q.isOptimize()){
            // replace pattern . service by join(pattern, service)
            service(exp, hasService);
        }
    }

    /**
     * It is a service and it has an URI (not a variable)
     */
    boolean isService(Exp exp) {
        return exp.type() == Exp.SERVICE && !exp.first().getNode().isVariable();
    }

    int sorting(Query q, Exp exp, List<String> lVar, List<Exp> lBind) {
        int hasService = 0;

        List<Node> lNode = new ArrayList<Node>();

        for (int i = 0; i < exp.size(); i++) {
            Exp e1 = exp.get(i);
            if (isService(e1)) {
                hasService += 1;
            }

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

//                if (i > 0 && q.isTest()) {
//                    check(q, e1, lNode);
//                }
                e1.bind(lNode);
            }
        }

        return hasService;
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

        if (n1 == 0 && n2 == 0) {
            if (beforeBind(q, e2, e1)) {
                return true;
            }
        }
        return n2 > n1;
    }

    /**
     * Edge with node in bindings has advantage
     */
    protected boolean beforeBind(Query q, Exp e2, Exp e1) {
        Mappings list = q.getMappings();
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

    /**
     * Draft: for each service in exp replace pattern . service by join(pattern,
     * service) it may be join(service, service)
     */
    void service(Exp exp, int nbs) {

        if (nbs < 1 || (nbs == 1 && isService(exp.get(0)))) {
            // nothing to do
            return;
        }

        int count = 0;
        int i = 0;

        Exp and = Exp.create(Exp.AND);

        while (count < nbs) {
            // there are services

            while (!isService(exp.get(i))) {
                // find next service
                and.add(exp.get(i));
                exp.remove(i);
            }

            // exp.get(i) is a service
            count++;

            if (and.size() == 0) {
                and.add(exp.get(i));
            } else {
                Exp join = Exp.create(Exp.JOIN, and, exp.get(i));
                and = Exp.create(Exp.AND);
                and.add(join);
            }

            exp.remove(i);

        }

        while (exp.size() > 0) {
            // no more service
            and.add(exp.get(0));
            exp.remove(0);
        }

        exp.add(and);

    }
}
