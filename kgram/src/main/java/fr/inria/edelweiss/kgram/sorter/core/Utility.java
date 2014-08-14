package fr.inria.edelweiss.kgram.sorter.core;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.BOUND;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.O;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.P;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.S;
import static fr.inria.edelweiss.kgram.sorter.core.TriplePattern.UNBOUND;
import java.util.List;

/**
 * Utility.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 8 août 2014
 */
public class Utility {

    public static void main(String[] args) {
        System.out.println(Utility.generateBasicPattern());
        Utility.generateBasicPattern(new int[]{30, 20, 10}, true);
        System.out.println(Utility.BASIC_PATTERN);
    }

    public static int[][] BASIC_PATTERN = null;
    private final static int[] default_order = {P, S, O};//p<s<o
    private final static int TRIPLE_LEN = 3, PATTERN_LEN = 8;

    public static int[][] generateBasicPattern() {
        return generateBasicPattern(null, true);
    }

    /**
     * Generate the basic patterns using the numbers of distince subject,
     * predicate and objects
     *
     * @param numbers array of distinct number s p o
     * @param regen if re-generate the patterns
     * @return
     */
    public static int[][] generateBasicPattern(int[] numbers, boolean regen) {
        if (BASIC_PATTERN != null && !regen) {
            return BASIC_PATTERN;
        }

        //1 step: get the order of s, p, o according to the number of distinct s, p, o
        // using default settings if the numbers of s, p, o are not available
        int[] order = default_order;
        if (numbers != null && numbers.length == TRIPLE_LEN) {
            order = order3Numbers(numbers);
        }

        //2 step: generate the patterns
        //!! TODO LIST needs to be considered in future, at the moment
        //LIST is considered as BOUND
        BASIC_PATTERN = new int[PATTERN_LEN][TRIPLE_LEN];
        BASIC_PATTERN[0] = new int[]{BOUND, BOUND, BOUND};
        for (int i = 0; i < order.length; i++) {
            for (int j = 0; j < TRIPLE_LEN; j++) {
                //1 first three patterns with two constants
                //2 second three patterns with ONE constants
                if (j == order[i]) {
                    BASIC_PATTERN[i + 1][j] = UNBOUND;
                    BASIC_PATTERN[PATTERN_LEN - 2 - i][j] = BOUND;
                } else {
                    BASIC_PATTERN[i + 1][j] = BOUND;
                    BASIC_PATTERN[PATTERN_LEN - 2 - i][j] = UNBOUND;
                }
            }
        }
        BASIC_PATTERN[PATTERN_LEN - 1] = new int[]{UNBOUND, UNBOUND, UNBOUND};

        return BASIC_PATTERN;
    }

    private static int[] order3Numbers(int[] numbers) {
        int[] order = new int[]{0, 1, 2};

        order[0] = 0;
        order[1] = 1;
        order[2] = 2;

        if (numbers[0] > numbers[1]) {
            swap(numbers, 0, 1);
            swap(order, 0, 1);
        }
        if (numbers[1] > numbers[2]) {
            swap(numbers, 1, 2);
            swap(order, 1, 2);
        }

        if (numbers[0] > numbers[1]) {
            swap(numbers, 0, 1);
            swap(order, 0, 1);
        }

        return order;
    }

    private static void swap(int[] arr, int a, int b) {
        int x = arr[a];
        arr[a] = arr[b];
        arr[b] = x;
    }

    public static boolean isBound(List<Exp> bindings, Node var) {
        for (Exp exp : bindings) {
            if (var.getLabel().equalsIgnoreCase(exp.get(0).getNode().getLabel())) {
                //todo in future (or not)??
                //calculate the number of constants bound to this variable
                return true;
            }
        }
        return false;
    }
}
