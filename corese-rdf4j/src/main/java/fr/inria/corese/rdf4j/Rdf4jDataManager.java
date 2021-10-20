package fr.inria.corese.rdf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

public class Rdf4jDataManager implements DataManager {

    private Model rdf4j_model;
    private Node default_corese_context;

    /****************
     * Constructors *
     ****************/

    /**
     * Constructor of Rdf4jDataManager from a RDF4J Model.
     * 
     * @param rdf4j_model RDF4J model.
     */
    public Rdf4jDataManager(Model rdf4j_model) {
        this.rdf4j_model = rdf4j_model;

        IDatatype default_context_datatype = DatatypeMap.createResource(ExpType.DEFAULT_GRAPH);
        this.default_corese_context = NodeImpl.create(default_context_datatype);
    }

    /**
     * Constructor of Rdf4jDataManager from a RDF4J Model and a specific default
     * corese context.
     * 
     * @param rdf4j_model            RDF4J model.
     * @param default_corese_context Value of the default corese context.
     */
    public Rdf4jDataManager(Model rdf4j_model, Node default_corese_context) {
        this.rdf4j_model = rdf4j_model;
        this.default_corese_context = default_corese_context;
    }

    /*********
     * Count *
     *********/

    @Override
    public int graphSize() {
        return this.rdf4j_model.size();
    }

    @Override
    public int countEdges(Node predicate) {
        // convert Corese node to RDF4J Value
        IRI rdf4j_predicate = (IRI) Convert.coreseNodeToRdf4jValue(predicate);

        return this.rdf4j_model.filter(null, rdf4j_predicate, null).size();
    }

    /************
     * GetEdges *
     ************/

    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> corese_contexts) {
        // convert Corese node to RDF4J Value
        Resource rdf4j_subject = (Resource) Convert.coreseNodeToRdf4jValue(subject);
        IRI rdf4j_predicate = (IRI) Convert.coreseNodeToRdf4jValue(predicate);
        Value rdf4j_object = Convert.coreseNodeToRdf4jValue(object);

        // convert list of corese contexts to list of RDF4J contexts
        Resource[] rdf4j_contexts;
        if (corese_contexts == null) {
            rdf4j_contexts = Convert.coreseGraphsToRdf4jContexts(this.default_corese_context, (Node) null);
        } else {
            Node[] corese_contexts_array = corese_contexts.toArray(new Node[corese_contexts.size()]);
            rdf4j_contexts = Convert.coreseGraphsToRdf4jContexts(this.default_corese_context, corese_contexts_array);
        }

        Iterable<Statement> statements = this.rdf4j_model.getStatements(rdf4j_subject, rdf4j_predicate, rdf4j_object,
                rdf4j_contexts);

        // convert Statements to Edges
        // remove duplicate edges (same edge with different context)
        HashMap<Integer, Edge> result = new HashMap<>();
        for (Edge statement : Convert.statementsToEdges(this.default_corese_context, statements)) {
            int hash = Objects.hash(statement.getSubject(), statement.getPredicate(), statement.getObject());
            result.put(hash, statement);
        }

        return result.values();
    }

    /*************
     * Get lists *
     *************/

    @Override
    public Iterable<Node> subjects(Node corese_context) {
        Set<Resource> subjects;

        // if corese_context is null then match with all contexts in graph
        if (corese_context == null) {
            subjects = this.rdf4j_model.subjects();
        } else {
            Resource rdf4j_context = Convert.coreseGraphToRdf4jContext(this.default_corese_context, corese_context);
            subjects = this.rdf4j_model.filter(null, null, null, rdf4j_context).subjects();
        }

        return Convert.rdf4jvaluestoCoresNodes(new ArrayList<>(subjects));
    }

    @Override
    public Iterable<Node> predicates(Node corese_context) {
        Set<IRI> predicates;

        // if corese_context is null then match with all contexts in graph
        if (corese_context == null) {
            predicates = this.rdf4j_model.predicates();
        } else {
            Resource rdf4j_context = Convert.coreseGraphToRdf4jContext(this.default_corese_context, corese_context);
            predicates = this.rdf4j_model.filter(null, null, null, rdf4j_context).predicates();
        }

        return Convert.rdf4jvaluestoCoresNodes(new ArrayList<>(predicates));
    }

    @Override
    public Iterable<Node> objects(Node corese_context) {
        Set<Value> objects;

        // if corese_context is null then match with all contexts in graph
        if (corese_context == null) {
            objects = this.rdf4j_model.objects();
        } else {
            Resource rdf4j_context = Convert.coreseGraphToRdf4jContext(this.default_corese_context, corese_context);
            objects = this.rdf4j_model.filter(null, null, null, rdf4j_context).objects();
        }

        return Convert.rdf4jvaluestoCoresNodes(new ArrayList<>(objects));
    }

    @Override
    public Iterable<Node> contexts() {
        Set<Resource> rdf4j_contexts = this.rdf4j_model.contexts();
        Node[] corese_contexts = Convert.rdf4jContextsToCoreseGraphs(this.default_corese_context,
                rdf4j_contexts.toArray(new Resource[rdf4j_contexts.size()]));
        return Arrays.asList(corese_contexts);
    }

    /**********
     * Insert *
     **********/
    @Override
    public Edge insert(Edge edge) {
        Statement statement = Convert.EdgeToStatement(edge);
        if (this.rdf4j_model.add(statement)) {
            return edge;
        } else {
            return null;
        }
    }

    /****************
     * Delete edges *
     ****************/

    public List<Edge> delete(Edge edge) {

        ArrayList<Edge> result = new ArrayList<>();

        // get list of statement that match the subject, predicate, object
        ArrayList<Statement> statements_to_remove = new ArrayList<>();
        this.rdf4j_model.getStatements(edge.getSubject(), edge.getPredicate(), edge.getObject())
                .forEach(statements_to_remove::add);

        // remove statement
        for (Statement statement : statements_to_remove) {
            if (this.rdf4j_model.remove(statement)) {
                result.add(Convert.statementToEdge(this.default_corese_context, statement));
            }
        }

        return result;
    }

    public List<Edge> delete(Edge edge, List<Node> corese_graph) {

        // convert list of corese graphs to list of RDF4J contexts
        Resource[] rdf4j_contexts;
        if (corese_graph == null) {
            return this.delete(edge);
        } else {
            Node[] graphs_java_list = new Node[corese_graph.size()];
            graphs_java_list = corese_graph.toArray(graphs_java_list);
            rdf4j_contexts = Convert.coreseGraphsToRdf4jContexts(this.default_corese_context, graphs_java_list);
        }

        // reconstitution of the statement with context
        ValueFactory vf = SimpleValueFactory.getInstance();
        ArrayList<Edge> result = new ArrayList<>();
        for (Resource context : rdf4j_contexts) {
            Statement statement = vf.createStatement(edge.getSubject(), edge.getPredicate(), edge.getObject(), context);

            // remove from model
            if (this.rdf4j_model.remove(statement)) {
                result.add(Convert.statementToEdge(this.default_corese_context, statement));
            }
        }

        return result;
    }

    /*********
     * Clear *
     *********/

    @Override
    public void clear(String name, boolean silent) {

        ValueFactory vf = SimpleValueFactory.getInstance();
        Resource context = vf.createIRI(name);

        this.rdf4j_model.clear(context);
    }

    @Override
    public void clearNamed() {
        Set<Resource> contexts = new HashSet<>(this.rdf4j_model.contexts());
        for (Resource context : contexts) {
            if (context != null) {
                this.clear(context.stringValue(), false);
            }
        }
    }

    /****************
     * Delete graph *
     ****************/

    @Override
    public void deleteGraph(String name) {
        this.clear(name, false);
    }

    @Override
    public void dropGraphNames() {
        this.clearNamed();
    }

    /*******************
     * Graph operation *
     *******************/
    public boolean add(String source, String target, boolean silent) {
        // convert source and target to RDF4J context
        ValueFactory vf = SimpleValueFactory.getInstance();
        Resource source_context = vf.createIRI(source);
        Resource target_context = vf.createIRI(target);

        // get list of statement in source context
        ArrayList<Statement> source_statements = new ArrayList<>();
        this.rdf4j_model.getStatements(null, null, null, source_context).forEach(source_statements::add);

        boolean is_modified = false;
        for (Statement source_statement : source_statements) {
            // build statement to add
            Statement target_statement = vf.createStatement(source_statement.getSubject(),
                    source_statement.getPredicate(), source_statement.getObject(), target_context);
            // add statement in target context
            if (this.rdf4j_model.add(target_statement)) {
                is_modified = true;
            }
        }

        return is_modified;
    }

    public boolean move(String source, String target, boolean silent) {
        Boolean result = this.copy(source, target, silent);
        this.clear(source, silent);
        return result;
    }

    public boolean copy(String source, String target, boolean silent) {
        this.clear(target, silent);
        Boolean result = this.add(source, target, silent);
        return result;
    }

}
