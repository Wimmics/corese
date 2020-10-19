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
import static fr.inria.corese.kgram.api.core.ExprType.LOAD;
import static fr.inria.corese.kgram.api.core.ExprType.MAP;
import static fr.inria.corese.kgram.api.core.ExprType.READ;
import static fr.inria.corese.kgram.api.core.ExprType.REDUCE;
import static fr.inria.corese.kgram.api.core.ExprType.WRITE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ENTAILMENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_HTTP_GET;
import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.TopExp;
import java.util.ArrayList;
import java.util.List;

/**
 * ASTQuery process(walker) walk through every Exp, Expression and Function
 * and call enter() leave() on each of them
 * This Walker check security issue and reports to Transformer in a Record List
 */
public class ASTWalker implements Walker {
    int countService = 0;
    ASTQuery ast;
    ArrayList<Record> list;
    boolean trace = false;
    
    public ASTWalker(ASTQuery ast) {
        this.ast = ast;
        trace = ast.hasMetadata(Metadata.TRACE);
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
        trace("Statement: " + exp);
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
     */
    
    @Override
    public void enter(Expression exp) {
        trace("Expression: " + exp);
        process(exp);
        processService(exp);
        if (exp.isExist()) {
            exp.getExist();
        }
    }

    @Override
    public void leave(Expression exp) {
        
    }
    
    
    
    // record unauthorized feature
    void record(Feature feature, String mess, TopExp exp) {
        Record rec = new Record(exp, mess);
        list.add(rec);
    }
    
    public List<Record> getRecord() {
        return list;
    }
    
    void check(Feature feature, Level level, String mess, TopExp exp) {
        if (Access.reject(feature, level)) {
            record(feature, mess, exp);
        }
    }
    
    /**
     * in service clause, funcall may be unauthorized if specific functions 
     * are unauthorized
     */
    void process(Expression exp) {
        switch (exp.oper()) {
            case READ:
            case LOAD:
            case XT_HTTP_GET:
                check(Feature.READ_WRITE, getLevel(), TermEval.READ_MESS, exp);
                break;
            case WRITE:
                check(Feature.READ_WRITE, getLevel(), TermEval.WRITE_MESS, exp);
                break;
            case JAVACALL:
            case DSCALL:
            case EXTERN:
                check(Feature.JAVA_FUNCTION, getLevel(), TermEval.JAVA_FUNCTION_MESS, exp);
                break;
            case XT_ENTAILMENT:
                check(Feature.LINKED_RULE, getLevel(), TermEval.LINKED_RULE_MESS, exp);
                break;
                
            case APPLY_TEMPLATES:
            case APPLY_TEMPLATES_WITH:
            case APPLY_TEMPLATES_WITH_GRAPH:
            case CALL_TEMPLATE:
            case CALL_TEMPLATE_WITH:
                check(Feature.LINKED_TRANSFORMATION, getLevel(), TermEval.LINKED_TRANSFORMATION_MESS, exp);
        }

    }
    
    
    void processService(Expression exp) {
        if (inService()) {
            switch (exp.oper()) {
                case FUNCALL:
                case APPLY:
                case REDUCE:
                case MAP:
                    if (Access.reject(Feature.READ_WRITE, getLevel())
                            || Access.reject(Feature.JAVA_FUNCTION, getLevel())) {
                        record(Feature.JAVA_FUNCTION, TermEval.JAVA_FUNCTION_MESS, exp);
                    }
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
