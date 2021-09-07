package fr.inria.corese.rdf4jimpl.CoreseModelTest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Test;

import fr.inria.corese.rdf4j.ModelApiImpl.CoreseModel;

public class OtherMethodsTest {

    private IRI edithPiafNode;
    private IRI isaProperty;
    private IRI singerNode;
    private IRI firstNameProperty;
    private Literal edithLiteral;
    private IRI context1;
    private IRI context2;
    private IRI context3;

    private CoreseModel buildCoreseModel() {
        String ex = "http://example.org/";

        // first statement
        this.edithPiafNode = Values.iri(ex, "EdithPiaf");
        this.isaProperty = Values.iri(RDF.TYPE.stringValue());
        this.singerNode = Values.iri(ex, "Singer");

        // second statement
        this.firstNameProperty = Values.iri(ex, "firstName");
        this.edithLiteral = Values.literal("Edith");
        this.context1 = Values.iri(ex, "context1");

        // third statement
        this.context2 = Values.iri(ex, "context2");

        this.context3 = Values.iri(ex, "context3");
        /////////////////
        // Build graph //
        /////////////////
        CoreseModel model = new CoreseModel();
        model.add(edithPiafNode, isaProperty, singerNode);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context3);
        model.getCoreseGraph().init();

        return model;
    }

    @Test
    public void isEmpty() {
        CoreseModel model = new CoreseModel();
        assertEquals(true, model.isEmpty());
        model = this.buildCoreseModel();
        assertEquals(false, model.isEmpty());
    }

    @Test
    public void experimentation() {
    }

    @Test
    public void size() {
        CoreseModel model = this.buildCoreseModel();
        assertEquals(4, model.size());
        model.clear();
        assertEquals(0, model.size());
    }

    @Test
    public void iterator() {
        CoreseModel model = this.buildCoreseModel();

        // buil an array with graph statement
        ValueFactory vf = SimpleValueFactory.getInstance();

        Statement statement_0 = vf.createStatement(edithPiafNode, isaProperty, singerNode);
        Statement statement_1 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context1);
        Statement statement_2 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);
        Statement statement_3 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context3);

        ArrayList<Statement> graph_statements = new ArrayList<>();
        graph_statements.add(statement_0);
        graph_statements.add(statement_1);
        graph_statements.add(statement_2);
        graph_statements.add(statement_3);

        // build an array with iterator result
        Iterator<Statement> iter = model.iterator();

        ArrayList<Statement> iterator_statements = new ArrayList<>();
        while (iter.hasNext()) {
            iterator_statements.add(iter.next());
        }

        // Tests
        assertEquals(true, iterator_statements.containsAll(graph_statements));
        assertEquals(true, graph_statements.containsAll(iterator_statements));
    }
}
