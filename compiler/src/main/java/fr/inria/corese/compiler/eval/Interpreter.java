package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.kgram.core.Stack;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.ComputerProxy;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.api.TransformVisitor;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Filter Evaluator
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
    static ASTExtension extension;
    int mode = DEFAULT_MODE;
    boolean hasListener = false;
    boolean isDebug = false;
    public static int count = 0;
    IDatatype ERROR_VALUE = null;

    static {
        extension = createExtension();
    }

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

    /**
     * Evaluator for bind (exp as var)
     */
    @Override
    public Node eval(Filter f, Environment env, Producer p) throws EngineException {
        IDatatype value = eval(f.getExp(), env, p);
        if (value == ERROR_VALUE) {
            return null;
        }
        return producer.getNode(value);
    }

    /**
     * Extension: Functions that return several variables as result such as:
     * values (VAR+) { unnest(exp) }
     */
    @Override
    public Mappings eval(Filter f, Environment env, List<Node> nodes)
            throws EngineException {
        int n = 1;
        Expr exp = f.getExp();
        switch (exp.oper()) {

            case UNNEST:
                if (hasListener) {
                    listener.listen(exp);
                }
                if (exp.arity() == 2) {
                    // unnest(exp, 2)
                    IDatatype dt = eval(exp.getExp(1), env, getProducer());
                    if (dt == ERROR_VALUE) {
                        return new Mappings();
                    }
                    n = dt.intValue();
                }
                exp = exp.getExp(0);

            default:
                IDatatype res = eval(exp, env, getProducer());
                if (res == ERROR_VALUE) {
                    return new Mappings();
                }
                return getProducer().map(nodes, res, n);
        }
    }

    /**
     * Evaluator of filter exp
     */
    @Override
    public boolean test(Filter f, Environment env) throws EngineException {
        return test(f, env, getProducer());
    }

    @Override
    public boolean test(Filter f, Environment env, Producer p) throws EngineException {
        IDatatype value = eval(f.getExp(), env, p);
        if (value == ERROR_VALUE) {
            return false;
        }
        return isTrue(value);
    }

    // Integer to IDatatype to Node
    // for kgram internal use of java values
    // e.g. count(*) ...
    @Override
    public Node cast(Object obj, Environment env, Producer p) {
        IDatatype val = DatatypeMap.cast(obj);
        Node node = p.getNode(val);
        return node;
    }

    /**
     * Bridge to expression evaluation for SPARQL filter and LDScript expression
     * Expressions are defined in fr.inria.corese.sparql.triple.parser Each
     * expression implements function: eval(Computer e, Binding b, Environment
     * env, Producer p)
     */
    public IDatatype eval(Expr exp, Environment env, Producer p) throws EngineException {
        // evalWE clean the binding stack if an EngineException is thrown
        IDatatype dt = exp.evalWE(this, env.getBind(), env, p);
        if (env.getBind().isDebug()) {
            System.out.println("eval: " + exp + " = " + dt);
            System.out.println(env);
        }
//        if (dt == null) {
//            // Evaluation error, may be overloaded by visitor event @error function 
//            DatatypeValue res = env.getVisitor().error(env.getEval(), exp, EMPTY);
//            if (res != null) {
//                return (IDatatype) res;
//            }
//        }
        return dt;
    }

    boolean isTrue(IDatatype dt) {
        try {
            return dt.isTrue();
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }

    @Override
    public IDatatype function(Expr exp, Environment env, Producer p) throws EngineException {
        throw new EngineException("Undefined expression: " + exp.toString());
    }

    /**
     * function.isSystem() == true function contains nested query or exists use
     * case: exists { exists { } } the inner exists need outer exists BGP to be
     * bound // hence we need a fresh Memory to start
     */
    public Computer getComputer(Environment env, Producer p, Expr function) {
        InterpreterEval eval = getComputerEval(env, p, function);
        return eval.getComputer();
    }

    /**
     * context: ldscript function call require new Interpreter with new
     * Environment see sparql.triple.function.script.Extension use case:
     * function contains a sparql query such as let(select where) the function
     * must be executed with a fresh Environment initialized with the function
     * definition global query hint: the function has been compiled within a
     * public query q1 which may be different from the query q2 where public
     * function call occur
     */
    @Override
    public InterpreterEval getComputerEval(Environment env, Producer p, Expr function) {
        Query q = getQuery(env, function);
        Eval currentEval = getEval(env);
        InterpreterEval eval = new InterpreterEval(p, this, currentEval.getMatcher());
        eval.setSPARQLEngine(currentEval.getSPARQLEngine());
        eval.set(currentEval.getProvider());
        eval.init(q);
        eval.setVisitor(currentEval.getVisitor());
        eval.getMemory().setBind(env.getBind());
        eval.getMemory().setGraphNode(env.getGraphNode());
        return eval;
    }

    Query getQuery(Environment env, Expr function) {
        if (function.isPublic() && env.getQuery() != function.getPattern()) {
            // function is public and contains query or exists
            // use function definition global query 
            return (Query) function.getPattern();
        }
        return env.getQuery();
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
    @Override
    public IDatatype exist(Expr exp, Environment env, Producer p) throws EngineException {
        try {
            if (hasListener) {
                listener.listen(exp);
            }
            if (exp.arity() == 1) {
                // argument return a graph on which we evaluate the exists
                IDatatype res = eval(exp.getExp(0), env, p);
                if (res == ERROR_VALUE) {
                    return ERROR_VALUE;
                }
                if (p.isProducer((Node) res)) {
                    p = p.getProducer((Node) res, env);
                }
            }
            Query q = env.getQuery();
            Exp pat = q.getPattern(exp);
            Node gNode = env.getGraphNode();
            Eval currentEval = getEval(env);
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
                            if (testNewEval) {
                                map = currentEval.getSPARQLEngine().eval(gNode, qq, getMapping(env, qq), p);
                            } else {
                                Eval eval = createEval(currentEval, exp, env, p);
                                if (eval == null) {
                                    return null;
                                }
                                map = eval.subEval(qq, gNode, Stack.create(sub), 0);
                            }
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

    // draft test
    // @todo: it does not clean the report in case of failure of the rest of the pattern
    void report(Query q, Environment env, Mappings map) {
        if (q.getGlobalAST().hasMetadata(Metadata.REPORT) && !map.isEmpty()) {
            IDatatype dt = getCreateReport(env);
            IDatatype list = dt.get("exists");
            list.getList().add(DatatypeMap.newInstance(map.toString(true)));
            dt.set("exists", list);
        }
    }

    IDatatype getCreateReport(Environment env) {
        IDatatype dt = env.getReport();
        if (dt == null) {
            dt = DatatypeMap.newServiceReport();
            env.setReport(dt);
        }
        if (dt.get("exists") == null) {
            dt.set("exists", DatatypeMap.newList());
        }
        return dt;
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

    public static boolean isDefined(Expr exp) {
        return extension.isDefined(exp);
    }

    @Override
    public Function getDefine(Expr exp, Environment env) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            Function def = ext.get(exp);
            if (def != null) {
                return def;
            }
        }

        return extension.get(exp);
    }

    @Override
    public Function getDefine(String name) {
        return extension.get(name);
    }

    @Override
    public Function getDefineGenerate(Expr exp, Environment env, String name, int n)
            throws EngineException {
        Function fun = getDefine(env, name, n);
        if (fun == null) {
            fun = getDefine(exp, env, name, n);
        }
        return fun;
    }

    /**
     * exp = funcall(arg, arg) arg evaluates to name Generate extension function
     * for predefined function name rq:plus -> function rq:plus(x, y){
     * rq:plus(x, y) }
     */
    Function getDefine(Expr exp, Environment env, String name, int n) throws EngineException {
        if (Processor.getOper(name) == ExprType.UNDEF) {
            return null;
        }
        Query q = env.getQuery().getGlobalQuery();
        ASTQuery ast = getAST((Expression) exp, q);
        Function fun = ast.defExtension(name, name, n);
        q.defineFunction(fun);
        ASTExtension ext = Interpreter.getCreateExtension(q);
        ext.define(fun);
        return fun;
    }

    // use exp AST to compile exp
    // use case: uri() uses ast base
    ASTQuery getAST(Expression exp, Query q) {
        ASTQuery ast = exp.getAST();
        if (ast != null) {
            return ast.getGlobalAST();
        } else {
            return q.getAST();
        }
    }

    @Override
    public Function getDefine(Environment env, String name, int n) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            Function ee = ext.get(name, n);
            if (ee != null) {
                return ee;
            }
        }
        return extension.get(name, n);
    }

    @Override
    public Function getDefineMetadata(Environment env, String metadata, int n) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            Function ee = ext.getMetadata(metadata, n);
            if (ee != null) {
                return ee;
            }
        }
        return extension.getMetadata(metadata, n);
    }

    /**
     * Retrieve a method with name and type
     */
    @Override
    public Function getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] param) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            if (env.getQuery().isDebug()) {
                ext.setDebug(true);
            }
            Function ee = ext.getMethod(name, type, param);
            if (ee != null) {
                return ee;
            }
        }
        return extension.getMethod(name, type, param);
    }

    public static void define(Function exp) {
        extension.define(exp);
    }

    public static ASTExtension getExtension() {
        return extension;
    }

    public ProxyInterpreter getComputerPlugin() {
        //return proxy.getComputerPlugin();
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
