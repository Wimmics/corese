package fr.inria.edelweiss.kgram.sorter.core;

import static fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity.MAX_SEL;
import static fr.inria.edelweiss.kgram.sorter.core.IEstimateSelectivity.MIN_SEL_APP;
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

    // S P O
    //FV: filter, number of variables(in the triple pattern) appeared in all the filters
    //    the more the better (less selectivity)
    //FN: filter, variables appeared in how many filters 
    //PV: number of variables appeared in prejection variables(select nodes)
    //OT: type of bound object, URI|literal|NA
    public final static int S = 0, P = 1, O = 2, FV = 3, FN = 4;
    private final static int PARAMETER_LEN = 5;
    List<String> variables = new ArrayList<String>();
    private final int[] pattern = new int[PARAMETER_LEN];

    private final BPGNode bpn;

    public TriplePattern(BPGNode n, int s, int p, int o) {
        this.bpn = n;
        this.pattern[S] = s;
        this.pattern[P] = p;
        this.pattern[O] = o;

        // get the variables in the triple pattern
        n.getExp().getVariables(variables);
    }

    /**
     * Get number of unbound values in a triple pattern 
     * 
     * @return 
     */
    public int getUnboundNumber() {
        return this.pattern[S] + this.pattern[P] + this.pattern[O];
    }

    /**
     * Set the paramters other then basic pattern
     * 
     * @param graph 
     */
    public void setParameters(BPGraph graph) {
        this.setFilterNumber(graph);
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
     * @return
     */
    public int match() {
        for (int i = 0; i < BASIC_PATTERN.length; i++) {
            boolean match = true;
            for (int j = 0; j < BASIC_PATTERN[i].length; j++) {
                match = match && (BASIC_PATTERN[i][j] == this.pattern[j]);
            }

            if (match) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Match two given patterns
     *
     * @param p1
     * @param p2
     * @return true if matched; otherwise false
     */
    public boolean match(TriplePattern p1, TriplePattern p2) {
        boolean match = true;
        for (int i = 0; i < p1.pattern.length; i++) {
            match = match && (p1.pattern[i] == p2.pattern[i]);
        }
        return match;
    }

    public boolean match(TriplePattern p1) {
        return match(p1, this);
    }

    /**
     * Sort a list of triple patterns according to predefined rules
     * 
     * @param patterns 
     */
    public static void sort(List<TriplePattern> patterns) {
        Collections.sort(patterns, new Comparator<TriplePattern>() {

            @Override
            public int compare(TriplePattern p1, TriplePattern p2) {
                //get the sequence in the simple pattern list
                int i1 = p1.match(), i2 = p2.match();
                //the first 3 pattern are not the same, return directly
                if (i1 != i2) {
                    return i1 > i2 ? 1 : -1;
                    //compare the others
                    //for FV and FN, the bigger, the selectivity is higher
                } else if (p1.pattern[FV] != p2.pattern[FV]) {
                    return p1.pattern[FV] < p2.pattern[FV] ? 1 : -1;
                } else if (p1.pattern[FN] != p2.pattern[FN]) {
                    return p1.pattern[FN] < p2.pattern[FN] ? 1 : -1;
                } else {
                    return 0;
                }
            }
        });
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
    private final static int[][] BASIC_PATTERN = new int[][]{
        {0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {1, 1, 0}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}
    };

    public static final int[][] JOINT_PATTERN = new int[][]{
        {P, O}, {S, P}, {S, O}, {O, O}, {S, S}, {P, P}
    };
}
