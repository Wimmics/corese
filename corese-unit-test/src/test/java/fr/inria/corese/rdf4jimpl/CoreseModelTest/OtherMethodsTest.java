package fr.inria.corese.rdf4jimpl.CoreseModelTest;

import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Test;

import fr.inria.corese.rdf4j.ModelApiImpl.CoreseModel;

public class OtherMethodsTest {

    @Test
    public void isEmpty() {

        String ex = "http://example.org/";

        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");

        CoreseModel model = new CoreseModel();

        assertEquals(true, model.isEmpty());

        model.add(edithPiafNode, isaProperty, singerNode);

        assertEquals(false, model.isEmpty());
    }
}
