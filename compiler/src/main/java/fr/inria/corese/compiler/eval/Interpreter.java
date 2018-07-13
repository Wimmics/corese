package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Binder;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Bind;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.Stack;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.filter.Extension;
import fr.inria.corese.kgram.filter.Proxy;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.ComputerProxy;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.api.TransformVisitor;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    protected ProxyInterpreter proxy;
    Producer producer;
    Eval kgram;
    IDatatype TRUE, FALSE;
    ResultListener listener;
    static HashMap<String, Extension> extensions;
    static Extension extension;
    int mode = KGRAM_MODE;
    boolean hasListener = false;
    boolean isDebug = false;
    public static int count = 0;
    IDatatype ERROR_VALUE = null;

    static {
        extension = new Extension();
    }

    public Interpreter(Proxy p) {
        proxy = (ProxyInterpreter) p;
        if (p.getEvaluator() == null) {
            p.setEvaluator(this);
        }
        TRUE = proxy.getValue(true);
        FALSE = proxy.getValue(false);
    }

    @Override
    public void setProducer(Producer p) {
        producer = p;
    }

    @Override
    public void setKGRAM(Eval o) {
        kgram = o;
    }
    
    @Override
    public void setDebug(boolean b) {
        isDebug = b;
    }

    @Override
    public Eval getEval() {
        return kgram;
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
    public Node eval(Filter f, Environment env, Producer p) {
        Expr exp = f.getExp();
        IDatatype value = eval(exp, env, p);
        if (value == ERROR_VALUE) {
            return null;
        }
        return producer.getNode(value);
    }

    @Override
    public List<Node> evalList(Filter f, Environment env) {

        Expr exp = f.getExp();
        switch (exp.oper()) {

            default:
                Object value = eval(exp, env);
                if (value == ERROR_VALUE) {
                    return null;
                }
                List<Node> lNode = producer.toNodeList(value);
                return lNode;
        }
    }

    /**
     * Functions that return several variables as result such as: sql("select
     * from where") as (?x ?y)
     */
    @Override
    public Mappings eval(Filter f, Environment env, List<Node> nodes) {
        Expr exp = f.getExp();
        switch (exp.oper()) {

            case UNNEST:
                if (hasListener) {
                    listener.listen(exp);
                }
                // unnest(sql()) as ()
                exp = exp.getExp(0);

            default:
                Object res = eval(exp, env);
                if (res == ERROR_VALUE) {
                    return new Mappings();
                }
                return producer.map(nodes, res);
        }
    }

    @Override
    public boolean test(Filter f, Environment env) {
        return test(f, env, producer);
    }

    @Override
    public boolean test(Filter f, Environment env, Producer p) {
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

    public Object eval(Expr exp, Environment env) {
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

    @Override
    public IDatatype eval(Expr exp, Environment env, Producer p) {
        return ((Expression) exp).eval(this, (Binding) env.getBind(), env, p);
    }

    boolean isTrue(IDatatype dt) {
        try {
            return dt.isTrue();
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }


    @Override
    public IDatatype function(Expr exp, Environment env, Producer p) {
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
                return exist(exp, env, p);


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
    public IDatatype eval(Expr exp, Environment env, Producer p, IDatatype[] args) {
        switch (exp.oper()) {

            case UNDEF:
                return ((Expression) exp).eval(this, (Binding) env.getBind(), env, p, args);
            //return extension(exp, env, p, args);

            default:
                return proxy.eval(exp, env, p, args);
        }
    }

    IDatatype[] evalArguments(Expr exp, Environment env, Producer p, int start) {
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
     * filter exists { } exists statement is also used to embed LDScript nested
     * query in this case it is tagged as system
     */
    @Override
    public IDatatype exist(Expr exp, Environment env, Producer p) {
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
        Memory memory = null;

        // push env Bind stack into new memory
        if (env instanceof Memory) {
            memory = kgram.getMemory((Memory) env, pat);
        } else if (env instanceof Mapping) {
            memory = kgram.getMemory((Mapping) env, pat);
        } else {
            return null;
        }
        
        Eval eval = kgram.copy(memory, p, this);
        eval.setSubEval(true);
        Mappings map = null;

        if (exp.isSystem()) {
            // system generated for LDScript nested query 
            // e.g. for (?m in select where) {}
            // is compiled with internal system exists 
            // for (?m in exists {select where}){}
            Exp sub = pat.get(0).get(0);

            if (sub.isQuery()) {
                Query qq = sub.getQuery();
                qq.setFun(true);
                if (qq.isConstruct()) {
                    // let (?g =  construct where)
                    Mappings m = kgram.getSPARQLEngine().eval(qq, getMapping(env, qq), p);
                    return (IDatatype) producer.getValue(m.getGraph());
                }
                if (qq.getService() != null) {
                    // @service <uri> let (?m = select where)
                    Mappings m = kgram.getSPARQLEngine().eval(qq, getMapping(env, qq), p);
                    return (IDatatype) producer.getValue(m);
                } else {
                    // let (?m = select where)
                    map = eval.subEval(qq, gNode, Stack.create(sub), 0);
                    // PB: below, memory is initialized with outer query, not with qq
//                    eval.setSubEval(false);
//                    map = eval.query(gNode==null?null:qq.getGraphNode(gNode), gNode==null?null:env.getNode(gNode), qq);
                }
            } else {
                // never happen
                map = eval.subEval(q, gNode, Stack.create(pat), 0);
            }
        } else {
            // SPARQL exists {}
            eval.setLimit(1);
            map = eval.subEval(q, gNode, Stack.create(pat), 0);
        }

        boolean b = map.size() > 0;

        if (exp.isSystem()) {
            return (IDatatype) producer.getValue(map);
        } else {
            return proxy.getValue(b);
        }
    }

    Mapping getMapping(Environment env, Query q) {
        if (env.hasBind()) {
            Mapping map = getMapping(env.getBind(), q);
            map.setBind(env.getBind());
            return map;
        }
        return null;
    }

    /**
     * TODO: remove duplicates in getVariables()
     * use case:
     * function us:fun(?x){let (select ?x where {}) {}}
     * variable ?x appears twice in the stack because it is redefined in the let clause
     */
    Mapping getMapping(Binder b, Query q) {
        ArrayList<Node> lvar = new ArrayList();
        ArrayList<Node> lval = new ArrayList();
        for (Expr var : b.getVariables()) {
            Node node = q.getProperAndSubSelectNode(var.getLabel());
            if (node != null && !lvar.contains(node)) {
                lvar.add(node);
                lval.add(b.get(var));
            }
        }
        Mapping m = Mapping.create(lvar, lval);
        return m;
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

    private IDatatype focus(Expr exp, Environment env, Producer p) {
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
    @Override
    public Expr getDefine(Expr exp, Environment env, Producer p, int n) {
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

    public IDatatype eval(String name, Environment env, Producer p, IDatatype value) {
        Expr function = getDefine(env, name, (value == null) ? 0 : 1);
        if (function == null) {
            return ERROR_VALUE;
        }
        return eval(function, env, p, value);
    }

    public IDatatype eval(Expr function, Environment env, Producer p, IDatatype value) {
        if (value == null) {
            return call(function.getFunction(), env, p, proxy.createParam(0), function);
        }
        IDatatype[] values = proxy.createParam(1);
        values[0] = value;
        return call(function.getFunction(), env, p, values, function);
    }

    /**
     * Try to execute a method name in the namespace of the generalized datatype URI
     * http://ns.inria.fr/sparql-datatype/triple#display(?x)
     * URI:   dt:uri#name
     * bnode: dt:bnode#name
     * literal: dt:datatype#name or dt:literal#name
     */
    public IDatatype method(String name, IDatatype type, IDatatype[] param, Environment env, Producer p) {
        Expr exp = getDefineMethod(env, name, type, param);
        if (exp == null) {
            return null;
        } else {
            return call(exp.getFunction(), env, p, param, exp);
        }
    }

    /**
     * Extension function call
     */
    @Deprecated
    public IDatatype call(Expr exp, Environment env, Producer p, IDatatype[] values, Expr function) {
        Expr fun = function.getFunction();
        env.set(function, fun.getExpList(), values);
        if (isDebug || function.isDebug()) {
            System.out.println(exp);
            System.out.println(env.getBind());
        }
        IDatatype res;
        if (function.isSystem()) {
            // function contains nested query or exists
            // use fresh Memory for not to screw Bind & Memory
            // use case: exists { exists { } }
            // the inner exists need outer exists BGP to be bound
            // hence we need a fresh Memory to start
            Query q = env.getQuery();
            if (function.isPublic() && env.getQuery() != function.getPattern()) {
                // function is public and contains query or exists
                // use function definition global query in order to have Memory 
                // initialized with the right set of Nodes for the nested query
                q = (Query) function.getPattern();
            }
            res = funEval(function, q, env, p);
        } else {
            res = ((Expression) function.getBody()).eval(this, (Binding) env.getBind(), env, p);
        }
        env.unset(function, fun.getExpList());
        if (isDebug || function.isDebug()) {
            System.out.println(exp + " : " + res);
        }
        if (res == ERROR_VALUE) {
            return res;
        }
        // keep this:
        return proxy.getResultValue(res);
    }

    /**
     * function.isSystem() == true
     * function contains nested query or exists
     * use case: exists { exists { } }  the inner exists need outer exists
     * BGP to be bound // hence we need a fresh Memory to start
     */
    @Override
    public Interpreter getComputer(Environment env, Producer p, Expr function) {
        Query q = getQuery(env, function);
        Interpreter in = new Interpreter(proxy);
        in.setProducer(p);
        Eval eval = Eval.create(p, in, kgram.getMatcher());
        eval.setSPARQLEngine(kgram.getSPARQLEngine());
        eval.set(kgram.getProvider());
        eval.setVisitor(kgram.getVisitor());
        eval.init(q);
        eval.getMemory().setBind(env.getBind());
        return in;
    }

    Query getQuery(Environment env, Expr function) {
        if (function.isPublic() && env.getQuery() != function.getPattern()) {
            // function is public and contains query or exists
            // use function definition global query 
            return (Query) function.getPattern();
        }
        return env.getQuery();
    }

    @Override
    public Environment getEnvironment() {
        return kgram.getEnvironment();
    }

    /**
     * Eval a function in new kgram with function's query use case: function
     * with exists or nested query
     *
     * @param exp function ex:name() {}
     */
    @Deprecated
    IDatatype funEval(Expr exp, Query q, Environment env, Producer p) {
        //System.out.println("FunEval: " + exp.getFunction());
        Interpreter in = new Interpreter(proxy);
        in.setProducer(p);
        Eval eval = Eval.create(p, in, kgram.getMatcher());
        eval.setSPARQLEngine(kgram.getSPARQLEngine());
        eval.init(q);
        eval.getMemory().setBind(env.getBind());

        return in.eval(exp.getBody(), eval.getMemory(), p);
    }

    @Override
    public int compare(Environment env, Producer p, Node n1, Node n2) {
        return proxy.compare(env, p, n1, n2);
    }
  
    public static boolean isDefined(Expr exp) {
        return extension.isDefined(exp);
    }

    @Override
    public Expr getDefine(Expr exp, Environment env) {
        Extension ext = env.getExtension();
        if (ext != null) {
            Expr def = ext.get(exp);
            if (def != null) {
                return def;
            }
        }

        Expr def = extension.get(exp);
        if (def != null) {
            return def;
        }

        return null;
    }

    public Expr getDefine(Expr exp, Environment env, String name) {
        Extension ext = env.getExtension();
        if (ext != null) {
            Expr ee = ext.get(exp, name);
            if (ee != null) {
                return ee;
            }
        }
        return extension.get(exp, name);
    }

    @Override
    public Expr getDefine(String name) {
        return extension.get(name);
    }

    @Override
    public Expr getDefineGenerate(Expr exp, Environment env, String name, int n) {
        Expr fun = getDefine(env, name, n);
        if (fun == null) {
            fun = proxy.getDefine(exp, env, name, n);
        }
        return fun;
    }

    @Override
    public Expr getDefine(Environment env, String name, int n) {
        Extension ext = env.getExtension();
        if (ext != null) {
            Expr ee = ext.get(name, n);
            if (ee != null) {
                return ee;
            }
        }
        return extension.get(name, n);
    }
    
    @Override
    public Expr getDefineMetadata(Environment env, String metadata, int n) {
        Extension ext = env.getExtension();
        if (ext != null) {
            Expr ee = ext.getMetadata(metadata, n);
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
    public Expr getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] param) {
        Extension ext = env.getExtension();
        if (ext != null) {
            if (env.getQuery().isDebug()) {
                ext.setDebug(true);
            }
            Expr ee = ext.getMethod(name, type, param);
            if (ee != null) {
                return ee;
            }
        }
        return extension.getMethod(name, type, param);
    }

    public static void define(Expr exp) {
        extension.define(exp);
    }

    public static Extension getExtension() {
        return extension;
    }

    /**
     * use case:  Eval funcall LDScript function
     * 
     */
    @Override
    public IDatatype eval(Expr f, Environment e, Producer p, Object[] values) {
        return eval(f, e, p, (IDatatype[]) values);
    }

//    @Override
//    public IDatatype eval(Expr f, Environment e, Producer p, Object value) {
//        return eval(f, e, p, (IDatatype) value);
//    }

    @Override
    public Expr getDefineMethod(Environment env, String name, Object type, Object[] values) {
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
