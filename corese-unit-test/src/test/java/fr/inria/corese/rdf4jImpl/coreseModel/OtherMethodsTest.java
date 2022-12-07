package fr.inria.corese.rdf4jImpl.coreseModel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.rdf4j.CoreseGraphModel;

public class OtherMethodsTest {

    private CoreseGraphModel model;
    private Statement statement_0;
    private Statement statement_1;
    private Statement statement_2;
    private Statement statement_3;
    private IRI isaProperty;
    private IRI firstNameProperty;
    private IRI edithPiafNode;
    private IRI context2;

    @Before
    public void buildCoreseModel() {
        // build an array with graph statement
        ValueFactory vf = SimpleValueFactory.getInstance();

        String ex = "http://example.org/";

        // statement zero
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        this.edithPiafNode = edithPiafNode;
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        this.isaProperty = isaProperty;
        IRI singerNode = Values.iri(ex, "Singer");
        this.statement_0 = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        // first statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        this.firstNameProperty = firstNameProperty;
        Literal edithLiteral = Values.literal("Edith");
        IRI context1 = Values.iri(ex, "context1");
        this.statement_1 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context1);

        // second statement
        IRI context2 = Values.iri(ex, "context2");
        this.context2 = context2;
        this.statement_2 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);

        IRI context3 = Values.iri(ex, "context3");
        this.statement_3 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context3);

        /////////////////
        // Build graph //
        /////////////////
        this.model = new CoreseGraphModel();
        this.model.add(edithPiafNode, isaProperty, singerNode);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context3);
        this.model.getCoreseGraph().init();
    }

    @Test
    public void isEmpty() {
        assertEquals(true, new CoreseGraphModel().isEmpty());
        assertEquals(false, this.model.isEmpty());
    }

    @Test
    public void size() {
        assertEquals(4, this.model.size());
        this.model.clear();
        assertEquals(0, this.model.size());
    }

    @Test
    public void iterator() {
        ArrayList<Statement> graph_statements = new ArrayList<>();
        graph_statements.add(this.statement_0);
        graph_statements.add(this.statement_1);
        graph_statements.add(this.statement_2);
        graph_statements.add(this.statement_3);

        // build an array with iterator result
        Iterator<Statement> iter = this.model.iterator();

        ArrayList<Statement> iterator_statements = new ArrayList<>();
        while (iter.hasNext()) {
            iterator_statements.add(iter.next());
        }

        // Tests
        assertEquals(true, iterator_statements.containsAll(graph_statements));
        assertEquals(true, graph_statements.containsAll(iterator_statements));
    }

    @Test
    public void removeFromIterator() {
        Iterator<Statement> iter = this.model.iterator();
        assertEquals(true, this.model.contains(this.statement_0));
        while (iter.hasNext()) {
            Statement last = iter.next();
            if (last.equals(this.statement_0)) {
                iter.remove();
            }
        }
        assertEquals(false, this.model.contains(this.statement_0));
    }

    @Test
    public void getStatements() {
        ArrayList<Statement> graph_statements = new ArrayList<>();
        graph_statements.add(this.statement_0);
        graph_statements.add(this.statement_1);
        graph_statements.add(this.statement_2);
        graph_statements.add(this.statement_3);

        // build an array with iterator result
        Iterable<Statement> iterable_statements = this.model.getStatements(null, null, null);

        List<Statement> result_statements = new ArrayList<>();
        iterable_statements.forEach(result_statements::add);

        // Tests
        assertEquals(true, result_statements.containsAll(graph_statements));
        assertEquals(true, graph_statements.containsAll(result_statements));
    }

    @Test
    public void getStatementsRemove() {
        assertEquals(true, this.model.contains(this.statement_0));

        // get statements
        Iterable<Statement> statements = this.model.getStatements(null, this.isaProperty, null);

        // test result of getStatement
        for (Statement statement : statements) {
            assertEquals(this.statement_0, statement);
        }

        // remove from getStatement iterator
        Iterator<Statement> iter = statements.iterator();
        iter.next();
        iter.remove();

        // test
        assertEquals(false, this.model.contains(this.statement_0));
    }

    @Test
    public void removeTermsIteration() {
        assertEquals(true, this.model.contains(this.statement_0));
        assertEquals(true, this.model.contains(this.statement_1));
        assertEquals(true, this.model.contains(this.statement_2));
        assertEquals(true, this.model.contains(this.statement_3));

        this.model.removeTermIteration(this.model.iterator(), null, this.firstNameProperty, null);
        assertEquals(true, this.model.contains(this.statement_0));
        assertEquals(false, this.model.contains(this.statement_1));
        assertEquals(false, this.model.contains(this.statement_2));
        assertEquals(false, this.model.contains(this.statement_3));
    }

    @Test
    public void filter() {
        assertEquals(true, this.model.contains(this.statement_0));
        assertEquals(true, this.model.contains(this.statement_1));
        assertEquals(true, this.model.contains(this.statement_2));
        assertEquals(true, this.model.contains(this.statement_3));

        Model filterModel = this.model.filter(this.edithPiafNode, null, null, this.context2, (Resource) null);
        assertEquals(true, filterModel.contains(this.statement_0));
        assertEquals(false, filterModel.contains(this.statement_1));
        assertEquals(true, filterModel.contains(this.statement_2));
        assertEquals(false, filterModel.contains(this.statement_3));

        model.remove(this.statement_2);
        filterModel.remove(this.statement_0);

        assertEquals(true, filterModel.isEmpty());

        assertEquals(false, this.model.contains(this.statement_0));
        assertEquals(true, this.model.contains(this.statement_1));
        assertEquals(false, this.model.contains(this.statement_2));
        assertEquals(true, this.model.contains(this.statement_3));
    }
}
