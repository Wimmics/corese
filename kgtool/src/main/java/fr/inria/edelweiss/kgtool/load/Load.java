package fr.inria.edelweiss.kgtool.load;

import com.github.jsonldjava.core.JSONLDTripleCallback;
import com.github.jsonldjava.core.JsonLdError;
import fr.inria.edelweiss.kgtool.load.rdfa.RDFaLoader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.ARP;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.com.hp.hpl.jena.rdf.arp.RDFListener;
import fr.com.hp.hpl.jena.rdf.arp.StatementHandler;
import fr.inria.acacia.corese.exceptions.QueryLexicalException;
import fr.inria.acacia.corese.exceptions.QuerySyntaxException;
import fr.inria.acacia.corese.triple.api.Creator;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.LoadTurtle;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.jsonld.CoreseJsonTripleCallback;
import fr.inria.edelweiss.kgtool.load.jsonld.JsonldLoader;
import fr.inria.edelweiss.kgtool.load.rdfa.CoreseRDFaTripleSink;
import fr.inria.edelweiss.kgtool.load.sesame.ParserLoaderSesame;
import fr.inria.edelweiss.kgtool.load.sesame.ParserTripleHandlerSesame;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.logging.Level;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.semarglproject.rdf.ParseException;

/**
 * Translate an RDF/XML document into a Graph use ARP
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Load
        implements StatementHandler, RDFListener, org.xml.sax.ErrorHandler,
        Loader {

    private static Logger logger = Logger.getLogger(Load.class);
    private static int DEFAULT_FORMAT = RDFXML_FORMAT;
    public static final String RULE = ".rul";
    static final String BRULE = ".brul";
    static final String IRULE = ".rl";
    static final String[] RULES = {RULE, BRULE, IRULE};
    static final String QUERY = ".rq";
    static final String UPDATE = ".ru";
    static final String[] QUERIES = {QUERY, UPDATE};
    static final String TURTLE = ".ttl";
    static final String NT = ".nt";
    static final String TRIG = ".trig";
    static final String NQUADS = ".nq";
    static final String HTML = ".html";
    static final String XHTML = ".xhtml";
    static final String SVG = ".svg";
    static final String XML = ".xml";
    static final String EXT_RDF = ".rdf";
    static final String EXT_RDFS = ".rdfs";
    static final String EXT_OWL = ".owl";
    static final String[] EXT_RDFA = {HTML, XHTML, SVG, XML};
    static final String[] RDF_XML = {EXT_RDF, EXT_RDFS, EXT_OWL};
    static final String JSONLD = ".jsonld";
    static final String[] SUFFIX = {EXT_RDF, EXT_RDFS, EXT_OWL, TURTLE, NT, NQUADS, TRIG, RULE, BRULE, IRULE, QUERY, UPDATE, HTML, XHTML, SVG, XML, JSONLD};
    static final String HTTP = "http://";
    static final String FTP = "ftp://";
    static final String FILE = "file://";
    static final String[] protocols = {HTTP, FTP, FILE};
    static final String OWL = "http://www.w3.org/2002/07/owl#";
    static final String IMPORTS = OWL + "imports";
    int maxFile = Integer.MAX_VALUE;
    Graph graph;
    Log log;
    RuleEngine engine;
    QueryEngine qengine;
    Hashtable<String, String> loaded;
    LoadPlugin plugin;
    Build build;
    String source;
    boolean debug = !true,
            hasPlugin = false;
    private boolean renameBlankNode = true;
    int nb = 0;
    private int limit = Integer.MAX_VALUE;

    Load(Graph g) {
        set(g);
    }

    public Load() {
    }

    public static Load create(Graph g) {
        return new Load(g);
    }

    public static Load create() {
        return new Load();
    }

    public static void setDefaultFormat(int f) {
        DEFAULT_FORMAT = f;
    }

    @Override
    public void init(Object o) {
        set((Graph) o);
    }

    public void setLimit(int max) {
        limit = max;
        if (build != null){
            build.setLimit(limit);
        }
    }

    void set(Graph g) {
        graph = g;
        log = g.getLog();
        loaded = new Hashtable<String, String>();
        build = BuildImpl.create(graph);
    }

    public void reset() {
        //COUNT = 0;
    }

    public void exclude(String ns) {
        build.exclude(ns);
    }

    public void setEngine(RuleEngine eng) {
        engine = eng;
    }

    @Override
    public RuleEngine getRuleEngine() {
        return engine;
    }

    public void setEngine(QueryEngine eng) {
        qengine = eng;
    }

    public void setPlugin(LoadPlugin p) {
        if (p != null) {
            plugin = p;
            hasPlugin = true;
        }
    }

    public void setBuild(Build b) {
        if (b != null) {
            build = b;
        }
    }

    public void setMax(int n) {
        maxFile = n;
    }

    public Build getBuild() {
        return build;
    }

    public QueryEngine getQueryEngine() {
        return qengine;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    String uri(String name) {
        if (!isURL(name)) {
            // use case: relative file name
            // otherwise URI is not correct (for ARP)
            name = new File(name).getAbsolutePath();
            // for windows
            if (System.getProperty("os.name").contains("indows")) {
                name = name.replace('\\', '/');
                if (name.matches("[A-Z]:.*")) {
                    name = "/" + name;
                }
            }
            name = FILE + name;
        }
        return name;
    }

    boolean isURL(String path) {
        try {
            new URL(path);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }
    
    int getTypeFormat(String contentType, int format){
        if (contentType.startsWith("text/turtle")){
            return TURTLE_FORMAT;
        }
        if (contentType.startsWith("text/n3")){
            return NT_FORMAT;
        }
        if (contentType.startsWith("text/trig")){
            return TRIG_FORMAT;
        }
        if (contentType.startsWith("text/n-quads")){
            return NQUADS_FORMAT;
        }        
        if (contentType.startsWith("application/rdf+xml")){
            return RDFXML_FORMAT;
        }
        if (contentType.startsWith("application/json")){
            return JSONLD_FORMAT;
        }
        
        return format;
    }

    // UNDEF_FORMAT loaded as RDF/XML
    @Override
    public int getFormat(String path) {
        return getFormat(path, UNDEF_FORMAT);
    }

    public int getFormat(String path, int defaultFormat) {
        if (defaultFormat != UNDEF_FORMAT){
            return defaultFormat;
        }
        if (isRDFXML(path)){
            return RDFXML_FORMAT;
        }
        else if (hasExtension(path, TURTLE)){
            return TURTLE_FORMAT;
        }
        else if (hasExtension(path, NT)){
            return NT_FORMAT;
        } 
         else if (hasExtension(path, JSONLD)){
            return JSONLD_FORMAT;
        }   
        else if (isRDFa(path)){
            return RDFA_FORMAT;
        }
        else if (isRule(path)){
            return RULE_FORMAT;
        }
        else if (isQuery(path)){
            return QUERY_FORMAT;
        }
        else if (hasExtension(path, TRIG)){
            return TRIG_FORMAT;
        }
        else if (hasExtension(path, NQUADS)){
            return NQUADS_FORMAT;
        }
        return defaultFormat;
    }

    private boolean hasExtension(String path, String ext) {
        return path.toLowerCase().endsWith(ext);
    }


    //check the extension of file to see if the file conforms to RDFa format
    private boolean hasExtension(String path, String[] list) {
        String str = path.toLowerCase();
        for (String s : list) {
            if (str.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRDFa(String path) {
        return hasExtension(path, EXT_RDFA);
    }

    private boolean isRDFXML(String path) {
        return hasExtension(path, RDF_XML);
    }

    @Override
    public boolean isRule(String path) {
        return hasExtension(path, RULES);
    }

    public boolean isQuery(String path) {
        return hasExtension(path, QUERIES);

    }

    @Override
    @Deprecated
    public void load(String path) {
        load(path, null);
    }

    @Override
    public void loadWE(String path) throws LoadException {
        loadWE(path, null);
    }
    
     public void loadWE(String path, int format) throws LoadException {
        loadWE(path, null, format);
    }

    @Override
    @Deprecated 
    public void load(String path, String src) {
        File file = new File(path);
        if (file.isDirectory()) {
            path += File.separator;
            int i = 0;
            for (String f : file.list()) {
                if (!suffix(f)) {
                    continue;
                }
                if (i++ >= maxFile) {
                    return;
                }
                String name = path + f;
                load(name, src);
            }
        } else {
            if (debug) {
                logger.info("** Load: " + nb++ + " " + graph.size() + " " + path);
            }
            try {
                load(path, src, null);
            } catch (LoadException e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void loadWE(String path, String src) throws LoadException {
         loadWE(path, src, UNDEF_FORMAT);
     }

     /**
      * format is a suggested default format when path has no extension
      * default UNDEF_FORMAT is loaded as RDF/XML
      */
     public void loadWE(String path, String src, int format) throws LoadException {
        File file = new File(path);
        if (file.isDirectory()) {
            path += File.separator;
            int i = 0;
            for (String f : file.list()) {
                if (!suffix(f)) {
                    continue;
                }
                if (i++ >= maxFile) {
                    return;
                }
                String name = path + f;
                loadWE(name, src, format);
            }
        } else {
            if (debug) {
                logger.info("** Load: " + nb++ + " " + graph.size() + " " + path);
            }
            load(path, src, null, format);
        }
    }
     
     

    void log(String name) {
        if (graph != null) {
            graph.log(Log.LOAD, name);
            graph.logLoad(name);
        }
    }

    /**
     * format is a suggested format when path has no extension
     */
    public void load(String path, int format) throws LoadException {
        localLoad(path, path, path, getFormat(path, format));
    }

    public void load(String path, String base, String source) throws LoadException {
        localLoad(path, base, source, getFormat(path));
    }

    @Override
    public void load(String path, String base, String source, int format) throws LoadException {
        localLoad(path, base, source, getFormat(path, format));
    }

    private void localLoad(String path, String base, String source, int format) throws LoadException {

        log(path);

        if (format == RULE_FORMAT) {
            loadRule(path, base);
            return;
        }
        else 
        if (format == QUERY_FORMAT) {
            loadQuery(path, base);
            return;
        }

        Reader read = null;
        InputStream stream = null;

        try {
            if (isURL(path)) {
                URL url = new URL(path);
                URLConnection c = url.openConnection();
                String contentType = c.getContentType();
                stream = c.getInputStream();
                read = reader(stream); 
                if (format == UNDEF_FORMAT && contentType != null){
                    format = getTypeFormat(contentType, format);
                }
            } else {
                read = new FileReader(path);
            }
        } catch (Exception e) {
            throw LoadException.create(e, path);
        }

        if (base != null) {
            // ARP needs an URI for base
            base = uri(base);
        } else {
            base = uri(path);
        }

        if (source == null) {
            source = base;
        }

        synLoad(read, path, base, source, format);

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    Reader reader(InputStream stream) throws UnsupportedEncodingException {
        return new InputStreamReader(stream);
    }

    void error(Object mes) {
        logger.error(mes);
    }

 
    public void load(InputStream stream) throws LoadException {
        load(stream, UNDEF_FORMAT);
    }
    
    public void loadString(String str, int format) throws LoadException {
        loadString(str, Entailment.DEFAULT, format);
    }

    public void loadString(String str, String src, int format) throws LoadException{
         loadString(str, src, src, src, format);
    }
    
     public void loadString(String str, String path, String src, String base, int format) throws LoadException{
        try {
            load(new ByteArrayInputStream(str.getBytes("UTF-8")), path, src, base, format);
        } catch (UnsupportedEncodingException ex) {
            throw new LoadException(ex);
        }
    }
     
    public void loadResource(String name, int format) throws  LoadException {
        loadResource(name, Entailment.DEFAULT, format);
    }
     
     public void loadResource(String name, String src, int format) throws  LoadException {
        InputStream stream = Load.class.getResourceAsStream(name);
        if (stream == null) {
            throw new LoadException(new IOException(name));
        }
        load(stream, src, format);
    }
    
    public void load(InputStream stream, int format) throws LoadException {
        load(stream, Entailment.DEFAULT, format);
    }

    public void load(InputStream stream, String source, int format) throws LoadException {
        load(stream, source, source, source, format);
    }

    public void load(InputStream stream, String path, String source, String base, int format) throws LoadException {
        log("stream");

        try {
            Reader read = reader(stream);
            synLoad(read, path, base, source,  format);
//            synLoad(read, path, source, base, format);
        } catch (UnsupportedEncodingException e) {
            throw new LoadException(e);
        }
    }



       
    void synLoad(Reader stream, String path, String base, String src, int format) throws LoadException {
        try {
            writeLock().lock();

            switch (format) {
                case TURTLE_FORMAT:
                case NT_FORMAT:
                    loadTurtle(stream, path, base, src);
                    break;
                    
                case NQUADS_FORMAT:
                    loadWithSesame(stream, path, base, src, RDFFormat.NQUADS);
                    break;
                case TRIG_FORMAT:
                    loadWithSesame(stream, path, base, src, RDFFormat.TRIG);
                    break;
                    
                case RULE_FORMAT:
                    loadRule(stream, src);
                    break;
                    
                case QUERY_FORMAT:
                    loadQuery(stream, src);
                    break;

                case RDFA_FORMAT:
                    loadRDFa(stream, path, base, src);
                    break;

                case JSONLD_FORMAT:
                    loadJsonld(stream, path, base, src);
                    break;

                case RDFXML_FORMAT:
                    loadRDFXML(stream, path, base, src);
                    break;
                    
                case UNDEF_FORMAT:
                default:
                    loadRDFXML(stream, path, base, src);
            }
        } finally {
            writeLock().unlock();
        }
        }

    Lock writeLock() {
        return graph.writeLock();
    }

    void loadRDFXML(Reader stream, String path, String base, String src) throws LoadException {

        if (hasPlugin) {
            src = plugin.statSource(src);
            base = plugin.statBase(base);
        }
        String save = source;
        source = src;
        build.setSource(src);
        build.start();
        ARP arp = new ARP();
        try {
            arp.setRDFListener(this);
        } catch (java.lang.NoSuchMethodError e) {
        }
        arp.setStatementHandler(this);
        arp.setErrorHandler(this);
        try {
            arp.load(stream, base);
            stream.close();
        } catch (SAXException e) {
            throw LoadException.create(e, arp.getLocator(), path);
        } catch (IOException e) {
            throw LoadException.create(e, arp.getLocator(), path);
        }
        finally {
            build.finish();
            source = save;
        }
    }

    void loadTurtle(Reader stream, String path, String base, String src) throws LoadException {

        Creator cr = CreateImpl.create(graph);
        cr.graph(Constant.create(src));
        cr.setRenameBlankNode(renameBlankNode);
        cr.setLimit(limit);
        cr.start();
        LoadTurtle ld = LoadTurtle.create(stream, cr, base);
        try {
            ld.load();
        } catch (QueryLexicalException e) {
            throw LoadException.create(e, path);
        } catch (QuerySyntaxException e) {
            throw LoadException.create(e, path);
        }
        finally {
            cr.finish();
        }
    }

    // load RDFa
    void loadRDFa(Reader stream, String path, String base, String src) throws LoadException {
        CoreseRDFaTripleSink sink = new CoreseRDFaTripleSink(graph, null);
        sink.setHelper(renameBlankNode, limit);

        RDFaLoader loader = RDFaLoader.create(stream, base);

        try {
            loader.load(sink);
        } catch (ParseException ex) {
            throw LoadException.create(ex, path);
        }

    }

    // load JSON-LD
    void loadJsonld(Reader stream, String path, String base, String src) throws LoadException {

        JSONLDTripleCallback callback = new CoreseJsonTripleCallback(graph, null);
        ((CoreseJsonTripleCallback) callback).setHelper(renameBlankNode, limit);

        JsonldLoader loader = JsonldLoader.create(stream, base);
        try {
            loader.load(callback);
        } catch (IOException ex) {
            throw LoadException.create(ex, path);
        } catch (JsonLdError ex) {
            throw LoadException.create(ex, path);
        }
    }

        //load turtle with parser sesame(openRDF)
        //can surpot format:.ttl, .nt, .nq and .trig
        //now only used for .trig and .nq
    void loadWithSesame(Reader stream, String path, String base, String src, RDFFormat format) throws LoadException {
        ParserTripleHandlerSesame handler = new ParserTripleHandlerSesame(graph, src);
        handler.setHelper(renameBlankNode, limit);
        ParserLoaderSesame loader = ParserLoaderSesame.create(stream, base);

        try {
            loader.loadWithSesame(handler, format);
        } catch (IOException ex) {
            throw LoadException.create(ex, path);
        } catch (RDFParseException ex) {
            throw LoadException.create(ex, path);
        } catch (RDFHandlerException ex) {
            throw LoadException.create(ex, path);
        }
    }
    void loadRule(String path, String src) throws LoadException {
        if (engine == null) {
            engine = RuleEngine.create(graph);
        }

        if (path.endsWith(IRULE)) {
            // individual rule
            QueryLoad ql = QueryLoad.create();
            String rule = ql.readWE(path);
            if (rule != null) {
                engine.addRule(rule);
            }
        } else {
            // rule base
            RuleLoad load = RuleLoad.create(engine);
            load.loadWE(path);
        }
    }

    public void loadRule(Reader stream, String src) throws LoadException {
        if (engine == null) {
            engine = RuleEngine.create(graph);
        }
        RuleLoad load = RuleLoad.create(engine);
        load.loadWE(stream);
    }

    void loadQuery(String path, String src) throws LoadException {
        if (qengine == null) {
            qengine = QueryEngine.create(graph);
        }
        QueryLoad load = QueryLoad.create(qengine);
        load.loadWE(path);
    }
    
    void loadQuery(Reader read, String src) throws LoadException  {
        if (qengine == null) {
            qengine = QueryEngine.create(graph);
        }
        QueryLoad load = QueryLoad.create(qengine);
        load.load(read);
    }
    

    boolean suffix(String path) {
        //if (isURL(path)) return true;

        for (String suf : SUFFIX) {
            if (path.endsWith(suf)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void statement(AResource subj, AResource pred, ALiteral lit) {
        build.statement(subj, pred, lit);
    }

    @Override
    public void statement(AResource subj, AResource pred, AResource obj) {
        build.statement(subj, pred, obj);
        if (pred.getURI().equals(IMPORTS)) {
            imports(obj.getURI());
        }
    }

    void imports(String uri) {
        if (! loaded.contains(uri)) {
            loaded.put(uri, uri);
            if (debug) {
                logger.info("Import: " + uri);
            }
            
            Build save = build;
            build = BuildImpl.create(graph);
            build.setLimit(save.getLimit());
            try {
                loadWE(uri);
            } catch (LoadException ex) {
                logger.error(ex);
            }
            build = save;
        }
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        // TODO Auto-generated method stub
    }

    /**
     * Process cos:graph statement TODO: generate different blanks in different
     * graphs
     */
    @Override
    public void setSource(String s) {
        if (s == null) {
            //cur = src;
            build.setSource(source);
            return;
        }

        if (hasPlugin) {
            s = plugin.dynSource(s);
        }
        build.setSource(s);
//		if (! cur.getLabel().equals(s)){
//			cur = build.getGraph(s);
//		}
    }

    @Override
    public String getSource() {
        return source;
    }

    public boolean isRenameBlankNode() {
        return renameBlankNode;
    }

    public void setRenameBlankNode(boolean renameBlankNode) {
        this.renameBlankNode = renameBlankNode;
    }

    
    
 
    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public void load(InputStream stream, String source) throws LoadException {
        load(stream, source, source);
    }

    /**
     * source is the graph name path is a pseudo path that may have an extension
     * and hence specify the input format
     * @deprecated
     */
    public void load(InputStream stream, String source, String path) throws LoadException {
        if (source == null) {
            source = Entailment.DEFAULT;
        }
        if (path == null) {
            path = Entailment.DEFAULT;
        }
        // ici source était aussi la base ... (au lieu de path)
        load(stream, path, source, source, getFormat(path));
    }

   
    
}
