package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.triple.function.script.Let;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Visit filter/select/bind expressions
 * function, let, map: declare arguments as local variables, 
 * generate variable index
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ExpressionVisitorVariable implements ExpressionVisitor {
  
    private static Logger logger = LoggerFactory.getLogger(ASTQuery.class);
   
    private boolean let = false;
    private boolean functionDefinition = false;
    private boolean trace = false;
    private int nbVariable = 0;
    
    // stack of defined variables: function parameter/let/for
    private List<Variable> list;
    private ASTQuery ast;
    private Function fun;
    HashMap<Variable, Integer> reference;

    ExpressionVisitorVariable() {
        list = new ArrayList<Variable>();
        reference = new HashMap<>();
    }
    
    // top level exp
    ExpressionVisitorVariable(ASTQuery ast) {
        this();
        this.ast = ast;
    }
    
    // function f(?x) { exp }
    ExpressionVisitorVariable(ASTQuery ast, Function fun) {
        this();
        this.ast = ast;
        this.fun = fun;
        functionDefinition = true;
        define(fun.getSignature().getFunVariables());
    }
       
    /**
     * Declare function parameters as local variables
     */
    void define(List<Variable> list){
        for (Variable var : list){
            define(var);
        }
    }
    
    /**
     * Declare function parameter and let/for variable
     */
    void define(Variable var){
        list.add(var);
        localize(var);
        var.setDeclaration(var);
        var.setIndex(getNbVariable());
        setNbVariable(getNbVariable() + 1);
    }
    
    void pop(Variable var){
        list.remove(list.size() - 1);
        reference.remove(var);
    }
    
    boolean reference(Variable var) {
        Variable decl = getDefinition(var);
        if (decl != null) {
            var.setDeclaration(decl);
            var.setIndex(decl.getIndex());
            defReference(decl);
            localize(var);
            return true;
        } 
        return false;
    }
    
    void defReference(Variable var) {
        Integer n = reference.get(var);
        if (n == null) {
            n = 0;
        }
        reference.put(var, 1 + n);
    }
    
    boolean hasReference(Variable var) {
        return reference.containsKey(var);
    }
    
    Variable getDefinition(Variable var) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Variable v = list.get(i);
            if (var.equals(v)) {
                return v;
            }
        }
        return null;
    }


    void localize(Variable var) {
        var.localize();
    }
    
    /**
     * Visit starts here
     */
    @Override
    public void start(Expression exp){
        exp.visit(this);
    }
       
    @Override
    public void visit(Exp exp) {
    }

    @Override
    public void visit(Term t) {
        switch (t.oper()){
            
            case ExprType.LET:
                let(t.getLet());
                break;
                
            case ExprType.FOR:
                loop(t);
                break;
                                                                      
//            case ExprType.AGGREGATE:
//                aggregate(t);
//                break;
                               
            case ExprType.EXIST:
               visitExist(t);
                // continue
                               
            default:
        
            process(t);
        }
    }
    
    
    void process(Term t) {
        for (int i=0; i<t.getArgs().size(); i++) {
            Expression e = t.getArg(i);
            e.visit(this);
            if (e.isVariable() && e.subtype() == ExprType.LOCAL){
                VariableLocal var = new VariableLocal(e.getLabel());
                var.setIndex(e.getIndex());
                t.setArg(i, var);
                t.setExp(i, var);
            }
        }
    }
            
    
    /**
     * function xt:fun(?x) { exp }
     * create a new Visitor to have own local variables index
     */
    @Override
    public void visit(Function f) {
        if (! f.isVisited()) {
           f.setVisited(true);
           function(f);
        }
    }
      

    @Override
    public void visit(Variable v) {
        variable(v);       
    }

    @Override
    public void visit(Constant c) {
    }
        
    void function(Function f) {
        if (trace) {
            System.out.println("**** Vis Fun: " + f);
        }
        
        if (isFunctionDefinition()){
            // f is lambda inside a function
            if (fun.isPublic()){
                // f is also public
                f.setPublic(true);
            }
        }
        
        Expression body = f.getBody();
        ExpressionVisitorVariable vis = new ExpressionVisitorVariable(ast, f);
        vis.start(body);
        f.setNbVariable(vis.getNbVariable());
        ast.define(f);        
    }
    
    void visitExist(Term t) {
        if (isFunctionDefinition()) {
            // inside a function 
            // special case: export function contain exists {}
            // will be evaluated with query that defines function
            fun.setSystem(true);
            if (t.isSystem() && fun.hasMetadata()) {
                // let (?m = select where {}){}
                Exp e = t.getExist().getBody().get(0).get(0);
                if (e.isQuery()) {
                    ASTQuery ast = e.getQuery();
                    ast.inherit(fun.getMetadata());
                }
            }
        }
    }
       
    /**
     * Visit variable in statement: declare it as local if it
     * corresponds to a parameter/let/for variable
     * Variable var refers to its declaration decl
     */
    void variable(Variable var) {
        if (reference(var)){   
            // ok
        }       
        else if (isFunctionDefinition()) {
//            ast.addError("Undefined variable: " + var + " in function: " + fun.getSignature().getName());
//            ast.addFail(true);
            var.undef();
        }
    }

    /**
     * let (var = exp) { body }
     * for (var in exp) { body }
     * declare var as local variable in body
     * toplevel means SPARQL filter (not in function)
     * toplevel let/for manage its own number of local variables for stack allocation (Bind)
     */ 
    void let(Let t) {
        boolean isTopLevel = isTopLevel();
        
        for (Expression decl : t.getDeclaration()){
            Variable var    = t.getVariable(decl);
            Expression exp  = t.getDefinition(decl);        
            exp.visit(this);
            define(var);
        }
        
        t.getBody().visit(this);
        
        boolean letquery = t.isLetQuery() && ! isTopLevel;
        ArrayList<Expression> list = new ArrayList<>();
       
        for (int i = t.getDeclaration().size() - 1; i>=0; i--) {
            Expression decl = t.getDeclaration().get(i);
            Variable var = t.getVariable(decl);
            if (letquery && !hasReference(var)) {
                list.add(decl);
            }
            pop(var);
        }
        
        for (Expression decl : list) {
            t.removeDeclaration(decl);
            //setNbVariable(getNbVariable() - 1);
        }
        
        if (isTopLevel) {
            // top level let/for, not in function
            t.setNbVariable(getNbVariable());
            setNbVariable(0);
        }
    }
    
     void loop(Term t) {
        boolean isTopLevel = isTopLevel();
        
        Variable var    = t.getVariable();
        Expression exp  = t.getDefinition();
        
        exp.visit(this);
        define(var);
        
        t.getBody().visit(this);
        
        pop(var);
        
        if (isTopLevel) {
            // top level let/for, not in function
            t.setNbVariable(getNbVariable());
            setNbVariable(0);
        }
    }
    
       
    /**
     * aggregate(exp, us:mediane)
     */
//    void aggregate(Term t){
//        t.getArg(0).visit(this);
//    }
    
    boolean isTopLevel(){
        return isExpression() && getNbVariable() == 0;
    }
    
    boolean isExpression() {
        return ! isFunctionDefinition();
    }
         
    /**
     * @return the functionDefinition
     */
    public boolean isFunctionDefinition() {
        return functionDefinition;
    }

    /**
     * @param functionDefinition the functionDefinition to set
     */
    public void setFunctionDefinition(boolean functionDefinition) {
        this.functionDefinition = functionDefinition;
    }
    
     /**
     * @return the nbVariable
     */
    public int getNbVariable() {
        return nbVariable;
    }

    /**
     * @param nbVariable the nbVariable to set
     */
    public void setNbVariable(int nbVariable) {
        this.nbVariable = nbVariable;
    }
    
}
