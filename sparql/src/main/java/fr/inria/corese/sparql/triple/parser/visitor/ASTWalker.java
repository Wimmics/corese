package fr.inria.corese.sparql.triple.parser.visitor;

import static fr.inria.corese.kgram.api.core.ExprType.APPLY;
import static fr.inria.corese.kgram.api.core.ExprType.APPLY_TEMPLATES;
import static fr.inria.corese.kgram.api.core.ExprType.APPLY_TEMPLATES_WITH;
import static fr.inria.corese.kgram.api.core.ExprType.APPLY_TEMPLATES_WITH_GRAPH;
import static fr.inria.corese.kgram.api.core.ExprType.CALL_TEMPLATE;
import static fr.inria.corese.kgram.api.core.ExprType.CALL_TEMPLATE_WITH;
import static fr.inria.corese.kgram.api.core.ExprType.DSCALL;
import static fr.inria.corese.kgram.api.core.ExprType.EXTERN;
import static fr.inria.corese.kgram.api.core.ExprType.FUNCALL;
import static fr.inria.corese.kgram.api.core.ExprType.JAVACALL;
import static fr.inria.corese.kgram.api.core.ExprType.KGRAM;
import static fr.inria.corese.kgram.api.core.ExprType.LOAD;
import static fr.inria.corese.kgram.api.core.ExprType.MAP;
import static fr.inria.corese.kgram.api.core.ExprType.READ;
import static fr.inria.corese.kgram.api.core.ExprType.REDUCE;
import static fr.inria.corese.kgram.api.core.ExprType.WRITE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ENTAILMENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_HTTP_GET;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.TopExp;
import java.util.ArrayList;
import java.util.List;

/**
 * ASTQuery process(walker) walk through every Exp, Expression and Function
 * and call enter() leave() on each of them
 * This Walker check security issue and reports to Transformer in a Record List
 * We are at compile time, hence before query execution
 */
public class ASTWalker implements Walker {
    int countService = 0;
    ASTQuery ast;
    ArrayList<Record> list;
    boolean trace = false;
    
    public ASTWalker(ASTQuery ast) {
        this.ast = ast;
        //trace = ast.hasMetadata(Metadata.TRACE);
        list = new ArrayList<>();
    }
    
    Level getLevel() {
        return ast.getLevel();
    }
    
    // global AST
    
    @Override
    public void start(ASTQuery ast) {
    }
    
    @Override
    public void finish(ASTQuery ast) {
    }
    
    // global AST and subquery AST
    
    @Override
    public void enter(ASTQuery ast) {
        trace("Walker enter ast");
    }
    
    @Override
    public void leave(ASTQuery ast) {
    }
    
    @Override
    public void enterSolutionModifier(ASTQuery ast) {
    }
    
    @Override
    public void leaveSolutionModifier(ASTQuery ast) {
    }
      
    
    // Function Definition
    
    @Override
    public void enter(Function fun) {
    }
    
    @Override
    public  void leave(Function fun) {
    }
    
    void trace(String str) {
        if (trace) {
            System.out.println(str);
        }
    }

    /**
     * Statement
     * BGP, Service, Optional, Union, Minus, Query, Triple, Filter, exists BGP, etc.
     * exists BGP appears here as instance of Exist, can be checked by isExist() 
     */
    
    @Override
    public void enter(Exp exp) {
        trace("Walker enter statement: " + exp);
        if (exp.isService()) {
            enter(exp.getService());
        }
    }

    @Override
    public void leave(Exp exp) {
        if (exp.isService()) {
            leave(exp.getService());
        }
    }

    
    /**
     * Filter expression
     * exists {} can be checked by isExist(), BGP can be retrieved by getExist()
     * walker enter here with exp = ExistFunction exists BGP  and then walk in BGP as BGP.walk(walker)
     * see triple.function.core.ExistFunction.walk()
     */
    
    @Override
    public void enter(Expression exp) {
        trace("Walker enter expression: " + exp);
        process(exp);
        processService(exp);       
    }

    @Override
    public void leave(Expression exp) {
        
    }
    
    
    
    // record unauthorized feature
    Record record(Feature feature, String mess, TopExp exp) {
        Record rec = new Record(exp, mess);
        list.add(rec);
        return rec;
    }
    
    public List<Record> getRecord() {
        return list;
    }
    
    void check(Feature feature, Level level, String mess, TopExp exp) {
        if (Access.reject(feature, level)) {
            record(feature, mess, exp);
        }
    }
    
    void check(Feature feature, Level level, String mess, TopExp exp, String uri) {
        if (Access.reject(feature, level, uri)) {
            Record rec = record(feature, mess, exp);
            rec.setUri(uri);
        }
    }
    
    /**
     * in service clause, funcall may be unauthorized if specific functions 
     * are unauthorized
     */
    void process(Expression exp) {
        switch (exp.oper()) {
            case READ:
                check(Feature.READ, getLevel(), TermEval.READ_MESS, exp);
                break;             
            case LOAD:
                check(Feature.READ_WRITE, getLevel(), TermEval.LOAD_MESS, exp);
                break;                
                                           
            case WRITE:
                check(Feature.READ_WRITE, getLevel(), TermEval.WRITE_MESS, exp);
                break;
                
            case XT_HTTP_GET:
                check(Feature.HTTP, getLevel(), TermEval.HTTP_MESS, exp);
                break;
                
            case JAVACALL:
            case DSCALL:
            case EXTERN:
                check(Feature.JAVA_FUNCTION, getLevel(), TermEval.JAVA_FUNCTION_MESS, exp);
                break;
            case XT_ENTAILMENT:
                checkWithArg(Feature.LINKED_RULE, TermEval.LINKED_RULE_MESS, exp);
                break;
            case KGRAM:
                check(Feature.LDSCRIPT_SPARQL, getLevel(), TermEval.SPARQL_MESS, exp);
                break;
                
                
            case APPLY_TEMPLATES:
            case CALL_TEMPLATE:
                check(Feature.LINKED_TRANSFORMATION, getLevel(), TermEval.LINKED_TRANSFORMATION_MESS, exp);
                break;
                
            case APPLY_TEMPLATES_WITH:
            case APPLY_TEMPLATES_WITH_GRAPH:
            case CALL_TEMPLATE_WITH:
                checkWithArg(Feature.LINKED_TRANSFORMATION, TermEval.LINKED_TRANSFORMATION_MESS, exp);
                
        }
    }
    
    void checkWithArg(Feature feature, String mes, Expression exp) {
        if (exp.arity() > 0 && exp.getArg(0).isConstant()) {
            // transformation URL available
            check(feature, getLevel(), mes, exp, getValue(exp, 0).getLabel());
        } else if (exp.arity() > 0 && inService()) {
            // st:apply-templates(?t) inside service clause
            // at compile time we do not know if ?t is authorized
            // lets challenge access level with Undefined URL
            check(feature, getLevel(), mes, exp, NSManager.UNDEF_URL);
        } else {
            // URL will be checked at runtime
            check(feature, getLevel(), mes, exp);
        }
    }
    
    IDatatype getValue(Expression exp, int n) {
        return exp.getArg(n).getConstant().getDatatypeValue();
    }
    
    
    void processService(Expression exp) {
        if (inService() && exp.isTerm()) {
            switch (exp.oper()) {
                case FUNCALL:
                case APPLY:
                case REDUCE:
                case MAP:
                    if (Access.reject(Feature.READ_WRITE, getLevel())
                            || Access.reject(Feature.JAVA_FUNCTION, getLevel())) {
                        //record(Feature.JAVA_FUNCTION, TermEval.JAVA_FUNCTION_MESS, exp);
                    }
                    break;
                
//                case UNDEF:
//                    // check LinkedFunction
//                    check(Feature.LINKED_FUNCTION, getLevel(), TermEval.UNDEFINED_EXPRESSION_MESS, exp);
            }
        }
    }
    
    boolean inService() {
        return countService > 0;
    }
    
    void enter(Service exp) {
        countService++;
        check(Feature.SPARQL_SERVICE, getLevel(), TermEval.SERVICE_MESS, exp);
    }
    
    void leave(Service exp) {
        countService--;
    }

}
