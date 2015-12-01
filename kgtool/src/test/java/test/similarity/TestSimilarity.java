package test.similarity;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.api.ASTVisitable;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.approximate.ext.ASTRewriter;
import fr.inria.edelweiss.kgram.tool.ApproximateSearchEnv;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * TestSimilarity.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 20 août 2015
 */
public class TestSimilarity {

    static String data = "/Users/fsong/NetBeansProjects/kgram/kgengine/src/test/resources/data/";

    public static void main(String[] args) throws EngineException, SAXException, ParserConfigurationException, IOException {
        Graph graph = Graph.create(true);

        Load ld = Load.create(graph);
        ld.load(data + "comma/comma.rdfs");
        ld.load(data + "comma/model.rdf");
        //ld.load(data + "comma/data");
        String options = " pragma { "
                + "kg:approximate"
//                + "               kg:algorithm 'jw'; "
//                + "               kg:priority_a '2'; "
//                + "               kg:strategy  'URI'; "
//                + "               kg:priority_s '1'; "
//                + "               kg:wn_path '/Users/fsong/NetBeansProjects/kgram/kgtool/target/classes/wordnet'; "
//                + "               kg:wn_ver '3.0'; "
//                + "               kg:pos_tagger '/Users/fsong/NetBeansProjects/kgram/kgtool/target/classes/tagger/english-left3words-distsim.tagger'; "
//                + "               kg:string_metric 'Lin'; "
                + "               kg:threshold '0.1'; "
                + "} ";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select more distinct ?x ?doc where {"
                //+ "?x rdf:type c:Engineer "
                + "?x c:hasCreated ?doc "
                + "?doc rdf:type c:WebPage }"  + options
                ;

        QueryProcess exec = QueryProcess.create(graph);
        exec.setVisitor(new ASTRewriter());
        Mappings map = exec.query(query);
        //SimilarityResults.getInstance().aggregate(map);
       // System.out.println(ApproximateSearchEnv.getInstance().toString());
        System.out.println("\n ******** Mappings ****\n" + map);
    }
}
