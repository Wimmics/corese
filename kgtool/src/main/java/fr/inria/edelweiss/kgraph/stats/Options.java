package fr.inria.edelweiss.kgraph.stats;

import fr.inria.edelweiss.kgram.sorter.core.IStatistics;

/**
 * Options and parameters for setting meta data statistics
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 17 juin 2014
 */
public class Options {

    //*** hash bucket *** 
    private static final int BUCKET_NUMBER_MAX = 10000;//maximum size of bucket
    private static final int BUCKET_SIZE_MIN = 10;//minimum number of objects in one buckets

    //*** reduced map ***
    //default limit of resources percentage of all resources
    private static final double RES_LIMIT_TOP = 0.01;
    private static final double RES_LIMIT_BOTTOM = 0.01;

    //default limit of triples percentage of all triples
    private static final double TRI_LIMIT_TOP = 0.50;
    private static final double TRI_LIMIT_BOTTOM = 0.10;

    //*** hash triple table ***
    private static final double HTT_MAX_SIZE = 100;
    //private static final double HTT_PRE = 10;
    //private static final double HTT_OBJ = 100;

    //*** default parameters ***
    public final static double[] DEF_PARA_CUTOFF = new double[]{RES_LIMIT_TOP, TRI_LIMIT_TOP, RES_LIMIT_BOTTOM, TRI_LIMIT_BOTTOM};
    public final static double[] DEF_PARA_HASH = new double[]{BUCKET_SIZE_MIN, BUCKET_NUMBER_MAX};
    public final static double[] DEF_PARA_HTT = new double[]{HTT_MAX_SIZE, HTT_MAX_SIZE};

    //For class Options
    public final static int HT_HASH = 10;
    public final static int HT_FULL = 20;
    public final static int HT_CUTOFF = 30;
    public final static int HT_AVERAGE = 40;
    public final static int HT_TRIPLE_HASH = 50;

    //default options
    //subject
    public final static Options DEF_OPT_SUB = new Options(HT_CUTOFF, IStatistics.SUBJECT, DEF_PARA_CUTOFF);
    //predicate
    public final static Options DEF_OPT_PRE = new Options(HT_FULL, IStatistics.PREDICATE, null);
    //object
    public final static Options DEF_OPT_OBJ = new Options(HT_HASH, IStatistics.OBJECT, DEF_PARA_HASH);
    //other
    public final static Options DEF_OPT_PAIR = new Options(HT_TRIPLE_HASH, IStatistics.NA, DEF_PARA_HTT);

    public final static Options[] DEFAULT_OPTIONS = new Options[]{DEF_OPT_SUB, DEF_OPT_PRE, DEF_OPT_OBJ, DEF_OPT_PAIR};

    private final int heuristic;
    private final int nodeType;
    private final double[] parameters;

    public Options(int heuristic, int nodeType, double[] parameters) {
        this.heuristic = heuristic;
        this.nodeType = nodeType;
        this.parameters = parameters;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public int getNodeType() {
        return nodeType;
    }

    public double[] getParameters() {
        return parameters;
    }
}
