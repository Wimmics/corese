package fr.inria.acacia.corese.triple.parser;

/**
 * Function definition function xt:fun(x) { exp }
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Function extends Statement {

    Function(Term fun, Expression body) {
        super(Processor.FUNCTION, fun, body);
    }

    @Override
    public Term getFunction() {
        return getArg(0).getTerm();
    }

    @Override
    public Expression getBody() {
        return getArg(1);
    }

    public StringBuffer toString(StringBuffer sb) {
        sb.append(getLabel());
        sb.append(" ");
        getFunction().toString(sb);
        sb.append(" { ");
        sb.append(Term.NL);
        getBody().toString(sb);
        sb.append(" }");
        return sb;
    }
}
