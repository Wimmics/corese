package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.function.template.*;
import fr.inria.corese.sparql.triple.function.core.*;
import fr.inria.corese.sparql.triple.function.aggregate.*;
import fr.inria.corese.sparql.triple.function.term.*;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.extension.*;
import fr.inria.corese.sparql.triple.function.script.*;
import fr.inria.corese.sparql.triple.function.proxy.*;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import java.util.ArrayList;
import java.util.List;
import fr.inria.corese.sparql.triple.cst.Keyword;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.kgram.api.core.ExpPattern;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import static fr.inria.corese.kgram.api.core.ExprType.XT_CREATE;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.parser.Access.Feature;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class Term extends Expression {

    public static final String NL = System.getProperty("line.separator");
    static final String RE_CHECK = "check";
    static final String RE_PARA = "||";
    public static final String RE_ALT = "|";
    public static final String RE_SEQ = "/";
    static final String OCST = "@{";
    static final String CCST = "}";
    static final String SPACE = " ";
    static final String STNOT = Keyword.STNOT;
    static final String SENOT = Keyword.SENOT;
    public static final String SEINV = "i";
    public static final String SREV = Keyword.SBE;
    public static final String SNOT = Keyword.SENOT;
    public static final String SEAND = Keyword.SEAND;
    static final String SEDIV = Keyword.SDIV;
    public static final String SEOR = Keyword.SEOR;
    public static final String SEQ = Keyword.SEQ;
    public static final String SNEQ = Keyword.SNEQ;
    public static final String STAR = "star";
    public static final String TEST = "test";
    static final String OPT = "opt";
    static final String PLUS = "plus";
    static final String XPATH = "xpath";
    static final String DIFFER = "differ";
    static final String ISDIFFER = "isDifferent";
    static final String SIM = "similarity";
    static final String SCORE = "score";
    static final String SBOUND = "bound";
    static final String EXIST = "exists";
    static final String LIST = "inList";
    static final String SERVICE = "service";
    // default processor to compile term
    static Processor processor;
    static final NSManager nsm = NSManager.create();
    // possibly dynamic processor to implement some functions: regex, ...
    Processor proc;
    Exist exist;
    Constant cname;
    public ArrayList<Expression> args = new ArrayList<Expression>();
    List<Expr> lExp;
    IDatatype[] arguments;
    // ast for let (((?x, ?y)) = select where)
    private ExpressionList nestedList;
    // additional system arg:
    Expression exp;
    // compiled kgram Exp
    ExpPattern pattern;
    boolean isFunction = false,
            isCount = false,
            isPlus = false;
    boolean isDistinct = false;
    boolean isShort = false;
    private boolean nested = false;
    String modality;
    int type = ExprType.UNDEF, oper = ExprType.UNDEF;
    int min = -1, max = -1;
    private int place = -1;
    private int arity = 0;

    static {
        processor = new Processor();
    }

    public Term() {
    }

    public Term(String name) {
        setName(name);
    }

    public Term(String name, Expression exp1) {
        setName(name);
        add(exp1);
    }

    public Term(String name, Expression exp1, Expression exp2) {
        setName(name);
        add(exp1);
        add(exp2);
    }

    public static Term create(String name, Expression exp1, Expression exp2) {
        //return new Term(name, exp1, exp2);
        return term(name, exp1, exp2);
    }

    public static Term term(String name, Expression exp1, Expression exp2) {
        switch (Processor.getOper(name)) {
            case ExprType.IN:
                return new In(name, exp1, exp2);
            case ExprType.OR:
                return new OrTerm(name, exp1, exp2);
            case ExprType.AND:
                return new AndTerm(name, exp1, exp2);

           
            case ExprType.EQ:
                return new EQ(name, exp1, exp2);
            case ExprType.NEQ:
                return new NEQ(name, exp1, exp2);
                
            case ExprType.EQUAL:
                return new Operation(name, exp1, exp2);
            case ExprType.NOT_EQUAL:
                return new Operation(name, exp1, exp2);    
                
            case ExprType.LE:
                return new LE(name, exp1, exp2);            
            case ExprType.LT:
                return new LT(name, exp1, exp2);
            case ExprType.GE:
                return new GE(name, exp1, exp2);
            case ExprType.GT:
                return new GT(name, exp1, exp2);

            case ExprType.PLUS:
                return new PlusTerm(name, exp1, exp2);
            case ExprType.MINUS:
                return new MinusTerm(name, exp1, exp2);   
            case ExprType.MULT:
                return new MultTerm(name, exp1, exp2);
            case ExprType.DIV:
                return new DivTerm(name, exp1, exp2);
                
            case ExprType.POWER:
                return new Operation(name, exp1, exp2);

            
            default:
                return new Term(name, exp1, exp2);
        }
    }

    public static Term create(String name, Expression exp1) {
        switch (Processor.getOper(name)) {
            case ExprType.NOT:
                return new NotTerm(name, exp1);
            default:
                return new Term(name, exp1);
        }
    }

    public static Term create(String name) {
        return new Term(name);
    }

    public static Term function(String name, String longName) {
        //Term fun = new Term(name); 
        Term fun = newFunction(name, longName);
        fun.setFunction(true);
        return fun;
    }

    public static Term function(String name) {
        return function(name, nsm.toNamespace(name));
    }

    static Term newFunction(String name, String longName) {
        switch (Processor.getOper(longName)) {
            // term as function: rq:plus(exp, exp)
            case ExprType.IN:
                return new In(name);
            case ExprType.OR:
                return new OrTerm(name);
            case ExprType.AND:
                return new AndTerm(name);

            case ExprType.EQ:
            case ExprType.NEQ:
            case ExprType.EQUAL:
            case ExprType.NOT_EQUAL:
            case ExprType.LT:
            case ExprType.LE:
            case ExprType.GE:
            case ExprType.GT:

            case ExprType.POWER:
            case ExprType.PLUS:
            case ExprType.MULT:
            case ExprType.MINUS:
            case ExprType.DIV:
                return new Operation(name);

            case ExprType.IF:
                return new IfThenElseTerm(name);
            case ExprType.BOUND:
            case ExprType.SAFE:
                return new Bound(name);
            case ExprType.COALESCE:
                return new Coalesce(name);
            case ExprType.EXIST:
                return new ExistFunction(name);

            case ExprType.DISPLAY:
            case ExprType.XT_DISPLAY:
            case ExprType.XT_PRINT:
            case ExprType.XT_PRETTY:
                return new Display(name);
                
            case ExprType.XT_XML:
            case ExprType.XT_JSON:
            case ExprType.XT_RDF:
                // return text format for Mappings
                return new ResultFormater(name);
                
            case ExprType.XT_ATTRIBUTES:
            case ExprType.XT_ATTRIBUTE:
            case ExprType.XT_HAS_ATTRIBUTE:
            case ExprType.XT_NODE_TYPE:
            case ExprType.XT_NODE_VALUE:
            case ExprType.XT_NODE_NAME:    
            case ExprType.XT_NODE_LOCAL_NAME:    
            case ExprType.XT_NODE_PARENT:    
            case ExprType.XT_NODE_DOCUMENT:    
            case ExprType.XT_NODE_ELEMENT:    
            case ExprType.XT_NODE_PROPERTY:    
            case ExprType.XT_ELEMENTS:
            case ExprType.XT_CHILDREN:
            case ExprType.XT_NODE_FIRST_CHILD:
            case ExprType.XT_TEXT_CONTENT:
            case ExprType.XT_NAMESPACE:
            case ExprType.XT_BASE:
            case ExprType.XT_XSLT:               
                return new XML(name);
 
            case ExprType.XT_SPIN:
                // return SPIN graph for query
                return new SPINFormater(name);
            case ExprType.EXTERNAL:
                return new Extern(name);
            case ExprType.HASH:
                return new HashFunction(name);
            case ExprType.STR:
            case ExprType.URI:
            case ExprType.STRLEN:
            case ExprType.UCASE:
            case ExprType.LCASE:
            case ExprType.ENCODE:
            case ExprType.XSDSTRING:
            case ExprType.LANG:
            case ExprType.CAST:
            case ExprType.CEILING:
            case ExprType.FLOOR:
            case ExprType.ABS:
            case ExprType.ROUND:
            case ExprType.DATATYPE:
            case ExprType.ISLITERAL:
            case ExprType.ISURI:
            case ExprType.ISBLANK:
            case ExprType.ISNUMERIC:
            case ExprType.ISWELLFORMED:
            case ExprType.ISLIST:
            case ExprType.ISUNDEFINED:
            case ExprType.ISSKOLEM:
            case ExprType.ISEXTENSION:
                return new UnaryFunction(name);

            case ExprType.CONCAT:
                return new Concat(name);
            case ExprType.CONTAINS:
            case ExprType.STRBEFORE:
            case ExprType.STRAFTER:
            case ExprType.STARTS:
            case ExprType.ENDS:
            case ExprType.XT_SPLIT:
                return new StrPredicate(name);
            case ExprType.REGEX:
            case ExprType.SUBSTR:
            case ExprType.STRREPLACE:
                return new BiTriFunction(name);
            case ExprType.STRLANG:
            case ExprType.STRDT:
            case ExprType.SAMETERM:
            case ExprType.LANGMATCH:
                return new BinaryFunction(name);
            case ExprType.FUUID:
            case ExprType.STRUUID:
                return new UUIDFunction(name);
            case ExprType.NOW:
            case ExprType.RANDOM:
                return new ZeroaryFunction(name);

            case ExprType.YEAR:
            case ExprType.MONTH:
            case ExprType.DAY:
            case ExprType.HOURS:
            case ExprType.MINUTES:
            case ExprType.SECONDS:
            case ExprType.TIMEZONE:
            case ExprType.TZ:
                return new DateFunction(name);

            case ExprType.MIN:
            case ExprType.MAX:
                return new AggregateMinMax(name);
            case ExprType.COUNT:
                return new AggregateCount(name);
            case ExprType.SUM:
            case ExprType.AVG:
                return new AggregateSumAvg(name);
            case ExprType.AGGLIST:
            case ExprType.AGGREGATE:
                return new AggregateList(name);

            case ExprType.GROUPCONCAT:
            case ExprType.STL_GROUPCONCAT:
                return new AggregateGroupConcat(name);

            case ExprType.AGGAND:
                return new AggregateAnd(name);
            case ExprType.SAMPLE:
                return new Aggregate(name);
            case ExprType.STL_AGGREGATE:
                return new AggregateTemplate(name);
                
            case ExprType.EXTCONT:
            case ExprType.EXTEQUAL:
            case ExprType.XPATH:
            case ExprType.XT_COMPARE:
                return new BinaryExtension(name);
                
            case ExprType.XT_REPLACE:
                return new TernaryExtension(name);
                
            case ExprType.UNDEF:
                return new Extension(name);
            case ExprType.XT_METHOD:
                return new MethodCall(name);
            case ExprType.XT_METHOD_TYPE:
                return new MethodTypeCall(name);
            case ExprType.EVAL:
                return new Eval(name);
            case ExprType.XT_ISFUNCTION:
                return new FunctionDefined(name);
            case ExprType.XT_EVENT:
                return new EventCall(name);
            case ExprType.APPLY:        
            case ExprType.FUNCALL:
                return new Funcall(name);
            case ExprType.JAVACALL:
                return new Javacall(name); 
            case ExprType.DSCALL:
                return new JavaDScall(name); 
            case ExprType.JAVACAST:
                return new JavaCast(name);         
            case ExprType.REDUCE:
                return new Reduce(name);
            case ExprType.SELF:
                return new Self(name);
            case ExprType.RETURN:
                return new Return(name);
            case ExprType.THROW:
            case ExprType.RESUME:    
                return new Throw(name);
            case ExprType.ERROR:
                return new ErrorFunction(name);

            case ExprType.BNODE:
            case ExprType.PATHNODE:
                return new BlankNode(name);

            case ExprType.MAPANY:
            case ExprType.MAPEVERY:
                return new MapAnyEvery(name);
            case ExprType.MAP:
            case ExprType.MAPLIST:
            case ExprType.MAPMERGE:
            case ExprType.MAPAPPEND:
            case ExprType.MAPFIND:
            case ExprType.MAPFINDLIST:
                return new MapFunction(name);

            case ExprType.SEQUENCE:
                return new Sequence(name);
            case ExprType.SET:
            case ExprType.UNSET:
            case ExprType.STATIC:
            case ExprType.STATIC_UNSET:
                return new SetFunction(name);

            case ExprType.LENGTH:
                //return new ZeroAry(name);
                return new PathLength(name);

            case ExprType.XT_GEN_GET:
            case ExprType.XT_LAST:
                return new GetGen(name);
                
            case ExprType.XT_GEN_REST:
                return new Rest(name);
                
            case ExprType.XT_HAS:
            case ExprType.XT_GET:
            case ExprType.XT_PATH:
                return new Get(name);
                
            case ExprType.XT_REVERSE:
            case ExprType.XT_FIRST:
            case ExprType.XT_REST:
                return new ListUnary(name);
            case ExprType.XT_SORT:
                return new ListSort(name);
            case ExprType.LIST:
                return new ListTerm(name);
            case ExprType.XT_MAP:
                return new MapTerm(name);
             case ExprType.XT_JSON_OBJECT:
                return new JSONTerm(name);    
                
            case ExprType.XT_COUNT:
                return new Size(name);
                
            case ExprType.XT_APPEND:
            case ExprType.XT_CONS:
            case ExprType.XT_MEMBER:
            case ExprType.XT_REMOVE:   
            case ExprType.XT_REMOVE_INDEX:   
                return new ListBinary(name);
                
            case ExprType.XT_SET:
            case ExprType.XT_ADD:
            case ExprType.IOTA:
                return new ListNary(name);
                
            case ExprType.XT_SWAP:
                return new Swap(name);
                
            case ExprType.XT_ITERATE:
                return new Iterate(name);

            case ExprType.DEBUG:
            case ExprType.SLICE:
            case ExprType.ENV:   
            case ExprType.XT_STACK:
            case ExprType.XT_RESULT:    
            case ExprType.XT_VISITOR:    
            case ExprType.XT_DATATYPE:
            case ExprType.XT_GET_DATATYPE_VALUE:
                return new SystemFunction(name);
                

            case ExprType.INDEX:
            case ExprType.XT_CONTENT:
            case ExprType.XT_DATATYPE_VALUE:
            case ExprType.XT_LOWERCASE:
            case ExprType.XT_UPPERCASE:
                return new UnaryExtension(name);
          
            case ExprType.XT_CAST:
                return new Cast(name, longName);
                
            case ExprType.XT_FOCUS:
                return new Focus(name);
                          
            case ExprType.STL_CONCAT:
                return new Concat(name);
                     
            case ExprType.XSDLITERAL:
            case ExprType.TURTLE:
                return new Turtle(name);
                
            case ExprType.STL_PREFIX:
            case ExprType.PROLOG:    
                return new Prefix(name);
                
            case ExprType.QNAME:
            case ExprType.XT_EXPAND:
            case ExprType.XT_DEFINE:
            case ExprType.XT_DOMAIN:
                return new Namespace(name);
                
            //case ExprType.STL_INDEX:
            case ExprType.STL_NUMBER:
            case ExprType.NUMBER:
                return new TemplateNumber(name);
                
            case ExprType.FOCUS_NODE:
                return new FocusNode(name);
                
            case ExprType.INDENT:
            case ExprType.STL_NL:
            case ExprType.STL_ISSTART: 
            case ExprType.STL_DEFINED:
            case ExprType.XT_MAPPINGS:
                return new TemplateAccess(name);
                
            case ExprType.APPLY_TEMPLATES:
            case ExprType.APPLY_TEMPLATES_ALL:
                return new ApplyTemplates(name); 
                
            case ExprType.APPLY_TEMPLATES_WITH:    
            case ExprType.APPLY_TEMPLATES_WITH_ALL:
                return new ApplyTemplatesWith(name);
                
            case ExprType.APPLY_TEMPLATES_WITH_GRAPH:           
            case ExprType.APPLY_TEMPLATES_WITH_NOGRAPH:
                return new ApplyTemplatesWithGraph(name);
                    
            case ExprType.CALL_TEMPLATE:
                return new CallTemplate(name);
                
            case ExprType.CALL_TEMPLATE_WITH:
                return new CallTemplateWith(name);
                
            case ExprType.STL_GET:
            case ExprType.STL_SET:
            case ExprType.STL_EXPORT:    
                return new GetSetContext(name);
                
            case ExprType.STL_CGET:
            case ExprType.STL_CSET:
                return new CGetSetContext(name);
                
            case ExprType.STL_FORMAT:
            case ExprType.FORMAT:
                return new TemplateFormat(name);
            case ExprType.STL_FUTURE:
                return new TemplateFuture(name);
            case ExprType.STL_PROCESS:
                return new TemplateProcess(name);
                
            case ExprType.STL_VISIT:
            case ExprType.STL_VISITED:
            case ExprType.STL_VGET:
            case ExprType.STL_VSET:
            case ExprType.STL_ERRORS:
            case ExprType.STL_ERROR_MAP:
            case ExprType.STL_VISITED_GRAPH:               
                return new TemplateVisitor(name);            
                
            case ExprType.LOAD:
            case ExprType.WRITE:
            case ExprType.READ:
            case ExprType.XT_HTTP_GET:
            case ExprType.XT_TUNE:
            case ExprType.SIM:                
            case ExprType.APP_SIM:                
            case ExprType.APPROXIMATE:
            case ExprType.DEPTH:
            case ExprType.XT_EDGES:
            case ExprType.XT_SUBJECTS:  
            case ExprType.XT_OBJECTS:    
            case ExprType.XT_EXISTS: 
            case ExprType.XT_VALUE:                
            case ExprType.XT_INSERT: 
            case ExprType.XT_DELETE: 
            case ExprType.XT_DEGREE:
            case ExprType.XT_MINDEGREE:
            case ExprType.XT_MINUS:
            case ExprType.XT_JOIN:
            case ExprType.XT_OPTIONAL:
            case ExprType.XT_UNION:
            case ExprType.XT_MERGE:
            case ExprType.XT_ENTAILMENT:    
            case ExprType.XT_SHAPE_GRAPH:    
            case ExprType.XT_SHAPE_NODE:    
            case ExprType.KGRAM: 
            case ExprType.STL_INDEX: 
            case ExprType.XT_TOGRAPH:
            case ExprType.XT_SYNTAX:
            case XT_CREATE:
                return new GraphSpecificFunction(name);
                
            case ExprType.XT_VALID_URI:
                return new IOFunction(name);
                
            case ExprType.XT_GRAPH:
            case ExprType.XT_SUBJECT:
            case ExprType.XT_OBJECT:
            case ExprType.XT_PROPERTY:
            case ExprType.XT_INDEX: 
            case ExprType.XT_NODE:              
            case ExprType.XT_VERTEX:              
                return new GraphFunction(name);
                
            case ExprType.XT_METADATA:
            case ExprType.XT_CONTEXT:  
            case ExprType.XT_QUERY:    
            //case ExprType.XT_MAPPINGS:    
            case ExprType.XT_FROM:    
            case ExprType.XT_NAMED:
            case ExprType.XT_NAME:
                return new Introspection(name);

            default:
                return new Term(name);
        }
    }

    public static Term list() {
        return Term.function(LIST);
    }

    public static Term function(String name, Expression exp) {
        Term t = function(name);
        t.add(exp);
        return t;
    }

    public static Term function(String name, Expression exp1, Expression exp2) {
        Term t = function(name, exp1);
        t.add(exp2);
        return t;
    }

    public static Term function(String name, Expression exp1, Expression exp2, Expression exp3) {
        Term t = function(name, exp1, exp2);
        t.add(exp3);
        return t;
    }

    @Override
    public String getLabel() {
        if (getLongName() != null) {
            return getLongName();
        }
        return name;
    }

    public void setCName(Constant c) {
        cname = c;
    }

    public Constant getCName() {
        return cname;
    }

    void setMin(int n) {
        min = n;
    }

    @Override
    public int getMin() {
        return min;
    }

    void setMax(int n) {
        max = n;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public boolean isCounter() {
        return (min != -1 || max != -1);
    }

    public void setModality(ExpressionList el) {
        setDistinct(el.isDistinct());
        setModality(el.getSeparator());
        setArg(el.getExpSeparator());
    }

    @Override
    public void setDistinct(boolean b) {
        isDistinct = b;
    }
    
    public Term distinct(boolean b) {
        setDistinct(b);
        return this;
    }

    @Override
    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public void setShort(boolean b) {
        isShort = b;
    }

    @Override
    public boolean isShort() {
        return isShort;
    }

    @Override
    public void setModality(String s) {
        modality = s;
    }

    @Override
    public String getModality() {
        return modality;
    }   

    public static Term negation(Expression exp) {
        //return new Term(SENOT, exp);
        return create(SENOT, exp);
    }

    @Override
    public boolean isTerm() {
        return true;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public String toRegex() {
        if (isCount()) {
            String str = paren(getArg(0).toRegex()) + "{";
            if (getMin() == getMax()) {
                str += getMin();
            } else {
                //if (getMin()!=0){
                str += getMin();
                //}
                str += ",";
                if (getMax() != Integer.MAX_VALUE) {
                    str += getMax();
                }
            }
            str += "}";
            return str;
        } else if (isPlus()) {
            return paren(getArg(0).toRegex()) + "+";
        } else if (isStar()) {
            return paren(getArg(0).toRegex()) + "*";
        } else if (isNot()) {
            return SNOT + paren(getArg(0).toRegex());
        } else if (isReverse()) {
            return SREV + paren(getArg(0).toRegex());
        } else if (isOpt()) {
            return paren(getArg(0).toRegex()) + "?";
        } else if (isSeq()) {
            if (getArg(1).isTest()) {
                return toTest();
            } else {
                return getArg(0).toRegex() + RE_SEQ + getArg(1).toRegex();
            }
        } else if (isAlt()) {
            return "(" + getArg(0).toRegex() + RE_ALT + getArg(1).toRegex() + ")";
        } else if (isPara()) {
            return getArg(0).toRegex() + RE_PARA + getArg(1).toRegex();
        }
        return toString();
    }

    String toTest() {
        return getArg(0).toRegex() + OCST + KeywordPP.FILTER + SPACE + getArg(1).getExpr() + CCST;
    }

    String paren(String s) {
        return "(" + s + ")";
    }
    
    @Override
    public ASTBuffer toString(ASTBuffer sb) {

        if (getName() == null) {
            return sb;
        }
        
        if (getName().equals(EXIST)) {
            return funExist(sb);
        }
        boolean isope = true;
        int n = args.size();

        if (isNegation(getName())) {
            sb.append(KeywordPP.OPEN_PAREN, SENOT);
            n = 1;
        } else if (isFunction()) {
            if (! getName().equals(LIST)) {
                functionName(sb);
            }
            isope = false;
        }

        sb.append(KeywordPP.OPEN_PAREN);

        if (isDistinct()) {
            // count(distinct ?x)
            sb.append(KeywordPP.DISTINCT, SPACE);
        }

        for (int i = 0; i < n; i++) {

            getArg(i).toString(sb);

            if (i < n - 1) {
                if (isope) {
                    sb.append(SPACE, getName(), SPACE);
                } else {
                    sb.append(KeywordPP.COMMA, SPACE);
                }
            }
        }

        if (getModality() != null && getName().equalsIgnoreCase(Processor.GROUPCONCAT)) {
            sb.append(Processor.SEPARATOR);
            Constant.toString(getModality(), sb);
        } else if (n == 0 && getName().equalsIgnoreCase(Processor.COUNT)) {
            // count(*)
            sb.append(KeywordPP.STAR);
        }

        sb.append(KeywordPP.CLOSE_PAREN);

        if (isNegation(getName())) {
            sb.append(KeywordPP.CLOSE_PAREN);
        }

        return sb;
    }
    
    void functionName(ASTBuffer sb) {
        if (sb.hasCompiler()) {
            // JavaCompiler pretty print query AST to generate Java code
            // we want Java function name
            // use case: let(select where) in a function
            String str = sb.getCompiler().getJavaName(getLongName());
            if (str != null) {
                sb.append(str);
                return;
            }
        }
        
        if (getCName() != null) {
            // when function name is prefix:namespace
            getCName().toString(sb);
        } else {
            sb.append(getName());
        }
    }

    ASTBuffer funExist(ASTBuffer sb) {
        if (isSystem()) {
            return ldscriptExist(sb);
        }
        return getExist().toString(sb);
    }

    ASTBuffer ldscriptExist(ASTBuffer sb) {
        Exp exp = getExist().get(0).get(0);
        sb.append("query(");
        exp.toString(sb);
        if (arity() > 0) {
            sb.append(", ");
            getArg(0).toString(sb);
        }
        sb.append(")");
        return sb;
    }
      
    @Override
    public void toJava(JavaCompiler jc, boolean arg) {
        jc.toJava(this, arg);
    }
    

    static boolean isNegation(String name) {
        return (name.equals(STNOT) || name.equals(SENOT));
    }

    @Override
    Bind validate(Bind env) {
        for (Expression exp : getArgs()) {
            exp.validate(env);
        }
        return env;
    }

    @Override
    public boolean validate(ASTQuery ast) {

        if (isExist()) {
            return getExist().validate(ast);
        }

        boolean ok = true;

        for (Expression exp : getArgs()) {
            boolean b = exp.validate(ast);
            ok = ok && b;
        }
        return ok;
    }

    // when it is not compiled !
    @Override
    public boolean isTermExist() {
        return getExist() != null;
    }

    @Override
    public boolean isNotTermExist() {
        return isNot() && getArg(0).isTermExist();
    }

    @Override
    public boolean isTermExistRec() {
        if (isTermExist()) {
            return true;
        }
        for (Expression exp : getArgs()) {
            if (exp.isTermExistRec()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isExist() {
        return getExist() != null;
    }
    
    @Override
    public boolean isRecExist() {
        return isTermExistRec();
//        if (isExist()) {
//            return true;
//        }
//        for (Expression exp : getArgs()) {
//            if (exp.isRecExist()) {
//                return true;
//            }
//        }
//        return false;
    }
    
    @Override
    void getPredicateList(List<Constant> list) {
        if (isNot()) {
            Constant cst = Constant.createResource(ASTQuery.getRootPropertyURI());
            if (! list.contains(cst)) {
                list.add(cst);
            }
        } else {
            for (Expression exp : getArgs()) {
                exp.getPredicateList(list);
            }
        }
    }

    @Override
    public boolean isSeq() {
        return getName().equals(RE_SEQ);
    }

    @Override
    public boolean isAnd() {
        return getName().equals(SEAND);
    }

    @Override
    public boolean isOr() {
        return getName().equals(SEOR);
    }

    @Override
    public boolean isAlt() {
        return getName().equals(RE_ALT);
    }

    @Override
    public boolean isPara() {
        return getName().equals(RE_PARA);
    }

    @Override
    public boolean isNot() {
        return getName().equals(SENOT);
    }

    public boolean isPathExp() {
        return getretype() != UNDEF;
    }

    @Override
    public boolean isInverse() {
        return getName().equals(SEINV) || super.isInverse();
    }

    @Override
    public boolean isReverse() {
        return getName().equals(SREV) || super.isReverse();
    }

    @Override
    public boolean isStar() {
        return isFunction(STAR);
    }

    @Override
    public boolean isOpt() {
        return isFunction(OPT);
    }

    @Override
    public boolean isTest() {
        return isFunction(TEST);
    }

    @Override
    public boolean isCheck() {
        return isFunction(RE_CHECK);
    }

    // final state in regexp
    @Override
    public boolean isFinal() {
        if (isStar() || isOpt()) {
            return true;
        }
        if (isAnd() || isAlt()) {
            for (Expression exp : args) {
                if (!exp.isFinal()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Return a copy of the reverse regex : p/q -> q/p
     *
     * use case: ?x exp <a>
     * walk the exp from <a> to ?x and set index = 1
     *
     */
    @Override
    public Expression reverse() {
        Term term = this;
        if (isSeq()) {
            term = Term.create(RE_SEQ, getArg(1).reverse(), getArg(0).reverse());
        } else if (isAlt() || isFunction()) {
            if (isAlt()) {
                term = Term.create(RE_ALT);
            } else {
                term = Term.function(getName());
            }
            for (Expression arg : getArgs()) {
                term.add(arg.reverse());
            }

            term.copy(this);
        }
        return term;
    }

    @Override
    void getConstants(List<Constant> list) {
        if (isNot()) {
            // ! p is a problem because we do not know the predicate nodes ...
            // let's return top level property, it subsumes all properties
            Constant.rootProperty.getConstants(list);
        } else {
            for (Expression e : getArgs()) {
                e.getConstants(list);
            }
        }
    }

    void copy(Term t) {
        setMax(t.getMax());
        setMin(t.getMin());
        setPlus(t.isPlus());
        setExpr(t.getExpr());
        setShort(t.isShort());
        setDistinct(t.isDistinct());
    }
    
    @Override
    public Term duplicate() {
        try {
            Term t = getClass().newInstance();
            fill(t);
            t.setArgs(new ArrayList<>());
            for (Expression e : getArgs()) {
                t.add(e.duplicate());
            }
            return t;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Term.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }
       
    /**
     * Copy as is except exists where BGP is copied
     * TODO: check all subclasses of TermEval with special data to be copied
     */
    @Override
    public Term copy() {
        if (isRecExist()) {
            return copyExist();
        }
        else {
            return this;
        }
    }
    
    Term copyExist() {
        try {
            Term t = getClass().newInstance();
            for (Expression exp : getArgs()) {
                Expression ee = exp.copy();
                t.add(ee);
            }            
            complete(t);          
            return t;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Term.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }
    
    public void basicFill(Term t){
        t.setName(getName());
        t.setCName(getCName());
        t.setLongName(getLongName());
        t.setArity(defineArity());
        t.setType(type());
        t.setOper(oper());
        t.setFunction(isFunction());
        t.setModality(getModality());
        t.setDistinct(isDistinct());
    }
    
     public void fill(Term term){
        basicFill(term);
        term.setArg(getArg());
        term.setArgs(getArgs());
        term.setExpList(getExpList());
    }
    
    public void complete(Term t) {
        basicFill(t);       
        // t.setProcessor(getProcessor());
        //t.setArguments();
        t.setArg(getArg());
        if (isExist()) {
            t.setExist(getExist().copy().getExist());
        }
    }
    

    /**
     * ^(p/q) -> ^q/^p
     *
     * and translate()
     *
     * inside reverse, properties (and ! prop) are setReverse(true)
     */
    @Override
    public Expression transform(boolean isReverse) {
        Term term = this;
        Expression exp;
        boolean trace = !true;

        if (isNotOrReverse()) {
            exp = translate();
            exp = exp.transform(isReverse);
            exp.setretype(exp.getretype());
            return exp;
        }

        if (isReverse()) {
            // Constant redefine transform()
            exp = getArg(0).transform(!isReverse);
            exp.setretype(exp.getretype());
            return exp;
        } else if (isReverse && isSeq() && !getArg(1).isTest()) {
            term = Term.create(getName(), getArg(1).transform(isReverse),
                    getArg(0).transform(isReverse));
        } else {
            if (isFunction()) {
                term = Term.function(getName());
            } else {
                term = Term.create(getName());
            }

            for (Expression arg : getArgs()) {
                term.add(arg.transform(isReverse));
            }

            switch (getretype()) {

                case NOT:
                    term.setReverse(isReverse);
                    break;

                case PARA:
                case OPTION:
                    // additional argument for checking
                    Term t = Term.function(RE_CHECK, term);
                    t.setretype(CHECK);
                    term.add(t);
                    break;


            }

            term.copy(this);
        }
        term.setretype(term.getretype());
        return term;
    }

    /**
     * this term is one of: ! (^ p) -> ^ !(p) ! (p | ^q) -> (!p) | ^ (!q)
     */
    @Override
    public Expression translate() {
        Expression exp = getArg(0);

        if (exp.isReverse()) {
            Expression e1 = Term.negation(exp.getArg(0));
            Expression e2 = Term.function(SREV, e1);
            return e2;
        }

        if (exp.isAlt()) {
            Expression std = null, rev = null;
            for (int i = 0; i < exp.getArity(); i++) {
                Expression ee = exp.getArg(i);
                if (ee.isReverse()) {
                    rev = add(RE_ALT, rev, Term.negation(ee.getArg(0)));
                } else {
                    std = add(RE_ALT, std, Term.negation(ee));
                }
            }
            Expression res = null;
            if (std != null) {
                res = std;
            }
            if (rev != null) {
                res = add(RE_ALT, res, Term.function(SREV, rev));
            }
            return res;
        }

        return this;
    }

    /**
     * ! (p1 | ^p2)
     */
    @Override
    public boolean isNotOrReverse() {
        if (!isNot()) {
            return false;
        }
        Expression ee = getArg(0);
        if (ee.isReverse()) {
            return true;
        }
        if (ee.isAlt()) {
            for (int i = 0; i < ee.getArity(); i++) {
                if (ee.getArg(i).isReverse()) {
                    return true;
                }
            }
        }
        return false;
    }

    Expression add(String ope, Expression e1, Expression e2) {
        if (e1 == null) {
            return e2;
        } else {
            return Term.create(ope, e1, e2);
        }
    }

    /**
     * Length of shortest path that matches the regexp
     */
    @Override
    public int regLength() {
        if (isStar()) {
            return 0; //getArg(0).length();
        } else {
            return length();
        }
    }

    @Override
    public int length() {
        if (isSeq()) {
            return getArg(0).length() + getArg(1).length();
        } else if (isAlt()) {
            return Math.min(getArg(0).length(), getArg(1).length());
        } else if (isNot()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isPlus() {
        return isPlus;
    }

    void setPlus(boolean b) {
        isPlus = b;
    }

    public boolean isCount() {
        return isCount;
    }

    void setCount(boolean b) {
        isCount = b;
    }

    @Override
    public boolean isTerm(String oper) {
        return name.equals(oper);
    }

    @Override
    public boolean isFunction() {
        return isFunction;
    }

    @Override
    public boolean isFuncall() {
        return isFunction;
    }

    public void setFunction(boolean b) {
        isFunction = b;
    }

    @Override
    public boolean isFunction(String str) {
        return isFunction && getName().equals(str);
    }

    @Override
    public boolean isType(ASTQuery ast, int type) {
        return isType(ast, null, type);
    }

    /**
     * 1. Is the exp of type aggregate or bound ? 2. When var!=null: if exp
     * contains var return false (sem checking)
     */
    @Override
    public boolean isType(ASTQuery ast, Variable var, int type) {
        if (isFunction()) {
            if (isType(getName(), type)) {
                return true;
            }
        } else if (isOr()) {
            // if type is BOUND : return true
            if (isType(getName(), type)) {
                return true;
            }
        }
        for (Expression arg : getArgs()) {
            if (var != null && arg == var && type == BOUND) {
                // it is not bound() hence we return false
                return false;
            }
            if (arg.isType(ast, type)) {
                return true;
            }
        }
        return false;
    }

    boolean isType(String name, int type) {
        switch (type) {
            case Expression.ENDFILTER:
                return name.equalsIgnoreCase(SIM) || name.equalsIgnoreCase(SCORE);
            case Expression.POSFILTER:
                return isAggregate(name);
            case Expression.BOUND:
                // see compiler
                return name.equalsIgnoreCase(SBOUND) || name.equals(SEOR);
        }
        return false;
    }

    public boolean isAggregate(String name) {
        for (String n : Processor.aggregate) {
            if (n.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRecAggregate() {
        if (isAggregate(getLabel())) {
            return true;
        }
        for (Expr exp : getExpList()) {
            if (exp.isRecAggregate()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAggregate() {
        return isAggregate(name);
    }

    @Override
    public boolean isFunctional() {
        if (!isFunction()) {
            return false;
        }
        String str = getLabel();
        return (str.equals(Processor.UNNEST)
                || str.equals(Processor.KGUNNEST)
                || str.equals(Processor.SQL)
                //|| str.equals(XPATH)
                || //str.equals(Processor.SPARQL) ||
                str.equals(Processor.EXTERN));
    }

    @Override
    public boolean isBound() {
        if (isFunction()) {
            return getName().equalsIgnoreCase(Processor.BOUND);
        } else {
            for (int i = 0; i < getArity(); i++) {
                if (getArg(i).isBound()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getArity() {
        return args.size();
    }

    public void setArgs(ArrayList<Expression> list) {
        args = list;
    }

    @Override
    public ArrayList<Expression> getArgs() {
        return args;
    }

    public void add(Expression exp) {
        args.add(exp);
    }

    public void add(int i, Expression exp) {
        args.add(i, exp);
    }

    public void setArg(int i, Expression exp) {
        args.set(i, exp);
    }

    @Override
    public Expression getArg(int n) {
        if (n > args.size() - 1) {
            return null;
        }
        return args.get(n);
    }
       
    @Override
    public Expression getBasicArg(int n) {
        return args.get(n);
    }

    public String getOper() {
        return getName();
    }

    public void setOper(String str) {
        setName(str);
    }

    /**
     * use case: select fun(?x) as ?y rewrite occurrences of ?y as fun(?x)
     * Exception: do not rewrite in case of aggregate: foo(?x) as ?y sum(?y) as
     * ?z
     */
    @Override
    public Expression process(ASTQuery ast) {
        if (isAggregate() || isFunctional()) { //(ast.isKgram() && isFunctional())){
            return this;
        }
        for (int i = 0; i < args.size(); i++) {
            Expression exp = getArg(i).process(ast);
            if (exp == null) {
                return null;
            }
            setArg(i, exp);
        }
        return this;
    }

    
    /**
     * Variables of filter and filter exists
     * Runtime version (Query)
     */
    @Override
    public void getVariables(List<String> list, boolean excludeLocal) {
        for (Expression ee : getArgs()) {
            ee.getVariables(list, excludeLocal);
        }
        if (oper() == ExprType.EXIST && getPattern() != null) {
            // runtime: compiled kgram Exp
            // return exists BGP variables except right part of minus
            // **and** filter variables **and** recursively in sub exists
            // use case: list of filter variables relevant for placing a filter in a BGP when sorting
            // see kgram QuerySorter sortFilter()
            getPattern().getVariables(list, excludeLocal);
        }
        else if (getExist() != null) {
            // compile time: return subscope variables:  surely bound in exists {}
            // when exists { filter f }, variables of filter f are not returned
            List<Variable> varList = getExist().getSubscopeVariables();
            for (Variable var : varList) {
                var.getVariables(list, excludeLocal);
            }
        }
    }
    
    /**
     * Filter variables
     * if scope.isExist()  collect exists {} variables, default is true
     * if scope.isFilter() collect filter variables within exists { .. filter f } default is false
     * compile time version (AST)
     */
    @Override
    void getVariables(VariableScope scope, List<Variable> list) {
        for (Expression ee : getArgs()) {
            ee.getVariables(scope, list);
        }
        if (getExist() != null && scope.isExist()) {
            getExist().getVariables(scope, list);
        }
    }

    // this = xt:fun(?x, ?y)
    public List<Variable> getFunVariables() {
        ArrayList<Variable> list = new ArrayList<>();
        for (Expression exp : getArgs()) {
            if (exp.isVariable()) {
                list.add(exp.getVariable());
            }
        }
        return list;
    }

    @Override
    public String getShortName() {
        if (proc == null || proc.getShortName() == null) {
            return getName();
        }
        return proc.getShortName();
    }

    @Override
    public Expr getExp(int i) {
        return lExp.get(i);
    }

    @Override
    public void setExp(int i, Expr e) {
        if (i < lExp.size()) {
            lExp.set(i, e);
        } else if (i == lExp.size()) {
            lExp.add(i, e);
        }
    }

    void setArguments() {
        if (lExp == null) {
            lExp = new ArrayList<Expr>();
            for (Expr e : getArgs()) {
                lExp.add(e);
            }
        }
    }

    @Override
    public int arity() {
        if (lExp == null) {
            return args.size();
        }
        return lExp.size();
    }

    @Override
    public Expression getArg() {
        return exp;
    }

    public void setArg(Expression e) {
        exp = e;
    }

    @Override
    public List<Expr> getExpList() {
        return lExp;
    }

    @Override
    public IDatatype[] getArguments(int n) {
        if (arguments == null) {
            arguments = new IDatatype[n];
        } else {
            java.util.Arrays.fill(arguments, null);
        }
        return arguments;
    }

    public void setExpList(List<Expr> l) {
        lExp = l;
    }

    @Override
    public ExpPattern getPattern() {
        return pattern;
    }

    public void setPattern(ExpPattern pat) {
        pattern = pat;
    }

    void setExist(Exist exp) {
        exist = exp;
    }

    @Override
    public Exist getExist() {
        return exist;
    }

    public Exist getExistPattern() {
        return exist;
    }

    public Exp getExistContent() {
        if (exist == null) {
            return null;
        }
        return exist.getContent();
    } 
    
    public Exp getExistBGP() {
        if (exist == null) {
            return null;
        }
        return exist.getBGP();
    }
    
    public void setExistBGP(Exp exp) {
        if (exist != null) {
            exist.setBGP(exp);
        }
    }

    // Exp
    @Override
    public Expression prepare(ASTQuery ast) throws EngineException {
        if (proc != null) {
            return this;
        }

        //proc = new Processor(this);
        proc = processor;
        proc.type(this, ast);
        
        int i = 0;
        for (Expression exp : getArgs()) {
            exp.prepare(ast);
        }

        // May create a specific Processor to manage this specific term
        // and overload proc field
        // Use case: regex, external fun, etc.
        proc.prepare(this, ast);

        if (getArg() != null) {
            getArg().prepare(ast);
        }
        typecheck(ast);
        return this;
    }
    
   
    
    void checkFeature(Feature feat, ASTQuery ast, String mess) throws EngineException  {
        if (reject(feat, ast)) {
            throw new SafetyException(mess);
        }
    }
    
    boolean reject(Feature feat, ASTQuery ast) {
        return Access.reject(feat, ast.getLevel());
    }

    @Override
    public boolean typecheck(ASTQuery ast) {
        if (getArity() < defineArity()) {
            ast.addError("Arity error: ", this);
            ast.setFail(true);
            return false;
        }
        return true;
    }

    public int defineArity() {
        return arity;
    }

    public void setArity(int n) {
        arity = n;
    }

    /**
     * Is this term a function signature ?
     *
     */
    boolean isFunctionSignature() {
        return hasExpression()
                && getExpression().oper() == ExprType.FUNCTION
                && this == getExpression().getFunction();
    }

    @Override
    public int type() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int oper() {
        return oper;
    }

    @Override
    public void setOper(int n) {
        oper = n;
    }

    public Processor getProcessor() {
        // TODO Auto-generated method stub
        return proc;
    }

    void setProcessor(Processor p) {
        proc = p;
    }

    @Override
    public Term copy(Variable o, Variable n) {
        Term f = null;
        if (isFunction()) {
            f = function(getName());
            f.setLongName(getLongName());
            f.setModality(getModality());
            if (getArg() != null) {
                f.setArg(getArg().copy(o, n));
            }
        } else {
            f = Term.create(getName());
        }
        for (Expression e : getArgs()) {
            Expression ee = e.copy(o, n);
            f.add(ee);
        }
        return f;
    }

    @Override
    public void visit(ExpressionVisitor v) {
        v.visit(this);
    }

    /**
     * @return the isExport
     */
    @Override
    public boolean isPublic() {
        return false;
    }

    /**
     * @param isExport the isExport to set
     */
    @Override
    public void setPublic(boolean isExport) {
    }

    @Override
    public Term getTerm() {
        return this;
    }
    
    // PRAGMA: this is term(exists{}) or not(term(exists{}))
    @Override
    public Term getTermExist() {
        if (isNot()) {
            return getArg(0).getTermExist();
        }
        return this;
    }

    /**
     * @return the nested
     */
    public boolean isNested() {
        return nested;
    }

    /**
     * @param nested the nested to set
     */
    public void setNested(boolean nested) {
        this.nested = nested;
    }

    /**
     * @return the nestedList
     */
    public ExpressionList getNestedList() {
        return nestedList;
    }

    /**
     * @param nestedList the nestedList to set
     */
    public void setNestedList(ExpressionList nestedList) {
        this.nestedList = nestedList;
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        return eval.function(this, env, p);
    }
}