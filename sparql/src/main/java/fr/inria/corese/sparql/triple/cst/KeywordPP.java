package fr.inria.corese.sparql.triple.cst;

import fr.inria.corese.sparql.datatype.RDF;



public interface KeywordPP {

	public final static String VAR1 = "?";
	public final static String VAR2 = "$";
	public final static String SDT = "^^";
	public final static String LANG = "@";
    public final static String QUOTE = "'";    
    public final static String DQUOTE = "\"";    
    public final static String TQUOTE = "\"\"\"";    
    public final static String BN = RDF.BLANKSEED ; 

    public static final String CORESE_PREFIX = "function://";
    
    /** Constants used for the pretty printer */
    public final static String BASE = "base";
    public final static String PREFIX = "prefix";
    public final static String AS = "as";
    public final static String SELECT = "select";
    public final static String DELETE = "delete";
    public final static String ASK = "ask";
    public final static String CONSTRUCT = "construct";
    public final static String INSERT = "insert";
    public final static String DATA = "data";
    public final static String DESCRIBE = "describe";
    public final static String DEBUG = "debug";
    public final static String NOSORT = "nosort";
    public final static String ONE = "one";
    public final static String MORE = "more";
    public final static String LIST = "list";
    public final static String MERGE = "merge";
    public final static String GROUP = "group";
    public final static String GROUPBY = "group by";
    public final static String COUNT = "count";
    public final static String SORT = "sort";
    public final static String DISPLAY = "display";
    public final static String TABLE = "table";
    public final static String DRDF = "rdf";
    public final static String FLAT = "flat";
    public final static String ASQUERY = "asquery";
    public final static String XML = "xml";
    public final static String BLANK = "blank";
    public final static String DISTINCT = "distinct";
    public final static String REDUCED = "reduced";
    public final static String SORTED = "sorted";
    public final static String STAR = "*";
    public final static String PROJECTION = "projection";
    public final static String RESULT = "result";
    public final static String THRESHOLD = "threshold";
    public final static String FROM  = "from";
    public final static String NAMED = "named";
    public final static String WHERE = "where";
    public final static String GRAPH = "graph";
    public final static String STATE = "state";
    public final static String LEAF  = "leaf";
    public final static String TUPLE = "tuple";
    public final static String JOIN  = "join";

    public final static String SCORE = "score";
    public final static String FILTER = "filter";
    public final static String OPTIONAL = "optional";
    public final static String UNION = "union";
    public final static String MINUS = "minus";
    public final static String DOT = " . ";
    public final static String COMMA = ",";
    public final static String ORDERBY = "order by";
    public final static String DESC = "desc";
    public final static String DISTANCE = "distance";
    public final static String LIMIT = "limit";
    public final static String HAVING = "having";
    public final static String OFFSET = "offset";
    public final static String BINDINGS = "values";
    public final static String UNDEF = "UNDEF";
    public final static String PRAGMA = "pragma";

    public final static String SPACE = " ";
    public final static String SPACE_LN = " " + System.getProperty("line.separator");
    
    public final static String OPEN_BRACKET = "{";
    public final static String CLOSE_BRACKET = "}";
    public final static String OPEN_SQUARE_BRACKET = "[";
    public final static String CLOSE_SQUARE_BRACKET = "]";
    public final static String OPEN_PAREN = "(";
    public final static String CLOSE_PAREN = ")";
    public final static String OPEN = "<";
    public final static String CLOSE = ">";
	
}
