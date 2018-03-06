package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getBasicArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getBasicArg(1).eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }

        switch (oper()) {
           
        }

        return null;

    }

   
}
