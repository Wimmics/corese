package fr.inria.corese.rdf4jImpl.dataManager;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.rdf4j.Convert;
import fr.inria.corese.rdf4j.CoreseModel;
import fr.inria.corese.rdf4j.Rdf4jDataManager;
import fr.inria.corese.sparql.datatype.DatatypeMap;

public class Rdf4jDataManagerTest {

    private Model model;
    private Statement statement_0;
    private Statement statement_1;
    private Statement statement_2;
    private Statement statement_bonus;
    private IRI isaProperty;
    private IRI firstNameProperty;
    private IRI singerNode;
    private IRI edithPiafNode;
    private Literal edithLiteral;
    private IRI context1;
    private IRI context2;
    private IRI context3;
    private Node default_graph;

    @Before
    public void init() {

        this.default_graph = NodeImpl.create(DatatypeMap.createResource(ExpType.DEFAULT_GRAPH));

        // build an array with graph statement
        ValueFactory vf = SimpleValueFactory.getInstance();

        String ex = "http://example.org/";

        // statement zero
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        this.edithPiafNode = edithPiafNode;
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        this.isaProperty = isaProperty;
        IRI singerNode = Values.iri(ex, "Singer");
        this.singerNode = singerNode;
        this.statement_0 = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        // first statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        this.firstNameProperty = firstNameProperty;
        Literal edithLiteral = Values.literal("Édith");
        this.edithLiteral = edithLiteral;
        IRI context1 = Values.iri(ex, "context1");
        this.context1 = context1;
        this.statement_1 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context1);

        // second statement
        IRI context2 = Values.iri(ex, "context2");
        this.context2 = context2;
        this.statement_2 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);

        // third statement
        IRI context3 = Values.iri(ex, "context3");
        this.context3 = context3;

        // bonus statement
        IRI lastNameProperty = Values.iri(ex, "lastName");
        Literal piafLiteral = Values.literal("Piaf");
        this.statement_bonus = vf.createStatement(edithPiafNode, lastNameProperty, piafLiteral);

        /////////////////
        // Build graph //
        /////////////////
        this.model = new TreeModel();
        this.model.add(edithPiafNode, isaProperty, singerNode);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context3);
    }

    @Test
    public void graphSize() {
        DataManager dataManager;

        dataManager = new Rdf4jDataManager(new CoreseModel());
        assertEquals(0, dataManager.graphSize());

        dataManager = new Rdf4jDataManager(this.model);
        assertEquals(4, dataManager.graphSize());

        dataManager = new Rdf4jDataManager(this.model.filter(null, this.isaProperty, null));
        assertEquals(1, dataManager.graphSize());
    }

    @Test
    public void graphSizeNode() {
        DataManager dataManager;
        Node fnp_corese_node = Convert.rdf4jValueToCoreseNode(this.firstNameProperty);
        Node ip_corese_node = Convert.rdf4jValueToCoreseNode(this.isaProperty);

        dataManager = new Rdf4jDataManager(new CoreseModel());
        assertEquals(0, dataManager.graphSize(fnp_corese_node));

        dataManager = new Rdf4jDataManager(this.model);
        assertEquals(3, dataManager.graphSize(fnp_corese_node));

        dataManager = new Rdf4jDataManager(this.model);
        assertEquals(1, dataManager.graphSize(ip_corese_node));
    }

    @Test
    public void getEdgeList() {
        DataManager dataManager;
        dataManager = new Rdf4jDataManager(this.model);

        // All edges
        Iterable<Edge> iterable = dataManager.getEdgeList(null, null, null, null);
        List<Edge> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(2, result.size());
        assertEquals(true, result.contains(this.statement_0));
        assertEquals(false, result.contains(this.statement_0) && result.contains(this.statement_1)
                && result.contains(this.statement_2));
        assertEquals(true, result.contains(this.statement_0) || result.contains(this.statement_1)
                || result.contains(this.statement_2));

        // All edges of default graph
        ArrayList<Node> graphs = new ArrayList<>();
        graphs.add(this.default_graph);
        iterable = dataManager.getEdgeList(null, null, null, graphs);
        result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(1, result.size());
        assertEquals(true, result.contains(this.statement_0));
        assertEquals(false, result.contains(this.statement_1));
    }

    @Test
    public void getGraphList() {
        DataManager dataManager;

        dataManager = new Rdf4jDataManager(this.model);
        Iterable<Node> iterable = dataManager.getGraphList();
        List<Node> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(4, result.size());
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.context1)));
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.context2)));
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.context3)));
        assertEquals(true, result.contains(this.default_graph));
    }

    @Test
    public void getPropertyList() {
        DataManager dataManager;

        dataManager = new Rdf4jDataManager(this.model);
        Iterable<Node> iterable = dataManager.getPropertyList();
        List<Node> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(2, result.size());
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.firstNameProperty)));
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.isaProperty)));
    }

    @Test
    public void getDefaultNodeList() {
        DataManager dataManager;

        dataManager = new Rdf4jDataManager(this.model);
        Iterable<Node> iterable = dataManager.getDefaultNodeList();
        List<Node> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(2, result.size());
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.edithPiafNode)));
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.singerNode)));
    }

    @Test
    public void getGraphNodeList() {
        DataManager dataManager;

        dataManager = new Rdf4jDataManager(this.model);
        Iterable<Node> iterable = dataManager.getGraphNodeList(Convert.rdf4jValueToCoreseNode(this.context1));
        List<Node> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(2, result.size());
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.edithPiafNode)));
        assertEquals(true, result.contains(Convert.rdf4jValueToCoreseNode(this.edithLiteral)));
    }

    @Test
    public void insert() {
        DataManager dataManager = new Rdf4jDataManager(this.model);

        // Insert new statement
        assertEquals(false, this.model.contains(this.statement_bonus));
        Edge edge_bonus = Convert.statementToEdge(this.default_graph, this.statement_bonus);
        Edge iterable = dataManager.insert(edge_bonus);
        assertEquals(iterable, edge_bonus);
        assertEquals(true, this.model.contains(this.statement_bonus));

        // Try insert statement already in graph
        iterable = dataManager.insert(edge_bonus);
        assertEquals(iterable, null);

        System.out.println(this.model);
    }

    @Test
    public void delete() {
        DataManager dataManager;
        Model modelCopy;

        modelCopy = new TreeModel(this.model);
        dataManager = new Rdf4jDataManager(modelCopy);
        Edge edge_0 = Convert.statementToEdge(this.default_graph, this.statement_0);
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        dataManager.delete(edge_0);
        assertEquals(false, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));

        modelCopy = new TreeModel(this.model);
        dataManager = new Rdf4jDataManager(modelCopy);
        Edge edge_1 = Convert.statementToEdge(this.default_graph, this.statement_1);
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(this.statement_2));
        dataManager.delete(edge_1);
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(false, modelCopy.contains(this.statement_1));
        assertEquals(false, modelCopy.contains(this.statement_1));
    }

    @Test
    public void deleteFromGraph() {
        DataManager dataManager;
        Model modelCopy;

        // Test 1
        modelCopy = new TreeModel(this.model);
        dataManager = new Rdf4jDataManager(modelCopy);

        Edge edge_1 = Convert.statementToEdge(this.default_graph, this.statement_1);

        List<Node> graphs = new ArrayList<>();
        graphs.add(Convert.rdf4jContextToCoreseGraph(this.default_graph, this.context1));
        graphs.add(Convert.rdf4jContextToCoreseGraph(this.default_graph, this.context2));

        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(this.statement_2));
        List<Edge> removed = dataManager.delete(edge_1, graphs);
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(false, removed.contains(this.statement_0));
        assertEquals(false, modelCopy.contains(this.statement_1));
        assertEquals(true, removed.contains(this.statement_1));
        assertEquals(false, modelCopy.contains(this.statement_2));
        assertEquals(true, removed.contains(this.statement_2));

        // Test 2
        modelCopy = new TreeModel(this.model);
        dataManager = new Rdf4jDataManager(modelCopy);

        Edge edge_0 = Convert.statementToEdge(this.default_graph, this.statement_0);

        graphs = new ArrayList<>();
        graphs.add(Convert.rdf4jContextToCoreseGraph(this.default_graph, this.context1));
        graphs.add(Convert.rdf4jContextToCoreseGraph(this.default_graph, this.context2));

        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(this.statement_2));
        removed = dataManager.delete(edge_0, graphs);
        assertEquals(true, removed.isEmpty());
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(this.statement_2));
    }

    @Test
    public void clear() {
        DataManager dataManager;
        Model modelCopy;
        modelCopy = new TreeModel(this.model);
        dataManager = new Rdf4jDataManager(modelCopy);

        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        dataManager.clear(this.context1.stringValue(), false);
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(false, modelCopy.contains(this.statement_1));
    }

    @Test
    public void clearNamed() {
        DataManager dataManager;
        Model modelCopy;
        modelCopy = new TreeModel(this.model);
        dataManager = new Rdf4jDataManager(modelCopy);

        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(this.statement_2));
        dataManager.clearNamed();
        assertEquals(true, modelCopy.contains(this.statement_0));
        assertEquals(false, modelCopy.contains(this.statement_1));
        assertEquals(false, modelCopy.contains(this.statement_2));
    }

    @Test
    public void addContext() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model modelCopy = new TreeModel(this.model);
        DataManager dataManager = new Rdf4jDataManager(modelCopy);

        // build and add a new statement in context_1
        Statement theoretical_old_statement = vf.createStatement(this.statement_bonus.getSubject(),
                this.statement_bonus.getPredicate(), this.statement_bonus.getObject(), this.statement_1.getContext());
        modelCopy.add(theoretical_old_statement);

        // build theorical result
        Statement theoretical_new_statement = vf.createStatement(this.statement_bonus.getSubject(),
                this.statement_bonus.getPredicate(), this.statement_bonus.getObject(), this.statement_2.getContext());

        // tests
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(false, modelCopy.contains(theoretical_new_statement));
        dataManager.add(this.context1.stringValue(), this.context2.stringValue(), false);
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(true, modelCopy.contains(theoretical_new_statement));
    }

    @Test
    public void moveContext() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model modelCopy = new TreeModel(this.model);
        DataManager dataManager = new Rdf4jDataManager(modelCopy);

        // build and add a new statement in context_1
        Statement theoretical_old_statement = vf.createStatement(this.statement_bonus.getSubject(),
                this.statement_bonus.getPredicate(), this.statement_bonus.getObject(), this.statement_1.getContext());
        modelCopy.add(theoretical_old_statement);
        modelCopy.remove(statement_2);

        // build theorical result
        Statement theoretical_new_statement = vf.createStatement(this.statement_bonus.getSubject(),
                this.statement_bonus.getPredicate(), this.statement_bonus.getObject(), this.statement_2.getContext());
        // statement_2 is the second theorical result

        // tests
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(false, modelCopy.contains(this.statement_2));
        assertEquals(false, modelCopy.contains(theoretical_new_statement));
        dataManager.move(this.context1.stringValue(), this.context2.stringValue(), false);
        assertEquals(false, modelCopy.contains(this.statement_1));
        assertEquals(false, modelCopy.contains(theoretical_old_statement));
        assertEquals(true, modelCopy.contains(this.statement_2));
        assertEquals(true, modelCopy.contains(theoretical_new_statement));
    }

    @Test
    public void copyContext() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model modelCopy = new TreeModel(this.model);
        DataManager dataManager = new Rdf4jDataManager(modelCopy);

        // build and add a new statement in context_1
        Statement theoretical_old_statement = vf.createStatement(this.statement_bonus.getSubject(),
                this.statement_bonus.getPredicate(), this.statement_bonus.getObject(), this.statement_1.getContext());
        modelCopy.add(theoretical_old_statement);
        modelCopy.remove(statement_2);

        // build theorical result
        Statement theoretical_new_statement = vf.createStatement(this.statement_bonus.getSubject(),
                this.statement_bonus.getPredicate(), this.statement_bonus.getObject(), this.statement_2.getContext());
        // statement_2 is the second theorical result

        // tests
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(false, modelCopy.contains(this.statement_2));
        assertEquals(false, modelCopy.contains(theoretical_new_statement));
        dataManager.copy(this.context1.stringValue(), this.context2.stringValue(), false);
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(true, modelCopy.contains(this.statement_2));
        assertEquals(true, modelCopy.contains(theoretical_new_statement));
    }
}
