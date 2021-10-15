package fr.inria.corese.rdf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private Node default_graph;

    /****************
     * Constructors *
     ****************/

    public Rdf4jDataManager(Model rdf4j_model) {
        this.rdf4j_model = rdf4j_model;

        IDatatype default_graph_datatype = DatatypeMap.createResource(ExpType.DEFAULT_GRAPH);
        this.default_graph = NodeImpl.create(default_graph_datatype);
    }

    public Rdf4jDataManager(Model rdf4j_model, Node default_graph) {
        this.rdf4j_model = rdf4j_model;
        this.default_graph = default_graph;
    }

    /*************
     * GraphSize *
     *************/

    @Override
    public int graphSize() {
        return this.rdf4j_model.size();
    }

    @Override
    public int graphSize(Node predicate) {
        // Convert Corese node to RDF4J Value
        IRI rdf4j_predicate = (IRI) Convert.coreseNodeToRdf4jValue(predicate);

        return this.rdf4j_model.filter(null, rdf4j_predicate, null).size();
    }

    /************
     * GetEdges *
     ************/

    /**
     * Compare to Statement without context.
     * 
     * @param statement_1 First statement to compare.
     * @param statement_2 Second statement to compare.
     * @return True if the two statements have the same subject, predicate, objects.
     *         False else.
     */
    private boolean compareStatementWithoutContext(Statement statement_1, Statement statement_2) {
        return statement_1.getSubject().equals(statement_2.getSubject())
                && statement_1.getPredicate().equals(statement_2.getPredicate())
                && statement_1.getObject().equals(statement_2.getObject());
    };

    /**
     * Check if a particular statment is in a list of Statements, statement are
     * compared without contexts.
     * 
     * @param compreStatement Statement to compare.
     * @param statements      List of statements.
     * @return True if the statement without context is equal with at least one
     *         statements without context in list. False else.
     */
    private boolean containSameStatementWithoutContext(Statement compreStatement, Iterable<Edge> statements) {
        for (Statement statement : statements) {
            if (this.compareStatementWithoutContext(compreStatement, statement)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Edge> getEdgeList(Node subject, Node predicate, Node object, List<Node> graphs) {
        // Convert Corese node to RDF4J Value
        IRI rdf4j_subject = (IRI) Convert.coreseNodeToRdf4jValue(subject);
        IRI rdf4j_predicate = (IRI) Convert.coreseNodeToRdf4jValue(predicate);
        IRI rdf4j_object = (IRI) Convert.coreseNodeToRdf4jValue(object);

        // Convert list of corese graphs to list of RDF4J contexts
        Resource[] rdf4j_contexts;
        if (graphs == null) {
            rdf4j_contexts = Convert.coreseGraphsToRdf4jContexts(this.default_graph, (Node) null);
        } else {
            Node[] graphs_java_list = new Node[graphs.size()];
            graphs_java_list = graphs.toArray(graphs_java_list);
            rdf4j_contexts = Convert.coreseGraphsToRdf4jContexts(this.default_graph, graphs_java_list);
        }

        Iterable<Statement> statements = this.rdf4j_model.getStatements(rdf4j_subject, rdf4j_predicate, rdf4j_object,
                rdf4j_contexts);

        // convert Statements to Edges
        // remove duplicate edges (same edge with different context)
        ArrayList<Edge> result = new ArrayList<>();
        for (Edge statement : Convert.statementsToEdges(this.default_graph, statements)) {
            if (!this.containSameStatementWithoutContext(statement, result)) {
                result.add(statement);
            }
        }

        return result;
    }

    /************
     * Get list *
     ************/

    @Override
    public Iterable<Node> getGraphList() {
        Set<Resource> contexts = this.rdf4j_model.contexts();
        Node[] graphs = Convert.rdf4jContextsToCoreseGraphs(this.default_graph,
                contexts.toArray(new Resource[contexts.size()]));
        return Arrays.asList(graphs);
    }

    @Override
    public Iterable<Node> getPropertyList() {
        ArrayList<Value> predicates = new ArrayList<Value>(this.rdf4j_model.predicates());
        return Convert.rdf4jvaluestoCoresNodes(predicates);
    }

    @Override
    public Iterable<Node> getDefaultNodeList() {
        return this.getGraphNodeList(this.default_graph);
    }

    @Override
    public Iterable<Node> getGraphNodeList(Node graph) {

        // convert graph to context
        Resource context = Convert.coreseGraphToRdf4jContext(this.default_graph, graph);
        if (context != null && !context.isResource()) {
            return new ArrayList<>();
        }

        // filter model on context
        Model context_model = this.rdf4j_model.filter(null, null, null, (Resource) context);

        // build and convert result graph
        ArrayList<Value> sub_obj_values = new ArrayList<>();
        sub_obj_values.addAll(context_model.subjects());
        sub_obj_values.addAll(context_model.objects());

        return Convert.rdf4jvaluestoCoresNodes(sub_obj_values);
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
                result.add(Convert.statementToEdge(this.default_graph, statement));
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
            rdf4j_contexts = Convert.coreseGraphsToRdf4jContexts(this.default_graph, graphs_java_list);
        }

        // reconstitution of the statement with context
        ValueFactory vf = SimpleValueFactory.getInstance();
        ArrayList<Edge> result = new ArrayList<>();
        for (Resource context : rdf4j_contexts) {
            Statement statement = vf.createStatement(edge.getSubject(), edge.getPredicate(), edge.getObject(), context);

            // remove from model
            if (this.rdf4j_model.remove(statement)) {
                result.add(Convert.statementToEdge(this.default_graph, statement));
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
