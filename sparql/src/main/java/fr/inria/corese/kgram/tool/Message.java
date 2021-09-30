package fr.inria.corese.kgram.tool;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {

    static Logger logger = LoggerFactory.getLogger(Message.class);

    public static final int UNDEF_VAR = 0;
    public static final int FAIL = 1;
    public static final int FAIL_AT = 2;
    public static final int EVAL = 3;
    public static final int FREE = 4;
    public static final int CHECK = 5;
    public static final int REWRITE = 6;
    public static final int PRAGMA = 7;
    public static final int LOOP = 8;
    public static final int UNDEF_FUN = 9;
    public static final int AGG = 10;

    public static final String NL = System.getProperty("line.separator");

    static Hashtable<Integer, String> table;

    public static String get(int code) {
        if (table == null) {
            init();
        }
        return table.get(code);
    }

    static void init() {
        table = new Hashtable<>();

        def(UNDEF_VAR, "Undefined variable: ");
        def(UNDEF_FUN, "Undefined function: ");
        def(FAIL, "KGRAM fail at compile time");
        def(FAIL_AT, "KGRAM fail at: ");
        def(EVAL, "Eval: ");
        def(FREE, "Pattern is Free: ");
        def(CHECK, "Check: ");
        def(REWRITE, "Compiler rewrite error: ");
        def(PRAGMA, "Pragma: ");
        def(LOOP, "Loop: ");
        def(AGG, "Aggregate limited to (defined) variable: ");
    }

    static void def(int code, String mes) {
        table.put(code, mes);
    }

    public static void log(int code) {
        logger.warn(get(code));
    }

    public static void log() {
        //System.out.println();
    }

    public static void log(int code, Object mes) {
        logger.warn(get(code) + mes);
    }

    public static void log(Object mes) {
        logger.warn(mes.toString());
    }

}
