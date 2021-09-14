package fr.inria.corese.core.query;

import java.util.List;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.Sorter;
import fr.inria.corese.core.Graph;

/**
 * Sort KGRAM edges in connected order before query process Take cardinality
 * into account
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class SorterImpl extends Sorter {

    Graph graph;

    SorterImpl() {
    }

    SorterImpl(Graph g) {
        graph = g;
    }

    public static SorterImpl create(Graph g) {
        return new SorterImpl(g);
    }

    @Override
    public void sort(Query q, Exp exp, List<String> lVar, List<Exp> lBind) {
        super.sort(q, exp, lVar, lBind);
    }

    /**
     * Refine std connected order by taking cardinality of edges into account
     */
    @Override
    protected boolean before(Query q, Exp e1, Exp e2, List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        int n1 = e1.nBind(lNode, lVar, lBind);
        int n2 = e2.nBind(lNode, lVar, lBind);

        if (n1 == 0 && n2 == 0) {
            if (beforeBind(q, e2, e1)) {
                return true;
            }
        }

        if (n1 == n2 && e1.isEdge() && e2.isEdge()) {
            int s1 = graph.size(e1.getEdge().getEdgeNode());
            int s2 = graph.size(e2.getEdge().getEdgeNode());
            if (s2 < s1) {
                return true;
            }
        }

        return n2 > n1;
    }

    @Override
    public boolean leaveFirst() {
        return false;
    }

}
