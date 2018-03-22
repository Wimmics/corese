package fr.inria.corese.core.approximate.algorithm;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.compiler.parser.Pragma;
import java.net.URL;
import java.util.List;

/**
 * Options
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 16 nov. 2015
 */
public class Parameters {

    private static final URL R_WN_PATH = Parameters.class.getClassLoader().getResource("wordnet");
    public static String WN_PATH = (R_WN_PATH == null) ? null : R_WN_PATH.getPath();

    private static final URL R_POS_TAGGER = Parameters.class.getClassLoader().getResource("tagger");
    public static String POS_TAGGER = (R_POS_TAGGER == null) ? null : R_POS_TAGGER.getPath() + "/english-left3words-distsim.tagger";

    public static String WN_VER = "3.0";
    public static String DEF_STRING_METRIC = "Lin";
    public static double THRESHOLD = 0.1;

    public static void init(ASTQuery ast) {
        //WordNet and POS tagger
        //WordNet dict path
        List<String> wnp = ast.getApproximateSearchOptions(Pragma.WN_PATH);
        if (check(wnp)) {
            WN_PATH = wnp.get(0);
        }

        //wordnet version
        List<String> wnv = ast.getApproximateSearchOptions(Pragma.WN_VERSION);
        if (check(wnv)) {
            WN_VER = wnv.get(0);
        }

        //pos tagger
        List<String> pos = ast.getApproximateSearchOptions(Pragma.POS_TAGGER);
        if (check(pos)) {
            POS_TAGGER = pos.get(0);
        }

        //String metric
        List<String> metric = ast.getApproximateSearchOptions(Pragma.STRING_METRIC);
        if (check(metric)) {
            DEF_STRING_METRIC = metric.get(0);
        }

        //threshold
        List<String> threshold = ast.getApproximateSearchOptions(Pragma.THRESHOLD);
        if (check(threshold)) {
            THRESHOLD = Double.valueOf(threshold.get(0));
        }
    }

    private static boolean check(List<String> list) {
        return list != null && !list.isEmpty();
    }
}
