package fr.inria.corese.sparql.triple.function.term;

import fr.inria.corese.kgram.api.core.Node;
import static fr.inria.corese.kgram.api.core.PointerType.NODE;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TermEval extends Term {
    public static Logger logger = LoggerFactory.getLogger(TermEval.class);
    public static final IDatatype TRUE = DatatypeMap.TRUE;
    public static final IDatatype FALSE = DatatypeMap.FALSE;
    public static boolean OVERLOAD = false;
    
    public static final String SPARQL_MESS = "SPARQL query unauthorized";
    public static final String LOAD_MESS = "Load unauthorized";
    public static final String READ_MESS = "Read unauthorized";
    public static final String HTTP_MESS = "HTTP unauthorized";
    public static final String WRITE_MESS = "Write unauthorized";
    public static final String JAVA_FUNCTION_MESS = "Java function unauthorized";
    public static final String LINKED_TRANSFORMATION_MESS = "Linked transformation unauthorized";
    public static final String LINKED_FORMAT_MESS = "Linked format unauthorized";
    public static final String LINKED_FUNCTION_MESS = "Linked function unauthorized";
    public static final String LINKED_RULE_MESS = "Linked rule unauthorized";
    public static final String FUNCTION_DEFINITION_MESS = "Function definition unauthorized";
    public static final String IMPORT_MESS = "Import unauthorized";
    public static final String UNDEFINED_EXPRESSION_MESS = "Undefined expression";
    public static final String SERVICE_MESS = "Service unauthorized";
    
    
    public TermEval(String name, Expression e1, Expression e2, Expression e3) {
        super(name, e1, e2);
        add(e3);
    }

    public TermEval(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }
    
    public TermEval(String name, Expression e){
        super(name, e);
    }
    
    public TermEval(String name){
        super(name);
    }
    
    public TermEval(){
    } 
    
    public boolean isTrue(IDatatype dt) {
        try {
            return dt.isTrue();
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }
      
    public IDatatype value(boolean value){
        return (value) ? DatatypeMap.TRUE : DatatypeMap.FALSE;
    }
    
    public IDatatype value(int value){
        return DatatypeMap.newInstance(value);
    }
          
    /**
     * Compatibility for strbefore (no lang or same lang)
     */
    public boolean compatible(IDatatype dt1, IDatatype dt2) {
        if (!dt1.hasLang()) {
            return !dt2.hasLang();
        } else if (!dt2.hasLang()) {
            return true;
        } else {
            return dt1.getLang().equals(dt2.getLang());
        }
    }
    
    public boolean isStringLiteral(IDatatype dt){
        return DatatypeMap.isStringLiteral(dt);
    }
    
    public boolean isSimpleLiteral(IDatatype dt){
        return DatatypeMap.isSimpleLiteral(dt);
    }
    
    public IDatatype result(String str, IDatatype dt1, IDatatype dt2) {
        if (dt1.hasLang() && str != "") {
            return DatatypeMap.createLiteral(str, null, dt1.getLang());
        } else if (DatatypeMap.isString(dt1)) {
            return DatatypeMap.newInstance(str);
        }
        return DatatypeMap.newLiteral(str);
    }
    
    public IDatatype result(IDatatype dt, String value) {
        if (dt.hasLang()) {
            return DatatypeMap.createLiteral(value, null, dt.getLang());
        } else if (dt.isLiteral() && dt.getDatatype() == null) {
            return DatatypeMap.newLiteral(value);
        }
        return DatatypeMap.newInstance(value);
    }
    

    public IDatatype[] evalArguments(Computer eval, Binding b, Environment env, Producer p, int start) throws EngineException {
        IDatatype[] param = new IDatatype[args.size() - start];
        int i = 0;
        for (int j = start; j < args.size(); j++) {
            param[i] = args.get(j).eval(eval, b, env, p);
            if (param[i] == null) {                
                return null;
            }
            i++;
        }
        return param;
    }
    
    
    public IDatatype error(Computer eval, Binding b, Environment env, Producer p, IDatatype... param) {
        return  env.getVisitor().error(env.getEval(), this, param);       
    }
    
    public IDatatype overload(Computer eval, Binding b, Environment env, Producer p, IDatatype dt1, IDatatype dt2, IDatatype res) {
        if (OVERLOAD) {
            if (env.getVisitor().overload(this, res, dt1, dt2)) {
                return overload(env, res, dt1, dt2);
            }
            if (res == null) {
                // TODO: create a vector for each error !!!
                return error(eval, b, env, p, dt1, dt2);
            }
        }
        return res;
    }
    
    public IDatatype overload(Environment env, IDatatype res, IDatatype... param) {
        return  env.getVisitor().overload(env.getEval(), this, res, param);  
    }
    
    public Node getNode(IDatatype dt, Producer p) {
        if (dt.pointerType() == NODE) {
            return dt.getPointerObject().getNode();
        }
        return p.getNode(dt);
    }
    
    public boolean accept(Feature feature, Binding b) {
        return Access.accept(feature, b.getAccessLevel());
    }
    
    public boolean reject(Feature feature, Binding b) {
        return Access.reject(feature, b.getAccessLevel());
    }
    
    public boolean reject(Feature feature, Binding b, String uri) {
        return Access.reject(feature, b.getAccessLevel(), uri);
    }
    
    public void check(Feature feature, Binding b, String uri, String mes) throws SafetyException {
        Access.check(feature, b.getAccessLevel(), uri, mes);
    }
    
    public void check(Feature feature, Binding b, String mes) throws SafetyException {
        Access.check(feature, b.getAccessLevel(), mes);
    }
    
    public static boolean isFile(String path) {
        return NSManager.isFile(path);
    }
    
    public void log(String mess) {
        logger.error(mess);
    }
      
}
