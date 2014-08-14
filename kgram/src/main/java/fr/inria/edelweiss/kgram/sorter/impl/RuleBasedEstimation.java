package fr.inria.edelweiss.kgram.sorter.impl;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.BPGEdge;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import fr.inria.edelweiss.kgram.sorter.core.IProducer;
import fr.inria.edelweiss.kgram.sorter.core.TriplePattern;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.JOINT_PATTERN;
import fr.inria.edelweiss.kgram.sorter.core.Utility;
import java.util.ArrayList;
import java.util.List;

/**
 * Estimate the selectivity of triple pattern by rules, not based on stats
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 juin 2014
 */
public class RuleBasedEstimation implements IEstimate {

    public static boolean modeSimple = false;
    private BPGraph graph;
    private IProducer meta = null;

    @Override
    public void estimate(BPGraph plein, Producer producer, Object utility) {
        graph = plein;
        //**1 get stats if available
        int[] numbers = null;
        if (producer instanceof IProducer) {
            meta = (IProducer) producer;
            if (meta.statsEnabled()) {
                numbers = new int[]{meta.getResourceNumber(), meta.getPropertyNumber(), meta.getObjectNumber()};
            }
        }

        //**2 estimate
        List<TriplePattern> patterns = new ArrayList<TriplePattern>();
        for (BPGNode n : plein.getNodeList()) {
            if (n.getType() == EDGE) {
                TriplePattern p = n.getPattern();
                p.setParameters(graph);
                patterns.add(p);
            }
        }

        //** 3 sort by priority
        //generate basic patterns using numbers({Ns Np No} | null)
        int[][] basicPatterns = Utility.generateBasicPattern(numbers, true);
        TriplePattern.sort(patterns, basicPatterns, false, meta);

        //** 4 assign selectivity for nodes
        assignSelForNode(patterns);

        //** 5 assign weights for edges
        assignSelForEdge();
    }

    //assign selectivity for nodes based on ?-tuple pattern
    private void assignSelForNode(List<TriplePattern> patterns) {
        //todo, BUG
        //--put the same triple patterns in one list and assign the same selectivity to
        //--all the patterns in this list, needs to be improved by distinguwish the same patterns 
        //--using stats data if available
        // l1: t11, t12
        // l2: t21
        // l3: t31, t32, t33
        // l4..
        List<List<TriplePattern>> patternList = new ArrayList<List<TriplePattern>>();
        //** 1 Group the patterns by their pattern
        for (int i = 0; i < patterns.size(); i++) {
            List l = new ArrayList<TriplePattern>();

            TriplePattern tp = patterns.get(i);
            l.add(tp);
            for (int j = i + 1; j < patterns.size(); j++) {
                if (tp.match(patterns.get(j), meta)) {
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

    private void assignSelForEdge(BPGEdge edge) {
        BPGNode n1 = edge.get(0), n2 = edge.get(1);
        int[][] jp = JOINT_PATTERN;
        //1. type of one of them is FILTER or VALUES
        if (n1.getType() == FILTER || n2.getType() == FILTER
                || n1.getType() == VALUES || n2.getType() == VALUES) {
            edge.setWeight(MAX_SEL);
            return;
        }

        //2. two edges
        //2.1 find the order in the pre-deinfed list of jointed pattern
        int seq = 1;
        for (int i = 0; i < jp.length; i++) {
            int p1 = jp[i][0], p2 = jp[i][1];
            //!!TODO 
            //bp[p1] == BPGNode.UNBOUND && bp[p2]==BPGNode.UNBOUND&&
            if (n1.get(p1).getLabel().equals(n2.get(p2).getLabel())
                    || n1.get(p2).getLabel().equals(n2.get(p1).getLabel())) {
                seq = jp.length - i;
                break;
            }
        }

        //number of shared variables (sv)
        int sv = edge.getVariables().size();
        double weight = (1 / seq * (sv == 0 ? 0 : 1 / sv));
        edge.setWeight(weight);

        //3. no pattern matched: means one( or both) of the patterns is (s p o) all bound
        edge.setWeight(MIN_SEL);
    }
}
