package fr.inria.corese.compiler.java;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import static fr.inria.corese.compiler.java.JavaCompiler.SPACE;

/**
 *
 * Compile exists {} and subquery clause
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
     * exp = exists { select where }
     */
    void query(Exp exp){
        ASTQuery ast = exp.get(0).get(0).getQuery();
        ast.validate();
        String cleanQuery = dtc.newInstance(clean(ast.toString()));
        // args: ast select variables that are bound in stack
        // pass them to kgram as Mapping: kgram(query, "?x", x)
        String args = getStackBinding(ast).toString();
        String str = String.format("kgram(%s%s)", cleanQuery, args);        
        append(str);
    }
    
    /**
     * select variable from ast that are bound in current dtack
     */
    StringBuilder getStackBinding(ASTQuery ast) {
        StringBuilder sb = new StringBuilder();
        for (Variable var : stack.getVariables()) {
            if (ast.isSelectVariable(var)) {
                // SPARQL variable name: "?x"
                sb.append(", ").append(dtc.newInstance(var.getName()));
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
            sb.append(", ").append(dtc.newInstance(var.getName()));
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
     * gget(kgram('select ?x ?b where { bind(exists {?x ?p ?y } as ?b) }', '?x', ?x), '?b')
     */
    void exist(Exp exp){
        String var = jc.getExistVar();
        String vars = getStackVariable().toString();
        
        String query = dtc.newInstance(
           String.format(
           "select %1$s %2$s where { bind(%3$s as %1$s) }", var, vars, exp));
        
        String args = getStackBinding().toString();
            
        String str = 
           String.format(
                "gget(kgram(%s%s), %s)", query, args, dtc.newInstance(var));
         
        append(str);
    }
    
    
   

}
