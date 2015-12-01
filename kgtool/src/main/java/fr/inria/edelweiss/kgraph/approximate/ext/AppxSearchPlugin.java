package fr.inria.edelweiss.kgraph.approximate.ext;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.CoreseNumber;
import fr.inria.acacia.corese.cg.datatype.CoreseStringLiteral;
import fr.inria.acacia.corese.cg.datatype.CoreseURI;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.tool.ApproximateSearchEnv;
import fr.inria.edelweiss.kgraph.approximate.algorithm.ISimAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.algorithm.SimAlgorithmFactory;
import fr.inria.edelweiss.kgraph.approximate.algorithm.Utils;
import static fr.inria.edelweiss.kgraph.approximate.algorithm.Utils.format;
import static fr.inria.edelweiss.kgraph.approximate.algorithm.Utils.msg;
import fr.inria.edelweiss.kgraph.query.PluginImpl;

/**
 * Plugin implementation for approximate search
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 1 oct. 2015
 */
public class AppxSearchPlugin implements ExprType {

    static final IDatatype TRUE = PluginImpl.TRUE;
    static final IDatatype FALSE = PluginImpl.FALSE;
    private final PluginImpl plugin;

    private IDatatype dt1, dt2;
    private String s1, s2, algs;
    private double threshold = 0;
    private boolean isMore = true;

    //private boolean isCalculateSimilarity = true;
    public AppxSearchPlugin(PluginImpl p) {
        this.plugin = p;
    }

    public Object eval(Expr exp, Environment env, Producer p) {
        switch (exp.oper()) {
            case APP_SIM:
                msg("[Eval sim ... ]: " + exp);

                ApproximateSearchEnv appxEnv = env.getAppxSearchEnv();
                double d = appxEnv.aggregate(env);
                IDatatype sim = plugin.getValue(d);
                return sim;
            default:
                return null;
        }
    }
    
    //filter approximate(var1, var2, 'alg1-alg2-alg3', 0.2, true)
    //args[0] = var 1
    //args[1] = uri
    //args[2] = alg list
    //args[3] = threshold
    //args[4] = false|true

    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {
        switch (exp.oper()) {
            case APPROXIMATE:
                isMore = ((ASTQuery)env.getQuery().getAST()).isMore();
                msg("[Eval appx ... ]: " + exp);

                //0. check parameters
                boolean ok = init(args);
                if (!ok) {
                    return FALSE;
                }

                //is pure function
                //TRUE: only calculate once, does not compute the value of similarity
                //FALSE: need to compute the value of similarity
                if (isMore) {
                    return this.approximate(env, exp);
                } else {
                    return this.match();
                }
            default:
                return null;
        }
    }

    private IDatatype match() {
        if (s1.equalsIgnoreCase(s2)) {
            return TRUE;
        }
        ISimAlgorithm alg = SimAlgorithmFactory.createCombined(algs, true);
        double sim = alg.calculate(s1, s2);

        return (sim > threshold) ? TRUE : FALSE;
    }

    private IDatatype approximate(Environment env, Expr exp) {
        //Key k = new Key(exp.getExp(0), dt2);
        Expr var = exp.getExp(0);
        //Value v = new Value(dt1, algs);
        ApproximateSearchEnv appxEnv = env.getAppxSearchEnv();
        //2.1 get the similarity of this pair (?_var_x, <uri_x>) -> (args[0], args[1]) -> (s1, s2)
        //2.1.1 check if already calculated, if yes, just retrieve the value
        Double combinedSim,
                singleSim = appxEnv.getSimilarity(var, dt1, algs);
        // Double combinedSim;

        boolean notExisted = (singleSim == null);

        if (s1.equalsIgnoreCase(s2)) {
            singleSim = ISimAlgorithm.MAX;
            //v.setSimilarity(singleSim);
            combinedSim = ISimAlgorithm.MAX;
        } else {
            if (notExisted) { //2.1.2 otherwise, re-calculate
                ISimAlgorithm alg = SimAlgorithmFactory.createCombined(algs, false);
                singleSim = alg.calculate(s1, s2);
            }
            //v.setSimilarity(singleSim);
            combinedSim = appxEnv.aggregate(env, var, singleSim);
        }

        //2.2 get the similarity of all
        //combinedSim = sr.aggregate(env, k, r);
        //3 finalize
        boolean filtered = combinedSim > threshold;
        msg("\t [Similarity,\t "+dt1+",\t"+dt2+"]: c:" + format(singleSim) + ", all:" + format(combinedSim) + ", " + filtered);

        //if:   filter (approximate) returns true & the value is re-calculated, 
        //then: set the value of similarity & add the result to results set
        if (notExisted) {
            appxEnv.add(var, dt2, dt1, algs, singleSim);
        }
        //msg("[Eval appx ... ]: " + env.getAppxSearchEnv());
        return filtered ? TRUE : FALSE;
    }

    //get corresponding values
    private boolean init(Object[] args) {
        if (args == null || args.length != 4) {
            return false;
        }

        dt1 = (IDatatype) (args[0]);
        dt2 = (IDatatype) (args[1]);

        //1 get strings
        //IF the types are different, then return FALSE directly
        if (dt1.getCode() != dt2.getCode()) {
            return false;
        }

        if (!(args[2] instanceof CoreseStringLiteral)) {
            return false;
        }

        algs = ((CoreseStringLiteral) args[2]).getStringValue();

        if (!(args[3] instanceof CoreseNumber)) {
            return false;
        }

        threshold = ((CoreseNumber) args[3]).doubleValue();

        if (dt1 instanceof CoreseURI) {
            String[] uri1 = Utils.split(dt1.getLabel());
            String[] uri2 = Utils.split(dt2.getLabel());

            //if (!uri1[0].equalsIgnoreCase(uri2[0])) {
            //return FALSE;
            //} else 
            {
                s1 = uri1[1];
                s2 = uri2[1];
            }
        } else if (dt1 instanceof CoreseStringLiteral) {
            s1 = ((CoreseStringLiteral) dt1).getStringValue();
            s2 = ((CoreseStringLiteral) dt2).getStringValue();
        } else {
            s1 = dt1.getLabel();
            s2 = dt2.getLabel();
        }

        if (s1 == null || s2 == null) {
            return false;
        }

        return true;
    }
}
