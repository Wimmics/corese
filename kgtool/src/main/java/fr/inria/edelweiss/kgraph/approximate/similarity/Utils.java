package fr.inria.edelweiss.kgraph.approximate.similarity;

import com.ibm.icu.text.DecimalFormat;

/**
 * Utils.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 15 oct. 2015
 */
public class Utils {

    private final static String DOUBLE_FORMAT = "##.####";
    private final static boolean SHOW_MSG = true;

    public static String format(double d) {
        return new DecimalFormat(DOUBLE_FORMAT).format(d);
    }

    public static void show(String alg, String s1, String s2, double sim) {
        if (SHOW_MSG) {
            System.out.println("\t [" + alg + "]: " + s1 + ", " + s2 + ", " + format(sim));
        }
    }
}
