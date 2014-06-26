package fr.inria.edelweiss.kgram.sorter.core;

import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
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
    private final static int S = 0, P = 1, O = 2, FV = 3, FN = 4, PV = 5, OT = 6;
    private final static int PARAMETER_LEN = 7;
    private final static int NA = 2, URI = 1, LIT = 0;
    List<String> variables = new ArrayList<String>();
    private final int[] pattern = new int[PARAMETER_LEN];

    private BPGNode bpn;

    public TriplePattern(BPGNode n) {
        if (n.getType() != EDGE) {
            return;
        }
        this.bpn = n;
        // get the variables in the triple pattern
        n.getExp().getVariables(variables);

        this.pattern[S] = n.getPattern()[S];
        this.pattern[P] = n.getPattern()[P];
        this.pattern[O] = n.getPattern()[O];
        //todo
    }

    public void setParameters(List<String> selectNodes, IProducer heuri, BPGraph graph) {
        this.setFilterNumber(graph);
        this.setProjectionVariables(selectNodes);
        this.setObjectType(heuri);
    }

    //
    public void setProjectionVariables(List<String> selectNodes) {

        if (variables.isEmpty() || selectNodes.isEmpty()) {
            return;
        }

        //check the number of variables in triple pattern appeared in select nodes
        int count = 0;
        for (String proj : variables) {
            for (String var : selectNodes) {
                if (proj.equalsIgnoreCase(var)) {
                    count++;
                }
            }
        }

        this.pattern[PV] = count;
    }

    public void setObjectType(IProducer heuri) {
        if (this.pattern[O] == BPGNode.BOUND) {
            this.pattern[OT] = heuri.isURI(this.bpn.getObject()) ? URI : LIT;
        } else {
            this.pattern[OT] = NA;
        }
    }

    public void setFilterNumber(BPGraph graph) {
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
        for (int i = 0; i < PATTERNS_BASIC.length; i++) {
            boolean match = true;
            for (int j = 0; j < PATTERNS_BASIC[i].length; j++) {
                match = match && (PATTERNS_BASIC[i][j] == this.pattern[j]);
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
                    //for PV and OT, the smaller, the selectivity is higher
                } else if (p1.pattern[PV] != p2.pattern[PV]) {
                    return p1.pattern[PV] > p2.pattern[PV] ? 1 : -1;
                } else if (p1.pattern[OT] != p2.pattern[OT]) {
                    return p1.pattern[OT] > p2.pattern[OT] ? 1 : -1;
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

    private final static int[][] PATTERNS_BASIC = new int[][]{
        {0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {1, 1, 0}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}
    };
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
