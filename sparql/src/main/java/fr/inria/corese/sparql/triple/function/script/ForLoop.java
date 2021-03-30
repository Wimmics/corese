package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.parser.ASTBuffer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ForLoop extends Statement {
    
    static final IDatatype TRUE = DatatypeMap.TRUE;

    public ForLoop() {}

        public ForLoop(Expression var, Expression exp, Expression body) {
        super(Processor.FOR, var, exp);
        add(body);
    }
    
    @Override
    public ASTBuffer toString(ASTBuffer sb) {         
        sb.append(Processor.FOR, " (");
        getArg(0).toString(sb);
        sb.append(" ", Processor.IN, " ");       
        getArg(1).toString(sb);
        sb.append(") {");
        sb.nlincr();
        getArg(2).toString(sb);
        sb.nldecr();
        sb.append("}");
        return sb;
    }  
    
     @Override
     public ForLoop getFor(){
         return this;
     }
     

    @Override
    public Variable getVariable() {
        return getBasicArg(0).getVariable();
    }

    @Override
    public Expression getDefinition() {
        return getBasicArg(1);
    }
    
    public void setDefinition(Expression exp) {
        // TODO: setExp ???
        setArg(1, exp);
    }

    @Override
    public Expression getBody() {
        return getBasicArg(2);
    }
    
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype list = getDefinition().eval(eval, b, env, p);
        if (list == null) {
            return null;
        }
        Variable var = getVariable();
        Expression body = getBody();
        IDatatype res = null;
        b.set(this, var, TRUE);
        for (IDatatype dt : list) {
            b.bind(this, var, dt);
            res = body.eval(eval, b, env, p);
            if (b.isResult()  || res == null) {
                b.unset(this, var, dt);
                return res;
            }
        }
        b.unset(this, var, TRUE);
        return TRUE;
    }
   
    
}
