package fr.inria.corese.jenaImpl.combination;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.corese.jenaImpl.combination.engine.Combination;
import fr.inria.corese.jenaImpl.combination.engine.LoadableFile;
import fr.inria.corese.jenaImpl.combination.engine.RdfFormat;

public class CombinationTestEdithGraph {

    private LoadableFile loadable_file_1 = new LoadableFile("edithPiaf/isa.ttl", RdfFormat.TURTLE);
    private LoadableFile loadable_file_2 = new LoadableFile("edithPiaf/firstName.ttl", RdfFormat.TURTLE,
            "http://example.org/Context1", "http://example.org/Context2", "http://example.org/Context3");

    @Test
    public void spo() {
        String query = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spog() {
        String query = "prefix ex: <http://example.org/> select * where { graph ?g {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogcn1() {
        String query = "prefix ex: <http://example.org/> select * from named ex:Context1 where { graph ?g {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogcn1cn2() {
        String query = "prefix ex: <http://example.org/> select * from named ex:Context1 from named ex:Context2 where { graph ?g {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogcndcn2() {
        String query = "prefix ex: <http://example.org/> select * from named kg:default from named ex:Context2 where { graph ?g {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogc1() {
        String query = "prefix ex: <http://example.org/> select * where { graph ex:Context1 {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogcd() {
        String query = "prefix ex: <http://example.org/> select * where { graph kg:default {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogc1c2() {
        String query = "prefix ex: <http://example.org/> select * from ex:Context1 from ex:Context2 where { graph ?g {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spogfc1fnc2() {
        String query = "prefix ex: <http://example.org/> select * from ex:Context1 from named ex:Context2 where { graph ?g {?s ?p ?o} }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

    @Test
    public void spofc1fnc2() {
        String query = "prefix ex: <http://example.org/> select * from ex:Context1 from named ex:Context2 where { ?s ?p ?o }";
        assertEquals(true, Combination.selectQuery(query, this.loadable_file_1, this.loadable_file_2));
    }

}
