/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gaignard
 */
public class Queries {

    public static String QueryBobbyA = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date ?place WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            //                + "     ?x ?y ?name2 ."
            //                + "     ?x dbpedia:birthPlace ?place ."
            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            //            + "     OPTIONAL {?x dbpedia:birthPlace ?place}"
            + " FILTER ((?name ~ 'Bobby A') )"
            //            + " FILTER ((?name ~ 'Bob') )"
            + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";
    public static String QueryBob = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date ?place WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            //                + "     ?x ?y ?name2 ."
            //                + "     ?x dbpedia:birthPlace ?place ."
            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            + "     OPTIONAL {?x dbpedia:birthPlace ?place}"
            //            + " FILTER ((?name ~ 'Bobby A') )"
            + " FILTER ((?name ~ 'Bob') )"
            + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";
    public static String LUBM_Q1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf- syntax-ns#> \n"
            + "PREFIX ub: <http://www.lehigh.edu/?zhp2/2004/ 0401/univ-bench.owl#>\n"
            + "SELECT ?X WHERE { \n"
            + "?X rdf:type ub:GraduateStudent. \n"
            + "?X ub:takesCourse. http://www.Department0.University0.edu/ GraduateCourse0 \n"
            + "}";
    public static String LUBM_Q2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf- syntax-ns#> \n"
            + "PREFIX ub: <http://www.lehigh.edu/?zhp2/2004/ 0401/univ-bench.owl#>\n"
            + "SELECT ?X, ?Y, ?Z WHERE {\n"
            + "?X rdf:type ub:GraduateStudent. ?Y rdf:type ub:University.\n"
            + "?Z rdf:type ub:Department.\n"
            + "?X ub:memberOf ?Z.\n"
            + "?Z ub:subOrganizationOf ?Y.\n"
            + "?X ub:undergraduateDegreeFrom ?Y\n"
            + "}";
    public static String LUBM_Q9 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf- syntax-ns#> \n"
            + "PREFIX ub: <http://www.lehigh.edu/?zhp2/2004/ 0401/univ-bench.owl#> \n"
            + "SELECT ?X, ?Y, ?Z WHERE { \n"
            + "?X rdf:type ub:Student. \n"
            + "?Y rdf:type ub:Faculty. \n"
            + "?Z rdf:type ub:Course. \n"
            + "?X ub:advisor ?Y.\n"
            + "?Y ub:teacherOf ?Z. \n"
            + "?X ub:takesCourse ?Z \n"
            + "}";
    public static String LUBM_Q14 = "";
}
