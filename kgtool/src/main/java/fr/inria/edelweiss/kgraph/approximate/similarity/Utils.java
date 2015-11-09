package fr.inria.edelweiss.kgraph.approximate.similarity;

import com.ibm.icu.text.DecimalFormat;

/**
 * Utils class
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 15 oct. 2015
 */
public class Utils {

    private final static String DEF_FORMAT = "##.####";
    private final static boolean SHOW_MSG = true;

    public static String format(double d, String format) {
        return new DecimalFormat(format).format(d);
    }

    public static String format(double d) {
        return format(d, DEF_FORMAT);
    }

    public static void show(String alg, String s1, String s2, double sim) {
        if (SHOW_MSG) {
            System.out.println("\t [" + alg + "]: " + s1 + ", " + s2 + ", " + format(sim));
        }
    }
}
