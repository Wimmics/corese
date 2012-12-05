//package test.kgraph;
//
//import java.util.Date;
//
//
//
//
//
//import fr.inria.acacia.corese.api.IDatatype;
//import fr.inria.acacia.corese.cg.datatype.CoreseBoolean;
//import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
//import fr.inria.acacia.corese.exceptions.EngineException;
//import fr.inria.edelweiss.kgenv.eval.QuerySolver;
//import fr.inria.edelweiss.kgenv.parser.NodeImpl;
//import fr.inria.edelweiss.kgram.core.Mapping;
//import fr.inria.edelweiss.kgram.core.Mappings;
//import fr.inria.edelweiss.kgraph.core.Graph;
//import fr.inria.edelweiss.kgraph.logic.Entailment;
//import fr.inria.edelweiss.kgraph.query.QueryProcess;
//import fr.inria.edelweiss.kgraph.rule.RuleEngine;
//import fr.inria.edelweiss.kgtool.load.Load;
//import fr.inria.edelweiss.kgtool.load.RuleLoad;
//import fr.inria.edelweiss.kgtool.print.JSONFormat;
//import fr.inria.edelweiss.kgtool.print.RDFFormat;
//import fr.inria.edelweiss.kgtool.print.XMLFormat;
//import fr.inria.edelweiss.kgtool.print.XSLTQuery;
//
//public class Test {
//	
//	public static void main(String[] args){
//		new Test().process();
//	}
//	
//	void process(){
//		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
//		String file = "file://" + data + "test.xml";
//		
//		String path = "file://" + data;
//		
//		QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");
//		QuerySolver.definePrefix("data",  path + "comma/");
//		QuerySolver.definePrefix("data1", path + "comma/data/");
//		QuerySolver.definePrefix("data2", path + "comma/data2/");
//
//		DatatypeMap.setLiteralAsString(false);
//
//		Graph graph = Graph.create(true);
//		graph.set(Entailment.RDFSRANGE, true);
//		graph.set(Entailment.RDFSSUBCLASSOF, !true);
//		//graph.set(Entailment.RDFSSUBPROPERTYOF, !true);
//
//		Load loader =  Load.create(graph);
//		
//		long t1 = new Date().getTime();
////		loader.load(data + "kgraph/rdf.rdf", Entailment.RDF);
////		loader.load(data + "kgraph/rdfs.rdf", Entailment.RDFS);
//		//loader.load("http://www.w3.org/2000/01/rdf-schema#");
////		loader.load(data + "meta.rdfs");
//		
////		loader.load(data + "comma/comma.rdfs");
////		loader.load(data + "comma/commatest.rdfs");
//		loader.load(data + "comma/testrdf.rdf");
////		loader.load(data + "comma/model.rdf", "http://www.test.fr/g1" );
////		loader.load(data + "comma/data");
////		
////		//loader.load(data + "kgraph/path.rdf");
////
////		loader.load(data + "comma/data2");
////		loader.load(data + "kgraph/tmp.rdf");
//
//		
//		//loader.load(data + "alban/opmo-20101012.owl");
//		
////		loader.load(data + "kgraph/GeologicalDatingOntology.owl");
//		//loader.load(data + "comma/data2");
//		
//		// ontology in snd graph
////		Graph onto = Graph.create(true);
////		onto.getInference().set(Entailment.RDFSSUBCLASSOF, !true);
////		ProducerImpl ponto =  ProducerImpl.create(onto);
////		Load loader2 =  Load.create(onto);
////		loader2.load(data + "comma/comma.rdfs");
//
//				
//		//System.out.println(graph.getIndex());
//		
//		//System.out.println(graph.getInference().display());		
//		long t2 = new Date().getTime();
//		System.out.println(graph);
//		System.out.println(graph.size());
//
//		System.out.println((t2-t1) / 1000.0 + "s");
//		
//		t1 = new Date().getTime();
//		graph.init();
//		t2 = new Date().getTime();
//		System.out.println(graph);
//		System.out.println((t2-t1) / 1000.0 + "s");
//		
//		
//		/**
//		 * parse  : 2.85
//		 * create : 1.62
//		 * entail : 2.12
//		 */
//
//
//		
//		String query;
//		
//		query = 
//			"select distinct ?x where {" +
//			"?x ?p ?y" +
//			"} limit 10" ;
//		
//		query = 
//			"select debug  * where {" +
//			"graph ?g {" +
//			"?x rdfs:label ?l " +
//			//"filter(?l = 'web site'@en) " +
//			"?y rdfs:label ?l " +
//			"filter(?x < ?y)" +
//			"}" +
//			"} order by ?l" ;
//		
//		query = 
//			"select debug  * where {" +
//			"?x rdfs:label 'web site'@en " +
//			"?x rdfs:comment ?c" +
//			"}" ;
//		
//		query = 
//			"select debug  * where {" +
//			"graph ?g {" +
//			"?x rdfs:label ?l " +
//			"?y rdfs:label ?l " +
//			"filter(?x < ?y)" +
//			"}" +
//			"} order by ?l" ;
//		
//		query = 
//			"select debug  * where {" +
//			"graph ?g {" +
//			"?x rdfs:label 'web site'@en " +
//			"?x ?p ?y " +
//			"?x ?p ?y " +
//			"}" +
//			"}" ;
//		
//		
//		
//		
//		
//		
//		query = 
//			"prefix data: </home/corby/workspace/coreseV2/src/test/resources/data/comma/data2/>" +
//			"prefix c: <http://www.inria.fr/acacia/comma#>" +
//			"select debug * " +
//			"from data:f578.rdf " +
//			"from data:f720.rdf " +
//			"from data:f451.rdf " +
//			"from data:f335.rdf " +
//			"where {" +
//			"?x c:FirstName ?name; c:FamilyName ?lname" +
//			"}" ;
//		
//		
//		
//		query = 
//			"prefix c: <http://www.inria.fr/acacia/comma#>" +
//			"select  debug * where {" +
//			"graph ?g " +
//			"{" +
//			"?x c:FirstName ?name " +
//			"filter(?lastName = 'Corby') " +
//			"?x c:FamilyName ?lastName " +
//			"?doc c:CreatedBy ?x " +
//			"?doc c:Title ?title " +
//			"}" +
//			"}" +
//			"order by ?title " +
//			"limit 10" ;
//		
//		
//		
//		//select (sum(?age) as ?sum) {?x c:age ?age}
////select ?x (count(?doc) as ?count) where {?x c:hasCreated ?doc} group by ?x having (?count > 50)
//
//		
//
//		
//
//query =
//		"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select (sum(?age) as ?sum) {?x c:age ?age}";
//
//query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
//		"select  * where {{?x ?r ?x     }  " +
//		"{select ?x ?y ?q where {  " +
//		"{select * where {{select * (self(?p) as ?q) where {?x ?p ?x}}}}}}} " +
//		"limit 1";
//		
//		query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
//				"select  * where {" +
//				"?x c:FirstName ?n " +
//				"?x c:IsInterestedBy::?q ?t" +
//				"}" +
//				"" ;
//		
//		query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
//		"select * where {" +
//		"{select ?x (count(?doc) as ?count) where { ?doc c:CreatedBy ?x} " +
//		"group by ?x having(?count > 20)} " +
//		"?x c:FirstName ?name " +
//		"}  order by desc(?count) limit 10 ";
//
//query = 
//	"select * where {" +
//	"graph ?g {?p ?p ?x minus {?q ?q ?y}}" +
//	"}";
//
//query =			
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select  ?x ?n where {" +
//		"{select * where {?x c:FirstName ?n}} " +
//		"filter( exists {?x c:FamilyName ?m})" +
//		"}";
//
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select   * " +
//	"from named </home/corby/workspace/coreseV2/src/test/resources/data/comma/testrdf.rdf> " +
//	"where {" +
//	"graph ?g " +
//	"{" +
//	"?x c:FirstName ?n " +
//	"?y c:hasCreated ?doc" +
//	"optional {?z ?p rdfs:label}" +
//	"}" +
//	"}" ;
//
//
//query = 
//	"prefix data: </home/corby/workspace/coreseV2/src/test/resources/data/comma/data2/>" +
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select debug * " +
//	"from named data:f578.rdf " +
//	"from named data:f720.rdf " +
//	"from named data:f451.rdf " +
//	"from named data:f335.rdf " +
//	"where {" +
//	"graph data:f578.rdf {?x ?p ?name }" +
//	"graph ?g1 {?y ?q ?lname filter(?name = ?lname)}" +
//	"}" ;
//
//
//query = 
//	
//"select * where {" +
//	"{select " +
//		"(xpath(rdfs:, '/rdf:RDF//rdfs:label/text()') as ?label) " +
//		"where {} " +
//	"}" +
//	"?x rdfs:label ?label" +
//"} order by ?label";
//
//query = 	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select  * " +
//"where {" +
//"graph ?g " +
//"{" +
//"?x c:FirstName ?n " +
//"?doc c:FamilyName ?z " +
//"}" +
//"}";
//query = 	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select * where {" +
//"?x rdfs:label ?l " +
//"filter(regex(?l, 'engineer'))" +
//"filter(regex(?l, ?l))" +
//"}" ;
//
//query = 	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select  * " +
//"where {" +
//"graph ?g " +
//"{" +
//"?x rdf:first ?l " +
//"?y rdf:rest*/rdf:first ?l" +
//"}" +
//"}" ;
//
//query = 	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select debug * " +
//"from </home/corby/workspace/coreseV2/src/test/resources/data/comma/data/article_madrid.rdf>" +
//"from </home/corby/workspace/coreseV2/src/test/resources/data/comma/data/article_pakm.rdf>" +
//"from named </home/corby/workspace/coreseV2/src/test/resources/data/comma/model.rdf>" +
//"where {" +
////"graph ?g " +
//"{?doc c:CreatedBy ?x }" +
//"graph ?g " +
//"{" +
//"?y c:FirstName|c:FamilyName ?name" +
//"}" +
//"}" ;
//
//query = 
//	"select debug * where {" +
//	"?x ?p ?y ?y ?p ?y " +
//	"}";
//
//
//query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select * where { " +
//		"graph ?g {" +
//			"optional { ?x rdf:rest*/rdf:first ?y  } " +
//			"filter(! bound(?y))" +
//			"?x c:hasCreated ?doc }" +
//		"} limit 1";
//
////select * where { graph ?g {optional { ?x rdf:rest*/rdf:first ?y  } filter(! bound(?y))?x c:hasCreated ?doc }} limit 1
////select * where { graph ?g {not { ?x rdf:rest*/rdf:first ?y  } ?x c:hasCreated ?doc }} limit 1
////select * where { graph ?g {optional { ?x rdf:rest*/rdf:first ?y  } filter(! bound(?y))not { ?x rdf:rest ?y  } ?x c:hasCreated ?doc }} limit 1
//
//
//query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select * " +
//		"from named <file:/home/corby/workspace/coreseV2/src/test/resources/data/comma/testrdf.rdf>" +
//		"where { graph ?g {not { ?x rdf:rest*/rdf:first ?y  } ?x c:hasCreated ?doc }} ";
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select * {" +
//	"graph ?g {?x rdf:type c:Person" +
//	//"; rdf:type ?class filter(?class != c:Person)" +
//	"}" +
//	"" +
//	"}" ;
//
//query = 
//"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select distinct * " +
////"from named </home/corby/workspace/coreseV2/src/test/resources/data/comma/testrdf.rdf>" +
//"where {" +
//" {?class (^rdfs:subClassOf)+ c:Person } " +
////"graph ?g {?x rdf:type ?class} filter(?c != ?class)" +
//"} " ;
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select distinct ?a ?p ?b " +
//	//"from named </home/corby/workspace/coreseV2/src/test/resources/data/comma/testrdf.rdf>" +
//	"where {" +
//	" {?x  (! (rdf:type| rdfs:subClassOf)) :: $path c:Person } " +
//	//"graph ?g {?x rdf:type ?class} filter(?c != ?class)" +
//	"graph $path {?a ?p ?b }" +
//	"} " ;
//
//query = "select  debug   distinct ?s1 ?p (count(?s1) as ?count)  where {  "+
//"graph ?s1 {?x ?p ?x}  optional {?u ?q ?r}  filter (?p = ?q && ?x = ?t) } order by ?s1 group by ?s1 ";
//
////select * (count(*) as ?c)  where {{select * where {?x rdf:rest*/rdf:first ?y }}minus {?x rdf:first ?y} filter(! exists{?x rdf:first ?y}) filter(?y  in (?x , ?y))} 
//
////testQuery(test.kgram.CoreseTest2): select * from named <file:/home/corby/workspace/coreseV2/src/test/resources/data/comma/testrdf.rdf>where { graph ?g {} } :  expected:<1> but was:<869>
////testQuery(test.kgram.CoreseTest2): 
//
//query ="select * where { _:b1 ?p _:b2  _:b2 ?p _:b1   }"; // 51
//
//query =	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select * where { ?x ?p ?y  ?y ?p ?x  filter(?p != c:SomeRelation) }"; // 51
//
//query = "select debug  distinct sorted ?x ?y where {" +
//		"?x rdfs:label ?l1  ?y rdfs:label ?l2  " +
//		" filter( ?l1 = ?l2 && ?x != ?y  )" +
//		"} " ;
//
//query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
//"describe ?x where {" +
//"?x c:FirstName 'Olivier' " +
//"}";
//
//
//
//query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
//"describe ?x where {" +
//"graph ?g {?x c:FirstName 'Olivier'} " +
//"}";
//
//query = 
//	"construct {?x rdfs:subClassOf ?z}" +
//	"where {" +
//	"?x rdfs:subClassOf [rdfs:subClassOf ?z]" +
//	"}";
//
//
//
////testQuery(test.kgram.CoreseTest2): select    where {?x ?p ?src  graph ?src {?a ?q ?b}} group by ?x :  expected:<1> but was:<30>
////testQuery(test.kgram.CoreseTest2): select distinct sorted where {?x ?p ?y ?x ?p ?z filter(?y != ?z &&  ?x ~ 'olivier.corby')} :  expected:<37> but was:<0>
//
//
////testValue(test.kgram.CoreseTest2): select (count(distinct *) as ?c) where {[ c:FirstName ?name ;c:isMemberOf ?org ]}"; 
//
////"select * (count(*) as ?c)  where {{select * where {?x rdf:rest*/rdf:first ?y }}minus {?x rdf:first ?y} filter(! exists{?x rdf:first ?y}) filter(?y  in (?x , ?y))} 
//
//// TODO
//query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select debug  *  where { " +
//		"?p rdfs:range ?range filter(?p != c:SomeRelation) ?x ?p ?y  filter(exists{?y rdf:type ?cc}) " +
//		"optional {?y rdf:type ?class filter (?class = ?range) } " +
//		"optional {?y rdf:type ?class2  ?class2 rdfs:subClassOf ?range } " +
//		"filter ( ! bound(?class) && ! bound(?class2)) }";
//
//
//query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select debug (count(distinct *) as ?c) where {[ c:FirstName ?name ;c:isMemberOf ?org ]}"; 
//
//
//query = "select * (count(*) as ?c)  where {" +
//		"{select * where {?x rdf:rest*/rdf:first ?y }} minus {?x rdf:first ?y} " +
//		"filter(! exists{?x rdf:first ?y}) filter(?y  in (?x , ?y))} ";
//
//
//query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select debug  *  where { " +
//		"?p rdfs:range ?range  ?x ?p ?y  filter(exists{?y rdf:type ?cc}) " +
//		"optional {?y rdf:type ?class filter (?class = ?range) } " +
//		"filter ( ! bound(?class)) }";
//
//query =
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select distinct ?class  ?xml where { " +
//	" c:Person rdfs:subClassOf ?class" +
//	"{select (xpath('" +
//	data + "comma/comma.rdfs', 'rdf:RDF/rdfs:Class/@rdf:ID' " +
//	") as ?xml) {}}" +
//	"}" ;
//
//
//
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select debug * " +
//	"where {" +
//	"?x c:isMemberOf ?org " +
//	"?x c:hasCreated ?doc " +
//	"?x c:FamilyName ?name " +
//	"filter(?name = 'Corby')" +
//
//	"}";
//
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select debug * " +
//	"where {" +
//	//"?a rdf:first|rdf:rest ?b " +
//	"?x rdf:rest*/rdf:first :: $path ?y " +
//	"graph $path {?a ?p ?b}" +
//	
//	"}";
//
//query = 
//	"select debug * " +
//	"(year(?date) as ?year)" +
//	"(month(?date) as ?month)" +
//	"(day(?date) as ?day)" +
//	"(hours(?date) as ?h)" +
//	"(minutes(?date) as ?m)" +
//	"(seconds(?date) as ?s)" +
//	"(strlen('abc') as ?len)" +
//	"(substr('abc', 2) as ?str)" +
//	"(ucase('abc') as ?u)" +
//	"(lcase('ABC') as ?l)" +
//	"(concat(?u, ' : ', ?l) as ?con)" +
//	"(ends(?u, ?u) as ?true)" +
//	"(ends(?u, ?l) as ?false)" +
//	"(rand() as ?r1)" +
//	"(rand() as ?r2)" +
//	"(abs(-2) as ?a1)" +
//	"(abs(-2.5) as ?a2)" +
//	"(round(2.7) as ?r)" +
//	"(floor(2.5) as ?f)" +
//	"(ceiling(2.7) as ?c)" +
//
//	"where {" +
//	"?x ?p ?date filter(datatype(?date) = xsd:dateTime) " +
//	"filter(contains(datatype(?date), 'date'))" +
//	"filter(starts(datatype(?date), xsd:))" +
//	"}";
//
//query = "select " +
//		"(concat('a'@en, 'b'@en) as ?a) " +
//		"(concat('a'@en, 'c', 'b'@en) as ?b) " +
//		"(concat('a'@en,  'b'@fr) as ?c) " +
//		"(concat('a'@en, 'c', 'b'@fr) as ?d) " +
//		"where {" +
//		"" +
//		"}";
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select debug * " +
//	"where {" +
//	"?p1  owl:inverseOf ?p2 " +
//	"graph ?g1 {?x ?p1 ?d}" +
//	"graph ?g2 {?x ?p1 ?d ?d ?p2 ?x}" +
//	"filter(?g1!=?g2)" +
//	"} limit 1" ;
//
//query = 
//	"prefix c: <http://www.inria.fr/acacia/comma#> " +
//	"prefix fun: <function://fr.inria.edelweiss.kgraph.test.Test>" +
//	"prefix ext: <function://fr.inria.edelweiss.kgramenv.util.Extension>" +
//	"select debug * (ext:self(?x) as ?y )" +
//	"(ext:equalsIgnoreAccent('é', 'e') as ?bool )" +
//	"where {" +
//	//"graph ?g " +
//	"{" +
//	"?x c:namez ?name " +
//	//"?x c:name 'Jojo' " +
//	"?x rdf:type c:PhysicalEntity" +
//	"?x rdf:type ?class" +
//	"}" +
//	"}" ;
//
//
//query =
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select debug * " +
//	"(xsd:integer(?val) as ?int) " +
//	"(encode_for_uri('http://www.test.fr?x=a&?y=<') as ?uri)" +
//	"(now() as ?now)" +
//	"(md5('test') as ?md5)" +
//	"(sha1('test') as ?sha1)" +
//	"(sha224('test') as ?sha224)" +
//	"(sha256('test') as ?sha256)" +
//	"(sha384('test') as ?sha384)" +
//	"(sha512('test') as ?sha512)" +
//	"(regex('test', 'est') as ?bool)" +
//	"where {" +
//	"{select (xpath('" + file + "', '/book') as ?book) where {}}" +
//	"{select (xpath(?book, 'num[@rdf:datatype]/text()') as ?val) where {}}" +
//	"?x c:FirstName ?n }"  ;
//
//query =
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//	"select debug * {" +
//	"?x c:FirstName ?n , 'Olivier' " +
//	//"filter('toto' != 'toto'@en) " +
//	"} " ;
//query = 	"prefix c: <http://www.inria.fr/acacia/comma#>" +
// "select (max(?age + ?age) as ?max) where {?x c:age ?age}";
//
//
//query = "select   distinct ?graph ?y " +
//		"from named data:model.rdf " +
//		"from named data2:f1.rdf " +
//		"from named data2:f2.rdf " +
//		"where {" +
//		"graph ?graph {?x rdfs:seeAlso ?src optional{?x c:FirstName ?nn1} " +
//		"optional {graph ?src2 {?x c:FirstName ?nn2}} " +
//		"graph ?src { optional { ?y c:FamilyName ?name ?y c:FirstName ?fn}}}} ";
//
//query = "select * where { _:b1 ?p _:b2  _:b2 ?p _:b1   }";
//
//
//query = "select * where { ?b1 ?p ?b2  ?b2 ?p ?b1   } limit 50";
//
//
//query = "prefix cc: <http://www.inria.fr/acacia/comma#>" +
//		"select   * where { " +
//		"graph ?g { ?doc cc:CreatedBy ?x  ?x cc:FamilyName ?name  filter( ?name = 'Corby' )} " +
//		"filter(?name = xpath(?g, '/rdf:RDF//*[cc:FamilyName = \"Corby\" ]/*/text()' ))" +
//		"{select (xpath(?g, '/rdf:RDF/*/cc:Title/text()' ) as ?title) where {}}" +
//		"} ";
//
//query = "select debug distinct ?x  where {" +
//		"?x c:isMemberOf  ?o1 " +
//		"?y c:isMemberOf ?o1 " +
//		"filter(?x ~ 'cselt')" +
//		" filter (?x != ?y )" +
//		"} order by ?x";
//
//
//query = "select debug  ?x  where {" +
//"?x rdf:type c:Person " +
//"?x c:FirstName ?n" +
//		"}" ;
//
//query =
//	"prefix c: <http://www.inria.fr/acacia/comma#>" +
//"construct {" +
//"?x c:FamilyName 'Olivier' ; rdf:type c:Person " +
//"graph ?g {" +
//"?x rdfs:seeAlso _:b }" +
//"_:b rdfs:seeAlso ?x " +
//"" +
//"rdfs:seeAlso rdfs:range c:Person " +
//"rdfs:seeAlso rdfs:subPropertyOf c:top " +
//"" +
//"} where {" +
//"graph ?g {?x c:FirstName 'Olivier'} " +
//"filter(! exists {?x rdfs:seeAlso ?y})" +
//"}";
//
//String squery = "prefix c: <http://www.inria.fr/acacia/comma#>" +
//"select  * where {" +
//"graph ?g " +
//"{?x ?p ?y; c:top ?z}" +
//"}";
//
//
//
//query = "prefix geo: <http://www.ifp.fr/GeologicalDatingOntology#>" +
//"select * where {" +
//"?class rdf:type owl:Class" +
//"?class rdfs:subClassOf [?p ?r ]" +
//"}" +
//"order by ?p";
//
//query = 
//	"construct {?x ?p ?z} "+
//	"where { ?p rdf:type owl:TransitiveProperty  "+
//		"?x ?p [?p ?z] "+
//		"filter(! exists{?x ?p ?z}) "+
//		"}";
//
//
//
//query = "select ?x ?y  (min(pathLength($path)) as ?l) where {" +
//		//"?x rdf:type c:Person " +
//		//"?x c:FirstName 'Albert' " +
//		//"?x c:FirstName 'Olivier' " +
//		"?x rdf:type c:Person " +
//		"filter(isBlank(?x))" +
//		"?x  s (c:SomeRelation+) :: $path ?y " +
//		"?y rdf:type c:Person" +
//		//"?x rdf:type ?class " +
//		"} group by ?x ?y ";
//
//query = 
//	"select * " +
//	//"(exists {?x c:FirstName 'Olivier'} as ?b) " +
//	"where {" +
//	"?x c:FirstName ?n" +
//	"}" +
//	//"order by exists {?x c:FirstName 'Olivier'}" +
//	//"group by exists {?x c:FirstName 'Olivier'}" +
//	"group by ?x "+
//	"having( exists {?x c:FirstName 'Olivier'})" +
//	"";
//
//
//query = 
//"SELECT ?P (COUNT(?O) AS ?C)"+
//		"WHERE { ?S ?P ?O }";
//
//
//query = "select ?any (pathLength($path) as ?l ) where {" +
//"c:Document (^rdfs:range/rdfs:domain?)+ :: $path ?any " +
////"filter(pathLength($path) >= 3 )" +
//"}";
//
//query = 
//	"select * " +
//	"(count(self(?x)) + sum(xsd:integer(?a)) as ?z) " +
//	"(if ( sum(xsd:integer(?a)) < 100, sum(xsd:integer(?a)) , ?a)  as ?val)" +
//	"(group_concat(?fn, '.', ?ln; separator='') as ?id) " +
//	"where { " +
//	"graph ?g {?x c:age ?a} " +
//	"?x c:FirstName ?fn; c:FamilyName ?ln" +
//	"}" ;
//
//query = 
//	"select ?x " +
//	"(sum(xsd:integer(?a)) as ?sum)" +
//	"(if (?x = <http://www-sop.inria.fr/acacia/personnel/corby/>, " +
//		"100, sum(xsd:integer(?a)) )  as ?val)" +
//	"where {" +
//	"?x c:age ?a" +
//	"}" +
//	"group by ?x" ;
//
////query = new W3CTest11KGraph().read(data + "semsna/test2.rq");
//
//query = 
//	"select * (count(distinct ?y) + count(distinct ?x) as ?count) where {" +
//	"?x c:SomeRelation ?y" +
//	"}" +
//	"group by any";
//
//
//
//query = 
//	"select * ((pathLength($path)) as ?l) where {" +
//	"?x  sa (c:SomeRelation+) :: $path ?y" +
//	"}" ;
//
//query = 
//	"select * (min(pathLength($path)) as ?l) where {" +
//	"?x   (c:SomeRelation+) :: $path ?y" +
//	"}" +
//	"group by ?x ?y " ;
//
//query = "select debug * (count(distinct ?x) as ?count) " +
//		//"(exists {?class rdfs:subClassOf ?class } as ?b) " +
//		"where {" +
//		"?x rdf:type ?class " +
//		"}" +
//		"group by ?class (exists {?class rdfs:subClassOf ?class } as ?t) " +
//		"order by desc(?count) exists {?class rdfs:subClassOf ?class } " +
//		"having (exists{?class rdfs:subClassOf ?sup } ) " +
//		"limit 10" ;
//
//query = "select * where {" +
//		"{select distinct ?x where {?x rdf:type/rdfs:subClassOf* c:Person}}" +
//		"}" +
//		"order by ?x";
//
//query = 
//	"insert { graph ?g { ?x c:age ?age } ?x c:FirstName ?n } "+ 
////"select * where { "+
//"where {" +
//"graph ?g {	?x c:FamilyName ?fname filter(?fname = 'Corby' ) } " +
//" ?x c:FirstName ?name " +
//"{select ((?name + '.' + ?fname) as ?n) ((12) as ?age) where {}}"+
//"}";
//
//query  =
//	"select *   where {" +
//	"" +
//	"{select ?x ?y (min(pathLength($path)) as ?l) (count($path) as ?nb) " +
//	"where {" +
//	"?x    (^rdfs:domain/rdfs:range)+ :: $path ?y" +
//	"}  group by ?x ?y }" +
//	//"filter(?nb > 1)" +
//	"}" +
//	"order by desc(?l)" ;
//
//
//
//query = "select * where {" +
//		"graph ?g {?c rdfs:label 'engineer'@en}" +
//		"}";
//
//query = "select more   *   (kg:similarity() as ?sim) where {" +
//"?x c:FirstName 'Olivier' " +
//" ?x rdf:type c:Person " +
//"} " +
//"order by ?sim";
//
//
//
//query = "select * " +
//		"from named <http://www.test.fr/g1> " +
//		"where {graph ?g {?x c:FirstName* ?n}} limit 1";
//
//query = "select * where {" +
//		"?x rdf:rest* ?y" +
//		"?a ?p ?x filter(! strstarts(?p, rdf:))" +
//		"}";
//
//
//query = 	
//	"select * (pathLength($path) as ?l) where {" +
//	"c:Document (^rdfs:range/rdfs:domain?)*::$path ?any " +
//	"filter(pathLength($path) = 3 )" +
//	"graph $path {?a ?p ?b}" +
//	"} limit 3 ";
//
//
//query = "select * " +
//		"from named <" + path + "/comma/comma.rdfs> " +
//		"where {" +
//		"graph ?g {?x rdfs:label* ?l }" +
//		"} limit 10";
//
//query = "select debug * " +
//		//"from named <http://www.test.fr/g1>" +
//		"where {" +
//		"graph ?g {?x c:FirstName ?y} " +
//		"filter(?g = <http://www.test.fr/g1>) " +
//		"}";
//
//
//
//query =	"select distinct ?p     where  { ?q rdfs:subPropertyOf ?p  " +
//		"optional{  ?a ?p ?b } filter (! bound(?b)) }"; 
//
//
//query =	"prefix c: <http://www.inria.fr/acacia/comma#> "+
//	"select  * where {" +
//	"?y c:Colleague ?x " +
//	//"?x ?p ?y" +
//	"{select * where { {select * where {{select * where {?y ?p ?x}}}}}}" +
//	"} limit 1 ";
//
//
//
//query = "select * " +
//"from  <http://www.test.fr/g1>" +
//"where {" +
//" {?x c:FirstName{0} 'Olivier'}" +
//"}";
//
//query = "select * " +
//"where {" +
//"graph ?g {?x c:FirstName{0} 'Olivier'}" +
//"}";
//
//query = "select * " +
//"from named <http://www.test.fr/g1>" +
//"from named <http://ns.inria.fr/edelweiss/2010/kgraph#default>" +
//"where {" +
//"graph ?g {?x c:FirstName{0} 'Olivier'}" +
//"}";
//
//query = "select * " +
//"from  <http://www.test.fr/g1>" +
//"from  <http://ns.inria.fr/edelweiss/2010/kgraph#default>" +
//"where {" +
//" {?x c:FirstName{0} 'Olivier'}" +
//"}";
//
//query = "select * " +
//"from named <http://www.test.fr/g1>" +
//"where {" +
//" graph ?g {?x c:FirstName{0} 'Olivier'}" +
//" graph ?g {?y c:FirstName{0} 'Olivier'}" +
//"" +
//"}";
//
//
//query = "select * " +
//"where {" +
//"graph <http://www.test.fr/g2> {?x c:FirstName{0} 'Olivier'}" +
//"}";
//
//query = "select * " +
////"from  <http://www.test.fr/g1>" +
////"from  <http://ns.inria.fr/edelweiss/2010/kgraph#default>" +
//"where {" +
//
////" graph ?g " +
//"{" +
//"?x c:FirstName* ?n   filter(?x ~ 'Olivier')}" +
//"}";
//
//query = "select distinct ?c1 ?c2 where {" +
//"?p rdfs:domain ?c1 ; rdfs:range ?c2" +
//"}" +
//"limit 1000";
//
//query = "select  * where {" +
//		"?c1 (^rdfs:domain/rdfs:range){1,5} ?c2" +
//		"}";
//
//
//query = "select debug  " +
////"(min (pathLength($path)) as ?l) " +
//"where {" +
//"{?c1 c:SomeRelation ?c2} union " +
//"{?c1 c:SomeRelation [ c:SomeRelation ?c2]}" +
//"} " 
//+"group by ?c1 ?c2" ;
//
//query = "select debug  " +
////"(min (pathLength($path)) as ?l) " +
//"where {" +
//"?c1 c:SomeRelation{1,3} :: $path ?c2" +
//"} " 
//+"group by ?c1 ?c2" 
//;
//
//
//// 240361 4.848s
//
//
//
//query = "select debug  " +
////"(min (pathLength($path)) as ?l) " +
//"where {" +
////"select * where {" +
//"{?c1 c:SomeRelation ?c2} union " +
//"{?c1 c:SomeRelation [ c:SomeRelation ?c2]} union " +
//"{?c1 c:SomeRelation [ c:SomeRelation [ c:SomeRelation ?c2]]} " +
////"} " +
//"} " 
////+"group by ?c1 ?c2" 
//;
//
//// 240360 26.94s min()
//// 240360 22.006s length()
//// 613501 19.177s no group by
//
//
//query = "select debug  " +
////"(min (pathLength($path)) as ?l) " +
//"where {" +
//"?c1 c:SomeRelation{1,3}  ?c2" +
//"} " 
////+"group by ?c1 ?c2" 
//;
//query = "select debug * (rand() as ?r) where {" +
//"service <test> {?x c:FirstName ?n}" +
//"}" 
//+
//"bindings ?n { ('Olivier') ('Paul') ('Alain') }";
//
//query = 
//	"select debug * " +
//	"from named data:comma.rdfs " +
//	"where {" +
//	"graph  ?g {" + 
//	"{?x c:FirstName* 'toto' }" +
//	//"?x rdfs:label ?y " +
//	"}" +
//	"} limit 1";
//
//
//query = "select  * where {graph ?g { ?x c:FirstName 'Olivier' }" +
//"?g  xpath('/rdf:RDF//c:FirstName/text()') 'Olivier' " +
////"?g  xpath('/rdf:RDF//*/@rdf:about') ?x " +
//"} limit 1";
//
//
//query = "select * where {" +
//		"?x c:name 'Albert' " +
//		"}";
//
//String update  = "insert data {" +
//		"<a> c:Include <b> " +
//		"c:Include rdf:type owl:SymmetricProperty" +
//		"}";
//
//query  = "select * where {" +
//		"?x c:Include ?y" +
//		"}";
//
////loader.load(data + "antiquity/all");
//
//update = "prefix db: <http://dbpedia.org/ontology/> " +
//"insert data {db:spouse rdf:type owl:SymmetricProperty}";
//
//query = "prefix p: <http://dbpedia.org/property/>"+
//"prefix db: <http://dbpedia.org/ontology/>"+
//"prefix r: <http://dbpedia.org/resource/>"+
//"select * where {"+
//"?x (db:father|db:mother|db:spouse)+  :: $path r:Claudius "+
//"filter(?x ~ 'Nero')"+
//"graph $path {?a ?p ?b}"+
//"}";
//
//update = 
//	"insert data {" +
//	"<John> c:name 'John' " +
//	"graph <g2> {c:name rdfs:domain c:Person " +
//		"c:Person rdfs:subClassOf c:Animal}" +
//	"}" 
//	;
//
//query = "select * where {" +
//		"graph ?g {?x rdf:type c:Animal; rdf:type ?class }" +
//		"}";
//
//String update2 = 
//	"delete data {c:name rdfs:domain c:Person} ;" +
//	
//	"delete {graph kg:default {?x ?p ?y}}" +
//	"where  {graph kg:default {?x ?p ?y}}" ;
//
//query = "select * where {" +
//"graph kg:default {?x ?p ?y }" +
//"}";
//
//query = "select * where {" +
//		"graph ?g {?x c:Designation ?n; ?p ?n}" +
//		"}" ;
//
//query = "select * where {" +
//		"?x ! (^ rdf:type | rdf:type) ?y" +
////		"?x ^ (! rdf:type) ?y" +
//		//"?x ^  ( ^ rdf:type) ?y" +
//
//		"} limit 10";
//
//query = "select * where {" +
//		"?x c:isMemberOf[  ! contains(?this,  'inria') ]+  ?org " +
//		"}";
//
//query = "select *  where {" +
//"?x c:isMemberOf " +
//"@[exists { {?this rdf:type c:Consortium} union " +
//		   "{?this c:Designation ?name filter(contains(?name, 'inria'))}} ] +  ?org " +
//"} ";
//
//query = "select *  where {" +
//"c:Document (^rdfs:range/rdfs:domain @[ ! (strstarts(?this, c:)) ] )" +
//"+ ?x" +
//"} " ;
//
//query = 
//	"prefix ext: <function://test.kgraph.Test>" +
//	"select * (pathLength($path) as ?l) where {" +
//	"?x rdf:type/rdfs:subClassOf " +
//	    "@[?this != c:Something && ext:display(?this) ] + :: $path ?class " +
//	    "filter(pathLength($path) > 2)" +
//	"} limit 10" ;
//
//
//query = 
//	"prefix ext: <function://test.kgraph.Test>" +
//	"select * (pathLength($path) as ?l) where {" +
//	"?x c:isMemberOf " +
//	"@{  " +
//		"?this rdf:type/rdfs:subClassOf* c:Consortium  " +
//	"} +  :: $path" +
//	"?org " +
//	"filter(pathLength($path) >= 1)" +
//	"} " ;
//
//
//query = 
//	"prefix ext: <function://test.kgraph.Test>" +
//	"select * (pathLength($path) as ?l) where {" +
//	"?x c:isMemberOf " +
//	"@{select ?this  where {?this ?p ?y} having(count(*) >= 20)}  " +
//	"+  :: $path" +
//	"?org " +
//	"filter(pathLength($path) >= 3)" +
//	"} " ;
//
//query = 
//	"prefix ext: <function://test.kgraph.Test>" +
//	"select * (pathLength($path) as ?l) where {" +
//	"?x (c:isMemberOf/c:isMemberOf) " +
//	"@{  " +
//		"?this rdf:type c:Consortium  " +
//	"}   :: $path" +
//	"?org " +
//	"filter(pathLength($path) > 1)" +
//	"} " ;
//
//query = 
//	"prefix ext: <function://test.kgraph.Test>" +
//	"select *  where {" +
//	//"?x c:isMemberOf/c:Designation  :: $path 'T-Nova' "  +
//	//"<http://www.inria.fr/olivier.corby> (!rdf:type)*   :: $path ?org "  +
//	//"c:a  !( c:q  | ^ c:p)  ?x " +
//	//"c:a  ( !c:q  | ^ (!c:p))  ?x " +
//	"?x (c:p|c:q) c:a" +
//	"}  " ;
//
//query = "select * where {?x  (rdf:rest*|rdf:first*)+ ?y}";
//
//query = "select * where {?x    (( rdf:rest{0,1}) *) + ?y}";
//
//query = "prefix cc: <http://www.inria.fr/acacia/comma#>" +
//		"select   * where { graph ?g {    ?doc cc:CreatedBy ?x " +
//		"?x cc:FamilyName ?name  filter( ?name = 'Corby' )} " +
//		"filter(?name = xpath(?g, '/rdf:RDF//*[cc:FamilyName = \"Corby\" ]/*/text()' ))" +
//				"{select (xpath(?g, '/rdf:RDF/*/cc:Title/text()' ) as ?title) where {}}}";
//
//
//query = "describe ?x where  {?x c:hasCreated ?doc filter(?x ~ 'olivier.corby') }"; 
//
//
//query = "select * where {?x (rdf:rest{0,1}/rdf:rest{0,1})*/rdf:first ?y}";
//
////query = "select * where {?x (rdf:rest{0,1})*/rdf:first ?y}";
//
//query = 
//"select * where {" +
//"?x (rdf:rest +) " +
//"@{" +
//"graph $path {?b rdf:rest ?this} " +
//"graph ?g {?b rdf:rest ?this  ?b rdf:first ?d} " +
//" " +
//"} / rdf:first :: $path ?y}";
//
//
//System.out.println(Integer.MAX_VALUE);
//
//RuleEngine re = RuleEngine.create(graph);
//RuleLoad rl = RuleLoad.create(re);
//
////rl.load(data + "kgraph/rdf.rul");
////rl.load(data + "kgraph/rdfs.rul");
//
//try {
//	QueryProcess exec = QueryProcess.create(graph);
//	//exec.setListGroup(true);
////	exec.add(ponto);
//	
//	//exec.addEventListener(EvalListener.create());
//	t1 = new Date().getTime();
//	//exec.setDebug(true);
////	Mappings lMap1 = exec.query(update);
////	re.process();
////	
//	Mappings lMap = exec.query(query);
//	//lMap1 = exec.query(update2);
//	//lMap = exec.query(query);
//	
//	//System.out.println(RDFFormat.create(lMap, exec.getAST(lMap).getNSM()));
//	
//	System.out.println(lMap);
//	
//	System.out.println(lMap.size());
//	t2 = new Date().getTime();
//	System.out.println(lMap.size() + " " + (t2-t1) / 1000.0 + "s");
//	
////	for (Mapping map : lMap){
////		for (Node node : lMap.getSelect())
////		System.out.println(map.getNodes(node));
////	}
//	
//	
////	System.out.println( XMLFormat.create(lMap));
////	System.out.println( JSONFormat.create(lMap));
//
//	//IResults res = exec.SPARQLQuery(query);
//	
////	Query qq = exec.createQuery(query, null, null);
////	exec.query(qq);
////	exec.query(qq);
////
////	Graph gg = exec.getConstruct();	
////	gg.setEntailment(true);
////	QueryExec exec2 = QueryExec.create(gg);
////	IResults res2 = exec2.SPARQLQuery(squery);
//
//	//if (lMap.size()<=10) 
//	//System.out.println(res);
//	//System.out.println(gg.getIndex());
//
////	NSManager nsm = NSManager.create(ns);
////	System.out.println(RDFFormat.create(onto, nsm));
////	
////	//XLSTQuery.sparql(exec, query);
////	XSLTQuery xslt = XSLTQuery.create(data + "kgraph/copy.xsl", exec);
//	//System.out.println(xslt.xslt(data + "kgraph/test.html"));
//	
//	
//	//System.out.println(res.size() + " " + (t2-t1) / 1000.0 + "s");
//
//} catch (EngineException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//	}
//	
//	
////	public IDatatype equalsIgnoreAccent(Object o1, Object o2){
////		IDatatype dt1 = (IDatatype) o1;
////		IDatatype dt2 = (IDatatype) o2;
////		boolean b = StringHelper.equalsIgnoreAccent(dt1.getLabel(), dt2.getLabel());
////		if (b) return CoreseBoolean.TRUE;
////		return CoreseBoolean.FALSE;
////	}
//	
//	
//	void trace(Graph graph){
//		System.out.println(graph);
////		graph.init();
//		//System.out.println(graph.getIndex());
//		int n = 0;
////		for (Entity ent : graph.getIndex().get(graph.getNode(RDF.RDFTYPE))){
////			System.out.println(ent);
////			if (n++>50) break;
////		}
//	}
//	
//	
//	public IDatatype display(Object obj){
//		System.out.println(obj);
//		return CoreseBoolean.TRUE;
//		
//	}
//	
//	
//	
//	void test(int n){
//		System.out.println(n);
//		test(n+1);
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//}
