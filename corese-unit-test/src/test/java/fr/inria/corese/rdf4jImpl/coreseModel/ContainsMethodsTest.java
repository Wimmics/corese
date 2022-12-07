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

public class ContainsMethodsTest {

    @Test
    public void containsSPO() {
        //////////////////////
        // Build statements //
        //////////////////////
        String ex = "http://example.org/";

        // first statement
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");

        // second statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        Literal edithLiteral = Values.literal("Edith");
        IRI context1 = Values.iri(ex, "context1");

        // third statement
        IRI context2 = Values.iri(ex, "context2");

        /////////////////
        // Build graph //
        /////////////////
        CoreseGraphModel model = new CoreseGraphModel();
        model.add(edithPiafNode, isaProperty, singerNode);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        model.getCoreseGraph().init();

        ////////////////
        // Test graph //
        ////////////////
        assertEquals(true, model.contains(null, null, null));
        assertEquals(true, model.contains(null, null, null, (Resource) null));
        assertEquals(true, model.contains(null, null, null, null, null));
        assertEquals(true, model.contains(null, null, null, null, null, null));

        assertEquals(true, model.contains(edithPiafNode, null, null));
        assertEquals(false, model.contains(singerNode, null, null));

        assertEquals(true, model.contains(null, isaProperty, null));
        assertEquals(true, model.contains(null, firstNameProperty, null));
        assertEquals(false, model.contains(null, singerNode, null));

        assertEquals(true, model.contains(null, null, singerNode));
        assertEquals(true, model.contains(null, null, edithLiteral));
        assertEquals(false, model.contains(null, null, edithPiafNode));

        assertEquals(true, model.contains(edithPiafNode, isaProperty, null));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, null));
        assertEquals(false, model.contains(edithPiafNode, edithPiafNode, null));

        assertEquals(false, model.contains(null, firstNameProperty, singerNode));
        assertEquals(true, model.contains(null, firstNameProperty, edithLiteral));
        assertEquals(true, model.contains(null, isaProperty, singerNode));
        assertEquals(false, model.contains(null, isaProperty, edithLiteral));

        assertEquals(true, model.contains(edithPiafNode, null, singerNode));
        assertEquals(true, model.contains(edithPiafNode, null, edithLiteral));
        assertEquals(false, model.contains(edithPiafNode, null, edithPiafNode));

        assertEquals(true, model.contains(edithPiafNode, isaProperty, singerNode));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral));
        assertEquals(false, model.contains(edithPiafNode, isaProperty, edithLiteral));

        assertEquals(false, model.contains(edithPiafNode, isaProperty, singerNode, context1));
        assertEquals(true, model.contains(edithPiafNode, firstNameProperty, edithLiteral, context1));
        assertEquals(true, model.contains(null, null, null, context1));
        assertEquals(true, model.contains(null, null, null, context1, context2));
        assertEquals(true, model.contains(null, null, null, context2, context1));

        assertEquals(false, model.contains(edithPiafNode, isaProperty, singerNode, context1));
        /////////////////////////
        // Build another graph //
        /////////////////////////
        model = new CoreseGraphModel();
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);

        ////////////////
        // Test graph //
        ////////////////
        assertEquals(true, model.contains(null, null, null));
        assertEquals(false, model.contains(null, null, null, (Resource) null));
        assertEquals(false, model.contains(null, null, null, null, null));
        assertEquals(false, model.contains(null, null, null, null, null, null));
    }

    @Test
    public void containsStatement() {

        //////////////////////
        // Build statements //
        //////////////////////
        ValueFactory vf = SimpleValueFactory.getInstance();
        String ex = "http://example.org/";

        // first statement
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");
        Statement f_statement = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        // second statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        Literal edithLiteral = Values.literal("Edith");
        IRI context1 = Values.iri(ex, "context1");
        Statement s_statement = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context1);

        // third statement
        IRI context2 = Values.iri(ex, "context2");
        Statement t_statement = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);

        // fake statement
        Statement fake_statement = vf.createStatement(edithPiafNode, isaProperty, edithLiteral, context2);

        /////////////////
        // Build graph //
        /////////////////
        CoreseGraphModel model = new CoreseGraphModel();
        model.add(f_statement);
        model.add(s_statement);
        model.add(t_statement);

        ////////////////
        // Test graph //
        ////////////////
        assertEquals(true, model.contains(f_statement));
        assertEquals(true, model.contains(s_statement));
        assertEquals(true, model.contains(t_statement));
        assertEquals(false, model.contains(fake_statement));
    }

    @Test
    public void containsAllStatement() {
        //////////////////////
        // Build statements //
        //////////////////////
        ValueFactory vf = SimpleValueFactory.getInstance();
        String ex = "http://example.org/";

        // first statement
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");
        Statement f_statement = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        // second statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        Literal edithLiteral = Values.literal("Edith");
        IRI context1 = Values.iri(ex, "context1");
        Statement s_statement = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context1);

        // third statement
        IRI context2 = Values.iri(ex, "context2");
        Statement t_statement = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral, context2);

        // fake statement
        Statement fake_statement = vf.createStatement(edithPiafNode, isaProperty, edithLiteral, context2);

        /////////////////
        // Build graph //
        /////////////////
        CoreseGraphModel model = new CoreseGraphModel();
        ArrayList<Statement> statements = new ArrayList<Statement>();
        statements.add(f_statement);
        statements.add(s_statement);
        statements.add(t_statement);
        model.addAll(statements);

        ////////////////
        // Test graph //
        ////////////////
        assertEquals(true, model.containsAll(statements));
        statements.add(fake_statement);
        assertEquals(false, model.containsAll(statements));
    }

}
