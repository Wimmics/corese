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
                if (n.getType() == EDGE) {
                    TriplePattern p = n.getPattern();
                    p.setParameters(selectNodes, (IProducer) producer, graph);
                    patterns.add(p);
                }
            }

            //** 3 sort by priority
            TriplePattern.sort(patterns);

            //** 4 assign selectivity
            assign(patterns);
            System.out.println();
        } else {
            System.err.println("!! The object type is not compitable, should be list<Exp> refering the select nodes !!");
        }
    }

    public void assign(List<TriplePattern> patterns) {
        List<List<TriplePattern>> patternList = new ArrayList<List<TriplePattern>>();
        //** 1 Group the patterns by their pattern
        for (int i = 0; i < patterns.size(); i++) {
            List l = new ArrayList<TriplePattern>();

            TriplePattern tp = patterns.get(i);
            l.add(tp);
            for (int j = i + 1; j < patterns.size(); j++) {
                if (tp.match(patterns.get(j))) {
                    l.add(patterns.get(j));
                    i++;
                } else {
                    break;
                }
            }
            patternList.add(l);
        }

        //** 2 assign selectivity
        for (int i = 0; i < patternList.size(); i++) {
            for (TriplePattern tp : patternList.get(i)) {
                tp.setSelectivity(patternList.size(), i);
            }
        }
    }
}
