package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.NSMANAGER;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 * <br>
 * This class is used to manage prefixes and namespaces.
 * <br>
 *
 * @author Olivier Corby
 */
public class NSManager extends ASTObject {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;
    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(NSManager.class);
    public static final String FPPN = "ftp://ftp-sop.inria.fr/wimmics/soft/pprint/";
    public static final String XSD = RDFS.XSD;
    public static final String XSI = "http://www.w3.org/2001/XMLSchema-instance#";
    public static final String XML = fr.inria.corese.sparql.datatype.RDF.XML;
    public static final String RDF = RDFS.RDF;
    public static final String OWL = RDFS.OWL;
    public static final String SKOS = "http://www.w3.org/2004/02/skos/core#";
    public static final String SPIN = "http://spinrdf.org/sp#";
    public static final String SQL = "http://ns.inria.fr/ast/sql#";
    public static final String FOAF = "http://xmlns.com/foaf/0.1/";
    public static final String RDFRESULT = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";
    public static final String SHACL = "http://www.w3.org/ns/shacl#";
    public static final String SHACL_PREFIX =  "sh";
    public static final String SHEX_SHACL = "http://ns.inria.fr/shex/shacl#";
    public static final String SHAPE = SHACL;
    public static final String SHACL_JAVA = "function://fr.inria.corese.core.extension.SHACL.";
    public static final String EXT_FUN    = "function://fr.inria.corese.core.extension.Extension.";
    public static final String DCTERM = "http://purl.org/dc/terms/";
    public static final String DBPEDIAFR = "http://fr.dbpedia.org/resource/";
    public static final String DBPEDIA = "http://dbpedia.org/resource/";
    public static final String RESOURCE = "http://ns.inria.fr/corese/";
    public static final String SHACL_SHACL = RESOURCE+"data/shaclshacl.ttl";
    
    public static final String COSNS = RDFS.COSNS;
    public static final String COS = RDFS.COS;
    public static final String SWL = ExpType.SWL;
    public static final String STL = ExpType.STL;
    public static final String D3 = STL + "d3#";
    public static final String STL_FORMAT = STL + "format/";
    public static final String JAVA = ExpType.EXT + "java/";
    public static final String DS = ExpType.EXT + "ds/";
    public static final String CAST = ExpType.EXT + "cast/";
    public static final String EXT = ExpType.EXT;
    public static final String DOM = ExpType.DOM;
    public static final String DT = ExpType.DT;
    public static final String USER = ExpType.UXT;
    public static final String KGRAM = ExpType.KGRAM;
    public static final String SPARQL = ExpType.SPARQL;
    public static final String CUSTOM = ExpType.CUSTOM;
    public static final String KPREF = ExpType.KPREF;
    // extended named graph: eng:describe eng:queries
    public static final String KGEXT = ExpType.KGRAM + "extension/";
    // construct extended named graph
    public static final String KGEXTCONS = ExpType.KGRAM + "construct/";
    public static final String KEPREF = "eng";
    public static final String KECPREF = "cw";
    static final String FPPP = "fp";
    public static final String PPN = KGRAM + "pprinter/";
    static final String PPP = "pp";
    private static final String SPIN_PREF = "sp";
    private static final String FOAF_PREF = "foaf";
    public static final String STL_PREF = "st";
    public static final String SWL_PREF = "sw";
    public static final String EXT_PREF = "xt";
    public static final String SPARQL_PREF = "rq";
    public static final String OWLRL = SWL + "owlrl";
    public static final String RDFSRL = SWL + "rdfs";

    /**
     * prefix seed (ns1, ns2,...)
     */
    private static final String seed = "ns";
    private static final String DOT = ".";
    public static final String HASH = "#";
    static final String NL = System.getProperty("line.separator");
    static final char[] END_CHAR = {'#', '/', '?'}; //, ':'}; // may end an URI ...
    static final String[] PB_CHAR = {"(", ")", "'", "\"", ","};
    static final String pchar = ":";
    int count = 0;
    static final NSManager nsm = create();
    static final HashMap<String, Boolean> number;

    HashMap<String, String> def; // system namespace with prefered prefix
    HashMap<String, Integer> index;  // namespace -> number
    HashMap<String, String> tns;     // namespace -> prefix
    HashMap<String, String> tprefix; // prefix -> namespace
    HashMap<String, String> trecord;
    String base;
    URI baseURI;

    /**
     * Corresponds to the namespaces declared by default
     *
     */
    private String defaultNamespaces = null;
    private boolean record = false;
    
    static {
        number = new HashMap<>();
        number();
    }
    
    // for pretty printing without ^^datatype
    static void number() {
        number.put(fr.inria.corese.sparql.datatype.RDF.xsdinteger, Boolean.TRUE);
        //number.put(fr.inria.corese.sparql.datatype.RDF.xsdint, Boolean.TRUE);
        //number.put(fr.inria.corese.sparql.datatype.RDF.xsdfloat, Boolean.TRUE);
        //number.put(fr.inria.corese.sparql.datatype.RDF.xsddouble, Boolean.TRUE);
        number.put(fr.inria.corese.sparql.datatype.RDF.xsddecimal, Boolean.TRUE);
    }

    private NSManager() {
        def = new HashMap<String, String>();
        tprefix = new HashMap<String, String>();
        tns = new HashMap<String, String>();
        index = new HashMap<String, Integer>();
        trecord = new HashMap<String, String>();
        define();
    }

    private NSManager(String defaultNamespaces) {
        this();
        this.defaultNamespaces = defaultNamespaces;
    }

    public static NSManager nsm() {
        return nsm;
    }

    /**
     * Warning: to be used only when we don't need default namespaces and their
     * prefixes
     *
     * @return a NamespaceManager with defaultNamespaces = ""
     */
    public static NSManager create() {
        NSManager nsm = new NSManager(null);
        nsm.init();
        return nsm;
    }

    /**
     *
     * @deprecated
     */
    public static NSManager create(String defaultNamespaces) {
        NSManager nsm = new NSManager(defaultNamespaces);
        nsm.init();
        return nsm;
    }

    public NSManager copy() {
        NSManager nsm = create();
        nsm.setBase(getBase());
        for (String p : getPrefixSet()) {
            nsm.definePrefix(p, getNamespace(p));
        }
        return nsm;
    }

    /**
     * Import nsm definitions
     */
    public NSManager complete(NSManager nsm) {
        if (getBase() == null) {
            setBase(nsm.getBase());
        }
        for (String p : nsm.getPrefixSet()) {
            definePrefix(p, nsm.getNamespace(p));
        }
        return this;
    }

    public void init() {
        initDefault();
        defNamespace();
    }

    public Iterable<String> getNamespaces() {
        return tns.keySet();
    }

    public Iterable<String> getPrefixes() {
        return getPrefixEnum();
    }

    public void clear() {
        tns.clear();
        index.clear();
        tprefix.clear();
        count = 0;
    }

    /**
     * application specific namespace/prefix if any
     */
    public void defNamespace() {
        if (defaultNamespaces != null) {
            String ns, p;
            StringTokenizer st = new StringTokenizer(defaultNamespaces);
            while (st.hasMoreTokens()) {
                p = st.nextToken();
                ns = st.nextToken();
                defNamespace(ns, p);
            }
        }
    }

    // default system namespaces, not for application namespace
    void define() {
        def.put(RDFS.XML, RDFS.XMLPrefix);
        def.put(RDFS.RDF, RDFS.RDFPrefix);
        def.put(RDFS.RDFS, RDFS.RDFSPrefix);
        def.put(RDFS.XSD, RDFS.XSDPrefix);
        def.put(RDFS.OWL, RDFS.OWLPrefix);
        def.put(SKOS, "skos");

        def.put(SPIN, SPIN_PREF);
        def.put(FOAF, FOAF_PREF);
        def.put("http://dbpedia.org/ontology/", "dbo");
        def.put("http://www.w3.org/2004/02/skos/core#", "skos");
        def.put(DCTERM, "dc");
        def.put(DBPEDIAFR, "db");
        def.put(DBPEDIA, "dbe");

        def.put(KGRAM, KPREF);
        def.put(KGEXT, KEPREF);
        def.put(KGEXTCONS, KECPREF);
        def.put(COS, RDFS.COSPrefix);
        def.put(FPPN, FPPP);
        def.put(PPN, PPP);
        def.put(STL, STL_PREF);
        def.put(D3, "d3");
        def.put(SWL, SWL_PREF);
        def.put(EXT, EXT_PREF);
        def.put(USER, "us");
        def.put(ExpType.DT, "dt");
        def.put(CUSTOM, "cs");
        def.put(SPARQL, SPARQL_PREF);
        //def.put(SHACL, "xsh");
        def.put(SHACL, SHACL_PREFIX);
        def.put(SHEX_SHACL, "shex");
        def.put(SHACL_JAVA, "jsh");
        def.put(EXT_FUN, "fun");
        def.put("http://example.org/ns#", "ex");
        def.put(JAVA, "java");
        def.put(DS, "ds");
        def.put(CAST, "cast");
        def.put(DOM, "dom");
    }

    // add default namespaces
    void initDefault() {
        for (String ns : def.keySet()) {
            defNamespace(ns, def.get(ns));
        }
        defPrefix("xsh", SHACL);
    }

    public boolean isSystem(String ns) {
        return def.containsKey(ns);
    }

    public boolean isNamespace(String ns) {
        return tns.containsKey(ns);
    }
    
    public static boolean isNumber(String ns) {
        return number.get(ns) != null;
    }

    /**
     * Define a namespace, returns the prefix
     */
    public String defNamespace(String ns) {
        if (ns == null) {
            return null;
        } else if (!tns.containsKey(ns)) {
            defNamespace(ns, makePrefix(ns));
        }
        return getPrefix(ns);
    }

    String makePrefix(String ns) {
        if (def.get(ns) != null) {
            return def.get(ns);
        } else {
            return createPrefix(seed);
        }
    }

    String createPrefix(String p) {
        if (!p.equals(seed)) {
            return p;
        }
        String str = (count == 0) ? seed : (seed + count);
        count++;
        return str;
    }

    public String definePrefix(String prefix, String ns) {
        if (prefix.equals(pchar)) {
            prefix = "";
        }
        return defNamespace(ns, prefix);
    }

    /**
     * function://fr.inria.Extern should ends with a "."
     */
    String prepare(String ns) {
        if (ns.startsWith(KeywordPP.CORESE_PREFIX) && !ns.endsWith(DOT)) {
            ns += DOT;
        }
        return ns;
    }

    public String defNamespace(String ns, String prefix) {
        if (ns != null && prefix != null) {
            ns = prepare(ns);
            prefix = createPrefix(prefix);
            if (!tns.containsKey(ns)) {
                tns.put(ns, prefix);
            }
            defPrefix(prefix, ns);
            index.put(ns, tns.size());
        }
        return prefix;
    }

    /**
     * Returns the prefix of the namespace
     */
    public String getPrefix(String ns) {
        return (String) tns.get(ns);
    }

    void defPrefix(String prefix, String ns) {
        tprefix.put(prefix, ns);
    }

    public String getNamespace(String prefix) {
        return tprefix.get(prefix);
    }

    Iterable<String> getPrefixEnum() {
        return tprefix.keySet();
    }

    public Set<String> getPrefixSet() {
        return tprefix.keySet();
    }

    public int getIndex(String ns) {
        if (getPrefix(ns) == null) {
            defNamespace(ns);
        }
        return (index.get(ns)).intValue();
    }

    public String toPrefix(String nsname) {
        return toPrefix(nsname, false);
    }

    /**
     * in XML, if the prefix is empty (:abc) do not add the ":"
     */
    public String toPrefixXML(String nsname) {
        return toPrefix(nsname, false, true);
    }

    /**
     * toPrefix() unless there are forbidden characters such as ( ) in this case
     * return <uri>
     */
    public String toPrefixURI(String nsname) {
        return toPrefixURI(nsname, true);
    }

    public String toPrefixURI(String nsname, boolean skip) {
        return toPrefixURI(nsname, skip, false);
    }

    /**
     *
     * @param skip: do not generate prefxi when prefix undefined
     * @param display: display mode, generate prefixed named even with ' or ()
     */
    public String toPrefixURI(String nsname, boolean skip, boolean display) {
        if (!display && containsChar(nsname)) {
            return uri(nsname);
        } else {
            String str = toPrefix(nsname, skip);
            if (str.equals(nsname)) {
                return uri(nsname);
            } else {
                return str;
            }
        }
    }

    boolean containsChar(String str) {
        for (String s : PB_CHAR) {
            if (str.contains(s)) {
                return true;
            }
        }
//        String name = strip(str);
//        if (name != str && name.length()>0 && name.charAt(0) >= '0' && name.charAt(0) <= '9'){
//            return true;
//        }

        return false;
    }

    String uri(String str) {
        return "<" + str + ">";
    }

    /**
     * If skip, if no prefix for this namespace, return nsname, else create a
     * prefix
     */
    public String toPrefix(String nsname, boolean skip) {
        return toPrefix(nsname, skip, false);
    }

    public String toPrefix(String nsname, boolean skip, boolean xml) {
        String ns = namespace(nsname);
        if (ns == null || ns.equals("") || ns.equals(nsname)) {
            return nsname;
        }
        String p = getPrefix(ns);
        if (p == null) {
            if (skip) {
                return nsname;
            } else {
                p = defNamespace(ns);
            }
        }
        String str = p;
        if (!(xml && p.equals(""))) {
            str += pchar;
        }
        record(ns);
        str += nsname.substring(ns.length());
        return str;
    }

    /**
     * Remember namespace is used for later pprint
     */
    void record(String ns) {
        if (isRecord() && !trecord.containsKey(ns)) {
            trecord.put(ns, ns);
        }
    }

    /**
     * pname is a QNAME, expand with namespace
     *
     * @param pname
     * @return
     */
    public String toNamespace(String pname) {
        if (pname == null) {
            return null;
        }

        for (String p : tprefix.keySet()) {
            if (pname.startsWith(p) && pname.indexOf(pchar) == p.length()) {
                record(getNamespace(p));
                return getNamespace(p) + pname.substring(p.length() + 1);
            }
        }
        return pname;
    }

    /**
     * With base
     */
    public String toNamespaceB(String str) {
        String uri = toNamespace(str);
        return toBase(uri);
    }

    public String toBase(String str) {
        if (isBase()) {
            try {
                URI uri = new URI(str);
                if (!uri.isAbsolute()) {
                    str = resolve(str);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return str;
    }

    boolean isAbsoluteURI(String s) {
        try {
            return new URI(s).isAbsolute();
        } catch (URISyntaxException e) {
        }
        return false;
    }

    String resolve(String str) {
        if (str.equals("")) {
            return base;
        }
        URI uri = baseURI.resolve(str);
        String res = uri.toString();

        if (res.matches("file:/[^/].*")) {
            // replace file:/ by file:///
            res = res.substring(5);
            res = "file://" + res;
        }

        return res;
    }

    @Override
    public String toString() {
        return toString(null, false, true);
    }

    public String toString(String title) {
        return toString(title, false, true);
    }

    public String toString(String title, boolean all) {
        return toString(title, all, true);
    }

    public String toString(String title, boolean all, boolean bas) {
        if (title == null) {
            title = "prefix";
        }
        StringBuilder sb = new StringBuilder();
        if (bas && base != null) {
            sb.append("base <").append(base).append(">").append(NL);
        }
        for (String p : getPrefixSet()) {
            String ns = getNamespace(p);
            if (all || isDisplayable(ns)) {
                sb.append(title).append(" ").append(p);
                sb.append(": <").append(getNamespace(p)).append(">");
                if (title.equals("@prefix")) {
                    sb.append(" ").append(".");
                }
                sb.append(NL);
            }
        }
        return sb.toString();
    }

    public IDatatype turtle(IDatatype dt) {
        return turtle(dt, false);
    }

    public IDatatype turtle(IDatatype dt, boolean force) {
        return turtle(dt, force, false);
    }

    /**
     *
     * @param force: generate a prefix if no prefix exist
     * @param display: display mode, generate prefixed name even with ' or ()
     */
    public IDatatype turtle(IDatatype dt, boolean force, boolean display) {
        String label = dt.getLabel();
        if (dt.isURI()) {
            setRecord(true);
            String uri = toPrefixURI(label, !force, display);
            dt = DatatypeMap.newStringBuilder(uri);
        } else if (dt.isLiteral()) {
            if ((dt.getCode() == IDatatype.INTEGER
                    && dt.getDatatypeURI().equals(fr.inria.corese.sparql.datatype.XSD.xsdinteger)
                    && (!(label.startsWith("0") && label.length() > 1)))
                    || (dt.getCode() == IDatatype.BOOLEAN && (label.equals("true") || label.equals("false")))) {
                // print string value as is
            } else {
                // add quotes around string, add lang tag if any
                dt = DatatypeMap.newStringBuilder(dt.toString());
            }
        }
        return dt;
    }

    boolean isDisplayable(String ns) {
        if (isRecorded(ns)) {
            return true;
        }
        return !isSystem(ns);
    }

    public boolean isRecorded(String ns) {
        return isRecord() && trecord.containsKey(ns);
    }

    public void setBase(String s) {
        if (s == null) {
            baseURI = null;
        } else {
            s = getBase(s);
            base = s;
            try {
                baseURI = new URI(s);
            } catch (URISyntaxException e) {
                baseURI = null;
                logger.error(e.getMessage());
            }
        }
    }

    public static String toURI(String path) {
        String str;
        try {
            str = new URL(path).toString();
        } catch (MalformedURLException ex) {
            try {
                str = new File(path).toURL().toString();
            } catch (MalformedURLException ex1) {
                return path;
            }
        }
        if (str.startsWith("file:/") && !str.startsWith("file://")) {
            str = str.replace("file:", "file://");
        }
        return str;
    }

    /**
     * s can be relative to the actual base ...
     */
    String getBase(String s) {
        if (isBase()) {
            s = toBase(s);
        }
        return s;
    }

    public boolean isBase() {
        return (baseURI != null);
    }

    public String getBase() {
        return base;
    }

    /**
     * Return the namespace of this QName
     */
    public String getQNamespace(String pname) {
        for (String p : tprefix.keySet()) {
            if (pname.startsWith(p) && pname.indexOf(pchar) == p.length()) {
                return getNamespace(p);
            }
        }
        return null;
    }

    public String getPackage(String qname) {
        String ns = getQNamespace(qname);
        int ind = ns.indexOf(KeywordPP.CORESE_PREFIX);
        if (ind != 0) {
            return ns;
        }
        ns = ns.substring(KeywordPP.CORESE_PREFIX.length());
        if (ns.endsWith(".")) {
            ns = ns.substring(0, ns.length() - 1);
        }
        return ns;

    }

    public String toNamespaceBN(String str) {
        return toNamespaceB(str);
    }

    @Override
    public int size() {
        return tns.size();
    }

    public boolean isUserDefine() {
        return size() > def.size();
    }

    public String stripns(String name, String namespace, boolean refp) {
        // if namespace not null, removes it
        // if refp add a #
        return ((namespace != null) && (inNamespace(name, namespace)))
                ? ((refp) ? HASH + strip(name) : strip(name)) : name;
    }

    public String strip(String name) {
        // remove namespace and #
        return nstrip(name);
    }

    public String strip(String name, String ns) {
        // remove ns from name
        return name.substring(ns.length());
    }

    public static String nstrip(String name) {
        // remove namespace
        int index;
        for (int i = 0; i < END_CHAR.length; i++) {
            index = name.lastIndexOf(END_CHAR[i]);// ???
            if (index != -1) {
                return name.substring(index + 1);
            }
        }
        return name;
    }

    public boolean sysNamespace(String name) {
        for (String ns : def.keySet()) {
            if (inNamespace(name, ns)) {
                return true;
            }
        }
        return false;
    }

    public boolean inNamespace(String type, String namespace) {
        // retourne si un type appartient au namespace
        if (namespace == null) {
            return true;
        } else {
            return type.startsWith(namespace);
        }
    }

    public static String namespace(String type) {  //retourne le namespace d'un type
        if (type.startsWith(HASH)) {
            return "";
        }
        int index;
        for (int i = 0; i < END_CHAR.length; i++) {
            index = type.lastIndexOf(END_CHAR[i]);
            if (index != -1) {
                String str = type.substring(0, index + 1);
                if (!str.equals("http://")) {
                    return str;
                }
            }
        }
        return "";
    }

    /*
     return last occurrence of pat (e.g. '/') in str
     if pat is last, find preceding occurrence
     */
    static int getIndex(String str, char pat) {
        int index = str.lastIndexOf(pat);
        if (index == str.length() - 1) {
            logger.debug(str + " " + index + " " + str.lastIndexOf(pat, index));
            return str.lastIndexOf(pat, index - 1);
        } else {
            return index;
        }
    }

    public static String putNamespace(String ns, String label) {
        return ns + label;
    }

    public String getDefaultNamespaces() {
        return defaultNamespaces;
    }

    @Override
    public PointerType pointerType() {
        return NSMANAGER;
    }

    @Override
    public IDatatype getList() {
        ArrayList<IDatatype> list = new ArrayList<IDatatype>();
        for (String p : tprefix.keySet()) {
            String n = getNamespace(p);
            if (isDisplayable(n)) {
                IDatatype ldt = DatatypeMap.createList(DatatypeMap.newInstance(p), DatatypeMap.newResource(n));
                list.add(ldt);
            }
        }
        return DatatypeMap.createList(list);
    }

    @Override
    public String getDatatypeLabel() {
        return String.format("[NSManager: size=%s]", size());
    }

    /**
     * @return the record
     */
    public boolean isRecord() {
        return record;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(boolean record) {
        this.record = record;
    }

}
