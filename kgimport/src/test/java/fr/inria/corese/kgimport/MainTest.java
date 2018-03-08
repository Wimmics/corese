package fr.inria.corese.kgimport;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgengine.QueryResults;
import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import org.apache.commons.lang.time.StopWatch;

/**
 *
 * @author gaignard
 */
public class MainTest {

    static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

    public static void main(String[] args) throws EngineException, Exception {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = MainTest.class.getClassLoader().getResourceAsStream("persondata_en.rdf");
        if (in == null) {
            throw new Exception("File: not found");
        }

        // read the RDF/XML file
        StopWatch sw = new StopWatch();
        sw.start();
        model.read(in, null);
        sw.stop();
        System.out.println("Jena model(size " + model.size() + ") init : " + sdf.format(new Date(sw.getTime())));

        sw.reset();
        sw.start();
        Graph g = JenaGraphFactory.createGraph(model);
        sw.stop();
        System.out.println("KGRAM graph (size " + g.size() + ") init : " + sdf.format(new Date(sw.getTime())));
//        System.out.println(RDFFormat.create(g));

        String sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dbpedia: <http://dbpedia.org/ontology/>"
                + "select ?n ?bd where { "
                + "    ?x a foaf:Person ."
                + "    ?x foaf:name ?n ."
                + "OPTIONAL {"
                + "     ?x dbpedia:birthDate ?bd ."
                + "}"
                + "    FILTER (?n ~ 'alban')"
                + "}";

        QueryProcess qp = QueryProcess.create(g);
        Mappings maps = qp.query(sparqlQuery);
        QueryResults qrs = QueryResults.create(maps);

        Enumeration<IResult> ress = qrs.getResults();
        String[] variables = qrs.getVariables();

        for (Enumeration<IResult> en = ress; en.hasMoreElements();) {
            IResult r = en.nextElement();
            HashMap<String, String> result = new HashMap<String, String>();
            for (String var : variables) {
                if (r.isBound(var)) {
                    IResultValue[] values = r.getResultValues(var);
                    for (int j = 0; j < values.length; j++) {
                        System.out.println(var + " = " + values[j].getStringValue());
//                            result.put(var, values[j].getStringValue());
                    }
                } else {
                    System.out.println(var + " = Not bound");
                }
            }

        }
    }
}
