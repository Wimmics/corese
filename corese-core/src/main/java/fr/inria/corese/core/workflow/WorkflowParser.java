package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.transform.ContextBuilder;
import fr.inria.corese.core.transform.Transformer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.parser.Access;

/**
 * Parse a graph that describes a Workflow 
 * [] a sw:Workflow ; sw:body ( [ a
 * sw:Query ; sw:uri <q1.rq>] [ a sw:Rule ; sw:uri sw:owlrl ] [ a sw:Workflow ;
 * sw:uri <wf.ttl> ] [ a sw:Template ; sw:uri st:turtle ] )] .
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class WorkflowParser {

    private static Logger logger = LoggerFactory.getLogger(WorkflowParser.class);
    public static final String PREF = NSManager.SWL;
        
    public static final String QUERY = PREF + "Query";
    public static final String UPDATE = PREF + "Update";
    public static final String RULE = PREF + "Rule";
    public static final String RULEBASE = PREF + "RuleBase";
    public static final String TEMPLATE = PREF + "Template";
    public static final String TRANSFORMATION = PREF + "Transformation";
    public static final String LOAD = PREF + "Load";
    public static final String TEST = PREF + "Test";
    public static final String FUNCTION = PREF + "Function";
    public static final String WORKFLOW = PREF + "Workflow";
    public static final String PARALLEL = PREF + "Parallel";
    public static final String DATASET = PREF + "Dataset";
    public static final String PROBE = PREF + "Probe";
    public static final String ASSERT = PREF + "Assert";
    public static final String DATASHAPE = PREF + "Shape";
    public static final String SHEX = PREF + "Shex";
    public static final String RESULT_FORMAT = PREF + "Result";
    
    public static final String URI = PREF + "uri";
    public static final String BODY = PREF + "body";
    public static final String DEBUG = PREF + "debug";
    public static final String LOOP = PREF + "loop";
    public static final String PARAM = PREF + "param";
    public static final String STL_PARAM = Context.STL_PARAM;
    public static final String PATH = PREF + "path";
    public static final String NAME = PREF + "name";
    public static final String NAMED = PREF + "named";
    public static final String FORMAT = PREF + "format";
    public static final String REC = PREF + "rec";
    public static final String PROBE_VALUE = WorkflowProcess.PROBE;
    public static final String DISPLAY = PREF + "display";
    public static final String RESULT = PREF + "result";
    public static final String GRAPH = PREF + "graph";
    public static final String MODE = PREF + "mode";
    public static final String IF = PREF + "if";
    public static final String THEN = PREF + "then";
    public static final String ELSE = PREF + "else";
    public static final String TEST_VALUE = PREF + "test";
    public static final String WORKFLOW_VALUE = PREF + "workflow";
    public static final String VALUE = PREF + "value";
    public static final String EXP = PREF + "exp";
    public static final String COLLECT = PREF + "collect";
    public static final String COMPARE = PREF + "compare";
    public static final String MAIN = PREF + "main";
    public static final String SHAPE = PREF + "shape";
    public static final String VISITOR = PREF + "visitor";
    public static final String SPIN = PREF + "spin";
    public static final String NEW = PREF + "new";
    public static final String SILENT = PREF +"silent";
    public static final String VALIDATE = PREF +"validate";
    public static final String TEXT = PREF +"text";
    public static final String ONUPDATE = PREF +"onupdate";
    
    public static final String FORMAT_PARAM = Context.STL_FORMAT; //PREF +"format";
    public static final String LOAD_PARAM   = Context.STL_PARAM;
    public static final String MODE_PARAM   = Context.STL_MODE;
    public static final String TEXT_PARAM   = Context.STL_ARG;
    
    public static final String JSON_FORMAT = NSManager.STL + "json";
    
    static final String[] propertyList = {NAME, DEBUG, DISPLAY, RESULT, MODE, COLLECT};

    private Graph graph;
    private SemanticWorkflow sw;
    private String path;
    private boolean debug = !true;
    private SWMap map;
    private Context context;
    private PreProcessor process;
    private boolean serverMode = false;
    
    static final ArrayList<String> topLevel;
    
    static {
        topLevel = new ArrayList<String>();
        topLevel.add(BODY);
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }
    
    Context getContext(){
        return context;
    }
   
    
    class SWMap extends HashMap<String, SemanticWorkflow> {}

    public WorkflowParser() {
        sw = new SemanticWorkflow();
        map = new SWMap();
    }

    // Graph describe Workflow
    public WorkflowParser(Graph g) {
        this();
        graph = g;
    }

    public WorkflowParser(SemanticWorkflow wp, Graph g) {
        this();
        graph = g;
        this.sw = wp;
        setContext(wp.getContext());
    }
    
    public SemanticWorkflow parse(Graph g) throws LoadException, SafetyException {
        return parse(g, null);
    }
    
    public SemanticWorkflow parse(Graph g, String name) throws LoadException, SafetyException {
        setGraph(g);
        sw.setWorkflowGraph(g);
        sw.setServerMode(isServerMode());
        Node node = getWorkflowNode(name);
        if (node == null) {
            if (path != null){
                logger.error(path);
            }
           logger.error("Parser: cannot find top level Workflow " + ((name == null) ? "" : name));
        }
        else {
            parse(node);
        }
        complete(g);
        return sw;
    }
    
    public SemanticWorkflow parse(String path) throws LoadException, SafetyException {
        return parse(path, null);
    }


    public SemanticWorkflow parse(String path, String name) throws LoadException, SafetyException {
        setPath(path);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path, getFormat(ld, path));
        //g.init();
        g.getEventManager().start(Event.WorkflowParser);
        SemanticWorkflow w = parse(g);
        w.setPath(path);
        return w;
    }
    
    public SemanticWorkflow parse(InputStream stream, String path) throws LoadException, SafetyException {
        return parse(new InputStreamReader(stream), path);
    }
     
    public SemanticWorkflow parse(Reader stream, String path) throws LoadException, SafetyException {
        setPath(path);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(stream, path, path, path, getFormat(ld, path));
        //g.init();
        g.getEventManager().start(Event.WorkflowParser);
        SemanticWorkflow w = parse(g);
        w.setPath(path);
        return w;
    } 
      

    public SemanticWorkflow parse(Node wf) throws LoadException, SafetyException {
        if (isDebug()) {
            System.out.println("WP: " + wf);
        }
        if (map.containsKey(wf.getLabel())) {
            // reference to already defined Workflow
            return map.get(wf.getLabel());
        }
        map.put(wf.getLabel(), sw);
        sw.setServerMode(isServerMode());
        loop(wf);
        context(wf);
        parseNode(wf);      
        complete(sw,  wf.getValue());
        return sw;
    }
    
    public SemanticWorkflow parseWE(Node wf) throws LoadException {
        try {
            return parse(wf);
        } catch (SafetyException ex) {
            throw new LoadException((ex));
        }
    }

    void parseNode(Node wf) throws LoadException, SafetyException {
        Node body = getNode(BODY, wf);
        Node uri  = getNode(URI, wf);

        if (body != null) {
            parse(wf, body);
        } else if (uri != null) {
            WorkflowParser parser = new WorkflowParser();
            parser.inherit(this);
            parser.setProcessMap(map);
            SemanticWorkflow w = parser.parse(uri.getLabel());
            sw.add(w);
        }
    }
    
    
    
          /**
       * path may be .sw .ttl .rdf
       * load graph as .ttl .rdf (not .sw)
       */
    int getFormat(Load ld, String path){
          int format = ld.getFormat(path);
          if (format == Load.WORKFLOW_FORMAT || format == Load.UNDEF_FORMAT){
              format = Load.TURTLE_FORMAT;
          }
          return format;
      }
 
    Node getNode(String name, Node node){
        return getGraph().getNode(name, node);
    }
    
    // get value in profile graph
    IDatatype getValue(String name, IDatatype dt){
        return getGraph().getValue(name, dt);
    }
    
    
      /**
     * dt is Workflow element of a sw:body : sw:body ( [ a sw:Workflow ; sw:body
     * | sw:uri ] ) dt is the subject of the subWorkflow definition
     */
    SemanticWorkflow subWorkflow(Node wf) throws LoadException, SafetyException {
        Node body = getNode(BODY, wf);
        Node uri  = getNode(URI, wf);
        if (body != null) {
            WorkflowParser parser = new WorkflowParser(getGraph()).complete(this);
            return parser.parse(wf); 
        } else if (uri != null) {
            WorkflowParser parser = new WorkflowParser().complete(this);
            return parser.parse(uri.getLabel());
        } else {
            return null;
        }
    }
    
    /**
     * 
     * This completed by wp
     */
    WorkflowParser complete(WorkflowParser wp){
        setProcessMap(wp.getProcessMap());
        setContext(wp.getContext());
        inherit(wp);
        return this;
    }
    
     WorkflowParser inherit(WorkflowParser wp){
        setServerMode(wp.isServerMode());
        return this;
    }
    
    /**
     *
     * wf a sw:Workflow ; sw:body body
     */
    void parse(Node wf, Node body) throws LoadException, SafetyException {
        IDatatype dt = graph.list(body);
        if (dt != null) {
            parse(dt);
        }
    }

    /**
     * list is the IDatatype list of values of sw:body slot
     */
    void parse(IDatatype list) throws LoadException, SafetyException {
        for (IDatatype dt : list.getValues()) {
            addProcess(dt);
        }
    }
    
    
    
    Node getWorkflowNode(String name) {
        if (name == null){
            Node node = getGraph().getNode(MAIN);
            if (node == null){
                node = getTopLevel(topLevel);
            }
            return node;
        }
        return getGraph().getNode(name);
    }
    
    Node getTopLevel(List<String> list) {
        for (String name : list) {
            for (Edge ent : getGraph().getEdges(name)) {
                if (!getGraph().hasEdge(ent.getNode(0), 1)) {
                    return ent.getNode(0);
                }
            }
        }
        return null;
    }
        
    
    /**
     * [] sw:loop 3 ; sw:body ( ... )
     * Workflow is a loop.
     */
    void loop(Node wf) {
        IDatatype dt = getValue(LOOP,  wf.getValue());
        if (dt != null) {
            sw.setLoop(dt.intValue());
        }
    }
    
    /**
     * [] a sw:Workflow ;
     *   sw:param [ sw:test 12 ] 
     * Parse sw:param as Workflow Context.
     */
    void context(Node wf){
        Node c = getNode(PARAM, wf);
        if (c == null){
            c = getNode(STL_PARAM, wf);
            if (c != null){
                logger.error("Use " + PARAM + " instead of " + STL_PARAM);
            }
        }
        if (c != null){
            parseContext(c);
        }
    }
    
    void parseContext(Node c) {
        ContextBuilder cb = new ContextBuilder(getGraph());
        if (getContext() != null) {
            // wp workflow already has a Context
            // complete wp context with current specification
            // use case: server profile has a st:param Context
            // and workflow has a sw:param Context
            // result: merge them
            cb.setContext(getContext());
        }
        Context context = cb.process(c);
        sw.setContext(context);
        if (isDebug()) {
            System.out.println(context);
        }
    }
    
    void complete(Graph g) {
        if (sw.getContext() != null){
            complete(sw.getContext(), g);
        }
    }
     
    void complete(Context c, Graph g){
        if (c.get(Context.STL_SERVER_PROFILE) == null){
            c.set(Context.STL_SERVER_PROFILE, DatatypeMap.createObject(g));
        }
    }
    
    void addProcess(IDatatype dt) throws LoadException, SafetyException{
        WorkflowProcess ap = createProcess(dt);
        if (ap != null){
            sw.add(ap);
        }
    }

    /**
     * dt is element of workflow sw:body ( ... dt ...)
     */
     WorkflowProcess createProcess(IDatatype dt) throws LoadException, SafetyException {
        WorkflowProcess ap = null;
        IDatatype dtype = getValue(RDF.TYPE, dt);
        if (isDebug()) {
            System.out.println("WP: " + dt + " " + dtype);
        }
        if (dtype == null) {
            if (dt.isURI()) {
                // default is Query
                ap = queryPath(dt, dt.getLabel());
            }
        } else {
             String type = dtype.getLabel();
             // System.out.println("Parser: " + type);
             if (type.equals(WORKFLOW)) {
                 // special case (with complete done)
                 ap = subWorkflow(getGraph().getNode(dt));
             } 
             else {
                 switch (type) {

                     case DATASHAPE:
                         ap = datashape(dt, false);
                         break;
                     case SHEX:
                         ap = datashape(dt, true);
                         break;
                     case TRANSFORMATION:
                         ap = transformation(dt);
                         break;
                     case RESULT_FORMAT:
                         ap = result(dt);
                         break;
                     case QUERY:
                     case UPDATE:
                     case TEMPLATE:
                         ap = query(dt);
                         break;

                     default:
                         IDatatype duri = getValue(URI, dt);
                         IDatatype dbody = getValue(BODY, dt);
                         IDatatype dtest = getValue(TEST_VALUE, dt);
                         boolean test = dtest != null && dtest.booleanValue();

                         if (duri != null) {
                             String uri = duri.getLabel();
                              if (type.equals(FUNCTION)) {
                                 ap = functionPath(uri);
                             } else if (type.equals(RULE) || type.equals(RULEBASE)) {
                                 RuleProcess rb = new RuleProcess(uri);
                                 ap = rb;
                                 IDatatype update = getValue(ONUPDATE, dt);
                                 if (update!=null) {
                                     rb.setOnUpdate(update.booleanValue());
                                 }
                             } else if (type.equals(LOAD)) {
                                 ap = load(dt);
                             }
                         } else if (type.equals(PROBE)) {
                             ap = probe(dt);
                         } else if (dbody != null) {
                             if (type.equals(FUNCTION)) {
                                 ap = new FunctionProcess(dbody.getLabel(), getPath());
                             } else if (type.equals(PARALLEL)) {
                                 ap = parallel(getGraph().getNode(dt));
                             }
                         } 
                         else if (type.equals(LOAD)) {
                             ap = load(dt);
                         } else if (type.equals(TEST)) {
                             ap = test(dt);
                         } else if (type.equals(DATASET)) {
                             ap = dataset(dt);
                         } else if (type.equals(ASSERT)) {
                             ap = asserter(dt);
                         }
                 }

                 if (ap != null) {
                     complete(ap, dt);
                 }
             }
         }
        return ap;
    }

    TransformationProcess transformation(IDatatype dt) throws SafetyException {
        String uri = getParam(dt, URI, MODE_PARAM, true, true);
        return new TransformationProcess(uri);
    }
    
    ResultProcess result(IDatatype dt) {
        ResultProcess r = new ResultProcess();
        IDatatype dtformat = getValue(FORMAT, dt);
        if (dtformat != null) {
           r.setFormat(getResultFormat(dtformat.stringValue()));
        }
        return r;
    }
    
    int getResultFormat(String format) {
        switch (format) {
              case JSON_FORMAT: return ResultFormat.JSON_FORMAT;
              default: return ResultFormat.UNDEF_FORMAT;
        }
    }

     /**
      * Special case: may get input from Context     
      */
    ShapeWorkflow datashape(IDatatype dt, boolean shex) throws SafetyException {
        IDatatype dtest = getValue(TEST_VALUE, dt);
        boolean test = (dtest == null) ? false : dtest.booleanValue();
        // format parameter => rdf and shacl input as text (otherwise as URL)
        String sformat = getStringParam(FORMAT_PARAM);
        boolean isURL = (sformat == null);
        // rdf
        String rdf = getParam(dt, URI, LOAD_PARAM, isURL, isURL);
        // shacl
        String shacl = getParam(dt, SHAPE, MODE_PARAM, isURL, isURL);
        String result = getParam(dt, PATH, PATH);

        int format = (isURL) ? Load.UNDEF_FORMAT : getFormat(sformat) ;

        ShapeWorkflow ap = null;
        ap = new ShapeWorkflow().setShex(shex);
        if (shex) {
            ap.setProcessor(getProcessor());
        }
        ap.create(shacl, rdf, result, !isURL, format, test, false);
        return ap;
    }
    
    int getFormat(String format){
        if (format.equals(Transformer.TURTLE)){
            return Load.TURTLE_FORMAT;
        }
        else if (format.equals(Transformer.RDFXML)){
            return Load.RDFXML_FORMAT;
        }
        else if (format.equals(Transformer.JSON)){
            return Load.JSONLD_FORMAT;
        }
        return Load.UNDEF_FORMAT;
    }
    
    
    /**
     * Retrieve param from workflow graph pred or from context name
     */
     String getParam(IDatatype node, String pred, String name) throws SafetyException{
         return getParam(node, pred, name, false, false);
     }

    /**
     * Retrieve param from workflow graph pred or from context name
     */
    String getParam(IDatatype node, String pred, String name, boolean uri) throws SafetyException {
        return getParam(node, pred, name, uri, false);
    }
         
    String getParam(IDatatype node, String pred, String name, boolean uri, boolean check) throws SafetyException{
        // in graph
        IDatatype dt = getValue(pred, node);
        if (dt == null){
            // in Context
            String value = getStringParam(name);
            if (value != null){
                if (uri){
                    if (value.isEmpty()) {
                        return null;
                    }
                    String res = resolve(value);
                    if (res == null) {
                        return null;
                    }
                    if (check) { 
                       check(res);
                    }
                    return res;
                }
                else {
                    return value;
                }
            }
        }
        if (dt == null){
            return null;
        }
        
        return dt.getLabel();
    }
    
    String getStringParam(String name){
        IDatatype dt = getParam(name);
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }
    
    String getCheckParam(String name) throws SafetyException {
        String value = getStringParam(name);
        check(value);
        return value;
    }
    
    // TODO: fix it 
    void check(String value) throws SafetyException {
        if (value != null) {
            if (isServerMode() && NSManager.isFile(value) && Access.isActive()) {
                throw new SafetyException("Path unauthorized: " + value);
            }
        }
    }
    
    IDatatype getParam(String name){
        if (getContext() == null || getContext().get(name) == null){
            return null;
        }
        return getContext().get(name);
    }
    
    String resolve(String uri) {
        if (getContext() == null){
            return uri;
        }
        IDatatype dts = getContext().get(Context.STL_SERVER);
        if (dts != null) {
            return resolve(uri, dts.getLabel());
        }
        return uri;
    }
    
    String resolve(String uri, String base){
        try {
            URI url = new URI(uri);
            if (! url.isAbsolute()) {
                url = new URI(base).resolve(uri);
                uri= url.toString();
            }
        } catch (URISyntaxException ex) {
            //logger.info(ex);
        }
        return uri;
    }
     
    DatasetProcess dataset(IDatatype dt) {
        return new DatasetProcess();
    }
     
    SPARQLProcess query(IDatatype dt) throws LoadException {
        IDatatype duri = getValue(URI, dt);
        if (duri != null) {
            return queryPath(dt, duri.stringValue());
        } else {
            return queryBody(dt);
        }
    }

    SPARQLProcess queryPath(IDatatype dt, String path) throws LoadException {
        String q = read(path);
        SPARQLProcess qq = new SPARQLProcess(q, path);
        complete(qq, dt);
        return qq;
    }
     
    String read(String path) throws LoadException {
        return QueryLoad.create().readWE(path, isServerMode());
    }
    
    SPARQLProcess queryBody(IDatatype dt){
        IDatatype dbody = getValue(BODY, dt); 
        String text = (dbody == null) ? null : dbody.stringValue();
        SPARQLProcess qq = new SPARQLProcess(text, getPath());
        complete(qq, dt);
        return qq;
    }
    
    void complete(SPARQLProcess q, IDatatype dt) {
        q.setValue(getValue(Context.STL_PATTERN_VALUE, dt));
        q.setProcess(getValue(Context.STL_PROCESS_QUERY, dt)); 
    }
    
    FunctionProcess functionPath(String path) throws LoadException{
        String q = read(path);
        return new FunctionProcess(q, path);
    } 
          
    
    WorkflowProcess load(IDatatype dt) throws SafetyException{
        Node subject     = getGraph().getNode(dt);
        IDatatype dname  = getValue(NAME, dt);
        IDatatype dnamed = getValue(NAMED, dt);
        IDatatype drec   = getValue(REC, dt);
        IDatatype format = getValue(FORMAT, dt);
        String name = (dname == null) ? null : dname.getLabel();
        boolean rec = (drec == null) ? false : drec.booleanValue();
        boolean named = (dnamed == null) ? false : dnamed.booleanValue();
        SemanticWorkflow w = new SemanticWorkflow();
        
        for (Edge ent : getGraph().getEdges(PATH, subject, 0)){
            String pp = ent.getNode(1).getLabel();
            LoadProcess load = new LoadProcess(pp, getName(pp, name, named), rec);
            if (format != null) {
                load.setRequiredFormat(format.getLabel());
            }
            w.add(load);
        }
        for (Edge ent : getGraph().getEdges(URI, subject, 0)){
            String pp = ent.getNode(1).getLabel();
            LoadProcess load = new LoadProcess(pp, getName(pp, name, named), rec);
            if (format != null) {
                load.setRequiredFormat(format.getLabel());
            }
            w.add(load);            
        }
        
        // get what to load from Context st:param
        String uri  = getStringParam(LOAD_PARAM);
        if (uri != null) {
            String pp = resolve(uri);
            check(pp);
            LoadProcess load = new LoadProcess(pp, getName(pp, name, named), rec);
            if (format != null) {
                load.setRequiredFormat(format.getLabel());
            }
            w.add(load);
        }
        
        // get text to load from Context st:arg
        String text = getStringParam(TEXT_PARAM);
        if (text != null) {
            //check(text);
            LoadProcess load = new LoadProcess(text, Loader.UNDEF_FORMAT);           
            w.add(load);
        }
        
        if (w.getProcessList().size() == 1){
            return w.getProcessLast();
        }
        return w;
    }
    
    
    
    String getName(String path, String name, boolean named){
        if (name == null && named){
            return path;
        }
        return name;
    }
       
    
    ProbeProcess probe(IDatatype dt) throws LoadException, SafetyException {
        IDatatype body = getValue(EXP, dt);
        if (body == null){
            return new ProbeProcess();
        }
        else {
            WorkflowProcess wp = createProcess(body);        
            return new ProbeProcess(wp);
        }
    }
    
    ParallelProcess parallel(Node wf) throws LoadException, SafetyException {
        Node body = getNode(BODY, wf);
        if (body != null) {
            IDatatype dtbody = graph.list(body);
            if (dtbody != null) {
                ParallelProcess pp = new ParallelProcess();
                for (IDatatype dt : dtbody.getValues()) {
                    WorkflowProcess wp = createProcess(dt);
                    pp.insert(wp);
                }
                return pp;
            }
        }
        return null;
    }
    
    AssertProcess asserter(IDatatype dt) throws LoadException, SafetyException {
        IDatatype dtest  = getValue(EXP, dt);
        IDatatype dvalue = getValue(VALUE, dt);
        if (dvalue == null){
            dvalue = DatatypeMap.TRUE;
        }
        if (dtest != null){
            WorkflowProcess w = fun(dtest);
            return new AssertProcess(w, dvalue);
        }
        return null;
    }
         
    TestProcess test(IDatatype dt) throws LoadException, SafetyException {
        WorkflowProcess pif = createIf(dt);
        if (pif != null) {
            IDatatype dthen = getValue(THEN, dt);
            IDatatype delse = getValue(ELSE, dt);
            WorkflowProcess pthen = null;
            WorkflowProcess pelse = null;
            if (dthen != null) {
                pthen = createProcess(dthen);
            }
            if (delse != null) {
                pelse = createProcess(delse);
            }
            return new TestProcess(pif, pthen, pelse);
        }
        return null;
    }
    
    WorkflowProcess createIf(IDatatype dt) throws LoadException, SafetyException {
        IDatatype dif = getValue(IF, dt);
        if (dif == null) {
            return null;
        }
       return fun(dif); 
    }
    
    WorkflowProcess fun(IDatatype dt) throws LoadException, SafetyException{
         if (dt.isLiteral()){
            // sw:Test   sw:if "us:test()"
            // sw:Assert sw:exp "xt:size(?g)"
            return new FunctionProcess("function xt:main(){ " + dt.getLabel() + " }", getPath());
        }
        else {
            return createProcess(dt);
        }     
    }
    
     /**
     * sw:debug true
     */
    void complete(WorkflowProcess p, IDatatype dt){
        p.setURI(dt.getLabel());
        for (String name : propertyList){
            IDatatype value = getValue(name, dt); 
            if (value != null){
                p.set(name, value);
            }
        }      
    }
     
    

    /**
     * @return the wp
     */
    public SemanticWorkflow getWorkflowProcess() {
        return sw;
    }

    /**
     * @param wp the wp to set
     */
    public void setWorkflowProcess(SemanticWorkflow wp) {
        this.sw = wp;
    }

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the processMap
     */
     SWMap getProcessMap() {
        return map;
    }

    /**
     * @param processMap the processMap to set
     */
     void setProcessMap(SWMap processMap) {
        this.map = processMap;
    }

    /**
     * @return the process
     */
    public PreProcessor getProcessor() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcessor(PreProcessor process) {
        this.process = process;
    }

    /**
     * @return the serverMode
     */
    public boolean isServerMode() {
        return serverMode;
    }

    /**
     * @param serverMode the serverMode to set
     */
    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }
}
