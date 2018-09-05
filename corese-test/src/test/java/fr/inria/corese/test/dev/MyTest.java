package fr.inria.corese.test.dev;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.print.RDFFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;
import static junit.framework.Assert.assertEquals;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class MyTest {

    static String root = MyTest.class.getClassLoader().getResource("test/").getPath() + "/";
    static String data = MyTest.class.getClassLoader().getResource("test/data").getPath() + "/";

    public static void main(String[] args) throws EngineException, LoadException {
        new MyTest().process();
    }

    
    private void process() throws EngineException, LoadException{
        Graph g = Graph.create();
         QueryProcess exec = QueryProcess.create(g);
         Load ld = Load.create(g);
         ld.parse(NSManager.RDF);
         RDFFormat f = RDFFormat.create(g);
         System.out.println(f);
         String q = "select * where { "
                 + "bind (10 as ?x) "
                 + "bind (10 as ?y) "
                 + "filter (?x * ?y + 3 * ?x <= 1000)"
                 + "}";
         Mappings map = exec.query(q);
         //System.out.println(map);
    }
       
     private void process10() throws EngineException, LoadException {
         Constant.setString(true);
         QueryLoad ql =  QueryLoad.create();
         String q = ql.readWE("/home/corby/AATest/test/queryscaler/q5.rq");
         //System.out.println(q);
         Graph g = Graph.create();
         QueryProcess exec = QueryProcess.create(g);
//         Mappings map = exec.query(q);
//         System.out.println(map);
//        Query qq = exec.compile(q);
//         System.out.println(qq.getAST());
         
         
         String q2 = "select * "
                 + "from named us:g "
                 + "from us:g "
                 + "where { "
                 + "{select * where {graph ?g { service <uri> { ?x ?p ?y } ?y ?q ?z }}}  "
                 + "optional {{?x ?p ?y, ?z} union { {?z ?r ?t, ?z} minus {?a ?p ?b} } }"
                 + "filter (?x < ?y + 1 || not exists { ?x ?p ?y , ?z })"
                 + "}"
                 
                 + "function us:fun(?x) {"
                 + "let (?x = ?y) {"
                 + "for (?x in ?l) {"
                 + "map(lambda(?x) { us:fun(?x) }, ?list)"
                 + "}"
                 + "}"
                 + "}";
         String q3 = "insert { us:a us:p us:b, us:c}  where { us:a us:p us:b, us:c}";
         
         Query qq = exec.compile(q2);
         System.out.println(qq.getAST());
//         ASTQuery ast = (ASTQuery)qq.getAST();
//         
//         System.out.println(ast.getBody().get(0).get(0).get(0));
     }
    
    
    private void process9() throws EngineException {

        String i = "prefix munc:   <http://ns.inria.fr/metauncertainty/v1/>"
                + "insert data { "
                + "graph us:g1 { us:John rdf:type foaf:Person ; foaf:name 'John' }"
                + "us:g1 munc:hasUncertainty [] "
                + "}";
        
        String q = "prefix munc:   <http://ns.inria.fr/metauncertainty/v1/>"
                + "@metadata "
                + "select * where {"
                + "graph ?g { "
                + "{triple( ?x ?p ?y ?m) }"
                + "union {"
                + "triple( ?s ?p ?o ?n) "
                + "}"
                + "}"
                + "}"
                
                + "function us:metaList(?Tm,?Gm) { }";
        
        String q2 = "prefix munc:   <http://ns.inria.fr/metauncertainty/v1/>"
                + "@visitor <fr.inria.corese.compiler.visitor.MetadataVisitor> "
                + "<fr.inria.corese.compiler.visitor.TraceVisitor>"
                + "select * "
                
               // + "from named us:g1 "
                + "where {"
                + "graph ?g { "
                                             
                + "{ triple(?s ?p ?o ?m1) } union { ?s ?q ?v }  "
                
                + "}"
                
                + "}"
                
                + "function munc:metaList(?t, ?g){"
                + "xt:list(?t, ?g)"
                + "}";

        Graph g = Graph.create();
        g.setMetadata(true);
        g.setVerbose(true);
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q2);
        System.out.println(map);
        System.out.println(map.size());
    }
    
    private void process7() throws EngineException {
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate <http://corese.inria.fr/sparql>  "
                + "<http://fr.dbpedia.org/sparql>"
                + "@bounce <http://corese.inria.fr/sparql>"
                + "@sparqlzero <http://corese.inria.fr/sparql>"
                + "@type kg:verbose kg:exist "
                //+ "@skip kg:group kg:select "
                + "@debug "
                + "select  * {"
                + " ?x h:name ?n "
                + "?y rdfs:label ?n"
                + " optional { ?x h:age ?a }"
                // + "?y rdfs:label ?n  "

                + "}"
                + "";
        
        String q2 = "select (avg(?n) as ?avg) (aggregate(?n) as ?list) (us:stdev(?list, ?avg) as ?std)"
                + " where {"
                + "values ?n { unnest(xt:iota(5)) }"
                + "}"
                + ""
                + "function us:stdev(?list, ?avg) {"
                + "    power(reduce(rq:plus, maplist(lambda(?val, ?avg) { power(?val - ?avg, 2) }, ?list, ?avg)), 0.5)"
                + "}";

        Graph g = Graph.create();
        g.setVerbose(true);
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q2);
        System.out.println(map);
        System.out.println("Size: " + map.size());

    }

    private void process6() throws EngineException, LoadException {
        Graph g = Graph.create();
        g.setVerbose(true);
        QueryProcess exec = QueryProcess.create(g);

        String q = "@debug @type kg:verbose "
                + "@federate <http://dbpedia.org/sparql> <http://fr.dbpedia.org/sparql>"
                + "select * "
                + "from <http://fr.dbpedia.org> "
                + "from <http://dbpedia.org> "
                + "where { ?x rdfs:label 'Antibes'@fr ; rdfs:comment ?c }" //+ "limit 10"
                ;

        String q2 = "@debug @type kg:verbose "
                + "@federate <http://fr.dbpedia.org/sparql> "
                + "<http://dbpedia.org/sparql>"
                + "select * "
                + "from named <http://fr.dbpedia.org> "
                + "from named <http://dbpedia.org> "
                + "where { graph ?g { ?x rdfs:label 'Antibes'@fr ; rdfs:comment ?c } }" //+ "limit 10"
                ;

        String q3 = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@debug @type kg:verbose kg:exist "
                + "@federate <http://fr.dbpedia.org/sparql> "
                + "<http://corese.inria.fr/sparql>"
                + "select * "
                + "where { "
                + "?x h:name ?n filter not exists {?x h:age ?a} "
                + "optional { ?x h:age ?a }"
                + "optional {?y rdfs:label ?n optional { ?y foaf:isPrimaryTopicOf ?c }}"
                + "}" //+ "limit 10"
                ;

        Mappings map = exec.query(q3);
        System.out.println(map);
        System.out.println(map.size());
    }

    private void process5() throws EngineException, LoadException {
        Graph g = Graph.create();
        g.setMetadata(true);
        g.setVerbose(true);
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "tmp.rul");
        RuleEngine re = ld.getRuleEngine();
        ld.parse(NSManager.RDF);
        re.process();

        String q = "select * where { ?s rdfs:seeAlso ?o}";
        Mappings map = exec.query(q);
        System.out.println(map.size());

        String q2 = "select * where { triple(?s rdfs:seeAlso ?o ?l ) }";
        Mappings map2 = exec.query(q2);
        System.out.println(map2);
        //System.out.println(g.display());
    }

    private void process4() throws EngineException {
        String i = "insert  {"
                + "us:Jim   foaf:knows  us:Jack "
                + "us:John  foaf:knows  us:Jack "
                + "us:James foaf:knows  us:Jack "
                + "}"
                + "where {"
                + "bind (xt:list(xt:list('trust', 0.5)) as ?l1)"
                + "bind (xt:list(xt:list('trust', 0.8)) as ?l2)"
                + "}"
                + "@public "
                + "function us:metadata2() {"
                + "coalesce(st:set(st:metadata, st:get(st:metadata) + 1), st:set(st:metadata, 0))"
                + "}";

        String q = "@debug "
                + "select  * "
                // + "(us:get('trust', ?v) as ?t) "
                + " where {"
                + "triple (?s ?p ?o ?v )"
                + " filter (?v >= 1)"
                + "}"
                + "function us:get(?name, ?l) {"
                + "for ((?label, ?value) in ?l) {"
                + "if (?name = ?label) {"
                + "return (?value)}"
                + "} ;"
                + "return (error())"
                + "}";

        String q2 = "construct { triple(?s ?p ?o ?v) } "
                + "where { triple(?s ?p ?o ?v) }";

        Graph g = Graph.create();
        g.setMetadata(true);
        g.setVerbose(true);
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        System.out.println(map);
        map = exec.query(q2);
        Graph gg = (Graph) map.getGraph();
        System.out.println(gg.isMetadata());
        System.out.println(gg.display());
        System.out.println("Size: " + map.size());

        QueryProcess exec2 = QueryProcess.create(gg);
        Mappings res = exec2.query(q);
        System.out.println(res);

        String qp = "select * where {"
                + "?x us:pp* ?y"
                + "}";
        res = exec2.query(qp);
        System.out.println(res);
    }

    private void process8() throws EngineException {
        String q = "prefix h: <http://www.inria.fr/2015/humans#>"
                + "@federate <http://corese.inria.fr/sparql>  <http://fr.dbpedia.org/sparql>"
                + "@type kg:verbose kg:exist "
                //+ "@skip kg:group kg:select "
                + "@debug "
                + "select  * {"
                + "service <http://corese.inria.fr/sparql> { ?x h:name ?n } "
                + "?y rdfs:label ?m "
                + "filter not exists { ?y h:age ?n }"
                + "filter not exists { ?y rdfs:comment ?n }"
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
