package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ZeroAry extends TermEval {

    public ZeroAry() {}
    
    public ZeroAry(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        switch (oper()) {
            case ExprType.LENGTH: return pathLength(env);
        }
        return null;
    }

    IDatatype pathLength(Environment env) {
        Node qNode = env.getQueryNode(getExp(0).getLabel());
        if (qNode == null) {
            return null;
        }
        return value(env.pathLength(qNode));
    }

}
