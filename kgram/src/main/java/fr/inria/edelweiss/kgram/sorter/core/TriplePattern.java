package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import static fr.inria.edelweiss.kgram.sorter.core.IEstimate.MAX_SEL;
import static fr.inria.edelweiss.kgram.sorter.core.IEstimate.MIN_SEL_APP;
import fr.inria.edelweiss.kgram.sorter.impl.StatsBasedEstimation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class for constructing the pattern of a triple, including many parameters
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 25 juin 2014
 */
public class TriplePattern {

    public final static int BOUND = 1, LIST = 1, UNBOUND = Integer.MAX_VALUE;
    // S P O
    //FV: filter, number of variables(in the triple pattern) appeared in all the filters
    //    the more the better (less selectivity)
    //FN: filter, variables appeared in how many filters 
    //PV: number of variables appeared in prejection variables(select nodes)
    //OT: type of bound object, URI|literal|NA
    public final static int S = 0, P = 1, O = 2, G = 3, FV = 4, FN = 5;
    private final static int PARAMETER_LEN = 6;
    List<String> variables = new ArrayList<String>();
    private final int[] pattern = new int[PARAMETER_LEN];

    private final BPGNode bpn;

    public TriplePattern(BPGNode n, Edge e, List<Exp> bindings) {
        //the value of S P O can be: 0[bound], ?[length of constant list], Integer.MAX_VALUE[unbound]
        this.bpn = n;
        this.pattern[S] = getNodeType(e.getNode(0), bindings);
        this.pattern[P] = getNodeType(e.getEdgeNode(), bindings);
        this.pattern[O] = getNodeType(e.getNode(1), bindings);

        // get the variables in the triple pattern
        n.getExp().getVariables(variables);
    }

    //Get the type of a node (s p o) in a triple pattern
    //Bound (constant) or unbound(varaible) or detected constant(s) (list of constant(s))
    private int getNodeType(Node n, List<Exp> bindings) {
        if (n == null) {
            return -1;
        }
        return n.isVariable() ? (Utility.isBound(bindings, n) ? LIST : UNBOUND) : BOUND;
    }

    /**
     * Set the paramters other then basic pattern
     *
     * @param graph
     */
    public void setParameters(BPGraph graph) {
        this.setFilterNumber(graph);
        //!!TODO set graph
        // this.pattern[G] = getNodeType(null, bindings);

    }

    // FV: filter variables
    // FN: filter number
    private void setFilterNumber(BPGraph graph) {
        List<BPGNode> nodes = graph.getNodeList(6);

        int noOfFilter = 0, noOfVariable = 0;

        //1 get all variables in all filters
        List<String> variablesInFilter = new ArrayList<String>();
        //**2 check the variables appeared in how many filters
        for (BPGNode n : nodes) {
            List<String> l = n.getExp().getFilter().getVariables();
            variablesInFilter.addAll(l);

            for (String var1 : l) {
                for (String var2 : variables) {
                    if (var1.equalsIgnoreCase(var2)) {
                        noOfFilter++;
                    }
                }
            }
        }

        //**3 calculate no. of variables appeared in all filters
        for (String var1 : variablesInFilter) {
            for (String var2 : variables) {
                if (var1.equalsIgnoreCase(var2)) {
                    noOfVariable++;
                }
            }
        }

        this.pattern[FV] = noOfVariable;
        this.pattern[FN] = noOfFilter;
    }

    /**
     * Set selectivity
     *
     * @param size size of pattern list
     * @param index
     */
    public void setSelectivity(int size, int index) {
        double itl = (MAX_SEL - MIN_SEL_APP) / (size - 1);
        this.bpn.setSelectivity(itl * index + MIN_SEL_APP);
    }

    /**
     * Find match by using defalut patterns list
     *
     * @param basicPatterns
     * @return
     */
    public final int match(int[][] basicPatterns) {
        for (int i = 0; i < basicPatterns.length; i++) {
            boolean match = true;
            for (int j = 0; j < basicPatterns[i].length; j++) {
                match = match && (basicPatterns[i][j] == this.pattern[j]);
            }

            if (match) {
                return i;
            }
        }

        return -1;
    }

    public final int match() {
        return this.match(DEFAULT_BASIC_PATTERN);
    }

    /**
     * Match two given patterns
     *
     * @param p1
     * @param p2
     * @param meta
     * @return true if matched; otherwise false
     */
    public boolean match(TriplePattern p1, TriplePattern p2, IProducer meta) {
        boolean match = true;
        for (int i = 0; i < p1.pattern.length; i++) {
            match = match && (p1.pattern[i] == p2.pattern[i]);
        }
        return match && (compareWithStats(p1.bpn, p2.bpn, meta)==0);
    }

    public boolean match(TriplePattern p1, IProducer meta) {
        return match(p1, this, meta);
    }

    /**
     * Sort a list of triple patterns according to predefined rules
     *
     * @param patterns
     * @param bp basic patterns
     * @param onlyBasic if only compare basic patterns
     * @param meta Producer for get stats data
     */
    public static void sort(List<TriplePattern> patterns, final int[][] bp, final boolean onlyBasic, final IProducer meta) {
        Collections.sort(patterns, new Comparator<TriplePattern>() {

            @Override
            public int compare(TriplePattern o1, TriplePattern o2) {
                 return compareTriple(o1, o2, bp, onlyBasic, meta);
            }
        });
    }

    public static int compareTriple(TriplePattern p1, TriplePattern p2, final int[][] bp, final boolean onlyBasic, final IProducer meta) {
        //get the sequence in the simple pattern list
        int i1 = p1.match(bp), i2 = p2.match(bp);
        //the first 3 pattern are not the same, return directly
        if (i1 != i2) {
            return i1 > i2 ? 1 : -1;
            //compare the others
            //for FV and FN, the bigger, the selectivity is higher
            //todo add graph
        } else if (!onlyBasic) {
            if (p1.pattern[G] != p2.pattern[G]) {
                return p1.pattern[G] < p2.pattern[G] ? 1 : -1;
            } else if (p1.pattern[FV] != p2.pattern[FV]) {
                return p1.pattern[FV] < p2.pattern[FV] ? 1 : -1;
            } else if (p1.pattern[FN] != p2.pattern[FN]) {
                return p1.pattern[FN] < p2.pattern[FN] ? 1 : -1;
            } else {
                return compareWithStats(p1.bpn, p2.bpn, meta);
            }
        } else {
            return compareWithStats(p1.bpn, p2.bpn, meta);
        }
    }

    //compare the two triple pattern by using stats if available
    private static int compareWithStats(BPGNode n1, BPGNode n2, IProducer meta) {
        if (meta == null || !meta.statsEnabled()) {
            return 0;
        }

        StatsBasedEstimation sbe = new StatsBasedEstimation(meta);
        double s1 = sbe.selTriplePattern(n1), s2 = sbe.selTriplePattern(n2);
        return (s1 == s2) ? 0 : ((s1 > s2) ? 1 : -1);
    }

    @Override
    public String toString() {
        String s = "[";
        for (int i = 0; i < this.pattern.length; i++) {
            s += this.pattern[i] + ", ";
        }

        s += bpn.getSelectivity() + "] ";
        return s;
    }

    //TODO: extend to more complex patterns
    private final static int[][] DEFAULT_BASIC_PATTERN = new int[][]{
        {0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {1, 1, 0}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}
    };

    //weight 6, 5, 4, 3, 2, 1
    public static final int[][] JOINT_PATTERN = new int[][]{
        {P, O}, {S, P}, {S, O}, {O, O}, {S, S}, {P, P}
    };
}
