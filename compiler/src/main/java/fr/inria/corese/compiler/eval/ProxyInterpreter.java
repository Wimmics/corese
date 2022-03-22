package fr.inria.corese.compiler.eval;

import fr.inria.corese.sparql.api.ComputerProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.event.EvalListener;
import fr.inria.corese.sparql.api.GraphProcessor;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.parser.Context;

/**
 * Evaluator of operators & functions of filter language 
 * implemented by fr.inria.corese.core.query.PluginImpl
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 * 
 */
public abstract class ProxyInterpreter implements ExprType {

    private static final String URN_UUID = "urn:uuid:";
    private static Logger logger = LoggerFactory.getLogger(ProxyInterpreter.class);
    public static final IDatatype TRUE = DatatypeMap.TRUE;
    public static final IDatatype FALSE = DatatypeMap.FALSE;
    public static final IDatatype UNDEF = DatatypeMap.UNBOUND;

    static final String UTF8 = "UTF-8";
    public static final String RDFNS = NSManager.RDF; //"http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFTYPE = RDFNS + "type";
    private static final String USER_DISPLAY = NSManager.USER + "display";
    public static int count = 0;
    //ProxyInterpreter plugin;
    Custom custom;
    SQLFun sql;
    Interpreter eval;
    private Producer producer;
    EvalListener el;
    // for LDScript java compiling only
    private Environment environment;
    int number = 0;
    // KGRAM is relax wrt to string vs literal vs uri input arg of functions
    // eg regex() concat() strdt()
    // setMode(SPARQL_MODE) 
    boolean SPARQLCompliant = false;
    protected IDatatype EMPTY = DatatypeMap.newStringBuilder("");

    public ProxyInterpreter() {
//        sql = new SQLFun();
//        custom = new Custom();
        //setPlugin(this);
    }

    public void setEvaluator(Interpreter ev) {
        eval =  ev;
//        if (plugin != null) {
//            plugin.setEvaluator(ev);
//        }
    }

    public Interpreter getEvaluator() {
        return eval;
    }

//    public void setPlugin(ProxyInterpreter p) {
//        plugin = p;
//        //plugin.setEvaluator(eval);
//    }

//    public ProxyInterpreter getPlugin() {
//        return plugin;
//    }
//
//    public ProxyInterpreter getComputerPlugin() {
//        return plugin;
//    }

    public GraphProcessor getGraphProcessor() {
        //return plugin.getGraphProcessor();
        return null;
    }

    public ComputerProxy getComputerTransform() {
        //return plugin.getComputerTransform();
        return null;
    }

    public void setMode(int mode) {
//        switch (mode) {
//
//            case Evaluator.SPARQL_MODE:
//                SPARQLCompliant = true;
//                break;
//
//            case Evaluator.KGRAM_MODE:
//                SPARQLCompliant = false;
//                break;
//        }
//        plugin.setMode(mode);
    }

    public void start() {
        number = 0;
    }

    
    public Producer getProducer() {
        return producer;
    }

    
    // for Core & Extension
    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public IDatatype getValue(boolean b) {
        if (b) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public IDatatype getValue(int value) {
        return DatatypeMap.newInstance(value);
    }

    public IDatatype getValue(long value) {
        return DatatypeMap.newInstance(value);
    }

    public IDatatype getValue(float value) {
        return DatatypeMap.newInstance(value);
    }

    public IDatatype getValue(double value) {
        return DatatypeMap.newInstance(value);
    }

    public IDatatype getValue(double value, String datatype) {
        return DatatypeMap.newInstance(value, datatype);
    }

    // return xsd:string
    public IDatatype getValue(String value) {
        return DatatypeMap.newInstance(value);
    }

    public Function getDefine(Expr exp, Environment env, String name, int n) throws EngineException {
        //return plugin.getDefine(exp, env, name, n);
        return null;
    }

    public void start(Producer p, Environment env) {
        //plugin.start(p, env);
    }

    public void finish(Producer p, Environment env) {
        //plugin.finish(p, env);
    }

//    public IDatatype getBufferedValue(StringBuilder sb, Environment env) {
//        //return plugin.getBufferedValue(sb, env);
//        return null;
//    }
   
    public Environment getEnvironment() {
        return environment;
    }

    public Context getContext() {
        return ((Interpreter) getEval().getEvaluator()).getContext(getEnvironment().getBind(), getEnvironment(), getProducer());
    }

    public Eval getEval() {
        return getEnvironment().getEval();
    }
    
    // for Core & Extension
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
