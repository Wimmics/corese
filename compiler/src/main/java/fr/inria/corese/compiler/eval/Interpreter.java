package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.ComputerProxy;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.api.TransformVisitor;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filter exists Evaluator
 *
 * @author Olivier Corby INRIA
 */
public class Interpreter implements Computer, Evaluator, ExprType {

    public static boolean testNewEval = false;
    private static Logger logger = LoggerFactory.getLogger(Interpreter.class);
    static final String MEMORY = Exp.KGRAM + "memory";
    static final String STACK = Exp.KGRAM + "stack";
    public static int DEFAULT_MODE = KGRAM_MODE;
    static final IDatatype[] EMPTY = new IDatatype[0];
    //protected ProxyInterpreter proxy;
    // fr.inria.corese.core.query.PluginImpl
    private ProxyInterpreter plugin;
    Producer producer;
    Eval kgram;
    ResultListener listener;
    int mode = DEFAULT_MODE;
    boolean hasListener = false;
    boolean isDebug = false;
    public static int count = 0;
    IDatatype ERROR_VALUE = null;


    public Interpreter() {
    }

    // for PluginImpl
    public void setPlugin(ProxyInterpreter plugin) {
        this.plugin = plugin;
        if (plugin.getEvaluator() == null) {
            plugin.setEvaluator(this);
        }
    }

    public static ASTExtension createExtension() {
        return new ASTExtension();
    }

    public static ASTExtension getCreateExtension(Query q) {
        ASTExtension ext = q.getExtension();;
        if (ext == null) {
            ext = createExtension();
            q.setExtension(ext);
        }
        return ext;
    }

    @Override
    public void setProducer(Producer p) {
        producer = p;
    }

    public Producer getProducer() {
        return producer;
    }

    @Override
    public void setKGRAM(Eval o) {
        //kgram = o;
    }

    @Override
    public void setDebug(boolean b) {
        isDebug = b;
    }

    Eval getEval(Environment env) {
        if (env.getEval() == null) {
            logger.warn("env.getEval() = null");
        }
        return env.getEval();
    }

    @Override
    public void addResultListener(ResultListener rl) {
        listener = rl;
        hasListener = rl != null;
    }
    
    @Override
    public Evaluator getEvaluator() {
        return this;
    }


//    Eval createEval(Eval currentEval, Expr exp, Environment env, Producer p) {
//        Exp pat = env.getQuery().getPattern(exp);
//        Memory memory = currentEval.createMemory(env, pat);
//        if (memory == null) {
//            return null;
//        }
//        // producer below must be original Producer, it is used for cast purpose
//        Eval eval = currentEval.copy(memory, p, exp.isSystem());
//        eval.setSubEval(true);
//        return eval;
//    }

    /**
     * filter exists { } exists statement is also used to embed LDScript nested
     * query in this case it is tagged as system
     */
//    @Override
//    public IDatatype exist(Expr exp, Environment env, Producer p) throws EngineException {
//        try {
//            if (hasListener) {
//                listener.listen(exp);
//            }
//            if (exp.arity() == 1) {
//                // argument return a graph on which we evaluate the exists
//                //IDatatype res = eval(exp.getExp(0), env, p);
//                IDatatype res = exp.getExp(0).evalWE(this, env.getBind(), env, p);
//                if (res == ERROR_VALUE) {
//                    return ERROR_VALUE;
//                }
//                if (p.isProducer((Node) res)) {
//                    p = p.getProducer((Node) res, env);
//                }
//            }
//            Query q = env.getQuery();
//            Exp pat = q.getPattern(exp);
//            Node gNode = env.getGraphNode();
//            Eval currentEval = getEval(env);
//            Mappings map = null;
//
//            // in case of // evaluation of a pattern
//            synchronized (exp) {
//                if (exp.isSystem()) {
//                    // system generated for LDScript nested query 
//                    // e.g. for (?m in select where) {}
//                    // is compiled with internal system exists 
//                    // for (?m in exists {select where}){}
//                    Exp sub = pat.get(0).get(0);
//
//                    if (sub.isQuery()) {
//                        Query qq = sub.getQuery();
//                        qq.setFun(true);
//                        if (qq.isConstruct() || qq.isUpdate()) {
//                            Mappings m = currentEval.getSPARQLEngine().eval(gNode, qq, getMapping(env, qq), p);
//                            return DatatypeMap.createObject((m.getGraph() == null) ? p.getGraph() : m.getGraph());
//                        }
//                        if (qq.getService() != null) {
//                            // @federate <uri> let (?m = select where)
//                            Mappings m = currentEval.getSPARQLEngine().eval(qq, getMapping(env, qq), p);
//                            return DatatypeMap.createObject(m);
//                        } else {
//                            // let (?m = select where)
//                            if (testNewEval) {
//                                map = currentEval.getSPARQLEngine().eval(gNode, qq, getMapping(env, qq), p);
//                            } else {
//                                Eval eval = createEval(currentEval, exp, env, p);
//                                if (eval == null) {
//                                    return null;
//                                }
//                                map = eval.subEval(qq, gNode, Stack.create(sub), 0);
//                            }
//                        }
//                    } else {
//                        // never happen
//                        return null;
//                    }
//                } else {
//                    // SPARQL exists {}               
//                    Eval eval = createEval(currentEval, exp, env, p);
//                    if (eval == null) {
//                        return null;
//                    }
//                    eval.setLimit(1);
//                    map = eval.subEval(q, gNode, Stack.create(pat), 0);
//                }
//            }
//
//            boolean b = map.size() > 0;
//
//            if (exp.isSystem()) {
//                return DatatypeMap.createObject(map);
//            } else {
//                //report(q, env, map);
//                return DatatypeMap.newInstance(b);
//            }
//        } catch (SparqlException e) {
//            throw EngineException.cast(e);
//        }
//    }

    /**
     * Create a mapping with var = val coming from Bind stack
     */
//    Mapping getMapping(Environment env, Query q) {
//        if (env.hasBind()) {
//            // share variables
//            Mapping map = Mapping.create(q, env.getBind());
//            // share global variables and ProcessVisitor
//            map.setBind(env.getBind());
//            return map;
//        } else {
//            // share global variables and ProcessVisitor
//            return Mapping.create(env.getBind());
//        }
//    }

    @Override
    public void setMode(int m) {
        mode = m;
        //proxy.setMode(m);
        getPlugin().setMode(m);
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public boolean isCompliant() {
        return mode == SPARQL_MODE;
    }

    @Override
    public void start(Environment env) {
        //proxy.start(producer, env);
    }

    @Override
    public void init(Environment env) {
        if (env.getBind() == null) {
            env.setBind(getBinder());
        }
    }

    @Override
    public Binding getBinder() {
        return Binding.create();
    }

    @Override
    public void finish(Environment env) {
        getPlugin().finish(producer, env);
    }

 

    public ProxyInterpreter getComputerPlugin() {
        return getPlugin();
    }

    @Override
    public GraphProcessor getGraphProcessor() {
        return getComputerPlugin().getGraphProcessor();
    }

    public ComputerProxy getComputerTransform() {
        return getComputerPlugin().getComputerTransform();
    }

    @Override
    public TransformProcessor getTransformer(Binding b, Environment env, Producer p) throws EngineException {
        return getComputerTransform().getTransformer(b, env, p);
    }

    @Override
    public TransformProcessor getTransformer(Binding b, Environment env, Producer p, Expr exp, IDatatype uri, IDatatype gname) throws EngineException {
        return getComputerTransform().getTransformer(b, env, p, exp, uri, gname);
    }

    @Override
    public TransformVisitor getVisitor(Binding b, Environment env, Producer p) {
        return getComputerTransform().getVisitor(b, env, p);
    }

    @Override
    public Context getContext(Binding b, Environment env, Producer p) {
        return getComputerTransform().getContext(b, env, p);
    }

    @Override
    public NSManager getNSM(Binding b, Environment env, Producer p) {
        return getComputerTransform().getNSM(b, env, p);
    }

    public ProxyInterpreter getPlugin() {
        return plugin;
    }

}
