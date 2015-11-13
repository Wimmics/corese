package fr.inria.edelweiss.kgenv.eval;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprLabel;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.filter.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements evaluator of operators & functions of filter language with
 * IDatatype values
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class ProxyImpl implements Proxy, ExprType {

    private static final String URN_UUID = "urn:uuid:";
    private static Logger logger = Logger.getLogger(ProxyImpl.class);
    public static IDatatype TRUE = DatatypeMap.TRUE;
    public static IDatatype FALSE = DatatypeMap.FALSE;
    static final String UTF8 = "UTF-8";
    public static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFTYPE = RDFNS + "type";
    Proxy plugin;
    SQLFun sql;
    Evaluator eval;
    EvalListener el;
    int number = 0;
    // KGRAM is relax wrt to string vs literal vs uri input arg of functions
    // eg regex() concat() strdt()
    // setMode(SPARQL_MODE) 
    boolean SPARQLCompliant = false;
    protected IDatatype EMPTY = DatatypeMap.newStringBuilder("");
    

    public ProxyImpl() {
        sql = new SQLFun();
    }

    public void setEvaluator(Evaluator ev) {
        eval = ev;
    }

    public Evaluator getEvaluator() {
        return eval;
    }

    public void setPlugin(Proxy p) {
        plugin = p;
        plugin.setEvaluator(eval);
    }

    public Proxy getPlugin() {
        return plugin;
    }

    public void setMode(int mode) {
        switch (mode) {

            case Evaluator.SPARQL_MODE:
                SPARQLCompliant = true;
                break;

            case Evaluator.KGRAM_MODE:
                SPARQLCompliant = false;
                break;
        }
        plugin.setMode(mode);
    }

    public void start() {
        number = 0;
    }
    
     @Override 
     public IDatatype cast(Object obj, Environment env, Producer p){
         return DatatypeMap.cast(obj);
     }
     
    @Override
     public IDatatype[] createParam(int n){
         return new IDatatype[n];
     }
     
     String label(int ope){
         switch (ope){
             case ExprType.EQ:  return ExprLabel.EQUAL;
             case ExprType.NEQ: return ExprLabel.DIFF;
             case ExprType.LT:  return ExprLabel.LESS;
             case ExprType.LE:  return ExprLabel.LESS_EQUAL;
             case ExprType.GT:  return ExprLabel.GREATER;
             case ExprType.GE:  return ExprLabel.GREATER_EQUAL;
             // Proxy implements IN with equal, lets use ext:equal as well
             case ExprType.IN:  return ExprLabel.EQUAL;
                 
             case ExprType.PLUS:    return ExprLabel.PLUS;
             case ExprType.MINUS:   return ExprLabel.MINUS;
             case ExprType.MULT:    return ExprLabel.MULT;
             case ExprType.DIV:     return ExprLabel.DIV;
         }
         return null;
     }
     
     /**
      * exp:      a = b
      * datatype: http://example.org/datatype
      * result:   http://example.org/datatype#equal
      */
     String label(Expr exp, String datatype){
         
         return datatype.concat(ExprLabel.SEPARATOR + label(exp.oper()));
     }
     
     IDatatype[] array(IDatatype o1, IDatatype o2){
          IDatatype[] args = new IDatatype[2];
          args[0] = o1;
          args[1] = o2;
          return args;
     }
     
     // TODO: check ExprLabel.COMPARE
     public int compare(Environment env, Producer p, Node o1, Node o2) {
        IDatatype dt1 = (IDatatype) o1.getValue();
        IDatatype dt2 = (IDatatype) o2.getValue();
        if (dt1.getCode() == IDatatype.UNDEF && dt2.getCode() == IDatatype.UNDEF){
            if (dt1.getDatatypeURI().equals(dt2.getDatatypeURI())){   
                // get an extension function that implements the operator for
                // the extended datatype
                Expr exp = eval.getDefine(env, ExprLabel.COMPARE, 2);
                if (exp != null){
                     IDatatype res = (IDatatype) eval.eval(exp.getFunction(), env, p, array(dt1, dt2), exp);
                     if (res == null){
                         return 0;
                     }
                     return res.intValue();
                }                
            }
        }
        return dt1.compareTo(dt2);
     }
     

    @Override
    public Object term(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt1 = (IDatatype) o1;
        IDatatype dt2 = (IDatatype) o2;
        
        if (dt1.getCode() == IDatatype.UNDEF && dt2.getCode() == IDatatype.UNDEF){
            String d1 = dt1.getDatatypeURI();
            if (d1.equals(dt2.getDatatypeURI())){   
                // get an extension function that implements the operator for
                // the extended datatype
                Expr ee = eval.getDefine(env, label(exp, d1), exp.arity()); 
                if (ee != null){
                   return  eval.eval(exp, env, p, array(dt1, dt2), ee);
                }                
            }
        }

        if (dt1.isBlank() && dt2.isBlank() && ! exp.isFuncall()) {
            // exclude funcall kg:equal() to prevent a loop
            Expr ee = eval.getDefine(env, label(exp, ExpType.BNODE), exp.arity());
            if (ee != null) {
                return eval.eval(exp, env, p, array(dt1, dt2), ee);
            }
        }
        
        boolean b = true;

        try {
            switch (exp.oper()) {
                                
                case IN:
                    return in(dt1, dt2);
                    
                case EQ:
                    b = dt1.equalsWE(dt2);
                    break;               
                case NEQ:
                    b = !dt1.equalsWE(dt2);
                    break;
                case LT:
                    b = dt1.less(dt2);
                    break;
                case LE:
                    b = dt1.lessOrEqual(dt2);
                    break;
                case GE:
                    b = dt1.greaterOrEqual(dt2);
                    break;
                case GT:
                    b = dt1.greater(dt2);
                    break;
                    
                case CONT:
                    b = dt1.contains(dt2);
                    break;
                case START:
                    b = dt1.startsWith(dt2);
                    break;

                case PLUS:
                    if (SPARQLCompliant) {
                        if (!(dt1.isNumber() && dt2.isNumber())) {
                            return null;
                        }
                    }
                    return dt1.plus(dt2);

                case MINUS:
                    return dt1.minus(dt2);
                case MULT:
                    return dt1.mult(dt2);
                case DIV:
                    try {
                        return dt1.div(dt2);
                    } catch (java.lang.ArithmeticException e) {
                        return null;
                    }

                default:
//                    if (plugin != null) {
//                        return plugin.term(exp, env, p, dt1, dt2);
//                    }
                    return null;

            }
        } catch (CoreseDatatypeException e) {
            return null;
        }

        return (b) ? TRUE : FALSE;
    }

    public Object function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {

             case CONCAT:
             case STL_CONCAT:
                return concat(exp, env, p);
                 
            case NUMBER:
                return getValue(env.count());
                           
            case RANDOM:
                return getValue(Math.random());

            case NOW:
                return DatatypeMap.newDate();

            case BNODE:
                return createBlank();

            case PATHNODE:
                return pathNode(env);

            case FUUID:
                return uuid();

            case STRUUID:
                return struuid();
                
            case FOR:
                return loop(exp, env, p);
                
            case XT_DISPLAY:
                System.out.println();
                return TRUE;

            default:
                if (plugin != null) {
                    return plugin.function(exp, env, p);
                }
        }

        return null;
    }

    public IDatatype struuid() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return DatatypeMap.createLiteral(str);
    }

    public IDatatype uuid() {
        UUID uuid = UUID.randomUUID();
        String str = URN_UUID + uuid;
        return DatatypeMap.createResource(str);
    }

    public Object function(Expr exp, Environment env, Producer p, Object o1) {

        IDatatype dt = (IDatatype) o1;

        switch (exp.oper()) {

            case ISURI:
                return (dt.isURI()) ? TRUE : FALSE;

            case ISLITERAL:
                return (dt.isLiteral()) ? TRUE : FALSE;

            case ISBLANK:
                return (dt.isBlank()) ? TRUE : FALSE;

            case ISSKOLEM:
                return (dt.isSkolem()) ? TRUE : FALSE;

            case ISNUMERIC:
                return (dt.isNumber()) ? TRUE : FALSE;

            case URI:
                return uri(exp, dt);

            case STR:
                return str(exp, dt);
                
            case XSDSTRING:
                return xsdstring(exp, dt);    

            case STRLEN:
                return getValue(dt.getLabel().length());

            case UCASE:
                return ucase(dt);

            case LCASE:
                return lcase(dt);

            case ENCODE:
                return encode(dt);

            case ABS:
                return abs(dt);

            case FLOOR:
                return getValue(Math.floor(dt.doubleValue()), dt.getDatatypeURI());

            case ROUND:
                return getValue(Math.round(dt.doubleValue()), dt.getDatatypeURI());

            case CEILING:
                return getValue(Math.ceil(dt.doubleValue()), dt.getDatatypeURI());

            case TIMEZONE:
                return DatatypeMap.getTimezone(dt);

            case TZ:
                return DatatypeMap.getTZ(dt);

            case YEAR:
            case MONTH:
            case DAY:
            case HOURS:
            case MINUTES:
            case SECONDS:
                return time(exp, dt);

            case HASH:
                return hash(exp, dt);

            case LANG:
                return dt.getDataLang();

            case BNODE:
                return bnode(dt, env);

            case DATATYPE:
                return dt.getDatatype();

            case DISPLAY:
               return display(exp, dt, null);
                                        
            case SLICE:
                return slice(env, dt);  
                
            case XT_COUNT:
                return count(dt);
                
            case XT_FIRST:
                return DatatypeMap.first(dt);
                
            case XT_REST:
                return DatatypeMap.rest(dt);
                               
            case XT_REVERSE:
                 return DatatypeMap.reverse(dt);
                
            case XT_SORT:
                return DatatypeMap.sort(dt);
                
            case XT_REJECT:
                return reject(env, dt); 
                
            case XT_DISPLAY:
                System.out.println(dt);
                return TRUE;
                    
                                                               
            default:
                if (plugin != null) {
                    return plugin.function(exp, env, p, o1);
                }

        }
        return null;
    }
    
    IDatatype display(Expr exp, IDatatype dt, IDatatype arg) {
        if (dt.getObject() != null) {
            System.out.println(exp.getExp(0) + " = " + dt.getObject());
        }
        else  if (arg == null){
            System.out.println(exp.getExp(0) + " = " + dt);
        }
        else {
            if (! arg.equals(FALSE)){
                System.out.println(arg.stringValue());
            }
            System.out.println(dt.stringValue());
            System.out.println();
        }       
        return TRUE;
    }

    public Object function(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt1 = (IDatatype) o1;
        IDatatype dt2 = (IDatatype) o2;
        boolean b;

        switch (exp.oper()) {
            
            case POWER:
               return getValue(Math.pow(dt1.doubleValue(), dt2.doubleValue()));
                
            case PLUS:
            case MINUS:
            case MULT:
            case DIV:
            case EQ:
            case NEQ:
                return term(exp, env, p, o1, o2);
                
            case OR:
                return or(dt1, dt2);
            case AND:
                return and(dt1, dt2);
            
             case DISPLAY:
               return display(exp, dt1, dt2);

            case CONT:
                return getValue(dt1.contains(dt2));

            case CONTAINS:
                if (!compatible(dt1, dt2)) {
                    return null;
                }
                b = dt1.getLabel().contains(dt2.getLabel());
                return (b) ? TRUE : FALSE;

            case STARTS:
                if (!compatible(dt1, dt2)) {
                    return null;
                }
                b = dt1.startsWith(dt2);
                return (b) ? TRUE : FALSE;

            case ENDS:
                if (!compatible(dt1, dt2)) {
                    return null;
                }
                b = dt1.getLabel().endsWith(dt2.getLabel());
                return (b) ? TRUE : FALSE;

            case SUBSTR:
                return substr(dt1, dt2, null);

            case STRBEFORE:
                return strbefore(dt1, dt2);

            case STRAFTER:
                return strafter(dt1, dt2);

            case LANGMATCH:
                return langMatches(dt1, dt2);

            case STRDT:
                if (SPARQLCompliant && !DatatypeMap.isSimpleLiteral(dt1)) {
                    return null;
                }
                return DatatypeMap.createLiteral(dt1.getLabel(), dt2.getLabel());

            case STRLANG:
                if (SPARQLCompliant && !DatatypeMap.isSimpleLiteral(dt1)) {
                    return null;
                }
                return DatatypeMap.createLiteral(dt1.getLabel(), null, dt2.getLabel());

            case REGEX: {
                if (!isStringLiteral(dt1)) {
                    return null;
                }
                Processor proc = getProcessor(exp);
                b = proc.regex(dt1.getLabel(), dt2.getLabel());
                return (b) ? TRUE : FALSE;
            }


            case XPATH: {
                // xpath(?g, '/book/title')
                Processor proc = getProcessor(exp);
                proc.setResolver(new VariableResolverImpl(env));
                IDatatype res = proc.xpath(dt1, dt2);
                return res;
            }

            case EXTEQUAL: {
                boolean bb = StringHelper.equalsIgnoreCaseAndAccent(dt1.getLabel(), dt2.getLabel());
                return (bb) ? TRUE : FALSE;
            }

            case EXTCONT: {
                boolean bb = StringHelper.containsWordIgnoreCaseAndAccent(dt1.getLabel(), dt2.getLabel());
                return (bb) ? TRUE : FALSE;
            }
                
            case XT_CONS:
                return DatatypeMap.cons(dt1, dt2);
                
            case XT_APPEND:
                return DatatypeMap.append(dt1, dt2);
                
            case XT_GET:
                return get(dt1, dt2);
                
            case XT_DISPLAY:
                System.out.println(dt1 + " " + dt2);
                return TRUE;    
                
            default:
                if (plugin != null) {
                    return plugin.function(exp, env, p, o1, o2);
                }


        }

        return null;
    }

    @Override
    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {
        IDatatype[] param =  (IDatatype[]) args;
        IDatatype val = (param.length>0) ?  param[0] : null;
        switch (exp.oper()) {
                                                
            case MAPANY:                
            case MAPEVERY:
                 return anyevery(exp, env, p, param);
                
            case MAP:
            case MAPLIST:
            case MAPMERGE:
            case MAPSELECT:
                return map(exp, env, p, param);
                               
            case APPLY:
                return apply(exp, env, p, val);

            case EXTERNAL:
                // user defined function with prefix/namespace
                // function://package.className
                Processor proc = getProcessor(exp);
                return proc.eval(param);

            case KGRAM:
            case EXTERN:
            case PROCESS:
                return plugin.eval(exp, env, p, param);

            case DEBUG:
                if (el == null) {
                    el = EvalListener.create();
                    env.getEventManager().addEventListener(el);
                }
                int i = 0;
                for (Object arg : param) {
                    Event e = EventImpl.create(Event.FILTER, exp.getExp(i++), arg);
                    env.getEventManager().send(e);
                }
                return TRUE;
                           
            case STL_AND:
                return and(param); 
                
            case XT_SET: 
                return DatatypeMap.set(param[0], param[1], param[2]);
                
            case XT_GEN_GET:
                return gget(param[0], param[1], param[2]);
                                               
            case LIST:
                return DatatypeMap.list(param);                
                
            case CONCAT:
            case STL_CONCAT:
            case XT_CONCAT:            
                return concat(exp, env, p, param);
                
            case XT_DISPLAY:
                for (IDatatype dt : param){
                    System.out.print(dt + " ");
                }
                System.out.println();
                return TRUE;       
            
        }


        boolean b = true;
        IDatatype dt = null, dt1 = null;
        if (param.length > 0) {
            dt =  param[0];
        }
        if (param.length > 1) {
            dt1 =  param[1];
        }

        switch (exp.oper()) {

            case REGEX:
                // it may have a 3rd argument stored as getModality()
                if (!isStringLiteral(dt)) {
                    return null;
                }
                Processor proc = getProcessor(exp);
                b = proc.regex(dt.getLabel(), dt1.getLabel());
                return (b) ? TRUE : FALSE;

            case SUBSTR:
                IDatatype dt2 = null;
                if (param.length > 2) {
                    dt2 = param[2];
                }
                return substr(dt, dt1, dt2);

            case CAST: // cast(?x, xsd:string, CoreseString)
                return dt.cast(dt1, param[2]);


            case STRREPLACE:

                if (! (param.length == 3 || param.length == 4)) {
                    return null;
                }
                
                if (! isStringLiteral(dt)) {
                    return null;
                }
                
                return strreplace(exp, dt, dt1, param[2]);


            case SQL:
                // return ResultSet
                return sql(exp, env, param);

            default:
                if (plugin != null) {
                    return plugin.eval(exp, env, p, param);
                }
        }

        return null;
    }

    boolean isStringLiteral(IDatatype dt) {
        return !SPARQLCompliant || DatatypeMap.isStringLiteral(dt);
    }

    IDatatype encode2(IDatatype dt) {
        try {
            String str = URLEncoder.encode(dt.getLabel(), UTF8);
            return DatatypeMap.createLiteral(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    IDatatype encode(IDatatype dt) {
        String str = encodeForUri(dt.getLabel());
        return DatatypeMap.createLiteral(str);
    }

    public String encodeForUri(String str) {

        StringBuilder sb = new StringBuilder(2 * str.length());

        for (int i = 0; i < str.length(); i++) {
            
            char c = str.charAt(i);

            if (stdChar(c)) {
                sb.append(c);
            } else {
                try {
                    byte[] bytes = Character.toString(c).getBytes("UTF-8");

                    for (byte b : bytes) {
                        sb.append("%");

                        char cc = (char) (b & 0xFF);

                        String hexa = Integer.toHexString(cc).toUpperCase();

                        if (hexa.length() == 1) {
                            sb.append("0");
                        }

                        sb.append(hexa);
                    }

                } catch (UnsupportedEncodingException e) {
                }
            }
        }

        return sb.toString();
    }

    boolean stdChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' 
                || c == '-' || c == '.' || c == '_' || c == '~';
    }

    // first index is 1
    IDatatype substr(IDatatype dt, IDatatype ind, IDatatype len) {
        String str = dt.getLabel();
        int start = ind.intValue();
        start = Math.max(start - 1, 0);
        int end = str.length();
        if (len != null) {
            end = len.intValue();
        }
        end = start + end;
        if (end > str.length()) {
            end = str.length();
        }
        str = str.substring(start, end);
        return getValue(dt, str);
    }

    // return a Literal (not a xsd:string)
    IDatatype str(Expr exp, IDatatype dt) {
        return DatatypeMap.createLiteral(dt.getLabel());
    }
    
     IDatatype xsdstring(Expr exp, IDatatype dt) {
        return DatatypeMap.newInstance(dt.getLabel());
    }

    IDatatype ucase(IDatatype dt) {
        String str = dt.getLabel().toUpperCase();
        return getValue(dt, str);
    }

    IDatatype lcase(IDatatype dt) {
        String str = dt.getLabel().toLowerCase();
        return getValue(dt, str);
    }

    IDatatype uri(Expr exp, IDatatype dt) {
        if (dt.isURI()) {
            return dt;
        }
        String label = dt.getLabel();
        if (exp.getModality() != null && !isURI(label)) {
            // with base
            return DatatypeMap.newResource(exp.getModality() + label);
        } else {
            return DatatypeMap.newResource(label);
        }
    }

    boolean isURI(String str) {
        return str.matches("[a-zA-Z0-9]+://.*");
    }

    /**
     * Compatibility for strbefore (no lang or same lang)
     */
    boolean compatible(IDatatype dt1, IDatatype dt2) {
        if (!dt1.hasLang()) {
            return !dt2.hasLang();
        } else if (!dt2.hasLang()) {
            return true;
        } else {
            return dt1.getLang().equals(dt2.getLang());
        }
    }

    IDatatype strbefore(IDatatype dt1, IDatatype dt2) {

        if (!isStringLiteral(dt1) || !compatible(dt1, dt2)) {
            return null;
        }

        int index = dt1.getLabel().indexOf(dt2.getLabel());
        String str = "";
        if (index != -1) {
            str = dt1.getLabel().substring(0, index);
        }
        return result(str, dt1, dt2);
    }

    IDatatype strafter(IDatatype dt1, IDatatype dt2) {
        if (!isStringLiteral(dt1) || !compatible(dt1, dt2)) {
            return null;
        }

        int index = dt1.getLabel().indexOf(dt2.getLabel());
        String str = "";
        if (index != -1) {
            str = dt1.getLabel().substring(index + dt2.getLabel().length());
        }
        return result(str, dt1, dt2);
    }

    IDatatype strreplace(Expr exp, IDatatype dt1, IDatatype dt2, IDatatype dt3) {
        Processor p = getProcessor(exp);
        String str = p.replace(dt1.getLabel(), dt3.getLabel());
        return result(str, dt1, dt3);
    }

    IDatatype result(String str, IDatatype dt1, IDatatype dt2) {
        if (dt1.hasLang() && str != "") {
            return DatatypeMap.createLiteral(str, null, dt1.getLang());
        } else if (DatatypeMap.isString(dt1)) {
            return getValue(str);
        }
        return DatatypeMap.createLiteral(str);
    }
    
    IDatatype slice (Environment env, IDatatype dt){
        env.getQuery().setSlice(dt.intValue());
        return TRUE;
    }

    /**
     * literals with same lang return literal@lang all strings return string
     * else return literal error if not literal or string
     */
      IDatatype concat(Expr exp, Environment env, Producer p) {
            return concat(exp, env, p, null);
      }

    /**
     * std usage: lval is null, evaluate exp
     * lval = list of values in this use case:
     * apply(concat(), maplist(st:fun(?x) , xt:list(...)))
     * 
     */
    IDatatype concat(Expr exp, Environment env, Producer p, IDatatype[] lval) {
        String str = "";
        String lang = null;

        if (exp.arity() == 0 && lval == null) {
            return EMPTY;
        }
        int length = 0;
        if (lval != null){
            length = lval.length;
        }
        
        // when template st:concat()
        // st:number() is not evaluated now
        // it will be evaluated by template group_concat aggregate
        // return future(concat(str, st:number(), str))
        boolean isSTLConcat = exp.oper() == STL_CONCAT;

        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = null;
        boolean ok = true, hasLang = false, isString = true;
        IDatatype dt = null;
        int i = 0;
        List<Expr> argList = exp.getExpList();
        for (int j = 0; j < ((length > 0) ? length : argList.size()); ) {

            if (lval == null) {
                Expr ee = argList.get(j);

                if (isSTLConcat && isFuture(ee)) {
                    // create a future
                    if (list == null) {
                        list = new ArrayList<Object>();
                    }
                    if (sb.length() > 0) {
                        list.add(result(env, sb, isString, (ok && lang != null) ? lang : null, isSTLConcat));
                        sb = new StringBuilder();
                    }
                    list.add(ee);
                    // Do not touch to j++ (see below int k = j;)   
                    j++;
                    continue;
                }
                dt = (IDatatype) eval.eval(ee, env, p);
            } else {
                dt =  lval[j];
            }
            // Do not touch to j++ (see below int k = j;)             
            j++;
            
            if (dt == null){
                return null;
            }

            if (isSTLConcat && dt.isFuture()){
                // result of ee is a Future
                // use case: ee = box { e1 st:number() e2 }
                // ee = st:concat(e1, st:number(), e2) 
                // dt = Future(concat(e1, st:number(), e2))
                // insert Future arg list (e1, st:number(), e2) into current argList
                // arg list is inserted after ee (indice j is already  set to j++)
                ArrayList<Expr> el = new ArrayList(argList.size());
                el.addAll(argList);
                Expr future = (Expr) dt.getObject();
                int k = j;

                for (Expr arg : future.getExpList()){
                    el.add(k++, arg);
                }
                argList = el;
                continue; 
            }
            
            if (i == 0 && dt.hasLang()) {
                hasLang = true;
                lang = dt.getLang();
            }
            i++;
            
            if (!isStringLiteral(dt)) {
                return null;
            }

            if (dt.getStringBuilder() != null) {
                sb.append(dt.getStringBuilder());
            } else {
                sb.append(dt.getLabel());
            }
          
            if (ok) {
                if (hasLang) {
                    if (!(dt.hasLang() && dt.getLang().equals(lang))) {
                        ok = false;
                    }
                } else if (dt.hasLang()) {
                    ok = false;
                }

                if (!DatatypeMap.isString(dt)) {
                    isString = false;
                }
            }
            
        }
            
        if (list != null){
            // return ?out = future(concat(str, st:number(), str)
            // will be evaluated by template group_concat(?out) aggregate
            if (sb.length()>0){
                list.add(result(env, sb, isString, (ok && lang != null)?lang:null, isSTLConcat));
            }  
            Expr e = plugin.createFunction(Processor.CONCAT, list, env);
            IDatatype res = DatatypeMap.createFuture(e);
            return res;
        }
        
        return result(env, sb, isString, (ok && lang != null)?lang:null, isSTLConcat);
    }
    
    boolean isFuture(Expr e){
        if  (e.oper() == STL_NUMBER 
                //|| e.oper() == STL_FUTURE
                ){
            return true;
        }
        if  (e.oper() == CONCAT || e.oper() == STL_CONCAT){
            // use case:  group { st:number() } box { st:number() }
            return false;
        }
        if (e.arity() > 0){
            for (Expr a : e.getExpList()){
                if (isFuture(a)){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     *     
     */
    IDatatype result(Environment env, StringBuilder sb, boolean isString, String lang, boolean isSTL){
        if (lang != null) {
            return DatatypeMap.createLiteral(sb.toString(), null, lang);
        } else if (isString) {
            if (isSTL){
                return (IDatatype) plugin.getBufferedValue(sb, env);
            }
            else {
                return DatatypeMap.newStringBuilder(sb);
            }
        } else {
            return DatatypeMap.createLiteral(sb.toString());
        }
    }
    
    IDatatype and(IDatatype[] val){
        for (IDatatype dt : val){
            if (dt == null){
                return null;
            }
            
            try {
                if (! dt.isTrue()){
                    return DatatypeMap.FALSE;
                }
            } catch (CoreseDatatypeException ex) {
                return null;
            }
            
        }
        return DatatypeMap.TRUE;
    }

    /**
     * same bnode for same label in same solution, different otherwise
     */
    Object bnode(IDatatype dt, Environment env) {
        Map map = env.getMap();
        Object bn = map.get(dt.getLabel());
        if (bn == null) {
            bn = createBlank();
            map.put(dt.getLabel(), bn);
        } else {
        }

        return bn;
    }

    IDatatype createBlank() {
        return DatatypeMap.createBlank();
    }

    IDatatype time(Expr exp, IDatatype dt) {
        if (dt.getDatatypeURI().equals(RDF.xsddate)
                || dt.getDatatypeURI().equals(RDF.xsddateTime)) {

            switch (exp.oper()) {

                case YEAR:
                    return DatatypeMap.getYear(dt);
                case MONTH:
                    return DatatypeMap.getMonth(dt);
                case DAY:
                    return DatatypeMap.getDay(dt);

                case HOURS:
                    return DatatypeMap.getHour(dt);
                case MINUTES:
                    return DatatypeMap.getMinute(dt);
                case SECONDS:
                    return DatatypeMap.getSecond(dt);
            }
        }

        return null;
    }

    IDatatype hash(Expr exp, IDatatype dt) {
        String name = exp.getModality();
        String str = dt.getLabel();
        String res = new Hash(name).hash(str);
        if (res == null) {
            return null;
        }
        return DatatypeMap.createLiteral(res);
    }

    IDatatype abs(IDatatype dt) {
        if (DatatypeMap.isInteger(dt)) {
            return getValue(Math.abs(dt.intValue()));
        } else if (DatatypeMap.isLong(dt)) {
            return getValue(Math.abs(dt.longValue()));
        } else {
            return getValue(Math.abs(dt.doubleValue()));
        }
    }

    /**
     * sum(?x)
     */
    public Object aggregate(Expr exp, Environment env, Producer p, Node qNode) {
        exp = decode(exp, env, p);
        Walker walk = new Walker(exp, qNode, this, env, p);

        // apply the aggregate on current group Mapping, 
        env.aggregate(walk, p, exp.getFilter());

        Object res = walk.getResult(env, p);
        return res;
    }
    
    public Expr decode (Expr exp, Environment env, Producer p){
        switch (exp.oper()){
            case STL_AGGREGATE:
                return plugin.decode(exp, env, p);
        }
        return exp;       
    }

    Processor getProcessor(Expr exp) {
        return ((Term) exp).getProcessor();
    }

    // IDatatype KGRAM value to target proxy value 
    public Object getConstantValue(Object value) {
        return value;
    }

    IDatatype pathNode(Environment env) {
        Query q = env.getQuery();
        int num = q.getGlobalQuery().nbPath();
        IDatatype dt = DatatypeMap.createBlank(Query.BPATH + Integer.toString(num));
        return dt;
    }

    @Override
    public boolean isTrue(Object value) {
        IDatatype dt = (IDatatype) value;
        //if (! dt.isTrueAble()) return false;
        try {
            return dt.isTrue();
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }

    public boolean isTrueAble(Object value) {
        IDatatype dt = (IDatatype) value;
        return dt.isTrueAble();
    }

    protected IDatatype datatype(Object o) {
        return (IDatatype) o;
    }

    @Override
    public IDatatype getValue(boolean b) {
        // TODO Auto-generated method stub
        if (b) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
    
    @Override
    public IDatatype getValue(Object val, Object obj){
       if (val instanceof Boolean){
           Boolean b = (Boolean) val;
           IDatatype dt = DatatypeMap.createInstance(b);
           dt.setObject(obj);
           return dt;
       }
       return null;
    }

    @Override
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

    // return rdfs:Literal or xsd:string wrt dt
    public IDatatype getValue(IDatatype dt, String value) {
        if (dt.hasLang()) {
            return DatatypeMap.createLiteral(value, null, dt.getLang());
        } else if (dt.isLiteral() && dt.getDatatype() == null) {
            return DatatypeMap.createLiteral(value);
        }
        return DatatypeMap.newInstance(value);
    }

    Object langMatches(IDatatype ln1, IDatatype ln2) {
        String l1 = ln1.getLabel();
        String l2 = ln2.getLabel();

        if (l2.equals("*")) {
            return getValue(l1.length() > 0);
        }
        if (l2.indexOf("-") != -1) {
            // en-us need exact match
            return getValue(l1.toLowerCase().equals(l2.toLowerCase()));
        }
        return getValue(l1.regionMatches(true, 0, l2, 0, 2));
    }

    public Object self(Object obj) {
        return obj;
    }

    public Object similarity(Environment env) {
        if (!(env instanceof Memory)) {
            return getValue(0);
        }
        Memory memory = (Memory) env;
        int count = 0, total = 0;

        for (Edge qEdge : memory.getQueryEdges()) {

            if (qEdge != null && qEdge.getLabel().equals(RDFTYPE)) {
                Entity edge = memory.getEdge(qEdge);
                if (edge != null) {
                    Node type = qEdge.getNode(1);
                    if (type.isConstant()) {
                        total += 1;
                        if (type.same(edge.getNode(1))) {
                            count += 1;
                        }
                    }
                }
            }
        }

        if (total == 0) {
            return getValue(1);
        } else {
            return getValue(count / total);
        }

    }

    /**
     * sql('db', 'login', 'passwd', 'query') sql('db', 'driver', 'login',
     * 'passwd', 'query') sql('db', 'driver', 'login', 'passwd', 'query', true)
     *
     * sort means list of sparql variables (sql() as (var)) must be sorted
     * according to sql variables in result
     */
    Object sql(Expr exp, Environment env, IDatatype[] args) {
        ResultSet rs;
        boolean isSort = false;
        if (args.length == 4) {
            // no driver
            rs = sql.sql(args[0], args[1], args[2], args[3]);
        } else {
            if (args.length == 6) {
                try {
                    isSort = args[5].isTrue();
                } catch (CoreseDatatypeException e) {
                }
            }
            // with driver
            rs = sql.sql(args[0], args[1], args[2], args[3], args[4]);
        }

        return new SQLResult(rs, isSort);
    }

    /**
     * ?x in (a b) ?x in (xpath())
     */
     Object in(IDatatype dt1, IDatatype dt2) {

        boolean error = false;

        if (dt2.isList()) {

            for (IDatatype dt : dt2.getValues()) {
                try {
                    if (dt1.equalsWE(dt)) {
                        return TRUE;
                    }
                } catch (CoreseDatatypeException e) {
                    error = true;
                }
            }

            if (error) {
                return null;
            }
            return FALSE;
        } else {
            try {
                if (dt1.equalsWE(dt2)) {
                    return TRUE;
                }
            } catch (CoreseDatatypeException e) {
                return null;
            }
        }

        return FALSE;
    }

    @Override
    public Expr createFunction(String name, List<Object> args, Environment env) {
        return null;    
    }

    @Override
    public void start(Producer p, Environment env) {
        plugin.start(p, env);
    }
    
     public void finish(Producer p, Environment env) {
        plugin.finish(p, env);
    }

     /**
      * for (var in list) { exp }
      * loop = for(var, list, exp)
      * @param exp
      * @param env
      * @param p
      * @return 
      */
    IDatatype loop(Expr loop, Environment env, Producer p){
        IDatatype list = (IDatatype) eval.eval(loop.getDefinition(), env, p);
        if (list == null){ 
            return null;
        }
        if (list.isList()){
            for (IDatatype dt : list.getValues()){           
                IDatatype res = let(loop.getBody(), loop.getVariable(), dt, env, p);
                if (res == null){
                    return null;
                }
            }
        }
        else { 
            for (Object obj : getValues(list)){  
                IDatatype res = let(loop.getBody(), loop.getVariable(), (IDatatype) p.getValue(obj), env, p);               
                if (res == null){
                    return null;
                }
            }
        }
        return TRUE;
    }
    
    
     /**
      * Loopable: Mappings Mapping Edge
      * iterator for (var in exp){}
      * */
    public Iterable<Object> getValues(IDatatype dt) {
        if (dt.isLoop()){
            return dt.getLoop();
        }
        return new ArrayList<Object>(0);
    }
    
      
     /**
      * map (xt:fun, ?x, ?l)
      * maplist (xt:fun, ?l1, ?l2)
      * maplist return list of results
      * mapselect return sublist of elements that match a boolean predicate
      * map return true
      * map on List or Loopable (IDatatype that contains e.g. Mappings)
     * PRAGMA: lists must have same length
      * @return 
      */
    private IDatatype map(Expr exp, Environment env, Producer p, IDatatype[] param) {
        boolean maplist   = exp.oper() == MAPLIST; 
        boolean mapmerge  = exp.oper() == MAPMERGE; 
        boolean mapselect = exp.oper() == MAPSELECT;
        boolean hasList   = maplist || mapmerge;
        
        IDatatype list = null;
        IDatatype ldt = null;
        Iterator loop = null ;
        boolean isList = false;
        
        for (IDatatype dt : param){  
            if (dt.isList()){
                isList = true;
                list = dt;
                break;
            }
            else if (dt.isLoop()) {
                hasList = false; // prevent maplist mapmerge
               ldt = dt; 
               loop = ldt.getLoop().iterator();
               break;
            }
        } 
        
        if (list == null && ldt == null){
            return null;
        }
        IDatatype[] value = new IDatatype[param.length];
        IDatatype[] res = (hasList) ? new IDatatype[list.size()] : null;
        ArrayList<IDatatype> sub = (mapselect) ? new ArrayList<IDatatype>() : null;
        int size = 0; 
        
        for (int i = 0;  (isList) ? i< list.size() : loop.hasNext(); i++){ 
            IDatatype elem = null;
            
            for (int j = 0; j<value.length; j++){
                IDatatype dt = param[j];
                if (dt.isList()){
                    value[j] = (i < dt.size()) ? dt.get(i) : dt.get(dt.size()-1);
                    if (mapselect && elem == null){
                        elem = value[j];
                    }
                }
                else if (dt.isLoop()){
                    if (loop.hasNext()){
                       value[j] = (IDatatype) p.getValue(loop.next());
                       if (mapselect && elem == null){
                         elem = value[j];
                       }
                    }
                    else {
                        return null;
                    }
                }
                else {
                    value[j] = dt;
                }
            }
            
            IDatatype val =  let(exp.getExp(0), env, p, value);
            
            if (val == null){
                return null;
            }
            else if (hasList) {
                if (val.isList()){
                    size += val.size();
                }
                else {
                    size += 1;
                }
               res[i] = val;
            }
            else if (mapselect && val.booleanValue()){
                    // select elem whose predicate is true
                    // mapselect (xt:prime, xt:iota(1, 100))
                    sub.add(elem);
            }
            
        }
        
        if (mapmerge){
            int i = 0;
            IDatatype[] merge = new IDatatype[size];
            for (IDatatype dt : res){
                if (dt.isList()){
                    for (IDatatype v : dt.getValues()){
                        merge[i++] = v;
                    }
                }
                else {
                    merge[i++] = dt;
                }
            }
            return DatatypeMap.createList(merge);
        }
        else if (maplist){
            return DatatypeMap.createList(res); 
        }
        else if (mapselect){
            return DatatypeMap.createList(sub);
        }
        return TRUE;
    }
      
  
    /**
     * every (xt:fun, ?list)   
     * every (xt:fun, ?x, ?list) 
     * every (xt:fun, ?l1, ?l2) 
     * PRAGMA: lists must have same length
     * error follow SPARQ semantics of OR (any) AND (every)
     * @return 
     */
    private IDatatype anyevery(Expr exp, Environment env, Producer p, IDatatype[] param) {
        boolean every = exp.oper() == MAPEVERY;       
        boolean any   = exp.oper() == MAPANY;       
        IDatatype list = null;        
        for (IDatatype dt : param){          
            if (dt.isList()){
                list = dt;
                break;
            }
        }               
        if (list == null){
            return null;
        }
        IDatatype[] value = new IDatatype[param.length];
        boolean error = false;      
        for (int i = 0; i<list.size(); i++){ 
            
            for (int j = 0; j<value.length; j++){
                IDatatype dt = param[j];
                if (dt.isList()){
                    value[j] = (i < dt.size()) ? dt.get(i) : dt.get(dt.size()-1);  
                }
                else {
                    value[j] = dt;
                }
            }
            
            IDatatype res =  let(exp.getExp(0), env, p, value);                   
            if (res == null){
                error = true;                
            }
            else {
                if (every) {
                    if (! res.booleanValue()){
                        return FALSE;
                    }
                }
                else if (any) {
                    // any
                    if (res.booleanValue()){
                        return TRUE;
                    }
                }
            }
        }
        if (error){
            return null;
        }
        return getValue(every);
    }
       
    
    /**
     * apply(kg:sum(?x), ?list)     
     */
//    private Object apply2(Expr exp, Environment env, Producer p, IDatatype dt) {
//        if (dt.getValues() == null){
//            return dt;
//        }
//        
//        return eval(exp.getExp(0), env, p, dt.getValues());
//               
//    }
    
    /**
     * apply(kg:plus, ?list)   
     * @return 
     */
    private IDatatype apply(Expr exp, Environment env, Producer p, IDatatype dt) {
        if (! dt.isList()) {
            return null;
        }
        List<IDatatype> list = dt.getValues();
        if (list.isEmpty()){
            return neutral(exp.getExp(0), dt);
        }
        IDatatype[] value = new IDatatype[2];
        IDatatype res = list.get(0);
        value[0] = res;
        
        for (int i = 1; i < list.size(); i++) {            
            value[1] = list.get(i);
            res =  let(exp.getExp(0), env, p, value);           
            if (res == null) {
               return error();
            }
            value[0] = res;
        }
        return res;
    }
    
    IDatatype neutral(Expr exp, IDatatype dt){
        switch (exp.oper()){
            case OR:
                return FALSE;
                
            case AND:
                return TRUE;
                
            case CONCAT:
                return DatatypeMap.EMPTY_STRING;
                
            case PLUS:
                return DatatypeMap.ZERO;
                
            case MULT:
                return DatatypeMap.ONE; 
                
            case XT_APPEND:
                return DatatypeMap.EMPTY_LIST;
                
            default: return dt;
        }
    }
    
     /**
     * exp = xt:fun(?x)
     */
    public IDatatype let(Expr exp, Environment env, Producer p, Object val) {
        return let(exp, exp.getExp(0), (IDatatype) val, env, p);
//        Expr var  = exp.getExp(0);  
//        env.set(exp, var, (IDatatype) val);
//        Object res = eval.eval(exp, env, p);
//        env.unset(exp, var);
//        return (IDatatype) res;
    }
    
    IDatatype let(Expr exp, Expr var, IDatatype value, Environment env, Producer p){
        env.set(exp, var, value);
        Object res = eval.eval(exp, env, p);
        env.unset(exp, var);
        return (IDatatype) res;
    }
    
    private IDatatype let(Expr exp, Environment env, Producer p, IDatatype[] values) {
        int i = 0;
        for (Expr var : exp.getExpList()){        
            env.set(exp, var, values[i++]);
        }
        Object res = eval.eval(exp, env, p);
        for (Expr var : exp.getExpList()){        
            env.unset(exp, var);
        }        
        return (IDatatype) res;
    }
      
     private IDatatype or(IDatatype dt1, IDatatype dt2) {
        boolean e1 = error(dt1);
        boolean e2 = error(dt2);
        if (e1 && e2) {
            return error();
        } else if (e1) {
            return errorOr(dt2);
        } else if (e2) {
            return errorOr(dt1);
        } else {
            return getValue(dt1.booleanValue() || dt2.booleanValue());
        }
    }
     
     boolean error(IDatatype dt){
         return dt == null || ! dt.isTrueAble();
     }
    
     IDatatype error() {
         return null;
     }
     
    IDatatype errorOr(IDatatype dt){
         if (dt.booleanValue()){
             return TRUE;
         }
         return error();
     }
   
     IDatatype errorAnd(IDatatype dt){
         if (dt.booleanValue()){
             return error();
         }
         return FALSE;
     }
     
    private IDatatype and(IDatatype dt1, IDatatype dt2) {
        boolean e1 = error(dt1);
        boolean e2 = error(dt2);
        if (e1 && e2) {
            return error();
        } else if (e1) {
            return errorAnd(dt2);
        } else if (e2) {
            return errorAnd(dt1);
        } else {
            return getValue(dt1.booleanValue() && dt2.booleanValue());
        }
    }

    @Override
    public Object getBufferedValue(StringBuilder sb, Environment env) {
        return plugin.getBufferedValue(sb, env);
    }
    
    IDatatype get(IDatatype dt1, IDatatype dt2){
        return gget(dt1, dt2, dt2);
    }
//        if (dt1.isList()){
//            return DatatypeMap.get(dt1, dt2);
//        }
//        if (dt1.getObject() != null){
//            if (dt1.getObject() instanceof Entity){
//                return get((Entity) dt1.getObject(), dt2.intValue());
//            }
//            if (dt1.getObject() instanceof Mapping){
//                return get((Mapping) dt1.getObject(), dt2.getLabel() );
//            }
//        }
//        return null;
//    }
    
    /**
     * Generic get with variable name and index
     */
    IDatatype gget(IDatatype dt, IDatatype var, IDatatype ind){
        if (dt.isList()){
            return DatatypeMap.get(dt, ind);
        }
        if (dt.getObject() != null){
            if (dt.getObject() instanceof Entity){
                return get((Entity) dt.getObject(), ind.intValue());
            }
            if (dt.getObject() instanceof Mappings){
                Mappings map = (Mappings) dt.getObject();
                if (map.size() == 0){
                    return null;
                }
                return get(map.get(0), var.getLabel() );
            }
            if (dt.getObject() instanceof Mapping){
                return get((Mapping) dt.getObject(), var.getLabel() );
            }
        }
        return null;
    }
    
    // ?x ?p ?y ?g
    IDatatype get(Entity ent, int n){
        Edge edge = ent.getEdge();
        switch (n){
            case 0: return nodeValue(edge.getNode(0));
            case 1: 
                Node var = edge.getEdgeVariable();
                return nodeValue((var == null) ? edge.getEdgeNode() : var);                  
            case 2: return nodeValue(edge.getNode(1));
            case 3: return nodeValue(ent.getGraph());
        }
        return null;
    }
    
    IDatatype nodeValue(Node n){
        return (IDatatype) n.getValue();
    }
    
    IDatatype get(Mapping m, String var){
        Node n = m.getNode(var);
        if (n == null){
            return null;
        }
        return nodeValue(n);
    }
    
    IDatatype reject(Environment env, IDatatype dtm){
        Object om = dtm.getObject();
        if (om != null &&  om instanceof Mapping){
            Mapping map = (Mapping) om;
            env.getMappings().reject(map);
        }
        return TRUE;
    }
    
    IDatatype count(IDatatype dt){
        if (dt.isList()){
            return DatatypeMap.size(dt);
        }
        Object obj = dt.getObject();
        if (obj == null){
            return null;
        }
        if (obj instanceof Mappings){
            return getValue(((Mappings)obj).size());
        }
        return null;
    }

    
}
