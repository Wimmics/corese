package fr.inria.edelweiss.kgram.sorter.impl;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
import fr.inria.edelweiss.kgram.sorter.core.IProducer;
import fr.inria.edelweiss.kgram.sorter.core.TriplePattern;
import java.util.ArrayList;
import java.util.List;

/**
 * SortByHeuristics.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 juin 2014
 */
public class HeuristicsBasedEstimation implements IEstimateSelectivity {
    
    private final List<String> selectNodes = new ArrayList<String>();
    public static boolean modeSimple = false;
    private BPGraph graph;
    
    @Override
    public void estimate(BPGraph plein, Producer producer, Object listSelectNodes) {
        if (listSelectNodes instanceof List && producer instanceof IProducer) {
            graph = plein;
            //** 1 Get list of select nodes
            List<Exp> list = (List<Exp>) listSelectNodes;
            for (Exp exp : list) {
                selectNodes.add(exp.getNode().getLabel());
            }

            //**2 estimate
            List<TriplePattern> patterns = new ArrayList<TriplePattern>();
            for (BPGNode n : plein.getNodeList()) {
                if (n.getType() != EDGE) {
                    n.setSelectivity(Double.MAX_VALUE);
                    continue;
                }
                TriplePattern p = new TriplePattern(n);
                p.setParameters(selectNodes, (IProducer) producer, graph);
                patterns.add(p);
            }

            //** 3 sort by priority
            TriplePattern.sort(patterns);

            //** 4 assign selectivity
            for (int i = 0; i < patterns.size(); i++) {
                TriplePattern pattern = patterns.get(i);
                pattern.setSelectivity(patterns.size(), i);
            }
            
        } else {
            System.err.println("!! The object type is not compitable, should be list<Exp> refering the select nodes !!");
        }
    }
}
