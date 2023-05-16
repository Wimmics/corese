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
import fr.inria.corese.core.api.DataBrokerConstruct;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.load.LoadException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadFormat;
import fr.inria.corese.core.producer.DataBrokerConstructLocal;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;

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

    private Graph graph;
    Load load;
    private QueryProcess queryProcess;
    // broker to corese graph or external graph 
    private DataBrokerConstruct dataBroker;

    public GraphManager(Graph g) {
        graph = g;
        load = getLoader();
        load.init(graph);
        // local data broker for corese graph
        // may be overloaded by QueryProcess create
        setDataBroker(new DataBrokerConstructLocal(this));
    }
    
    public Graph getGraph() {
        return graph;
    }
    
    public static Load getLoader() {
        return new Load();
    }
    
    public boolean isRDFStar(){
        return getGraph().isRDFStar();
    }

    
    /***********************************************************
     * Construct Graph Manager
     *     
     **********************************************************/
    
    public void startRuleEngine() {
        getDataBroker().startRuleEngine();
    }
    
    public void endRuleEngine() {
        getDataBroker().endRuleEngine();
    }
    
    public void startRule() {
        getDataBroker().startRule();
    }
    
    public void endRule() {
        getDataBroker().endRule();
    }

    /**
     * Before construct/insert/delete starts
     */
    public void start(Event e){
        getGraph().getEventManager().start(e);        
    }
    
    /**
     * After construct/insert/delete completes 
     * Tell the graph to recompile its Index
     */
    public void finish(Event e) {
        if (getGraph().size() > 0) {
            // index graph
            getGraph().getEventManager().finish(e);
        }
    }

    public void trace() {
        System.out.println(getGraph().display());
    }
    
    /**
     * Corese extension:
     * Additional system named graph 
     * Optional
     */
    public GraphManager getNamedGraph(String label) {
        Graph g = getGraph().getNamedGraph(label);
        if (g != null && g != getGraph().getGraph()) {
            return new GraphManager(g);
        }
        return null;
    }
    
    public Node getDefaultGraphNode(){
        return getGraph().addDefaultGraphNode();
    }
    
    // When insert in new graph name, update insert dataset named += name
    public boolean isDefaultGraphNode(String name){
        return getGraph().isDefaultGraphNode(name);
    }
    
    public Node getRuleGraphName(boolean constraint) {
        return getGraph().getRuleGraphName(constraint);
    }
    
    /**
     * Constraint rule may have specific construct graph where to record
     * constraint error
     */
    public GraphManager getGraphManager(boolean isConstraint) {
        if (isConstraint) {
            Graph g = getGraph().getConstraintGraph();
            if (g != getGraph()) {
                return new GraphManager(g);
            }
        }
        return this;
    }
      
    public int size() {
        return getDataBroker().graphSize();
    }
    
    public int size(Node pred) {
        return getDataBroker().graphSize(pred);
    }
    
    public String reference(Node n) {
        return getGraph().reference(n);
    }
    
    public Edge beforeInsert(Edge edge) {
        return getGraph().beforeInsert(edge);
    }

    /**
     * Return null if edge already exists in graph
     * and in its named graph
     * Used by construct/insert/rule
     */
    public Edge insert(Edge edge) {
        return getDataBroker().insert(edge);
    }
    
    // corese optimization, not for extern
    public void insert(Node predicate, List<Edge> list) {
        getDataBroker().insert(predicate, list);
    }

    /**
     * Test if an edge already exists in the graph, in any named graph
     * Used by RuleEngine only
     */
    public boolean exist(Node property, Node subject, Node object) {
        return getDataBroker().exist(property, subject, object);
    }
    
    // find occurrence of (instantiated query) edge in target graph
    // in order to get its reference node if any
    // use case: rdf star
    public Edge find(Edge edge) {
        return getDataBroker().find(edge);
    }

    /**
     * Add a Node as a Vertex in the graph (subject of object of an edge)
     */
    public void add(Node node) {
        getDataBroker().add(node);
    }
    
    public void add(Node node, int n) {
        getDataBroker().add(node, n);
    }
    
    /**
     * Return existing Node (any type of Node) If not exist, create a Node, do
     * not add it into the graph gNode is the name of a named graph gNode is not
     * used with corese graph implementation
     * Node may be skolemized by graph
     */
    public Node getNode(Node gNode, IDatatype dt) {
        return getDataBroker().getNode(gNode, dt);
    }
  
    public void addPropertyNode(Node property) {
        getDataBroker().addPropertyNode(property);
    }

    public void addGraphNode(Node node) { 
        getDataBroker().addGraphNode(node);
    }    
    
    /**
     * Create temporary Edge for construct / update
     */

    /**
     * Create a candidate edge to be inserted
     * Do not insert it yet, it will be done explicitely by insert().
     */
    public Edge create(Node source, Node subject, Node property, Node object) {
        //return graph.create(source, subject, property, object);
        return getGraph().createForInsert(source, subject, property, object);
    }
    
    public Edge create(Node source, Node property, List<Node> list) {
        return getGraph().create(source, property, list);
    }

    /**
     * Create a candidate edge to be deleted
     * Do not delete it yet, it will be done explicitely by delete().
     */
    public Edge createDelete(Node source, Node subject, Node property, Node object) {
        return getGraph().createDelete(source, subject, property, object);
    }
    
    public Edge createDelete(Node source, Node property, List<Node> list) {
        return getGraph().createDelete(source, property, list);
    }

    public String newBlankID() {
        return getDataBroker().blankNode();
    }

    public IDatatype createBlank(String str) {
        return getGraph().createBlank(str);
    }
    
    public IDatatype createBlank() {
        return getGraph().createBlank(newBlankID());
    }
    
    public IDatatype createTripleReference() {
        return getGraph().createTripleReference();
    }
    
    public Node createTripleReference(Node s, Node p, Node o) {
        return getGraph().addTripleReference(s, p, o);
    }
    
    
    /*****************************************************************************
     * 
     *   SPARQL Update Manager
     * 
     ****************************************************************************/
    
    /**
     * Delete occurrences of edge in named graphs of from list
     * keep other occurrences
     * edge has no named graph
     * Return the list of deleted edges
     */
    public List<Edge> delete(Edge ent, List<Constant> from) {
        return getDataBroker().delete(ent, from);
    }

    /**
     * If Edge have a named graph: delete this occurrence
     * Otherwise: delete all occurrences of edge 
     */
    public List<Edge> delete(Edge ent) {
        return getDataBroker().delete(ent);
    }

    void clear(String name, boolean silent) {
        getDataBroker().clear(name, silent);
    }

    void deleteGraph(String name) {
        getDataBroker().deleteGraph(name);
    }

    void clearNamed() {
        getDataBroker().clearNamed();
    }

    void dropGraphNames() {
        getDataBroker().dropGraphNames();
    }

    void clearDefault() {
        getDataBroker().clearDefault();
    }

    boolean add(String source, String target, boolean silent) {
        return getDataBroker().add(source, target, silent);
    }

    boolean move(String source, String target, boolean silent) {
        return getDataBroker().move(source, target, silent);
    }

    boolean copy(String source, String target, boolean silent) {
        return getDataBroker().copy(source, target, silent);
    }

    void addGraph(String uri) {
        getDataBroker().addGraph(uri);
    }
    
    boolean load(Query q, Basic ope, Level level, AccessRight access) throws EngineException {
        return getDataBroker().load(q, ope, level, access);
    }

    
    
   public boolean myLoad(Query q, Basic ope, Level level, AccessRight access) throws EngineException {
        Load load = Load.create(getGraph());
        load.setLevel(level);
        if (AccessRight.isActive()) {
            load.setAccessRight(access);
        }
        //getQueryProcess().init(q);
        load.setQueryProcess(getQueryProcess());
        String uri = ope.getURI();
        IDatatype dt = DatatypeMap.newResource(uri);
        String src = ope.getTarget();
        int format = getFormat(q);
        getGraph().logStart(q);
        getGraph().getEventManager().start(Event.LoadUpdate);
        if (ope.isSilent()) {
            try {
                if (NSManager.isFile(uri)) {
                    // load access file system ?
                    Access.check(Feature.LOAD_FILE, level, uri, TermEval.LOAD_MESS);
                }
                load(load, src, uri, format);
            } catch (LoadException | SafetyException ex) {
                logger.error("Load silent trap error: " + ex.getMessage());
            }
            getGraph().logFinish(q);
        } else {
            if (NSManager.isFile(uri)) {
                Access.check(Feature.LOAD_FILE, level, uri, TermEval.LOAD_MESS);
            }
            
            try {
                load(load, src, uri, format);
            }
            catch (LoadException e) {
                if (e.isSafetyException()) {
                    throw e.getSafetyException();
                }
                logger.error("Load error: " + ope.getURI() + "\n" + e);
                q.addError("Load error: ", ope.getURI() + "\n" + e);
                return ope.isSilent();
            }
            finally {
                getGraph().logFinish(q);
                getGraph().getEventManager().finish(Event.LoadUpdate);
            }
        }
            
        if (load.isRule(uri) && load.getRuleEngine() != null) { 
            // load rule base into workflow
            // TODO ? load <rulebase.rul> into kg:workflow
            // pros: if there are several rule base load, they will be process() together
            // cons: it is stored in the workflow and run forever on update
            // (des)activate
            // pragma {kg:kgram kg:rule true/false}
            getGraph().addEngine(load.getRuleEngine());
            getGraph().getEventManager().start(Event.ActivateEntailment);
        }

        return true;
    }
   
   // format from query metadata @format st:rdfxml
    void load(Load load, String src, String uri, int format) throws LoadException {
        if (format == Loader.UNDEF_FORMAT) {
            load(load, src, uri);
        }
        else {
            load.parse(uri, src, uri, format);
        }
    }

    // try RDF/XML and if parse error try Turtle
    void load(Load load, String src, String uri) throws LoadException {
        try {
            load.parse(uri, src);
        } catch (LoadException e) {
            if (load.getFormat(uri) == Loader.UNDEF_FORMAT
                    && e.getException() != null
                    && e.getException().getMessage().contains("{E301}")) {
                load.parse(uri, src, uri, Loader.TURTLE_FORMAT);
            }
        }
    }
    
    // @format st:rdfxml st:json st:turtle
    int getFormat(Query q) {
        String ft = q.getAST().getMetadataValue(Metadata.FORMAT);
        if (ft == null) {
            return Loader.UNDEF_FORMAT;
        }
        return LoadFormat.getDTFormat(ft);
    }

    
    /**
     * Corese extension wrt SPARQL Update: optional
     */
     void system(Basic ope) {
        String uri = ope.getGraph();

        if (!isSystem(uri)) {
            return;
        }

        Workflow wf = getGraph().getWorkflow();

        switch (ope.type()) {

            case Update.DROP:

                if (isRule(uri)) {
                    // clear also the rule base
                    wf.removeEngine(Engine.RULE_ENGINE);
                }

            case Update.CLEAR:

                if (isEntailment(uri)) {
                    //graph.setEntailment(false);
                    getGraph().getEventManager().finish(Event.ActivateRDFSEntailment);
                } else if (isRule(uri)) {
                   // wf.setActivate(Engine.RULE_ENGINE, false);
                    getGraph().getEventManager().finish(Event.ActivateRuleEngine);
                }
                break;


            case Update.CREATE:

                if (isEntailment(uri)) {
                    //graph.setEntailment(true);
                    //graph.setEntail(true);
                    getGraph().getEventManager().start(Event.ActivateRDFSEntailment);
                } else if (isRule(uri)) {
//                    wf.setActivate(Engine.RULE_ENGINE, true);
//                    graph.setEntail(true);
                    getGraph().getEventManager().start(Event.ActivateRuleEngine);
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

    public DataBrokerConstruct getDataBroker() {
        return dataBroker;
    }

    public void setDataBroker(DataBrokerConstruct dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

}
