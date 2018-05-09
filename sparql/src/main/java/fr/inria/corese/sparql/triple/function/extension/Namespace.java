package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
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
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }

        switch (oper()) {
            case ExprType.QNAME:
                return qname(env, dt);
        }

        return null;

    }
    
    
    NSManager nsm(Environment env) {        
        ASTQuery ast = (ASTQuery) env.getQuery().getAST();
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

   
}
