package fr.inria.acacia.corese.triple.parser;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ForLoop extends Statement {

    ForLoop(Expression var, Expression exp, Expression body) {
        super(Processor.FOR, var, exp);
        add(body);
    }
    
    @Override
    public StringBuffer toString(StringBuffer sb) {         
        sb.append(Processor.FOR);
        sb.append(" (");
        getArg(0).toString(sb);
        sb.append(" ");       
        sb.append(Processor.IN);
        sb.append(" ");       
        getArg(1).toString(sb);
        sb.append(")");
        sb.append(" {");
        sb.append(Term.NL);
        sb.append("  ");
        getArg(2).toString(sb);
        sb.append(Term.NL);
        sb.append("}");
        return sb;
    }  
    
     @Override
     public ForLoop getFor(){
         return this;
     }
     

    @Override
    public Variable getVariable() {
        return getArg(0).getVariable();
    }

    @Override
    public Expression getDefinition() {
        return getArg(1);
    }

    @Override
    public Expression getBody() {
        return getArg(2);
    }
}
