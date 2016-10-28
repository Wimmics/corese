package fr.inria.edelweiss.kgram.filter;

import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import java.util.List;

import org.apache.logging.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Stack;
import fr.inria.edelweiss.kgram.event.ResultListener;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;

/**
 * A generic filter Evaluator Values are Java Object Target processing is
 * delegated to a proxy and a producer (for Node)
 *
 * @author Olivier Corby INRIA
 *
 */
public class Interpreter implements Evaluator, ExprType {

    private static Logger logger = LogManager.getLogger(Interpreter.class);
    static final String MEMORY = Exp.KGRAM + "memory";
    static final String STACK = Exp.KGRAM + "stack";
    protected Proxy proxy;
    Producer producer;
    Eval kgram;
    DatatypeValue TRUE, FALSE;
    ResultListener listener;
    static HashMap<String, Extension> extensions ;
    static Extension extension;
    int mode = KGRAM_MODE;
    boolean hasListener = false;
    boolean isDebug = false;
    public static int count = 0;
    Object ERROR_VALUE = null;
    
    static {
        extension = new Extension();
    }

    public Interpreter(Proxy p) {
        proxy = p;
        if (p.getEvaluator() == null) {
            p.setEvaluator(this);
        }
        TRUE  = (DatatypeValue) proxy.getValue(true);
        FALSE = (DatatypeValue) proxy.getValue(false);        
    }

    @Override
    public void setProducer(Producer p) {
        producer = p;
    }

    @Override
    public void setKGRAM(Object o) {
        if (o instanceof Eval) {
            kgram = (Eval) o;
        }
    }
    
    @Override
    public void setDebug(boolean b){
        isDebug = b;
    }
    
    @Override
    public Eval getEval(){
        return kgram;
    }

    @Override
    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public void addResultListener(ResultListener rl) {
        listener = rl;
        hasListener = rl != null;
    }

    @Override
    public Node eval(Filter f, Environment env, Producer p) {
        Expr exp = f.getExp();
        Object value = eval(exp, env, p);
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
        Object value = eval(exp, env, p);
        if (value == ERROR_VALUE) {
            return false;
        }
        return proxy.isTrue(value);
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
    public Node cast(Object obj, Environment env, Producer p){
        Object val = proxy.cast(obj, env, p);
        Node node = p.getNode(val);
        return node;
    }

    @Override
    public Object eval(Expr exp, Environment env, Producer p) {
        //System.out.println("Interpret: " + exp + " " + env.getClass().getName());
        switch (exp.type()) {

            case CONSTANT:
                return exp.getDatatypeValue(); 

            case VARIABLE:
                Node node = env.getNode(exp);
                if (node == null) {
                    return null;
                }
                return node.getDatatypeValue();

            case BOOLEAN:
                return connector(exp, env, p);
            case TERM:
                return term(exp, env, p);
            case FUNCTION:
                return function(exp, env, p);
        }
        return null;
    }

    private DatatypeValue connector(Expr exp, Environment env, Producer p) {
        switch (exp.oper()) {
            case AND:
                return and(exp, env, p);
            case OR:
                return or(exp, env, p);
            case NOT:
                return not(exp, env, p);
        }
        return null;
    }

    private DatatypeValue not(Expr exp, Environment env, Producer p) {
        Object o = eval(exp.getExp(0), env, p);
        if (o == ERROR_VALUE || !proxy.isTrueAble(o)) {
            return null;
        }
        if (proxy.isTrue(o)) {
            return FALSE;
        } else {
            return TRUE;
        }
    }

    private DatatypeValue or(Expr exp, Environment env, Producer p) {
        boolean error = false;
        for (Expr arg : exp.getExpList()) {
            Object o = eval(arg, env, p);
            if (o == ERROR_VALUE || !proxy.isTrueAble(o)) {
                    error = true;
            } 
            else if (proxy.isTrue(o)) {
                    return TRUE;
            }           
        }
        if (error) {
            return null;
        }
        return FALSE;
    }
    

    private DatatypeValue and(Expr exp, Environment env, Producer p) {
        boolean error = false;
        for (Expr arg : exp.getExpList()) {
            Object o = eval(arg, env, p);
            if (o == ERROR_VALUE || !proxy.isTrueAble(o)) {
                error = true;
            }
            else if (! proxy.isTrue(o)) {
                return FALSE;
            }
        }
        if (error){
            return null;
        }
        return TRUE;
    }

    Object function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {
            
            case ERROR:
                return null;

            case ENV:
                return env;

            case SKIP:
            case GROUPBY:
            case STL_DEFINE:
            case PACKAGE:
            case FUNCTION:
                return TRUE;

            case BOUND:
                Node node = env.getNode(exp.getExp(0));
                if (node == null) {
                    return FALSE;
                }
                return TRUE;

            case COALESCE:
                for (Expr arg : exp.getExpList()) {
                    Object o = eval(arg, env, p);
                    if (o != null) {
                        return o;
                    }
                }
                return null;
                
            case SEQUENCE:
                return sequence(exp, env, p);
                
            case FOR:
                return proxy.function(exp, env, p);
                
            case LET:
                return let(exp, env, p);
                
            case SET:
                return set(exp, env, p);

            case EXIST:
                return exist(exp, env, p);

            case IF:
                return ifthenelse(exp, env, p);

            case LENGTH: {
                Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
                if (qNode == null) {
                    return null;
                }
                int value = env.pathLength(qNode);
                return proxy.getValue(value);
            }

            case PWEIGHT: {
                Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
                if (qNode == null) {
                    return null;
                }
                int value = env.pathWeight(qNode);
                return proxy.getValue(value);
            }

            case COUNT:
            case MIN:
            case MAX:
            case SUM:
            case AVG:
            case SAMPLE:
            case GROUPCONCAT:
            case STL_GROUPCONCAT:
            case AGGAND:
            case AGGLIST:
            case STL_AGGREGATE:
            case AGGREGATE:
                return aggregate(exp, env, p);

            case SYSTEM:
                return system(exp, env);


            case SELF:
                return eval(exp.getExp(0), env, p);

            case CONCAT:
            case STL_CONCAT:
            //case XT_CONCAT:
                return proxy.function(exp, env, p);

            case STL_AND:
            case EXTERNAL:
            case CUSTOM:
            case UNDEF:
            case STL_PROCESS:
            case LIST:
            case IOTA:           
                break;
                
            case FUNCALL:
                return funcall(exp, env, p);
                
            case MAP:
            case MAPLIST:
            case MAPMERGE:
            case MAPAPPEND:
            case MAPFIND:
            case MAPFINDLIST:
            case MAPEVERY:
            case MAPANY:
            case APPLY:
                // map(xt:fun(?a, ?b), ?x, ?list)
                
                Object[] args = evalArguments(exp, env, p, 1);
                if (args == ERROR_VALUE) {
                    return null;
                }
                return proxy.eval(exp, env, p, args);
                          
            default:                
                switch (exp.getExpList().size()) {

                    case 0:
                        return proxy.function(exp, env, p);

                    case 1:
                        Object val = eval(exp.getExp(0), env, p);
                        if (val == ERROR_VALUE) {
                            return null;
                        }
                        return proxy.function(exp, env, p, val);

                    case 2:
                        Object value1 = eval(exp.getExp(0), env, p);
                        if (value1 == ERROR_VALUE) {
                            return null;
                        }
                        Object value2 = eval(exp.getExp(1), env, p);
                        if (value2 == ERROR_VALUE) {
                            return null;
                        }
                        return proxy.function(exp, env, p, value1, value2);
                }

        }

        Object[] args = evalArguments(exp, env, p, 0);
        if (args == ERROR_VALUE) {
            switch (exp.oper()){
                case UNDEF:
                    logger.error("Error eval arguments: " + exp.getExpList());
            }
            return null;
        }
        
        return eval(exp, env, p, args);
    }
    
    @Override
    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {
        switch (exp.oper()) {

            case UNDEF:
                return extension(exp, env, p, args);
                
            default:
                return proxy.eval(exp, env, p, args);
        }
    }

    /**
     * use case: exp: max(?count) iterate all values of ?count to get the max
     */
    Object aggregate(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {

            case COUNT:
            case AGGREGATE:
                if (exp.arity() == 0) {
                    return proxy.aggregate(exp, env, p, null);
                }

            default:
                if (exp.arity() == 0) {
                    return null;
                }

                Node qNode = null;

                if (exp.getExp(0).isVariable()) {
                    qNode = env.getQueryNode(exp.getExp(0).getLabel());
                }

                return proxy.aggregate(exp, env, p, qNode);
        }

    }

    Object[] evalArguments(Expr exp, Environment env, Producer p, int start) {
        Object[] args = proxy.createParam(exp.arity() - start);
        int i = 0;
        for (int j = start; j<exp.arity(); j++) {
            Expr arg = exp.getExp(j);
            Object o = eval(arg, env, p);
            if (o == ERROR_VALUE) {
                return null;
            }
            args[i++] = o;
        }
        return args;
    }

    Object term(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {

            case IN:
                return in(exp, env, p);
        }

        Object o1 = eval(exp.getExp(0), env, p);
        if (o1 == ERROR_VALUE) {
            return null;
        }

        Object o2 = eval(exp.getExp(1), env, p);
        if (o2 == ERROR_VALUE) {
            return null;
        }

        Object res = proxy.term(exp, env, p, o1, o2);
        return res;
    }

    DatatypeValue in(Expr exp, Environment env, Producer p) {
        Object o1 = eval(exp.getExp(0), env, p);
        if (o1 == ERROR_VALUE) {
            return null;
        }

        boolean error = false;
        Expr list = exp.getExp(1);

        for (Expr arg : list.getExpList()) {
            Object o2 = eval(arg, env, p);
            if (o2 == ERROR_VALUE) {
                error = true;
            } else {
                Object res = proxy.term(exp, env, p, o1, o2);
                if (res == ERROR_VALUE) {
                    error = true;
                } else if (proxy.isTrue(res)) {
                    return TRUE;
                }
            }
        }
        if (error) {
            return null;
        }
        return FALSE;
    }

    /**
     *
     * filter(! exists {PAT})
     */
    Object exist(Expr exp, Environment env, Producer p) {
        if (hasListener) {
            listener.listen(exp);
        }
        
        Query q = env.getQuery();
        Exp pat = q.getPattern(exp);
        Node gNode = env.getGraphNode();
        Memory memory = null;
        
        if (env instanceof Memory) {
            memory = kgram.getMemory((Memory) env, pat);
        } 
        else if (env instanceof Mapping) {
            memory = kgram.getMemory((Mapping) env, pat);
        }
        else {
            return null;
        }
        
        Eval eval = kgram.copy(memory, p, this);
        eval.setSubEval(true);
                   
        Mappings map = null;
        
        if (exp.isSystem()) {
            // system generated exists:
            // for (?m in exists {select where}){}
            Exp sub = pat.get(0).get(0);

            if (sub.isQuery()) {
                Query qq = sub.getQuery();
                qq.setFun(true);
                if (qq.isConstruct()) {
                    // let (?g =  construct where)
                    Mappings m = kgram.getSPARQLEngine().eval(qq, getMapping(env, qq));                     
                    return producer.getValue(m.getGraph());
                } 
                if (qq.getService() != null){
                    // @service <uri> let (?m = select where)
                    Mappings m = kgram.getSPARQLEngine().eval(qq, getMapping(env, qq));                     
                    return producer.getValue(m);
                }
                else {
                    // let (?m = select where)
                    map = eval.subEval(qq, gNode, Stack.create(sub), 0);
                }
            }
            else {
                // never happen
                map = eval.subEval(q, gNode, Stack.create(pat), 0);
            }
        } 
        else {
            // exists {}
            eval.setLimit(1);
            map = eval.subEval(q, gNode, Stack.create(pat), 0);
        }
        
        boolean b = map.size() > 0;
        
        if (exp.isSystem()){
            return producer.getValue(map);
        }
        else {
            return proxy.getValue(b);
        }
    }
    
    Mapping getMapping(Environment env, Query q){
        if (env.hasBind()){
             return env.getBind().getMapping(q);
        }
       return  null;
    }
    

    Object ifthenelse(Expr exp, Environment env, Producer p) {
        Object test = eval(exp.getExp(0), env, p);
        Object value = null;
        if (test == ERROR_VALUE) {
            return null;
        }
        if (proxy.isTrue(test)) {
            value = eval(exp.getExp(1), env, p);
        } else if (exp.arity() == 3) {
            value = eval(exp.getExp(2), env, p);
        }
        return value;
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
    public void start(Environment env) {
        proxy.start(producer, env);
    }
 
    @Override
    public void finish(Environment env) {
        proxy.finish(producer, env);
    }
    
    private Object sequence(Expr exp, Environment env, Producer p) {
        Object res = TRUE;
        for (Expr e : exp.getExpList()){
            res = eval(e, env, p);
            if (res == ERROR_VALUE){
                return ERROR_VALUE;
            }
        }
        return res;
    }


    /**
     * let (?x = ?y, exp) 
     */
    private Object let(Expr exp, Environment env, Producer p) {
       // Node val  = eval(exp.getDefinition().getFilter(), env, p); 
        Node val  = (Node) eval(exp.getDefinition(), env, p); 
        if (val == ERROR_VALUE){
            return null;
        }
        return let(exp.getBody(), env, p, exp, exp.getVariable(), val);
    }
    
    /**
     * set(?x, ?x + 1)   
     */
    private Object set(Expr exp, Environment env, Producer p) {
        Object val  = eval(exp.getExp(1), env, p);
        if (val == ERROR_VALUE){
            return null;
        }
        env.bind(exp, exp.getExp(0), (Node) val);
        return val;
    }
    
    /**
     * PRAGMA: let ((?y) = select where)
     * if ?y is not bound, let do not bind ?y 
     */
     private Object let(Expr exp, Environment env, Producer p, Expr let, Expr var, Node val) { 
        boolean bound =  proxy.getConstantValue(val) != null;
        if (bound){
            env.set(let, var, val);
        }
        Object res = eval(exp, env, p);
        if (bound){
            env.unset(let, var);
        }
        return res;
    }
    
    /**
     * Extension manage extension functions
     * Their parameters are tagged as local variables, managed in a specific stack
     */    
    public Object extension(Expr exp, Environment env, Producer p, Object[] values){ 
        Expr def = getDefine(exp, env);       
        if (def == null){
            logger.error("Undefined function: " + exp);
            return ERROR_VALUE;
        }
        return eval(exp, env, p, values, def);
    }
    
     public Object funcall(Expr exp, Environment env, Producer p){
        Object[] args = evalArguments(exp, env, p, 1);
        if (args == null){
            return ERROR_VALUE;
        }
        Expr def = getDefine(exp.getExp(0), env, p, args.length);
        if (def == null){
            return ERROR_VALUE;
        }
        return eval(exp, env, p, args, def);
    }
     
     /**
      * exp is an expression that evaluates to a function name URI
      * evaluate exp
      * return function definition corresponding to name with arity n.
      */
    @Override
     public Expr getDefine(Expr exp, Environment env, Producer p, int n){
         Object name = eval(exp, env, p);
         if (name == ERROR_VALUE){
            return null;
        }
        Expr def = getDefine(env, p.getDatatypeValue(name).stringValue(), n);
        if (def == null){
            return null;
        } 
        return def;
     }
    
    /**
     * name is the name of a proxy function that overloads the function of exp
     * use case: overload operator for extended datatypes
     * name = http://example.org/datatype/equal
     */
    @Override
     public Object eval(Expr exp, Environment env, Producer p, Object[] values, String name){ 
        Expr def = getDefine(exp, env, name);
        if (def == null){
            return null;
        }
        return eval(exp, env, p, values, def);
    }
        
    /**
     * Extension function call  
     */
    @Override
    public Object eval(Expr exp, Environment env, Producer p, Object[] values, Expr def){   
        //count++;
        Expr fun = def.getFunction(); //getExp(0);
        env.set(def, fun.getExpList(), values);
        if (isDebug || def.isDebug()){
            System.out.println(exp);
            System.out.println(env.getBind());
        }
        Object res;
        // TODO: check also getGlobalQuery()
        if (def.isSystem() && def.isPublic() && env.getQuery() != def.getPattern()){
            // function is export and has exists {}
            // use function query
            res = funEval(def, env, p); 
        }
        else {
            res = eval(def.getBody(), env, p); 
        }
        env.unset(def, fun.getExpList()); 
        if (isDebug || def.isDebug()){
            System.out.println(exp + " : " + res);
        }
        return res;
    }
    
    /**
     * Eval a function in new kgram with function's query
     * use case: export function with exists {}
     * @param exp function ex:name() {}
     */
    Object funEval(Expr exp, Environment env, Producer p){
        Interpreter in = new Interpreter(proxy);
        in.setProducer(p);
        Eval eval = Eval.create(p, in, kgram.getMatcher());
        eval.setSPARQLEngine(kgram.getSPARQLEngine());
        eval.init((Query) exp.getPattern());
        eval.getMemory().setBind(env.getBind());
        return in.eval(exp.getBody(), eval.getMemory(), p);
    }
    
    @Override
    public int compare(Environment env, Producer p, Node n1, Node n2){
        return proxy.compare(env, p, n1, n2);
    }
    
    /**
     * Use case: st:process() overloaded by an extension function   
     */
    @Override
      public Object eval(Expr exp, Environment env, Producer p, Object[] values, Extension ext ){
        Expr def = ext.get(exp, values);
        if (def == null){
            return null;
        }
        return eval(exp, env, p, values, def);
     }
    
    
    public static boolean isDefined(Expr exp){       
        return extension.isDefined(exp);
    }
    
    public Expr getDefine(Expr exp, Environment env) {
//        Expr ee = exp.getDefine();
//        if (ee != null) {
//            return ee;
//        }
        Extension ext = env.getExtension();
        if (ext != null) {          
            Expr def = ext.get(exp);
            if (def != null) {
                //exp.setDefine(def);
                return def;
            }
        }
       
        Expr def = extension.get(exp);
        if (def != null) {
            //exp.setDefine(def);
            return def;
        }
 
        return null;
    }
    
     public Expr getDefine(Expr exp, Environment env, String name) {        
        Extension ext = env.getExtension();
        if (ext != null) {
           Expr ee = ext.get(exp, name);
           if (ee != null){
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
     public Expr getDefine(Environment env, String name, int n){
        Extension ext = env.getExtension();
        if (ext != null) {
           Expr ee = ext.get(name, n);
           if (ee != null){
               return ee;
           }
        }
        return extension.get(name, n);               
     }
       
    public static void define(Expr exp){
        extension.define(exp);
    }
    
    public static Extension getExtension(){
        return extension;
    }
    
}
