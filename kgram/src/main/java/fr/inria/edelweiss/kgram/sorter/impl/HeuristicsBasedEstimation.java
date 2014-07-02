package fr.inria.edelweiss.kgram.sorter.impl;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.BPGEdge;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
import fr.inria.edelweiss.kgram.sorter.core.IProducer;
import fr.inria.edelweiss.kgram.sorter.core.TriplePattern;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.JOINT_PATTERN;
import java.util.ArrayList;
import java.util.List;

/**
 * Estimate the selectivity of triple pattern by rules, not based on stats
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

            //** 4 assign selectivity of single node
            assignSel(patterns);

            //** 5 assign weights for edges
            assignSelForEdge();
        } else {
            System.err.println("!! The object type is not compitable, should be list<Exp> refering the select nodes !!");
        }
    }

    //assign selectivity for nodes based on 6-tuple pattern
    private void assignSel(List<TriplePattern> patterns) {
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

    //assign weight/sel for edge between triple pattern
    private void assignSelForEdge() {
        for (BPGEdge edge : graph.getEdgeList()) {
            assignSelForEdge(edge);
        }
    }

    public static void assignSelForEdge(BPGEdge edge) {
        BPGNode n1 = edge.get(0), n2 = edge.get(1);
        int[][] jp = JOINT_PATTERN;
        //one of them is filter
        if (n1.getType() == FILTER || n2.getType() == FILTER
                || n1.getType() == VALUES || n2.getType() == VALUES) {
            edge.setWeight(MAX_SEL);
            return;
        }

        //two edges
        for (int i = 0; i < jp.length; i++) {
            int p1 = jp[i][0], p2 = jp[i][1];
            //bp[p1] == BPGNode.UNBOUND && bp[p2]==BPGNode.UNBOUND&&
            if (n1.get(p1).getLabel().equals(n2.get(p2).getLabel())
                    || n1.get(p2).getLabel().equals(n2.get(p1).getLabel())) {
                //matched
                double weight = (MAX_SEL - MIN_SEL) * (i + 1) / (jp.length);
                edge.setWeight(weight);
                return;
            }
        }

        //no pattern matched: means one( or both) of the patterns is (s p o) all bound
        edge.setWeight(MIN_SEL);
    }
}
