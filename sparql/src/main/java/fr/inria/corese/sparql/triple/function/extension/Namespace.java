package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Namespace extends TermEval {

    public Namespace() {}
    
    public Namespace(String name) {
        super(name);
        //setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }

        switch (oper()) {
            case ExprType.XT_DEFINE:
                return define(dt, eval, b, env, p);
                
            case ExprType.XT_EXPAND: return uri(env, dt);
            case ExprType.QNAME:
                if (arity() == 2) {
                    // deprecated, use xt:expand(x)
                    return uri(env, dt);
                }
                return qname(env, dt);
                
            case ExprType.XT_DOMAIN:
                if (getArity()>1) {
                    IDatatype dt2 = getBasicArg(1).eval(eval, b, env, p);
                    if (dt2 != null) {
                        return DatatypeMap.URIDomain(dt, dt2);
                    }
                }
                return DatatypeMap.URIDomain(dt);
        }

        return null;

    }    
    
    IDatatype define(IDatatype dt, Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt2 = getBasicArg(1).eval(eval, b, env, p);
        if (dt2 == null) {
            return null;
        }
        nsm(env).definePrefix(dt.getLabel(), dt2.getLabel());
        return dt2;
    }
    
    
    NSManager nsm(Environment env) {        
        ASTQuery ast =  env.getQuery().getAST();
        return ast.getNSM();
    }

    IDatatype qname(Environment env, IDatatype dt) {
        if (!dt.isURI()) {
            return dt;
        }        
        String qname = nsm(env).toPrefix(dt.getLabel(), true);
        if (qname.equals(dt.getLabel())) {
            return dt;
        }
        return DatatypeMap.newInstance(qname);
    }
    
    IDatatype uri(Environment env, IDatatype dt) {           
        String uri = nsm(env).toNamespace(dt.getLabel());
        if (uri.equals(dt.getLabel())) {
            return dt;
        }
        return DatatypeMap.newResource(uri);
    }

   
}
