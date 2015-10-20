package fr.inria.acacia.corese.triple.parser;

/**
 * Function definition
 * function xt:f(x) { exp }
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Function extends Expression {
    
   Term fun;
   Expression body;
   
   Function(Term f, Expression b){
       fun = f;
       body = b;
   }
   
  public Term getFunction(){
       return fun;
   }
   
  public Expression getBody(){
       return body;
   }
   
   public String toString(){
       StringBuffer sb = new StringBuffer();
       fun.toString(sb);
       sb.append(" { ");
       body.toString(sb);
       sb.append(" }");
       return sb.toString();
   }

}
