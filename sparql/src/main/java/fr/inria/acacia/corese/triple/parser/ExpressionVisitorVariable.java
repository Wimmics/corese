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
    Function fun;

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
    
    // function f(?x) { exp }
    ExpressionVisitorVariable(ASTQuery ast, Function fun) {
        this();
        this.list = fun.getFunction().getFunVariables();
        this.ast = ast;
        this.fun = fun;
        define = true;
        init();
    }
    
    void init(){
        //trace = ast.isDebug();
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
                
            case ExprType.FOR:
                loop(t);
                break;
                
            case ExprType.FUNCTION:
                define((Function) t);
                break;
                                           
            case ExprType.MAP:
            case ExprType.MAPLIST:                
            case ExprType.MAPMERGE:                
            case ExprType.MAPFIND:                
            case ExprType.MAPFINDLIST:                
            case ExprType.MAPEVERY:                
            case ExprType.MAPANY: 
                 map(t);
                break;
                
            case ExprType.APPLY:                
                map(t);
                //apply(t);
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
    
    void visitExist(Term t) {
        if (fun != null) {
            // inside a function 

            //if (fun.isExport()) {
                // special case: export function contain exists {}
                // will be evaluated with query that defines function
                fun.setSystem(true);
            //}

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
     * function(xt:fun(?x) = exp)
     * create a new Visitor to have own local variables index
     */
    void define(Function t){
        if (trace) System.out.println("Vis Fun: " + t);
        Expression body = t.getBody(); 
        
        ExpressionVisitorVariable vis = new ExpressionVisitorVariable(ast, t);
        body.visit(vis);
        
        t.setPlace(vis.count());
        ast.define(t); //fun);
        if (trace) System.out.println("count: " + vis.count());
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
    
    /**
     * let (?x = e1, e2)
     */
    void let(Term t){
        if (trace) System.out.println("Vis Let: " + t);
            Variable var    = t.getVariable();
            Expression exp  = t.getDefinition();           
            Expression body = t.getBody();       

//            if (isLocal(var)){
//                ast.addError("Variable already defined: " + var);
//                ast.addFail(true);
//            }
//            else 
            {
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
    
    // for (?x in exp){ exp }
    void loop(Term t) {
        Variable var    = t.getVariable();
        Expression exp  = t.getDefinition();
        Expression body = t.getBody();

//        if (isLocal(var)) {
//            ast.addError("Variable already defined: " + var);
//            ast.addFail(true);
//        } 
//        else 
        {
            exp.visit(this);
            localize(var);
            list.add(var);
            clet++;
            body.visit(this);
            clet--;
            list.remove(list.size() - 1);
            remove(var);
            if (!define && clet == 0) {
                // top level let
                t.setPlace(count);
            }
        }
    }
    
    
    /**
     * map (xt:fun(?x), ?list)
     * var ?x is local because it is interpreted as:
     * (for dt : ?list) {let (?x = dt, xt:fun(?x))}
    */
    void map(Term t) {
        if (trace) System.out.println("Vis Map: " + t);
        if (t.getArgs().size() >= 2){
            Expression fun = t.getArg(0);
            if (fun.isFunction()) {
                for (Expression arg : fun.getArgs()){  
                    if (arg.isVariable()){
                        Variable var = arg.getVariable();
                        localize(var);
                        remove(var);
                    }
                }                
            }
            else if (fun.isVariable()){
                Variable var = fun.getVariable();
                localize(var);
                remove(var);
            }
            
            for (int i = 1; i<t.getArgs().size(); i++){
                t.getArg(i).visit(this);
            }
        }       
    }
    
    void map2(Term t) {
        if (trace) System.out.println("Vis Map: " + t);
        if (t.getArgs().size() >= 2){
            Expression fun = t.getArg(0);
            if (fun.isFunction()) {
                for (Expression arg : fun.getArgs()){  
                    if (arg.isVariable()){
                        Variable var = arg.getVariable();
                        localize(var);
                        remove(var);
                    }
                }
                for (int i = 1; i<t.getArgs().size(); i++){
                    t.getArg(i).visit(this);
                }
                return;
            }
        }
        ast.setError("Incorrect Map: " + t);
        ast.setFail(true);
    }
    
    void apply(Term t) {
        if (trace) System.out.println("Vis Apply: " + t);
        if (t.getArgs().size() >= 2){        
            for (int i = 1; i < t.getArgs().size(); i++) {
                t.getArg(i).visit(this);
            }
            return;
        }
        ast.setError("Incorrect Apply: " + t);
        ast.setFail(true);
    }
    
    /**
     * aggregate(?x, xt:mediane(?list))
     * @param t 
     */
    void aggregate(Term t){
        t.getArg(0).visit(this);
        if (t.getArgs().size() == 2){
            Expression fun = t.getArg(1);
            Expression arg = fun.getArg(0);
            Variable var = arg.getVariable();
            localize(var);
            remove(var);
        }
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
