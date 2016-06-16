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
import java.util.HashMap;
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
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.corese.kgtool.workflow.WorkflowParser;
import fr.inria.corese.kgtool.workflow.SemanticWorkflow;
import fr.inria.edelweiss.kgram.api.core.Node;
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
import java.io.FileFilter;
import java.net.URLConnection;
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
    static final String HTTP = "http://";
    static final String FTP = "ftp://";
    static final String FILE = "file://";
    static final String[] protocols = {HTTP, FTP, FILE};
    static final String OWL = NSManager.OWL; 
    static final String IMPORTS = OWL + "imports";
    // true:  load files into kg:default graph when no named graph is given
    // false: load files into named graphs (name = URI of file) 
    private static boolean DEFAULT_GRAPH = false;

    
    int maxFile = Integer.MAX_VALUE;
    Graph graph;
    Log log;
    RuleEngine engine;
    QueryEngine qengine;
    private SemanticWorkflow workflow;
    HashMap<String, String> loaded;
    LoadPlugin plugin;
    Build build;
    String source;
    boolean debug = !true,
            hasPlugin = false;
    private boolean renameBlankNode = true;
    private boolean defaultGraph = DEFAULT_GRAPH;
    int nb = 0;
    private int limit = Integer.MAX_VALUE;
    
    /**
     * true means load in default graph when no named graph is given
     */
    public static void setDefaultGraphValue(boolean b) {
        DEFAULT_GRAPH = b;
    }

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
        if (build != null) {
            build.setLimit(limit);
        }
    }

    void set(Graph g) {
        graph = g;
        log = g.getLog();
        loaded = new HashMap<String, String>();
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

   int getTypeFormat(String contentType, int format) {
       return LoadFormat.getTypeFormat(contentType, format);
   }

    // UNDEF_FORMAT loaded as RDF/XML
    @Override
    public int getFormat(String path) {
        return getFormat(path, UNDEF_FORMAT);
    }
    
    boolean hasFormat(String path){
        return getFormat(path) != UNDEF_FORMAT;
    }

    public int getFormat(String path, int proposedFormat) {
        if (proposedFormat != UNDEF_FORMAT) {
            return proposedFormat;
        }
        return LoadFormat.getFormat(path);
    }
    
    public int getFormatDefault(String path, int defaultFormat) {
        int f = LoadFormat.getFormat(path);
        if (f != UNDEF_FORMAT) {
            return f;
        }
        return defaultFormat;
    }
       
    @Override
    public boolean isRule(String path) {
        return getFormat(path) == RULE_FORMAT;
    }

    /**
     * parse directory content
     */
    public void parseDir(String path) throws LoadException {
        parseDir(path, null, false);
    }
           
    /**
     * Parse directory (not subdirectory)
     * name is named graph (if not null) else path is named graph
     * base is now the path (it used to be the name)
     */
    public void parseDir(String path, String name) throws LoadException {
        parseDir(path, name, false);
    }
        
    public void parseDirRec(String path) throws LoadException {
        parseDir(path, null, true);
    } 
        
    public void parseDirRec(String path, String name) throws LoadException {
        parseDir(path, name,  true);
    } 
       
    public void parseDir(String path, String name, boolean rec) throws LoadException {
        parseDir(new File(path), path, name, rec);
    }
    
     /**
     * name is the named graph where to create triples 
     * if name = null name := path of each file
     * Difference with loadWE:
     * recursion on subdirectory when rec = true
     * no recursion on directory with SW extension (even if rec = true)
     * base is now the path of each file (not the name)
     */
    void parseDir(File file, String path, String name,  boolean rec) throws LoadException {
        if (file.isDirectory()) {
            path += File.separator;           
            for (String f : file.list()) {
                String pname = path + f;
                if (hasFormat(f)) {                                      
                    parseDoc(pname, name);
                }
                else if (rec) {
                    File dir = new File(pname);
                    if (dir.isDirectory()){
                        parseDir(dir, pname, name,  rec);
                    }
                }
            }
        } 
        else {           
            parseDoc(path, name);
        }
    }
    
    
    /**
     * Load files according to filter extensions (use ExtensionFilter)
     */
    public void parse(File file, FileFilter ff, String name, boolean rec) throws LoadException {
        if (file.isDirectory()) {
            for (File f : file.listFiles(ff)) {                
               if (! f.isDirectory()){
                   parseDoc(f.getAbsolutePath(), name);
               }       
            }
            if (rec){
                for (File dir : file.listFiles()){
                    if (dir.isDirectory()){
                        parse(dir, ff, name,  rec);
                    }
                }
            }              
        } 
        else if (ff.accept(file)){
            parseDoc(file.getAbsolutePath(), name);
        }       
    }
    
    boolean match(String path, int format){
        if (format == UNDEF_FORMAT){
            return true;
        }
        return getFormat(path) == format;
    }
    
    void parseDoc(String path, String name) throws LoadException {       
        if (debug) {
                logger.info("** Load: " + nb++ + " " + graph.size() + " " + path);
         }
         parse(path, name, path, UNDEF_FORMAT);
    }

    /**
     * format is a suggested format when path has no extension
     * default format is RDF/XML
     */
    public void parse(String path) throws LoadException {
        parse(path, null, null, UNDEF_FORMAT);
    }
    
    public void parse(String path, int format) throws LoadException {
        parse(path, null, null, format);
    }
    
    public void parse(String path, String name) throws LoadException {
        parse(path, name, null, UNDEF_FORMAT);
    }
    
    public void parse(String path, String name, int format) throws LoadException {
        parse(path, name, null, UNDEF_FORMAT);
    }

    /**
     * name: the named graph (if null, name = path)
     * base: base for relative URI (if null, base = path)
     * getFormat: 
     * if format  = UNDEF use path extension if any
     * if format != UNDEF use format (even if it contradicts the extension)
     * use case: rdf/xml file has .xml extension but we want to load it as RDFXML_FORMAT
     * if format is UNDEF and path is URI with content type: use content type format
     */
    @Override
    public void parse(String path, String name, String base, int format) throws LoadException {
        name = target(name, path);
        base   = (base == null)   ? path : base;
        name = uri(name);
        base   = uri(base);
        localLoad(path, base, name, getFormat(path, format));
    }
    
    /**
     * 
     */
    String target(String name, String path){
        if (name == null){
            if (isDefaultGraph()){
                return defaultGraph();
            }
            else {
                return path;
            }
        }
        return name;
    }
    
    public String defaultGraph(){
        Node node = graph.addDefaultGraphNode();
        return node.getLabel();
    }
    
    public void parse(InputStream stream) throws LoadException {
        parse(stream, UNDEF_FORMAT);
    }

    public void parse(InputStream stream, int format) throws LoadException {
        parse(stream, defaultGraph(), format);
    }

    public void parse(InputStream stream, String name, int format) throws LoadException {
        parse(stream, name, name, name, format);
    }

    // TODO: clean arg order
    public void parse(InputStream stream, String path, String name, String base, int format) throws LoadException {
        log("stream");

        try {
            Reader read = reader(stream);
            synLoad(read, path, base, name, format);
        } catch (UnsupportedEncodingException e) {
            throw new LoadException(e);
        }
    }

    // if base = null   : base = uri(path)
    // if name = null : name = base
    // base used for rdf/xml & turtle
    private void localLoad(String path, String base, String name, int format) throws LoadException {

        log(path);

        if (format == RULE_FORMAT) {
            loadRule(path, base);
            return;
        } else if (format == QUERY_FORMAT) {
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
                if (format == UNDEF_FORMAT && contentType != null) {
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

        if (name == null) {
            name = base;
        }
        
        synLoad(read, path, base, name, format);

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void log(String name) {
        if (graph != null) {
            graph.log(Log.LOAD, name);
            graph.logLoad(name);
        }
    }

    Reader reader(InputStream stream) throws UnsupportedEncodingException {
        return new InputStreamReader(stream);
    }

    void error(Object mes) {
        logger.error(mes);
    }

    public void loadString(String str, int format) throws LoadException {
        loadString(str, defaultGraph(), format);
    }

    public void loadString(String str, String name, int format) throws LoadException {
        loadString(str, name, name, name, format);
    }

    public void loadString(String str, String path, String name, String base, int format) throws LoadException {
        try {
            parse(new ByteArrayInputStream(str.getBytes("UTF-8")), path, name, base, format);
        } catch (UnsupportedEncodingException ex) {
            throw new LoadException(ex);
        }
    }

    public void loadResource(String path, int format) throws LoadException {
        loadResource(path, defaultGraph(), format);
    }

    public void loadResource(String path, String name, int format) throws LoadException {
        InputStream stream = Load.class.getResourceAsStream(path);
        if (stream == null) {
            throw new LoadException(new IOException(path));
        }
        parse(stream, name, format);
    }

    void synLoad(Reader stream, String path, String base, String name, int format) throws LoadException {
        try {
            writeLock().lock();
            parse(stream, path, base, name, format);            
        } finally {
            writeLock().unlock();
        }
    }
    
    
    public void parse(Reader stream, String path, String base, String name, int format)  throws LoadException {
        switch (format) {
                case TURTLE_FORMAT:
                case NT_FORMAT:
                    loadTurtle(stream, path, base, name);
                    break;

                case NQUADS_FORMAT:
                    loadWithSesame(stream, path, base, name, RDFFormat.NQUADS);
                    break;
                    
                case TRIG_FORMAT:
                    loadWithSesame(stream, path, base, name, RDFFormat.TRIG);
                    break;

                case RULE_FORMAT:
                    loadRule(stream, name);
                    break;
                    
                case WORKFLOW_FORMAT:
                    loadWorkflow(stream, path);
                    break;
                    
                case QUERY_FORMAT:
                    loadQuery(stream, name);
                    break;

                case RDFA_FORMAT:
                    loadRDFa(stream, path, base, name);
                    break;

                case JSONLD_FORMAT:
                    loadJsonld(stream, path, base, name);
                    break;

                case RDFXML_FORMAT:
                    loadRDFXML(stream, path, base, name);
                    break;

                case UNDEF_FORMAT:
                default:
                    parse(stream, path, base, name, (DEFAULT_FORMAT == UNDEF_FORMAT) ? RDFXML_FORMAT : DEFAULT_FORMAT);
            }
    }

    Lock writeLock() {
        return graph.writeLock();
    }
    
    void loadWorkflow(Reader read, String path) throws LoadException{
        WorkflowParser wp = new WorkflowParser();
        wp.parse(read, path);
        setWorkflow(wp.getWorkflowProcess());
    }

    void loadRDFXML(Reader stream, String path, String base, String name) throws LoadException {

        if (hasPlugin) {
            name = plugin.statSource(name);
            base = plugin.statBase(base);
        }
        String save = source;
        source = name;
        build.setSource(name);
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
        } finally {
            build.finish();
            source = save;
        }
    }

    void loadTurtle(Reader stream, String path, String base, String name) throws LoadException {

        Creator cr = CreateImpl.create(graph, this);
        cr.graph(Constant.create(name));
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
        } finally {
            cr.finish();
        }
    }

    // load RDFa
    void loadRDFa(Reader stream, String path, String base, String name) throws LoadException {
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
    void loadJsonld(Reader stream, String path, String base, String name) throws LoadException {

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
    void loadWithSesame(Reader stream, String path, String base, String name, RDFFormat format) throws LoadException {
        ParserTripleHandlerSesame handler = new ParserTripleHandlerSesame(graph, name);
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

    void loadRule(String path, String name) throws LoadException {
        if (engine == null) {
            engine = RuleEngine.create(graph);
        }

        if (path.endsWith(LoadFormat.IRULE)) {
            // individual rule
            QueryLoad ql = QueryLoad.create();
            String rule = ql.readWE(path);
            if (rule != null) {
                engine.addRule(rule);
            }
        } else {
            // rule base
            RuleLoad load = RuleLoad.create(engine);
            load.parse(path);
        }
    }

    public void loadRule(Reader stream, String name) throws LoadException {
        if (engine == null) {
            engine = RuleEngine.create(graph);
        }
        RuleLoad load = RuleLoad.create(engine);
        load.parse(stream);
    }

    void loadQuery(String path, String name) throws LoadException {
        if (qengine == null) {
            qengine = QueryEngine.create(graph);
        }
        QueryLoad load = QueryLoad.create(qengine);
        load.parse(path);
    }

    void loadQuery(Reader read, String name) throws LoadException {
        if (qengine == null) {
            qengine = QueryEngine.create(graph);
        }
        QueryLoad load = QueryLoad.create(qengine);
        load.parse(read);
    }

//    boolean suffix(String path) {
//        for (String suf : SUFFIX) {
//            if (path.endsWith(suf)) {
//                return true;
//            }
//        }
//        return false;
//    }

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

    void imports(String uri)  {
        if (!loaded.containsKey(uri)) {
            loaded.put(uri, uri);
            if (debug) {
                logger.info("Import: " + uri);
            }

            Build save = build;
            build = BuildImpl.create(graph);
            build.setLimit(save.getLimit());
            try {
                parse(uri);
            } catch (LoadException ex) {
                logger.error(ex);
            }
            build = save;
        }
    }
    
    void parseImport(String uri) throws LoadException {
        if (!loaded.containsKey(uri)) {
            loaded.put(uri, uri);
            if (debug) {
                logger.info("Import: " + uri);
            }
            parse(uri);
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
     * *******************************************************
     */
    @Deprecated
    @Override
    public void load(InputStream stream, String source) throws LoadException {
        load(stream, source, source);
    }

    /**
     * source is the graph name path is a pseudo path that may have an extension
     * and hence specify the input format
     *
     * @deprecated
     */
    public void load(InputStream stream, String source, String path) throws LoadException {
        if (source == null) {
            source = defaultGraph();
        }
        if (path == null) {
            path = defaultGraph();
        }
        // ici source était aussi la base ... (au lieu de path)
        load(stream, path, source, source, getFormat(path));
    }

    @Override
    @Deprecated
    public void load(String path) {
        load(path, null);
    }

    @Override
    @Deprecated
    public void loadWE(String path) throws LoadException {
        loadWE(path, null);
    }

    @Deprecated
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
                if (! hasFormat(f)){ //(!suffix(f)) {
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
    @Deprecated
    public void loadWE(String path, String src) throws LoadException {
        loadWE(path, src, UNDEF_FORMAT);
    }

    @Deprecated        
     public void loadWE(String path, String source, int format) throws LoadException {
        File file = new File(path);
        if (file.isDirectory()) {
            path += File.separator;
            int i = 0;
            for (String f : file.list()) {
                if (! hasFormat(f)){ //(!suffix(f)) {
                    continue;
                }
                if (i++ >= maxFile) {
                    return;
                }
                String name = path + f;
                loadWE(name, source, format);
            }
        } else {
            if (debug) {
                logger.info("** Load: " + nb++ + " " + graph.size() + " " + path);
            }
            load(path, source, null, format);
        }
    }
    

    @Deprecated
    public void load(String path, int format) throws LoadException {
        localLoad(path, path, path, getFormat(path, format));
    }

    @Deprecated
    public void load(String path, String base, String source) throws LoadException {
        localLoad(path, base, source, getFormat(path));
    }

    @Deprecated
    @Override
    public void load(String path, String base, String source, int format) throws LoadException {
        localLoad(path, base, source, getFormat(path, format));
    }

    @Deprecated
    public void load(InputStream stream) throws LoadException {
        load(stream, UNDEF_FORMAT);
    }

    @Deprecated
    public void load(InputStream stream, int format) throws LoadException {
        load(stream, defaultGraph(), format);
    }

    @Deprecated
    public void load(InputStream stream, String source, int format) throws LoadException {
        load(stream, source, source, source, format);
    }

    @Deprecated
    public void load(InputStream stream, String path, String source, String base, int format) throws LoadException {
        parse(stream, path, source, base, format);
    }

    /**
     * @return the workflow
     */
    public SemanticWorkflow getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(SemanticWorkflow workflow) {
        this.workflow = workflow;
    }

    /**
     * @return the defaultGraph
     */
    public boolean isDefaultGraph() {
        return defaultGraph;
    }

    /**
     * @param defaultGraph the defaultGraph to set
     */
    public void setDefaultGraph(boolean defaultGraph) {
        this.defaultGraph = defaultGraph;
    }
}
