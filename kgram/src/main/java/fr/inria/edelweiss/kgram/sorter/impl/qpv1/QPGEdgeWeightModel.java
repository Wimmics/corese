package fr.inria.edelweiss.kgram.sorter.impl.qpv1;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import fr.inria.edelweiss.kgram.sorter.core.AbstractCostModel;
import fr.inria.edelweiss.kgram.sorter.core.QPGEdge;
import fr.inria.edelweiss.kgram.sorter.core.QPGNode;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import static fr.inria.edelweiss.kgram.sorter.core.IEstimate.MAX_COST;
import static fr.inria.edelweiss.kgram.sorter.core.QPGNode.O;
import static fr.inria.edelweiss.kgram.sorter.core.QPGNode.P;
import static fr.inria.edelweiss.kgram.sorter.core.QPGNode.S;
import java.util.List;

/**
 * QPGEdgeWeight.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 oct. 2014
 */
public class QPGEdgeWeightModel extends AbstractCostModel {

    //weight 6, 5, 4, 3, 2, 1
    private static final int[][] JOINT_PATTERN = new int[][]{
        {P, O}, {S, P}, {S, O}, {O, O}, {S, S}, {P, P}
    };
    private final QPGEdge edge;
    private int Nshare = 0;
    private int Jtype = -1;

    public QPGEdgeWeightModel(QPGEdge edge) {
        this.edge = edge;
        if (this.estimatable()) {
            if(this.edge.get(0).getType()== EDGE &&this.edge.get(1).getType()== EDGE){
                this.setJtype();
            }
            this.setNshare();
            this.edge.setType(QPGEdge.BI_DIRECT);
        }else{
            this.edge.setType(QPGEdge.SIMPLE);
        }
    }

    private void setNshare() {
        this.Nshare = this.edge.getVariables().size();
    }

    private void setJtype() {
        QPGNode node1 = edge.get(0), node2 = edge.get(1);

        int[][] jp = JOINT_PATTERN;
        for (int i = 0; i < jp.length; i++) {
            int p1 = jp[i][0], p2 = jp[i][1];
            
            if (node1.get(p1).getLabel().equals(node2.get(p2).getLabel())
                    || node1.get(p2).getLabel().equals(node2.get(p1).getLabel())) {
                Jtype = jp.length - i;
            }
        }
    }

    @Override
    public void estimate(List<Object> params) {
        if(!(isParametersOK(params) && estimatable())){
            this.edge.setCost(IEstimate.NA_COST);
            return;
        }
        
        QPGNode node1 = edge.get(0), node2 = edge.get(1);

        int tNode1 = node1.getType(), tNode2 = node2.getType();
        //1. type of one of them is FILTER or VALUES, ne assign pas le weight
        if (tNode1 == FILTER || tNode2 == FILTER
                || tNode1 == VALUES || tNode2 == VALUES) {
            this.edge.setCost(MAX_COST);
            return;
        }

        //3.2. no pattern matched: means no shared variables
        if (Nshare == 0) {
            this.edge.setCost(MAX_COST);
            return;
        }
        
        //2 The EDGE connects at least a GRAPH
        if (tNode1 == GRAPH || tNode2 == GRAPH) {
            this.edge.setCost(1.0 / 3.0 * Nshare);
            return;
        }

        //3. two EDGEs
        //3.2. no pattern matched: means no shared variables
        if (Jtype == -1) {
            this.edge.setCost(MAX_COST);
            return;
        }

        //3.3 pattern matched, assign weight
        this.edge.setCost(1.0 / Jtype * 1.0 / Nshare);
    }

    @Override
    public String toString() {
        return "QPGEdgeWeightModel{" + "Nshare=" + Nshare + ", Jtype=" + Jtype + '}';
    }

    @Override
    public boolean isParametersOK(List<Object> params) {
        return true;
    }

    @Override
    final public boolean estimatable() {
        int tn1 = this.edge.get(0).getType();
        int tn2 = this.edge.get(1).getType();

        return ((tn1 == EDGE && (tn2 == GRAPH || tn2 == EDGE))
                || (tn2 == EDGE && (tn1 == GRAPH || tn1 == EDGE)));
    }
}
