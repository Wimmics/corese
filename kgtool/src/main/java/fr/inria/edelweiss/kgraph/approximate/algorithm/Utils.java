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
    private final static boolean SHOW_MSG = !true;

    /**
     * Format a real number using given format
     * @param d
     * @param format
     * @return 
     */
    public static String format(double d, String format) {
        return new DecimalFormat(format).format(d);
    }

    /**
     * Format a double using default format "##.####"
     * 
     * @param d
     * @return 
     */
    public static String format(double d) {
        return format(d, DEF_FORMAT);
    }

    /**
     * Print a specific msg
     * @param alg
     * @param s1
     * @param s2
     * @param parameter
     * @param sim 
     */
    public static void msg(String alg, String s1, String s2, String parameter, double sim) {
        if (SHOW_MSG) {
            System.out.println("\t [" + alg + ", "+parameter+"]: " + s1 + ", " + s2 + ", " + format(sim));
        }
    }

    /**
     * Print a message
     *
     * @param msg
     */
    public static void msg(String msg) {
        if (SHOW_MSG) {
            System.out.println(msg);
        }
    }

    /**
     * Print a message with an exceptional message
     * 
     * @param msg
     * @param exceptional 
     */
    public static void msg(String msg, boolean exceptional) {
        if (exceptional) {
            System.out.println(msg);
        } else {
            msg(msg);
        }
    }

    /**
     * Split a URL into prefix+suffix
     * (to be elaborated ...)
     * @param uri
     * @return 
     */
    public static String[] split(String uri) {
        int index = uri.lastIndexOf("#");
        if (index == -1) {
            index = uri.lastIndexOf("/");
        }

        String prefix = (index == -1) ? "" : uri.substring(0, index + 1);
        String suffix = (index == -1) ? uri : uri.substring(index + 1);
        return new String[]{prefix, suffix};
    }

    /**
     * Check if a string is null or empty
     * @param s
     * @return 
     */
    public static boolean empty(String s) {
        return s == null || s.length() == 0;
    }
}
