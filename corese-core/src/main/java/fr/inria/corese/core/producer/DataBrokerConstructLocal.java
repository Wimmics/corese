
package fr.inria.corese.core.producer;

import java.util.List;

import fr.inria.corese.core.api.DataBrokerConstruct;
import fr.inria.corese.core.query.update.GraphManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.Basic;

/**
 * Broker between GraphManager and Corese graph
 * Used by:
 * sparql construct/update with corese graph
 * sparql construct when external graph
 * Hence, construct always a corese graph.
 * 
 */
public class DataBrokerConstructLocal
        extends DataBrokerLocal
        implements DataBrokerConstruct {

    private GraphManager graphManager;

    public DataBrokerConstructLocal(GraphManager mgr) {
        super(mgr.getGraph());
        setGraphManager(mgr);
    }

    @Override
    public Node getNode(Node gNode, IDatatype dt) {
        return getGraph().getNode(gNode, dt, true, false);
    }

    @Override
    public void add(Node node) {
        getGraph().add(node);
    }

    @Override
    public void add(Node node, int n) {
        getGraph().add(node, n);
    }

    @Override
    public void addPropertyNode(Node node) {
        getGraph().addPropertyNode(node);
    }

    @Override
    public void addGraphNode(Node node) {
        getGraph().addGraphNode(node);
    }

    @Override
    public boolean exist(Node property, Node subject, Node object) {
        return getGraph().exist(property, subject, object);
    }

    @Override
    public Edge find(Edge edge) {
        return getGraph().find(edge);
    }
    
    @Override
    public String blankNode() {
        return getGraph().newBlankID();
    } 

    /**
     * Return null if edge already exists
     */
    @Override
    public Edge insert(Edge ent) {
        return getGraph().addEdge(ent);
    }

    @Override
    public void insert(Node predicate, List<Edge> list) {
        getGraph().addOpt(predicate, list);
    }

    /******************
     * Update
     */

    @Override
    public List<Edge> delete(Edge edge) {
        return getGraph().delete(edge);
    }

    /**
     * Delete occurrences of edge in named graphs of from list
     * keep other occurrences
     * edge has no named graph
     * Return the list of deleted edges
     */
    @Override
    public List<Edge> delete(Edge ent, List<Constant> from) {
        return getGraph().delete(ent, from);
    }

    @Override
    public boolean load(Query q, Basic ope, Access.Level level, AccessRight access)
            throws EngineException {
        return getGraphManager().myLoad(q, ope, level, access);
    }

    @Override
    public void clear(String name, boolean silent) {
        getGraph().clear(name, silent);
    }

    @Override
    public void deleteGraph(String name) {
        getGraph().deleteGraph(name);
    }

    @Override
    public void clearNamed() {
        getGraph().clearNamed();
    }

    @Override
    public void dropGraphNames() {
        getGraph().dropGraphNames();
    }

    @Override
    public void clearDefault() {
        getGraph().clearDefault();
    }

    @Override
    public boolean add(String source, String target, boolean silent) {
        return getGraph().add(source, target, silent);
    }

    @Override
    public boolean move(String source, String target, boolean silent) {
        return getGraph().move(source, target, silent);
    }

    @Override
    public boolean copy(String source, String target, boolean silent) {
        return getGraph().copy(source, target, silent);
    }

    @Override
    public void addGraph(String uri) {
        getGraph().addGraph(uri);
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

}
