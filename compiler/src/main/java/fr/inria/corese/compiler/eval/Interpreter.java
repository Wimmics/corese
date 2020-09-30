package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Binder;
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
import fr.inria.corese.kgram.filter.Proxy;
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
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.function.script.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.Level;

/**
 * A generic filter Evaluator Values are Java Object Target processing is
 * delegated to a proxy and a producer (for Node).
 *
 * @author Olivier Corby INRIA
 */
public class Interpreter implements Computer, Evaluator, ExprType {
    public static boolean testNewEval = true;
    private static Logger logger = LoggerFactory.getLogger(Interpreter.class);
    static final String MEMORY = Exp.KGRAM + "memory";
    static final String STACK = Exp.KGRAM + "stack";
    public static int DEFAULT_MODE = KGRAM_MODE;
    static final IDatatype[] EMPTY = new IDatatype[0];
    protected ProxyInterpreter proxy;
    Producer producer;
    Eval kgram;
    IDatatype TRUE, FALSE;
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

    public Interpreter(Proxy p) {
        proxy = (ProxyInterpreter) p;
        if (p.getEvaluator() == null) {
            p.setEvaluator(this);
        }
        TRUE = proxy.getValue(true);
        FALSE = proxy.getValue(false);
    }
    
    public static ASTExtension createExtension() {
        return new ASTExtension();
    }
    
    public static ASTExtension getCreateExtension(Query q) {
        ASTExtension ext = getExtension(q);
        if (ext == null) {
            ext = createExtension();
            q.setExtension(ext);
        }
        return ext;
    }
    
    public static ASTExtension getExtension(Environment env) {
        return (ASTExtension) env.getExtension();
    }
    
    public static ASTExtension getExtension(Query q) {
        return (ASTExtension) q.getExtension();
    }

    @Override
    public void setProducer(Producer p) {
        producer = p;
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
    public ProxyInterpreter getProxy() {
        return proxy;
    }

    public ProxyInterpreter getComputerProxy() {
        return proxy;
    }

    
    public ProxyInterpreter getComputerPlugin() {
        return proxy.getComputerPlugin();
    }

    public ComputerProxy getComputerTransform() {
        return proxy.getComputerTransform();
    }

    @Override
    public void addResultListener(ResultListener rl) {
        listener = rl;
        hasListener = rl != null;
    }

    @Override
    public Node eval(Filter f, Environment env, Producer p) throws EngineException {
        Expr exp = f.getExp();
        IDatatype value = eval(exp, env, p);
        if (value == ERROR_VALUE) {
            return null;
        }
        return producer.getNode(value);
    }

//    @Override
//    public List<Node> evalList(Filter f, Environment env) {
//
//        Expr exp = f.getExp();
//        switch (exp.oper()) {
//
//            default:
//                Object value = eval(exp, env);
//                if (value == ERROR_VALUE) {
//                    return null;
//                }
//                List<Node> lNode = producer.toNodeList(value);
//                return lNode;
//        }
//    }

    /**
     * Functions that return several variables as result such as: sql("select
     * from where") as (?x ?y)
     */
    @Override
    public Mappings eval(Filter f, Environment env, List<Node> nodes) throws EngineException {
        Expr exp = f.getExp();
        switch (exp.oper()) {

            case UNNEST:
                if (hasListener) {
                    listener.listen(exp);
                }
                // unnest(sql()) as ()
                exp = exp.getExp(0);

            default:
                IDatatype res = eval(exp, env);
                if (res == ERROR_VALUE) {
                    return new Mappings();
                }
                return producer.map(nodes, res);
        }
    }

    @Override
    public boolean test(Filter f, Environment env) throws EngineException {
        return test(f, env, producer);
    }

    @Override
    public boolean test(Filter f, Environment env, Producer p) throws EngineException {
        Expr exp = f.getExp();
        IDatatype value = eval(exp, env, p);
        if (value == ERROR_VALUE) {
            return false;
        }
        return isTrue(value);
    }

    Node getNode(Expr var, Environment env) {
        return env.getNode(var);
    }

    Object getValue(Expr var, Environment env) {
        Node node = env.getNode(var);
        if (node == null) {
            return null;
        }
        return node.getValue();
    }

    Object getValue(Node node) {
        //return producer.getNodeValue(node);
        return node.getValue();

    }

    public IDatatype eval(Expr exp, Environment env) throws EngineException {
        return eval(exp, env, producer);
    }

    // Integer to IDatatype to Node
    // for kgram internal use of java values
    // e.g. count(*) ...
    @Override
    public Node cast(Object obj, Environment env, Producer p) {
        IDatatype val = proxy.cast(obj, env, p);
        Node node = p.getNode(val);
        return node;
    }

    /**
     * Bridge to expression evaluation
     */
    //@Override
    public IDatatype eval(Expr exp, Environment env, Producer p) throws EngineException {
        if (env.getEval() == null) {
            logger.error("Environment getEval() = null in: ");
            logger.info(exp.toString());
        }
        Binding b = (Binding) env.getBind();
        IDatatype dt = ((Expression) exp).eval(this, b, env, p);
        if (b.isDebug()) {
            System.out.println("eval: " + exp + " = " + dt);
        }
        if (dt == null) {
            DatatypeValue res = env.getVisitor().error(env.getEval(), exp, EMPTY);
            if (res != null) {
                return (IDatatype) res;
            }
        }
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
        //System.out.println(exp + " " + exp.getClass().getName());
        switch (exp.oper()) {

//            case ERROR:
//                return null;

            case ENV:
                return DatatypeMap.createObject(env);

            case SKIP:
            case GROUPBY:
            case PACKAGE:
                return TRUE;

            case EXIST:
            {
                try {
                    return exist(exp, env, p);
                } catch (EngineException ex) {
                    java.util.logging.Logger.getLogger(Interpreter.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }



            case PWEIGHT: {
                Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
                if (qNode == null) {
                    return null;
                }
                int value = env.pathWeight(qNode);
                return proxy.getValue(value);
            }


            case STL_AND:
            case CUSTOM:
                // use function call below with param array 
                break;

            default:
                switch (exp.getExpList().size()) {

                    case 0:
                        return proxy.function(exp, env, p);

                    case 1:
                        IDatatype val = eval(exp.getExp(0), env, p);
                        if (val == ERROR_VALUE) {
                            return null;
                        }
                        return proxy.function(exp, env, p, val);

                    case 2:
                        IDatatype value1 = eval(exp.getExp(0), env, p);
                        if (value1 == ERROR_VALUE) {
                            return null;
                        }
                        IDatatype value2 = eval(exp.getExp(1), env, p);
                        if (value2 == ERROR_VALUE) {
                            return null;
                        }
                        return proxy.function(exp, env, p, value1, value2);
                }

        }

        IDatatype[] args = evalArguments(exp, env, p, 0);

        if (args == null) {
            return null;
        }

        return eval(exp, env, p, args);
    }

    // called by Eval for system functions (xt:produce()) and xt:main
    public IDatatype eval(Expr exp, Environment env, Producer p, IDatatype[] args) throws EngineException {
        switch (exp.oper()) {

            case UNDEF:
                return ((Expression) exp).eval(this, (Binding) env.getBind(), env, p, args);
            //return extension(exp, env, p, args);

            default:
                return proxy.eval(exp, env, p, args);
        }
    }

    IDatatype[] evalArguments(Expr exp, Environment env, Producer p, int start) throws EngineException {
        IDatatype[] args = new IDatatype[exp.arity() - start];
        int i = 0;
        for (int j = start; j < exp.arity(); j++) {
            args[i] = eval(exp.getExp(j), env, p);
            if (args[i] == ERROR_VALUE) {
                if (env.getQuery().isDebug()) {
                    logger.error("Error eval argument: " + exp.getExp(j) + " in: " + exp);
                }
                return null;
            }
            i++;
        }
        return args;
    }
    
     /**
     * function.isSystem() == true
     * function contains nested query or exists
     * use case: exists { exists { } }  the inner exists need outer exists
     * BGP to be bound // hence we need a fresh Memory to start
     */
    public Computer getComputer(Environment env, Producer p, Expr function) {
        InterpreterEval eval = getComputerEval(env, p, function);
        return eval.getComputer();
    }
    
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
                        return DatatypeMap.createObject((m.getGraph()==null)?p.getGraph():m.getGraph());
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
            //return (IDatatype) producer.getValue(map);
            return DatatypeMap.createObject(map);
        } else {
            return proxy.getValue(b);
        }
        }
        catch (SparqlException e) {
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
        }
        else {
            // share global variables and ProcessVisitor
            return Mapping.create(env.getBind());
        }
        //return null;
    }
  
    /**
     * exp : system(kg:memory)
     */
    Object system(Expr exp, Environment env) {
        if (exp.arity() > 0) {
            Expr arg = exp.getExp(0);
            if (arg.type() == CONSTANT) {
                String label = arg.getLabel();
                if (label.equals(MEMORY)) {
                    return env;
                } else if (label.equals(STACK)) {
                    return ((Memory) env).getStack();
                }
            }
        }
        return env;
    }

    @Override
    public void setMode(int m) {
        mode = m;
        proxy.setMode(m);
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
        proxy.start(producer, env);
    }

    @Override
    public void init(Environment env) {
        if (env.getBind() == null) {
            env.setBind(getBinder());
        }
    }

    @Override
    public Binder getBinder() {
        return Binding.create() ;
    }

    @Override
    public void finish(Environment env) {
        proxy.finish(producer, env);
    }

    private IDatatype focus(Expr exp, Environment env, Producer p) throws EngineException {
        if (exp.arity() < 2) {
            return ERROR_VALUE;
        }
        IDatatype res = eval(exp.getExp(0), env, p);
        if (res == ERROR_VALUE || !p.isProducer(res)) {
            return ERROR_VALUE;
        }
        Producer pp = p.getProducer((Node) res, env);
        return eval(exp.getExp(1), env, pp);
    }


    /**
     * exp is funcall(arg, arg) arg is an expression that evaluates to a
     * function name URI evaluate arg return function definition corresponding
     * to name with arity n.
     */
    //@Override
    public Expr getDefine(Expr exp, Environment env, Producer p, int n) throws EngineException {
        IDatatype name = eval(exp.getExp(0), env, p);
        if (name == ERROR_VALUE) {
            return null;
        }
        Expr def = getDefineGenerate(exp, env, name.stringValue(), n);
        if (def == null) {
            return null;
        }
        return def;
    }


  
    public static boolean isDefined(Expr exp) {
        return extension.isDefined(exp);
    }

    @Override
    public Function getDefine(Expr exp, Environment env) {
        ASTExtension ext = getExtension(env);
        if (ext != null) {
            Function def = ext.get(exp);
            if (def != null) {
                return def;
            }
        }

        return extension.get(exp);
    }

    public Function getDefine(Expr exp, Environment env, String name) {
        ASTExtension ext = getExtension(env);
        if (ext != null) {
            Function ee = ext.get(exp, name);
            if (ee != null) {
                return ee;
            }
        }
        return extension.get(exp, name);
    }

    @Override
    public Function getDefine(String name) {
        return extension.get(name);
    }

    @Override
    public Function getDefineGenerate(Expr exp, Environment env, String name, int n) 
            throws EngineException{
        Function fun = getDefine(env, name, n);
        if (fun == null) {
            fun = (Function) proxy.getDefine(exp, env, name, n);
        }
        return fun;
    }

    @Override
    public Function getDefine(Environment env, String name, int n) {
        ASTExtension ext = getExtension(env);
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
        ASTExtension ext = getExtension(env);
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
        ASTExtension ext = getExtension(env);
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

    /**
     * use case:  Eval funcall LDScript function
     * 
     */
    //@Override
    public IDatatype eval(Expr f, Environment e, Producer p, Object[] values) throws EngineException {
        return eval(f, e, p, (IDatatype[]) values);
    }

//    @Override
//    public IDatatype eval(Expr f, Environment e, Producer p, Object value) {
//        return eval(f, e, p, (IDatatype) value);
//    }

    @Override
    public Function getDefineMethod(Environment env, String name, Object type, Object[] values) {
        return getDefineMethod(env, name, (IDatatype) type, (IDatatype[]) values);
    }

    @Override
    public TransformProcessor getTransformer(Environment env, Producer p) {
        return getComputerTransform().getTransformer(env, p);
    }

    @Override
    public TransformProcessor getTransformer(Environment env, Producer p, Expr exp, IDatatype uri, IDatatype gname) {
        return getComputerTransform().getTransformer(env, p, exp, uri, gname);
    }

    @Override
    public TransformVisitor getVisitor(Environment env, Producer p) {
        return getComputerTransform().getVisitor(env, p);
    }
    
    @Override
    public GraphProcessor getGraphProcessor() {
        return getComputerPlugin().getGraphProcessor();
    }

    @Override
    public Context getContext(Environment env, Producer p) {
        return getComputerTransform().getContext(env, p);
    }

    @Override
    public NSManager getNSM(Environment env, Producer p) {
        return getComputerTransform().getNSM(env, p);
    }


}
