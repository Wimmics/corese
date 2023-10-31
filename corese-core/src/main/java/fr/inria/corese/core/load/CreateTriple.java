package fr.inria.corese.core.load;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;

/**
 *
 * @author Olivier Corby, INRIA 2020
 */
public class CreateTriple {
    public static Logger logger = LoggerFactory.getLogger(CreateTriple.class);
    static final String STAR = "*";

    private QueryProcess queryProcess;
    Load load;
    Graph graph;
    private DataManager dataManager;
    private String path;
    IDatatype dtpath;
    ArrayList<String> exclude;
    private boolean skip = false;
    int count = 1;
    int limit = Integer.MAX_VALUE;

    CreateTriple() {
    }

    CreateTriple(Graph g, Load ld) {
        load = ld;
        queryProcess = load.getCreateQueryProcess();
        graph = g;
        exclude = new ArrayList<>();
    }

    public void start() {
        declare();
        graph.getEventManager().start(Event.LoadAPI);
    }

    public Graph getGraph() {
        return graph;
    }

    void declare() {
        dtpath = DatatypeMap.newResource(getPath() == null ? Entailment.DEFAULT : getPath());
    }

    public void finish() {
        graph.getEventManager().finish(Event.LoadAPI);
    }

    void add(Edge e) {
        if (AccessRight.isActive()) {
            if (!getAccessRight().setInsert(e)) {
                return;
            }
        }
        Edge edge = addEdge(e);
        if (edge != null) {
            declare(edge);
        }
    }

    Node getProperty(Atom res) {
        return addProperty(res.getLabel());
    }

    String newBlankID() {
        if (hasDataManager()) {
            return getDataManager().blankNode();
        }
        return graph.newBlankID();
    }

    Edge create(Node g, Node s, Node p, Node o) {
        return graph.createForInsert(g, s, p, o);
    }

    Edge create(Node g, Node p, List<Node> list) {
        return graph.create(g, p, list);
    }

    Edge create(Node g, Node p, List<Node> list, boolean nested) {
        Edge e = graph.create(g, p, list, nested);
        return e;
    }

    /**
     * Graph api
     */

    Edge addEdge(Edge e) {
        if (hasDataManager()) {
            return getDataManager().insert(e);
        } else {
            // System.out.println("CT: add " + e);
            return graph.addEdge(e);
        }
    }

    /**
     * We can keep Node creation functions on the graph
     * or we could provide NodeImpl() with a DataBroker
     * All we need is to have a broker for addEdge above.
     */

    Node addGraph(String src) {
        return graph.addGraph(src);
    }
    
    Node addGraph(Atom src) {
        return graph.addGraph(src.getLabel(), src.isBlank());
    }

    Node addDefaultGraphNode() {
        return graph.addDefaultGraphNode();
    }

    Node addProperty(String label) {
        return graph.addProperty(label);
    }

    Node addLiteral(String predicate, String label, String datatype, String lang) {
        return graph.addLiteral(predicate, label, datatype, lang);
    }

    Node addLiteral(String label, String datatype, String lang) {
        return graph.addLiteral(label, datatype, lang);
    }

    Node addBlank(String label) {
        return graph.addBlank(label);
    }

    Node addTripleReference(String label) {
        return graph.addTripleReference(label);
    }

    Node addResource(String label) {
        return graph.addResource(label);
    }

    Node addNode(Constant lit) {
        return graph.getNode(lit.getDatatypeValue(), true, true);
    }

    void declare(Edge edge) {
        if (load.isEvent() && getQueryProcess() != null) {
            getQueryProcess().getVisitor().insert(dtpath, edge);
        }
    }

    AccessRight getAccessRight() {
        return load.getAccessRight();
    }

    public void exclude(String ns) {
        if (ns == null) {
            exclude.clear();
        } else if (ns.equals(STAR)) {
            setSkip(true);
        } else {
            exclude.add(ns);
        }
    }

    public void exclude(List<String> list) {
        for (String name : list) {
            exclude(name);
        }
    }

    public boolean accept(String pred) {
        if (raiseLimit()) {
            return false;
        }
        if (count > 100000) {
            graph.getEventManager().process(Event.LoadStep);
            count = 2;
        } else {
            count++;
        }
        if (isSkip()) {
            return false;
        }
        if (exclude.isEmpty()) {
            return true;
        }
        for (String ns : exclude) {
            if (pred.startsWith(ns)) {
                return false;
            }
        }
        return true;
    }

    public boolean raiseLimit() {
        return graph.size() >= getLimit();
    }

    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    boolean hasDataManager() {
        return getDataManager() != null;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
