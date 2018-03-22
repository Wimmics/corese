package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.triple.function.script.Statement;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Let extends Statement {

    public Let() {}
    
    public Let(Expression def, Expression body) {
        super(Processor.LET, def, body);
    }

    @Override
    public Let getLet() {
        return this;
    }

    /**
     * let (var = exp){ exp }
     *
     * @return
     */
    @Override
    public Variable getVariable() {
        return getVariableDefinition().getBasicArg(0).getVariable();
    }

    @Override
    public Expression getDefinition() {
        return getVariableDefinition().getBasicArg(1);
    }

    @Override
    public Expression getBody() {
        return getBasicArg(1);
    }

    public Expression getVariableDefinition() {
        return getBasicArg(0);
    }

    @Override
    public StringBuffer toString(StringBuffer sb) {
        sb.append(Processor.LET);
        Expression def = getArg(0);
        sb.append(" (");
        getArg(0).getArg(0).toString(sb);
        sb.append(" = ");
        // may be match() after parsing ...
        getDefinition().toString(sb);
        sb.append(") {");
        sb.append(NL);
        sb.append("  ");
        getBody().toString(sb);
        sb.append(NL);
        sb.append("}");
        return sb;
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype val = getDefinition().eval(eval, b, env, p);
        if (val == null) {
            return null;
        }
        if (val == DatatypeMap.UNBOUND) {
            val = null;
        }
        Variable var = getVariable();
        b.set(this, var, val);
        IDatatype res = getBody().eval(eval, b, env, p);
        env.unset(this, var, val);
        return res;
    }
    
    @Override
    public void tailRecursion(Function fun) {
        getBody().tailRecursion(fun);
    }
}
