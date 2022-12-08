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
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.rdf4j.Rdf4jModelDataManagerBuilder;
import fr.inria.corese.rdf4j.convert.ConvertRdf4jCorese;
import fr.inria.corese.sparql.datatype.DatatypeMap;

public class Rdf4jModelDataManagerTest {

    private Model model;

    private Statement statement_0;
    private Statement statement_1;
    private Statement statement_2;
    private Statement statement_3;
    private Statement statement_bonus;

    private IRI edith_piaf_node;
    private IRI george_brassens_node;

    private IRI isa_property;
    private IRI first_name_property;

    private IRI singer_node;
    private Literal edith_literal;

    private IRI context1;
    private IRI context2;
    private IRI context3;

    private Node default_context;

    Edge edge_0;
    Edge edge_1;
    Edge edge_2;
    Edge edge_3;
    Edge edge_bonus;

    @Before
    public void init() {

        this.default_context = NodeImpl.create(DatatypeMap.createResource(ExpType.DEFAULT_GRAPH));

        ValueFactory vf = SimpleValueFactory.getInstance();

        String ex = "http://example.org/";

        // statement zero
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        this.edith_piaf_node = edithPiafNode;
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        this.isa_property = isaProperty;
        IRI singerNode = Values.iri(ex, "Singer");
        this.singer_node = singerNode;
        this.statement_0 = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        // first statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        this.first_name_property = firstNameProperty;
        Literal edithLiteral = Values.literal("Édith");
        this.edith_literal = edithLiteral;
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
        this.statement_3 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context3);

        // bonus statement
        IRI georgeBrassensNode = Values.iri(ex, "GeorgeBrassens");
        this.george_brassens_node = georgeBrassensNode;
        this.statement_bonus = vf.createStatement(georgeBrassensNode, isaProperty, singerNode);

        /////////////////
        // Build graph //
        /////////////////
        this.model = new TreeModel();
        this.model.add(edithPiafNode, isaProperty, singerNode);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context3);

        // Convert statements to edges
        this.edge_0 = ConvertRdf4jCorese.statementToEdge(this.statement_0);
        this.edge_1 = ConvertRdf4jCorese.statementToEdge(this.statement_1);
        this.edge_2 = ConvertRdf4jCorese.statementToEdge(this.statement_2);
        this.edge_3 = ConvertRdf4jCorese.statementToEdge(this.statement_3);
        this.edge_bonus = ConvertRdf4jCorese.statementToEdge(this.statement_bonus);

    }

    @Test
    public void graphSize() {
        DataManager data_manager;

        data_manager = new Rdf4jModelDataManagerBuilder().build();
        assertEquals(0, data_manager.graphSize());

        data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();
        assertEquals(4, data_manager.graphSize());

        data_manager = new Rdf4jModelDataManagerBuilder().model(this.model.filter(null, this.isa_property, null)).build();
        assertEquals(1, data_manager.graphSize());
    }

    @Test
    public void countEdges() {
        DataManager data_manager;
        Node fnp_corese_node = ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.first_name_property);
        Node ip_corese_node = ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property);

        data_manager = new Rdf4jModelDataManagerBuilder().build();
        assertEquals(0, data_manager.countEdges(fnp_corese_node));

        data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();
        assertEquals(3, data_manager.countEdges(fnp_corese_node));

        data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();
        assertEquals(1, data_manager.countEdges(ip_corese_node));

        data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();
        assertEquals(4, data_manager.countEdges(null));
    }

    @Test
    public void getEdgesAll() {
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();

        // All edges
        Iterable<Edge> iterable = data_manager.getEdges(null, null, null, null);
        List<Edge> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(2, result.size());
        assertEquals(true, result.contains(this.edge_0));
        assertEquals(false, result.contains(this.edge_0) && result.contains(this.edge_1)
                && result.contains(this.edge_2));
        assertEquals(true, result.contains(this.edge_0) || result.contains(this.edge_1)
                || result.contains(this.edge_2));
    }

    @Test
    public void getEdgesDefault() {
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();

        // All edges of default context
        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(this.default_context);

        Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
        List<Edge> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(1, result.size());
        assertEquals(true, result.contains(this.edge_0));
        assertEquals(false, result.contains(this.edge_1));
    }

    @Test
    public void getEdgesIgnoreNull() {
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();

        // All edges (with ignore null values)
        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(null);
        contexts.add(null);
        contexts.add(null);
        Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
        List<Edge> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(2, result.size());
        assertEquals(true, result.contains(this.edge_0));
        assertEquals(false, result.contains(this.edge_0) && result.contains(this.edge_1)
                && result.contains(this.edge_2));
        assertEquals(true, result.contains(this.edge_0) || result.contains(this.edge_1)
                || result.contains(this.edge_2));
    }

    @Test
    public void getEdgesIgnoreNull2() {
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();

        // All edges of context 1 (with a ignore null value)
        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(null);
        contexts.add(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1));
        Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
        List<Edge> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(1, result.size());
        assertEquals(false, result.contains(this.edge_0));
        assertEquals(false, result.contains(this.edge_2));
        assertEquals(true, result.contains(this.edge_1));
    }

    @Test
    public void getNodes() {
        Iterable<Node> iterable;
        List<Node> result;

        Model model_copy = new TreeModel(this.model);
        model_copy.add(this.statement_bonus);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        // All contexts
        iterable = data_manager.getNodes(null);
        result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(4, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.george_brassens_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_literal)));

        // Context 1
        iterable = data_manager.getNodes(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1));
        result = new ArrayList<>();
        iterable.forEach(result::add);
        assertEquals(2, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_literal)));

        // Default context
        iterable = data_manager.getNodes(this.default_context);
        result = new ArrayList<>();
        iterable.forEach(result::add);
        assertEquals(3, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.george_brassens_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node)));

        // No duplication
        model_copy.add(this.george_brassens_node, this.isa_property, this.george_brassens_node);

        iterable = data_manager.getNodes(null);
        result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(4, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.george_brassens_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_literal)));
    }

    @Test
    public void predicates() {
        Iterable<Node> iterable;
        List<Node> result;

        Model model_copy = new TreeModel(this.model);
        model_copy.add(this.statement_bonus);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        // All contexts
        iterable = data_manager.predicates(null);
        result = new ArrayList<>();
        iterable.forEach(result::add);
        assertEquals(2, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.first_name_property)));

        // Context 1
        iterable = data_manager.predicates(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1));
        result = new ArrayList<>();
        iterable.forEach(result::add);
        assertEquals(1, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.first_name_property)));

        // Default context
        iterable = data_manager.predicates(this.default_context);
        result = new ArrayList<>();
        iterable.forEach(result::add);
        assertEquals(1, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property)));
    }

    @Test
    public void contexts() {
        DataManager data_manager;

        data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();
        Iterable<Node> iterable = data_manager.contexts();
        List<Node> result = new ArrayList<>();
        iterable.forEach(result::add);

        assertEquals(4, result.size());
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context1)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context2)));
        assertEquals(true, result.contains(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context3)));
        assertEquals(true, result.contains(this.default_context));
    }

    @Test
    public void insertSPO() {
        Model model = new TreeModel();
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model).build();

        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context1));
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context2));

        data_manager.insert(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node),
                contexts);

        assertEquals(2, model.size());
        assertEquals(false, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, (IRI) null));
        assertEquals(true, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, this.context1));
        assertEquals(true, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, this.context2));
        assertEquals(false, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, this.context3));
    }

    @Test
    public void insertSPODefault() {
        Model model = new TreeModel();
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model).build();

        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(this.default_context);

        Iterable<Edge> results = data_manager.insert(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node),
                contexts);

        assertEquals(1, model.size());
        assertEquals(results.iterator().next().getGraph(), this.default_context);
        assertEquals(true, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, (IRI) null));
        assertEquals(false, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, this.context1));
        assertEquals(false, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, this.context2));
        assertEquals(false, model.contains(this.edith_piaf_node, this.isa_property, this.singer_node, this.context3));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void insertSPOError1() {
        Model model = new TreeModel();
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model).build();

        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context1));
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context2));

        data_manager.insert(null, ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node), contexts);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void insertSPOError2() {
        Model model = new TreeModel();
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model).build();

        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context1));
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context2));
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(null));

        data_manager.insert(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node),
                contexts);
    }

    @Test
    public void insertEdgeEdge() {
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(this.model).build();

        // Insert new statement
        assertEquals(false, this.model.contains(this.statement_bonus));

        Edge iterable = data_manager.insert(edge_bonus);
        assertEquals(iterable, edge_bonus);
        assertEquals(true, this.model.contains(this.statement_bonus));

        // Try insert statement already in graph
        iterable = data_manager.insert(edge_bonus);
        assertEquals(null, iterable);
    }

    @Test
    public void deleteSPO() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(this.default_context);
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context2));

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(4, model_copy.size());

        data_manager.delete(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.edith_piaf_node),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.isa_property),
                ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.singer_node),
                contexts);

        assertEquals(false, model_copy.contains(this.statement_0));
        assertEquals(3, model_copy.size());
    }

    @Test
    public void deleteContext2() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        ArrayList<Node> contexts = new ArrayList<>();
        contexts.add(ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.context2));

        assertEquals(true, model_copy.contains(this.statement_2));
        assertEquals(4, model_copy.size());

        data_manager.delete(null, null, null, contexts);

        assertEquals(false, model_copy.contains(this.statement_2));
        assertEquals(3, model_copy.size());
    }

    @Test
    public void deleteFirstName() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_1));
        assertEquals(true, model_copy.contains(this.statement_2));
        assertEquals(true, model_copy.contains(this.statement_3));
        assertEquals(4, model_copy.size());

        data_manager.delete(null, ConvertRdf4jCorese.rdf4jValueToCoreseNode(this.first_name_property), null, null);

        assertEquals(false, model_copy.contains(this.statement_1));
        assertEquals(false, model_copy.contains(this.statement_2));
        assertEquals(false, model_copy.contains(this.statement_3));
        assertEquals(1, model_copy.size());
    }

    @Test
    public void deleteSPOAll() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(false, model_copy.isEmpty());

        data_manager.delete(null, null, null, null);

        assertEquals(true, model_copy.isEmpty());
    }

    @Test
    public void deleteEdgeStatement0() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));

        data_manager.delete(edge_0);

        assertEquals(false, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));
    }

    @Test
    public void deleteEdgeStatement1() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));
        assertEquals(true, model_copy.contains(this.statement_2));

        data_manager.delete(edge_1);

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(false, model_copy.contains(this.statement_1));
        assertEquals(false, model_copy.contains(this.statement_1));
    }

    @Test
    public void deleteFromContext() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));
        assertEquals(true, model_copy.contains(this.statement_2));
        assertEquals(true, model_copy.contains(this.statement_3));

        List<Node> contexts = new ArrayList<>();
        contexts.add(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1));
        contexts.add(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context2));

        Iterable<Edge> removed = data_manager.delete(edge_1.getSubjectNode(), edge_1.getProperty(),
                edge_1.getObjectNode(), contexts);

        List<Edge> result = new ArrayList<>();
        removed.forEach(result::add);
        assertEquals(false, result.contains(this.edge_0));
        assertEquals(true, result.contains(this.edge_1));
        assertEquals(true, result.contains(this.edge_2));

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(false, model_copy.contains(this.statement_1));
        assertEquals(false, model_copy.contains(this.statement_2));
        assertEquals(true, model_copy.contains(this.statement_3));
    }

    @Test
    public void deleteFromContextNoRemove() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));
        assertEquals(true, model_copy.contains(this.statement_2));
        assertEquals(true, model_copy.contains(this.statement_3));

        List<Node> contexts = new ArrayList<>();
        contexts.add(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1));
        contexts.add(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context2));

        Iterable<Edge> removed = data_manager.delete(edge_0.getSubjectNode(), edge_0.getProperty(),
                edge_0.getObjectNode(), contexts);

        List<Edge> result = new ArrayList<>();
        removed.forEach(result::add);
        assertEquals(true, result.isEmpty());

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));
        assertEquals(true, model_copy.contains(this.statement_2));
        assertEquals(true, model_copy.contains(this.statement_3));
    }

    @Test
    public void clearContext1() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));

        data_manager.clear(List.of(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1)), false);

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(false, model_copy.contains(this.statement_1));
    }

    @Test
    public void clear() {
        Model model_copy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(model_copy).build();

        assertEquals(true, model_copy.contains(this.statement_0));
        assertEquals(true, model_copy.contains(this.statement_1));
        assertEquals(true, model_copy.contains(this.statement_2));
        data_manager.clear();
        assertEquals(true, model_copy.isEmpty());
    }

    @Test
    public void addContext() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model modelCopy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(modelCopy).build();

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
        data_manager.addGraph(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1),
                ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context2), false);
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(true, modelCopy.contains(theoretical_new_statement));
    }

    @Test
    public void moveContext() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model modelCopy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(modelCopy).build();

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
        data_manager.moveGraph(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1),
                ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context2), false);
        assertEquals(false, modelCopy.contains(this.statement_1));
        assertEquals(false, modelCopy.contains(theoretical_old_statement));
        assertEquals(true, modelCopy.contains(this.statement_2));
        assertEquals(true, modelCopy.contains(theoretical_new_statement));
    }

    @Test
    public void copyContext() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model modelCopy = new TreeModel(this.model);
        DataManager data_manager = new Rdf4jModelDataManagerBuilder().model(modelCopy).build();

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
        data_manager.copyGraph(ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context1),
                ConvertRdf4jCorese.rdf4jContextToCoreseContext(this.context2), false);
        assertEquals(true, modelCopy.contains(this.statement_1));
        assertEquals(true, modelCopy.contains(theoretical_old_statement));
        assertEquals(true, modelCopy.contains(this.statement_2));
        assertEquals(true, modelCopy.contains(theoretical_new_statement));
    }
}
