package fr.inria.acacia.corese.triple.parser;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Let extends Statement {

     Let (Expression def, Expression body) {
        super(Processor.LET, def, body);
    }
     
     @Override
     public Let getLet(){
         return this;
     }
     
      /**
       * let (var = exp){ exp }
       * @return 
       */
        @Override
      public Variable getVariable(){
          return getVariableDefinition().getArg(0).getVariable();
      }
        
        @Override
        public Expression getDefinition(){
            return getVariableDefinition().getArg(1);
        }
        
        @Override
        public Expression getBody(){
            return getArg(1);
        }
        
        public Expression getVariableDefinition(){
            return getArg(0);
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
    
}
