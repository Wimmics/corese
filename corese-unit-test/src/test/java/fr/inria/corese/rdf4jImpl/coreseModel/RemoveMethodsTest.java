package fr.inria.corese.rdf4jImpl.coreseModel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Test;

import fr.inria.corese.rdf4j.CoreseGraphModel;

public class RemoveMethodsTest {

    private IRI edithPiafNode;
    private IRI isaProperty;
    private IRI singerNode;
    private IRI firstNameProperty;
    private Literal edithLiteral;
    private IRI context1;
    private IRI context2;
    private IRI context3;

    private CoreseGraphModel buildCoreseModel() {
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
        CoreseGraphModel model = new CoreseGraphModel();
        model.add(edithPiafNode, isaProperty, singerNode);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context3);
        model.getCoreseGraph().init();

        return model;
    }

    @Test
    public void removeSPO() {
        CoreseGraphModel model = this.buildCoreseModel();

        assertEquals(true, model.contains(null, null, null));
        assertEquals(true, model.remove(null, null, null));
        assertEquals(false, model.remove(null, null, null));
        assertEquals(false, model.contains(null, null, null));
        assertEquals(true, model.isEmpty());

        model = this.buildCoreseModel();
        assertEquals(true, model.contains(edithPiafNode, isaProperty, singerNode));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(true, model.remove(null, null, null, (Resource) null));
        assertEquals(false, model.contains(edithPiafNode, isaProperty, singerNode));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));

        model = this.buildCoreseModel();
        assertEquals(true, model.contains(edithPiafNode, isaProperty, singerNode));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(true, model.remove(null, firstNameProperty, null));
        assertEquals(false, model.remove(null, firstNameProperty, null));
        assertEquals(true, model.contains(edithPiafNode, isaProperty, singerNode));
        assertEquals(false, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));

        model = this.buildCoreseModel();
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context2));
        assertEquals(true, model.remove(null, null, null, context1, context2));
        assertEquals(false, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(false, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context2));

        model = this.buildCoreseModel();
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context2));
        assertEquals(true, model.remove(null, null, null, context2, context1));
        assertEquals(false, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(false, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context2));

    }

    @Test
    public void removeStatement() {
        CoreseGraphModel model = this.buildCoreseModel();

        ValueFactory vf = SimpleValueFactory.getInstance();
        Statement statement = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);

        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context2));
        assertEquals(true, model.remove(statement));
        assertEquals(false, model.remove(statement));
        assertEquals(false, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context2));
    }

    @Test
    public void removeAllStatement() {
        CoreseGraphModel model = this.buildCoreseModel();

        ValueFactory vf = SimpleValueFactory.getInstance();

        Statement statement_0 = vf.createStatement(edithPiafNode, isaProperty, singerNode);
        Statement statement_1 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context1);
        Statement statement_2 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);
        Statement statement_3 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context3);

        ArrayList<Statement> statements = new ArrayList<>();
        statements.add(statement_0);
        statements.add(statement_1);
        statements.add(statement_2);
        statements.add(statement_3);

        assertEquals(true, model.containsAll(statements));
        assertEquals(true, model.removeAll(statements));
        assertEquals(false, model.containsAll(statements));
        assertEquals(true, model.isEmpty());
    }

}
