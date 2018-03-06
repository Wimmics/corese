package fr.inria.corese.kgram.sorter.impl.qpv1;

import static fr.inria.corese.kgram.api.core.ExpType.EDGE;
import static fr.inria.corese.kgram.api.core.ExpType.GRAPH;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.sorter.core.AbstractCostModel;
import fr.inria.corese.kgram.sorter.core.QPGEdge;
import fr.inria.corese.kgram.sorter.core.QPGNode;
import fr.inria.corese.kgram.sorter.core.QPGraph;
import fr.inria.corese.kgram.sorter.core.IEstimate;
import fr.inria.corese.kgram.sorter.core.IProducerQP;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Estimate the selectivity of triple pattern by heuristics
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 juin 2014
 */
public class HeuristicsBasedEstimation implements IEstimate {

    private QPGraph graph;
    private IProducerQP producer;

    @Override
    public void estimate(QPGraph graph, Producer producer, Object parameters) {
        this.graph = graph;
        this.producer = (producer instanceof IProducerQP) ? (IProducerQP) producer : null;

        estimateNodes();
        estimateEdges();
    }

    //assign costs for nodes based on ?-tuple pattern
    private void estimateNodes() {
        // 1. get all models of nodes in QPG
        List<QPGNodeCostModel> models = new ArrayList<QPGNodeCostModel>();
        for (QPGNode n : this.graph.getAllNodes()) {
            if (n.getType() == EDGE || n.getType() == GRAPH) {
                QPGNodeCostModel p = n.getCostModel();
                if (n.getType() == EDGE) {
                    p.setParameters(graph);
                }
                models.add(p);
            }
        }

        //** 3 sort by priority
        //generate basic patterns using numbers({Ns Np No} | null)
        int[][] basicPatterns = BasicPatternGenerator.generateBasicPattern(producer, true);
        QPGNodeCostModel.sort(models, basicPatterns, producer);

        //--put the same triple patterns in one list and assign the same selectivity to
        //--all the patterns in this list, needs to be improved by distinguwish the same patterns 
        //--using stats data if available
        // l1: t11, t12
        // l2: t21
        // l3: t31, t32, t33
        // l4..
        List<List<QPGNodeCostModel>> modelList = new ArrayList<List<QPGNodeCostModel>>();
        //** 1 Group the patterns by their pattern
        for (int i = 0; i < models.size(); i++) {
            List l = new ArrayList<QPGNodeCostModel>();

            QPGNodeCostModel model = models.get(i);
            l.add(model);
            for (int j = i + 1; j < models.size(); j++) {
                if (QPGNodeCostModel.compareModel(model, models.get(j), basicPatterns, producer) == 0) {
                    l.add(models.get(j));
                    i++;
                } else {
                    break;
                }
            }
            modelList.add(l);
        }

        //** 2 assign cost
        for (int i = 0; i < modelList.size(); i++) {
            for (AbstractCostModel model : modelList.get(i)) {
                model.estimate(Arrays.asList(new Object[]{modelList.size(), i}));
            }
        }
    }

    //assign weight/sel for edge between triple pattern
    private void estimateEdges() {
        for (QPGEdge edge : graph.getEdges(QPGEdge.BI_DIRECT)) {
            edge.getCostModel().estimate(null);
        }
    }
}
