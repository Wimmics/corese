package test.similarity;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.tool.ApproximateSearchEnv;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.approximate.ext.ASTRewriter;

/**
 * Tests.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 sept. 2015
 */
public class Tests {

    public static void main(String[] ars) throws EngineException {
        Graph g = Graph.create();

        QueryProcess exec = QueryProcess.create(g);
        String init = ""
                + "PREFIX kg:<http://ns.inria.fr/edelweiss/2010/kgram/>  "
                + "PREFIX foaf:<http://xmlns.com/foaf/> "
                + "insert data {"
                + "kg:person foaf:name 'John'. "
                + "kg:person2 foaf:name 'John'. "
                + "kg:person3 foaf:name 'Johnny'. "
                + "kg:person4 foaf:name 'John Wang'. "
                + "kg:person5 kg:name 'Super King' "
                + "kg:person6 kg:name 'John' "
                + ""
                + "foaf:name owl:equivalentProperty kg:name "
                + "kg:person5 owl:sameAs kg:person "
                + "kg:person4 kg:listen 'my heart will go on' "
                + "kg:person3 kg:listen 'I do not listen music' "
                + "kg:person2 kg:listen 'my heart is broken' "
                + "}";

        String q3 = " PREFIX kg:<http://ns.inria.fr/edelweiss/2010/kgram/> "
                + "PREFIX foaf:<http://xmlns.com/foaf/> "
                + "select more *  (sim() as ?sim33) "
                + "where {"
                + "kg:person foaf:name \"John\" ."
                + "} order by desc(?sim33) ";//limit 5

        String q32 = " PREFIX kg:<http://ns.inria.fr/edelweiss/2010/kgram/> "
                + "PREFIX foaf:<http://xmlns.com/foaf/> "
                + "select more *  (sim() as ?sim33) "
                + "where {"
                + "kg:person foaf:name \"John\" "
                + "optional { ?person kg:listen 'my broken heart will go on' } "
                + "} order by desc(?sim33) ";//limit 5

        String q31 = " PREFIX kg:<http://ns.inria.fr/edelweiss/2010/kgram/> "
                + "select more * (sim() as ?sim33)"
                + "where {"
                + "?person foaf:name \"John\" ."
                + "?person kg:listen 'my broken heart will go on' "
                + "} ";

        //pure function
        String q37 = " PREFIX kg:<http://ns.inria.fr/edelweiss/2010/kgram/>"
                + "PREFIX foaf:<http://xmlns.com/foaf/> "
                + "select * (sim() as ?sim33)"
                + "where {"
                + "?person foaf:name ?z ."
                + "filter approximate(?person, kg:person, 'ng', 0.8, true)"
                + "} ";

        String options = "pragma { "
                + "kg:approximate kg:algorithm 'jw', 'ng'; "
                + "               kg:priority_a '2', '2'; "
                + "               kg:strategy  'URI_NAME', 'URI_LABEL', 'LITERAL_LEX'; "
                // + "               kg:priority_s '1', '3', '5'; "
                + "               kg:wn_path '/Users/fsong/NetBeansProjects/kgram/kgtool/target/classes/wordnet'; "
                + "               kg:wn_ver '3.0'; "
                + "               kg:pos_tagger '/Users/fsong/NetBeansProjects/kgram/kgtool/target/classes/tagger/english-left3words-distsim.tagger'; "
                + "               kg:string_metric 'Lin'; "
                + "               kg:threshold '0.0'; "
                + "} "
                + "";

        String q34 = "select *\n"
                + "where\n"
                + "{?_var_0 ?_var_1 ?_var_2 . \n"
                + "filter approximate(?_var_0, kg:person, 'ng-jw-wn', '0.0'^^<http://www.w3.org/2001/XMLSchema#double> )  "
                + ""
                + "optional {?_var_0 <http://www.w3.org/2002/07/owl#sameAs> kg:person . \n"
                + "kg:person <http://www.w3.org/2002/07/owl#sameAs> ?_var_0 . }\n"
                + ""
                + "filter approximate(?_var_1, foaf:name, 'ng-jw-wn', '0.0'^^<http://www.w3.org/2001/XMLSchema#double>, true)  "
                + ""
                + "optional {?_var_1 <http://www.w3.org/2002/07/owl#sameAs> foaf:name . \n"
                + "foaf:name <http://www.w3.org/2002/07/owl#sameAs> ?_var_1 . } "
                + ""
                + "optional {?_var_1 <http://www.w3.org/2002/07/owl#equivalentProperty> foaf:name . \n"
                + "foaf:name <http://www.w3.org/2002/07/owl#equivalentProperty> ?_var_1 . }\n"
                + ""
                + "filter approximate(?_var_2, 'John', 'wn-ng-jw', '0.0'^^<http://www.w3.org/2001/XMLSchema#double>, true) }";

        String q341 = "select *\n"
                + "where\n"
                + "{?_var_0 ?_var_1 ?_var_2 . \n"
                + "filter approximate(?_var_0, kg:person, 'ng-jw-wn', '0.0'^^<http://www.w3.org/2001/XMLSchema#double> , true)  "
                + "filter approximate(?_var_1, foaf:name, 'ng-jw-wn', '0.0'^^<http://www.w3.org/2001/XMLSchema#double>, true)  "
                + "filter approximate(?_var_2, 'John', 'wn-ng-jw', '0.0'^^<http://www.w3.org/2001/XMLSchema#double>, true) "
                + ""
                + "optional {?_var_0 <http://www.w3.org/2002/07/owl#sameAs> kg:person . \n"
                + "kg:person <http://www.w3.org/2002/07/owl#sameAs> ?_var_0 . }\n"
                + ""
                + "optional {?_var_1 <http://www.w3.org/2002/07/owl#sameAs> foaf:name . \n"
                + "foaf:name <http://www.w3.org/2002/07/owl#sameAs> ?_var_1 . } "
                + ""
                + "optional {?_var_1 <http://www.w3.org/2002/07/owl#equivalentProperty> foaf:name . \n"
                + "foaf:name <http://www.w3.org/2002/07/owl#equivalentProperty> ?_var_1 . }\n"
                + "}";

        exec.query(init);
        exec.setVisitor(new ASTRewriter());

        Mappings map = exec.query(q32);

        System.out.println(" *** appx search env ***\n" + ApproximateSearchEnv.get(1));
        //System.out.println(" *** query ***\n" + map.getQuery());
        System.out.println("\n ***** mappings [" + map.size() + "] *****\n" + map.toString());
    }
}
