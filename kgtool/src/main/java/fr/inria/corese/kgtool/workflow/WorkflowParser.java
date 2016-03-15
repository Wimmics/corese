package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.transform.ContextBuilder;
import org.apache.log4j.Logger;

/**
 * Parse a graph that describes a Workflow 
 * [] a st:Workflow ; st:body ( [ a
 * st:Query ; st:uri <q1.rq>] [ a st:Rule ; st:uri st:owlrl ] [ a st:Workflow ;
 * st:uri <wf.ttl> ] [ a st:Template ; st:uri st:turtle ] )] .
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class WorkflowParser {

    private static Logger logger = Logger.getLogger(WorkflowParser.class);
    public static final String PREF = NSManager.STL;
    public static final String QUERY = PREF + "Query";
    public static final String RULE = PREF + "Rule";
    public static final String TEMPLATE = PREF + "Template";
    public static final String WORKFLOW = PREF + "Workflow";
    public static final String URI = PREF + "uri";
    public static final String BODY = PREF + "body";
    public static final String DEBUG = PREF + "debug";
    public static final String LOOP = PREF + "loop";
    public static final String PARAM = PREF + "param";
    
    private Graph graph;
    private WorkflowProcess wp;
    String path;
    private boolean debug = false;

    public WorkflowParser() {
        wp = new WorkflowProcess();
    }

    // Graph describe Workflow
    public WorkflowParser(Graph g) {
        this();
        graph = g;
    }

    public WorkflowParser(WorkflowProcess wp, Graph g) {
        graph = g;
        this.wp = wp;
    }

    public WorkflowProcess parse(Graph g) throws LoadException {
        setGraph(g);
        Entity ent = getBody();
        if (ent != null) {
            parse(ent.getNode(0), ent.getNode(1));
        }
        return wp;
    }

    public WorkflowProcess parse(String path) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path);
        g.init();
        return parse(g);
    }

    // use case: server transformer focus on one specific workflow
    // list is the IDatatype list value of st:workflow slot (special case)
    public WorkflowProcess workflow(IDatatype list) throws LoadException {
        parse(list);
        return wp;
    }

    /**
     * Search st:body edge of outer Workflow Return first result.
     */
    Entity getBody() {
        for (Entity ent : getGraph().getEdges(BODY)) {
            Iterable<Entity> it = getGraph().getEdges(ent.getNode(0), 1);
            if (!it.iterator().hasNext()) {
                return ent;
            }
        }
        return null;
    }

    /**
     *
     * wf a st:Workflow ; st:body body
     */
    void parse(Node wf, Node body) throws LoadException {
        loop(wf);
        debug(wf);
        context(wf);
        IDatatype dt = graph.list(body);
        if (dt != null) {
            parse(dt);
        }
    }

    /**
     * list is the IDatatype list of values of st:body slot
     */
    void parse(IDatatype list) throws LoadException {
        for (IDatatype dt : list.getValues()) {
            add(dt);
        }
    }

    /**
     * dt is element of st:body slot    
     */
    void add(IDatatype dt) throws LoadException {
        if (dt.isURI()) {
            // default: URI is Query
            wp.addQueryPath(dt.getLabel());
        } else {
            addProcess(dt);
        }
    }

    /**
     * [] st:loop 3 ; st:body ( ... )
     * Workflow is a loop.
     */
    void loop(Node wf) {
        IDatatype dt = getGraph().getValue(LOOP, wf);
        if (dt != null) {
            wp.setLoop(dt.intValue());
        }
    }

    void debug(Node wf) {
        IDatatype dt = getGraph().getValue(DEBUG, wf);
        if (dt != null) {
            wp.setDebug(dt.booleanValue());
        }
    }
    
    /**
     * [] a st:Workflow ;
     *   st:param [ st:test 12 ] 
     * Parse st:param as Workflow Context.
     */
    void context(Node wf){
        Node c = getGraph().getNode(PARAM, wf);
        if (c != null){
            ContextBuilder cb = new ContextBuilder(getGraph());
            Context context = cb.process(c);
            wp.setContext(context);
            System.out.println(context);
        }
    }

    /**
     * dt is element of workflow st:body ( ... dt ...)
     */
    void addProcess(IDatatype dt) throws LoadException {
        IDatatype dtype = getGraph().getValue(RDF.TYPE, dt);
        if (isDebug()) {
            System.out.println("WP: " + dt + " " + dtype);
        }
        if (dtype != null) {
            String type = dtype.getLabel();
            if (type.equals(WORKFLOW)) {
                addWorkflow(dt);
            } else {
                IDatatype duri = getGraph().getValue(URI, dt);
                if (duri != null) {
                    String uri = duri.getLabel();
                    if (type.equals(QUERY)) {
                        wp.addQueryPath(uri);
                    } else if (type.equals(RULE)) {
                        wp.addRule(uri);
                    } else if (type.equals(TEMPLATE)) {
                        wp.addTemplate(uri);
                    }
                }
            }
        }
    }

    /**
     * dt is a (sub) Workflow element    
     */
    void addWorkflow(IDatatype dt) throws LoadException {
        WorkflowProcess p = parseWorkflow(dt);
        if (p != null) {
            wp.add(p);
        }
    }

    /**
     * dt is Workflow element of a st:body : st:body ( [ a st:Workflow ; st:body
     * | st:uri ] ) dt is the subject of the subWorkflow definition
     */
    WorkflowProcess parseWorkflow(IDatatype dt) throws LoadException {
        Node wf = getGraph().getNode(dt);
        if (wf != null) {
            Edge body = getGraph().getEdge(BODY, wf, 0);
            Edge uri  = getGraph().getEdge(URI, wf, 0);
            if (body != null) {
                WorkflowParser parser = new WorkflowParser(getGraph());
                parser.parse(body.getNode(0), body.getNode(1));
                return parser.getWorkflowProcess();
            } 
            else if (uri != null) {
                WorkflowParser parser = new WorkflowParser();
                parser.parse(uri.getNode(1).getLabel());
                return parser.getWorkflowProcess();
            }
        }
        return null;
    }

    /**
     * @return the wp
     */
    public WorkflowProcess getWorkflowProcess() {
        return wp;
    }

    /**
     * @param wp the wp to set
     */
    public void setWorkflowProcess(WorkflowProcess wp) {
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
}
