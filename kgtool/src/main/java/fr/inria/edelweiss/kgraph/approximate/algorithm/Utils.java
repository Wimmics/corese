package fr.inria.edelweiss.kgraph.approximate.algorithm;

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

    public static void msg(String alg, String s1, String s2, double sim) {
        if (SHOW_MSG) {
            System.out.println("\t [" + alg + "]: " + s1 + ", " + s2 + ", " + format(sim));
        }
    }

    /**
     * Print a message
     * @param msg 
     */
    public static void msg(String msg) {
        if (SHOW_MSG) {
            System.out.println(msg);
        }
    }

    public static void msg(String msg, boolean exceptional) {
        if (exceptional) {
            System.out.println(msg);
        } else {
            msg(msg);
        }
    }

    //to be refined
    public static String[] split(String uri) {
        int index = uri.lastIndexOf("#");
        if (index == -1) {
            index = uri.lastIndexOf("/");
        }

        String prefix = (index == -1) ? "" : uri.substring(0, index + 1);
        String suffix = (index == -1) ? uri : uri.substring(index + 1);
        //msg("\t"+prefix + ":" + suffix);
        return new String[]{prefix, suffix};
    }
}
