/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.bgp;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.kgram.core.Mappings;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Approx {

     
    public static void main(String [] args) throws EngineException, LoadException{
       new Approx().testApprox();
    }
    
       public void testApprox() throws EngineException, LoadException {
             Graph g = Graph.create(); 
             QueryProcess exec = QueryProcess.create(g);
             
             String i = "insert data {"
                     + "foaf:Person  rdfs:subClassOf foaf:Thing "
                     + "foaf:Man  rdfs:subClassOf foaf:Thing "
                     
                     + "us:John a foaf:Person , xt:Person ; "
                     + "rdfs:label 'John' "
                     + "us:Jim rdfs:label 'Jim' "
                     + "us:Jan rdfs:label 'Jan' "
                     + "[] rdfs:label 'the cat is on the mat' "
                    + "}"; 
             
             String q = //"@relax @debug " +
                      "select  *"
                     + " (sim() as ?s) "
                     + "where {"
                    // + "?x a us:Person "
                     + "?x rdfs:label 'Jon' "
                     + "}"
                     + "order by desc(?s)";
             
           // +"pragma {kg:approximate kg:strategy "
//                             + "'URI_LEX',  "
//                             + "'LITERAL_LEX' "
//                             + "}"
                             
                             ;
                                         
                     
//                     + "pragma {"
//                     + "kg:approximate kg:strategy  "
//                     + "'URI_LEX', "
////                     + "'PROPERTY_EQUALITY', "
////                     + "'URI_EQUALITY' "
////                     + "'CLASS_HIERARCHY', "
////                     + "'LITERAL_WN', "
//                     + "'LITERAL_LEX' "
////                     + ";"     
////                    + " kg:algorithm 'jw', 'ng', 'ch', 'wn', 'eq' . " 
//                     + "}" ;  
                     
                     String q2 =
                   "prefix h: <http://www.inria.fr/2007/09/11/humans.rdfs#>"
                   + "@relax   xsd:anyURI  "
                   + "select * (sim() as ?s)  where {"
                   + "  {us:Jon  h:name 12} union { us:Jim rdfs:label 'Jimmy' }"
                   + "}"
                   + "order by desc(sim()) " ;
            
                     Load ld = Load.create(g);
                     ld.parse("/home/corby/Cours/2016/done/tp/human_2013.rdf");
             exec.query(i);
             
             Mappings map = exec.query(q2);
             System.out.println(map.getQuery().getAST());

             System.out.println(map);
//             System.out.print(ApproximateSearchEnv.get(4));
             System.out.println(map.size());
            
       }
      
      
    
    
}
