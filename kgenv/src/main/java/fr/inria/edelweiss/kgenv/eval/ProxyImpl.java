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
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import static fr.inria.edelweiss.kgram.api.core.ExprType.CONT;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.filter.Proxy;

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
    protected static IDatatype TRUE = DatatypeMap.TRUE;
    protected static IDatatype FALSE = DatatypeMap.FALSE;
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
    }

    public void start() {
        number = 0;
    }

    @Override
    public Object eval(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt1 = (IDatatype) o1;
        IDatatype dt2 = (IDatatype) o2;
        boolean b = true;

        try {
            switch (exp.oper()) {

                case NEQ:
                    b = !dt1.equals(dt2);
                    break;
                case IN:
                    return in(dt1, dt2);
                case EQ:
                    b = dt1.equals(dt2);
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
                    if (plugin != null) {
                        return plugin.eval(exp, env, p, dt1, dt2);
                    }
                    return null;

            }
        } catch (CoreseDatatypeException e) {
            return null;
        }

        return (b) ? TRUE : FALSE;
    }

    public Object function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {

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
                System.out.println(exp + " = " + dt);
                return TRUE;

            default:
                if (plugin != null) {
                    return plugin.function(exp, env, p, o1);
                }

        }
        return null;
    }

    public Object function(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt = (IDatatype) o1;
        IDatatype dt1 = (IDatatype) o2;
        boolean b;

        switch (exp.oper()) {

            case CONT:
                return getValue(dt.contains(dt1));

            case CONTAINS:
                if (!compatible(dt, dt1)) {
                    return null;
                }
                b = dt.getLabel().contains(dt1.getLabel());
                return (b) ? TRUE : FALSE;

            case STARTS:
                if (!compatible(dt, dt1)) {
                    return null;
                }
                b = dt.startsWith(dt1);
                return (b) ? TRUE : FALSE;

            case ENDS:
                if (!compatible(dt, dt1)) {
                    return null;
                }
                b = dt.getLabel().endsWith(dt1.getLabel());
                return (b) ? TRUE : FALSE;

            case SUBSTR:
                return substr(dt, dt1, null);

            case STRBEFORE:
                return strbefore(dt, dt1);

            case STRAFTER:
                return strafter(dt, dt1);

            case LANGMATCH:
                return langMatches(dt, dt1);

            case STRDT:
                if (SPARQLCompliant && !DatatypeMap.isSimpleLiteral(dt)) {
                    return null;
                }
                return DatatypeMap.createLiteral(dt.getLabel(), dt1.getLabel());

            case STRLANG:
                if (SPARQLCompliant && !DatatypeMap.isSimpleLiteral(dt)) {
                    return null;
                }
                return DatatypeMap.createLiteral(dt.getLabel(), null, dt1.getLabel());

            case REGEX: {
                if (!isStringLiteral(dt)) {
                    return null;
                }
                Processor proc = getProcessor(exp);
                b = proc.regex(dt.getLabel(), dt1.getLabel());
                return (b) ? TRUE : FALSE;
            }


            case XPATH: {
                // xpath(?g, '/book/title')
                Processor proc = getProcessor(exp);
                proc.setResolver(new VariableResolverImpl(env));
                IDatatype res = proc.xpath(dt, dt1);
                return res;
            }

            case EXTEQUAL: {
                boolean bb = StringHelper.equalsIgnoreCaseAndAccent(dt.getLabel(), dt1.getLabel());
                return (bb) ? TRUE : FALSE;
            }

            case EXTCONT: {
                boolean bb = StringHelper.containsWordIgnoreCaseAndAccent(dt.getLabel(), dt1.getLabel());
                return (bb) ? TRUE : FALSE;
            }

            default:
                if (plugin != null) {
                    return plugin.function(exp, env, p, o1, o2);
                }


        }

        return null;
    }

    @Override
    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {
        switch (exp.oper()) {

            case EXTERNAL:
                // user defined function with prefix/namespace
                // function://package.className
                Processor proc = getProcessor(exp);
                return proc.eval(args);

            case KGRAM:
            case EXTERN:
            case PROCESS:
                return plugin.eval(exp, env, p, args);

            case DEBUG:
                if (el == null) {
                    el = EvalListener.create();
                    env.getEventManager().addEventListener(el);
                }
                int i = 0;
                for (Object arg : args) {
                    Event e = EventImpl.create(Event.FILTER, exp.getExp(i++), arg);
                    env.getEventManager().send(e);
                }
                return TRUE;

            case CONCAT:
                return concat(args);
        }


        boolean b = true;
        IDatatype dt = null, dt1 = null;
        if (args.length > 0) {
            dt = (IDatatype) args[0];
        }
        if (args.length > 1) {
            dt1 = (IDatatype) args[1];
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
                if (args.length > 2) {
                    dt2 = datatype(args[2]);
                }
                return substr(dt, dt1, dt2);

            case CAST: // cast(?x, xsd:string, CoreseString)
                return dt.cast(dt1, datatype(args[2]));


            case STRREPLACE:

                if (args.length != 3) {
                    return null;
                }
                if (!isStringLiteral(dt)) {
                    return null;
                }
                return strreplace(exp, dt, dt1, datatype(args[2]));


            case SQL:
                // return ResultSet
                return sql(exp, env, args);

            default:
                if (plugin != null) {
                    return plugin.eval(exp, env, p, args);
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

    String encodeForUri(String str) {

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

    /**
     * literals with same lang return literal@lang all strings return string
     * else return literal error if not literal or string
     */
    IDatatype concat(Object[] args) {
        String str = "";
        String lang = null;

        if (args.length == 0) {
            return EMPTY;
        }

        StringBuilder sb = new StringBuilder();

        IDatatype dt = datatype(args[0]);
        boolean ok = true, hasLang = false, isString = true;
        if (dt.hasLang()) {
            hasLang = true;
            lang = dt.getLang();
        }

        for (Object obj : args) {

            dt = datatype(obj);

            if (!isStringLiteral(dt)) {
                return null;
            }

            if (dt.getStringBuilder() != null) {
                sb.append(dt.getStringBuilder());
            } else {
                sb.append(dt.getLabel());
            }
            //str += dt.getLabel();

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

        //str = sb.toString();

        if (ok && lang != null) {
            return DatatypeMap.createLiteral(sb.toString(), null, lang);
        } else if (isString) {
            return DatatypeMap.newStringBuilder(sb);
        } else {
            return DatatypeMap.createLiteral(sb.toString());
        }
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
        Walker walk = new Walker(exp, qNode, this, env);

        // apply the aggregate on current group Mapping, 
        env.aggregate(walk, p, exp.getFilter());

        Object res = walk.getResult();
        return res;
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
    Object sql(Expr exp, Environment env, Object[] args) {
        ResultSet rs;
        boolean isSort = false;
        if (args.length == 4) {
            // no driver
            rs = sql.sql(datatype(args[0]), datatype(args[1]), datatype(args[2]), datatype(args[3]));
        } else {
            if (args.length == 6) {
                try {
                    isSort = datatype(args[5]).isTrue();
                } catch (CoreseDatatypeException e) {
                }
            }
            // with driver
            rs = sql.sql(datatype(args[0]), datatype(args[1]), datatype(args[2]), datatype(args[3]), datatype(args[4]));
        }

        return new SQLResult(rs, isSort);
    }

    /**
     * ?x in (a b) ?x in (xpath())
     */
    Object in(IDatatype dt1, IDatatype dt2) {

        boolean error = false;

        if (dt2.isArray()) {

            for (IDatatype dt : dt2.getValues()) {
                try {
                    if (dt1.equals(dt)) {
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
                if (dt1.equals(dt2)) {
                    return TRUE;
                }
            } catch (CoreseDatatypeException e) {
                return null;
            }
        }

        return FALSE;
    }
}
