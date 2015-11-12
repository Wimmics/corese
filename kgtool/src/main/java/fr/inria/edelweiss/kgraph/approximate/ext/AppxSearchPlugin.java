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
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;
import static fr.inria.edelweiss.kgraph.approximate.similarity.Utils.format;
import static fr.inria.edelweiss.kgraph.approximate.similarity.Utils.msg;
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

                String s1, s2;
                IDatatype dt1 = this.getIDatatype(args[0]);
                IDatatype dt2 = this.getIDatatype(args[1]);

                //IF the types are different, then return FALSE directly
                if (dt1.getCode() != dt2.getCode()) {
                    return FALSE;
                } else if (dt1 instanceof CoreseURI) {

                    String[] uri1 = Utils.split(dt1.getLabel());
                    String[] uri2 = Utils.split(dt2.getLabel());

                    if (!uri1[0].equalsIgnoreCase(uri2[0])) {
                        return FALSE;
                    } else {
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

                //todo if types are URI
                String algs = ((CoreseStringLiteral) args[2]).getStringValue();

                msg("[Eval]:" + s1 + ",\t" + s2 + ",\t" + algs);
                if (s1 == null || s2 == null) {
                    return FALSE;
                }

                Key k = getKey(exp, args);
                Value r = getResult(exp, args, algs);

                //check if already calculated, if yes, just retrieve the value
                Double sim = SimilarityResults.getInstance().getSimilarity(k, r.getNode(), algs);
                boolean notExisted = false;

                //otherwise, re-calculate
                if (sim == null) {
                    notExisted = true;
                    //if equal, return 1
                    if (s1.equalsIgnoreCase(s2)) {
                        sim = ISimAlgorithm.MAX;
                    } else {
                        ISimAlgorithm alg = SimAlgorithmFactory.createCombined(algs);
                        sim = alg.calculate(s1, s2);
                    }
                }

                boolean filtered = sim > BaseAlgorithm.THRESHOLD;
                msg("\t [Similarity]: " + format(sim) + ", " + filtered + "\n");

                //if:   filter (approximate) returns true & the value is re-calculated, 
                //then: set the value of similarity & add the result to results set
                if (filtered && notExisted) {
                    r.setSimilarity(sim);
                    SimilarityResults.getInstance().add(k, r);
                }

                //IF:   filter (approximate) returns false & the value is existing
                //THEN: removes this record from result set
                if (!filtered && !notExisted) {
                    //SimilarityResults.getInstance().remove(k, r.getNode());
                }

                return filtered ? TRUE : FALSE;
            default:
                return null;
        }
    }

    private IDatatype getIDatatype(Object obj) {
        //System.out.println("Eval :"+obj+", " + obj.getClass().getName());
        if (obj instanceof CoreseURI) {
            return (CoreseURI) obj;
        } else if (obj instanceof CoreseStringLiteral) {
            return (CoreseStringLiteral) obj;
        } else {
            return (IDatatype) obj;
        }
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
