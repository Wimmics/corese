package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Visit filter/select/bind expressions
 * function, let, map: declare arguments as local variables, index
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ExpressionVisitorVariable implements ExpressionVisitor {

    boolean let = false;
    boolean define = false;
    boolean trace = false;
    int count = 0;
    int clet = 0;
    
    List<Variable> list;
    HashMap<String, Integer> map;
    ASTQuery ast;

    ExpressionVisitorVariable() {
        map = new HashMap<String, Integer>();
        list = new ArrayList<Variable>();
    }
    
    // top level exp
    ExpressionVisitorVariable(ASTQuery ast) {
        this();
        this.ast = ast;
        init();
    }
    
    // define (f(?x) = exp)
    ExpressionVisitorVariable(ASTQuery ast, List<Variable> list) {
        this();
        this.list = list;
        this.ast = ast;
        define = true;
        init();
    }
    
    void init(){
        trace = ast.isDebug();
        if (list != null){
            declare();
        }
    }
    
    void declare(){
        for (Variable var : list){
            localize(var);
        }
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
                
            case ExprType.STL_DEFINE:
                define(t);
                break;
                                           
            case ExprType.MAP:
            case ExprType.MAPLIST:                
                map(t);
                break;
               
                
            case ExprType.PACKAGE:
                export(t); 
                // continue
                
            default:
        
            for (Expression e : t.getArgs()) {
                e.visit(this);
            }
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
    
    
    
    
    void variable(Variable v) {
        if (isLocal(v)) {
            localize(v);
        } 
        else if (define) {
            v.undef();
        }
    }

    
    
    /**
     * define(xt:fun(?x) = exp)
     * create a new Visitor to have own local variables index
     */
    void define(Term t){
        if (trace) System.out.println("Vis Fun: " + t);
        Term fun  = t.getArg(0).getTerm();
        Term def  = fun.getArg(0).getTerm();
        Expression body = fun.getArg(1);
        
        List<Variable> list = def.getFunVariables(); 
        ExpressionVisitorVariable vis = new ExpressionVisitorVariable(ast, list);
        body.visit(vis);
        
        fun.setPlace(vis.count());
        ast.define(fun);
        if (trace) System.out.println("count: " + vis.count());
    }
    
    
    void export(Term t) {
        for (Expression exp : t.getArgs()) {
            if (exp.isTerm() && !exp.getArgs().isEmpty()) {
                Expression fun = exp.getArg(0);
                fun.setExport(true);
            }
        }
    }
    
    /**
     * let (?x = e1, e2)
     */
    void let(Term t){
        if (trace) System.out.println("Vis Let: " + t);
        Expression def  = t.getArg(0);
        Expression body = t.getArg(1);
        if (def.isTerm() 
                && def.getArgs().size() == 2 
                && def.getArg(0).isVariable()){
            
            Variable var    = def.getArg(0).getVariable();
            Expression exp  = def.getArg(1);        

            if (isLocal(var)){
                ast.addError("Variable already defined: " + var);
                ast.addFail(true);
            }
            else {
                exp.visit(this);
                localize(var);
                list.add(var);
                clet++;
                body.visit(this);
                clet--;
                list.remove(list.size()-1);
                remove(var);
                if (! define && clet == 0){
                    // top level let
                    t.setPlace(count);
                }
            }
        }
        else {
            ast.setError("Incorrect Let: " + t);
            ast.setFail(true);
        }
    }
    
    
    
    
    /**
     * map (xt:fun(?x), ?list)
     * var ?x is local because it is interpreted as:
     * (for dt : ?list) {let (?x = dt, xt:fun(?x))}
    */
    void map(Term t) {
        if (trace) System.out.println("Vis Map: " + t);
        if (t.getArgs().size() == 2){
            Expression fun = t.getArg(0);
            if (fun.isFunction() && fun.getArgs().size() == 1) {
                Expression arg = fun.getArg(0);
                if (arg.isVariable()){
                    Variable var = arg.getVariable();
                    localize(var);
                    remove(var);
                    t.getArg(1).visit(this);
                    return;
                }
            }
        }
        ast.setError("Incorrect Map: " + t);
        ast.setFail(true);
    }
    
   
    boolean isLocal(Variable var) {
        for (Variable v : list) {
            if (v.equals(var)) {
                return true;
            }
        }
        return false;
    }

    void index(Variable var) {
        Integer n = map.get(var.getLabel());
        if (n == null) {
            count += 1;
            n = count;
            map.put(var.getLabel(), n);
        }
        var.setIndex(n);
        if (trace) System.out.println("EVL: " + var + " " + n);
    }
    
    void remove(Variable var){
        map.remove(var.getLabel());
    }

    void localize(Variable var) {
        var.localize();
        index(var);
    }
}
