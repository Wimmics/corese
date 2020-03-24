package fr.inria.corese.core.query.update;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Update;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.Workflow;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.load.LoadException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 *
 * Graph Manager for Construct and Update operations
 * Partially used by RuleEngine
 * By convention Edges are Quads, they all have a named graph
 * which may be the default graph name
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class GraphManager {
    static Logger logger = LoggerFactory.getLogger(GraphManager.class);

    //static final String DEFAULT_GRAPH = Entailment.DEFAULT;
    // default loader, by meta protocol to preserve modularity
    static final String LOADER = "fr.inria.corese.kgtool.load.Load";
    private Graph graph;
    Load load;
    private QueryProcess queryProcess;

    public GraphManager(Graph g) {
        graph = g;
        load = getLoader();
        load.init(graph);
    }
    
   
      /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
    
    public static Load getLoader() {
        return new Load();
    }

    
    /***********************************************************
     * Construct Graph Manager
     *     
     **********************************************************/

    /**
     * Corese extension:
     * Additional system named graph 
     * Optional
     */
    public GraphManager getNamedGraph(String label) {
        Graph g = graph.getNamedGraph(label);
        if (g != null && g != graph.getGraph()) {
            return new GraphManager(g);
        }
        return null;
    }
    
    public Node getDefaultGraphNode(){
        return graph.addDefaultGraphNode();
    }
    
    public boolean isDefaultGraphNode(String name){
        return graph.isDefaultGraphNode(name);
    }
     
    /**
     * Return existing resource Node (URI only)
     * If not exist, create a Node, do not add it into the graph
     */
    Node getResourceNode(IDatatype dt) {
        return graph.getResourceNode(dt, true, false);
    }

    /**
     * Return existing Node (any type of Node)
     * If not exist, create a Node, do not add it into the graph
     * gNode is the name of a named graph
     */
    public Node getNode(Node gNode, IDatatype dt) {
        return graph.getNode(gNode, dt, true, false);
    }
    
    /**
     * Return existing Node (any type of Node)
     * If not exist, create a Node, add it into the graph
     * gNode is the name of a named graph
     */
    public Node getCreateNode(Node gNode, IDatatype dt) {
        return graph.getNode(gNode, dt, true, true);
    }
    

    /**
     * Before construct/insert/delete starts
     */
    public void start(Event e){
        graph.getEventManager().start(e);        
    }
    /**
     * After construct/insert/delete completes 
     * Tell the graph to recompile its Index
     */
    public void finish(Event e) {
        if (graph.size() > 0) {
            // index graph
            graph.getEventManager().finish(e);
        }
    }

    /**
     * Delete occurrences of edge in named graphs of from list
     * keep other occurrences
     * edge has no named graph
     * Return the list of deleted edges
     */
    public List<Edge> delete(Edge ent, List<Constant> from) {
        return graph.delete(ent, from);
    }

    /**
     * If Edge have a named graph: delete this occurrence
     * Otherwise: delete all occurrences of edge 
     */
    public List<Edge> delete(Edge ent) {
        return graph.delete(ent);
    }

    /**
     * Return null if edge already exists in graph
     * and in its named graph
     */
    public Edge insert(Edge ent) {
        return graph.addEdge(ent);
    }

    /**
     * Test if an edge already exists in the graph, in any named graph
     * Used by RuleEngine only
     */
    public boolean exist(Node property, Node subject, Node object) {
        return graph.exist(property, subject, object);
    }

    /**
     * Add a Node as a Vertex in the graph (subject of object of an edge)
     */
    public void add(Node node) {
        graph.add(node);
    }
    
    public void add(Node node, int n) {
        if (graph.isMetadata() && n > 1){
            return;
        }
        graph.add(node);
    }
    
    public void addPropertyNode(Node property) {
        graph.addPropertyNode(property);
    }

    public Node addGraphNode(Node source) {
        Node node = graph.getNode(source);
        if (node == null){
            node = source;
        }
        graph.addGraphNode(node);
        return node;       
    }

    /**
     * Create a candidate edge to be inserted
     * Do not insert it yet, it will be done explicitely by insert().
     */
    public Edge create(Node source, Node subject, Node property, Node object) {
        return graph.create(source, subject, property, object);
    }
    
    public Edge create(Node source, Node property, List<Node> list) {
        return graph.create(source, property, list);
    }

    /**
     * Create a candidate edge to be deleted
     * Do not delete it yet, it will be done explicitely by delete().
     */
    public Edge createDelete(Node source, Node subject, Node property, Node object) {
        return graph.createDelete(source, subject, property, object);
    }
    
    public Edge createDelete(Node source, Node property, List<Node> list) {
        return graph.createDelete(source, property, list);
    }

    public String newBlankID() {
        return graph.newBlankID();
    }

    public IDatatype createBlank(String str) {
        return graph.createBlank(str);
    }
    
    
    /*****************************************************************************
     * 
     *   SPARQL Update Manager
     * 
     ****************************************************************************/

    void clear(String name, boolean silent) {
        graph.clear(name, silent);
    }

    void deleteGraph(String name) {
        graph.deleteGraph(name);
    }

    void clearNamed() {
        graph.clearNamed();
    }

    void dropGraphNames() {
        graph.dropGraphNames();
    }

    void clearDefault() {
        graph.clearDefault();
    }

    boolean add(String source, String target, boolean silent) {
        return graph.add(source, target, silent);
    }

    boolean move(String source, String target, boolean silent) {
        return graph.move(source, target, silent);
    }

    boolean copy(String source, String target, boolean silent) {
        return graph.copy(source, target, silent);
    }

    void addGraph(String uri) {
        graph.addGraph(uri);
    }
    
    
    boolean load(Query q, Basic ope) {
        Load load = Load.create(graph);
        if (AccessRight.isActive()) {
            ASTQuery ast = (ASTQuery) q.getAST();
            load.setAccessRight(ast.getAccess());
        }
        //getQueryProcess().init(q);
        load.setQueryProcess(getQueryProcess());
        String uri = ope.getURI();
        IDatatype dt = DatatypeMap.newResource(uri);
        String src = ope.getTarget();
        graph.logStart(q);
        graph.getEventManager().start(Event.LoadUpdate);
        //getQueryProcess().getCurrentVisitor().beforeLoad(dt);
        if (ope.isSilent()) {
            try {
                load.parse(uri, src);
            } catch (LoadException ex) {
                logger.error(ex.getMessage());
            }
            graph.logFinish(q);
        } else {
            try {
                load.parse(uri, src);
                graph.logFinish(q);
            } catch (LoadException e) {
                
                boolean error = false;
                
                if (load.getFormat(uri) == Loader.UNDEF_FORMAT
                        && e.getException() != null
                        && e.getException().getMessage().contains("{E301}")) {
                    try {
                        //load.parse(uri, src, src, Loader.TURTLE_FORMAT);
                        load.parse(uri, src, uri, Loader.TURTLE_FORMAT);
                    } catch (LoadException ex) {
                        error = true;
                    }
                }

                if (error) {
                    logger.error("Load error: " + ope.getURI() + "\n" + e);
                    q.addError("Load error: ", ope.getURI() + "\n" + e);
                }
                graph.logFinish(q);
                return ope.isSilent();
            } finally {
                   //getQueryProcess().getCurrentVisitor().afterLoad(dt);
                   graph.getEventManager().finish(Event.LoadUpdate);
            }
        }

        if (load.isRule(uri) && load.getRuleEngine() != null) { 
            // load rule base into workflow
            // TODO ? load <rulebase.rul> into kg:workflow
            // pros: if there are several rule base load, they will be process() together
            // cons: it is stored in the workflow and run forever on update
            // (des)activate
            // pragma {kg:kgram kg:rule true/false}
            graph.addEngine(load.getRuleEngine());
            //graph.setEntail(true);
            graph.getEventManager().start(Event.ActivateEntailment);
        }

        return true;
    }

    
    
    
    /**
     * Corese extension wrt SPARQL Update: optional
     */
     void system(Basic ope) {
        String uri = ope.getGraph();

        if (!isSystem(uri)) {
            return;
        }

        Workflow wf = graph.getWorkflow();

        switch (ope.type()) {

            case Update.DROP:

                if (isRule(uri)) {
                    // clear also the rule base
                    wf.removeEngine(Engine.RULE_ENGINE);
                }

            case Update.CLEAR:

                if (isEntailment(uri)) {
                    //graph.setEntailment(false);
                    graph.getEventManager().finish(Event.ActivateRDFSEntailment);
                } else if (isRule(uri)) {
                   // wf.setActivate(Engine.RULE_ENGINE, false);
                    graph.getEventManager().finish(Event.ActivateRuleEngine);
                }
                break;


            case Update.CREATE:

                if (isEntailment(uri)) {
                    //graph.setEntailment(true);
                    //graph.setEntail(true);
                    graph.getEventManager().start(Event.ActivateRDFSEntailment);
                } else if (isRule(uri)) {
//                    wf.setActivate(Engine.RULE_ENGINE, true);
//                    graph.setEntail(true);
                    graph.getEventManager().start(Event.ActivateRuleEngine);
                }
                break;
        }
    }

    boolean isSystem(String uri) {
        return uri != null && uri.startsWith(Entailment.KGRAPH);
    }

    boolean isEntailment(String uri) {
        return uri.equals(Entailment.ENTAIL);
    }

    boolean isRule(String uri) {
        return uri.equals(Entailment.RULE);
    }

    /**
     * @return the queryProcess
     */
    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    /**
     * @param queryProcess the queryProcess to set
     */
    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }

}
