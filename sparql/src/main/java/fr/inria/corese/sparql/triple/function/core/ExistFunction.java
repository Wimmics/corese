package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.kgram.core.Stack;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Expression;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ExistFunction extends TermEval {  
    
    public ExistFunction() {}
    
    public ExistFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        if (isSystem()) {
            // LDScript subquery
            check(Feature.LDSCRIPT_SPARQL, b, SPARQL_MESS);
        }
        try {
            //return eval.exist(this, env, p);
            return exist(this, eval, env, p);
        } catch (EngineException ex) {
            log(ex.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isTermExist() {
        return true;
    }
    
     @Override
    public Expression prepare(ASTQuery ast) throws EngineException {
        if (isSystem()) {
            ast.setLDScript(true);
            ast.getGlobalAST().setLDScript(true);
        }
        return super.prepare(ast);
    }
    
    @Override
    public void walk(Walker walker) {
        walker.enter(this);
        getExist().walk(walker);
        walker.leave(this);
    }
    
    
    
    Eval createEval(Eval currentEval, Expr exp, Environment env, Producer p) {
        Exp pat = env.getQuery().getPattern(exp);
        Memory memory = currentEval.createMemory(env, pat);
        if (memory == null) {
            return null;
        }
        // producer below must be original Producer, it is used for cast purpose
        Eval eval = currentEval.copy(memory, p, exp.isSystem());
        eval.setSubEval(true);
        return eval;
    }

    /**
     * filter exists { } exists statement is also used to embed LDScript nested
     * query in this case it is tagged as system
     */
    public IDatatype exist(Expr exp, Computer evaluator, Environment env, Producer p) throws EngineException {
        try {
            Eval currentEval = env.getEval();
            if (currentEval.getListener()!=null) {
                currentEval.getListener().listen(exp);
            }
            if (exp.arity() == 1) {
                // argument return a graph on which we evaluate the exists
                //IDatatype res = eval(exp.getExp(0), env, p);
                IDatatype res = exp.getExp(0).evalWE(evaluator, env.getBind(), env, p);
                if (res == null) {
                    return null;
                }
                if (p.isProducer((Node) res)) {
                    p = p.getProducer((Node) res, env);
                }
            }
            Query q = env.getQuery();
            Exp pat = q.getPattern(exp);
            Node gNode = env.getGraphNode();
            Mappings map = null;

            // in case of // evaluation of a pattern
            synchronized (exp) {
                if (exp.isSystem()) {
                    // system generated for LDScript nested query 
                    // e.g. for (?m in select where) {}
                    // is compiled with internal system exists 
                    // for (?m in exists {select where}){}
                    Exp sub = pat.get(0).get(0);

                    if (sub.isQuery()) {
                        Query qq = sub.getQuery();
                        qq.setFun(true);
                        if (qq.isConstruct() || qq.isUpdate()) {
                            Mappings m = currentEval.getSPARQLEngine().eval(gNode, qq, getMapping(env, qq), p);
                            return DatatypeMap.createObject((m.getGraph() == null) ? p.getGraph() : m.getGraph());
                        }
                        if (qq.getService() != null) {
                            // @federate <uri> let (?m = select where)
                            Mappings m = currentEval.getSPARQLEngine().eval(qq, getMapping(env, qq), p);
                            return DatatypeMap.createObject(m);
                        } else {
                            // let (?m = select where)
                            Eval eval = createEval(currentEval, exp, env, p);
                            if (eval == null) {
                                return null;
                            }
                            map = eval.subEval(qq, gNode, Stack.create(sub), 0);
                        }
                    } else {
                        // never happen
                        return null;
                    }
                } else {
                    // SPARQL exists {}               
                    Eval eval = createEval(currentEval, exp, env, p);
                    if (eval == null) {
                        return null;
                    }
                    eval.setLimit(1);
                    map = eval.subEval(q, gNode, Stack.create(pat), 0);
                }
            }

            boolean b = map.size() > 0;

            if (exp.isSystem()) {
                return DatatypeMap.createObject(map);
            } else {
                //report(q, env, map);
                return DatatypeMap.newInstance(b);
            }
        } catch (SparqlException e) {
            throw EngineException.cast(e);
        }
    }

    /**
     * Create a mapping with var = val coming from Bind stack
     */
    Mapping getMapping(Environment env, Query q) {
        if (env.hasBind()) {
            // share variables
            Mapping map = Mapping.create(q, env.getBind());
            // share global variables and ProcessVisitor
            map.setBind(env.getBind());
            return map;
        } else {
            // share global variables and ProcessVisitor
            return Mapping.create(env.getBind());
        }
    }
   
}
