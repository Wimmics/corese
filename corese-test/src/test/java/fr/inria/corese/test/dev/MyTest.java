package fr.inria.corese.test.dev;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class MyTest {
    
    public static void main(String[] args) throws EngineException {
        new MyTest().process();
    }

    private void process() throws EngineException {
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@service <http://corese.inria.fr/sparql>  <http://fr.dbpedia.org/sparql>"
                + "@type kg:verbose kg:exist "
                //+ "@skip kg:group kg:select "
                + "@debug "
                + "select  * {"
                + "?x h:name ?n , ?m "
                + "?y rdfs:label ?n "
                + "filter exists { ?x h:name ?nn }"
              //  + "filter exists { ?y rdfs:comment ?c }"
                + "}"
                + "";
        
        Graph g = Graph.create();
        g.setVerbose(true);
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        System.out.println(map);
        System.out.println("Size: " + map.size());
       
    }

}
