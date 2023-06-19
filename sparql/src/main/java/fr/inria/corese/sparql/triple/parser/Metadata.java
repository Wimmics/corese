package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Metadata extends ASTObject
        implements Iterable<String> {
    static final String NL = System.getProperty("line.separator");
    static final int UNDEFINED = -1;
    static final String AT = "@";
    public static final int TEST = 0;
    public static final int DEBUG = 1;
    public static final int TRACE = 2;
    public static final int PUBLIC = 3;
    public static final int IMPORT = 4;
    public static final int NEW = 5;

    // Query
    public static final int SPARQL = 10; // federate sparql
    public static final int RELAX = 11;
    public static final int MORE = 12;
    public static final int FEDERATE = 13;
    public static final int DISPLAY = 14;
    public static final int BIND = 15; // @event @bind
    public static final int TYPE = 16;
    public static final int COMPILE = 17;
    public static final int SKIP = 18;
    public static final int PATH = 19;
    public static final int ENCODING = 20;
    public static final int DB = 21;
    public static final int DB_FACTORY = 22;
    public static final int ALGEBRA = 23;
    public static final int BOUNCE = 24;
    public static final int SPARQL10 = 25;
    public static final int TRAP = 26;
    public static final int FEDERATION = 27;
    public static final int COUNT = 28;
    public static final int PARALLEL = 29;
    public static final int SEQUENCE = 30;
    public static final int VARIABLE = 31;
    public static final int SERVER = 32;
    public static final int PROVENANCE = 33;
    public static final int DUPLICATE = 34;
    public static final int LDPATH = 35;
    public static final int ENDPOINT = 36;
    public static final int FILE = 37;
    public static final int DETAIL = 38;
    public static final int ACCEPT = 39;
    public static final int REJECT = 40;
    public static final int OPTION = 41;
    public static final int SPLIT = 42;
    public static final int LOCK = 43;
    public static final int UNLOCK = 44;
    public static final int LIMIT = 45;
    public static final int GRAPH = 46;
    public static final int FROM = 47;
    public static final int UPDATE = 48;
    public static final int BINDING = 50; // service binding
    public static final int INDEX = 51; // service binding
    public static final int LOG = 52;
    public static final int EXPLAIN = 53;
    public static final int WHY = 54;
    public static final int MESSAGE = 55;
    public static final int MERGE_SERVICE = 56;
    public static final int BROWSE = 57;
    public static final int EVENT = 58;
    public static final int FORMAT = 59;
    public static final int SELECT = 60;
    public static final int REPORT = 61;
    public static final int DISTINCT = 62;
    public static final int ENUM = 63;
    public static final int HEADER = 64;
    public static final int COOKIE = 65;
    public static final int TIMEOUT = 66;
    // data producer return asserted and nested edge for sparql query
    public static final int RDF_STAR_SELECT = 67;
    // delete update query remove (nested) edge
    public static final int RDF_STAR_DELETE = 68;

    // uncertainty triple metadata
    public static final int METADATA = 70;
    public static final int VISITOR = 71;
    public static final int MOVE = 72;
    public static final int PATH_TYPE = 73;
    public static final int SLICE = 74;
    public static final int FOCUS = 75;

    static final String PREF = NSManager.KGRAM;
    public static final String DISPLAY_TURTLE = PREF + "turtle";
    public static final String DISPLAY_JSON_LD = PREF + "jsonld";
    public static final String DISPLAY_RDF_XML = PREF + "rdfxml";

    public static final String DISPLAY_JSON = PREF + "json";
    public static final String DISPLAY_XML = PREF + "xml";
    public static final String DISPLAY_RDF = PREF + "rdf";
    public static final String DISPLAY_MARKDOWN = PREF + "markdown";

    public static final String RELAX_URI = PREF + "uri";
    public static final String RELAX_PROPERTY = PREF + "property";
    public static final String RELAX_LITERAL = PREF + "literal";

    public static final String PROBE = PREF + "probe";
    public static final String VERBOSE = PREF + "verbose";
    public static final String SELECT_SOURCE = PREF + "select";
    public static final String SELECT_FILTER = PREF + "selectfilter";
    public static final String GROUP = PREF + "group";
    public static final String MERGE = PREF + "merge";
    public static final String SIMPLIFY = PREF + "simplify";
    public static final String EXIST = PREF + "exist";
    public static final String SKIP_STR = PREF + "skip";
    public static final String ALL = "all";
    public static final String EMPTY = "empty";

    public static final String DISTRIBUTE_NAMED = PREF + "distributeNamed";
    public static final String DISTRIBUTE_DEFAULT = PREF + "distributeDefault";
    public static final String REWRITE_NAMED = PREF + "rewriteNamed";

    public static final String METHOD = "@method";
    public static final String ACCESS = "@access";
    public static final String LEVEL = "@level";
    public static final String POST = "@post";
    public static final String GET = "@get";
    public static final String FORM = "@form";
    public static final String OLD_SERVICE = "@oldService";
    public static final String SHOW = "@show";
    public static final String SELECTION = "@selection";
    public static final String DISCOVERY = "@discovery";
    public static final String LOOP = AT + URLParam.LOOP;
    public static final String START = AT + URLParam.START;
    public static final String UNTIL = AT + URLParam.UNTIL;;
    public static final String HIDE = "@hide";
    public static final String LIMIT_STR = "@limit";

    public static final String FED_BGP = "@federateBgp";
    public static final String FED_JOIN = "@federateJoin";
    public static final String FED_OPTIONAL = "@federateOptional";
    public static final String FED_MINUS = "@federateMinus";
    public static final String FED_UNDEFINED = "@federateUndefined";
    public static final String FED_COMPLETE = "@federateComplete";
    public static final String FED_PARTITION = "@federatePartition";
    public static final String FED_SUCCESS = "@" + URLParam.FED_SUCCESS;
    public static final String FED_LENGTH = "@" + URLParam.FED_LENGTH;
    public static final String FED_INCLUDE = "@" + URLParam.FED_INCLUDE;
    public static final String FED_EXCLUDE = "@exclude";
    public static final String FED_BLACKLIST = "@blacklist";
    public static final String FED_WHITELIST = "@whitelist";
    public static final String FED_CLASS = "@federateClass";
    public static final String SAVE = "@save";

    private static HashMap<String, Integer> annotation;
    private static HashMap<Integer, String> back;

    HashMap<String, String> map;
    HashMap<String, List<String>> value;
    HashMap<String, IDatatype> literal;
    // inherited metadata such as @public { function ... }
    private Metadata metadata;

    static {
        initAnnotate();
    }

    static void initAnnotate() {
        annotation = new HashMap();
        back = new HashMap();
        define("@debug", DEBUG);
        define("@trace", TRACE);
        define("@test", TEST);
        define("@new", NEW);
        define("@parallel", PARALLEL);
        define("@sequence", SEQUENCE);
        define("@variable", VARIABLE);
        define("@provenance", PROVENANCE);
        define("@log", LOG);
        define("@duplicate", DUPLICATE);
        define("@distinct", DISTINCT);
        define("@count", COUNT);
        define("@server", SERVER);
        define("@export", PUBLIC);
        define("@public", PUBLIC);
        define("@more", MORE);
        define("@relax", RELAX);
        define("@federate", FEDERATE);
        define("@federation", FEDERATION);
        define("@sparql", SPARQL);
        define("@index", INDEX);
        define(LIMIT_STR, LIMIT);
        define("@slice", SLICE);
        define("@move", MOVE);
        define("@bounce", BOUNCE);
        define("@sparqlzero", SPARQL10);
        define("@encoding", ENCODING);
        define("@bind", BIND); // @event @bind
        define("@binding", BINDING); // service bind: to differ from @event @bind
        define("@import", IMPORT);
        define("@display", DISPLAY);
        define("@type", TYPE);
        define("@compile", COMPILE);
        define("@path", PATH);
        define("@pathtype", PATH_TYPE);
        define("@skip", SKIP);
        define("@db", DB);
        define("@dbfactory", DB_FACTORY);
        define("@algebra", ALGEBRA);
        define("@metadata", METADATA);
        define("@visitor", VISITOR);
        define("@trap", TRAP);
        define("@ldpath", LDPATH);
        define("@endpoint", ENDPOINT);
        define("@file", FILE);
        define("@detail", DETAIL);
        define("@report", REPORT);
        define("@header", HEADER);
        define("@cookie", COOKIE);
        define("@timeout", TIMEOUT);
        define("@enum", ENUM);
        define("@accept", ACCEPT);
        define("@reject", REJECT);
        define("@option", OPTION);
        define("@split", SPLIT);
        define("@lock", LOCK);
        define("@unlock", UNLOCK);
        define("@graph", GRAPH);
        define("@from", FROM);
        define("@explain", EXPLAIN);
        define("@why", WHY);
        define("@message", MESSAGE);
        define("@browse", BROWSE);
        define("@merge", MERGE_SERVICE);
        define("@focus", FOCUS);
        define("@format", FORMAT);
        // update query evaluated as select query
        define("@select", SELECT);
        define("@selectrdfstar", RDF_STAR_SELECT);
        define("@deleterdfstar", RDF_STAR_DELETE);

        define("@update", UPDATE);
        define("@event", EVENT);
        // define(META_BEFORE, BEFORE);
        // define(META_AFTER, AFTER);
        // define(META_PRODUCE,PRODUCE);
        // define(META_RESULT, RESULT);
        // define(META_STATEMENT, STATEMENT);
    }

    static void define(String str, int type) {
        annotation.put(str, type);
        back.put(type, str);
    }

    public Metadata() {
        map = new HashMap<>();
        value = new HashMap();
        literal = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Metadata:");
        sb.append(NL);
        for (String m : this) {
            sb.append(m);
            List<String> list = getValues(m);
            if (list != null && !list.isEmpty()) {
                sb.append(" : ");
                sb.append(getValues(m));
            }
            sb.append(NL);
        }

        for (String key : literal.keySet()) {
            sb.append(key).append(" : ").append(getDatatypeValue(key));
        }

        return sb.toString();
    }

    /**
     * Subset of Metadata for xt:sparql() see PluginImpl
     */
    public Metadata selectSparql() {
        if (hasMetadata(REPORT)) {
            return new Metadata().add(REPORT);
        }
        return null;
    }

    public Metadata share(Metadata meta) {
        add(meta);
        return this;
    }

    public Metadata add(String str) {
        map.put(str, str);
        return this;
    }

    public Metadata add(int type) {
        String name = name(type);
        if (name != null) {
            add(name);
        }
        return this;
    }

    public Metadata remove(int type) {
        String name = name(type);
        if (name != null) {
            map.remove(name);
        }
        return this;
    }

    public Metadata add(int type, String value) {
        String name = name(type);
        if (name != null) {
            add(name, value);
        }
        return this;
    }

    public void add(String name, String val) {
        add(name);
        List<String> list = value.get(name);
        if (list == null) {
            list = new ArrayList<>();
            value.put(name, list);
        }
        if (!list.contains(val)) {
            list.add(val);
        }
    }

    public void set(int type, List<String> list) {
        String name = name(type);
        if (name != null) {
            set(name, list);
        }
    }

    public void set(String name, List<String> list) {
        if (!list.isEmpty()) {
            add(name, list.get(0));
        }
        value.put(name, list);
    }

    public void add(String name, Constant val) {
        if (val.isResource()) {
            add(name, val.getLongName());
        } else if (val.isLiteral()) {
            literal.put(name, val.getDatatypeValue());
        }
    }

    public void add(int type, IDatatype val) {
        add(name(type), val);
    }

    public void add(String name, IDatatype val) {
        if (val.isURI()) {
            add(name, val.getLabel());
        } else if (val.isLiteral()) {
            literal.put(name, val);
        }
    }

    public boolean hasMetadata(int type) {
        String str = name(type);
        if (str == null) {
            return false;
        }
        return hasMetadata(str);
    }

    public boolean hasMetadata(int... type) {
        for (int val : type) {
            if (hasMetadata(val)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMetadata(String... type) {
        for (String val : type) {
            if (hasMetadata(val)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMetadata(String name) {
        return map.containsKey(name);
    }

    // add without overloading local
    public void add(Metadata m) {
        for (String name : m) {
            if (!hasMetadata(name)) {
                add(name);
                if (m.getValues(name) != null) {
                    value.put(name, m.getValues(name));
                }
            }
        }
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public String getValue(int type) {
        return getValue(name(type));
    }

    public IDatatype getDatatypeValue(int type) {
        return getDatatypeValue(name(type));
    }

    public IDatatype getDatatypeValue(String type) {
        return literal.get(type);
    }

    public int intValue(int type) {
        IDatatype dt = getDatatypeValue(type);
        if (dt == null) {
            return -1;
        }
        return dt.intValue();
    }

    public boolean hasDatatypeValue(int type) {
        return getDatatypeValue(type) != null;
    }

    public boolean hasDatatypeValue(String type) {
        return getDatatypeValue(type) != null;
    }

    public String getStringValue(int type) {
        String value = getValue(type);
        if (value == null) {
            return null;
        }
        return NSManager.nstrip(value);
    }

    boolean hasReportKey(String key) {
        List<String> list = getValues(REPORT);
        if (list == null) {
            return true;
        }
        // @report empty: empty is not a key
        if (list.size() == 1 && list.contains(EMPTY)) {
            return true;
        }
        return list.contains(key);
    }

    public boolean hasValue(int meta) {
        return getValue(meta) != null;
    }

    public boolean hasValue(int meta, String value) {
        String str = getValue(meta);
        return str != null && str.equals(value);
    }

    public boolean hasValues(int meta, String value) {
        List<String> list = getValues(meta);
        if (list == null) {
            return false;
        }
        return list.contains(value);
    }

    public String getValue(String name) {
        if (name == null) {
            return null;
        }
        List<String> list = getValues(name);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<String> getValues(int type) {
        return getValues(name(type));
    }

    public List<String> getValues(String name) {
        if (name == null) {
            return null;
        }
        return value.get(name);
    }

    @Override
    public Iterator<String> iterator() {
        return map.keySet().iterator();
    }

    public Collection<String> getMetadataList() {
        return map.keySet();
    }

    public int type(String name) {
        Integer i = annotation.get(name);
        if (i == null) {
            i = UNDEFINED;
        }
        return i;
    }

    public String name(int type) {
        return back.get(type);
    }

    @Override
    public PointerType pointerType() {
        return PointerType.METADATA;
    }

    @Override
    public IDatatype getList() {
        ArrayList<IDatatype> list = new ArrayList<IDatatype>();
        for (String key : map.keySet()) {
            IDatatype name = DatatypeMap.newLiteral(key);
            list.add(name);
        }
        return DatatypeMap.createList(list);
    }

    @Override
    public String getDatatypeLabel() {
        return String.format("[Metadata: size=%s]", size());
    }

    /**
     * @return the metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    // ________________________________________________

    // @graph <server1> <g1> <g2> <server2> <g3>
    public List<String> getGraphList(String service) {
        List<String> graphList = getValues(FROM);
        List<String> serverList = getValues(FEDERATE);
        ArrayList<String> res = new ArrayList<>();
        boolean find = false;
        if (graphList != null && serverList != null) {
            for (String str : graphList) {
                if (find) {
                    if (serverList.contains(str)) {
                        break;
                    } else {
                        res.add(str);
                    }
                } else if (str.equals(service)) {
                    find = true;
                }
            }
        }

        return res;
    }

}
