package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.transform.ContextBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

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

    private static Logger logger = Logger.getLogger(WorkflowParser.class);
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
    
    public static final String URI = PREF + "uri";
    public static final String BODY = PREF + "body";
    public static final String DEBUG = PREF + "debug";
    public static final String LOOP = PREF + "loop";
    public static final String PARAM = PREF + "param";
    public static final String PATH = PREF + "path";
    public static final String NAME = PREF + "name";
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
    public static final String VALUE = PREF + "value";
    public static final String EXP = PREF + "exp";
    public static final String COLLECT = PREF + "collect";
    
    static final String[] propertyList = {NAME, DEBUG, DISPLAY, RESULT, MODE, COLLECT};

    private Graph graph;
    private SemanticWorkflow sw;
    private String path;
    private boolean debug = !true;
    private SWMap map;
    
    static final ArrayList<String> topLevel;
    
    static {
        topLevel = new ArrayList<String>();
        topLevel.add(BODY);
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
    }
    
    public SemanticWorkflow parse(Graph g) throws LoadException {
        return parse(g, null);
    }
    
    public SemanticWorkflow parse(Graph g, String name) throws LoadException {
        setGraph(g);
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
    
    public SemanticWorkflow parse(String path) throws LoadException {
        return parse(path, null);
    }


    public SemanticWorkflow parse(String path, String name) throws LoadException {
        setPath(path);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path, getFormat(ld, path));
        g.init();
        return parse(g, name);
    }
    
    public SemanticWorkflow parse(InputStream stream, String path) throws LoadException {
        return parse(new InputStreamReader(stream), path);
    }
     
    public SemanticWorkflow parse(Reader stream, String path) throws LoadException {
        setPath(path);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(stream, path, path, path, getFormat(ld, path));
        g.init();
        return parse(g);
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
    
    IDatatype getValue(String name, IDatatype dt){
        return getGraph().getValue(name, dt);
    }
    
    public SemanticWorkflow parse(Node wf) throws LoadException {
        if (isDebug()) {
            System.out.println("WP: " + wf);
        }
        if (map.containsKey(wf.getLabel())) {
            // reference to already defined Workflow
            return map.get(wf.getLabel());
        }
        map.put(wf.getLabel(), sw);
        loop(wf);
        context(wf);
        
        Node body = getNode(BODY, wf);
        Node uri  = getNode(URI, wf);
        
        if (body != null) {
            parse(wf, body);
        }
        else if (uri != null) {
            WorkflowParser parser = new WorkflowParser();
            parser.setProcessMap(map);
            SemanticWorkflow w = parser.parse(uri.getLabel());
            sw.add(w);
        }
        
        complete(sw, (IDatatype) wf.getValue());
        return sw;
    }
    
    /**
     *
     * wf a sw:Workflow ; sw:body body
     */
    void parse(Node wf, Node body) throws LoadException {
        IDatatype dt = graph.list(body);
        if (dt != null) {
            parse(dt);
        }
    }

    /**
     * list is the IDatatype list of values of sw:body slot
     */
    void parse(IDatatype list) throws LoadException {
        for (IDatatype dt : list.getValues()) {
            addProcess(dt);
        }
    }
    
    
    
    Node getWorkflowNode(String name) {
        if (name == null){
            return getTopLevel(topLevel);
        }
        return getGraph().getNode(name);
    }
    
    Node getTopLevel(List<String> list) {
        for (String name : list) {
            for (Entity ent : getGraph().getEdges(name)) {
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
        IDatatype dt = getValue(LOOP, (IDatatype) wf.getValue());
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
        if (c != null){
            ContextBuilder cb = new ContextBuilder(getGraph());           
            if (sw.getContext() != null){
                // wp workflow already has a Context
                // complete wp context with current specification
                // use case: server profile has a st:param Context
                // and workflow has a sw:param Context
                // result: merge them
                cb.setContext(sw.getContext());
            }
            Context context = cb.process(c);
            sw.setContext(context);           
            if (isDebug()){
                System.out.println(context);
            }
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
    
    void addProcess(IDatatype dt) throws LoadException{
        WorkflowProcess ap = createProcess(dt);
        if (ap != null){
            sw.add(ap);
        }
    }

    /**
     * dt is element of workflow sw:body ( ... dt ...)
     */
     WorkflowProcess createProcess(IDatatype dt) throws LoadException {
        WorkflowProcess ap = null;
        IDatatype dtype = getValue(RDF.TYPE, dt);
        if (isDebug()) {
            System.out.println("WP: " + dt + " " + dtype);
        }
        if (dtype == null){
            if (dt.isURI()){
                // default is Query
                ap = queryPath(dt.getLabel());
            }
        }
        else {
            String type = dtype.getLabel();
            if (type.equals(WORKFLOW)) {
                // special case (with complete done)
                ap = subWorkflow(getGraph().getNode(dt));
            } 
            else {
                IDatatype duri  = getValue(URI, dt);
                IDatatype dbody = getValue(BODY, dt);
                if (duri != null) {
                    String uri = duri.getLabel();
                    if (type.equals(QUERY) || type.equals(UPDATE)) {
                        ap = queryPath(uri);
                    } else if (type.equals(RULE) || type.equals(RULEBASE)) {
                        ap = new RuleProcess(uri);
                    } else if (type.equals(TEMPLATE) || type.equals(TRANSFORMATION)) {
                        ap = new TemplateProcess(uri);
                    } else if (type.equals(LOAD)) {
                        ap = load(dt);
                    }
                }  
                else if (dbody != null) {
                    if (type.equals(QUERY) || type.equals(UPDATE)) {
                        ap = new SPARQLProcess(dbody.getLabel(), getPath());
                    }
                    else if (type.equals(FUNCTION)){
                        ap = new FunctionProcess(dbody.getLabel(), getPath());
                    }
                    else if (type.equals(PARALLEL)){
                        ap = parallel(getGraph().getNode(dt));
                    }                   
                }
                else if (type.equals(PROBE)){
                     ap = probe(dt);
                 }
                else if (type.equals(LOAD)) {
                    ap = load(dt);
                }
                else if (type.equals(TEST)) {
                    ap = test(dt);
                }
                else if (type.equals(DATASET)){
                    ap = dataset(dt);
                }
                else if (type.equals(ASSERT)){
                    ap = asserter(dt);
                }
                
                if (ap != null) {
                    complete(ap, dt);
                }
            }
         }
        return ap;
    }
     
     DatasetProcess dataset(IDatatype dt){
         return new DatasetProcess();
     }
     
    SPARQLProcess queryPath(String path) throws LoadException{
        String q = QueryLoad.create().readWE(path);
        return new SPARQLProcess(q, path);
    } 
          
    
    WorkflowProcess load(IDatatype dt){
        Node subject = getGraph().getNode(dt);
        IDatatype dname = getValue(NAME, dt);
        IDatatype drec  = getValue(REC, dt);
        String name = (dname == null) ? null : dname.getLabel();
        boolean rec = (drec == null) ? false : drec.booleanValue();
        SemanticWorkflow w = new SemanticWorkflow();
        
        for (Entity ent : getGraph().getEdges(PATH, subject, 0)){
            String pp = ent.getNode(1).getLabel();
            w.add(new LoadProcess(pp, name, rec));
        }
        for (Entity ent : getGraph().getEdges(URI, subject, 0)){
            String pp = ent.getNode(1).getLabel();
            w.add(new LoadProcess(pp, name, rec));
        }
        if (w.getProcessList().size() == 1){
            return w.getProcessLast();
        }
        return w;
    }
       

    /**
     * dt is Workflow element of a sw:body : sw:body ( [ a sw:Workflow ; sw:body
     * | sw:uri ] ) dt is the subject of the subWorkflow definition
     */
    SemanticWorkflow subWorkflow(Node wf) throws LoadException {
        Node body = getNode(BODY, wf);
        Node uri  = getNode(URI, wf);
        if (body != null) {
            WorkflowParser parser = new WorkflowParser(getGraph());
            parser.setProcessMap(map);
            return parser.parse(wf); 
        } else if (uri != null) {
            WorkflowParser parser = new WorkflowParser();
            parser.setProcessMap(map);
            return parser.parse(uri.getLabel());
        } else {
            return null;
        }
    }
    
    ProbeProcess probe(IDatatype dt) throws LoadException {
        IDatatype body = getValue(EXP, dt);
        WorkflowProcess wp = createProcess(body);
        return new ProbeProcess(wp);
    }
    
    ParallelProcess parallel(Node wf) throws LoadException {
        Node body = getNode(BODY, wf);
        if (body != null) {
            IDatatype dtbody = graph.list(body);
            if (dtbody != null) {
                ParallelProcess pp = new ParallelProcess();
                for (IDatatype dt : dtbody.getValues()) {
                    WorkflowProcess wp = createProcess(dt);
                    pp.add(wp);
                }
                return pp;
            }
        }
        return null;
    }
    
    AssertProcess asserter(IDatatype dt) throws LoadException {
        IDatatype dtest  = getValue(EXP, dt);
        IDatatype dvalue = getValue(VALUE, dt);
        if (dtest != null && dvalue != null){
            WorkflowProcess w = fun(dtest);
            return new AssertProcess(w, dvalue);
        }
        return null;
    }
         
    TestProcess test(IDatatype dt) throws LoadException {
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
    
    WorkflowProcess createIf(IDatatype dt) throws LoadException {
        IDatatype dif = getValue(IF, dt);
        if (dif == null) {
            return null;
        }
       return fun(dif); 
    }
    
    WorkflowProcess fun(IDatatype dt) throws LoadException{
         if (dt.isLiteral()){
            // sw:if "us:test()"
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
}
