package fr.inria.corese.rdf4jImpl.combination;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.corese.rdf4jImpl.combination.engine.Combination;
import fr.inria.corese.rdf4jImpl.combination.engine.LoadableFile;
import fr.inria.corese.rdf4jImpl.combination.engine.RdfFormat;

public class CombinationTestMusic {

    private LoadableFile beatles = new LoadableFile("beatles.ttl", RdfFormat.TURTLE);
    private LoadableFile music = new LoadableFile("music.ttl", RdfFormat.TURTLE);

    @Test
    public void count() {
        String query = "prefix ex: <http://example.org/> select (count(*) as ?triplet) where { ?s ?p ?o }";
        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void spo() {
        String query = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";
        assertEquals(true, Combination.selectQuery(query, this.beatles));
    }

    @Test
    public void triplePatterns1() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?artist "
                + "WHERE { "
                + "?album a :Album . "
                + "?album :artist ?artist . "
                + "}";
        System.out.println(query);
        assertEquals(true, Combination.selectQuery(query, this.beatles));
    }

    @Test
    public void triplePatterns2() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT * "
                + "WHERE { "
                + "?album a :Album . "
                + "?album :artist ?artist . "
                + "?artist a :SoloArtist . "
                + "}";
        assertEquals(true, Combination.selectQuery(query, this.beatles));
    }

    @Test
    public void orderingResults() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT * "
                + "WHERE { "
                + "?album a :Album ; "
                + ":artist ?artist ; "
                + ":date ?date . "
                + "}"
                + "ORDER BY ?date";
        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void orderingLimitResults() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT * "
                + "WHERE { "
                + "?album a :Album ; "
                + ":artist ?artist ; "
                + ":date ?date . "
                + "} "
                + "ORDER BY desc(?date) "
                + "LIMIT 2";
        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void filteringResults() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT * "
                + "WHERE { "
                + "?album a :Album ; "
                + ":artist ?artist ; "
                + ":date ?date . "
                + "FILTER (?date >= \"1970-01-01\"^^xsd:date) "
                + "} "
                + "ORDER BY ?date ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void bindingValues() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT * "
                + "WHERE { "
                + "?album a :Album ; "
                + ":artist ?artist ; "
                + ":date ?date . "
                + "BIND (year(?date) AS ?year) "
                + "FILTER (?year >= 1970) "
                + "} "
                + "ORDER BY ?date ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void distinctValues() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT DISTINCT ?year "
                + "WHERE { "
                + "?album a :Album ; "
                + ":artist ?artist ; "
                + ":date ?date . "
                + "BIND (year(?date) AS ?year) "
                + "} "
                + "ORDER BY ?year ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void aggregation1() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT (min(?date) as ?minDate) (max(?date) as ?maxDate) "
                + "WHERE { "
                + "?album a :Album ; "
                + ":date ?date . "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void aggregation2() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT (count(?album) as ?count) "
                + "WHERE { "
                + "?album a :Album . "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void grouping() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?year (count(distinct ?album) AS ?count) "
                + "WHERE { "
                + "?album a :Album ; "
                + ":date ?date . "
                + "BIND (year(?date) AS ?year) "
                + "} "
                + "GROUP BY ?year "
                + "ORDER BY desc(?count) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void having() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?album "
                + "WHERE { "
                + "?album a :Album ; "
                + ":date ?date . "
                + "} "
                + "GROUP BY ?album "
                + "HAVING (count(?date) > 1) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void subqueries() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT (avg(?count) AS ?avgCount) "
                + "WHERE { "
                + "SELECT ?year (count(?album) AS ?count) "
                + "WHERE { "
                + "?album a :Album ; "
                + ":date ?date ; "
                + "BIND (year(?date) AS ?year) "
                + "} "
                + "GROUP BY ?year "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void union() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?name "
                + "WHERE { "
                + "{ ?artist a :SoloArtist } "
                + "UNION "
                + "{ ?artist a :Band } ."
                + "?artist :name ?name ."
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void optionnal1() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?song "
                + "WHERE { "
                + "?song a :Song . "
                + "OPTIONAL { "
                + "?song :length ?length . "
                + "} "
                + "} "
                + "ORDER BY desc(?song) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void optionnal2() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                // + "SELECT ?song ?length "
                + "SELECT ?song "
                + "WHERE { "
                + "?song a :Song . "
                + "OPTIONAL { "
                + "?song :length ?length . "
                + "} "
                + "FILTER(!bound(?length)) "
                + "} "
                + "ORDER BY desc(?song) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void notExist() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?song "
                + "WHERE { "
                + "?song a :Song . "
                + "FILTER NOT EXISTS { "
                + "?song :length ?length . "
                + "} "
                + "} "
                + "ORDER BY desc(?song) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void dilterDiff() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select distinct ?artist ?cowriter "
                + "WHERE { "
                + "?song :writer ?artist . "
                + "?song :writer ?cowriter "
                + "FILTER (?artist != ?cowriter) "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void inversePath() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select distinct ?artist ?cowriter "
                + "WHERE { "
                + "?artist ^:writer ?song . "
                + "?song :writer ?cowriter "
                + "FILTER (?artist != ?cowriter) "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void sequencePath() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select distinct ?cowriter "
                + "WHERE { "
                + ":Paul_McCartney ^:writer/:writer ?cowriter "
                + "FILTER (?cowriter != :Paul_McCartney) "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    // @Ignore
    public void recursivePath() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select distinct ?cowriter "
                + "WHERE { "
                + ":Paul_McCartney (^:writer/:writer)+ ?cowriter "
                + "FILTER (?cowriter != :Paul_McCartney) "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void sequencePath2() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select ?album "
                + "WHERE { "
                + "?album :artist/:member :Paul_McCartney "
                + "} "
                + "ORDER BY desc(?album) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void starPath() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT  * "
                + "WHERE { "
                + "?album :artist* ?artist . "
                + "}";

        assertEquals(true, Combination.selectQuery(query, this.beatles));
    }

    @Test
    public void optionalMatch() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select ?album "
                + "WHERE { "
                + "?album :artist/:member? :Paul_McCartney "
                + "} "
                + "ORDER BY desc(?album) ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void alternativePath() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "select ?song "
                + "WHERE { "
                + "?song (^:track/:artist/:member?)|:writer :Paul_McCartney "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void triplePatterns3() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT * "
                + "WHERE { "
                + "?band a :Band . "
                + "?song :writer ?band . "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void minus() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?song "
                + "WHERE { "
                + "?song a :Song . "
                + "?song :writer :Paul_McCartney . "
                + "MINUS { "
                + "?song :writer :John_Lennon ."
                + "} "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void filterExist() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "SELECT ?song "
                + "WHERE { "
                + "?song a :Song . "
                + "?song :writer :Paul_McCartney . "
                + "FILTER EXISTS { "
                + "SELECT ?song "
                + "WHERE { "
                + "?song :writer :John_Lennon . "
                + "} "
                + "} "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }

    @Test
    public void construct() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "CONSTRUCT { "
                + "?member a :BandMember "
                + "} "
                + "WHERE { "
                + "?band a :Band ; "
                + ":member ?member . "
                + "} ";

        assertEquals(true, Combination.constructQuery(query, this.music));
    }

    @Test
    public void describe() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "DESCRIBE :The_Beatles ";

        assertEquals(true, Combination.describeQuery(query, this.music));
    }

    @Test
    public void ask() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "ASK { "
                + "?band a :Band . "
                + "?song :writer ?band . "
                + "} ";

        assertEquals(true, Combination.askQuery(query, this.music));
    }

    @Test
    public void insertWhere() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "INSERT { "
                + "?member a :BandMember "
                + "} "
                + "WHERE { "
                + "?band a :Band ; "
                + ":member ?member . "
                + "} ";

        assertEquals(true, Combination.updateQuery(query, this.beatles));
    }

    @Test
    public void insertData() {
        String query = "PREFIX :<http://stardog.com/tutorial/> "
                + "INSERT DATA { "
                + ":Abbey_Road a :Album ; "
                + ":name \"Abbey Road\" ; "
                + ":date \"1969-09-26\"^^xsd:date ; "
                + ":artist :The_Beatles . "
                + "} ";

        assertEquals(true, Combination.updateQuery(query, this.beatles));
    }

    @Test
    public void federate1() {
        String query = "PREFIX wd: <http://www.wikidata.org/entity/> "
                + "PREFIX wdt: <http://www.wikidata.org/prop/direct/> "
                + "SELECT ?name "
                + "WHERE { "
                + "SERVICE <https://query.wikidata.org/bigdata/namespace/wdq/sparql> { "
                + "wd:q1299 wdt:p527 ?member. "
                + "?member wdt:p1559 ?name. "
                + "} "
                + "} ";

        assertEquals(true, Combination.selectQuery(query, this.music));
    }
}
