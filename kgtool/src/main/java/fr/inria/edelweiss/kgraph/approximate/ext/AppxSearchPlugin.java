package fr.inria.edelweiss.kgraph.approximate.ext;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.CoreseStringLiteral;
import fr.inria.acacia.corese.cg.datatype.CoreseURI;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgraph.approximate.result.Key;
import fr.inria.edelweiss.kgraph.approximate.result.Value;
import fr.inria.edelweiss.kgraph.approximate.result.SimilarityResults;
import fr.inria.edelweiss.kgraph.approximate.similarity.ISimAlgorithm;
import fr.inria.edelweiss.kgraph.approximate.similarity.SimAlgorithmFactory;
import static fr.inria.edelweiss.kgraph.approximate.similarity.Utils.format;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.BaseAlgorithm;
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

    //private boolean isCalculateSimilarity = true;
    public AppxSearchPlugin(PluginImpl p) {
        this.plugin = p;
    }

//    public Object function(Expr exp, Environment env, Producer p) {
//        return null;
//    }
//
//    public Object function(Expr exp, Environment env, Producer p, IDatatype dt) {
//        return null;
//    }
//
//    public Object function(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
//        return null;
//    }
    //filter approximate(var1, var2, 'alg1-alg2-alg3')
    //args[0] = var 1
    //args[1] = var 2 | uri
    //args[2] = alg list
    //args[3] = variable (optional)
    //args[4] = uri (optional)
    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {
        switch (exp.oper()) {
            case APPROXIMATE:

                String s1 = this.getString(args[0]);
                String s2 = this.getString(args[1]);
                String algs = this.getString(args[2]);

                System.out.println("[Eval]:" + s1 + ",\t" + s2 + ",\t" + algs);
                if (s1 == null || s2 == null) {
                    return FALSE;
                }

                Key k = getKey(exp, args);
                Value r = getResult(exp, args, algs);

                //check if already calculated, if yes, just retrieve the value
                Double sim = SimilarityResults.getInstance().getSimilarity(k, r.getNode(), algs);
                boolean reCalculated = false;

                //otherwise, re-calculate
                if (sim == null) {
                    reCalculated = true;
                    //if equal, return 1
                    if (s1.equalsIgnoreCase(s2)) {
                        sim = ISimAlgorithm.MAX;
                    } else {
                        ISimAlgorithm alg = SimAlgorithmFactory.createCombined(algs);
                        sim = alg.calculate(s1, s2);
                    }
                }

                boolean b = sim > BaseAlgorithm.THRESHOLD;
                System.out.println("\t [Similarity]: " + format(sim) + ", " + b + "\n");

                if (b && reCalculated) {
                    r.setSimilarity(sim);
                    SimilarityResults.getInstance().add(k, r);
                }

                return b ? TRUE : FALSE;
            default:
                return null;
        }
    }

    private String getString(Object obj) {
        //System.out.println("Eval :"+obj+", " + obj.getClass().getName());
        if (obj instanceof CoreseURI) {
            return ((CoreseURI) obj).getLabel();
        } else if (obj instanceof CoreseStringLiteral) {
            return ((CoreseStringLiteral) obj).getStringValue();
        }

        return null;
    }

    private IDatatype getIDatatype(Object obj) {
        //System.out.println("Eval :"+obj+", " + obj.getClass().getName());
        if (obj instanceof CoreseURI) {
            return (CoreseURI) obj;
        } else if (obj instanceof CoreseStringLiteral) {
            return (CoreseStringLiteral) obj;
        }

        return null;
    }

    private Key getKey(Expr exp, Object[] args) {
        Expr e0 = exp.getExp(0);
        Expr e1 = exp.getExp(1);
        Key k;
        if (e0.isVariable() && e1.isVariable()) {
            //filter app(?label1, ?label2, "alg", ?var, <uri>)
            k = new Key(exp.getExp(3), getIDatatype(args[4]));
        } else {
            //filter app(?var, uri, "algs")
            k = new Key(e0, getIDatatype(args[1]));
        }
        return k;
    }

    private Value getResult(Expr exp, Object[] args, String algs) {
        Expr e0 = exp.getExp(0);
        Expr e1 = exp.getExp(1);
        Value r;
        if (e0.isVariable() && e1.isVariable()) {
            //filter app(?label1, ?label2, "alg", ?var, <uri>)
            r = new Value((IDatatype) args[3], algs);
        } else {
            //filter app(?var, uri, "algs")
            r = new Value((IDatatype) args[0], algs);
        }
        return r;
    }
}
