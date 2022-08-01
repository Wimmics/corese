package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.function.script.Let;
import fr.inria.corese.sparql.triple.function.script.Function;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.function.SQLFun;
import fr.inria.corese.sparql.datatype.function.VariableResolver;
import fr.inria.corese.sparql.datatype.function.XPathFun;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.kgram.api.core.ExpPattern;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import static fr.inria.corese.kgram.api.core.ExprType.IS_TRIPLE;
import static fr.inria.corese.kgram.api.core.ExprType.OBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.PREDICATE;
import static fr.inria.corese.kgram.api.core.ExprType.SUBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.TRIPLE;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.HashMap;
import java.util.logging.Level;

public class Processor {
	private static Logger logger = LoggerFactory.getLogger(Processor.class); 
        
        static Class[] noargs = new Class[0];

	static final String functionPrefix = KeywordPP.CORESE_PREFIX;
        static final String DOM      = NSManager.DOM;
        static final String EXT      = NSManager.EXT;
        static final String EXT_PREF = NSManager.EXT_PREF + ":";
        static final String SPARQL   = NSManager.SPARQL;       
        static final String KGRAM    = NSManager.KGRAM;
        static final String KPREF    = NSManager.KPREF+":";
        static final String STL      = NSManager.STL;
        static final String CUSTOM   = NSManager.CUSTOM;
        
	public static final String BOUND    = "bound";
	public static final String COUNT    = "count";
	public static final String INLIST   = Term.LIST;
	public static final String XT_MAP      = EXT+"map";
	public static final String XT_JSON_OBJECT      = EXT+"jsonobject";
	public static final String XT_LIST     = EXT+"list";
	public static final String XT_TOLIST   = EXT+"toList";
	public static final String XT_IOTA     = EXT+"iota";
	public static final String XT_ITERATE = EXT+"iterate";
	public static final String XT_REVERSE  = EXT+"reverse";
	public static final String XT_APPEND   = EXT+"append";
	public static final String XT_MEMBER   = EXT+"member";
	public static final String XT_MERGE    = EXT+"merge";
	public static final String XT_SORT     = EXT+"sort";
	public static final String XT_RESULT   = EXT+"result";
 	public static final String XT_COMPARE  = EXT+"compare";
 	public static final String XT_VISITOR  = EXT+"visitor";
 	public static final String XT_REPLACE  = EXT+"replace";
 	public static final String XT_LOWERCASE= EXT+"lowerCase";
 	public static final String XT_UPPERCASE= EXT+"upperCase";
       
	public static final String IN  	 = "in";

	private static final String SAMETERM = "sameTerm";
	private static final String MIN = "min";
	private static final String MAX = "max";
	private static final String SUM = "sum";
	private static final String AVG = "avg";
	private static final String ISURI = "isURI";
	private static final String ISIRI = "isIRI";
	private static final String ISUNDEF = "isUndefined";
	public static final String ISWELLFORMED = "isWellFormed";
	public static final String ISBLANK = "isBlank";
	private static final String ISLITERAL = "isLiteral";
	private static final String ISLIST = "isList";
	private static final String ISNUMERIC = "isNumeric";
	private static final String LANG = "lang";
	private static final String REGEX = "regex";
        public static final String APPROXIMATE = "approximate";
        private static final String APP_SIM = "sim";
	public static  final String MATCH = "match";
	private static final String LANGMATCH = "langMatches";
	public static final String STRDT = "strdt";
	private static final String STRLANG = "strlang";
	public static final String IF = "if";
	public static final String COALESCE = "coalesce";
	public static final String BNODE = "bnode";
	public static final String GROUPCONCAT = "group_concat";
	static final String SEPARATOR = "; separator=";
	private static final String SAMPLE = "sample";
        
	private static final String JAVACALL = "java";
	private static final String FUNCALL  = "funcall";
	private static final String EVAL  = "eval";
	public static final String RETURN  = "return";
	public static final String SEQUENCE  = "sequence";
	public static final String SET     = "set";
	public static final String STATIC     = "static";
	public static final String LET     = "let";
        public static final String FOR             = "for";
	private static final String MAP     = "map";
	static final String MAPLIST = "maplist";
	static final String MAPFUN = "mapfun";
	private static final String MAPMERGE = "mapmerge";
	private static final String MAPAPPEND = "mapappend";
	private static final String MAPSELECT = "mapselect";
	private static final String MAPFIND   = "mapfind";
	private static final String MAPFINDLIST   = "mapfindlist";
	static final String APPLY   = "apply";
	static final String REDUCE  = "reduce";
        
        public static final String STOP        = "stop";
        public static final String THROW        = "throw";
        public static final String TRY_CATCH     = "trycatch";
        public static final String XT_GET_DATATYPE_VALUE     = EXT + "getDatatypeValue";
        private static final String XT_SELF     = EXT + "self";
        public static final String XT_FIRST    = EXT + "first";
        public static final String XT_REST     = EXT + "rest";
        public static final String XT_GET      = EXT + "get";
        public static final String XT_PATH     = EXT + "path";
        public static final String XT_HAS      = EXT + "has";
        public static final String XT_REMOVE   = EXT + "remove";
        public static final String XT_REMOVE_INDEX = EXT + "removeindex";
        public static final String XT_GEN_REST  = EXT + "grest";
        static final String FUN_XT_GREST        = EXT_PREF + "grest";
        public static final String XT_GEN_GET   = EXT + "gget";
        static final String XT_LAST             = EXT + "last";  
        static final String FUN_XT_LAST         = EXT_PREF + "last";       
        static final String FUN_XT_GGET         = EXT_PREF + "gget";
        static final String FUN_XT_GET          = EXT_PREF + "get";
        public static final String XT_SET      = EXT + "set";
        public static final String XT_CONS      = EXT + "cons";        
        public static final String XT_ADD       = EXT + "add";
        private static final String XT_SWAP     = EXT + "swap";
        private static final String XT_MAPPING  = EXT + "mapping";
        public static final String XT_SIZE      = EXT + "size";      
        private static final String XT_FOCUS    = EXT + "focus";
        private static final String XT_TOGRAPH    = EXT + "tograph";
        private static final String XT_GRAPH    = EXT + "graph";
        private static final String XT_SUBJECT  = EXT + "subject";
        private static final String XT_PROPERTY = EXT + "property";
        private static final String XT_OBJECT   = EXT + "object";
        private static final String XT_VALUE    = EXT + "value";
        private static final String XT_INDEX    = EXT + "index";
        private static final String XT_CAST     = EXT + "cast";        
        private static final String XT_REJECT   = EXT + "reject";
        private static final String XT_VARIABLES= EXT + "variables";
        public static final String XT_EDGES     = EXT + "edges";
        private static final String XT_NAME     = EXT + "name";        
        private static final String XT_QUERY    = EXT + "query";
        private static final String XT_AST      = EXT + "ast";
        private static final String XT_CONTEXT  = EXT + "context";
        private static final String XT_METADATA = EXT + "metadata";
        private static final String XT_ANNOTATION= EXT + "annotation";        
        private static final String XT_PREFIX   = EXT + "prefix";        
        private static final String XT_NSMANAGER= EXT + "nsmanager";        
        private static final String XT_FROM     = EXT + "from";        
        private static final String XT_NAMED    = EXT + "named";        
        private static final String XT_TRIPLE   = EXT + "triple";
        public static  final String XT_MAIN      = EXT + "main";
        public static  final String FUN_XT_MAIN  = EXT_PREF + "main";
        private static final String XT_ENTAILMENT = EXT + "entailment";
        private static final String XT_SHAPE_GRAPH = EXT + "shaclGraph";
        private static final String XT_SHAPE_NODE  = EXT + "shaclNode";
        private static final String XT_DATATYPE = EXT + "datatype";
        private static final String XT_KIND     = EXT + "kind";
        private static final String XT_METHOD   = "method";
        private static final String XT_METHOD_TYPE= EXT + "method";
        private static final String XT_EXISTS   = EXT + "exists";
        private static final String XT_INSERT   = EXT + "insert";
        private static final String XT_DELETE   = EXT + "delete";
        private static final String XT_DEGREE   = EXT + "degree";
        private static final String XT_MINDEGREE   = EXT + "mindegree";
       

	private static final String PLENGTH = "pathLength";
	private static final String KGPLENGTH = KGRAM + "pathLength";
	private static final String PWEIGHT = "pathWeight";
	private static final String KGPWEIGHT = KGRAM +"pathWeight";

	private static final String DATATYPE = "datatype";
	private static final String STR = "str";
	private static final String XSDSTRING = RDFS.XSD + "string";
	private static final String URI = "uri";
	private static final String IRI = "iri";

	private static final String SELF = "self";
	private static final String DEBUG = EXT+"debug";
	private static final String TRACE = EXT+"trace";

	static final String EXTERN 	= "extern";
	public static final String XPATH= Term.XPATH;
	static final String SQL 	= "sql";
	static final String KGXPATH     = KGRAM + "xpath";
	static final String KGSQL 	= KGRAM + "sql";
	static final String PROVENANCE 	= KGRAM + "provenance";
	static final String INDEX 	= KGRAM + "index";
	static final String ID          = KGRAM + "id";
	static final String TIMESTAMP 	= KGRAM + "timestamp";
	static final String TEST 	= KGRAM + "test";
	static final String STORE 	= KGRAM + "store";
	public static final String DESCRIBE 	= KGRAM + "describe";
	public static final String QUERY 	= KGRAM + "query";
	public static final String EXTENSION 	= KGRAM + "extension";
	public static final String KGEXTENSION 	= KPREF + "extension";
	
	static final String READ 		= KGRAM + "read"; 
	static final String WRITE 		= KGRAM + "write"; 
	static final String PPRINT 		= KGRAM + "pprint"; 
	static final String PPRINTWITH 		= KGRAM + "pprintWith"; 
	static final String PPRINTALL		= KGRAM + "pprintAll"; 
	static final String PPRINTALLWITH	= KGRAM + "pprintAllWith"; 
	static final String TEMPLATE		= KGRAM + "template"; 
	static final String TEMPLATEWITH	= KGRAM + "templateWith"; 
	static final String KG_EVAL 		= KGRAM + "eval";
        static final String PROLOG 		= KGRAM + "prolog";

	public static final String AGGREGATE    = "aggregate"; 
	public static final String STL_AGGREGATE    = STL + "aggregate"; 
	public static final String STL_DEFAULT      = STL + "default"; 
	public static final String STL_PROCESS      = STL + "process"; 
	public static final String STL_PROCESS_URI  = STL + "processURI"; 
        static final String FOCUS_NODE              = STL + "getFocusNode";
        
        static final String APPLY_TEMPLATES         = STL + "apply-templates";
        static final String APPLY_TEMPLATES_WITH    = STL + "apply-templates-with";
        static final String ATW                     = STL + "atw";
  	static final String APPLY_TEMPLATES_ALL     = STL + "apply-templates-all";        
  	static final String APPLY_TEMPLATES_GRAPH   = STL + "apply-templates-graph";        
  	static final String APPLY_TEMPLATES_NOGRAPH = STL + "apply-templates-nograph";        
  	static final String APPLY_TEMPLATES_WITH_ALL= STL + "apply-templates-with-all";        
   	static final String APPLY_TEMPLATES_WITH_GRAPH= STL + "apply-templates-with-graph";        
   	static final String APPLY_TEMPLATES_WITH_NOGRAPH= STL + "apply-templates-with-nograph";        
       
        // deprecated:
        static final String APPLY_ALL_TEMPLATES     = STL + "apply-all-templates";
        static final String APPLY_ALL_TEMPLATES_WITH= STL + "apply-all-templates-with";
        
        static final String STL_TEMPLATE            = STL + "template";
        static final String CALL_TEMPLATE           = STL + "call-template";
        static final String CALL_TEMPLATE_WITH      = STL + "call-template-with";
        static final String CTW                     = STL + "ctw";
	static final String STL_TURTLE              = STL + "turtle"; 
	static final String STL_STRIP               = STL + "strip"; 
	static final String STL_URI                 = STL + "uri"; 
	static final String STL_URILITERAL          = STL + "uriLiteral"; 
	static final String STL_XSDLITERAL          = STL + "xsdLiteral"; 
	static final String STL_PROLOG              = STL + "prolog"; 
	static final String STL_LEVEL               = STL + "level"; 
        static final String STL_DEFINE              = STL + "define";
        static final String DEFINE                  = "define";
        public static final String FUNCTION                = "function";
        static final String PACKAGE                 = "package";
        static final String EXPORT                  = "export";
        static final String LAMBDA                  = "lambda";
        static final String ERROR                   = "error";
        static final String MAPANY                  = "mapany";
        static final String MAPEVERY                = "mapevery";
      
        static final String STL_PREFIX              = STL + "prefix";
 	static final String STL_INDENT              = STL + "indent";
 	static final String STL_SELF                = STL + "self";
 	static final String STL_LOAD                = STL + "load";
 	static final String STL_IMPORT              = STL + "import";
	static final String STL_ISSTART             = STL + "isStart"; 
	static final String STL_SET                 = STL + "set"; 
	static final String STL_GET                 = STL + "get";
	static final String STL_HASGET              = STL + "getp";
        static final String STL_CSET                = STL + "cset"; 
	static final String STL_CGET                = STL + "cget";         
	static final String STL_EXPORT              = STL + "export"; 
	static final String STL_VSET                = STL + "vset"; 
	static final String STL_VGET                = STL + "vget"; 
        static final String STL_VISIT               = STL + "visit"; 
	static final String STL_ERRORS              = STL + "errors";
	static final String STL_ERROR_MAP           = STL + "errormap";        
	static final String STL_VISITED             = STL + "visited";        
	static final String STL_VISITED_GRAPH       = STL + "visitedGraph";        
	static final String STL_BOOLEAN             = STL + "boolean"; 
        
	public static final String STL_GROUPCONCAT  = STL + "group_concat"; 
	public static final String STL_CONCAT       = STL + "concat"; 
	public static final String STL_NL           = STL + "nl"; 
	public static final String STL_AGGAND       = STL + "agg_and";
	public static final String STL_AGGLIST      = STL + "agg_list";
	public static final String STL_AND          = STL + "and";
	public static final String STL_NUMBER       = STL + "number";
	public static final String STL_INDEX        = STL + "index";
	public static final String STL_FUTURE       = STL + "future";
	public static final String STL_FORMAT       = STL + "format";
	public static final String FORMAT           =   "format";
        
	public static final String FUN_NUMBER       = NSManager.STL_PREF + ":"  + "_n_";
	public static final String FUN_NL           = NSManager.STL_PREF + ":" + "nl"; 
        public static final String FUN_PROCESS      = NSManager.STL_PREF + ":" + "process"; 
        public static final String FUN_PROCESS_URI  = NSManager.STL_PREF + ":" + "processURI"; 
	public static final String FUN_INDENT       = NSManager.STL_PREF + ":" + "indent"; 
	public static final String FUN_CONCAT       = NSManager.STL_PREF + ":" + "concat"; 
	public static final String FUN_GROUPCONCAT  = NSManager.STL_PREF + ":" + "group_concat"; 
	public static final String FUN_AGGREGATE    = NSManager.STL_PREF + ":" + "aggregate"; 
	public static final String FUN_TURTLE       = NSManager.STL_PREF + ":" + "turtle"; 
	public static final String FUN_FORMAT       = NSManager.STL_PREF + ":" + "format"; 

	       
	static final String QNAME 	= KGRAM + "qname"; 
	static final String TURTLE 	= KGRAM + "turtle"; 
	static final String LEVEL 	= KGRAM + "level"; 
	static final String INDENT 	= KGRAM + "indent"; 
	static final String PPURI 	= KGRAM + "uri"; 
	static final String URILITERAL 	= KGRAM + "uriLiteral"; 
	static final String VISITED     = KGRAM + "isVisited"; 
	static final String ISSKOLEM    = KGRAM + "isSkolem"; 
	static final String SKOLEM      = KGRAM + "skolem"; 

	static final String UNNEST = "unnest";
	static final String KGUNNEST = KGRAM +UNNEST;
	static final String SYSTEM = "system";
	static final String GROUPBY = "groupBy";

	static final String KG_SPARQL= KGRAM + "sparql";
	static final String SIMILAR  = KGRAM + "similarity";
	static final String CSIMILAR = KGRAM + "cSimilarity";
	static final String PSIMILAR = KGRAM + "pSimilarity";
	static final String ANCESTOR = KGRAM + "ancestor";
	static final String DEPTH    = KGRAM + "depth";
	static final String GRAPH    = KGRAM + "graph";
	static final String NODE     = KGRAM + "node";
	static final String GET_OBJECT      = KGRAM + "getObject";
	static final String SET_OBJECT      = KGRAM + "setObject";
	static final String GETP     = KGRAM + "getProperty";
	static final String SETP     = KGRAM + "setProperty";
	static final String LOAD     = KGRAM + "load";
	static final String NUMBER   = KGRAM + "number";
	static final String EVEN     = KGRAM + "even";
	static final String ODD      = KGRAM + "odd";
	static final String DISPLAY  = KGRAM + "display";
	static final String EXTEQUAL = KGRAM + "equals";
	static final String EXTCONT  = KGRAM + "contains";
	static final String PROCESS  = KGRAM + "process";
	static final String ENV  	 = KGRAM + "env";
	static final String XT_ENV  	 = EXT + "env";
	static final String XT_STACK  	 = EXT + "stack";
	public static final String PATHNODE = KGRAM + "pathNode";
	static final String SLICE       = KGRAM + "slice";
	static final String DB          = KGRAM + "db";

	static final String EXIST 	= Term.EXIST;
	public static final String STRLEN 	= "strlen";
	static final String SUBSTR 	= "substr";
	static final String UCASE 	= "ucase";
	static final String LCASE 	= "lcase";
	static final String ENDS 	= "strends";
	static final String STARTS 	= "strstarts";
	static final String CONTAINS    = "contains";
	static final String ENCODE 	= "encode_for_uri";
	public static final String CONCAT="concat"; 
	static final String STRBEFORE 	= "strbefore"; 
	static final String STRAFTER 	= "strafter"; 
	static final String STRREPLACE 	= "replace"; 
	static final String UUID 	= "uuid"; 
	static final String STRUUID 	= "struuid"; 

	
	static final String RANDOM 	= "rand"; 
	static final String ABS 	= "abs"; 
	static final String CEILING     = "ceil"; 
	static final String FLOOR 	= "floor"; 
	static final String ROUND 	= "round";
	static final String POWER 	= "power";
        

	static final String NOW 	= "now"; 
	static final String YEAR 	= "year"; 
	static final String MONTH 	= "month"; 
	static final String DAY 	= "day"; 
	static final String HOURS 	= "hours";
	static final String MINUTES     = "minutes";
	static final String SECONDS     = "seconds";
	static final String TIMEZONE    = "timezone";
	static final String TZ 		= "tz";

	static final String MD5     	= "md5";
	static final String SHA1 	= "sha1";
	static final String SHA224 	= "sha224";
	static final String SHA256	= "sha256";
	static final String SHA384 	= "sha384";
	static final String SHA512 	= "sha512";              
                     
	static final String RQ_POWER 	= SPARQL + "power";
        static final String RQ_PLUS 	= SPARQL + "plus";
        static final String RQ_MINUS 	= SPARQL + "minus";
        static final String RQ_MULT 	= SPARQL + "mult";
        static final String RQ_DIV 	= SPARQL + "divis";
        static final String RQ_AND 	= SPARQL + "and";
        static final String RQ_OR 	= SPARQL + "or";
        static final String RQ_NOT 	= SPARQL + "not";
        static final String RQ_EQUAL 	= SPARQL + "equal";
        static final String RQ_DIFF 	= SPARQL + "diff";
        static final String RQ_EQ 	= SPARQL + "eq";
        static final String RQ_NE 	= SPARQL + "ne";
        static final String RQ_LE 	= SPARQL + "le";
        static final String RQ_LT 	= SPARQL + "lt";
        static final String RQ_GE 	= SPARQL + "ge";
        static final String RQ_GT 	= SPARQL + "gt";
                      
        static final String XT_VALID_URI= EXT + "validURI";
        static final String XT_LOAD 	= EXT + "load";
        static final String XT_CONTENT 	= EXT + "content";
        public static final String XT_DISPLAY 	= EXT + "display";
        public static final String XT_PRINT 	= EXT + "print";
        static final String XT_PRETTY   = EXT + "pretty";
        static final String XT_ATTRIBUTES= EXT + "attributes";
        static final String XT_XML      = EXT + "xml";
        static final String XT_RDF      = EXT + "rdf";
        static final String XT_JSON     = EXT + "json";
        static final String XT_SPIN     = EXT + "spin";        
        static final String XT_GDISPLAY = EXT + "gdisplay";
        static final String XT_GPRINT 	= EXT + "gprint";
        static final String XT_TUNE 	= EXT + "tune";
        static final String XT_UNION 	= EXT + "union";
        static final String XT_MINUS 	= EXT + "minus";
        static final String XT_OPTIONAL = EXT + "optional";
        static final String XT_JOIN     = EXT + "join";
        
        public static final String[] aggregate = 
	{AVG, COUNT, SUM, MIN, MAX, SAMPLE, 
         GROUPCONCAT, STL_GROUPCONCAT, STL_AGGAND, STL_AGGLIST, STL_AGGREGATE, AGGREGATE};
        
        // do not generate xt:set for set
        static final HashMap<String, Boolean> fixed; 
        	
	Term term;
        // function definition for UNDEF function call
	private Expr define;
	Pattern pat;
	Matcher match;
	XPathFun xfun;
	SQLFun sql;
	VariableResolver resolver;
	Object processor;
	Method fun;
	//ExpPattern pattern;
	private boolean isCorrect = true;
        boolean isCompiled = false;
	public static int count = 0;
	private static final int IFLAG[] = {
		Pattern.DOTALL, Pattern.MULTILINE, Pattern.CASE_INSENSITIVE,
		Pattern.COMMENTS};
	static final String SFLAG[] = {"s", "m", "i", "x"};
        		
	public static HashMap<String, Integer> table;
	public static HashMap<Integer, String> tname, toccur;
        static ASTQuery ast;
    private String name;
    
     static {
            fixed = new HashMap();
            fixed.put(SET, true);
            init();
            deftable();
        }
     
     static void init(){
         ast = ASTQuery.create();
         ast.setBody(BasicGraphPattern.create());
     }
    
	Processor(){            
        }
        
	Processor(Term t){
		term = t;
	}
        
        Term getTerm() {
            return term;
        }
        
        public static Processor create(){
            return new Processor();
        }
			
	
	// filter(exist {PAT})
//	public ExpPattern getPattern(){
//		return pattern;
//	}
//	
//	public void setPattern(ExpPattern pat){
//		 pattern = pat;
//	}
	       	
	public void type(Term term, ASTQuery ast){
                if (term.type() != ExprType.UNDEF){
                    // already done
                }
                else if (term.isFunction()){
			term.setType(ExprType.FUNCTION);
			term.setOper(getOper(term));
                        preprocess(term, ast);
		}
		else if (term.isAnd()){
			term.setType(ExprType.BOOLEAN);
			term.setOper(ExprType.AND);
		}
		else if (term.isOr()){
			term.setType(ExprType.BOOLEAN);
			term.setOper(ExprType.OR);
		}
		else if (term.isNot()){
			term.setType(ExprType.BOOLEAN);
			term.setOper(ExprType.NOT);
		}
		else {
			term.setType(ExprType.TERM);
			term.setOper(getOper(term));
		}
		
		if (term.oper() == ExprType.UNDEF){
			if (term.isPathExp()){
				// Property Path Exp
			}
			else {
                            ast.undefined(term);
			}
		}
		
	}
                            
       void prepare(Term term, ASTQuery ast) {
        if (term.isFunction() && ! term.isFunctionSignature()) {
            // skip signature of function xsd:integer(?n) { xsd:integer(?x) }
            switch (term.oper()) {                
                case ExprType.HASH:
                    compileHash(term);
                    break;
                case ExprType.URI:
                    compileURI(term, ast);
                    break;
                   
                case ExprType.REGEX:
                case ExprType.EXIST:                  
                case ExprType.STRREPLACE:                   
                case ExprType.XPATH:                   
                case ExprType.SQL:                   
                case ExprType.EXTERNAL:                    
                case ExprType.CUSTOM:                    
                    prepareOwn(term, ast);
            }
        }

        term.setArguments();
        check(term, ast);
    }
       
       /**
        * Create a specific Processor for this term
        * because we store specific data for the function
        * Use case: regex
        */
       void prepareOwn(Term term, ASTQuery ast){
           term.setProcessor(new Processor(term));
           term.getProcessor().prepare2(term, ast);
       }
       
       /**
        * Run on the specific Processsor      
        */
      void prepare2(Term term, ASTQuery ast) {
        if (term.isFunction()) {
            switch (term.oper()) {
                 case ExprType.REGEX:
                    compileRegex(term);
                    break;               
                case ExprType.STRREPLACE:
                    compileReplace(term);
                    break;
                case ExprType.XPATH:
                    compileXPath(term, ast);
                    break;
                case ExprType.SQL:
                    compileSQL(term, ast);
                    break;
                case ExprType.EXTERNAL:
                    // done at runtime 
                    //compileExternal();
                    break;
                case ExprType.CUSTOM:
                    compileCustom(term, ast);
                    break;
            }
        }
    }
        
	
	// TODO: error message
	void check(Term term, ASTQuery ast){
		if (term.isAggregate()){ 
                   if (term.getName().equalsIgnoreCase(COUNT)){
                        if (term.getArity() > 1){
                            ast.setCorrect(false);
                        }
                    }
                    else if (term.getArity() != 1){
                           ast.setCorrect(false);
                    }
                }
	}
	
	
	static void deftable(){
		table = new HashMap<String, Integer>();
		tname = new HashMap<Integer, String>();
		toccur = new HashMap<Integer, String>();

		defoper("<", 	ExprType.LT);
		defoper("<=", 	ExprType.LE);
		defoper("=", 	ExprType.EQ);
		defoper("==", 	ExprType.EQUAL);
		defoper("!=", 	ExprType.NEQ);
		defoper("!==", 	ExprType.NOT_EQUAL);
		defoper(">", 	ExprType.GT);
		defoper(">=", 	ExprType.GE);
		defoper("~", 	ExprType.CONT);
		defoper(KGRAM+"tilda", 	ExprType.CONT);
		defoper("^", 	ExprType.START);
		defoper(IN, 	ExprType.IN);
		defoper("+", 	ExprType.PLUS);
		defoper("-", 	ExprType.MINUS);
		defoper("*", 	ExprType.MULT);
		defoper("/", 	ExprType.DIV);
		defoper("&&", 	ExprType.AND);
		defoper("||", 	ExprType.OR);
		defoper("!", 	ExprType.NOT);
		defoper(Term.STAR, ExprType.STAR);
				
		defoper("safe", ExprType.SAFE);
		defoper(BOUND, ExprType.BOUND);
		defoper(COUNT, 	ExprType.COUNT);
		defoper(MIN, 	ExprType.MIN);
		defoper(MAX, 	ExprType.MAX);
		defoper(SUM, 	ExprType.SUM);
		defoper(AVG, 	ExprType.AVG);
		defoper(ISURI, 	ExprType.ISURI);
		defoper(ISIRI, 	ExprType.ISURI);
		defoper(ISUNDEF, ExprType.ISUNDEFINED);
		defoper(ISWELLFORMED, ExprType.ISWELLFORMED);
		defoper(ISBLANK, ExprType.ISBLANK);
		defoper(ISLITERAL, ExprType.ISLITERAL);
		defoper(ISLIST, ExprType.ISLIST);
		defoper(EXT+"isList", ExprType.ISLIST);
		defoper(ISNUMERIC, ExprType.ISNUMERIC);
		defoper(LANG, 	ExprType.LANG);
		defoper(LANGMATCH, ExprType.LANGMATCH);
                
                defoper("triple", TRIPLE);
                defoper("isTriple", IS_TRIPLE);
                defoper("subject", SUBJECT);
                defoper("predicate", PREDICATE);
                defoper("object", OBJECT);
                defoper("sparql-compare", ExprType.SPARQL_COMPARE);
                defoper(EXT+"edge", ExprType.XT_EDGE);
                defoper(EXT+"asserted", ExprType.XT_ASSERTED);
		
		defoper(STRDT, 		ExprType.STRDT);
		defoper(STRLANG, 	ExprType.STRLANG);
		defoper(BNODE, 		ExprType.BNODE);
		defoper(PATHNODE, 	ExprType.PATHNODE);
		defoper(COALESCE, 	ExprType.COALESCE);
		defoper(IF, 		ExprType.IF);
		defoper(GROUPCONCAT,    ExprType.GROUPCONCAT);
		defoper(STL_AGGAND,     ExprType.AGGAND);
		defoper(STL_AGGLIST,    ExprType.AGGLIST);
		defoper(STL_AND,        ExprType.STL_AND);
		defoper(SAMPLE, 	ExprType.SAMPLE);
		defoper(INLIST,         ExprType.INLIST);
		defoper(ISSKOLEM,       ExprType.ISSKOLEM);
		defoper("isSkolem",     ExprType.ISSKOLEM);
                defoper("isExtension",  ExprType.ISEXTENSION);
		defoper(SKOLEM,         ExprType.SKOLEM);
		defoper(RETURN,         ExprType.RETURN);
		defoper(SEQUENCE,       ExprType.SEQUENCE);
		defoper(EXT+SEQUENCE,   ExprType.SEQUENCE);
		defsysoper(LET,         ExprType.LET);
		defsysoper(TRY_CATCH,   ExprType.TRY_CATCH);
		defoper(THROW,          ExprType.THROW);
		defoper(STOP,           ExprType.RESUME);
		defoper(SET,            ExprType.SET);
		defoper(EXT+"unset",    ExprType.UNSET);
		defoper(EXT+"unsetStatic",    ExprType.STATIC_UNSET);
		defoper(EXT+"staticUnset",    ExprType.STATIC_UNSET);
		defoper(STATIC,         ExprType.STATIC);
                
		defoper(XT_GET_DATATYPE_VALUE, ExprType.XT_GET_DATATYPE_VALUE);
		defoper(XT_JSON_OBJECT, ExprType.XT_JSON_OBJECT);
		defoper(XT_MAP,         ExprType.XT_MAP);
		defoper(XT_LIST,        ExprType.LIST);
		defoper(XT_TOLIST,      ExprType.XT_TOLIST);
		defoper(XT_IOTA,        ExprType.IOTA);
		defoper(XT_ITERATE,     ExprType.XT_ITERATE);
		defoper(XT_REVERSE,     ExprType.XT_REVERSE);
		defoper(XT_APPEND,      ExprType.XT_APPEND);
		defoper(XT_MERGE,       ExprType.XT_MERGE);
		defoper(XT_SORT,        ExprType.XT_SORT);
		defoper(XT_RESULT,      ExprType.XT_RESULT);
 		defoper(XT_COMPARE,     ExprType.XT_COMPARE);
 		defoper(XT_VISITOR,     ExprType.XT_VISITOR);
               
                defoper("isFunction",      ExprType.XT_ISFUNCTION);                
                defoper(EXT+"isFunction",  ExprType.XT_ISFUNCTION);                
                defoper(EXT+"event",       ExprType.XT_EVENT);                
		defoper(JAVACALL,          ExprType.JAVACALL);                
		defoper(FUNCALL,           ExprType.FUNCALL);                
		defsysoper(EVAL,           ExprType.EVAL);                
		defsysoper(REDUCE,         ExprType.REDUCE);
		defsysoper(APPLY,          ExprType.APPLY);
		defsysoper(MAP,            ExprType.MAP);
		defsysoper(FOR,            ExprType.FOR);
		defsysoper(MAPLIST,        ExprType.MAPLIST);
		defsysoper(MAPFUN,         ExprType.MAPFUN);
		defsysoper(MAPMERGE,       ExprType.MAPMERGE);
		defsysoper(MAPAPPEND,      ExprType.MAPAPPEND);
		defsysoper(MAPSELECT,      ExprType.MAPFINDLIST);
		defsysoper(MAPFIND,        ExprType.MAPFIND);
		defsysoper(MAPFINDLIST,    ExprType.MAPFINDLIST);
		defsysoper(MAPANY,         ExprType.MAPANY);
		defsysoper(MAPEVERY,       ExprType.MAPEVERY);
                
		defoper(XT_MAPPING,     ExprType.XT_MAPPING);
		defoper(XT_MEMBER,      ExprType.XT_MEMBER);
		defoper(XT_ADD,         ExprType.XT_ADD);
		defoper(XT_CONS,        ExprType.XT_CONS);
		defoper(XT_FIRST,       ExprType.XT_FIRST);
		defoper(XT_REST,        ExprType.XT_REST);
		defoper(XT_SELF,        ExprType.SELF);
		defoper(XT_HAS,         ExprType.XT_HAS);
		defoper(XT_GET,         ExprType.XT_GET);
		defoper(XT_PATH,        ExprType.XT_PATH);
		defoper(XT_REMOVE,      ExprType.XT_REMOVE);
		defoper(XT_REMOVE_INDEX,ExprType.XT_REMOVE_INDEX);
		defoper(XT_SWAP,        ExprType.XT_SWAP);               
		defoper(XT_LAST,        ExprType.XT_LAST);
		defoper(XT_GEN_GET,     ExprType.XT_GEN_GET);
		defoper(XT_GEN_REST,    ExprType.XT_GEN_REST);
		defoper(XT_SET,         ExprType.XT_SET);
 		defoper(XT_REJECT,      ExprType.XT_REJECT);
               
		defoper(XT_FOCUS,        ExprType.XT_FOCUS);
		defoper(XT_SIZE,         ExprType.XT_COUNT);		
		defoper(XT_TOGRAPH,      ExprType.XT_TOGRAPH);
		defoper(XT_GRAPH,        ExprType.XT_GRAPH);
                defoper(EXT+"create",    ExprType.XT_CREATE);
		defoper(XT_SUBJECT,      ExprType.XT_SUBJECT);
		defoper(EXT+"predicate", ExprType.XT_PROPERTY);
		defoper(XT_PROPERTY,     ExprType.XT_PROPERTY);
		defoper(XT_OBJECT,       ExprType.XT_OBJECT);
		defoper(XT_VALUE,        ExprType.XT_VALUE);                
		defoper(XT_INDEX,        ExprType.XT_INDEX);
		defoper(EXT+"label",     ExprType.XT_LABEL);
		defoper(EXT+"reference", ExprType.XT_REFERENCE);
		defoper(XT_VARIABLES,    ExprType.XT_VARIABLES);
		defoper(XT_EDGES,        ExprType.XT_EDGES);
                defoper(EXT+"node",      ExprType.XT_NODE);
                defoper(EXT+"subjects",  ExprType.XT_SUBJECTS);
                defoper(EXT+"objects",   ExprType.XT_OBJECTS);
                defoper(EXT+"vertex",    ExprType.XT_VERTEX);
		defoper(XT_NAME,         ExprType.XT_NAME);
		defoper(XT_TRIPLE,       ExprType.XT_TRIPLE);
		defoper(XT_QUERY,        ExprType.XT_QUERY);
		defoper(EXT+"mappings",  ExprType.XT_MAPPINGS);
		defoper(EXT+"parseMappings",  ExprType.XT_PARSE_MAPPINGS);
		defoper(EXT+"loadMappings",  ExprType.XT_LOAD_MAPPINGS);
		defoper(XT_CONTEXT,      ExprType.XT_CONTEXT);
		defoper(XT_METADATA,     ExprType.XT_METADATA);
		defoper(XT_ANNOTATION,   ExprType.XT_METADATA);
		defoper(XT_NSMANAGER,    ExprType.STL_PREFIX);
		defoper(XT_ENTAILMENT,   ExprType.XT_ENTAILMENT);
		defoper(XT_SHAPE_GRAPH,  ExprType.XT_SHAPE_GRAPH);
		defoper(XT_SHAPE_NODE,   ExprType.XT_SHAPE_NODE);
		defoper(XT_DATATYPE,     ExprType.XT_DATATYPE);
		defoper(XT_KIND,         ExprType.XT_KIND);
		defoper(XT_METHOD,       ExprType.XT_METHOD);
		defoper(XT_METHOD_TYPE,  ExprType.XT_METHOD_TYPE);
		defoper(XT_EXISTS,       ExprType.XT_EXISTS);
		defoper(XT_INSERT,       ExprType.XT_INSERT);
		defoper(XT_DELETE,       ExprType.XT_DELETE);
		defoper(XT_DEGREE,       ExprType.XT_DEGREE);
		defoper(XT_MINDEGREE,    ExprType.XT_MINDEGREE);
                
		defoper(XT_FROM,         ExprType.XT_FROM);
		defoper(XT_NAMED,        ExprType.XT_NAMED);
                
		defsysoper(REGEX, 	ExprType.REGEX);
                defoper(APPROXIMATE,	ExprType.APPROXIMATE);
                defoper(EXT+APPROXIMATE,ExprType.APPROXIMATE);
                defoper(APP_SIM,	ExprType.APP_SIM);
                defoper(EXT+"sim",      ExprType.APP_SIM);
		defoper(DATATYPE, 	ExprType.DATATYPE);
		defoper(STR, 		ExprType.STR);
		defoper(XSDSTRING, 	ExprType.XSDSTRING);
		defoper(URI, 		ExprType.URI);
		defoper(IRI, 		ExprType.URI);
		defoper(SELF, 		ExprType.SELF);
		defoper(DEBUG, 		ExprType.DEBUG);
		//defoper(TRACE, 		ExprType.XT_TRACE);

		defoper(MATCH, 	ExprType.SKIP);
		defoper(PLENGTH, ExprType.LENGTH);
		defoper(KGPLENGTH, ExprType.LENGTH);
		defoper(KGPWEIGHT, ExprType.PWEIGHT);

		defsysoper(XPATH, 	ExprType.XPATH);
		defsysoper(KGXPATH, 	ExprType.XPATH);
		defsysoper(EXT+"xpath", ExprType.XPATH);
		defsysoper(SQL, 	ExprType.SQL);
		defoper(KGSQL, 	ExprType.SQL);
		defoper(KG_SPARQL, ExprType.KGRAM);
		defoper(EXT+"sparql", ExprType.KGRAM);
		defoper(EXTERN, ExprType.EXTERN);
		defoper(UNNEST, ExprType.UNNEST);
		defoper(KGUNNEST, ExprType.UNNEST);
		defsysoper(EXIST,  ExprType.EXIST);
		defoper(SYSTEM, ExprType.SYSTEM);
		defoper(GROUPBY, ExprType.GROUPBY);
		
		defoper(EXT+"read",     ExprType.READ);
		defoper(EXT+"httpget",  ExprType.XT_HTTP_GET);
		defoper(READ,           ExprType.READ);
		defoper(EXT+"write",    ExprType.WRITE);
		defoper(WRITE,          ExprType.WRITE);
		defoper(QNAME,          ExprType.QNAME);
                defoper(EXT+"contract", ExprType.QNAME);
		defoper(EXT+"expand",   ExprType.XT_EXPAND);
		defoper(EXT+"define",   ExprType.XT_DEFINE);
                
		defoper(PROVENANCE, 	ExprType.PROVENANCE);
 		defoper(INDEX,          ExprType.INDEX);
 		//defoper(EXT+"index",    ExprType.INDEX);
 		defoper(ID,             ExprType.ID);
 		defoper(TIMESTAMP,      ExprType.TIMESTAMP);
 		defoper(TEST,           ExprType.TEST);
 		defoper(DESCRIBE,       ExprType.DESCRIBE);
 		defoper(STORE,          ExprType.STORE);
 		defoper(QUERY,          ExprType.QUERY);
 		defoper(EXTENSION,      ExprType.EXTENSION);
               
		//defoper(PPRINT, 	ExprType.APPLY_TEMPLATES);
		defoper(KG_EVAL, 		ExprType.APPLY_TEMPLATES);
//		defoper(PPRINTWITH, 	ExprType.APPLY_TEMPLATES_WITH);
//		defoper(PPRINTALL, 	ExprType.APPLY_TEMPLATES_ALL);
//		defoper(PPRINTALLWITH, 	ExprType.APPLY_TEMPLATES_WITH_ALL);
		defoper(TEMPLATE, 	ExprType.CALL_TEMPLATE);
		defoper(TEMPLATEWITH, 	ExprType.CALL_TEMPLATE_WITH);
		defoper(TURTLE,         ExprType.TURTLE);                
                defoper(FOCUS_NODE,     ExprType.FOCUS_NODE);
                
                defoper(APPLY_TEMPLATES,            ExprType.APPLY_TEMPLATES);
		defoper(APPLY_TEMPLATES_ALL,        ExprType.APPLY_TEMPLATES_ALL);
		defoper(APPLY_TEMPLATES_GRAPH,      ExprType.APPLY_TEMPLATES_GRAPH);
		defoper(APPLY_TEMPLATES_NOGRAPH,    ExprType.APPLY_TEMPLATES_NOGRAPH);
		defoper(APPLY_TEMPLATES_WITH,       ExprType.APPLY_TEMPLATES_WITH);
		defoper(ATW,                        ExprType.APPLY_TEMPLATES_WITH);
		defoper(APPLY_TEMPLATES_WITH_ALL,   ExprType.APPLY_TEMPLATES_WITH_ALL);                
		defoper(APPLY_TEMPLATES_WITH_GRAPH, ExprType.APPLY_TEMPLATES_WITH_GRAPH);
		defoper(APPLY_TEMPLATES_WITH_NOGRAPH, ExprType.APPLY_TEMPLATES_WITH_NOGRAPH);
		defoper(CALL_TEMPLATE,              ExprType.CALL_TEMPLATE);
		defoper(CALL_TEMPLATE_WITH,         ExprType.CALL_TEMPLATE_WITH);
		defoper(STL_TEMPLATE,               ExprType.STL_TEMPLATE);
		defoper(CTW,                        ExprType.CALL_TEMPLATE_WITH);
                
                // 3 deprecated:
		defoper(APPLY_ALL_TEMPLATES, 	ExprType.APPLY_TEMPLATES_ALL);
		defoper(APPLY_ALL_TEMPLATES_WITH,ExprType.APPLY_TEMPLATES_WITH_ALL);
                
                defoper(STL_PROCESS,            ExprType.STL_PROCESS);
                defoper(STL_PROCESS_URI,        ExprType.STL_PROCESS_URI);
		defoper(STL_TURTLE,             ExprType.TURTLE);
		defoper(STL_STRIP,              ExprType.STL_STRIP);
                defoper(STL_URI,                ExprType.PPURI);
                defoper(STL_PROLOG,             ExprType.PROLOG);
                defoper(STL_PREFIX,             ExprType.STL_PREFIX);
                defoper(XT_PREFIX,              ExprType.STL_PREFIX);
		defoper(STL_INDENT,             ExprType.INDENT);
		defoper(STL_LEVEL,              ExprType.LEVEL);
		defoper(STL_NL,                 ExprType.STL_NL);
		defoper(STL_SELF,               ExprType.SELF);
		defoper(STL_URILITERAL, 	ExprType.URILITERAL);
		//defoper(STL_XSDLITERAL,         ExprType.XSDLITERAL);
		defoper(STL_NUMBER,             ExprType.STL_NUMBER);
		defoper(STL_FORMAT,             ExprType.STL_FORMAT);
		defoper(EXT+"format",           ExprType.STL_FORMAT);
		defoper(FORMAT,                 ExprType.FORMAT);
                
		defoper(STL_INDEX,              ExprType.STL_INDEX);
		defoper(STL_FUTURE,             ExprType.STL_FUTURE);
		defoper(STL_LOAD,               ExprType.STL_LOAD);
		defoper(STL_IMPORT,             ExprType.STL_IMPORT);
                defoper(STL_ISSTART,            ExprType.STL_ISSTART);
                defoper(STL_SET,                ExprType.STL_SET);
                defoper(STL_GET,                ExprType.STL_GET);
                defoper(STL_HASGET,             ExprType.STL_HASGET);
                defoper(STL_CSET,               ExprType.STL_CSET);
                defoper(STL_CGET,               ExprType.STL_CGET);                
                defoper(STL_EXPORT,             ExprType.STL_EXPORT);
                defoper(STL_VSET,               ExprType.STL_VSET);
                defoper(STL_VGET,               ExprType.STL_VGET);
                defoper(STL_VISIT,              ExprType.STL_VISIT);
                defoper(STL_VISITED,            ExprType.STL_VISITED);
                defoper(STL_VISITED_GRAPH,      ExprType.STL_VISITED_GRAPH);
                defoper(STL_ERRORS,             ExprType.STL_ERRORS);
                defoper(STL_ERROR_MAP,          ExprType.STL_ERROR_MAP);
                defoper(STL_BOOLEAN,            ExprType.STL_BOOLEAN);
                defoper(STL+"defined",          ExprType.STL_DEFINED);
                defoper(EXT+"xslt",             ExprType.XT_XSLT);

		defoper(LEVEL,          ExprType.LEVEL);
		defoper(INDENT,         ExprType.INDENT);
		defoper(PPURI,          ExprType.PPURI);
		defoper(URILITERAL, 	ExprType.URILITERAL);
		defoper(STL_XSDLITERAL, ExprType.XSDLITERAL);
		defoper(VISITED,        ExprType.VISITED);
		defoper(PROLOG,         ExprType.PROLOG);
		defoper(PACKAGE,        ExprType.PACKAGE);
		defoper(EXPORT,         ExprType.PACKAGE);
		defsysoper(STL_DEFINE,     ExprType.FUNCTION);
		defsysoper(FUNCTION,       ExprType.FUNCTION);
		defoper(DEFINE,         ExprType.STL_DEFINE);                
		defoper(LAMBDA,         ExprType.LAMBDA);
		defoper(ERROR,          ExprType.ERROR);
                //defoper(STL_DEFAULT,    ExprType.STL_DEFAULT);
                defoper(STL_CONCAT,     ExprType.STL_CONCAT);
                defoper(STL_GROUPCONCAT, ExprType.STL_GROUPCONCAT);
                defoper(STL_AGGREGATE,   ExprType.STL_AGGREGATE);
                defsysoper(AGGREGATE,       ExprType.AGGREGATE);

		defoper(EXT+"distance", ExprType.XT_DISTANCE);
		defoper(SIMILAR, ExprType.SIM);
                defoper(EXT+"similarity", ExprType.SIM);
		defoper(CSIMILAR, ExprType.SIM);
		defoper(PSIMILAR, ExprType.PSIM);
		defoper(ANCESTOR, ExprType.ANCESTOR);
		defoper(EXT+"ancestor", ExprType.ANCESTOR);
		defoper(DEPTH,   ExprType.DEPTH);
		defoper(EXT+"depth",   ExprType.DEPTH);
		defoper(GRAPH,   ExprType.KG_GRAPH);
		defoper(NODE,    ExprType.NODE);
		defoper(GET_OBJECT,     ExprType.GET_OBJECT);
		defoper(SET_OBJECT,     ExprType.SET_OBJECT);
		defoper(GETP,    ExprType.GETP);
		defoper(SETP,    ExprType.SETP);
		
		defoper(LOAD,    ExprType.LOAD);
		defoper(NUMBER,  ExprType.NUMBER);
		defoper(EVEN,  ExprType.EVEN);
		defoper(ODD,   ExprType.ODD);
             
                defoper(RQ_PLUS,   ExprType.PLUS);
                defoper(RQ_MULT,   ExprType.MULT);
                defoper(RQ_MINUS,  ExprType.MINUS);
                defoper(RQ_DIV,    ExprType.DIV);
                defoper(RQ_AND,    ExprType.AND);
                defoper(RQ_OR,     ExprType.OR);
                defoper(RQ_NOT,    ExprType.NOT);
                defoper(RQ_EQUAL,  ExprType.EQUAL);
                defoper(RQ_DIFF,   ExprType.NOT_EQUAL); 
                defoper(RQ_EQ,     ExprType.EQ);                 
                defoper(RQ_NE,     ExprType.NEQ);                 
                defoper(RQ_LT,     ExprType.LT); 
                defoper(RQ_LE,     ExprType.LE); 
                defoper(RQ_GT,     ExprType.GT); 
                defoper(RQ_GE,     ExprType.GE); 
                
                defoper(EXT+"nodetype",     ExprType.XT_NODE_TYPE);
                defoper(EXT+"nodename",     ExprType.XT_NODE_NAME);
                defoper(EXT+"nodevalue",    ExprType.XT_NODE_VALUE);
                defoper(EXT+"nodeproperty", ExprType.XT_NODE_PROPERTY);
                defoper(EXT+"attributes",   ExprType.XT_ATTRIBUTES);
                defoper(EXT+"elements",     ExprType.XT_ELEMENTS);
                defoper(EXT+"children",     ExprType.XT_CHILDREN);
                defoper(EXT+"text",         ExprType.XT_TEXT_CONTENT);
                defoper(EXT+"nodetext",     ExprType.XT_TEXT_CONTENT);
                defoper(EXT+"nodeparent",   ExprType.XT_NODE_PARENT);
                defoper(EXT+"nodedocument", ExprType.XT_NODE_DOCUMENT);
                defoper(EXT+"nodeelement",  ExprType.XT_NODE_ELEMENT);
                defoper(EXT+"objectvalue",  ExprType.XT_DATATYPE_VALUE);
                defoper(EXT+"namespace",    ExprType.XT_NAMESPACE);
                defoper(EXT+"domain",       ExprType.XT_DOMAIN);
                defoper(EXT+"base",         ExprType.XT_BASE);
                defoper(EXT+"split",        ExprType.XT_SPLIT);
                
                defoper(DOM+"getNodeProperty", ExprType.XT_NODE_PROPERTY);
                defoper(DOM+"getNodeDatatypeValue", ExprType.XT_DATATYPE_VALUE);

                defoper(DOM+"getNodeType",     ExprType.XT_NODE_TYPE);
                defoper(DOM+"getNodeName",     ExprType.XT_NODE_NAME);
                defoper(DOM+"getLocalName",    ExprType.XT_NODE_LOCAL_NAME);
                defoper(DOM+"getNodeValue",    ExprType.XT_NODE_VALUE);
                defoper(DOM+"getAttributes",   ExprType.XT_ATTRIBUTES);
                defoper(DOM+"getElementsByTagName",     ExprType.XT_ELEMENTS);
                defoper(DOM+"getElementsByTagNameNS",   ExprType.XT_ELEMENTS);
                defoper(DOM+"getFirstChild",    ExprType.XT_NODE_FIRST_CHILD);
                defoper(DOM+"getChildNodes",    ExprType.XT_CHILDREN);
                defoper(DOM+"getTextContent",   ExprType.XT_TEXT_CONTENT);
                defoper(DOM+"getNodeParent",    ExprType.XT_NODE_PARENT);
                defoper(DOM+"getOwnerElement",  ExprType.XT_NODE_PARENT);
                defoper(DOM+"getOwnerDocument", ExprType.XT_NODE_DOCUMENT);
                defoper(DOM+"getElementById",   ExprType.XT_NODE_ELEMENT);
                defoper(DOM+"getNamespaceURI",  ExprType.XT_NAMESPACE);
                defoper(DOM+"getBaseURI",       ExprType.XT_BASE);
                defoper(DOM+"hasAttribute",     ExprType.XT_HAS_ATTRIBUTE);
                defoper(DOM+"hasAttributeNS",   ExprType.XT_HAS_ATTRIBUTE);
                defoper(DOM+"getAttribute",     ExprType.XT_ATTRIBUTE);
                defoper(DOM+"getAttributeNS",   ExprType.XT_ATTRIBUTE);
                
                
              
                defoper(XT_VALID_URI,ExprType.XT_VALID_URI);  
                defoper(XT_LOAD,   ExprType.LOAD);  
                defoper(XT_CONTENT,ExprType.XT_CONTENT);  
                defoper(XT_PRETTY, ExprType.XT_PRETTY);  
                defoper(XT_DISPLAY,ExprType.XT_DISPLAY);  
                defoper(XT_PRINT,  ExprType.XT_PRINT);                 
                defoper(EXT+"syntax",  ExprType.XT_SYNTAX);                 
                defoper(XT_XML,    ExprType.XT_XML);
                defoper(XT_RDF,    ExprType.XT_RDF);
                defoper(XT_JSON,   ExprType.XT_JSON);
                defoper(XT_SPIN,   ExprType.XT_SPIN);
                defoper(XT_GDISPLAY,ExprType.XT_DISPLAY);  
                defoper(XT_GPRINT, ExprType.XT_PRINT);  
                defoper(XT_TUNE,   ExprType.XT_TUNE); 
                defoper(XT_REPLACE,ExprType.XT_REPLACE); 
                defoper(XT_UPPERCASE,ExprType.XT_UPPERCASE); 
                defoper(XT_LOWERCASE,ExprType.XT_LOWERCASE); 
                
                defoper(XT_UNION,  ExprType.XT_UNION);  
                defoper(XT_MINUS,  ExprType.XT_MINUS);  
                defoper(XT_OPTIONAL,ExprType.XT_OPTIONAL);  
                defoper(XT_JOIN,    ExprType.XT_JOIN);  
                
		defoper(DISPLAY, ExprType.DISPLAY);
		defoper(EXTEQUAL,ExprType.EXTEQUAL);
                defoper(EXT+"similar",ExprType.EXTEQUAL);
		defoper(EXTCONT, ExprType.EXTCONT);
                defoper(EXT+"contains", ExprType.EXTCONT);
		defoper(PROCESS, ExprType.PROCESS);
		defoper(ENV, 	 ExprType.ENV);
		defoper(XT_ENV,  ExprType.ENV);
		defoper(XT_STACK,ExprType.XT_STACK);
		defoper(SLICE, 	 ExprType.SLICE);
		defoper(DB, 	 ExprType.DB);

		defoper(SAMETERM, ExprType.SAMETERM);
		defoper(STRLEN, ExprType.STRLEN);
		defoper(SUBSTR, ExprType.SUBSTR);
		defoper(UCASE, 	ExprType.UCASE);
		defoper(LCASE, 	ExprType.LCASE);
		defoper(ENDS, 	ExprType.ENDS);
		defoper(STARTS, ExprType.STARTS);
		defoper(CONTAINS, ExprType.CONTAINS);
		defoper("strcontains", ExprType.CONTAINS);
		defoper(ENCODE, ExprType.ENCODE);
		defoper(CONCAT, ExprType.CONCAT);
		defoper(STRBEFORE, ExprType.STRBEFORE);
		defoper(STRAFTER, ExprType.STRAFTER);
		defoper(STRREPLACE, ExprType.STRREPLACE);
		defoper(UUID, ExprType.FUUID);
		defoper(STRUUID, ExprType.STRUUID);

		
		defoper(POWER,  ExprType.POWER);
		defoper(RANDOM, ExprType.RANDOM);
		defoper(ABS, 	ExprType.ABS);
		defoper(FLOOR, 	ExprType.FLOOR);
		defoper(ROUND, 	ExprType.ROUND);
		defoper(CEILING, ExprType.CEILING);

		defoper(NOW, 	ExprType.NOW);
		defoper(YEAR, 	ExprType.YEAR);
		defoper(MONTH, 	ExprType.MONTH);
		defoper(DAY, 	ExprType.DAY);
		defoper(HOURS, 	ExprType.HOURS);
		defoper(MINUTES, ExprType.MINUTES);
		defoper(SECONDS, ExprType.SECONDS);
		defoper(TIMEZONE, ExprType.TIMEZONE);
		defoper(TZ, 	ExprType.TZ);


		defoper(MD5, 	ExprType.HASH);
		defoper(SHA1, 	ExprType.HASH);
		defoper(SHA224, ExprType.HASH);
		defoper(SHA256, ExprType.HASH);
		defoper(SHA384, ExprType.HASH);
		defoper(SHA512, ExprType.HASH);

	}
	
	static void defoper(String key, int value){
            define(key, value);
        }
        
        static void defextoper(String key, int value) throws EngineException{
            defextoper(key, value, 2);
        }
         
        static void defextoper(String key, int value, int arity) throws EngineException{
            define(key, value);
            defExtension(key, arity);
        }
        
        static void defsysoper(String key, int value){
            define(key, value);
        }
        
	static void define(String key, int value){
                // isURI
		table.put(key.toLowerCase(), value);
                if (! key.startsWith("http://")){
                    // rq:isURI
                    table.put(SPARQL + key.toLowerCase(), value);   
                }
		tname.put(value, key);  
	}
        
        static void defExtension(String key, int arity) throws EngineException{
            String name = key.toLowerCase();
            if (! key.startsWith("http://")){
                name = SPARQL + key;
            }
            Function fun = ast.defExtension(name, key, arity);
        }
        
        public static ASTQuery getAST(){
            return ast;
        }
        
        boolean fixed(String name){
            return  fixed.containsKey(name);
        }
        
        public static void test(){
            for (String name : table.keySet()){
                if (! name.startsWith("http://")){
                    if (table.containsKey(KGRAM + name)){
                        System.out.println(name);
                    }
                }
            }
        }
	       
	
	static int getOper(Term term){
                return getOper(term.getLabel());
        }
                
       public static int getOper(String name) {
        Integer n = table.get(name.toLowerCase());
        if (n == null) {
            if (name.startsWith(RDFS.XSDPrefix) || name.startsWith(RDFS.XSD)
                    || name.startsWith(RDFS.RDFPrefix) || name.startsWith(RDFS.RDF)) {
                n = ExprType.CAST;
            } 
            else if (name.startsWith(NSManager.DT)) {
                n = ExprType.XT_CAST;
            } 
            else if (name.startsWith(CUSTOM)) {
                n = ExprType.CUSTOM;
            } else if (name.startsWith(KeywordPP.CORESE_PREFIX)) {
                n = ExprType.EXTERNAL;
            } 
            else if (name.startsWith(NSManager.JAVA)) {
                n = ExprType.JAVACALL;
            } 
            else if (name.startsWith(NSManager.DS)) {
                n = ExprType.DSCALL;
            } 
             else if (name.startsWith(NSManager.CAST)) {
                n = ExprType.JAVACAST;
            } 
            else {
                n = ExprType.UNDEF;
            }
        }
        // draft: record occurrences during test case
        //toccur.put(n, name);
        return n;
    }
        	
	public static void finish(){
		for (Integer n : table.values()){
			if (! toccur.containsKey(n)){
				System.out.println("Missing test: " + tname.get(n));
			}
		}
	}
	
		       
        void preprocess(Term term, ASTQuery ast){
            switch (term.oper()){
                
                case ExprType.MAP:
                case ExprType.MAPLIST:
                case ExprType.MAPMERGE:
                case ExprType.MAPFIND:
                case ExprType.MAPFINDLIST:
                case ExprType.MAPEVERY:
                case ExprType.MAPANY:
                   
                // do not compile apply(rq:regex, ?list) now
                // as apply(lambda(?a, ?b) { rq:regex(?a, ?b) }, ?list)
                // because we don't know the arity yet 
                //case ExprType.APPLY:
                case ExprType.REDUCE:
                case ExprType.FUNCALL:
                    
                    processMap(term, ast);
                    break;
                    
                case ExprType.AGGREGATE:
                    processAggregate(term, ast);
                    break;
                                                      
                case ExprType.LET:
                    processLet(term, ast);
                    break;
                                  
                    
            }
        }
        
        /**
         * let (?x = exp, ?y = exp, exp)
         * ->
         * let (?x = exp, let (?y = exp, exp))
         * @param ast 
         */
        void processLet(Term term, ASTQuery ast){
            processMatch(term.getLet(), ast);
        }
        
        
        /**
         * let (match(?x, ?p, ?y) = ?l) {}
         * ::=
         * let (?x = xt:get(?l, 0), ?p = xt:get(?l, 1), ?y = xt:get(?l, 2)) {} 
         * @param ast 
         */
       void processMatch(Let term, ASTQuery ast) {
            ast.getFactory().processMatch(term);
        }
               
        /**
         * map(rq:fun, ?list)
         * -> 
         * map(lambda(?x){ rq:fun(?x) }, ?list)
         */
    void processMap(Term term, ASTQuery ast) {
        ast.processMap(term);
    }
                          
      // aggregate(?x, xt:mediane)
    void processAggregate(Term term, ASTQuery ast) {
        //ast.processAggregate(term);
    } 
                     	
	/**
	 * sha256(?x) ->
	 * hash("SHA-256", ?x)
	 */
	void compileHash(Term term){
		String name = term.getName();
                // use case:  rq:sha256
                name = NSManager.nstrip(name);
		if (name.startsWith("sha") || name.startsWith("SHA")){
			name = "SHA-" + name.substring(3);
		}
		term.setModality(name);
	}
	
	void compileURI(Term term, ASTQuery ast){
		String base = ast.getNSM().getBase();
		if (base!=null && base!=""){
			term.setModality(ast.getNSM().getBase());
		}
	}
        			
	/**
	 * term = regex(?x,  ".*toto",  ["i"])
	 * match.reset(string);
	 * boolean res = match.matches();
	 */
	void compileRegex(Term term){
            // use case: apply(rq:regex, ?list)
            if (term.getArity() >= 2 && term.getArg(1).isConstant() && (term.getArity() == 2 || term.getArg(2).isConstant())){
                isCompiled = true;           
		String sflag = null;
                if (term.getArity() == 3){
                    sflag = term.getArg(2).getName();
		}		
                compilePattern(term.getArg(1).getName(), sflag, true);
            }
	}
	
	
	void compilePattern(String patdtvalue, String sflag, boolean regex){		
		int flag = 0;
		if (sflag != null){ // flag argument "smix"
			for (int i = 0; i < IFLAG.length; i++){
				if (sflag.indexOf(SFLAG[i]) != -1){ //is flag[i] present
					flag =  flag | IFLAG[i]; // add the corresponding int flag
				}
			}
		}

		if (regex && 
                        !patdtvalue.startsWith("^") && !patdtvalue.startsWith(".*")){
			patdtvalue = ".*"+patdtvalue;
                }
		if (regex && 
                        !patdtvalue.endsWith("$") && !patdtvalue.endsWith(".*")){
			patdtvalue = patdtvalue+".*";
                }
		if (flag == 0){
			pat = Pattern.compile(patdtvalue);
                }
		else {
                    pat = Pattern.compile(patdtvalue, flag);
                }
		
		match = pat.matcher("");
	}
	
        // replace(str, old, new, flag)
	void compileReplace(Term term){
		if (term.getArg(1).isConstant() && (term.getArity() == 3 || term.getArg(3).isConstant())){
                    isCompiled = true;
                    String sflag = null;
                    if (term.getArity() == 4){
                            sflag = term.getArg(3).getName();
                    }                    
                    compilePattern(term.getArg(1).getLabel(), sflag, false);
		}
	}
		
	// replace('%abc@def#', '[^a-z0-9]', '-')
	public String replace(String str, String pat, String rep, String flag){ 
            if (! isCompiled){
                compilePattern(pat, flag, false);
            }
            match.reset(str);
            String res = match.replaceAll(rep);
            return res;
	}	
	
	public boolean regex(String str, String exp, String sflag){
		if (! isCompiled){                    
                    compilePattern(exp, sflag, true);
		}
		match.reset(str);
		return match.matches();
	}
	
	void compileSQL(Term term, ASTQuery ast){
		//sql = new SQLFun();
	}
	
	// @deprecated
	public ResultSet sql(IDatatype uri, IDatatype login, IDatatype passwd, IDatatype query){
		return null; //return sql.sql(uri, login, passwd, query);
	}
	
	public ResultSet sql(IDatatype uri, IDatatype driver, IDatatype login, IDatatype passwd, IDatatype query){
		return null; //return sql.sql(uri, driver, login, passwd, query);
	}
	
	/**
	 * xpath(?g, '/book/title')
	 */
	void compileXPath(Term term, ASTQuery ast){
		xfun = new XPathFun();
		if (ast == null) ast = ASTQuery.create();
		xfun.init(ast.getNSM(),  !true);
	}
	
	public XPathFun getXPathFun(){
		return xfun;
	}
	
	public IDatatype xpath(IDatatype doc, IDatatype exp){
		try {
		IDatatype dt = xfun.xpath(doc, exp);
		return dt;
		}
		catch (RuntimeException e){
			logger.error("XPath error: " + e.getMessage());
		}
		return null;
	}
	
	public VariableResolver getResolver(){
		return xfun.getResolver();
	}
	
	public void setResolver(VariableResolver res){
		xfun.set(res);
	}
	

	public void setProcessor(Object obj){
		processor = obj;
	}
        
        public Object getProcessor(){
            return processor;
        }
	
	public void setMethod(Method m){
		fun = m;
	}
	
	public Method getMethod() {
		return fun;
	}
	
	
	/**
	 * Load external method definition for ext:fun
	 * prefix ext: <function://package.className>
	 * ext:fun() 
	 */
        
        public void compile(){
            if (! isCompiled){
                isCompiled = true;
                compileExternal();
            }
        }
        
         void compileExternal() {
            setCorrect(false);
            try {
                String methodName = term.getLabel();
                if (!methodName.startsWith(functionPrefix)) {
                    String message = "Undefined function: " + methodName;
                    if (methodName.contains("://")) {
                        message += "\nThe prefix should start with \"" + functionPrefix + "\"";
                    }
                    logger.warn(message);
                    return;
                }
                int lio = methodName.lastIndexOf(".");
                if (lio == -1) {
                    logger.error("Undefined function: " + methodName);
                    return;
                }
                String packageName = methodName.substring(0, lio);
                String classPackage = packageName.substring(functionPrefix.length(), packageName.length());
                methodName = methodName.substring(packageName.length() + 1, methodName.length());

                ClassLoader cl = getClass().getClassLoader();
                Class className = cl.loadClass(classPackage);

                Class<IDatatype>[] aclasses = new Class[term.getArity()];
                for (int i = 0; i < aclasses.length; i++) {
                    aclasses[i] = IDatatype.class;
                }
               
                try {
                    Method singleton = className.getMethod("singleton", noargs);
                    setProcessor(singleton.invoke(className));
                }
                catch (NoSuchMethodException ex) {}
                
                if (getProcessor() == null) {
                    setProcessor(className.getDeclaredConstructor().newInstance());
                }
                setMethod(className.getMethod(methodName, aclasses));
                setCorrect(true);
            } catch (InvocationTargetException | ClassNotFoundException | SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                java.util.logging.Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, e);
            }
           
    }
	
	
	/**
	 * Eval external method
	 */
	public Object eval(IDatatype[] args){
		if (! isCorrect()) {
                    return null;
                }
		try {
			return getMethod().invoke(getProcessor(), args);
		} 
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                catch (NullPointerException ex){
                    
                }
		return null;
	}
        
        void compileCustom(Term term, ASTQuery ast){
            name = term.getLabel().substring(CUSTOM.length());
        }
        
        // for custom extension function 
        String getShortName(){
            return name;
        }

    /**
     * @return the isCorrect
     */
    public boolean isCorrect() {
        return isCorrect;
    }

    /**
     * @param isCorrect the isCorrect to set
     */
    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }


}
