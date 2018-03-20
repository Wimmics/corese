package fr.inria.corese.similarity;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.approximate.ext.ASTRewriter;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;

/**
 * Tests.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 sept. 2015
 */
public class Tests {

    public static void main(String[] ars) throws EngineException {
        //init();
        //test(q_use_approximate_as_pure_function);
        //test2(q_with_optinoal);
        //test2(q_with_pragma);
        //test2(q_with_similarity);
        test2(q_with_similarity_and_having);
        //test(q_without_more_but_with_sim);
        //test2(q_without_similarity);
        //test(q_data2, data2);
        //test(q_plein);
    }

    static QueryProcess getExec() throws EngineException {
        return getExec(data);
    }

    static QueryProcess getExec(String data) throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(data);

        return exec;
    }

    static void test2(String q) throws EngineException {
        QueryProcess exec = getExec();

        System.out.println("\n-- Query --\n" + q);

        Mappings map = exec.query(q);
        System.out.println("-- real mappings [" + map.size() + "] --\n" + map.toString());

        exec.setVisitor(new ASTRewriter());
        map = exec.query(q);
        System.out.println("-- approximate mappings [" + map.size() + "] --\n" + map.toString());
    }

    static void test(String q) throws EngineException {
        test(q, data);
    }

    static void test(String q, String data) throws EngineException {
        QueryProcess exec = getExec(data);
        exec.setVisitor(new ASTRewriter());

        Mappings map = exec.query(q);
        System.out.println("\n-- Query --\n" + q);
        System.out.println("-- approximate mappings [" + map.size() + "] --\n" + map.toString());
    }

    //static QueryProcess exec;
    final static String Prefix = "PREFIX kg:<http://ns.inria.fr/edelweiss/2010/kgram/> \n"
            + "PREFIX foaf:<http://xmlns.com/foaf/> \n";

    static String data = Prefix
            + "insert data {"
            + "kg:person foaf:name 'John'. "
            + "kg:person2 foaf:name 'John'. "
            + "kg:person3 foaf:name 'Johnny'. "
            + "kg:person4 kg:name 'John Wang'. "
            + "kg:person5 kg:name 'Super King' "
            + "kg:person6 kg:name 'John' "
            + ""
            + "foaf:name owl:equivalentProperty kg:name "
            + "kg:person5 owl:sameAs kg:person "
            + "kg:person4 kg:listen 'my heart will go on' "
            + "kg:person3 kg:listen 'I do not listen music' "
            + "kg:person2 kg:listen 'my heart is broken' "
            + "}";

    static String data2 = "insert data {"
            + "foaf:Person  rdfs:subClassOf foaf:Thing "
            + "foaf:Man  rdfs:subClassOf foaf:Thing "
            + "us:John a foaf:Person ; "
            + "rdfs:label 'John' "
            + "us:Jim rdfs:label 'Jim' "
            + "us:Jan rdfs:label 'Jan' "
            + "[] rdfs:label 'the cat is on the mat' "
            + "}";

    static String q_data2
            = "select more  *"
            + " (sim() as ?s) "
            + "where {"
            + "?x a  us:Person "
            + "}"
            + "order by desc(?s) "
            + "pragma {kg:approximate kg:strategy 'URI_LEX', 'URI_EQUALITY', 'PROPERTY_EQUALITY', 'LITERAL_LEX'}";

    static String options = "pragma { "
            + "kg:approximate kg:algorithm 'jw', 'ng'; "
            + "               kg:priority_a '2', '2'; "
            + "               kg:strategy  'URI_LEX', 'URI_EQUALITY', 'PROPERTY_EQUALITY', LITERAL_LEX'; "
            + "               kg:wn_path '/Users/fsong/NetBeansProjects/kgram/kgtool/target/classes/wordnet'; "
            + "               kg:wn_ver '3.0'; "
            + "               kg:pos_tagger '/Users/fsong/NetBeansProjects/kgram/kgtool/target/classes/tagger/english-left3words-distsim.tagger'; "
            + "               kg:string_metric 'Lin'; "
            + "               kg:threshold '0.0'; "
            + "} "
            + "";

    //use sim() but no keyword more
    static String q_without_more_but_with_sim = Prefix
            + "select * (sim() as ?sim33) "
            + "where {"
            + "kg:person foaf:name ?name ."
            + "} order by desc(?sim33) ";//limit 5

    static String q_with_optinoal = Prefix
            + "select more * "
            + "where {"
            + "kg:person foaf:name \"John\" "
            + "optional { ?person kg:listen 'my broken heart will go on' } "
            + "}  ";

    static String q_with_pragma = Prefix
            + "select more * "
            + "where {"
            + "kg:person foaf:name \"John\" "
            + "}  "
            + options;

    static String q_without_similarity = Prefix
            + "select more * "
            + "where {"
            + "kg:person foaf:name \"John\" "
            + "} ";

    static String q_with_similarity = Prefix
            + "select more * (sim() as ?sim33)"
            + "where {"
            + "?person foaf:name \"John\" ."
            + "?person kg:listen 'my broken heart will go on' "
            + "} "
            + "";

    static String q_with_similarity_and_having = Prefix
            + "select more * (sim() as ?sim33) "
            + "where { "
            + "?person foaf:name \"John\". "
            + "?person kg:listen 'my broken heart will go on' "
            + "} "
            + " order by desc(?sim33) "
            + " group by ?sim33 "
            //+ " having (?sim33 > 0.0) "
            + "";

    //use approximate() as pure filter function
    static String q_use_approximate_as_pure_function = Prefix
            + "select * "
            + "where {"
            + "?person foaf:name ?z ."
            + "filter approximate(?person, kg:person, 'ng', 0.8)"
            + "} ";

    static String q_plein = "select more * where {?x kg:name 'John'}";
}
