package fr.inria.corese.compiler.java;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import static fr.inria.corese.compiler.java.JavaCompiler.SPACE;

/**
 *
 * Compile exists {} and subquery clause with kgram() function
 * Pass bound variables as parameters:  kgram(query, "?x", x)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Pattern {
    
    JavaCompiler jc;
    Datatype dtc;
    Stack stack;
    
    Pattern(JavaCompiler jc){
        this.jc = jc;
        this.dtc = jc.dtc;
        this.stack = jc.stack;
    }
    
    void exist(Term term){
        if (term.isSystem()){
            query(term.getExist());
        }
        else {
            exist(term.getExist());
        }
    }
    
    StringBuilder append(String str){
        return jc.append(str);
    }
    
    /**
     * Compile subquery: 
     * let (?m = select where {})
     * let (?g = construct {} where {})
     * exp = exists { select|construct where }
     * 
     * return kgram("select where", "?x", x)
     */
    void query(Exp exp){
        ASTQuery ast = exp.get(0).get(0).getQuery();
        ast.validate();
        String cleanQuery = dtc.string(clean(ast.toString()));
        // args: ast select variables that are bound in stack
        // pass them to kgram as Mapping: kgram(query, "?x", x)
        StringBuilder args = getStackBinding(ast);
        String str = String.format("kgram(%s%s)", cleanQuery, args);        
        append(str);
    }
    
    /**
     * select variable from ast that are bound in current stack
     * generate:   "?x", x, "?y", y
     */
    StringBuilder getStackBinding(ASTQuery ast) {
        StringBuilder sb = new StringBuilder();
        for (Variable var : stack.getVariables()) {
            if (ast.isSelectVariable(var)) {
                // SPARQL variable name: "?x"
                sb.append(", ").append(dtc.string(var.getName()));
                // Java variable name: x
                sb.append(", ").append(jc.name(var));
            }
        }
        return sb;
    }

    /**
     * TODO: return only variables relevant for exists clause
     */
    StringBuilder getStackBinding(){
        StringBuilder sb = new StringBuilder();
        for (Variable var : stack.getVariables()){
            sb.append(", ").append(dtc.string(var.getName()));
            sb.append(", ").append(jc.name(var));
        }
        return sb;
    }
    
     StringBuilder getStackVariable(){
        StringBuilder sb = new StringBuilder();
        for (Variable var : stack.getVariables()){
            sb.append(var.getName()).append(SPACE);
        }
        return sb;
    }
    
    
    String clean(String str){
        return str.replace("\n", "\\n");
    }
    
     /**
      * compile exists clause as a subquery
      * the result of exists is bound to a generated boolean bind variable (here ?b)
      * pass bound variables as parameters of kgram function
      * gget return the value of boolean variable ?b
      * 
      * return gget(kgram('select ?x ?b where { bind (exists {?x ?p ?y } as ?b) }', '?x', ?x), '?b')
      */
    void exist(Exp exp) {
        String var = jc.getExistVar();
        String vars = getStackVariable().toString();
        
        String query = dtc.string(
           String.format(
           "select %1$s %2$s where { bind (%3$s as %1$s) } values (%2$s) { (%4$s) }", 
           var, vars, clean(exp.toString()), undef()));
        
        StringBuilder args = getStackBinding();
            
        String str = 
           String.format("GetGen.gget(kgram(%s%s), %s)", query, args, dtc.string(var));
         
        append(str);
    }
    
    String undef(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stack.getVariables().size(); i++){
            sb.append("UNDEF ");
        }
        return sb.toString();
    }
   

}
