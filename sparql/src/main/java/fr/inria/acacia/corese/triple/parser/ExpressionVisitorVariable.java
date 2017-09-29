package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Visit filter/select/bind expressions
 * function, let, map: declare arguments as local variables, index
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ExpressionVisitorVariable implements ExpressionVisitor {
  
    private static Logger logger = LogManager.getLogger(ASTQuery.class);
   
    private boolean let = false;
    private boolean functionDefinition = false;
    private boolean trace = false;
    private int count = 0;
    private int clet = 0;
    private int nbVariable = 0;
    
    // stack of defined variables: function parameter/let/for
    List<Variable> list;
    ASTQuery ast;
    Function fun;

    ExpressionVisitorVariable() {
        list = new ArrayList<Variable>();
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
        var.setIndex(getNbVariable());
        setNbVariable(getNbVariable() + 1);
    }
    
    void pop(Variable var){
        list.remove(list.size() - 1);
    }
    
    boolean reference(Variable var) {
        Variable decl = getDefinition(var);
        if (decl != null) {
            var.setDeclaration(decl);
            localize(var);
            return true;
        } 
        return false;
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
                let(t);
                break;
                
            case ExprType.FOR:
                loop(t);
                break;
                                                                      
            case ExprType.AGGREGATE:
                aggregate(t);
                break;
                               
            case ExprType.EXIST:
               visitExist(t);
                // continue
                               
            default:
        
            for (Expression e : t.getArgs()) {
                e.visit(this);
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
    
    
    int count(){
        return count;
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
        //body.visit(vis);
        vis.start(body);
        f.setNbVariable(vis.getNbVariable());

        f.setPlace(vis.count());
        ast.define(f);
        if (trace) {
            System.out.println("count: " + vis.count());
        }
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
            logger.error("Undefined variable: " + var + " in function: " + fun.getSignature().getName());
            var.undef();
        }
    }

        
    /**
     * @deprecated
     * 
     */
    void export(Term t) {
        for (Expression exp : t.getArgs()) {
            if (exp.isTerm() && !exp.getArgs().isEmpty()) {
                Expression fun = exp.getArg(0);
                fun.setPublic(true);
            }
        }
    }
        
    void letloop(Term t) {
        Variable var = t.getVariable();
        Expression exp = t.getDefinition();
        Expression body = t.getBody();

        exp.visit(this);
        define(var);
        clet++;
        body.visit(this);
        clet--;
        pop(var);
        if (!isFunctionDefinition() && clet == 0) {
            // top level let
            t.setPlace(count);
        }
    }
    
    /**
     * let (var = exp) { body }
     */    
    void let(Term t) {
        letloop(t);
    }
    
    // for (var in exp){ body }
    void loop(Term t) {
        letloop(t);
    }
    
       
    /**
     * aggregate(exp, us:mediane)
     */
    void aggregate(Term t){
        t.getArg(0).visit(this);
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
