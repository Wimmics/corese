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
public class UnaryExtension extends TermEval {

    public UnaryExtension(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }

        switch (oper()) {
            case ExprType.INDEX:
                return index(dt, p);
        }

        return null;

    }

    IDatatype index(IDatatype dt, Producer p) {
        Node n = p.getNode(dt);
        if (n == null) {
            return null;
        }
        return value(n.getIndex());
    }
}
