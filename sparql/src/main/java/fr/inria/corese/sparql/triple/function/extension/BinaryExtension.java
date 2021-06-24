package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.ExprType.EXTCONT;
import static fr.inria.corese.kgram.api.core.ExprType.EXTEQUAL;
import static fr.inria.corese.kgram.api.core.ExprType.XPATH;
import static fr.inria.corese.kgram.api.core.ExprType.XT_COMPARE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_DISTANCE;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.function.StringHelper;
import fr.inria.corese.sparql.datatype.function.VariableResolverImpl;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class BinaryExtension extends TermEval {

    public BinaryExtension(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt1 = getBasicArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getBasicArg(1).eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }

        switch (oper()) {
            case EXTEQUAL: {
                boolean bb = StringHelper.equalsIgnoreCaseAndAccent(dt1.getLabel(), dt2.getLabel());
                return (bb) ? TRUE : FALSE;
            }

            case EXTCONT: {
                boolean bb = StringHelper.containsWordIgnoreCaseAndAccent(dt1.getLabel(), dt2.getLabel());
                return (bb) ? TRUE : FALSE;
            }
            
            case XPATH: {
                // xpath(?g, '/book/title')
                getProcessor().setResolver(new VariableResolverImpl(env));
                IDatatype res = getProcessor().xpath(dt1, dt2);
                return res;
            }
            
            case XT_COMPARE:
               return value(dt1.compareTo(dt2));  
               
            case XT_DISTANCE:
                return distance(dt1, dt2);
                
        }
        return null;
    }
    
    IDatatype distance(IDatatype dt1, IDatatype dt2) {
        Integer i = LevenshteinDistance
                .getDefaultInstance()
                .apply(dt1.getLabel(), dt2.getLabel());
        return DatatypeMap.newInstance(i);
    }
}
