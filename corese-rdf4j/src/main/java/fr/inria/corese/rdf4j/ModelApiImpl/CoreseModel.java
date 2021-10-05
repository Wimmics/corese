package fr.inria.corese.rdf4j.ModelApiImpl;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.AbstractModel;
import org.eclipse.rdf4j.model.impl.FilteredModel;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl.Utils;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class CoreseModel extends AbstractModel {

    private Graph corese_graph;
    private final Set<Namespace> namespaces;

    /***************
     * Constructors *
     ****************/

    public CoreseModel() {
        this.corese_graph = Graph.create();
        this.namespaces = new TreeSet<>();
    }

    /****************
     * add functions *
     *****************/

    /**
     * Add one statement ({@code subj}, {@code pred}, {@code obj}, {@code context})
     * in the Corese graph.
     * 
     * @param subj    Subject of the statement.
     * @param pred    Predicate of the statement.
     * @param obj     Object of the statement.
     * @param context Context of the statement.
     * @return True if the Corese graph is modify, else false.
     */
    private Boolean addStatementInCoreseGraph(IDatatype subj, IDatatype pred, IDatatype obj, IDatatype context) {

        Node subj_node = this.corese_graph.addNode(subj);
        Node pred_node = this.corese_graph.addProperty(pred.getLabel());
        Node obj_node = this.corese_graph.addNode(obj);

        Edge edge;
        if (context == null) {
            edge = this.corese_graph.addEdge(subj_node, pred_node, obj_node);
        } else {
            Node context_node = this.corese_graph.addGraph(context.getLabel());
            edge = this.corese_graph.addEdge(context_node, subj_node, pred_node, obj_node);
        }

        return edge != null;
    }

    @Override
    public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {

        if (subj == null || pred == null || obj == null) {
            throw new UnsupportedOperationException("Incomplete statement");
        }

        // Convert subject, predicate, object into IDatatype
        IDatatype subj_corese = Rdf4jValueToCoreseDatatype.convert(subj);
        IDatatype pred_corese = Rdf4jValueToCoreseDatatype.convert(pred);
        IDatatype obj_corese = Rdf4jValueToCoreseDatatype.convert(obj);

        // Add statements with no context
        if (contexts == null || contexts.length == 0 || (contexts.length == 1 && contexts[0] == null)) {
            return this.addStatementInCoreseGraph(subj_corese, pred_corese, obj_corese, null);
        }

        // Add statements with one or more contexts
        boolean changed = false;
        for (Resource context : contexts) {
            IDatatype context_corese = Rdf4jValueToCoreseDatatype.convert(context);
            changed |= this.addStatementInCoreseGraph(subj_corese, pred_corese, obj_corese, context_corese);
        }
        return changed;
    }

    /*********************
     * contains functions *
     **********************/

    @Override
    public boolean contains(Resource subj, IRI pred, Value obj, Resource... contexts) {
        // get edges
        Iterable<Statement> statements = Utils.getInstance().getEdges(this.getCoreseGraph(), subj, pred, obj, contexts);

        // test if result is empty
        if (statements == null || !statements.iterator().hasNext() || statements.iterator().next() == null) {
            return false;
        }
        return true;
    }

    /*******************
     * remove functions *
     ********************/

    @Override
    public boolean remove(Resource subj, IRI pred, Value obj, Resource... contexts) {
        // get edges
        Iterable<Statement> statements = Utils.getInstance().getEdges(this.getCoreseGraph(), subj, pred, obj, contexts);

        // remove edges
        boolean change = false;
        for (Statement statement : statements) {
            change |= !this.corese_graph.delete((Edge) statement).isEmpty();
        }
        return change;
    }

    /**********************
     * Namespace functions *
     ***********************/

    @Override
    public Set<Namespace> getNamespaces() {
        return this.namespaces;
    }

    @Override
    public void setNamespace(Namespace namespace) {
        this.removeNamespace(namespace.getPrefix());
        this.namespaces.add(namespace);
    }

    @Override
    public Optional<Namespace> removeNamespace(String prefix) {
        Optional<Namespace> result = this.getNamespace(prefix);
        result.ifPresent(namespaces::remove);
        return result;
    }

    /**********
     * Filter *
     ***********/

    @Override
    public Model filter(Resource subj, IRI pred, Value obj, Resource... contexts) {
        return new FilteredModel(this, subj, pred, obj, contexts) {
            @Override
            public Iterator<Statement> iterator() {
                return Utils.getInstance().getFilterIterator(CoreseModel.this, subj, pred, obj, contexts);
            }

            @Override
            protected void removeFilteredTermIteration(Iterator<Statement> iter, Resource subj, IRI pred, Value obj,
                    Resource... contexts) {
                CoreseModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
            }
        };
    }

    /***********
     * Iterator *
     ************/

    @Override
    public Iterator<Statement> iterator() {
        return Utils.getInstance().getFilterIterator(this, null, null, null);
    }

    @Override
    public void removeTermIteration(Iterator<Statement> iterator, Resource subj, IRI pred, Value obj,
            Resource... contexts) {
        this.remove(subj, pred, obj, contexts);
    }

    /*******************
     * other functions *
     *******************/

    @Override
    public int size() {
        return this.corese_graph.size();
    }

    /**
     * Get the equivalent corese graph.
     * 
     * @return Equivalent corese graph.
     */
    public Graph getCoreseGraph() {
        return this.corese_graph;
    }

}
