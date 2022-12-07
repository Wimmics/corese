package fr.inria.corese.rdf4jImpl.combination;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.corese.rdf4jImpl.combination.engine.Combination;
import fr.inria.corese.rdf4jImpl.combination.engine.LoadableFile;
import fr.inria.corese.rdf4jImpl.combination.engine.RdfFormat;

public class CombinationidatatypeCompare {

    private LoadableFile loadable_file_1 = new LoadableFile("idatatypeCompare.trig", RdfFormat.TRIG);

    @Test
    public void spo() {
        String query = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";
        // String query = "prefix ex: <http://example.org/> select * where { graph ?g {?s ?p ?o}}";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1));
    }

}
