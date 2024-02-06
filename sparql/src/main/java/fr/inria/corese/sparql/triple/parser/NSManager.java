package fr.inria.corese.sparql.triple.parser;

import static fr.inria.corese.kgram.api.core.PointerType.NSMANAGER;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.cst.RDFS;

/**
 * <p>
 * Title: Corese
 * </p>
 * <p>
 * Description: A Semantic Search Engine
 * </p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007
 * </p>
 * <p>
 * Company: INRIA
 * </p>
 * <p>
 * Project: Acacia
 * </p>
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
    public static final String OWL_RL_PROFILE = "http://www.w3.org/ns/owl-profile/RL";
    public static final String XML = fr.inria.corese.sparql.datatype.RDF.XML;
    public static final String RDF = RDFS.RDF;
    public static final String RDFS_NS = RDFS.RDFS;
    public static final String OWL = RDFS.OWL;
    public static final String SKOS = "http://www.w3.org/2004/02/skos/core#";
    public static final String SPIN = "http://spinrdf.org/sp#";
    public static final String SQL = "http://ns.inria.fr/ast/sql#";
    public static final String FOAF = "http://xmlns.com/foaf/0.1/";
    public static final String RDFRESULT = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";
    public static final String XMLRESULT = "http://www.w3.org/2005/sparql-results#";
    public static final String EXT_FUN = "function://fr.inria.corese.core.extension.Extension.";
    public static final String EXT_FUN_REP = "function://fr.inria.corese.core.extension.Report.";
    public static final String DCTERM = "http://purl.org/dc/terms/";
    public static final String DBPEDIAFR = "http://fr.dbpedia.org/resource/";
    public static final String DBPEDIA = "http://dbpedia.org/resource/";
    public static final String RESOURCE = "http://ns.inria.fr/corese/";
    public static final String CORESE = "http://ns.inria.fr/corese/";
    public static final String RULE = CORESE + "rule/";
    public static final String HTTP = "http://ns.inria.fr/http/";
    public static final String GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String INDEX = "http://ns.inria.fr/rdf/index/";
    public static final String FEDERATE = "http://ns.inria.fr/federation/";

    public static final String SHACL = "http://www.w3.org/ns/shacl#";
    public static final String SHACL_FUNCTION_PATH = "http://www.w3.org/ns/shacl/functionpath#";
    public static final String SHACL_FUNCTION = "http://www.w3.org/ns/shacl/function#";
    public static final String SHACL_MESSAGE = "http://www.w3.org/ns/shacl/message#";
    public static final String SHACL_RESULT = "http://www.w3.org/ns/shacl/result#";
    public static final String SHACL_PREFIX = "sh";
    public static final String SHEX_SHACL = "http://ns.inria.fr/shex/shacl#";
    public static final String SHAPE = SHACL;
    public static final String SHACL_JAVA = "function://fr.inria.corese.core.extension.SHACL.";
    public static final String SHACL_SHACL = RESOURCE + "data/shaclshacl.ttl";
    // Probabilistic SHACL
    public static final String PROBSHACL = "http://ns.inria.fr/probabilistic-shacl/";

    public static final String COSNS = RDFS.COSNS;
    public static final String COS = RDFS.COS;
    public static final String SWL = ExpType.SWL;
    public static final String STL = ExpType.STL;
    public static final String D3 = STL + "d3#";
    public static final String STL_FORMAT = STL + "format/";
    // transform=stm:mapper generate map
    public static final String STL_MAPPER = STL + "navlab#";
    public static final String STL_D3 = STL + "d3#";
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
    public static final String UNDEF_URL = "http://example.org/_undefined_";
    // use case: determine if a string can be considered as an uri
    // use case: json string converted to uri or string
    static String[] protocol = { "http://", "https://", "file://", "ftp://", "urn:" };

    public static final int XSD_LENGTH = XSD.length();

    /**
     * prefix seed (ns1, ns2,...)
     */
    private static final String seed = "ns";
    private static final String DOT = ".";
    public static final String HASH = "#";
    static final String NL = System.getProperty("line.separator");
    static final char[] END_CHAR = { '#', '/', '?' }; // , ':'}; // may end an URI ...
    static final String[] PB_CHAR_NAME = { ".", "\u2013", ":", "#", "(", ")", "'", "\"", ",", ";", "[", "]", "{", "}",
            "?", "&" };
    static final String[] PB_CHAR_URI = { "(", ")", "'", "\"", ",", ";", "[", "]", "{", "}", "?", "&" };
    static final String pchar = ":";
    int count = 0;
    static final NSManager nsm;
    static final HashMap<String, Boolean> number;

    static HashMap<String, String> def; // system namespace with prefered prefix
    HashMap<String, Integer> index; // namespace -> number
    HashMap<String, String> tns; // namespace -> prefix
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
        define();
        nsm = create();
    }

    // for pretty printing without ^^datatype
    static void number() {
        number.put(fr.inria.corese.sparql.datatype.RDF.xsdinteger, Boolean.TRUE);
        // number.put(fr.inria.corese.sparql.datatype.RDF.xsdint, Boolean.TRUE);
        // number.put(fr.inria.corese.sparql.datatype.RDF.xsdfloat, Boolean.TRUE);
        // number.put(fr.inria.corese.sparql.datatype.RDF.xsddouble, Boolean.TRUE);
        number.put(fr.inria.corese.sparql.datatype.RDF.xsddecimal, Boolean.TRUE);
    }

    private NSManager() {
        tprefix = new HashMap<>();
        tns = new HashMap<>();
        index = new HashMap<>();
        trecord = new HashMap<>();
        // define();
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
        nsm.setRecord(isRecord());
        // for defining prefix of pretty printed qname
        nsm.setRecordedPrefix(getRecordedPrefix());
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

    // default predefined namespaces
    static void define() {
        def = new HashMap<>();
        def.put(RDFS.XML, RDFS.XMLPrefix);
        def.put(RDFS.RDF, RDFS.RDFPrefix);
        def.put(RDFS.RDFS, RDFS.RDFSPrefix);
        def.put(RDFS.XSD, RDFS.XSDPrefix);
        def.put(RDFS.OWL, RDFS.OWLPrefix);
        def.put(SKOS, "skos");

        def.put(SPIN, SPIN_PREF);
        def.put(FOAF, FOAF_PREF);
        // def.put("http://dbpedia.org/ontology/", "dbo");
        def.put("http://www.w3.org/2004/02/skos/core#", "skos");
        def.put(DCTERM, "dc");
        // def.put(DBPEDIAFR, "db");
        // def.put(DBPEDIA, "dbe");

        def.put(KGRAM, KPREF);
        def.put(KGEXT, KEPREF);
        def.put(KGEXTCONS, KECPREF);
        def.put(COS, RDFS.COSPrefix);
        def.put(FPPN, FPPP);
        def.put(PPN, PPP);
        def.put(STL, STL_PREF);
        def.put(STL_MAPPER, "stm");
        // def.put(STL_D3, "std");
        def.put(D3, "d3");
        def.put(SWL, SWL_PREF);
        def.put(EXT, EXT_PREF);
        def.put(USER, "us");
        def.put(ExpType.DT, "dt");
        def.put(CUSTOM, "cs");
        def.put(SPARQL, SPARQL_PREF);
        // def.put(SHACL, "xsh");
        def.put(SHACL, SHACL_PREFIX);
        def.put(SHACL_FUNCTION, "sx");
        def.put(SHACL_FUNCTION_PATH, "sxp");
        def.put(SHACL_MESSAGE, "sm");
        def.put(SHACL_RESULT, "sr");
        def.put(SHEX_SHACL, "shex");
        def.put(SHACL_JAVA, "jsh");
        def.put(EXT_FUN, "fun");
        def.put(EXT_FUN_REP, "js");
        def.put("http://example.org/ns#", "ex");
        def.put(JAVA, "java");
        def.put(DS, "ds");
        def.put(CAST, "cast");
        def.put(DOM, "dom");
        def.put(RESOURCE, "res");
        def.put(INDEX, "idx");
        def.put(GEO, "geo");
    }

    // add default namespaces
    void initDefault() {
        for (String ns : getDefaultNS().keySet()) {
            defNamespace(ns, getDefaultNS().get(ns));
        }
        defPrefix("xsh", SHACL);
    }

    public boolean isSystem(String ns) {
        return def.containsKey(ns);
    }

    public boolean isSystemURI(String uri) {
        return isSystem(namespace(uri));
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
        if (getDefaultNS().get(ns) != null) {
            return getDefaultNS().get(ns);
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
        return index.get(ns);
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
     * @param skip:    do not generate prefix when prefix undefined
     * @param display: display mode, generate prefixed named even with ' or ()
     */
    public String toPrefixURI(String nsname, boolean skip, boolean display) {
        if (!display && containsChar(nsname, PB_CHAR_URI)) {
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

    boolean containsChar(String str, String[] pb) {
        for (String s : pb) {
            if (str.contains(s)) {
                return true;
            }
        }
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
        String namespace = extractNamespace(nsname);
        if (isInvalidNamespace(namespace, nsname)) {
            return nsname;
        }

        String prefix = getPrefix(namespace);
        if (prefix == null) {
            if (skip) {
                return nsname;
            }
            prefix = defineDefaultNamespace(namespace);
        }

        String name = extractLocalName(nsname, namespace);
        if (containsForbiddenCharacters(name)) {
            return nsname;
        }

        String result = assembleResult(prefix, name, xml);
        record(namespace);
        return result;
    }

    private String extractNamespace(String nsname) {
        return namespace(nsname);
    }

    private boolean isInvalidNamespace(String namespace, String nsname) {
        return namespace == null || namespace.isEmpty() || namespace.equals(nsname);
    }

    private String defineDefaultNamespace(String namespace) {
        return defNamespace(namespace);
    }

    private String extractLocalName(String nsname, String namespace) {
        return nsname.substring(namespace.length());
    }

    private boolean containsForbiddenCharacters(String name) {
        return containsChar(name, PB_CHAR_NAME);
    }

    private String assembleResult(String prefix, String name, boolean xml) {
        if (xml && prefix.isEmpty()) {
            return name;
        }
        return prefix + pchar + name;
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
     * @param force:   generate a prefix if no prefix exist
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
        } else if (dt.isTriple()) {
            dt = DatatypeMap.newStringBuilder(dt.toString());
        }
        return dt;
    }

    public boolean isDisplayable(String ns) {
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
                str = new File(path).toURI().toASCIIString();
            } catch (Exception ex1) {
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
                ? ((refp) ? HASH + strip(name) : strip(name))
                : name;
    }

    public String strip(String name) {
        // remove namespace and #
        return nstrip(name);
    }

    public static String strip(String name, String ns) {
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

    public static String domain(String uri) {
        return domain(uri, true);
    }

    public static String domain(String uri, boolean scheme) {
        try {
            URI url = new URI(uri);
            if (url.getScheme() == null || url.getAuthority() == null) {
                return null;
            }
            if (scheme) {
                String res = url.getScheme().concat(":");
                if (url.getScheme().startsWith("http")) {
                    res = res.concat("//");
                }
                return res.concat(url.getAuthority());
            } else {
                return url.getAuthority();
            }
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public boolean sysNamespace(String name) {
        for (String ns : def.keySet()) {
            if (inNamespace(name, ns)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPredefinedTransformation(String uri) {
        return inNamespace(uri, STL);
    }

    public static boolean isPredefinedNamespace(String uri) {
        return isResource(uri);
    }

    /**
     * URL denote a resource to be found in the software archive
     */
    public static boolean isResource(String uri) {
        return inNamespace(uri, RESOURCE) || inNamespace(uri, STL);
    }

    public static boolean inNamespace(String type, String namespace) {
        // retourne si un type appartient au namespace
        if (namespace == null) {
            return true;
        } else {
            return type.startsWith(namespace);
        }
    }

    public static boolean isFile(String path) {
        try {
            URL url = new URL(path);
            return url.getProtocol().equals("file");
        } catch (MalformedURLException ex) {
            return true;
        }
    }

    public static boolean isURI(String str) {
        for (String pro : protocol) {
            if (str.startsWith(pro)) {
                return true;
            }
        }
        return false;
    }

    /**
     * path = http://ns.inria.fr/corese/rule/owl.rul
     * return directory of resource: /rule/owl.rul
     */
    public static String stripResource(String uri) {
        String ns = RESOURCE;
        if (uri.startsWith(STL)) {
            ns = STL;
        }
        return "/" + strip(uri, ns);
    }

    /**
     * Create a namespace from a URI
     * 
     * @param type URI
     * @return namespace
     */
    public static String namespace(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }

        // Return empty string if type starts with HASH
        if (type.startsWith(HASH)) {
            return "";
        }

        // Iterate through END_CHAR array to find the last occurrence of any char in it
        int lastIndex = -1;
        for (char endChar : END_CHAR) {
            int currentIndex = type.lastIndexOf(endChar);
            if (currentIndex > lastIndex) {
                lastIndex = currentIndex;
            }
        }

        // Check if a valid index is found and the substring is not a specific unwanted
        // string
        if (lastIndex != -1) {
            String namespace = type.substring(0, lastIndex + 1);
            if (!"http://".equals(namespace)) {
                return namespace;
            }
        }

        return "";
    }

    /*
     * return last occurrence of pat (e.g. '/') in str
     * if pat is last, find preceding occurrence
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
    public NSManager setRecord(boolean record) {
        this.record = record;
        return this;
    }

    static HashMap<String, String> getDefaultNS() {
        return def;
    }

    public static void defineDefaultPrefix(String p, String ns) {
        def.put(ns, p);
    }

    public HashMap<String, String> getRecordedPrefix() {
        return trecord;
    }

    public void setRecordedPrefix(HashMap<String, String> map) {
        trecord = map;
    }

}
