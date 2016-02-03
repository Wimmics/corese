package junit;
// failure: 143
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

import java.util.*;






import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.cst.RDFS;

import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;

public class TestKgram extends TestSuite
{
  static boolean displayResult = true;
  
  static Graph qGraph;
  static NSManager nsm;

    public TestKgram(String Name_)

    {
        super(Name_);

    } //public CoreseTestSuite(String Name_)

    public TestKgram(){}
   
   
	
   
    
    static String getQuery(String name){
    	name = nsm.toNamespace(name);
    	Edge edge = qGraph.getEdge(RDFS.COS + "value", name, 0);
    	if (edge == null) return null;
    	return edge.getNode(1).getLabel();
    }

    public static Test suite() {
      TestKgram suite= new TestKgram();
      Param mod;
      String corese = null;
      String query;
      //String DATA = "file:///home/corby/workspace/coreseV2/src/test/resources/data";
      //String DATA = "file:///home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data";
      String DATA = "file://" + TestKgram.class.getClassLoader().getResource("data").getPath();
      if (true){   	     	 
            
    	 

    	  QuerySolver.definePrefix("data",  DATA + "/comma/");
    	  QuerySolver.definePrefix("data1", DATA + "/comma/data/");
    	  QuerySolver.definePrefix("data2", DATA + "/comma/data2/");
    	  QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");
    	  
    	  if (!true) {
    		 //displayResult =  true;
    		  for (int i = 0; i < 1; i++) {
                     // QueryProcess.setJoin(true);
                   
                      
  suite.addTest(new CoreseTest2(true, "testQuery", corese,
   "select ?y where { ?x rdf:type c:Person  ?x c:SomeRelation ?y " +
        "filter(datatype(?y) = xsd:string)   filter(contains(?y,  'x')) }"
          + "order by ?y" +
   "", 19));
  
 


                  }
          }
        
        
      else for (int i=0; i<1; i++) {
    	  
          //QueryProcess.setJoin(true);
    	  
    	  suite.addTest(new CoreseTest2(true, "testQuery", corese,
   "select  where { rdfs:domain rdfs:domain ?x} " +
   "", 1));
    	  
    	  if (true){
    		  
    		  qGraph = Graph.create();
    		  Load load = Load.create(qGraph);
    		  nsm = NSManager.create();
    		  nsm.defNamespace("http://www.inria.fr/edelweiss/2008/query#", "q");
    		  load.load(DATA + "/comma/query.rdf");
			  qGraph.index();

 //Query.testJoin = false; 
query =  "select * where {?x c:isMemberOf @{?this rdf:type c:Consortium} + ?org}";			  
			  
suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  7));


query =  "select * where {?x c:isMemberOf @{graph $path {?a c:isMemberOf ?this} ?a rdf:type c:Person} + :: $path  ?org}";			  

suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  16));


query =  "select * where {?x c:isMemberOf @{filter(?this = <http://www.inria.fr/>)} + ?org}";			  

suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  5));


query = "select * where {?x c:FirstName ?n} values ?n { 'Olivier' }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  11));

query = "select (max(?age+?age) as ?max) where {?x c:age ?age}";

suite.addTest(new CoreseTest2("testValue", corese, query, "?max", 90));

query =
	"select distinct * where {" +
	"?x rdf:type c:Person filter(?x ~ 'olivier.corby') " +
	"?x (rdf:type/rdfs:subClassOf)+ ?y" +
	"}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  13));



//query =
//	"select * where {" +
//	"?x rdf:rest{0,}/rdf:first ?y" +
//	"}";
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  10));

query = 
"select ?x (count(?y) as ?c) where { "+
"{select * where {?x rdf:rest*/rdf:first ?y}} "+
"minus {?x rdf:first ?y} "+
"filter(! exists{?x rdf:first ?y}) "+
"filter(?y  in (?x , ?y))} "+
"group by ?x";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  3));



query =	"select  ?x ?n where {"+
	"{select * where {?x c:FirstName ?n}} "+
	"filter( exists {?x c:FamilyName ?n})}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query,  1));



query =
"select * (count(*) as ?c)  where {" +
"{select * where {?x rdf:rest*/rdf:first ?y }}"+
"minus {?x rdf:first ?y} "+
"filter(! exists{?x rdf:first ?y}) "+
"filter(?y  in (?x , ?y))}";

suite.addTest(new CoreseTest2("testValue", corese, query, "?c", 6));

query =
	"select * (count(*) as ?c)  where {" +
	"{select * where {?x rdf:rest*/rdf:first ?y }}"+
	"filter(exists{?x rdf:rest []  filter(! exists{?x rdf:first ?y}) }) "+
	"}";

	suite.addTest(new CoreseTest2("testValue", corese, query, "?c", 6));




query = 
	"select ?x (count(?doc) as ?c)  where {" +
	"?x c:hasCreated ?doc " +
	"}" +
	"group by ?x " +
	"having (?c >= 50 && exists{?x c:Designation ?n})";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5));




query = 
	"select * where {" +
	"?x c:FirstName ?name " +
	"filter(?name in (?x, self(?x), 'Olivier', 'Bernard'))" +
	"}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 18));



query =
	"select distinct * where {" +
	"[ c:FirstName ?name ;" +
	"c:isMemberOf ?org ]" +
	"} ";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 16));


query =
	"select (count(distinct *) as ?c) where {" +
	"[ c:FirstName ?name ;" +
	"c:isMemberOf ?org ]" +
	"} ";

suite.addTest(new CoreseTest2("testValue", corese, query, "?c", 16));


//query = 
//	"select * where {" +
//	"?x (rdf:rest{0,1}/rdf:rest{0,1})*/rdf:first ?y" +
//	"}";
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 32));

//query = 
//	"select * where {" +
//	"?x rdf:rest{0,3}/rdf:first ?y" +
//	"}";
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10));


query =
	"select  * " +
	"from named <http://www.w3.org/2000/01/rdf-schema#> where {" +
	//"graph ?g1 " +
	"{" +
	"{select * where {graph ?g {?p ?p ?x }}}}}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5));

query =
	"select  * " +
	"from named <http://www.w3.org/2000/01/rdf-schema#> " +
	"from named <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"where {" +
	"graph ?g " +
	"{" +
	"{select * where {graph ?g {?p ?p ?x }}}}}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 6));



query =
	"select  * " +
	"from named <http://www.w3.org/2000/01/rdf-schema#> " +
	"from named <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"where {" +
	"graph ?g1 " +
	"{" +
	"{select * where {graph ?g {?p ?p ?x }}}}}";
// TODO
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 6));



query =
	"select  * " +
	"from named <http://www.w3.org/2000/01/rdf-schema#> " +
	"from named <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"where {" +
	"graph ?g " +
	"{" +
	"{select ?p where {graph ?g {?p ?p ?x }}}}}";

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 6));


query =
	"select  * " +
	"from named <http://www.w3.org/2000/01/rdf-schema#> " +
	"from named <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"where {" +
	"graph ?g " +
	"{" +
	"{select * where { {?p ?p ?x }}}}}";


suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 6));


query =
	"select  * " +
	"from named <http://www.w3.org/2000/01/rdf-schema#> " +
	"from named <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"where {" +
	"graph ?g " +
	"{" +
	"{select * where { {?p ?p ?x filter(?g != <uri>)}}}}}";


suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));





query =
	"select * where {graph ?g {?p ?p ?x  " +
	"{select * where {?q ?q ?y}}     }}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 35));

query = 
	"select * where {?p ?p ?x minus {?q ?q ?y}}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 8));

query = 
	"select * where {graph ?g {?p ?p ?x minus {?q ?q ?y}}}";

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 4));

query = 
	"select  * where {" +
	"{?x rdfs:seeAlso ?y} " +
	"minus {{?x rdfs:label ?z } union {?x rdfs:seeAlso rdfs:comment}}}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 3));

query = 
	"select * where {" +
	"?x ?p ?y optional {?a ?a ?a} ?z ?q ?t " +
	"filter(?x = ?y && ! (?y = ?x))" +
	"}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));

query =
	"select * where {" +
	"?x c:FirstName ?name " +
	"filter(?name = 'Olivier') " +
	"?z ?q ?t " +
	"filter(?name = ?t && ?z = ?x)" +
	"}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 33));

query =
	"prefix fun: <function://fr.inria.edelweiss.kgramenv.util.QueryExec>" +
	"select * where {" +
	"{select (unnest(fun:kgram('select * where {graph ?g {?p ?p ?x}} ')) " +
		"as (?g, ?p, ?x)) where {} }" +
	"}";

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 4));

query =
	"prefix fun: <function://fr.inria.edelweiss.kgramenv.parser.ProxyImpl>" +
	"select *  (fun:self(?x) as ?self) where {" +
	"?x ?p ?y " +
	"} limit 1";

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

query = "select * where { " +
	"graph ?g {" +
		"optional { ?x rdf:rest*/rdf:first ?y  } " +
		"filter(! bound(?y))" +
		"?x c:hasCreated ?doc " +
		"}} limit 1 " ;

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));







query = "select * where { graph ?g {} }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 870));

query = "select distinct ?g where { graph ?g { optional{ {graph ?g {}} union {graph ?g {}}}  } }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 870));




query = "select * where { graph ?g {} graph ?g {} }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 870));






query = "select * " +
		"from named <" + DATA + "/comma/testrdf.rdf>" +
		"where { graph ?g {} }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));











//query = 
//	"select * where {" +
//	"{select (sparql('select * where {?x ?p ?y} limit 1') as  (?x, ?p, ?y))  " +
//	"where {}}" +
//	//"{select extern('test', ?x, ?p, ?y) as ?z where {}}" +
//	"}" ;

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

query = "select * from  <" + DATA + "/comma/testrdf.rdf> where { " +
	"?x rdf:rest*/rdf:first   ?y  }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10));

query = "select * from named <" + DATA + "/comma/testrdf.rdf> where { " +
"graph ?g {?x rdf:rest*/rdf:first ?y } }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10));


query = "select (sum(?age) as ?sum) {?x c:age ?age}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));


query = "select * where {" +
"graph ?g {{select * where {?x ?p ?g}}}" +
"} limit 2";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2));

query =
	"select  * where {" +
	"graph ?g { ?x c:FirstName 'Olivier' }" +
	"?g  xpath('/rdf:RDF//c:FirstName/text()') 'Olivier' " +
	"?g  xpath('/rdf:RDF//*/@rdf:about') ?x " +
	"} limit 1";

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

query = "select * where {" +
"c:Document (^rdfs:range/rdfs:domain?)*::$path ?any " +
"filter(pathLength($path) >= 3 )" +
"} limit 1";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

query = 
	"prefix c: <http://www.inria.fr/acacia/comma#> " +
	"select * where {" +
	"{select ?x (count(?doc) as ?count) where { " +
		"?x c:hasCreated ?doc} group by ?x " +
		"having(?count > 50)} " +
	"?x c:Designation ?name " +
	"}  order by desc(?count) limit 10 ";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 8));

query =
	"prefix c: <http://www.inria.fr/acacia/comma#> " +
	"select  * where {" +
	"{?x ?r ?x     }  " +
	"{select ?x ?y ?q where { " +
		" {select * where {" +
			"{select * (self(?p) as ?q) where {?x ?p ?x}}" +
		"}}" +
	"}}" +
	"} limit 1";



suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));






query =
	"prefix c: <http://www.inria.fr/acacia/comma#> " +
	"select  * where {" +
	"{?y ?r ?x     }  " +
		"{{select * where {" +
			"{select * where {?x ?p ?y}}" +
		"} " +
		"}}" +	
	"} limit 1";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

query =
	"prefix c: <http://www.inria.fr/acacia/comma#> " +
	"select  * where {" +
	"?y c:Colleague ?x " +
	"{select * where { " +
		"{select * where {" +
			"{select * where {?x ?p ?y}}" +
		"}}" +
	"}}" +
	"} limit 1";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));


query =
	"prefix c: <http://www.inria.fr/acacia/comma#> " +
	"select  *  where { " +
"?x c:FirstName ?y  " +
"{select $path where {  ?x rdf:rest*/rdf:first :: $path ?y  }}" +
"?x ?q ?y  " +
"} limit 1" ;

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

//suite.addTest(new CoreseTest2(true, "testQuery", corese,
//	      "select  where {" +
//	      "?x c:FirstName ?z   ?y c:FirstName ?t filter (! ( ?x && ?y) || (?z && ?t) )} projection 1", 1));

	// 68
//	        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//	       "select  where {" +
//	       "?x c:FirstName ?z   ?y c:FirstName ?t filter ((?x && ?y) || (?z && ?t))} projection 1", 1));
//
	        
	        
//query =         "select   projection 1 where {" +
//	 "?x c:FirstName ?z   ?y c:FirstName ?t " +
//	  "filter  ( ( ! ?x && ?y) ||  (! ?z && ?t)) }";
//	          
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));






query =
	"select ?x (count(?doc) as ?count) where {" +
	"?x c:hasCreated ?doc" +
	"} group by ?x " +
	"having (?count > 50)";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5)); 


query = 
	"select * (pathLength($path) as ?l) where { " +
	"{select $path where { " +
	"?x rdf:rest*/rdf:first :: $path ?y }}" +
	"{select $path1 where { " +
	"?x rdf:rest*/rdf:first :: $path1 ?y }}" +
	"graph $path  {?a ?p ?b}" +
	"graph $path1 {?a ?p ?b}}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 64)); 

query = 
	"select  distinct ?src where { " +
	"graph ?src {<http://www.inria.fr/alain.giboin> rdf:type c:Something" +
	"<http://www.inria.fr/alain.giboin> ?p ?y " +
	"<http://www.inria.fr/alain.giboin> ?p ?y " +
	"}}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 4)); 


query =
	"prefix ci: <http://www.inria.fr/acacia/comma/instance#>" +
	"select  * where {" +
	"{?y rdf:type ?class  filter(?y = ci:BuildingTopic) } union" +
	"{?y rdf:type ?class  filter(?y = ci:ComputerScienceTopic ) }  " +
	"?x  c:isMemberOf / c:IsInterestedBy ?y }" ;

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 90)); 

//query = "construct {?a <p> ?b} select genURI('a') as ?a genURI('b') as ?b where { }" ;
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query = "select * where {?x   ^ rdfs:label ?y  filter(?x ^ 'engineer'@en)  }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query = 
	"select * where { ?x rdf:rest * / rdf:first ?y }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10)); 

query =
	"select * where { " +
	"?x  i  c:CreatedBy ? / (rdfs:domain / rdfs:range)*   ?y " +
	"filter(?x ^ c:Person) " +
	"filter(?y ^ c:)" +
	"} limit 1";


//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query =
	"select *  where { " +
	"?x  i  (rdfs:domain / rdfs:range)* / c:CreatedBy ?  ?y " +
	"filter(?y ^ c:)" +
	"} limit 1";


//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 


query =
	"select * where { " +
	"?x  i  (rdfs:domain / rdfs:range)*   ?y " +
	"filter(?x ^ c:Person) " +
	"filter(?y ^ c:)" +
	"} limit 1";


//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query =
	"select *  where { " +
	"?x  i  (rdfs:domain / rdfs:range)*   ?y " +
	"filter(?y ^ c:)" +
	"} limit 1";


//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

suite.addTest(new CoreseTest2(true, "testQuery",corese,
        "select distinct ?x where { " +
        "{?x rdf:type c:Person ?x c:isMemberOf ?org} union " +
"{?org rdf:type c:Organization ?org c:Include ?x}}" +
"order by ?x", 46));

//query = "select * where {?x ?p ?y ?y ?p ?x}";
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));

//suite.addTest(new CoreseTest2(true, "testQuery",corese,
//		   "select ?c ?p where { ?c rdfs:subClassOf::?p c:Document " + 
//		" optional { ?c  direct::rdfs:subClassOf::?q c:Document } " +
//		     "filter (! bound(?q)) } order by ?c ", 43));
		         
query =
	"select * where {?x ?p ?y filter(?x ~ 'olivier.corby')} limit 100 offset 10";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 45)); 

query =
	"select * where {?x ?p ?y filter(?x ~ 'olivier.corby')} limit 10 offset 10";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10)); 

query =
	"select * where {?x ?p ?y filter(?x ~ 'olivier.corby')} limit 100 offset 50";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5)); 



query =getQuery("q:groupfun");

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 


//query =getQuery("q:selfun");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 


query =getQuery("q:distinctFun1");

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query =getQuery("q:distinctFun");

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query = 
	"select more * where { {?x rdf:type c:Event ?x c:hasCreated ?doc}}" +
	"limit 10";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10)); 

//query = getQuery("q:aggregate");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query = "select  * where {" +
		"optional{<http://www.inria.fr/olivier.corby> rdf:type c:Person}" +
		"}";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 4)); 

query = "construct {graph ?g {?x c:name '?a' ?a c:name <?a> }" +
		"?x c:name _:b1  ?a c:name _:b1} " +
"where { graph ?g {?x ?p ?a}  filter(! isLiteral(?a))} projection 1 limit 1";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query = getQuery("q:base");

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 11)); 


//query = getQuery("q:xslconstruct");
//
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

//query = getQuery("q:xconstruct");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

query = getQuery("q:pathType");

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10)); 


//query = getQuery("q:fromVar");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 14)); 


query = getQuery("q:construct");

//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

//query = getQuery("q:onto");

query = "select    *  "+
"where { graph ?g { ?elem rdfs:label ?label }	" +
"{select ?elem ?label " +
"(xpath(?g, '/rdf:RDF/*[ contains($elem, @rdf:ID) ]/rdfs:comment[ contains(., $label) ]/text()') as ?comment) " +
"where {}}" +
"}limit 1";


//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 

//query = getQuery("q:rewrite");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 3)); 

//query = getQuery("q:bug");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 3)); 


//query = getQuery("q:row");
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 4)); 



//query = getQuery("q:path");

query = 
	"prefix cc: <http://www.inria.fr/acacia/comma#>"+
"select   * "+
"where { "+
"graph ?g { ?doc cc:CreatedBy ?x ?x cc:FamilyName ?name  filter( ?name = 'Corby' )} "+
"filter(?name in (xpath(?g, '/rdf:RDF//*[cc:FamilyName = \"Corby\" ]/*/text()' )))" +
"bind (xpath(?g, '/rdf:RDF/*/cc:Title/text()' ) as ?title) " +
"}";


suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 4)); 


query = getQuery("q:path1.1");




//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 


    	
query =
"select * where { _:b ?p ?v  filter(?v = 'Olivier') }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 36)); 

query =
	"select * where { _:b ?p _:b    }";

	suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 3)); 
	
	query =
		"select * where { _:b1 ?p _:b2  _:b2 ?p _:b1   }";

		suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5359)); 
		
		
		
		
		query = "select * where {" +
		"c:Document i (rdfs:range/(rdfs:domain?))* :: $path ?any " +
		//"filter(match($path,  star(rdfs:range &&  opt (rdfs:domain)), 'i' ))" +
		"filter(pathLength($path) >= 3 )" +
		"} limit 1";
		
		
		
		query ="select * where {" +
		"c:Person (rdfs:range | rdfs:domain*)* c:Document " +
		"} limit 1";
		
suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
		
query ="select * where {" +
		"c:Person (rdfs:range | rdfs:domain*)*  c:Document " +
		//"filter(match($path, star(rdfs:range || star(rdfs:domain))))" +
		"} limit 1";
		
suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));

    		  query = "select   ?x ?y where {" +
    	 		"  ?x rdf:first ?any  ?x rdf:rest*/rdf:first :: $path ?y " +
    	 		"filter(pathLength($path) <= 10) " +
    	 		//"filter(match($path, star(rdf:rest) && rdf:first)) " +
    	 		"optional {?v rdf:rest ?x} filter(! bound(?v)) " +
    	 		//"graph $path {?a ?p ?b}" +
    	 		"}  group by ?x  order by ?x  ";
    	          		  
suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 
    		  
//    query = "select ?g ?a ?q ?b where {" +
//      		"rec graph ?gg {?x ?p ?y filter(pathLength(?p) = 2)}" +
//      			"graph ?p {?a ?q ?b} " +
//      			"graph ?g {?a ?q ?b}" +
//      		"} projection 2 limit 2";
//          		
//       suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2)); 
//          		      		
           		  
    		  
    	
//   query = "select  * count(?doc) as ?count where {" +
//   		"?pers c:hasCreated ?doc filter(?count + ?count > 50)} " +
//   		"group by ?pers  order by desc(?count)  limit 5 "; 
//   
//   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5)); 
    		  
    query =		"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select  ?a ?q ?b where {" +
				"c:Document d i ( rdfs:range | rdfs:domain | rdfs:subClassOf)* :: $path c:FirstName " +
				"graph $path { ?a ?q ?b       }" +
				//"filter(match($path,  star( rdfs:range || rdfs:domain || rdfs:subClassOf),  'di' ))" +
				"filter(pathLength($path) = 5 )" +
				"}  limit 5";
    
    //suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5)); 
     
    		  
//  query =  		"prefix c: <http://www.inria.fr/acacia/comma#>" +
//				"select ?a ?q ?b  where {" +
//				"?x rdf:type ?class  filter(?x = c:FirstName) " +
//				"?x $path c:Document " +
//				"graph $path { ?a ?q ?b   ?c rdfs:subClassOf ?d  }" +
//				"filter(match($path,  " +
//					" star(rdfs:domain || rdfs:range || rdfs:subClassOf),  'i' ))" +
//				"filter(pathLength($path) = 5 )" +
//				"filter(isDistinct(?a, ?q, ?b))" +
//				"} limit 5";
//					
//  suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5)); 
    		  
//   query = "select * where {?x c:age ?age filter(member(?age, [46, 45]))}";
//   
//   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 
   
   
   query = "select   *   where { ?x c:hasCreated ?doc " +
   		"  { ?x rdf:type c:uaiSYDFU } }   limit 1" ; 
    	   
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0)); 	
    		  
   query = "select     where { optional { ?x c:hasCreated2 ?doc " +
   		"?x c:hasCreated4 ?doc } }" ; 
   
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 
    		  
   query ="select * where {" +
   		"?x c:hasCreated [ c:Title ?title ; c:CreatedBy ?x ] ; " +
   		"c:FirstName ?name, ?n2 " +
   		"filter(?x ~ 'olivier.corby')}";
   
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5)); 
   
   
   query ="select * where { ?x ?p (?y) }";
   
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1)); 
   
    		  
    query ="construct {?doc c:CreatedBy ?x" +
    		"?x c:age 10 ?doc ?p ?v } where {" +
    		"?x c:hasCreated ?doc ?doc ?p ?v filter(?x ~ 'olivier.corby')" +
    		"}";
    
    suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 81));  
    
    
    query ="ask {" +
	"?x c:hasCreated ?doc filter(?x ~ 'olivier.corby') }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));  

query ="describe ?x where  {" +
"?x c:hasCreated ?doc filter(?x ~ 'olivier.corby') }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 345));  


query ="describe <http://www.inria.fr/olivier.corby> where  { }";

suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 69));  
    
//    		  query = 
//   			   "select *  where {" +
//   			   "?doc c:CreatedBy ?x " +
//   			   "?doc c:CreatedBy ?y " +
//   			   "?doc c:CreatedBy ?z " +
//   			   "filter(isDistinct(?y, ?z)) " +
//   			   "filter(?x ~ 'olivier.corby')"+
//   			   "}  ";
//
//
//    		  suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 40));  
//    		  
//    		  query = 
//      			   "select  *  where {" +
//      			   "?doc c:CreatedBy ?x " +
//      			   "?doc c:CreatedBy ?y " +
//      			   "?doc c:CreatedBy ?z " +
//      			   "filter(isDistinctSorted(?y, ?z)) " +
//      			   "filter(?x ~ 'olivier.corby')"+
//      			   "}  ";
//
//
//       		  suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 24)); 
    		  
//    		  query = 
//   			   "select  *  where {" +
//   			   "?doc c:CreatedBy ?x " +
//   			   "?doc c:CreatedBy ?y " +
//   			   "?doc c:CreatedBy ?z " +
//   			   "filter(isDifferent(?x, ?y, ?z)) " +
//   			   "filter(?x ~ 'olivier.corby')"+
//   			   "}  ";
//
//
//   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 26));             		
//   		  		  
    		  
   query="select * where {" +
    	 		"?x ?p ?y optional{?x ?p ?z} filter(!bound(?z)) }";       		
    	        		
    suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
    	        		  
    		  
    		  query="select * where {" +
    	 		"?x c:FirstName '\"?Olivier\"' } " ;
    	 		
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));
    		  	  
    		  
   query = "select  where {" +
	   "?x c:hasCreated ?doc filter(?x ~ 'olivier.corby')} display rdf";
    		  	      
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5));
   
//   query = "select  * where {?x c:isMemberOf ?y  ?y c:isMemberOf ?z " +
//   		"?x direct::c:isMemberOf ?z}";
//    	    		   
//   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
//    	     		  
    	// remove 21/01/2010	  
   query = "select  * where {" +
   		"?x rdf:type ?class " +
   		"?x cos:Property * :: $path ?y   filter(?x ~ 'olivier.corby')" +
   		"filter (pathLength($path) <= 2) graph $path {?a ?a ?a}}";
    		   
  // suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
    		     
   
   query = "select * where {?x c:hasCreated ?doc ?doc rdf:type c:Annotation}";
   
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
   
//   query = "select distinct * where {" +
//   "?x $path 'Olivier'   " +
//   "filter(match($path, ! c:Designation, 'p' )) " +
//   "filter(pathLength($path) = 1 )" +
//   "} "; 
//   
//   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 12));
   
   query = "select distinct * where {" +
   "?x $path 'Olivier'   " +
   "filter(match($path, ! c:Designation, '' )) " +
   "filter(pathLength($path) = 1 )" +
   "} "; 
   
   //suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
    		  
   query = "select distinct * where {" +
   "?x $path ?y  filter (?y = 'Olivier' ) " +
   "filter(match($path, c:Designation, '' )) " +
   "filter(pathLength($path) = 1 )" +
   "} "; 
   
   //suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 12));  
   
//   query = "select distinct * where {" +
//   "?x $path ?y  filter (?y = 'Olivier' ) " +
//   "filter(match($path, c:Designation, 'p' )) " +
//   "filter(pathLength($path) = 1 )" +
//   "} "; 
//   
//   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));  
   
//   query =	"select  pathLength($path) as ?length  where {" +
//	"<http://www.inria.fr/olivier.corby> c:SomeRelation::$path ?y " +
//"filter(?y ~ 'cstb') " +
//"filter(match($path, star(c:SomeRelation), 'ds' )) }" +
//"order by pathLength($path)";
//
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 9));     
//   
   
    		  
    		   query = "select   * where {" +
    		  "?x rdf:type ?class " +
    		  "?x direct::$path ?y  filter (?x ~ 'olivier.corby') " +
    		  "filter(match($path,  " +
    		  "plus(c:isMemberOf || c:Include) && star(c:SomeRelation)  " +
    		  "&& plus (c:HasForWorkInterest || c:IsInterestedBy  )" +
    		  ")) " +
    		  "filter(isURI(?y))" +
    		  "} limit 5"; 
    		  
   // suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5));
    		  
    suite.addTest(new CoreseTest2(true, "testQuery", corese,
    "select * where {" +
    "<http://www.inria.fr/olivier.corby> c:SomeRelation * :: $path ?y " +
    "filter(?y ~ 'cstb') filter(pathLength($path) <= 3)" +
    "optional { graph $path { ?a ?p ?b filter(?b ~ 'homepage') } }" +
    "filter(! bound(?b)) }", 0));
    			    
    		  	  
    		  
//    	suite.addTest(new CoreseTest2(true, "testQuery", corese,
//    	"select distinct ?y where {" +
//    	"<http://www.inria.fr/olivier.corby> c:SomeRelation*::$path ?y " +
//    	"filter(?y ~ 'cstb') filter(pathLength($path) <= 3) }", 1));
    	
    	
    		  
    	  suite.addTest(new CoreseTest2(true, "testQuery", corese,
    "select more ?x   (score() as ?score)  where {?x rdf:type c:Event ?x rdf:type ?class} limit 10", 10));
    
    	  //pragma
    	  
//    	  suite.addTest(new CoreseTest2(true, "testQuery",corese,
//    			  "select       distinct ?n1 ?n2   where {  ?x c:FirstName ?n1 " + 
//    			  "  { ?y c:FirstName ?n2 filter (?n1 < ?n2) } " +
//    			  " optional { ?z c:FirstName ?n3 filter( ?n3 < ?n2 && ?n3 > ?n1 ) } " + 
//    			  "filter ( ! bound(?n3)) } order by ?n1 ?n2  limit 10  "
//    			  , 10));  
    	  
//    	  suite.addTest(new CoreseTest2(true, "testQuery", corese,
//    			  "select         distinct  sorted  ?l1 ?l2   where {" +
//    			  " ?x ?p ?l1 filter isLiteral(?l1)    ?y ?q ?l2  filter (isLiteral(?l2))  "
//    			  + " filter(xsd:integer(?l1) = ?l2)  filter ( ! sameTerm(?l1,  ?l2) ) }"
//    			  , 6));
//    	  
//    	  suite.addTest(new CoreseTest2(true, "testQuery", corese,
//    			  "select          distinct  sorted  ?l1 ?l2   where { " +
//    			  " ?y ?q ?l2 ?x ?p ?l1  filter isLiteral(?l1)     filter (isLiteral(?l2))  "
//    			  + " filter ( xsd:integer(?l1) = ?l2 ) filter   ( ! sameTerm(?l1,  ?l2) ) }" 
//    			  , 6));
//    	  
//    	  suite.addTest(new CoreseTest2(true, "testQuery", corese,
//    			   "select nosort   distinct ?x   where {?x ?p ?y ?y ?r ?t ?t ?q ?z " +
//    			   "?z ?v ?w    ?x c:isMemberOf ?org}" , 46));

    	  
    	  }
    	  
        if (true) {
          // a set of combinatoric queries (take half time of all queries)
        	
 query = "select * where {?x c:FirstName ?name} order by str(?x) limit 10";
    
  suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10));
  
  query = "select   *  where { filter (?src ~ '122' ) " + 
  " graph ?src { optional { ?x rdf:type c:Person filter (?src ~ '122' ) " +
  "optional { ?y rdf:type c:Person  } filter (?y = ?y) }   }  } ";	 

  suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));
            		        	 
  
        	    		        	 
 query =  "select    *  where { filter (?src ~ '122' ) " + 
  "  graph ?src    { optional { ?x rdf:type c:Person filter (?x = ?x) }    }  }" +
  "   order by (?src) limit 10";       	 
 
 suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));
        			        	  	 
 query = "select distinct ?src  where { graph ?src    { ?x rdf:type  c:Person  }   }" +
 "  limit 1000 " ;
 
 suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 845));
            		        	 
 query = "select ?x (count(?doc) as ?count) where { ?x c:hasCreated ?doc  } " + 
 " order by desc(count(?doc)) group by ?x limit 5";
 
     suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 5));
        				     	 
        	 
//     query = "select    where { ?x c:hasCreated ?doc " + 
//     " optional { ?x ?p ?y ?y ?q ?z } filter(! bound(?z)) }";     	 
//        	 
//       suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
            		       	 
 
//query =  "select  distinct  sorted ?v1 ?v2   where { ?x ?p ?v1  ?y ?q ?v2 " + 
//        "filter ( datatype(?v1) != datatype(?v2) && ?v1 = ?v2 ) }";
//
//       suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 3));

       
//query =       "select        distinct  sorted  ?l1 ?l2   where { " +
//       " ?x ?p ?l1 filter isLiteral(?l1)    ?y ?q ?l2  filter isLiteral(?l2)  "
//       + " filter ( datatype(?l1) = xsd:boolean && str(?l1) = str(?l2)) }" ;
//
//          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 7));
//
// query=         "select   distinct  sorted  ?l1 ?l2   where { " +
//          " ?x ?p ?l1 filter isLiteral(?l1)  ?y ?q ?l2  filter isLiteral(?l2)  "
//           + "  filter (sameTerm( str(?l1),  ?l2 )  && ! sameTerm( ?l1, ?l2 )) }";
          
 //   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 13));
        	          
          
 

//   query = "select        distinct  sorted  ?l1 ?l2   where { " +
//   " ?x ?p ?l1 filter isLiteral(?l1)    ?y ?q ?l2  filter isLiteral(?l2)  "
//   + " filter (str(?l1) = str(?l2) &&  ( ! sameTerm(?l1,  ?l2) ) && ?l1 ~ 'http' ) } ";
//   
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 7));

query = "select ?x   where {" +
"?x c:FirstName ?name ?x c:hasCreated ?doc} order by ?name limit 10";

          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 10));

// query =         "select   where { ?x rdfs:label ?l filter( ?l = 'engineer'@en )  " +
//          "  ?y rdfs:label ?l2 filter( ?l2 = 'engineer'@en  ) " +
//          "filter ( lang(?l)  = lang(?l2)) }  ";
//          
//          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));
          
//   query =       "select   where { ?x rdfs:label ?l filter (lang(?l) = 'en') " +
//          " ?y rdfs:label ?ll filter (lang(?ll) = 'fr' && ?x != ?y && ?l == ?ll) }";
//          
//          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));
//
// query =         "select  where { ?x rdfs:label ?l filter (lang(?l) = 'en') " +
//          "?y rdfs:label ?ll " +
//          "filter (lang(?ll) = 'fr' && ?x != ?y && str(?l) = str(?ll)) }";
// 
//          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 15));

 query =         "select   *  where { ?p rdfs:range ?range  ?x ?p ?y ?y rdf:type ?cc "+
          "optional {?y rdf:type ?class filter (?class = ?range) } "+
          "optional {?y rdf:type ?class2  ?class2 rdfs:subClassOf ?range } "+
          "filter ( ! bound(?class) && ! bound(?class2)) }";
 
          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));

          
 query =         "select   distinct ?p  where { ?p rdfs:range ?range  ?x ?p ?y " +
          "optional {?y rdf:type ?class filter (?class = ?range)}  "+
          "optional {?y rdf:type ?class ?class rdfs:subClassOf ?range}" +
          "    filter (! bound(?class)) }";
          
          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 22));

 query =         "select   where {?x rdf:type rdfs:Class " +
          "optional {?x rdfs:subClassOf ?class " +
          "filter (?class = rdfs:Resource)} filter (! bound(?class)) }";          
          
    //      suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 3));

 

query =          "select distinct ?x  result 10000  where { ?x ?p ?l " +
          "filter ( lang(?l) || ! lang(?l) ) }";
          
          suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2322));

 query =         "select distinct ?x  result 10000   where { ?x ?p ?l " +
          "filter ( isLiteral(?l) ) }";         
          
    suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2322));

    
// query =   "select   projection 1 where { ?x c:FirstName ?z .  ?y c:FirstName ?t " + 
//    "filter (! ( ?x && ?y) || ! (?z && ?t)) }";    
//
// suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));

 query = "SELECT   DISTINCT sorted ?x ?y WHERE " +
 "{ ?x c:FirstName ?n  . ?y c:FirstName ?n  . FILTER  ( ?x < ?y) " +
 " OPTIONAL { ?x c:FirstName ?n . FILTER (?n = 'toto' ) } } limit 1000 ";
 
         suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 887));

          
 query =         "select   distinct sorted ?x ?y where { " +
          "?x c:FirstName ?n .  ?y c:FirstName ?n   filter(  ?x < ?y ) }" +
          "limit 1000";
 
   suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 887));


           
        }

       // else 
        
        {
        // sparql like syntax

        displayResult = false;


        mod = new Param();
         String[] bbb = {"?x + asdfyouif"};
         String[] oper = {">="};
         String[] exp = {"count(?x) - 2"};
         String[] bool = {"||"};
         mod.put("name", bbb);
         mod.put("oper", oper);
         mod.put("exp", exp);
         mod.put("bool", bool);

 boolean qlong = true;
 boolean qshort = true;

 suite.addTest(new CoreseTest2(true, "testQuery", corese,
  "select    distinct  sorted  ?x ?l1    where { ?x ?p  ?l1    " +
  " filter(str(?l1) = '45') } ", 1));


 if (true){
   // long queries
	 
	 suite.addTest(new CoreseTest2(true, "testQuery", corese,
			 "select *  where { ?x ?p ?val   " + 
			 " filter ( ?val = 2  ) } ", 4));
	 
	 suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select distinct ?t where { ?x c:FirstName ?fn  ?x c:FamilyName ?ln  ?y c:FirstName ?fn filter (?x != ?y) " +
"?y c:isMemberOf ?org  ?z c:isMemberOf ?org filter (?y != ?z) ?z c:hasCreated ?doc  ?doc c:CreatedBy ?pers " +
"filter (?pers != ?z) ?pers ?p ?t  ?t c:IsInterestedBy ?topic2 }"
			 ,8));
	 
	 // with degree : 0.2, without : 20 !!!

//	 suite.addTest(new CoreseTest2(true, "testQuery", corese,
// "select distinct sorted ?x ?y ?z where { " +
// "?a ?q ?x  ?b ?r ?y ?c ?s ?z . ?x ?p ?y  ?y ?p ?z  ?z ?p ?x }" , 7));

			   
//   suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select   distinct ?src where { graph ?src { ?x c:hasCreated ?doc " +
//" optional { ?y c:hasCreated ?doc2 filter (?x != ?y) } " +
//"  filter ( ! bound(?y) ) } }"
//, 22)); // 7 sec



 

   suite.addTest(new CoreseTest2(true, "testQuery", corese,
    "select   where { ?x ?p ?x   filter (?x = ?z) } " , 0));

   suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select   where { ?x ?p ?x   filter (?x = ?p || ?p != ?x) }" , 3));

   suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select  where { ?x ?p ?y   filter ( ?x = ?y && ?y = ?x) " +
      "?z ?q ?t  filter (?z = ?t && ?x = ?t) }" , 3));

       suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x ?p ?y  ?y rdf:type rdf:Resource " +
        " filter (?x = ?y && ?y = ?p) } "
         , 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select  where { ?x ?p ?y   filter (?x = ?y && ?p = ?y) }"
       , 0));


//   suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select   distinct ?s1 ?p  count (?s1) as ?count  where {" +
//"graph ?s1 {?x ?p ?y}  graph ?s2 {?z ?q ?t }" +
//      " filter (?x = ?t && ?z = ?y && ?p = ?q && ?s1 = ?s2) " +
//      "} order by ?s1 group by ?s1",    5));

//      suite.addTest(new CoreseTest2(true, "testQuery", corese,
//      "select    distinct ?s1 ?p (count(?s1) as ?count)  where { " +
//      " graph ?s1 {?x ?p ?x}  optional {?u ?q ?r}  filter (?p = ?q" +
//      " && ?x = ?t) } order by ?s1 group by ?s1 ",    0));

//      suite.addTest(new CoreseTest2(true, "testQuery", corese,
//      "select  distinct ?s1 ?p  count (?s1) as ?count  where {" +
//      "graph ?s1 {?x ?p ?y}  graph ?s2 {?z ?q ?t} " +
//     " filter (?x = ?t && ?z = ?y && ?p = ?q && ?s1 = ?s2 && ?s1 != ?s2)}" +
//     "order by ?s1 group by ?s1",    0));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select   where {?x ?p ?y   filter (?x = ?y && ?y = ?x) }", 3));


// suite.addTest(new CoreseTest2(true, "testQuery", corese,
//     "select * where { ?x1 ?p1 ?z1 . filter (datatype(?z1) = xsd:integer) " +
//     "?x2 ?p2 ?z2 . filter (datatype(?z2) = xsd:integer) " +
//     "filter (regex (str(datatype (?z1 / ?z2)), '.*decimal' ) )  } ", 36));
//


//      suite.addTest(new CoreseTest2(true, "testQuery", corese,
//      "select more threshold 1  where {score ?s { ?x rdf:type c:HeadOfPole }}",
//      100));
//
//
//      suite.addTest(new CoreseTest2(true, "testQuery", corese,
//           "select more  *  threshold 1   where {" +
//           "score ?s { ?x rdf:type c:Engineer ?x rdf:type c:HeadOfPole }}  ",
//          100));


//         suite.addTest(new CoreseTest2(true, "testQuery", corese,
//  "select where { ?x rdf:type c:Person . ?x c:hasCreated ?doc . " +
//  "?doc rdf:type c:TechnicalReport " +
//  "optional { ?xNot ?p ?doc2 . ?doc2 rdf:type c:ResearchReport . " +
//  "filter ( ?xNot = ?x ) } filter ( ! bound(?xNot) ) }", 46));


//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//            "select distinct * where {" +
//            "graph ?s1 {?x ?p ?y}    graph ?s2 {?z ?p ?t } " +
//            " filter( ?t = ?s1 && ?s1 ~ 'model.rdf' )} projection 2 "
//            , 1));


//           suite.addTest(new CoreseTest2(true, "testQuery", corese,
//           "select distinct ?p   where { " +
//           "graph ?s1 {?x ?p ?y }   graph ?s2 {?z ?q ?t }" +
//           "filter(     ?q = ?p && ?t = ?s1 ) }"
//           , 1));
//
//
//           suite.addTest(new CoreseTest2(true, "testQuery", corese,
//       "select    distinct ?s1 where {" +
//       "graph ?s1 {?x ?p ?y}  graph ?s2 {?t ?q ?z} " +
//     " filter( ?s1 = ?z)} projection 10 ", 1));

//           suite.addTest(new CoreseTest2(true, "testQuery", corese,
//                "select distinct ?s1 where {graph ?s1 {?x ?p ?y} " +
//                " graph ?s2 {?z ?q ?t} " +
//                "filter(?x ~ 'olivier.corby' && ?s1 = ?s2 ) } ", 6));
//
//
//                suite.addTest(new CoreseTest2(true, "testQuery", corese,
//                "select distinct ?s1 where {" +
//                "graph ?s1 {?x ?p ?y}  graph ?s2 {?z ?q ?t} " +
//                "filter(?x ~ 'olivier.corby' && ?s1 >= ?s2 && ?s1 <= ?s2) }", 6));
//
//           suite.addTest(new CoreseTest2(true, "testQuery", corese,
//      "select distinct ?p where { ?x ?p ?y   ?z ?q ?t " +
//      "filter(?x ~ 'olivier' && ?p >= ?q && ?p <= ?q) }", 15));

//           suite.addTest(new CoreseTest2(true, "testQuery", corese,
//              "select    distinct ?s1 where { " +
//              "graph ?s1 {?x ?p ?y}  graph ?s2 {?t ?q ?z}" +
//              " filter(?s1 = ?q) }" +
//              "projection 10  ", 0));
//
//           suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select  distinct ?s1 where {" +
//        "graph ?s1 {?s1 ?p ?y}  graph ?s2 {?s2 ?q ?z} " +
//        " filter( ?s1 >= ?s2 && ?s1 <= ?s2) }" +
//        "projection 10 ", 10));

// remove 22/12
      
     suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select    where {?x ?p ?src  graph ?src {?a ?q ?b}} group by ?x", 31));

//  suite.addTest(new CoreseTest2(true, "testQuery", corese,
//                 "select distinct ?p where {?x ?p ?y filter(?x ~ 'olivier')  ?z ?q ?t " +
//                 " filter( ?q = ?p) } ", 15));

 }


 //******************



if (true){ // short queries
	

	
	
	// string != Literal != XMLLiteral return type error :
	
	
	suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto' != 'toto'@en )}" , 0));
		
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'@en != 'toto' )}" , 0));
		
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto' != 'toto'^^rdf:XMLLiteral )}" , 0));
			
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'^^rdf:XMLLiteral != 'toto' )}" , 0));
		
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'@en != 'toto'^^rdf:XMLLiteral )}" , 0));
				
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'^^rdf:XMLLiteral != 'toto'@en )}" , 0));
		
		
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto' != 'toto'^^c:aa )}" , 0));
			
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'^^c:aa != 'toto' )}" , 0));
		
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'^^c:aa != 'toto'^^rdf:XMLLiteral )}" , 0));
				
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'^^rdf:XMLLiteral != 'toto'^^c:aa )}" , 0));
			
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'@en != 'toto'^^c:aa )}" , 0));
			
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
			"select * where { ?x c:Designation ?val  filter ('toto'^^c:aa != 'toto'@en )}" , 0));
		
	
		suite.addTest(new CoreseTest2(true, "testQuery", corese,
				"select * where { ?x c:Designation ?val  filter ('toto'^^c:aa != 'toto'^^c:bb )}" , 0));
								
			suite.addTest(new CoreseTest2(true, "testQuery", corese,
				"select * where { ?x c:Designation ?val  filter (20 != 'toto'@en )}" , 0));
									
			suite.addTest(new CoreseTest2(true, "testQuery", corese,
					"select * where { ?x c:Designation ?val  " +
					"filter (?x = <http://www.inria.fr/> && ?val != <http://www.inria.fr> )}", 6));
			
			suite.addTest(new CoreseTest2(true, "testQuery", corese,
					"select * where { ?x c:Designation ?val  " +
					"filter (?x = <http://www.inria.fr/> && ?val != 'http://www.inria.fr'@en )}", 1));
			
			
			suite.addTest(new CoreseTest2(true, "testQuery", corese,
					"select * where { ?x c:Designation ?val  " +
					"filter (?x = <http://www.inria.fr/> && ?val != 'http://www.inria.fr' )}", 2));
			
			suite.addTest(new CoreseTest2(true, "testQuery", corese,
					"select * where { ?x c:Designation ?val  " +
					"filter (?x = <http://www.inria.fr/> && ?val != 'http://www.inria.fr'^^xsd:string )}", 2));
			
			suite.addTest(new CoreseTest2(true, "testQuery", corese,
					"select * where { ?x c:Designation ?val  " +
					"filter (?x = <http://www.inria.fr/> && ?val != 'http://www.inria.fr'^^rdf:XMLLiteral )}", 1));
			

			
			
			
			
			
			
			
			
			
			
suite.addTest(new CoreseTest2(true, "testQuery", corese,
	"select * where { ?x c:age ?v filter (?v != 'true'^^c:undef ) }", 0));


suite.addTest(new CoreseTest2(true, "testQuery", corese,
   "select * where { ?x c:pp ?v filter (?v = 'true'^^c:undef) }", 1));

		     	

//suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select  distinct sorted ?y ?t    where { " +
//"?x ?p ?y  filter (isLiteral(?y) && isLiteral(?t) && ?x ~ 'olivier.corby') . ?z ?q ?t    " +
//"   filter (?z ~ 'olivier.corby' && key(?y) = key(?t) ) } order by ?y "
//, 8));


    suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select         distinct sorted ?y ?t      where { " +
"?x ?p ?y   filter( ?x ~ 'olivier.corby' ) ?z ?q ?t  filter( ?z ~ 'olivier.corby') " +
"   filter (self(?y) < self(?t))  filter (isLiteral(?y) && isLiteral(?t)) } order by ?y"
, 9));


    suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select         distinct sorted ?y ?t    where {" +
"?x ?p ?y   filter( ?x ~ 'olivier.corby')  ?z ?q ?t   filter(?z ~ 'olivier.corby') " +
" filter isBlank(?y) filter (datatype(?y) = datatype(?t)) } order by ?y"
, 0));


    suite.addTest(new CoreseTest2(true, "testQuery", corese,
    		"select         distinct sorted ?y ?t    where {" +
    		"?x ?p ?y   filter( ?x ~ 'olivier.corby')  ?z ?q ?t   filter(?z ~ 'olivier.corby') " +
    		"  filter (datatype(?y) = datatype(?t)) } order by ?y"
    		, 14));



    suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select    distinct  sorted  ?l1 ?l2   where {" +
" ?x rdfs:label ?l1 ?y rdfs:label ?l2 "
+ " filter( ?x != ?y &&    str(?l1) = str(?l2)  && lang(?l1) != lang(?l2)) }" +
		" limit 10   "
, 10));

    suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select  * where { ?doc c:CreatedBy ?a ?doc c:Title ?t filter(?t ~ 'knowledge' ) " +
      "optional { ?a c:IsInterestedBy ?topic1 } " +
  "optional { ?aa c:IsInterestedBy ?topic filter(?aa = ?a) } " +
  "  filter(!bound(?aa)) }", 6));


//    suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select   where { ?x ?p ?y ?z ?q ?t filter (?x > ?y && ! ( ?x > ?y )) }",    0));
//
//    suite.addTest(new CoreseTest2(true, "testQuery", corese,
// "select  where { ?x ?p ?y ?z ?q ?t filter (?x = ?y && ! ( ?x = ?y )) } ",    0));
//
//
//    suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select   where { ?x ?p ?y ?z ?q ?t filter (?x != ?x) }",    0));


    suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select distinct  ?x ?y  where { { ?x c:isMemberOf ?y} union " +
      "{?y c:Include  ?x } filter(?x ~ 'corby')} ", 2));


 suite.addTest(new CoreseTest2(true, "testQuery", corese,
 "select where { <http://www.inria.fr/olivier.corby> c:FirstName 'Olivier' }", 1));

 suite.addTest(new CoreseTest2(true, "testQuery", corese,
 "select distinct ?name where {{ ?x c:FirstName ?name filter (?name = 'Olivier')} UNION "+
 "{?x c:FirstName ?name filter ( ?name = 'Olivier')} }",1));


 suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   distinct ?x where {" +
"graph data:model.rdf {optional{?x rdf:type c:Event}}} ", 1));



 suite.addTest(new CoreseTest2(true, "testQuery", corese,
 "select   where {filter(?p ^ owl:)    ?x  ?p ?y}",5));

 suite.addTest(new CoreseTest2(true, "testQuery", corese, 
		 "select where {graph ?src {?p rdfs:seeAlso ?src}}", 1));

 
 suite.addTest(new CoreseTest2(true, "testQuery", corese,
		 "select  where { rdfs:Class  rdf:type rdfs:Resource}", 2));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//   "select  *    where {" +
//   "  ?x c:FirstName ?name ?x c:hasCreated ?doc " +
//   " optional { ?y c:FirstName ?name2  ?y c:hasCreated ?doc2  ?y c:isMemberOf ?org " +
//   "  filter (?name2 < ?name && ?name != 'toto' && " +
//   "( ?name = 'toto' || (?x != ?doc && ?y != ?doc2) )) } } order by ?name limit 2  ",
//           2));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select * where {  ?x c:FirstName ?name  " +
     " optional { ?x c:hasCreated ?doc ?doc c:Title ?t ?x c:FamilyName ?fn " +
     "filter (?doc ~ 'knowledge' && ?fn ~ 'e' )} }", 761));

//   suite.addTest(new CoreseTest2(true, "testQuery", corese,
//  "select * where { optional {?x c:isMemberOf ?org}  ?x c:FirstName ?name " +
//  "filter (?name = 'Olivier') " +
//  " optional { ?x c:hasCreated ?doc ?x c:FamilyName ?fn filter (?org = ?name) }}", 15));

     suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select   where {?x c:isMemberOf ?org  ?x c:FirstName ?name " +
   " optional { ?x c:hasCreated ?doc ?x c:FamilyName ?fn filter ('toto' = 'Olivier' ) }} group by ?x", 15));

     
     
     
     
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:isMemberOf ?org  ?x c:FirstName ?name " +
        " optional { ?x c:hasCreated ?doc ?x c:FamilyName ?fn " +
        "filter (?name = 'Olivier' )}} group by ?x", 15));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select nosort   ?x ?name   where {" +
         "?x c:isMemberOf ?org ?x c:FirstName ?name ?x c:hasCreated ?doc}" +
         "order by ?name limit 2"
         , 2));

        

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//      "select  more ?s1 ?s2  " +
//      "    threshold 20   where { " +
//      "score ?s1 { ?x rdf:type c:Manager ?x c:hasCreated ?doc }   " +
//      "score ?s2 { ?topic rdf:type c:JavaProgrammingTopic  " +
//      "?x c:IsInterestedBy::?p ?topic }  filter (?s1 > 0.5 && ?s2 > 0.5) }" +
//      "order by desc(?s1)  desc(?s2)"
//, 7));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   more ?x  where { ?x c:IsInterestedBy ?topic ?topic rdf:type c:MusicTopic " +
"optional {?x c:hasCreated  ?doc} optional{ ?x c:isMemberOf ?org} } group by ?x", 54));



//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select     * where {?x c:FirstName 'Olivier' ?x rdf:type ?class " +
//" filter (?x <: ?class)} ", 4));


//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select    * where { ?x c:FirstName 'Olivier' ?x rdf:type ?class " +
//        " filter (?x <=: ?class)} ", 14));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select    distinct * where {" +
//        " <http://www.inria.fr/olivier.corby> c:FirstName 'Olivier' " +
//        " filter (<http://www.inria.fr/olivier.corby> <: c:Person)} ", 1));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select     * where {" +
//        "<http://www.inria.fr/olivier.corby> rdf:type ?class " +
//        " filter (<http://www.inria.fr/olivier.corby> <: ?class)} ", 4));


      suite.addTest(new CoreseTest2(true,"testQuery",corese,
      "select ?x where {?x c:FirstName 'Olivier' " +
      "filter ('true'^^xsd:boolean = 'true')}  ", 0));

      suite.addTest(new CoreseTest2(true,"testQuery",corese,
"select   ?x where {?x c:FirstName 'Olivier' " +
"filter( datatype('true'^^xsd:boolean) = xsd:boolean && " +
"xsd:string('true'^^xsd:boolean) = 'true')} ", 11));

        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x where {" +
        "?x c:FirstName 'Olivier' filter (xsd:float(1 + 1) = 2.0)}", 11));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?v where {" +
        "?x c:hasCreated ?doc filter (xsd:integer('a' ) = 12 )}", 0));

  
//        String[] aaa = {"http://www.telecom-italia.com/"};
//
//       suite.addTest(new CoreseTest2(true, "testBind", corese,
//       "select distinct ?org   where {" +
//       "?x c:isMemberOf ?org } order by desc(self(?org))", aaa));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select    distinct ?x ?y    where {" +
      "?x c:hasCreated ?d1  filter(?x ~ 'jean') " +
      "optional  {?y c:hasCreated ?d2  filter (?y ~ 'toto')} }  "  ,26));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select    distinct ?y    where {" +
        "?x c:hasCreated ?d1  filter(?x ~ 'jean') " +
        "optional  {?y c:hasCreated ?d2  filter (?y ~ 'toto')} }  "  ,1));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select    distinct ?y    where {" +
        "?x c:hasCreated ?d1  filter(?x ~ 'jean') " +
        "optional  {?y c:hasCreated ?d2  filter (?y ~ 'olivier')}}   " ,12));

//          suite.addTest(new CoreseTest2(true, "testQuery", corese,
//         "select    distinct ?y    where {" +
//         "?x c:hasCreated ?d1  filter(?x ~ 'jean') " +
//         "optional  {?y c:hasCreated ?d2  filter (?y ~ 'olivier')}   " +
//         "optional  {?y c:hasCreated ?d3  filter (?y ~ 'james') } }" ,12));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select    distinct ?y    where {" +
       "?x c:hasCreated ?d1  filter(?x ~ 'jean') " +
       "{select ?y where {optional  {?y c:hasCreated ?d2  filter (?y ~ 'olivier')}   " +
       		"optional  {?y c:hasCreated ?d3  filter (?y ~ 'james') } }  }}" ,12));
          
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select     distinct ?x  " +
//"from   data:model.rdf  " +
//"from named data2:f1.rdf " +
//"from named data2:f2.rdf" +
//"  where {" +
//" graph ?src2 { ?z c:FirstName ?nz  graph ?src3 " +
//"{ optional {?y c:FirstName ?ny  " +
//"graph ?src4 {?t c:FirstName ?nt} ?u c:FirstName ?nu}}} " +
//"?x c:FirstName ?nx }", 15));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select    distinct ?x " +
//"from   data:model.rdf from named data2:f1.rdf from named data2:f2.rdf " +
//" where {" +
//"?x c:FirstName ?nx  " +
//"graph ?src2 { ?z c:FirstName ?nz " +
//"optional {  graph ?src3 {?y c:FirstName ?ny}}}}", 15));


//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
// "select    distinct ?x  " +
// "from   data:model.rdf from named data2:f1.rdf from named data2:f2.rdf  where { " +
// " graph ?src2 { ?z c:FirstName ?nz  graph ?src3 {optional {?y c:FirstName ?ny}}} ?x c:FirstName ?nx}", 15));
//
//
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select    distinct ?src  " +
//"from   data:model.rdf from named data2:f1.rdf from named data2:f2.rdf  where { " +
//"graph ?src { ?x c:FirstName ?nx optional {graph ?src2 { ?y c:FirstName ?ny}}} }", 2));
//


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   distinct ?src  " +
"from named data:model.rdf from named data2:f1.rdf from named data2:f2.rdf where {" +
"graph ?src {} }", 3));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   distinct ?src  " +
"from named data:model.rdf from named data2:f1.rdf from named data2:f2.rdf where {" +
"graph ?src {optional {?x rdf:type c:Event}}} ", 3));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   distinct ?graph ?y " +
"from named data:model.rdf from named data2:f1.rdf from named data2:f2.rdf where {" +
"graph ?graph {?x rdfs:seeAlso ?src optional{?x c:FirstName ?nn1} " +
"optional {graph ?src2 {?x c:FirstName ?nn2}} " +
"graph ?src { optional { ?y c:FamilyName ?name ?y c:FirstName ?fn}}}} " , 15));



        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  distinct ?graph ?y " +
"from named data:model.rdf from named data2:f1.rdf from named data2:f2.rdf where {" +
"graph ?graph {?x rdfs:seeAlso ?src} " +
"graph ?src { optional { ?y c:FamilyName ?name ?y c:FirstName ?fn}}} " , 15));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select distinct ?graph ?y " +
"from named data:model.rdf from named data2:f1.rdf from named data2:f2.rdf where {" +
"graph ?graph {?x rdfs:seeAlso ?src } " +
"optional { graph ?src {?y c:FamilyName ?name }}}" , 15));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   distinct ?graph ?y ?z " +
"from  data2:f1.rdf from  data2:f2.rdf where {" +
"graph ?graph {?x rdfs:seeAlso ?src } " +
"optional { ?y c:FamilyName 'Bernard' ?z c:FirstName 'Olivier' }" +
"optional { ?t c:isMemberOf ?org}}", 5));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select   distinct ?graph ?y ?z " +
"from  data2:f1.rdf from  data2:f2.rdf where {" +
"graph ?graph {?x rdfs:seeAlso ?src}  " +
"optional { ?y c:FamilyName 'Bernard' ?z c:FirstName 'Olivier' }}", 5));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select    *  " +
"from  data2:f1.rdf from  data2:f2.rdf where {" +
" {?x c:FirstName ?name ?x c:FamilyName ?fname}}   " , 8));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select more where {" +
        "?x c:FirstName 'Olivier' optional {?x c:isMemberOf ?org}}", 12));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois') }   " +
" filter ( ! ( 'x' != 'x' ) ) } ", 4));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x ?p ?y filter (1 = 2) }", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois') }   " +
" optional { ?x c:hasCreated ?doc filter (?x = ?y  )}}  ", 4));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois') }   " +
" filter  (! bound(?z)) }  ", 4));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois') }   " +
" filter (! bound(?name) || ! bound(?z) )} ", 4));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois') }   " +
" filter ( bound(?z) ||  bound(?name) )} ", 4));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois') }   " +
" filter ( bound(?z) || ! bound(?name) )} ", 0));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois')   } " +
" filter (! bound(?name) || bound(?z) )} ", 0));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  where { { ?x c:FirstName ?name filter( ?name = 'Francois')   } " +
 " filter (! bound(?name))}   ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  where { { ?x c:FirstName ?name filter( ?name = 'Francois')   } " +
           " filter (! bound(?name) || bound(?name))}  ", 4));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {" +
        "{?x c:FirstName 'Olivier'} union {?y c:FamilyName 'Corby'}  " +
        "filter (?x ~ 'o' || ?y ~ 'o' )}", 13));

        // test the sorting of edge.
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select projection 1  where {" +
//"?x c:hasCreated ?doc1    ?z c:hasCreated ?doc2 ?y c:hasCreated ?doc3" +
//        " filter ( ?doc2 = ?doc1 &&   ?doc3 != ?doc1 && ?doc3 = ?x)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select   * where {" +
        "?x c:height ?h1  ?y c:height ?h2  filter ((?h1 / ?h2) = '1.5'^^xsd:decimal )}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select   distinct ?src     where { " +
        "graph ?src { ?x c:FirstName ?fn   } " +
        " } order by ?src limit 1000" +
        "" , 833));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select  distinct ?src     where { " +
      "graph ?src { ?x c:FirstName 'Olivier'   } " +
      "graph ?src2 { ?y c:FirstName 'Olivier'   }  filter (?src = ?src2) } order by ?src", 96));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?s   where {" +
        "graph ?s {?x ?p ?y} filter( ?x ~ 'olivier.corby') } order by ?s", 7));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?n2 where {?x c:FirstName ?n1  ?y c:FirstName ?n2  " +
        "filter(?n1 > 'K' && ?n1 < 'L'  &&   ?n1 > ?n2 && ?n2 > 'J' )} " +
        "", 55));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?n2 where { ?x c:FirstName ?n1  ?y c:FirstName ?n2  " +
        "filter(?n1 > 'K' && ?n1 < 'L' &&    ?n2 < ?n1 && ?n2 > 'J' )}" +
        " ", 55));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x where { ?x c:FirstName ?n1  ?y c:FirstName ?n2 " +
          "filter(?x <= ?y &&  ?x >= ?y && ?x ~ 'olivier') }", 12));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select distinct ?x where {?x c:FirstName ?n1  ?y c:FirstName ?n2 " +
         "filter(?x >= ?y &&  ?x <= ?y && ?x ~ 'olivier' )}", 12));








// ** here


      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select    distinct ?l1 where {" +
      "?x rdfs:label ?l1  ?y rdfs:label ?l1  " +
      "    filter(?x != ?y  )} order by ?l1 ",    38));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
           "select   distinct ?l1 where {" +
           "?x rdfs:label ?l1  ?y rdfs:label ?l1  " +
           " filter( ?x != ?y )} order by ?l1 ",    38));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {" +
        "?x c:isMemberOf  ?o1  ?y c:isMemberOf ?o2  " +
        "filter (?x != ?y && ?o1 = ?o2)}", 43));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x where {" +
          "?x c:isMemberOf  ?o1  ?y c:isMemberOf ?o1  " +
          "filter (?x != ?y )}", 43));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select   distinct sorted ?x ?y where {" +
       "?x rdfs:label ?l1  ?y rdfs:label ?l2  " +
       " filter( ?l1 = ?l2 && ?x < ?y  )} order by ?x ?y",    24));



        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select  distinct ?p ?a  where { " +
         " ?p rdf:type ?class   optional  { ?a ?p ?a } " +
         "filter( ?p ^ rdf:) }  ", 20));




        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?p     where  { ?q rdfs:subPropertyOf ?p " +
           " optional{  ?a ?p ?b } filter (! bound(?b)) }", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?src     where {" +
        " graph ?src{?a ?p ?a}  graph ?src {?b ?q ?b }}", 3));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select *       where { ?p ?p ?x }", 8));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select *       where { ?q rdfs:subPropertyOf ?p    ?a ?p ?a} ", 3));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select     distinct ?p  where { ?p ?q c:Person   ?x ?p ?y }", 8));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select   where { ?a ?p ?a  ?p rdf:type ?class   }", 6));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select   where {" +
        "<http://www.inria.fr/olivier.corby> ?r ?p  " +
        "optional{?a ?p ?a}}", 55));

        //  two groups at null used to crash : 10
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
                " select * where { " +
                "filter (?a = <http://www.inria.fr/olivier.corby>) ?a ?p ?v "+
                " optional {?x ?p 'toto' }    optional {?y ?p 'toto' } } " +
                " group by ?x ?y  ", 1));
 
        // test compiled constant marker
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select    *  where " +
          " {?x ?p 'true'^^xsd:boolean }", 1));
      
       suite.addTest(new CoreseTest2(true, "testQuery", corese,
           "select    *  where {" +
           " ?x ?p '1'^^xsd:boolean }", 1));

        // test constant marker
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
            "select    *   where {" +
            " ?x ?p  ?v filter ( ?v = 'true'^^xsd:boolean  )}", 1));

          // test constant marker
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select    *   where {" +
          " ?x ?p ?v  filter ( ?v = '1'^^xsd:boolean  )}", 1));

      // test && within =
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select    *  where {" +
     " ?x ?p ?v  " +
     "filter ( ?v = (  ( 'true'^^xsd:boolean  && 'true'^^xsd:boolean ) ))}", 1));

      // test ! within =
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select    *  where { " +
       " ?x ?p ?v  " +
       "filter ( ?v = ( ! ( 'false'^^xsd:boolean  && 'true'^^xsd:boolean ) ))}", 1));

      // test || within =
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select    *  where {" +
        " ?x ?p ?v  " +
        "filter ( ?v = (  'false'^^xsd:boolean  || 'true'^^xsd:boolean ) ) }", 1));

       // test language with literal
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdfs:label 'engineer'@en }", 1));

       //  language with literal
          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {?x rdfs:label 'engineer'@fr} ", 0));

        //  direct access with constant
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         " select   * where {<http://www.inria.fr/olivier.corby> ?p ?v "+
         " ?y ?q <http://www.inria.fr/olivier.corby> filter(  ?v = ?y)} ", 28));

        //  direct access with constant
//          suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        " select   * where {" +
//        " ?a ?p ?v filter (?a = <http://www.inria.fr/olivier.corby>) "+
//        " ?y ?q  ?b filter (?b = <http://www.inria.fr/olivier.corby> &&   ?v = ?y)}", 11));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
      " select   * where {" +
  " <http://www.inria.fr/olivier.corby> ?p ?v  "+
  " ?y ?q  <http://www.inria.fr/olivier.corby> filter (?v = ?y)}", 28));

          
          
        // option  with lonely filter with constant
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x c:FirstName 'Olivier' ?x c:FamilyName ?name " +
        "optional{filter(?name = 'Corby' )} }", 13));

        // test query relation sorting
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {?x ?p ?y  ?x c:hasCreated ?doc " +
        "?x c:isMemberOf ?org  ?x c:IsInterestedBy ?topic} ", 5));

        // tests datatype clashes
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:age ?age filter  (! ( '2004-01-01'^^xsd:date > 12 )) }", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:age ?age filter (?age >= '2004-01-01'^^xsd:date)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:age ?age filter (! (?age < '2004-01-01'^^xsd:date))}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:age ?age filter (?age > '2004-01-01'^^xsd:date)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:age ?age filter (! (?age <= '2004-01-01'^^xsd:date))}", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select  where {?x c:age ?age filter (  '2004-01-01'^^xsd:date >= ?age)}", 0));

     suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select  where {?x c:age ?age filter (! (  '2004-01-01'^^xsd:date < ?age))}", 0));

     suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select  where {?x c:age ?age filter (  '2004-01-01'^^xsd:date > ?age)}", 0));

     suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select  where {?x c:age ?age filter (! (  '2004-01-01'^^xsd:date <= ?age))}", 0));


          suite.addTest(new CoreseTest2(true, "testQuery", corese,
 "select  where {?x c:FirstName ?name filter (! ( '2004-01-01'^^xsd:date <= ?name))}", 0));

                 suite.addTest(new CoreseTest2(true, "testQuery", corese,
                 "select  where {?x c:FirstName ?name filter (! (  '2004-01-01'^^xsd:date < ?name))} ", 0));

                 suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select  where {?x c:FirstName ?name filter (! (  '2004-01-01'^^xsd:date > ?name))} ", 0));

                 suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select  where {?x c:FirstName ?name filter (! ('2004-01-01'^^xsd:date >= ?name))} ", 0));



        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name " +
        "filter (! (?name <= '2004-01-01'^^xsd:date ))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name " +
        "filter (! (?name < '2004-01-01'^^xsd:date ))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name " +
        "filter (! (?name > '2004-01-01'^^xsd:date ))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name " +
        "filter (! (?name >= '2004-01-01'^^xsd:date ))} ", 0));


          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  where {?x c:FirstName ?name filter (! (10 <= ?name))} ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  where {?x c:FirstName ?name filter (! (10 < ?name))} ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  where {?x c:FirstName ?name filter (! (10 > ?name))} ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  where {?x c:FirstName ?name filter (! (10 >= ?name))} ", 0));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name filter (! (?name <= 10))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name filter (! (?name < 10))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x c:FirstName ?name filter (! (?name > 10))} ", 0));

         suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  where {?x c:FirstName ?name filter (! (?name >= 10))} ", 0));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x ?p ?age filter (?age >= 45)}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x ?p ?age filter (! (?age < 45))}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x ?p ?age filter (?age > 44)}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x ?p ?age filter (! (?age <= 44))}", 1));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {graph ?src {?x ?p ?src}}", 31));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where {?x ?p ?x}", 3));


      suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select distinct ?x  where {" +
"?x rdf:type c:Engineer optional {?x c:isMemberOf ?org} } " +
"order by ?x ?org", 7));

query = "select   distinct ?t1 ?t2     where {"+
"?x c:hasCreated ?d1 ?y c:hasCreated ?d2 ?d1 c:Title ?t2 ?d2 c:Title ?t1 "+
"filter (?t1 < ?t2 && ?t1 > 'a' && ?t1 < 'z' && ?t2 > 'a' && ?t2 < 'z' )} " +
"order by  ?t1 desc( ?t2) limit 2";

       String[] ans = {"A  Markovian Model For Contour Grouping", "Xeve : an  Esterel  Verification Environment (Version v1_3) "};

      //  suite.addTest(new CoreseTest2(true, "testBind", corese, query, ans));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where{?x c:age ?age  filter (?age + xsd:integer( '1' ) = 46)}", 1));


          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x   where {" +
        "?x rdfs:comment ?l filter (langMatches(lang(?l),  'fr' ))} limit 1000", 544));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x  where {" +
        "?x rdfs:comment ?l filter (langMatches(lang(?l),  '*' ))} " +
        "limit 1000", 546));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x  where {?x rdfs:comment ?l filter ( lang(?l) )} " +
          "limit 1000", 546));

 
   
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        " select * where {<http://www.inria.fr/olivier.corby> rdf:type c:Person "+
        " filter  ( 1 = 1)} ", 4));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
       " select * where {<http://www.inria.fr/olivier.corby> rdf:type c:Person "+
       " filter  ( 1 = 0) }", 0));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        " select  * where { " +
        "<http://www.inria.fr/olivier.corby> rdf:type c:Person "+
       " optional {<http://www.inria.fr/olivier.corby> rdf:type c:Engineer " +
       " <http://www.inria.fr/olivier.corby> c:IsInterestedBy ?n }}"  ,
      4));


        


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where {<http://www.inria.fr/olivier.corby>  rdf:type c:Person " +
         "optional{ <http://www.inria.fr/olivier.corby>  rdf:type c:Person  " +
         " optional{?a ?q <http://www.inria.fr/olivier.corby>   " +
         "<http://www.inria.fr/olivier.corby>  rdf:type c:Engineer " +
         "<http://www.inria.fr/olivier.corby> c:FirstName ?n3}} " +
         "optional{<http://www.inria.fr/olivier.corby>  rdf:type c:Manager " +
         "<http://www.inria.fr/olivier.corby> c:FamilyName ?n4}} limit 1" ,
             1));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {optional{<http://www.inria.fr/olivier.corby>  rdf:type c:Person  " +
        " optional{<http://www.inria.fr/olivier.corby>  rdf:type c:Person}} " +
        "optional{<http://www.inria.fr/olivier.corby>  rdf:type c:Person}} " +
        "limit 1" ,
            1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where{optional{rdfs:Class rdf:type rdfs:Class}} ", 1));

      

   suite.addTest(new CoreseTest2(true, "testQuery", corese,
 "select where {" +
 "<http://www.inria.fr/olivier.corby>  rdf:type c:Person }" +
 "", 4));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select where {" +
       "optional{<http://www.inria.fr/olivier.corby> c:FamilyName ?n} " +
       "<http://www.inria.fr/olivier.corby> c:date ?d}", 2));

       suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select where {" +
       "optional{<http://www.inria.fr/olivier.corby>  c:FamilyName ?n " +
       "<http://www.inria.fr/olivier.corby> c:date ?d}}", 2));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {<http://www.inria.fr/olivier.corby>  c:FamilyName ?n " +
        "<http://www.inria.fr/olivier.corby> c:date ?d}", 2));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select *  where {?c direct::rdfs:subClassOf c:Person " +
//           " filter (depth(?c) = depth(c:Person) + 1) }", 4));
//
//
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where {?x rdf:type rdfs:Class filter (depth(?x) = depth(rdfs:Resource))} ", 1));


//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where {?p rdf:type rdf:Property " +
//        "optional {?q rdf:type rdf:Property filter  (cardinality(?q) > cardinality(?p))}"+
//        "filter (! bound(?q))}", 1));

        
        
//        query =
//        "select ?p cardinality(?p) as ?card max(?card) as ?max where {" +
//        "?p rdf:type rdf:Property } having(cardinality(?p) = ?max) ";
        
        query =
        	 "select ?p ?max where {" +
          	"{select (cardinality(?p) as ?card) (max(?card) as ?max) where {" +
          	"?p rdf:type rdf:Property }} " +
          "?p rdf:type rdf:Property filter(cardinality(?p) = ?max) " +
          "} limit 1";

        
       // suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));
        
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where {?x rdf:type rdfs:Class filter (depth(?x) > 11)}", 12));
//TODO: debug
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x ?p ?y optional{?y rdf:type ?class}" +
        " filter (! bound(?class) && ! isLiteral(?y))}", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select  * where {?x c:age ?age  filter (str(?age) = '45')} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select  * where {?x c:age ?age  filter (regex(?x , '.*corby.*' ))} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select  * where {?x c:age ?age  filter (! regex(?x, '.*corby.*' ))} ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
              "select  * where {?x c:age ?age  filter regex(?x, '.*cORby.*', 'mi' )} ", 1));

//         suite.addTest(new CoreseTest2(true, "testQuery", corese,
//         "select where {?x rdf:type rdfs:Class  filter (namespace(?x) = owl:)} ", 15));

//       suite.addTest(new CoreseTest2(true, "testQuery", corese,
//       "select where {?x ?p ?v  filter (  sqrt(?v) > 5) }", 1));

       suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select where {?c rdfs:label ?l  filter (?l ~ 'person' &&  lang(?l) = 'en')} ", 4));

//          suite.addTest(new CoreseTest2(true, "testQuery", corese,
//         "select   where {?x rdf:type c:Person  filter isMulti(?x) } ", 10));

//       suite.addTest(new CoreseTest2(true, "testQuery", corese,
//       "select * where { " +
//       "?x c:HasForActivity ?act ?x c:IsInterestedBy ?topic " +
//       "filter (similarity(?act, ?topic) > 0.028) }", 38));

      

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        " select where {c:Person rdfs:label ?l filter  (?l ~ 'person')} ", 2));

 
          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?p1   where { ?p1 rdf:type c:Engineer ?p2 rdf:type c:Engineer filter(?p1 != ?p2) " +
        "optional {filter (?t1 = ?t2) optional{?p1 c:IsInterestedBy ?t1} " +
        "optional{ ?p2 c:IsInterestedBy ?t2 }} }", 7));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?p1   where {?p1 rdf:type c:Engineer ?p2 rdf:type c:Engineer filter(?p1 != ?p2) " +
          "optional {filter (?t1 = ?t2 ) ?p1 c:IsInterestedBy ?t1  " +
          "?p2 c:IsInterestedBy ?t2 }}", 7));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select ?x  where {?x c:IsInterestedBy ?topic } limit 200", 122));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select ?x  where {?x c:IsInterestedBy ?topic " +
          "optional {filter bound(?org) filter bound(?name) ?x c:isMemberOf ?org " +
          " optional{ filter bound(?name) optional{ filter bound(?name) ?org c:Designation ?name}} " +
          " optional{ filter bound(?name) filter bound(?org) optional{?x c:FirstName ?name }}} } limit 1000", 122));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select ?x  where {?x c:IsInterestedBy ?topic " +
        "optional{ filter (! bound(?org)   && ?topic   && ! bound(?name))" +
        "   optional { ?x c:isMemberOf ?org  ?org c:Designation ?name}}} limit 200 ", 122));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select ?x   where {?x c:IsInterestedBy ?topic " +
      "optional{filter (! bound(?org))  filter (?topic)   filter (! bound(?name))   " +
      "optional{ ?x c:isMemberOf ?org  ?org c:Designation ?name}}} limit 200", 122));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x   where {?x c:IsInterestedBy ?topic " +
        "optional{filter (?topic)   filter (! bound(?name))   " +
        "optional{ ?x c:isMemberOf ?org  ?org c:Designation ?name}} } limit 200", 122));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select ?x  where {?x c:IsInterestedBy ?topic " +
      "optional{ optional{?x c:isMemberOf ?org  " +
      "optional{?org c:Designation ?name} filter (! bound(?name))  }} } limit 200", 122));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select ?x   where {"
      + "?x c:IsInterestedBy ?topic " +
      "optional{?x c:isMemberOf ?org "
              + " optional {?org c:Designation ?name  "
//              + "filter bound(?topic)  " +
       //             +  "optional{?org c:IsInterestedBy ?topic }"
              + "}" +
      " filter (! (bound(?name)) ) } } limit 200",
      122));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select ?x where {?x c:IsInterestedBy ?topic " +
      "optional{?x c:isMemberOf ?org  optional{?org c:Designation ?name} " +
      "filter bound(?name) }} limit 1000",
      	156));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x  where {?x c:IsInterestedBy ?topic " +
        "optional{?x c:isMemberOf ?org  optional{?org c:Designation ?name} " +
        "filter (! (! bound(?name)) )} " +
        "} limit 1000",
        156));


      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select ?x   where {?x c:IsInterestedBy ?topic " +
      "optional{?x c:isMemberOf ?org ?org c:Designation ?name}} limit 1000",
      156));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x  where {?x c:IsInterestedBy ?topic " +
        "optional{?x c:isMemberOf ?org  optional {?org c:Designation ?name} " +
        "filter (! bound(?name)  ) }} limit 200 ", 122));

//      suite.addTest(new CoreseTest2(true, "testQuery", corese,
//      "select where {?x  c:FamilyName ?name  " +
//      "filter  ( ?x <=: c:Researcher )} ", 12));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where {?x  c:FamilyName ?name  filter (  ?x <=: c:Researcher )" +
//        " optional{ ?x c:hasCreated ?doc}  " +
//        "filter (?doc <=: c:TechnicalReport)} ", 2));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where {?x  c:FamilyName ?name  filter (  ?x <=: c:Researcher )" +
//        " optional{ ?x c:hasCreated ?doc  " +
//        "filter( ?doc <=: c:TechnicalReport )}} ", 12));
//
//          suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where {?x  c:FamilyName ?name  " +
//        "filter (  ?x <=: c:Engineer && ! (?x <=: c:Researcher)) }", 4));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x where {?x c:FirstName ?name  ?x c:FamilyName ?fname " +
        "filter( ?fname = 'Corby') " +
        "optional{?x ?p ?y filter (! (?p ~ 'rdf')) }} ", 149));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x where {?x c:FirstName ?name  ?x c:FamilyName ?fname " +
          "filter (?fname = 'Corby' && ! (?name = ?fname) )}", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x  where {?x c:FirstName ?name  ?x c:FamilyName ?fname " +
          "filter (?fname = 'Corby' &&  (?name != ?fname))} ", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x where {?x c:FirstName ?name  ?x c:FamilyName 'Corby' " +
          "filter (?name != 'Olivier')} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x where {?x c:FirstName ?name  ?x c:FamilyName 'Corby' " +
          "filter (! (?name = 'Olivier'))} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where {?x c:FirstName ?name  ?x c:FamilyName 'Corby' " +
         "filter (! (! (?name = 'Olivier' )))} ", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {?x c:FirstName ?name  ?x c:FamilyName ?fam " +
          "filter  (! (?fam != 'Corby' ) && ! (! (?name = 'Olivier' )))} ", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x rdf:type c:Engineer " +
         "optional {?x c:IsInterestedBy ?topic ?topic rdf:type c:SocialScienceTopic}}", 7));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:test ?v1 filter (?v1)  optional{ filter (?v3)  ?x c:test2 ?v2 }}        ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
           " select where {?x c:test ?v1 filter (?v1)  optional { filter( ?v2)  ?x c:test2 ?v2 }}        ", 1));
           // test2 is false

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         " select distinct ?x where {?x c:FirstName 'Olivier' " +
         "optional {?x c:isMemberOf ?org optional {filter (?org ~ 'toto') " +
         " optional{ ?org c:Designation ?name}}}}", 11));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x c:FamilyName 'Corby'  filter (! 'false'^^xsd:boolean)}", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        " select where {?x c:FamilyName 'Corby'  filter ('false'^^xsd:boolean)}", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:FamilyName 'Corby'  filter ('true'^^xsd:boolean)}", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:FamilyName ?name  " +
          "filter (lang(?name) = 'en' || ?name ~'rby'  || ?name ~ 'Co' )}", 
          27));

          mod =  new Param() ; // for parameters for get:ui

          String[] val0 = {"http://www.inria.fr/olivier.corby"};

        mod.put("name", val0);
        
//        suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//        "select  where {  get:name  rdf:type c:Person  " +
//        "get:name ?p ?doc } group by any", 1));

          String[] val01 = {"http://www.inria.fr/olivier.corby"};
          String[] val02 = {"c:Researcher"};

          ( (Hashtable) mod).put("name", val01);
          ( (Hashtable) mod).put("type02", val02);
          
//          suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//              "select where { get:name::?x rdf:type get:type02  " +
//              "?x ?p ?doc } group by any ", 1));


          String[] val03 = {"http://www.inria.fr/olivier.corby"};
          String[] val04 = {"inria"};

         ( (Hashtable) mod).put("name", val03);
         ( (Hashtable) mod).put("doc", val04);
         ( (Hashtable) mod).put("doc_oper", "~"); // ?doc ~ data
         
//         suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//         "select where { get:name::?x ?p get:doc }", 11));
//
//          ( (Hashtable) mod).put("doc_oper", "!~"); // ?doc ~ data
//          suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//          "select distinct ?p where { get:name::?x ?p get:doc }", 6));


//          String[] val1 = {"c:Engineer", "c:Researcher"};
//          ( (Hashtable) mod).put("type1", val1);
//          suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//          " select where { ?x rdf:type get:type1  ?x rdf:type ?c }", 34));

//        String[] val2 = {"'Corby'", "'Dieng'"};
//          ( (Hashtable) mod).put("name1", val2);
//          suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//          " select where { ?x c:FamilyName get:name1 }  ", 3));

//        String[] val3 = {"c:Engineer"};
//     ( (Hashtable) mod).put("type2", val3);
//     suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//     " select where { ?x rdf:type get:type2  ?x rdf:type ?c }", 14));
//
//   String[] val4 = {"'Corby'"};
//     ( (Hashtable) mod).put("name2", val4);
//     suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//     "select where { ?x c:FamilyName get:name2  } ", 2));
//
//        String[] val5 = {"http://www-sop.inria.fr/"};
//           ( (Hashtable) mod).put("org", val5);
//           suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//           "prefix got: <http://www.inria.fr/acacia/corese/eval#>" +
//           " select where { ?x c:isMemberOf got:org }  ", 26));

//        suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//        " select where {?x c:isMemberOf  <http://www-sop.inria.fr/> } ", 20));

//        suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//        "prefix get:  <http://www.inria.fr/acacia/comma#>" +
//        "select where {   ?x rdf:type get:Engineer }", 7));

//          ((Hashtable)mod).put("label", "person@en");
//          suite.addTest(new CoreseTest2(true, mod, "testQuery", corese,
//         " select where { ?x rdfs:label get:label  } ", 1));

        suite.addTest(new CoreseTest2(true,  "testQuery", corese,
         " select where {?x rdfs:label 'person'@en }  ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "prefix cc:   <http://www.inria.fr/acacia/comma#>  " +
          "select where {   ?x rdfs:label ?l  " +
          "filter (?x = cc:Person )}", 6));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "prefix cc:   <http://www.inria.fr/acacia/comma#>   " +
          "select where {cc:Person rdfs:label ?l}", 6));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:FamilyName 'Corby'  ?x c:isMemberOf ?org  " +
          "?x c:FirstName ?name  " +
          "filter  (?name = 'toto' || ?org ~ 'inria' )} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:age  ?age   optional {?x c:isMemberOf ?org}" +
          "filter ( ( ?org ~ 'inria') ||  ?age <= 45)}  ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x c:age  ?age  filter ( ?age > 45 || ?x ~ 'corby')}", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
           "select where { ?x c:test4 ?v  filter (?v ~ 'abc')} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
           "select where { ?x c:test4 ?v  filter (?v = 'abc'^^c:test)} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:test4 ?v  filter( ?v != 'abcdef'^^c:test )}", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x c:test4 ?v}", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         " select distinct ?x where {?x ?p ?v filter ( ! ?v)}", 42));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x c:age  ?age  filter ( ?age)}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x c:age  ?age  filter ( ! ( ?age - 45))} ", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x c:age  ?age  filter ( ?age - 45) }", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where { ?x rdfs:comment ?l  " +
          "filter  (! lang(?l))} ", 32));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {?x c:test3 ?v  filter (?v)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {?x c:test3 ?v  filter (! ?v)}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select *    where {?x c:FirstName 'Olivier' optional { ?x c:FamilyName ?name } " +
          " filter (?x && ?name = 'James' )}", 0));


          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x  where {?x c:FirstName 'Olivier'  optional { ?x c:age ?age }  " +
        "filter (bound(?age) && ! ( ?age = 45))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x  where {?x c:FirstName 'Olivier' optional { ?x c:age ?age }  " +
        "filter   ( ?age != 45)} ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select ?x  where {?x c:FirstName 'Olivier' optional { ?x c:age ?age }  " +
         "filter ( !  ?age ) }", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x  where {?x c:FirstName 'Olivier' optional { ?x c:age ?age }" +
        "  filter  (! bound( ?age) )} ", 10));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select ?x  where {?x c:FirstName 'Olivier' " +
          "optional { ?x c:age ?age }  filter (?age)} ", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x  where {?x c:FirstName 'Olivier' optional { ?x c:age ?age } " +
        " filter ('true'^^xsd:boolean &&  ?age)} ", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select *    where {?x c:FamilyName 'Corby' optional { ?x c:FirstName ?name } "+
          " filter  (?name = 'James')} ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {{ { ?x c:FirstName ?name filter( ?name = 'Francois') } UNION  " +
          "{?x c:age ?age }}  " +
          "filter (?age = 45 && bound(?name) ) }  ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { { ?x c:FirstName ?name  filter(?name = 'Francois') } " +
        "UNION  {?x c:age ?age}   " +
        "filter (?age = 45 || bound(?name)) }  ", 5));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select where { { ?x c:FirstName ?name  filter(?name = 'Francois') } UNION  " +
       "{?x c:age ?age }" +
       "  filter( ?age = 46 || ?name = 'Francoise')}  ", 0));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
         " select where { ?x c:age ?age FILTER ( ! (?age = 45)  || ! (?age != 45) && ?age = 45)} ", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where { {?x c:FirstName 'Olivier' optional { ?x c:age ?age " +
        "FILTER  (! (?age = 45)  || ! (?age != 45) && ?age = 45) } } " +
        " union { ?x c:FamilyName 'Corby' optional { ?x c:age ?age " +
        "FILTER  (! (?age = 45)  || ! (?age != 45) && ?age = 45) } } }", 11));

//          suite.addTest(new CoreseTest2(true, "testQuery", corese,
//          "select where {?y c:FamilyName ?x " +
//          "FILTER (?x = 'Corby') FILTER ( ?x ^ 'C' &&  !(?x ^ 'a') &&  ?x ~ 'or' && !(?x ~ 'yy') " +
//          "&& ?x ~ 'rby'  && !(?x ~ 'toto' ) && ?y <: c:Person && ?y <=: c:Person )"+
//          "filter (! (?y <=: c:Document)) }", 2));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { ?x c:age ?age " +
        "FILTER ( datatype(?age) = xsd:integer && (  ?age + 1 < ?age   ||   ?age - 1 < ?age ) )} ", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
         "select where { ?x c:age ?age " +
         "FILTER ( ( ?age + 1 ) < ?age   ||   (?age - 1 < ?age ))} ", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?x where {  ?x c:age ?age " +
          "FILTER ( ! (?age != 45) ). ?x c:age ?age . ?x c:FirstName ?name . " +
          "FILTER (! ( ?age > 2 * ?age)) }", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {  ?x c:age ?age FILTER   ( ! (?age != 45) &&  ! ( ?age > 2 * ?age) ) }", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {  ?x c:age ?age " +
          "FILTER ( ! ( ! ( ?age != 45 )) ||  ( ! (?age != 45) &&  ! ( ?age > 2 * ?age))) }", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select where {?x c:FirstName 'Olivier'  ?x c:FamilyName ?n . " +
          "  FILTER (! (isBlank(?x )) && isURI(?x) && ! ( isLiteral(?x)) && "+
          " (bound(?x) ) && ! ( ! ( ?n = 'Corby' )) && " +
          " ! ( (?n = 'Corby' && ?n != 'Corby')  || ! (?n = 'Corby' ) || ! ( ?x = ?x))) }   ", 2));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { ?x c:FirstName 'Olivier'  ?x c:FamilyName ?n . " +
        "  FILTER (! isBlank(?x)  &&  isURI(?x) && ! (  isLiteral(?x)) && "+
        " (bound(?x)) && ! ( ! ( ?n = 'Corby' )) && " +
        " ! ( (?n = 'Corby' && ?n != 'Corby')  || ! (?n = 'Corby' ) || ! ( ?x = ?x)))  }  ", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { ?x c:FirstName 'Olivier'  ?x c:FamilyName ?n . " +
        "  FILTER (! (isBlank(?x)) &&  isURI(?x) && ! (isLiteral(?x)) && "+
         "  ! ( ! ( ?n = 'Corby' )) && " +
          " ! ( (?n = 'Corby' && ?n != 'Corby')  || ! (?n = 'Corby' ) || ! ( ?x = ?x)))  }  ", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
             "select  from  data:model.rdf  where {" +
             "graph data1:Olivier_Corby.rdf {?x c:FamilyName ?n1} ?y c:FamilyName ?n1}" +
             "group by ?x ", 1));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  FROM  data:model.rdf FROM NAMED data1:Olivier_Corby.rdf where {" +
        "graph ?src {?x c:FamilyName ?n1} ?y c:FamilyName ?n1} group by ?x", 1));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      " select   FROM NAMED data:model.rdf FROM NAMED data1:Olivier_Corby.rdf where {" +
      " graph ?src {?x c:FamilyName ?n1 ?x c:FirstName ?n2}} group by ?x", 16));

//      String[] answer={"Corby", "Olivier"};
//      suite.addTest(new CoreseTest2(true, "testBind", corese,
//      "select   ?fname ?name where {" +
//      "?x c:FamilyName ?fname filter( ?fname = 'Corby' ) ?x c:FirstName ?name } " +
//      "order by desc(?name)",
//      answer));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select where { ?x c:test ?v1 ?x c:test2 ?v2  filter (?v1 && ?v2)  }       ", 0));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select where { ?x c:test ?v1 ?x c:test2 ?v2  filter (?v1 || ?v2)   }      ", 1));

/*

*/

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "SELECT distinct ?src WHERE { " +
        "{GRAPH ?src { ?x c:FirstName 'Olivier' } } UNION   " +
        "{GRAPH ?src { ?y c:FamilyName 'Corby' }} } ", 96));


  
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select   where {" +
        "graph ?s1 {?x c:isMemberOf ?org graph ?s2 {?x c:hasCreated ?doc}}} group by ?x", 5));

  
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select   where {?x c:age ?age  filter(xsd:string(?age) = '45')}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
"select  distinct ?x where {"
+ "?x c:hasCreated ?doc  ?doc rdf:type c:TechnicalReport "+
"optional {"
   + "?x c:isMemberOf ?org  " +
     "optional {?org2 c:isMemberOf ?org3  ?org3 c:Designation ?des3} " +
     "filter(?org ~ 'http') "+
     "?org c:isMemberOf ?org2  "
   + "optional{"
         + "?org c:Designation ?des  " 
          //+ "optional{?org2 c:Designation ?des2}"
         + "}"
+ "}" +
"  ?x c:isMemberOf ?org} ", 2));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
           "select  distinct ?x where {?x c:hasCreated ?doc ?x rdf:type c:Researcher " +
           "optional{?x c:isMemberOf ?org  " +
           "optional{?org c:Designation ?des}}}", 4));
 
          suite.addTest(new CoreseTest2(true, "testQuery", corese,
               "select distinct ?x  where {?x c:isMemberOf ?org  " +
               "optional {?x c:hasCreated ?doc} " +
               "optional{ ?x c:IsInterestedBy ?topic}}", 46));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
// "select distinct ?x  where {"
//                + "?x c:hasCreated ?doc ?x rdf:type c:Researcher " +
// "{optional{?x c:isMemberOf ?org}} union {optional {?x c:hasCreated ?d2 ?d2 rdf:type c:TechnicalReport}} " +
// "{optional {?x c:isMemberOf ?o  filter(?o ~ 'atos')}} union {optional {?x c:isMemberOf ?o2 filter(?o2 ~ 'sophia')}} "
// + "}",
//            4));

}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?x where {" +
//        "?x c:hasCreated ?doc  ?x rdf:type c:Researcher ?doc c:Title ?title " +
//  "optional{filter(?t ~ 'knowledge') ?x c:hasCreated ?d2 filter(?t ~ 'knowledge') ?d2 c:Title ?t  " +
//  "filter (?t ~ 'knowledge') } " +
//  "filter (bound(?d2))} ", 3));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select distinct ?x where {?x c:hasCreated ?doc  ?x rdf:type c:Researcher ?doc c:Title ?title " +
      "optional{filter (?t ~ 'knowledge')   ?x c:hasCreated ?d2  " +
      "filter (?t ~ 'knowledge')  ?d2 c:Title ?t  " +
      "filter (?t ~ 'knowledge' )} filter (! bound(?d2))}", 1));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select distinct ?x where {?x c:hasCreated ?doc  ?x rdf:type c:Researcher ?doc c:Title ?title " +
      "optional{filter (?t ~ 'knowledge') ?x c:hasCreated ?d2 filter (?t ~ 'knowledge') ?d2 c:Title ?t  " +
      "filter (?t ~ 'knowledge' )}  filter (! bound(?d2) ||   ! bound(?d2) ) }", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdf:type c:Engineer  filter bound(?x)}", 7));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdf:type c:Engineer  filter (! bound( ?x))} ", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdfs:label ?l filter ( ?l = 'engineer'@en &&  ?l != 'engineer'@en)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdfs:label ?l filter (?l = 'engineer'@en && lang(?l)  = 'en')} ", 1 ));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdfs:label ?l  filter (?l = 'engineer' && lang(?l)  = 'fr')} ", 0 ));


        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdfs:label ?l   filter (?l = 'person'@en )}", 1));

  
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select distinct ?x where { ?x c:hasCreated ?doc ?doc c:Title ?title filter(?title ~ 'knowledge') "+
     " optional{?x c:isMemberOf ?org   ?org c:Designation ?name  " +
     "filter( ?name ~ 'acacia')}}", 10));


//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select distinct sorted ?p  where {" + 
//"?x rdf:type c:Manager ?x ?p ?y " + 
//" optional { ?p  rdfs:subPropertyOf::?q c:IsInterestedBy } filter (! bound(?q))}" +
//"order by ?p group by ?p", 15));


  
          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  ?y   where {filter(?x ~ 'olivier.corby') ?x ?p ?y}" +
        "group by ?x    ?p ", 14));


    suite.addTest(new CoreseTest2(true, "testQuery", corese,
    "select ?x (count (?x) as ?y) where {" +
    "?x rdf:type c:Archivist optional {?x c:hasCreated ?doc} } order by ?x  group by ?x", 1));
    


        suite.addTest(new CoreseTest2(true, "testQueryCount", corese,
        "select ?x  (count(?doc) as ?count) where {?x c:hasCreated ?doc}" +
        "group by ?x " +
        "order by desc( ?count)" +
        "  ", 100));
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select  where {?x c:isMemberOf ?org ?x rdf:type ?c} group by ?org   ?c", 128));
        
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select  where {?x c:isMemberOf ?org ?x rdf:type ?c } group by ?org", 13));

      //TODO 1
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
           "select  distinct  ?n where {" +
           "?x c:FirstName ?n   ?y c:FirstName ?n  filter( ?x != ?y )} limit 200", 127));
 
        suite.addTest(new CoreseTest2(true, "testRelation", corese,
           "select ?x where {?x c:FamilyName 'Corby'} ", 0));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x where {?x c:Designation ?name " +
        "filter (?name = 'Corby'  && ((?name != 'Corby' && ?name = 'toto') || ?name = 'Corby')) } ", 2));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {{?x c:Designation 'Olivier'} union " +
        "{?x c:Designation 'Corby' }}", 12));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {?x c:Designation ?name  " +
        "filter (?name = 'Olivier' || ?name = 'Corby' )}", 12));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x where {graph ?src  {?x ?p ?y} filter(   ?x ~ 'olivier.corby' )" +
        " optional{?src c:date ?date} } group by ?src", 7));


        if (true) {

          suite.addTest(new CoreseTest2(true, "testQueryCount", corese,
          "select ?x  (count (?src) as ?count)    where { " + 
          "  ?src rdf:type rdfs:Resource   graph ?src { ?x c:hasCreated ?y }}" +
          "group by ?x order by desc(?count)", 100));
 
          suite.addTest(new CoreseTest2(true, "testQueryCount", corese,
          "select  (count (?y) as ?count)   where {" +
          "  graph ?src {  ?x c:hasCreated ?y }}" +
          "group by ?x order by   desc(?count)", 100));
 
          suite.addTest(new CoreseTest2(true, "testQueryCount", corese,
          "select ?x  (count( ?src) as ?count)   where { " + 
          " graph ?src {?x c:hasCreated ?y} ?src rdf:type rdfs:Resource }" +
          "group by ?x order by desc(?count)", 100));
 
         suite.addTest(new CoreseTest2(true, "testQuery", corese,
          "select distinct ?src where {" +
          "?p rdfs:subPropertyOf* c:IsInterestedBy " +
          "graph ?src { ?x ?p ?topic}" +
          "" +
          "}", 11));
 
         suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		 "select where {graph ?src {?p ?p ?x}}", 9));
 
         suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		 "select ?src where {graph ?src  {?src ?p ?src}}", 1));

         suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		 "select where {graph ?src  {?src ?p ?src}}", 1));
 
         suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		 "select where {graph ?src {?p rdfs:seeAlso ?src}}", 1));
 
         suite.addTest(new CoreseTest2(true, "testQuery", corese,
              "select distinct ?src where {graph ?src  {?doc c:CreatedBy  ?x    ?x ?p ?y}  " +
              "filter(?x ~ 'olivier.corby')}", 6));
        }
        
        

        suite.addTest(new CoreseTest2(true, "testQueryCount", corese,
      "select ?x  (count (?y) as ?count)     where { " +
      "?x c:hasCreated ?y }" +
      "group by ?x    order by desc( ?count) limit 800 ", 100));


//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct sorted where {" +
//        "?x ?p ?y ?x ?p ?z filter(?y != ?z &&  ?x ~ 'olivier.corby')}", 37));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
       "select   where { ?x ?r ?p   filter (isLiteral(?p) && ?p ^ 'h')}" +
       "group by ?p", 42));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
       "select   where { ?x ?r ?p filter  (isLiteral(?p) && isBlank(?p) )}" +
       "group by ?p", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
       "select  where { ?p ?r ?x  filter  (isBlank(?p)  && ! isBlank(?p))}" +
       "limit 1000", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
       "select where {?p ?r ?x  filter ( isBlank(?p) && ! isBlank(?p) )}", 0));
//here
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        "select distinct ?p  where { ?p ?r ?x  filter  isBlank(?p) } limit 200", 151));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        "select where {?p ?p ?x}", 8));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct sorted where {" +
        "?p c:height ?i " +
        " filter ( 1 + ?i = ?i + 1  &&  ?i = ?i - ?i + ?i - 2 * ?i + 2 * ?i )}", 4));
        

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct sorted where {?p c:height  ?i  " +
        "filter (?i = ?i + (- ?i) + ?i )}", 4));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct sorted where { ?p c:height ?i  " +
        "filter (?i = ?i - ?i + ?i) }", 4));
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        "select  where { ?y ?p ?x  filter (?x = '2'^^xsd:integer) } group by ?x ",1));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select distinct sorted where { " +
       "?p c:height ?i ?p c:height ?l   filter ( 2 * ?i +  ?l = ?l + 2 * ?i) }", 16));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select distinct sorted where { " +
       "?p c:height ?i ?p c:height ?l ?p c:height ?d  filter ( ?i +  ?l < ?d )}", 4));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//       "select where {?c1 rdf:type ?cc1  ?c2 rdf:type ?cc2  " +
//       "filter ( ?c1 ^ rdfs: && ?c2 ^ owl: && ?c1 - rdfs: = ?c2 - owl: ) }", 2));
// 
//      suite.addTest(new CoreseTest2(true, "testQuery", corese,
//       "select where { ?c1 rdf:type ?cc1  ?c2 rdf:type ?cc2 " +
//       "filter( ?c1 ^ rdfs: && ?c2 ^ owl: && ?c1 = rdfs: + (?c2 - owl:) )}", 2));
       
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?p where {?p c:FirstName ?f  ?p c:FamilyName ?n " +
        "filter (! (?p ~ ?f + '.' + ?n))}", 9));
      
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
   "select distinct ?p where {?p c:FirstName ?f  ?p c:FamilyName ?n " +
   "filter (! (?p ~ (?f + '.' ) + ?n ))}", 9));
    	     
      
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?class rdfs:comment ?c " +
        "filter (?c ~ ' inanimate' + ' or ' && ?c ~ 'ina' + 'nimate' )  }", 1));
        
        
        

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?class rdfs:comment ?c " +
        		"filter (?c ~ ' inanimate' + ' or ')}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?class rdfs:comment ?c filter (?c = 10 + 10)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?class rdfs:comment ?c " +
        		"filter (?c ~ 'Whatever exists ')}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?class rdfs:comment ?c " +
        		"filter (?c ~ \"Whatever exists \")}", 1));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?class rdfs:comment ?c " +
        		"filter (?c ~ ' inanimate or '^^xsd:string)}", 1));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?class rdfs:comment ?c " +
        		"filter (?c ~ ' inanimate or ' )}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?class rdfs:comment ?c " +
"filter (?c ~ ' inanimate ur ' ||  (?c ~ ' inanimate ar ')  " +
"|| (?c ~ ' inanimate or '^^xsd:string)) }", 1));
 
        
        
  /** HERE **/ 
        
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select distinct sorted where { ?p c:height ?i ?p c:height ?l  " +
      "filter (?i = ?l / 2 &&  ?l = 2 * ?i  && ?i = ?l - 2 &&   ?l = 2 + ?l - 2)}", 1 ));
 
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select distinct sorted where { ?p c:height ?i ?p c:height ?l ?p c:height ?d  " +
       "filter (?d >= (2 * ?i +  3 * ?l) / 2)} ", 4));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
       "select distinct sorted where { ?p c:height ?i ?p c:height ?l ?p c:height ?d " +
       "filter ( ?d > ?i +  ?l)}", 4));
 
       suite.addTest(new CoreseTest2(true, "testQuery", corese, 
    		   "select ?x   where { ?x rdfs:label ?l ?x rdfs:label ?l " +
    		   "filter ( lang(?l) = 'en' )} limit 800 ", 711));
 
      suite.addTest(new CoreseTest2(true, "testQuery", corese, 
    		  "select where {?x rdfs:label ?l filter (?l = 12)}", 0));

      suite.addTest(new CoreseTest2(true, "testQuery", corese, 
    		  "select where {?x rdfs:label ?l filter (?l = 12)}", 0));

      suite.addTest(new CoreseTest2(true, "testQuery", corese, 
    "select * where {?x rdfs:label ?l1 filter(lang(?l1) = 'en')" +
    "?x rdfs:label ?l2 filter(lang(?l2) = 'fr') " +
    "filter(?l1 = ?l2)}", 0));
      
      
 
 
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select  where { ?doc c:Designation '2'^^xsd:long " + 
      " ?doc c:Designation ?x ?doc rdfs:comment ?y " + 
      " ?doc rdfs:comment  ?z filter (?x = ?y && ?y = ?z && ?y <= '3'^^xsd:long ) } group by ?doc ", 1));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select where {?doc c:Designation '2'^^xsd:long " + 
      " ?doc c:Designation '2'^^xsd:integer } ", 1));
      
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select where {?doc c:Designation ?v1   ?doc c:Designation ?v2  " +
      "filter(?v1 = '2'^^xsd:integer && ?v1 >= ?v2)}", 1));

      suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?doc where {?doc rdf:type c:Document ?doc c:Title ?title  " +
        "?doc c:CreatedBy ?p  ?p rdf:type c:Person ?p c:Designation ?des  " +
        "filter(?title ~ ?des && ?title ~ ?des  )}", 28));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?y c:BirthDate ?date " +
        		"filter(?date > '1959-10-16'^^xsd:date)}", 1));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?y c:date ?date filter( ?date > '1959-10-16'^^xsd:date)}", 1)); // **

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?x c:date ?d1 ?y c:date ?d2 filter(?x != ?y && ?d1 = ?d2)}", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?x c:date ?d ?y c:date ?d filter(?x != ?y)}", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?x c:date '1959-10-16'^^xsd:date}", 2));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese, 
        		"select where {?x c:date ?date filter(?date >= '1959-10-16'^^xsd:date)}", 3));
        
        
        
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?x ?y ?doc where {?x rdf:type c:Engineer ?y rdf:type c:Researcher " +
        "optional{?y c:hasCreated ?doc}} group by ?x " , 7));


 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {?x rdf:type c:Engineer ?y rdf:type c:Researcher " +
        "?y c:hasCreated ?doc}" , 7));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {?x rdf:type c:Engineer   ?y rdf:type c:Researcher}" , 7));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdf:type c:Engineer}" , 7));
 
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select more threshold 1  where {" +
//        "?pers rdf:type c:Engineer ?pers c:hasCreated ?doc ?doc rdf:type c:Abstract}" , 3));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select one where {{?x rdf:type c:Engineer } union { ?x rdf:type c:Researcher}}" , 18));

        
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
  "select distinct ?x where {?x c:hasCreated ?l ?x c:isMemberOf+ ?org ?pers ?pp ?x " +
  "?org c:Include+ ?pers  ?pers c:hasCreated ?doc}" , 5));

//             suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select  distinct ?x where {?x c:hasCreated ?l ?pers ?pp ?x ?org c:Include+ ?pers  " +
//        "?pers c:hasCreated ?doc ?x c:isMemberOf+ ?org}" , 5));

//             suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?x where {?x rdf:type c:Person filter(!(?x =: c:Person)) ?x c:hasCreated ?doc " +
//        "optional{?x c:isMemberOf ?org ?org c:Designation ?name}}"
//        , 5));
             
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//       "select distinct ?doc where {?doc c:CreatedBy ?a  ?doc c:Title ?t  filter(?t ~ 'knowledge') " +
//       "optional{?a c:IsInterestedBy ?topic} " + 
//       "optional { ?a c:IsInterestedBy::?q ?topic } filter (! bound(?q)) }", 3));
//
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?doc where {?doc c:CreatedBy ?a  ?doc c:Title ?t  filter(?t ~ 'knowledge') " +
//        "optional{?a c:IsInterestedBy ?topic1} " + 
//        "optional { ?a c:IsInterestedBy::?q ?topic2 } filter(! bound(?q))}", 3));
// 
        
        
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?doc where {?doc c:CreatedBy ?a  ?doc c:Title ?t  filter(?t ~ 'knowledge')  " +
        "optional{?a c:IsInterestedBy ?topic ?a c:isMemberOf ?team}}", 4));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x c:Designation <http://www.inria.fr>}", 1));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x c:Designation 'http://www.inria.fr'}", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x c:Designation 'http://www.inria.fr'   " +
        "?x c:Designation ?d1 filter( lang(?d1) = 'en' )      " +
        "?x c:Designation ?d2   filter(?d1 = ?d2)}", 1));

             suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x ?p 'http://www.inria.fr'}", 2));
             
             
 
             suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x ?p 'http://www.inria.fr'^^xsd:string}", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x ?p 'http://www.inria.fr'^^rdf:XMLLiteral}", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x ?p ?l   " +
        " filter( lang(?l) = 'en' || lang(?l) = 'fr' ) " +
        "filter (?l = 'http://www.inria.fr'^^rdf:XMLLiteral)}", 0));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { ?x ?p 'http://www.inria.fr/acacia/comma#Person'}", 0));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { ?x ?p <http://www.inria.fr/acacia/comma#Person>}", 785));
        
        
        
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select distinct ?x where {?x rdf:type c:Person ?x c:age \"45\"^^xsd:integer  " +
        "?x c:FirstName \"?Olivier\"^^xsd:string " +
        "?x c:FirstName \"\"\"Olivier\"\"\" " +
        "}", 1));
 
       
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?a ?p ?x  filter(?x > '1'^^xsd:string && ?x < '2'^^xsd:string) } group by ?x", 71));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdf:type xsd:string  filter(?x > '1'^^xsd:integer)}", 0));
        
       
        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where { ?p rdf:type c:Person " +
        "?p rdf:type ?c } group by ?c limit 1", 1));
        
         
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?p where { ?p rdf:type owl:TransitiveProperty    " +
//        "?x c:SomeRelation::?p ?y }",0));
//        
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?p where { ?p rdf:type owl:TransitiveProperty   " +
//        "?p rdf:type ?c  ?x c:SomeRelation::?p ?y }",0));
//        
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  ?doc where { ?doc rdf:type c:Document  ?doc c:Title ?t  " +
        "filter(?t ~ \"knowledge\") }", 8));
        
//        suite.addTest(new CoreseTest2(true, "testCountConcept", corese,
//        "select  ?doc where { ?doc rdf:type c:Document  ?doc c:Title ?t  " +
//        "filter(?t ~ \"knowledge\") }", 4));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select where { ?x rdf:type c:Person  ?x direct::rdf:type ?p  " +
//        "filter( ?p = c:Engineer) }", 7));
 
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?p where { ?x rdf:type c:Person    " +
//        "optional{?x c:isMemberOf::?p ?org }}", 2));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "prefix cc:  <http://www.inria.fr/acacia/comma#> " +
        "select where {?p rdf:type cc:Engineer}", 7));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?p where { ?doc ?p ?v  " +
        "filter(?p ^ 'http://www.inria.fr/acacia/comma#')  " +
        "?doc c:Title ?t filter(?t ~ \"knowledge\") } ", 46));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select ?p where {?doc c:SomeRelation::?p ?v ?doc c:Title ?t " +
//        "filter(?t ~ \"knowledge\") } ", 21));
//        
//        
        

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select ?doc  where {?doc c:CreatedBy ?a " +
        "?doc c:Title ?t filter ( ?t ~ \"knowledge\" ) } group by ?a", 10));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where { ?x rdfs:label 'engineer'@en }", 1));

        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?x rdfs:label ?l filter (lang(?l) = 'fr'  &&  ?l ~ \"engineer\") }", 0));
     
//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select more where {?doc c:Title ?t filter(?t ~ \"knowledge\") " +
//        "?doc c:CreatedBy ?p ?p rdf:type c:Engineer}", 9));
//    
        suite.addTest(new CoreseTest2(true, "testQueryCount", corese,
        "select  (count(?doc) as ?count) where {" +
        "?doc c:Title ?t filter(?t ~ \"knowledge\" ) } ", 4));
        
        
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select where {?doc c:Title ?t filter(?t ~ \"knowledge\")" +
        " ?doc c:CreatedBy ?p}", 13));

        
//        suite.addTest(new CoreseTest2(true, "testQuery",corese,
//        "select where {?c direct::rdfs:subClassOf c:Document}", 36));
//        
       
        
//        suite.addTest(new CoreseTest2(true, "testQuery",corese,
//        "select distinct ?x ?y where {  ?x ?p  ?y " +
//        "filter( ?y ~ \"corby\") }", 33));
        
        suite.addTest(new CoreseTest2(true, "testQueryCount",corese,
        "select  (count (?doc) as ?count)  where { ?doc c:CreatedBy ?p " +
        "filter ( ?p ~ \"corby\" )  } group by ?p ", 7));
        
//        suite.addTest(new CoreseTest2(true, "testQuery",corese,
//        "select * where { ?x c:FamilyName ?name " + 
//        " optional { ?x c:FamilyName::?q ?y } filter(! bound(?q)) }", 0));
//        
        suite.addTest(new CoreseTest2(true, "testQueryCount",corese,
        "select (count (?doc) as ?count) where { ?x c:FamilyName ?name filter(?name = \"Corby\" ) " +
        "?x c:hasCreated ?doc } group by ?x", 5));
        
        
        
        suite.addTest(new CoreseTest2(true, "testQuery",corese,
        "select where { ?x c:FamilyName ?name " +
        "filter( ?name = \"Corby\" || ?name = \"Giboin\" ) }", 3));
        
//        suite.addTest(new CoreseTest2(true, "testQuery",corese,
//        "select where { ?x c:FamilyName ?name " +
//        "{filter(?name = \"Corby\" || ?name = \"Giboin\")} union " +
//        "{?x c:FirstName \"Rose\" filter(?name = \"Dieng\") }} ", 4));

   
      suite.addTest(new CoreseTest2(true, "testQuery",corese,
        "select where { ?x c:Designation \"Olivier\" " +
        "{?x rdf:type c:Person} union {?x rdf:type c:Organization}}", 16));
 
        suite.addTest(new CoreseTest2(true, "testQuery",corese,
        "select distinct ?x where { ?x rdf:type c:Person  " +
        "{{?x c:FamilyName \"Olivier\"} union {?x c:FirstName \"Olivier\"}} " +
        "{{?x c:isMemberOf ?org} union {?x c:IsInterestedBy ?t}} }", 1));

        suite.addTest(new CoreseTest2(true, "testQuery",corese,
       "select distinct ?x where { ?x rdf:type c:Person  " +
       "{?x c:FamilyName \"Olivier\"} union {?x c:FirstName \"Olivier\"  ?x c:isMemberOf ?org} " +
       "{?x c:isMemberOf ?org} union {?x c:IsInterestedBy ?t} } ", 1));
 
//        suite.addTest(new CoreseTest2(true, "testQuery",corese,
//        "select * where { ?x rdf:type c:Person ?x c:isMemberOf ?org " + 
//     " { { optional { ?x c:IsInterestedBy::?p1 ?t } filter(! bound(?p1)) } union " + 
//        "{ optional { ?x c:isMemberOf::?p2 ?a }    filter (! bound(?p2)) } } } ", 0));


        suite.addTest(new CoreseTest2(true, "testQuery",corese,
     "select where { ?x rdf:type c:Person  ?x c:FamilyName \"Corby\" } ", 6));
        
        
        
        

        suite.addTest(new CoreseTest2(true,"testQuery",corese,
        "select distinct ?x where {?x c:isMemberOf ?org   ?x c:IsInterestedBy ?t    " +
        "?t rdf:type c:JavaProgrammingTopic}", 5));

        suite.addTest(new CoreseTest2(true,"testQuery",corese,
        "select where {?x rdf:type c:Engineer  ?x c:FamilyName ?n " +
        "filter(  ?n >= \"Olivier\")}", 1));
 
        
       
        
        
//        suite.addTest(new CoreseTest2(true,"testQuery", corese,
//        "select distinct ?x where { ?x rdf:type c:Person  ?x c:isMemberOf ?org " + 
//        " optional { ?x c:IsInterestedBy::?q ?t } filter(! bound(?q))  " +
//        " ?t rdf:type c:JavaProgrammingTopic }", 0));
//
//        suite.addTest(new CoreseTest2(true,"testQuery",corese,
//        "select * where { ?x rdf:type c:Person  ?x c:FirstName \"Olivier\" " + 
//        " optional { ?x c:isMemberOf::?q ?org  } filter (! bound(?q)) " +
//        "optional{?x c:IsInterestedBy ?t}   ?t rdf:type c:JavaProgrammingTopic }", 11));

        suite.addTest(new CoreseTest2(true,"testQuery", corese,
        "select where { ?x rdf:type c:Person  ?x c:SomeRelation ?y " +
        "filter(datatype(?y) = xsd:string)   filter(?y ~ \"x\") }", 24));

        // here **
//   suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select distinct ?x where { ?x rdf:type c:Person  ?x c:Designation ?n  ?org c:Include ?x " + 
//" optional { ?x c:IsInterestedBy::?q ?t2 ?t2 rdf:type c:NetworkTopic } filter (! bound(?q))   " + 
// " optional { ?x c:IsInterestedBy::?q2 ?t ?t rdf:type c:ProgrammingTopic } filter (! bound(?q2))  }", 8));

//        suite.addTest(new CoreseTest2(true, "testQuery", corese,
//        "select distinct ?x where { ?x rdf:type c:Person  ?x c:Designation ?n  ?org c:Include ?x " + 
//        " optional {?x c:IsInterestedBy@?q ?t2  ?t2 rdf:type c:NetworkTopic } filter (! bound(?q)) " + 
//        " optional{?x c:IsInterestedBy ?t} ?t rdf:type c:ProgrammingTopic } ", 1));

  
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
        "select  where { ?d rdf:type c:Document  " +
        "?d c:Title ?title filter (  ?title ~ \"knowledge\" ) " +
        " ?d c:CreatedBy ?p1 ?d c:CreatedBy ?p2 } group by ?p1  ?p2", 44));
 
        suite.addTest(new CoreseTest2(true, "testQuery", corese,
 "select distinct ?p1 ?p2  group ?p1 group ?p2  where { ?d rdf:type c:Document  ?d c:Title ?title  filter(?title ~ \"knowledge\" )" + 
        "  ?d c:CreatedBy  ?p1  ?d c:CreatedBy  ?p2 }", 44));
        
       
      suite.addTest(new CoreseTest2(true, "testQuery", corese,
      "select distinct ?doc where { ?doc rdf:type c:Document " +
      "optional { ?doc c:Concern ?topic} ?doc c:CreatedBy ?pers "+
      "?doc c:Title ?title filter (?title ~ 'knowledge' || ?title ~ 'connaissance') " +
      "?pers c:Designation ?des }", 5));

          suite.addTest(new CoreseTest2(true, "testQuery", corese,
     "select \n" +
     "# last query \n" +
     "where { ?doc c:CreatedBy ?p  ?doc c:Title ?title  ?p rdf:type c:Person " +
     "?p c:Colleague ?r  ?p c:Designation ?n  " +
     "?r rdf:type c:Person  filter (?r ~ 'dieng') }", 0));

//    suite.addTest(new CoreseTest2(true, "testEngine",    corese,
//    "select distinct ?doc where { ?doc c:CreatedBy ?p  ?doc c:Title ?title  ?p rdf:type c:Person " +
//     "graph ?s {?p c:Colleague ?r}  ?p c:Designation ?n  " +
//     "?r rdf:type c:Person  filter (?r ~ 'dieng') }",7));
//    
//    
//
//     suite.addTest(new CoreseTest2(true, "testQuery", corese,
//     "select ?pers where { ?org  rdfs:seeAlso ?pers " +
//     "?pers c:HasForActivity  ?act ?pers rdf:type c:Person }", 29));
//
//     suite.addTest(new CoreseTest2(true, "testQuery", corese,
//     "select where { ?x c:FirstName 'Olivier' " +
//     "?x c:FirstName 'Laurent'^^xsd:string  " +
//     "?x c:FirstName 'Bernard'^^xsd:string  " +
//     "?x c:age '42'^^xsd:integer ?x c:FirstName 'OK' }", 2));
// 
//     suite.addTest(new CoreseTest2(true, "testQuery", corese,
//     "select distinct sorted * where { ?x c:Colleague ?y } limit  200", 115));
//     
//     suite.addTest(new CoreseTest2(true, "testQuery", corese,
//    	"select distinct ?p where { graph cos:engine { ?x ?p ?y } }", 11));
//     
////     suite.addTest(new CoreseTest2(true, "testQuery", corese,
////    	"select distinct ?pers where {  ?org  rdfs:seeAlso ?pers " +
////    	"filter byRule(?pers) } ", 29));
////     
//     suite.addTest(new CoreseTest2(true, "testQuery", corese,
//"select distinct ?y where { graph cos:engine { ?x ?p ?y } " +
//"filter(isBlank(?y)) }", 0));
//     
//     query = "construct {?s ?p ?o} where " +
//		"{?p ?s ?o filter(isURI(?p) && !(?p <=: rdf:Property))}  projection 1 ";
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 1));



//query = "add {?x c:FirstName ?n graph ?g { ?x c:age ?age } } "+ 
//"select ((?name + '.' + ?fname) as ?n) "+
//"((12) as ?age) where { "+
//"graph ?g {	?x c:FamilyName ?fname filter(?fname = 'Corby' ) } " +
//" ?x c:FirstName ?name }";
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 0));

//query = 
//	"select distinct ?x where {" +
//	"?x c:FirstName ?fname  filter(?fname = 'Olivier.Corby' )" +
//	"graph ?g " +
//	"{ ?x c:FirstName ?nn ?x c:FirstName ?nono " +
//	"graph ?gg { ?x c:age ?age }"+
//	"}" +
//	"}";
//  	
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2));

//query = "add {?x c:total ?val} select * (sum(?age) as ?val) " +
//		"where {?x c:age ?age} group by ?x";
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2));


//query = "select * where {?x c:total ?t}";
//
//suite.addTest(new CoreseTest2(true, "testQuery", corese, query, 2));

}   
     
      }}


      return suite;
    }






    public static void main(String[] args)
    {
    	String[] testSuiteName = {TestKgram.class.getName()};
    	Date d1=new Date();
    	TestRunner.main(testSuiteName);
    	Date d2=new Date();
 
    } 
} 
