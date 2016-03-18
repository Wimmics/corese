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
import fr.inria.edelweiss.kgtool.transform.ContextBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
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

    public static final String WORKFLOW = PREF + "Workflow";
    public static final String URI = PREF + "uri";
    public static final String BODY = PREF + "body";
    public static final String DEBUG = PREF + "debug";
    public static final String LOOP = PREF + "loop";
    public static final String PARAM = PREF + "param";
    public static final String PATH = PREF + "path";
    public static final String NAME = PREF + "name";
    public static final String REC = PREF + "rec";
    public static final String PROBE = AbstractProcess.PROBE;
    public static final String DISPLAY = PREF + "display";
    public static final String RESULT = PREF + "result";
    public static final String GRAPH = PREF + "graph";
   
    private Graph graph;
    private SemanticWorkflow wp;
    private String path;
    private boolean debug = false;
    
    static final ArrayList<String> topLevel;
    
    static {
        topLevel = new ArrayList<String>();
        topLevel.add(BODY);
    }

    public WorkflowParser() {
        wp = new SemanticWorkflow();
    }

    // Graph describe Workflow
    public WorkflowParser(Graph g) {
        this();
        graph = g;
    }

    public WorkflowParser(SemanticWorkflow wp, Graph g) {
        graph = g;
        this.wp = wp;
    }

    public SemanticWorkflow parse(Graph g) throws LoadException {
        setGraph(g);
        Node node = getWorkflowNode();
        if (node != null) {
            parse(node);
        }
        complete(g);
        return wp;
    }

    public SemanticWorkflow parse(String path) throws LoadException {
        setPath(path);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path, getFormat(ld, path));
        g.init();
        return parse(g);
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
 
 
    
     public SemanticWorkflow parse(Node wf) throws LoadException {
        loop(wf);      
        context(wf);
        Node body = getGraph().getNode(BODY, wf);
        if (body != null) {
            parse(wf, body);
        } 
        complete(wp, (IDatatype) wf.getValue());
        return wp;
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
    
    
    
    Node getWorkflowNode() {
        return getTopLevel(topLevel);
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
        IDatatype dt = getGraph().getValue(LOOP, wf);
        if (dt != null) {
            wp.setLoop(dt.intValue());
        }
    }
    
    /**
     * [] a sw:Workflow ;
     *   sw:param [ sw:test 12 ] 
     * Parse sw:param as Workflow Context.
     */
    void context(Node wf){
        Node c = getGraph().getNode(PARAM, wf);
        if (c != null){
            ContextBuilder cb = new ContextBuilder(getGraph());           
            if (wp.getContext() != null){
                // wp workflow already has a Context
                // complete wp context with current specification
                // use case: server profile has a st:param Context
                // and workflow has a sw:param Context
                // result: merge them
                cb.setContext(wp.getContext());
            }
            Context context = cb.process(c);
            wp.setContext(context);           
            if (isDebug()){
                System.out.println(context);
            }
        }
    }
    
    void complete(Graph g) {
        if (wp.getContext() != null){
            complete(wp.getContext(), g);
        }
    }
    
    void complete(Context c, Graph g){
        if (c.get(Context.STL_SERVER_PROFILE) == null){
            c.set(Context.STL_SERVER_PROFILE, DatatypeMap.createObject(g));
        }
    }

    /**
     * dt is element of workflow sw:body ( ... dt ...)
     */
    void addProcess(IDatatype dt) throws LoadException {
        IDatatype dtype = getGraph().getValue(RDF.TYPE, dt);
        if (isDebug()) {
            System.out.println("WP: " + dt + " " + dtype);
        }
        if (dtype == null){
            if (dt.isURI()){
                // default is Query
                wp.addQueryPath(dt.getLabel());
            }
        }
        else {
            String type = dtype.getLabel();
            if (type.equals(WORKFLOW)) {
                addWorkflow(dt);
            } else {
                IDatatype duri = getGraph().getValue(URI, dt);
                IDatatype dbody = getGraph().getValue(BODY, dt);
                
                boolean ok = false;
                if (duri != null) {
                    String uri = duri.getLabel();
                    if (type.equals(QUERY) || type.equals(UPDATE)) {
                        wp.addQueryPath(uri); ok = true;
                    } else if (type.equals(RULE) || type.equals(RULEBASE)) {
                        wp.addRule(uri); ok = true;
                    } else if (type.equals(TEMPLATE) || type.equals(TRANSFORMATION)) {
                        wp.addTemplate(uri); ok = true;
                    }                    
                }
                else if (type.equals(LOAD)) {
                        load(dt); ok = true;
                }
                else if (dbody != null) {
                    if (type.equals(QUERY) || type.equals(UPDATE)) {
                        wp.addQuery(dbody.getLabel(), getPath()); ok = true;
                    }
                } 
                
                if (ok){
                    complete(wp.getProcessLast(), dt);
                }
            }
        }
    }
    
    /**
     * sw:debug true
     * sw:probe true
     */
    void complete(AbstractProcess p, IDatatype dt){
        p.setURI(dt.getLabel());
        IDatatype db = getGraph().getValue(DEBUG, dt);      
        if (db != null){
            p.setDebug(db.booleanValue());
        }
        IDatatype dp = getGraph().getValue(PROBE, dt);      
        if (dp != null){
            p.setProbe(dp.booleanValue());
        }
        IDatatype dd = getGraph().getValue(DISPLAY, dt);      
        if (dd != null){
            p.setDisplay(dd.booleanValue());
        }
        IDatatype dr = getGraph().getValue(RESULT, dt);      
        if (dr != null){
            p.setResult(dr.getLabel());
        }
    }
    
    
    void load(IDatatype dt){
        Node subject = getGraph().getNode(dt);
        IDatatype dname = getGraph().getValue(NAME, dt);
        IDatatype drec = getGraph().getValue(REC, dt);
        String name = (dname == null) ? null : dname.getLabel();
        boolean rec = (drec == null) ? false : drec.booleanValue();
        for (Entity ent : getGraph().getEdges(PATH, subject, 0)){
            String pp = ent.getNode(1).getLabel();
            wp.add(new LoadProcess(pp, name, rec));
        }
    }

    /**
     * dt is a (sub) Workflow element    
     */
    void addWorkflow(IDatatype dt) throws LoadException {
        Node wf = getGraph().getNode(dt);
        if (wf != null) {
            SemanticWorkflow p = parseSubWorkflow(wf);
            if (p != null) {
                wp.add(p);
            }
        }
    }

    /**
     * dt is Workflow element of a sw:body : sw:body ( [ a sw:Workflow ; sw:body
     * | sw:uri ] ) dt is the subject of the subWorkflow definition
     */
    SemanticWorkflow parseSubWorkflow(Node wf) throws LoadException {
        Node body = getGraph().getNode(BODY, wf);
        Node uri  = getGraph().getNode(URI, wf);
        if (body != null) {
            WorkflowParser parser = new WorkflowParser(getGraph());
            parser.parse(wf); 
            return parser.getWorkflowProcess();
        } else if (uri != null) {
            WorkflowParser parser = new WorkflowParser();
            parser.parse(uri.getLabel());
            return parser.getWorkflowProcess();
        } else {
            return null;
        }
    }

    /**
     * @return the wp
     */
    public SemanticWorkflow getWorkflowProcess() {
        return wp;
    }

    /**
     * @param wp the wp to set
     */
    public void setWorkflowProcess(SemanticWorkflow wp) {
        this.wp = wp;
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
}
