package fr.inria.corese.rdf4jImpl.dataManager;

import java.io.IOException;

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
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.rdf4j.CoreseModel;
import fr.inria.corese.rdf4j.Rdf4jDataManager;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;

public class Rdf4jDataManagerTestSparql {

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
    public void selectWhereSpo() throws EngineException, IOException {
        DataManager dataManager = new Rdf4jDataManager(this.model);

        // Sparql query
        QueryProcess exec = QueryProcess.create(dataManager);
        Mappings map = exec.query("select * where { ?s ?p ?o }");

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void selectWhereSpoTemoin() throws EngineException, IOException {
        // Sparql query
        QueryProcess exec = QueryProcess.create(new CoreseModel(this.model).getCoreseGraph());
        Mappings map = exec.query("select * where { ?s ?p ?o }");

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void insertData() throws EngineException {
        CoreseModel corese_model = new CoreseModel(this.model);
        DataManager dataManager = new Rdf4jDataManager(corese_model);

        // Sparql query
        QueryProcess exec = QueryProcess.create(dataManager);
        exec.query("PREFIX dc: <http://purl.org/dc/elements/1.1/>" + "INSERT DATA" + "{ "
                + "<http://example/book1> dc:title \"A new book\" ;" + "dc:creator \"A.N.Other\" ." + "}");

        // Print result
        System.out.println(corese_model.toString());
    }

    @Test
    public void insertDataTemoin() throws EngineException {
        // Sparql query
        CoreseModel corese_model = new CoreseModel(this.model);
        QueryProcess exec = QueryProcess.create(corese_model.getCoreseGraph());
        exec.query("PREFIX dc: <http://purl.org/dc/elements/1.1/>" + "INSERT DATA" + "{ "
                + "<http://example/book1> dc:title \"A new book\" ;" + "dc:creator \"A.N.Other\" ." + "}");

        // Print result
        System.out.println(corese_model);
    }
}
