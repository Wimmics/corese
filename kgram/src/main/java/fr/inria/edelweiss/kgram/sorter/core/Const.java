package fr.inria.corese.kgram.sorter.core;

import static fr.inria.corese.kgram.api.core.ExpType.BIND;
import static fr.inria.corese.kgram.api.core.ExpType.EDGE;
import static fr.inria.corese.kgram.api.core.ExpType.FILTER;
import static fr.inria.corese.kgram.api.core.ExpType.GRAPH;
import static fr.inria.corese.kgram.api.core.ExpType.OPTIONAL;
import static fr.inria.corese.kgram.api.core.ExpType.VALUES;

/**
 * Constants
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 27 oct. 2014
 */
public final class Const {

    public static final int ALL = 0;
    public static final int SUBJECT = 1;
    public static final int PREDICATE = 2;
    public static final int OBJECT = 3;
    public static final int TRIPLE = 4;
    public static final int NA = -1;

    public final static int BOUND = 0, LIST = 0, UNBOUND = Integer.MAX_VALUE;

    public static final int[] EVALUABLE_TYPES = {EDGE, GRAPH};
    public static final int[] NOT_EVALUABLE_TYPES = {FILTER, VALUES, BIND, OPTIONAL};

    public static boolean plannable(int type) {
        for (int e : NOT_EVALUABLE_TYPES) {
            if (type == e) {
                return true;
            }
        }

        return evaluable(type);
    }

    public static boolean evaluable(int type) {
        for (int e : EVALUABLE_TYPES) {
            if (type == e) {
                return true;
            }
        }

        return false;
    }
}
