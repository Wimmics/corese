package fr.inria.corese.rdf4j;

import java.util.ArrayList;
import java.util.Collection;
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
import fr.inria.corese.rdf4j.convert.ConvertRdf4jCorese;
import fr.inria.corese.rdf4j.convert.datatypes.Rdf4jValueToCoreseDatatype;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * A RDF4J model construct on a Corese Graph
 */
public class CoreseGraphModel extends AbstractModel {

    private Graph corese_graph;
    private final Set<Namespace> namespaces;

    /***************
     * Constructors *
     ****************/

    public CoreseGraphModel() {
        this.corese_graph = Graph.create();
        this.namespaces = new TreeSet<>();
    }

    public CoreseGraphModel(Model rdf4j_model) {
        this(rdf4j_model.getNamespaces());
        addAll(rdf4j_model);
    }

    public CoreseGraphModel(Graph corese_graph) {
        this();
        this.corese_graph = corese_graph;
    }

    public CoreseGraphModel(Collection<? extends Statement> c) {
        this();
        addAll(c);
    }

    public CoreseGraphModel(Set<Namespace> namespaces, Collection<? extends Statement> c) {
        this(c);
        this.namespaces.addAll(namespaces);
    }

    public CoreseGraphModel(Set<Namespace> namespaces) {
        this();
        this.namespaces.addAll(namespaces);
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
        Iterable<Edge> edges = this.choose(subj, pred, obj, contexts);

        // test if result is empty
        if (edges == null || !edges.iterator().hasNext() || edges.iterator().next() == null) {
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
        Iterable<Edge> edges = this.choose(subj, pred, obj, contexts);

        // remove edges
        boolean change = false;
        for (Edge edge : edges) {
            change |= !this.corese_graph.delete(edge).isEmpty();
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
                return CoreseGraphModel.this.getFilterIterator(subj, pred, obj, contexts);
            }

            @Override
            protected void removeFilteredTermIteration(Iterator<Statement> iter, Resource subj, IRI pred, Value obj,
                    Resource... contexts) {
                CoreseGraphModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
            }
        };
    }

    /***********
     * Iterator *
     ************/

    @Override
    public Iterator<Statement> iterator() {
        return this.getFilterIterator(null, null, null);
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

    /*********
     * Tools *
     *********/

    /**
     * Return statements with the specified subject, predicate, object and
     * (optionally) context exist in this model. The subject, predicate and object
     * parameters can be null to indicate wildcards. The contexts parameter is a
     * wildcard and accepts zero or more values. If no contexts are specified,
     * statements will match disregarding their context. If one or more contexts are
     * specified, statements with a context matching one of these will match. Note:
     * to match statements without an associated context, specify the value null and
     * explicitly cast it to type Resource.
     * 
     * @param subj        The subject of the statements to match, null to match
     *                    statements with any subject.
     * @param predThe     Predicate of the statements to match, null to match
     *                    statements with any predicate.
     * @param objThe      Object of the statements to match, null to match
     *                    statements with any object.
     * @param contextsThe Contexts of the statements to match. If no contexts are
     *                    specified, statements will match disregarding their
     *                    context.
     * @return Corese model Statements that match the specified pattern.
     */
    private Iterable<Edge> choose(Resource subj, IRI pred, Value obj, Resource... contexts) {
        this.corese_graph.init();

        // convert subject, predicate, object into Corese Node
        Node subj_node = ConvertRdf4jCorese.rdf4jValueToCoreseNode(subj);
        Node pred_node = ConvertRdf4jCorese.rdf4jValueToCoreseNode(pred);
        Node obj_node = ConvertRdf4jCorese.rdf4jValueToCoreseNode(obj);

        // convert contexts into Corese Nodes
        Node[] contexts_node = ConvertRdf4jCorese.rdf4jContextsToCoreseContexts(contexts);

        // get edges
        Iterable<Edge> corese_iterable;
        if (contexts_node == null) {
            corese_iterable = this.corese_graph.getEdgesRDF4J(subj_node, pred_node, obj_node);
        } else {
            corese_iterable = this.corese_graph.getEdgesRDF4J(subj_node, pred_node, obj_node, contexts_node);
        }

        // Create a new clean iterable (because corse iterable does not have a perfectly
        // defined behavior for optimization reasons)
        ArrayList<Edge> result = new ArrayList<>();
        for (Edge edge : corese_iterable) {
            if (edge != null) {
                result.add(this.corese_graph.getEdgeFactory().copy(edge));
            }
        }

        return result;
    }

    /**
     * Get a Corese model iterator with Statements that match the specified subject,
     * predicate, object and (optionally) context. The subject, predicate and object
     * parameters can be null to indicate wildcards. The contexts parameter is a
     * wildcard and accepts zero or more values. If no contexts are specified,
     * statements will match disregarding their context. If one or more contexts are
     * specified, statements with a context matching one of these will match. Note:
     * to match statements without an associated context, specify the value null and
     * explicitly cast it to type Resource.
     * 
     * @param subj     The subject of the statements to match, null to match
     *                 statements with any subject.
     * @param pred     The Predicate of the statements to match, null to match
     *                 statements with any predicate.
     * @param obj      The Object of the statements to match, null to match
     *                 statements with any object.
     * @param contexts The Contexts of the statements to match. If no contexts are
     *                 specified, statements will match disregarding their context.
     *                 If one or more contexts are specified, statements with a
     *                 context matching one of these will match.
     * @return Corese model iterator on Statements that match the specified subject,
     *         predicate, object and (optionally) context.
     */
    private Iterator<Statement> getFilterIterator(Resource subj, IRI pred, Value obj, Resource... contexts) {
        this.corese_graph.init();

        /**
         * Iterator for the Corese model
         */
        class CoreseModelIterator implements Iterator<Statement> {

            private Iterator<Statement> iter;

            private Statement last;

            public CoreseModelIterator(Iterator<Statement> iter) {
                this.iter = iter;
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Statement next() {
                return last = iter.next();
            }

            @Override
            public void remove() {
                if (last == null) {
                    throw new IllegalStateException();
                }
                CoreseGraphModel.this.remove(last);
                iter.remove();
            }
        }

        // get edges
        Iterable<Statement> edges = ConvertRdf4jCorese.EdgesTostatements(this.choose(subj, pred, obj, contexts));
        return new CoreseModelIterator(edges.iterator());
    }

}
