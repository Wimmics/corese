package fr.inria.corese.rdf4jImpl.combination;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.corese.rdf4jImpl.combination.engine.Combination;
import fr.inria.corese.rdf4jImpl.combination.engine.LoadableFile;
import fr.inria.corese.rdf4jImpl.combination.engine.RdfFormat;

public class CombinationTestEdithGraph {

    private LoadableFile loadable_file_1 = new LoadableFile("edithPiaf/isa.ttl", RdfFormat.TURTLE);
    private LoadableFile loadable_file_2 = new LoadableFile("edithPiaf/firstName.ttl", RdfFormat.TURTLE,
            "http://example.org/Context1", "http://example.org/Context2", "http://example.org/Context3");

    @Test
    public void spo() {
        String query = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

}
