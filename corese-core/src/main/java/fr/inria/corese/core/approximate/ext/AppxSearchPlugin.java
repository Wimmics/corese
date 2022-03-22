package fr.inria.corese.core.approximate.ext;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseStringLiteral;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.tool.ApproximateSearchEnv;
import fr.inria.corese.core.approximate.algorithm.ISimAlgorithm;
import fr.inria.corese.core.approximate.algorithm.SimAlgorithmFactory;
import static fr.inria.corese.core.approximate.algorithm.Utils.format;
import static fr.inria.corese.core.approximate.algorithm.Utils.msg;
import fr.inria.corese.core.approximate.algorithm.impl.BaseAlgorithm;
import fr.inria.corese.core.query.PluginImpl;

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

    public AppxSearchPlugin(PluginImpl p) {
        this.plugin = p;
    }

    public IDatatype eval(Expr exp, Environment env, Producer p) {
        switch (exp.oper()) {
            case APP_SIM:
                ApproximateSearchEnv appxEnv = env.getAppxSearchEnv();
                double d = appxEnv.aggregate(env);
                IDatatype sim = plugin.getValue(d);
                msg("[Eval sim() ]: " + sim);
                return sim;
            default:
                return null;
        }
    }

    public IDatatype eval(Expr exp, Environment env, Producer p, Object[] args) {
        IDatatype[] param = (IDatatype[]) args;
        switch (exp.oper()) {
            
            case APPROXIMATE:
                msg("[Eval appx ... ]: " + exp);
                //0. check parameters
                return eval(exp, env, param);
                
            default:
                return null;
        }
    }

    // Use approximate as a filter function
    private IDatatype match(IDatatype dt1, IDatatype dt2, String parameter, String algs, double threshold) {
        String s1 = stringValue(dt1);
        String s2 = stringValue(dt2);    
        if (s1.equalsIgnoreCase(s2)) {
            return TRUE;
        }
        ISimAlgorithm alg = SimAlgorithmFactory.createCombined(algs, true);
        double sim = alg.calculate(s1, s2, parameter);

        return (sim > threshold) ? TRUE : FALSE;
    }
    
    String stringValue(IDatatype dt){
        if (dt.hasLang()){
            return dt.stringValue().concat("@").concat(dt.getLang());
        }
        return dt.stringValue();
    }

    // Use 'approximate' as appx search and calculate similarity
    private IDatatype approximate(IDatatype dt1, IDatatype dt2, String parameter, String algs, double threshold, Environment env, Expr exp) {
        //0. initialize
        String s1 = stringValue(dt1);
        String s2 = stringValue(dt2);   
        Expr var = exp.getExp(0);
        ApproximateSearchEnv appxEnv = env.getAppxSearchEnv();

        Double combinedSim;
        Double singleSim = appxEnv.getSimilarity(var, dt1, algs);//check appx env to see if already computed

        boolean notExisted = (singleSim == null);

        //1 calculation to get current similarity and overall similarity
        if (s1.equalsIgnoreCase(s2)) {
            singleSim   = ISimAlgorithm.MAX;
            combinedSim = ISimAlgorithm.MAX;
        } else {
            if (notExisted) { //2.1.2 otherwise, re-calculate
                ISimAlgorithm alg = SimAlgorithmFactory.createCombined(algs, false);
                singleSim = alg.calculate(s1, s2, parameter);
            }
            combinedSim = appxEnv.aggregate(env, var, singleSim);
        }

        //3 finalize
        boolean filtered = combinedSim > threshold;
        msg("\t [Similarity,\t " + dt1 + ",\t" + dt2 + "]: c:" + format(singleSim) + ", all:" + format(combinedSim) + ", " + filtered);

        if (notExisted) {
            appxEnv.add(var, dt2, dt1, algs, singleSim);
        }
        return filtered ? TRUE : FALSE;
    }

    
    /** 
     * filter approximate(var1, var2, 'ng-jw-wn-eq-mult', 0.2, true)
        args[0] = var 1
        args[1] = uri
        args[2] = alg list
        args[3] = threshold
        args[4] = false|true.
    */
    private IDatatype eval(Expr exp, Environment env, IDatatype[] args) {
        if (args.length != 4) {
            return FALSE;
        }

        IDatatype dt1 = args[0];
        IDatatype dt2 = args[1];
        if (dt1.stringValue() == null || dt2.stringValue() == null) {
            return null;
        }
        //IF the types are different, then return FALSE directly
        boolean match = match(dt1.getCode(), dt2.getCode());
        if (! match){
            return FALSE;
        }

        if (!(args[2] instanceof CoreseStringLiteral)
                || !args[3].isNumber()) {
            return FALSE;
        }

        String algs = args[2].stringValue();
        double threshold = args[3].doubleValue();

        String parameter = null;
        if (dt1.isURI()) {
            parameter = BaseAlgorithm.OPTION_URI;
        }

        ASTQuery ast =  env.getQuery().getAST();
        //is approximate search
        //TRUE: only calculate once, does not compute the value of similarity
        //FALSE: need to compute the value of similarity
        if (ast.isRelax()) {
            return this.approximate(dt1, dt2, parameter, algs, threshold, env, exp);
        } else {
            return this.match(dt1, dt2, parameter, algs, threshold);
        }

    }
    
    boolean match(int c1, int c2){
        if (c1 == c2){
            return true;
        }
        if ((c1 == IDatatype.STRING && c2 == IDatatype.LITERAL) ||
            (c2 == IDatatype.STRING && c1 == IDatatype.LITERAL)){
            return true;
        }
        
        return false;
    }
  
}
