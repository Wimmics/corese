package fr.inria.edelweiss.kgram.sorter.impl;

import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity;
import fr.inria.edelweiss.kgram.sorter.core.IHeuristics;
import fr.inria.edelweiss.kgram.sorter.core.IStatistics;
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

    @Override
    public void estimate(BPGraph plein, Producer producer, Object listSelectNodes) {
        if (listSelectNodes instanceof List && producer instanceof IStatistics) {
            //** 1 Get list of select nodes
            List<Exp> list = (List<Exp>) listSelectNodes;
            for (Exp exp : list) {
                selectNodes.add(exp.getNode().getLabel());
            }

            //**2 estimate
            for (BPGNode n : plein.getNodeList()) {
                assignSel(n, (IHeuristics) producer);
            }
        } else {
            System.err.println("!! The object type is not compitable, should be list<Exp> refering the select nodes !!");
        }
    }

    public void assignSel(BPGNode n, IHeuristics heuri) {
        // rule 1: (s p o) < (s ? o) < (? p o)< (s p ?)< (? ? o)< (s ? ?)< (? p ?)< (? ? ?) =8
        // rule 2: o(literal) > o(uri) 
        // rule 3: the less projection variables, the higher priority

        double itl = (MAX_SEL - MIN_SEL_APP) / (PATTERNS_SIMPLE.length - 1);
        int[] pat = n.getPattern();
        if (pat == null) {
            n.setSelectivity(MAX_SEL);
            return;
        }

        // if (!modeSimple) {
        //process the pattern of triple
        //1 check object type
        if (pat[2] == BPGNode.BOUND) {
            pat[3] = heuri.isURI(n.getObject()) ? URI : LIT;//LIT
            System.out.println(n.getObject() + " is URI:" + pat[3]);
        } else {
            pat[3] = NA;
        }
        //2 check projection variable
        pat[4] = getVariablesNo(n);
        //}

        //3 find pattern matchings
        int[][] pattern = modeSimple ? PATTERNS_SIMPLE : PATTERNS_COMPLEX;

        for (int j = 0; j < pattern.length; j++) {
            boolean match = true;
            for (int i = 0; i < pattern[j].length; i++) {
                match = match & pat[i] == pattern[j][i];
            }

            //matched
            if (match) {
                n.setSelectivity(itl * j + MIN_SEL_APP);
                break;
            }
        }
    }

    private int getVariablesNo(BPGNode n) {
        List<String> projection = new ArrayList<String>();
        n.getExp().getVariables(projection);
        if (projection.isEmpty() || selectNodes.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String proj : projection) {
            for (String var : selectNodes) {
                if (proj.equalsIgnoreCase(var)) {
                    count++;
                }
            }
        }

        return count;
    }

    private final static int[][] PATTERNS_SIMPLE = new int[][]{
        {0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {1, 1, 0}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}
    };
    private final static int URI = 1, LIT = 0, NA = 2;
    //first three bits: {s p o}, 0:bound, 1:unbound
    //4th bit: type of bound object, 0:literal, 1:uri
    //5th bit: number of prejection variables appeared in the triple pattern
    private final static int[][] PATTERNS_COMPLEX = new int[][]{
        //(s p o)
        {0, 0, 0, LIT, 0},
        {0, 0, 0, URI, 0},
        //(s ? o)
        {0, 1, 0, LIT, 0}, //(s ? o, literal, 0 proj. var.)
        {0, 1, 0, URI, 0},
        {0, 1, 0, LIT, 1},
        {0, 1, 0, URI, 1},
        //(? p o)
        {1, 0, 0, LIT, 0},
        {1, 0, 0, URI, 0},
        {1, 0, 0, LIT, 1},
        {1, 0, 0, URI, 1},
        //(s p ?)
        {0, 0, 1, NA, 0},
        {0, 0, 1, NA, 1},
        //(? ? o)
        {1, 1, 0, LIT, 0},
        {1, 1, 0, URI, 0},
        {1, 1, 0, LIT, 1},
        {1, 1, 0, URI, 1},
        {1, 1, 0, LIT, 2},
        {1, 1, 0, URI, 2},
        //(s ? ?)
        {0, 1, 1, NA, 0},
        {0, 1, 1, NA, 1},
        {0, 1, 1, NA, 2},
        //(? p ?)
        {1, 0, 1, NA, 0},
        {1, 0, 1, NA, 1},
        {1, 0, 1, NA, 2},
        //(? ? ?)
        {1, 1, 1, NA, 0},
        {1, 1, 1, NA, 1},
        {1, 1, 1, NA, 2},
        {1, 1, 1, NA, 3}
    };
}
