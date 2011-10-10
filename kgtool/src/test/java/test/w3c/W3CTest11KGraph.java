package test.w3c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.query.SorterImpl;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;

import fr.inria.edelweiss.kgtool.load.BuildOptim;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.RuleLoad;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.TSVFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;

/**
 * KGRAM benchmark on W3C SPARQL 1.1 Query Language Test cases
 * 
 * entailment:
 * 
 * error in w3c ?
 * rdfs08.rq inherit rdfs:range ?
 * rdfs11.rq subclassof is reflexive
 * 
 * error in kgram:
 * rdfs09.rq one answer with rdf:type & subsumption
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class W3CTest11KGraph {
	// root of test case RDF data
	static final String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	
	static final String root  = data + "w3c-sparql11/WWW/2009/sparql/docs/tests/data-sparql11/";
	static final String more  = data + "w3c-sparql11/data/";

	static final String root0 = data + "test-suite-archive/data-r2/";
	
	
	 
	
	// query
	static final String man = 
		"prefix mf:  <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> " + 
		"prefix qt:  <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> " +
		"prefix sd:  <http://www.w3.org/ns/sparql-service-description#> " +
		"prefix ent: <http://www.w3.org/ns/entailment/> " +
		"prefix dawgt: <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> " +
		"select  * where {" +
		"?x mf:action ?a " +
		"minus {?x dawgt:approval dawgt:Withdrawn}" +
		
		"optional {?a qt:query ?q} " +
		"optional {?a qt:data ?d}" +
		"optional {?a qt:graphData ?g} " +
		"optional {?a sd:entailmentRegime ?ent}" +

		"optional {?x sd:entailmentRegime ?ent}" +
		"optional {?x mf:result ?r}" +
		"optional {?x rdf:type ?t}" +
		"" +
		"optional { ?a qt:serviceData [qt:endpoint ?ep ; qt:data ?ed] }" +
		
		"{?man rdf:first ?x} " +

		"} " +
		"group by ?x order by ?q ";
	
	
	
	// update
	static final String man2 = 
		"prefix mf:  <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> " + 
		"prefix ut:     <http://www.w3.org/2009/sparql/tests/test-update#> " +
		"prefix sd:  <http://www.w3.org/ns/sparql-service-description#> " +
		"prefix ent: <http://www.w3.org/ns/entailment/> " +
		"prefix dawgt: <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> " +
		"select  * where {" +
		"?x mf:action ?a " +
		"minus {?x dawgt:approval dawgt:Withdrawn}" +
		
		"optional {?a ut:request ?q} " +
		"optional {?a ut:data ?d}" +
		"optional {?a ut:graphData [?p ?g; rdfs:label ?name] " +
			"filter(?p = ut:data || ?p = ut:graph) } " +
		
		"optional {?x sd:entailmentRegime ?ent}" +
		"optional {?x mf:result ?ee filter(! exists {?ee ?qq ?yy}) }" +
		"optional {?x mf:result [ut:data ?r] }" +
		"optional {?x mf:result [ut:graphData [rdfs:label ?nres; ?pp ?gr ]] " +
			"filter(?pp = ut:data || ?pp = ut:graph) }" +
		"optional {?x rdf:type ?t}" +
		"{?man rdf:first ?x} " +
		"} " +
		"group by ?x order by ?q ";

	
	static String ENTAILMENT = "http://www.w3.org/ns/entailment/";
	static String NEGATIVE = "Negative";
	static String POSITIVE = "Positive";

	Test tok, tko;  
	int gok = 0, gko = 0, 
	total =0, nbtest = 0;
	boolean verbose = true;
	boolean sparql1 = true;
	boolean strict = true;
	boolean trace = true;
	
	List<String> errors = new ArrayList<String>();
	List<String> names = new ArrayList<String>();
	Earl earl;

	class Test extends Hashtable<String,Integer> {}
		
	public W3CTest11KGraph (){
		DatatypeMap.setSPARQLCompliant(true);
		tko = new Test();
		tok = new Test();
		earl = new Earl();
	}
	
	public static void main(String[] args){
		new W3CTest11KGraph().process();
	}
	
	/**
	 * SPARQL 1.0
	 */
	void test0(){
		sparql1=false;
		
		test(root0 + "distinct");
		test(root0 + "algebra");
		test(root0 + "ask");
		test(root0 + "basic");
		test(root0 + "bnode-coreference");
		test(root0 + "optional");
		test(root0 + "boolean-effective-value");
		test(root0 + "bound");
		test(root0 + "optional-filter");
		test(root0 + "cast");
		test(root0 + "expr-equals");
		test(root0 + "expr-ops");
		test(root0 + "graph");
		test(root0 + "i18n");
		test(root0 + "regex");
		test(root0 + "solution-seq");
		test(root0 + "triple-match");
		test(root0 + "type-promotion");
		test(root0 + "syntax-sparql1");
		test(root0 + "syntax-sparql2");
		test(root0 + "syntax-sparql3");
		test(root0 + "syntax-sparql4");
		test(root0 + "syntax-sparql5");
		test(root0 + "open-world");
		test(root0 + "sort");		
		test(root0 + "dataset");
		test(root0 + "reduced");
		test(root0 + "expr-builtin");	
		test(root0 + "construct");


	}

	
	/**
	 * SPARQL 1.1
	 */
	void test1(){
		sparql1=true;

		skip(root + "service");
		
		test(root + "syntax-fed");
		test(root + "entailment");
		test(root + "syntax-query");			 
		test(root + "negation");
		test(root + "project-expression");
		test(root + "subquery");
		test(root + "construct");
		test(root + "grouping");
		test(root + "functions");
		test(root + "json-res");
		test(root + "csv-tsv-res");
		test(root + "aggregates");
		test(root + "property-path");
		test(root + "bind");
		test(root + "bindings");
		test(root + "exists");

	}
	
	void testUpdate(){
		sparql1=true;
		
		 test(root + "syntax-update-1", true);
		 test(root + "syntax-update-2", true);
		 test(root + "basic-update", true);		 
		 test(root + "delete-data", true);
		 test(root + "delete-where", true);		 
		 test(root + "clear", true);
		 test(root + "delete", true);
		 test(root + "drop", true);
		 test(root + "delete-insert", true);		 
		 test(root + "update-silent", true);
	}
	
	void test(){
		sparql1=true;
		
		test(root + "functions");			 

	}
	
	void process(){
		gok=0;
		gko=0;
		
		//test0();
		
		if (true){
			test1();
			testUpdate();
		}
		else {
			test();
		}
		
		ArrayList<String> vec = new ArrayList<String>();
		for (String key : tok.keySet()){
			vec.add(key);
		}
		Collections.sort(vec);
		
		total = gok+gko;

		
		System.out.println("<html><head>");
		System.out.println("<title>Corese 3.0/KGRAM  SPARQL 1.1 Query &amp; Update W3C Test cases</title>");

		System.out.println("<style type = 'text/css'>");
		System.out.println(".success   {background:lightgreen}");
		System.out.println("body {font-family: Verdana, Arial, Helvetica, Geneva, sans-serif}");
		System.out.println("</style>");
		System.out.println("<link rel='stylesheet' href='kgram.css' type='text/css'  />");
		System.out.println("</head><body>");
		System.out.println("<h2>KGRAM  SPARQL 1.1 Query &amp; Update W3C Test cases</h2>");
		System.out.println("<p> Olivier Corby - Edelweiss - INRIA Sophia Antipolis Méditerranée</p>");
		System.out.println("<p>" + new Date() + " - KGRAM <a href='http://www-sop.inria.fr/edelweiss/software/corese/kgram'>homepage</a></p>");

		//System.out.println("<p><a href='http://www.w3.org/2001/sw/DataAccess/tests/r2'>SPARQL test cases</a></p>");
		System.out.println("<table border='1'>");
		System.out.println("<tr>");
		System.out.println("<th/> <th>test</th><th>success</th><th>failure</th><th>ratio</th>");
		System.out.println("</tr>");
		
		System.out.println("<th/> <th>total</th><th>" + gok + "</th><th>" + gko + "</th><th>" + 
				(100*gok)/(total) + "%</th>");
		int i = 1;
		
		for (String key : vec){
			int ind = key.lastIndexOf("/");
			String title = key.substring(0, ind);
			ind = title.lastIndexOf("/");
			title = title.substring(ind+1);
			
			int suc =  tok.get(key);
			int fail = tko.get(key);
			String att = "";
			if (fail==0) att = " class='success'";
			System.out.print("<tr" + att + ">");
			System.out.print("<th>" + i++ +"</th>");
			System.out.print("<th>" + title +"</th>");
			System.out.print("<td>" + suc +"</td>");
			System.out.print("<td>" + fail +"</td>");

			int ratio = 0;
			try { ratio = 100*suc / (suc+fail)  ;}
			catch (Exception e){
				
			}
			System.out.print("<td>" + ratio +"%</td>");
			System.out.println("</tr>");
		}
		
		System.out.println("</table>");
		int j = 0, k = 1;
		if (errors.size()>0){
			System.out.println("<h2>Failure</h2>");
		}
		for (String name : names){
			System.out.println(k++ + ": " + name.substring(name.indexOf("data-sparql11")));
			//System.out.println(k++ + ": " + name);
			System.out.println("<pre>\n" + errors.get(j++) + "\n</pre>");
			System.out.println();
		}
		System.out.println("</body><html>");
		
		if (total != nbtest){
			System.out.println("*** Missing result: " + total + " " + nbtest);
		}
		
		earl.toFile(more + "earl.ttl");

		//Processor.finish();
				
	}
	
	
	
	/**
	 * Test one manifest
	 */
	void skip(String path){
		test(path, false, false);
	}
	
	void test(String path){
		test(path, false, true);
	}
	
	void test(String path, boolean update){
		test(path, update, true);
	}
	
	void test(String path, boolean update, boolean process){
		String manifest = path + "/manifest.rdf";
		try {
			System.out.println("** Load: " + pp(manifest));
			
			Graph g = Graph.create();
			Load load = Load.create(g);
			load.load(manifest);
			QueryProcess exec2 = QueryProcess.create(g);
			exec2.setListGroup(true);
			
			String qman = man;
			if (update) qman = man2;
			
			Mappings res2 = exec2.query(qman);
			System.out.println("** NB test: " + res2.size());
			//System.out.println(res2);
			nbtest += res2.size();
			
			int ok = 0, ko = 0;
			
			for (Mapping map : res2){
					
				if (! process){
					String test	 = getValue(map, "?x");
					earl.skip(test);
				}
				else if (query(path, map)){
					ok++;
				}
				else {
					ko++;
				}
			}
			
			gok += ok;
			gko += ko;
			
			tok.put(pp(manifest), ok);
			tko.put(pp(manifest), ko);
		} 
		catch (EngineException e) {
			// TODO Auto-generated catch block
			System.out.println(manifest);
			e.printStackTrace();
		}
	}
	
	String getValue(Mapping map, String var){
		if (map.getNode(var)!=null){
			return map.getNode(var).getLabel();
		}
		return null;
	}
	
	
	String[] getValues(Mapping map, String var){
		List<Node> list = map.getNodes(var, true);
		String[] fnamed = new String[list.size()];
		int j = 0;
		for (Node n : list){
			fnamed[j++] = n.getLabel();
		}
		return fnamed;
	}
	

	/**
	 * One test
	 * @param fquery
	 * @param fdefault  RDF file for default graph
	 * @param fresult
	 * @param fnamed  RDF files for named graphs
	 * @param ent entailment
	 * 
	 * @return
	 */
	boolean query(String path, Mapping map){
		//System.out.println(map);
				
		String defbase = uri(path + File.separator);

		String[] fnamed   = getValues(map, "?g");
		String[] fnames   = getValues(map, "?name");
		String[] fdefault = getValues(map, "?d");;
		String[] frnamed  = getValues(map, "?gr");
		String[] frnames  = getValues(map, "?nres");
		String test	  	  = getValue(map, "?x");
		String fquery	  = getValue(map, "?q");
		String fresult 	  = getValue(map, "?r");
		String ent 		  = getValue(map, "?ent");
		String type		  = getValue(map, "?t");
		
		String[] ep  = getValues(map, "?ep");
		String[] ed  = getValues(map, "?ed");

		
		boolean isEmpty   = getValue(map, "?ee") != null;
		boolean isBlankResult = false;
		boolean isJSON = false, isCSV = false, isTSV = false;
		boolean rdfs = ent != null &&  ent.equals(ENTAILMENT+"RDFS");
		boolean rdf  = ent != null &&  ent.equals(ENTAILMENT+"RDF");
		int entail = QueryProcess.STD_ENTAILMENT;
		
		if (fresult!=null){
			Node nr = map.getNode("?r");
			isBlankResult = nr.isBlank();
		}
		
		
//		String man = getValue(map, "?man");
//		if (man == null){
//			System.out.println("**************************");
//			System.out.println(map);
//		}
		
		if (fquery == null) fquery = getValue(map, "?a");
		
		//if (! fquery.contains("replace03")) return true;


		if (trace) System.out.println(pp(fquery));	
		
		if (fresult!=null) fresult = clean(fresult); // remove file://
		fquery =  clean(fquery);
		String query = read(fquery);
		if (query == null || query == ""){
			System.out.println("** ERROR 1: " + fquery + " " + query);
		}
						
		Graph graph = Graph.create();
		if (rdf || rdfs){
			graph.setEntailment();
			graph.set(Entailment.RDFS, rdfs);
			if (rdfs){
				graph.set(Entailment.RDFSSUBCLASSOF, true);
			}
		}
		
		RuleEngine re = RuleEngine.create(graph);
		RuleLoad ld   = RuleLoad.create(re);
		Load load     = Load.create(graph);
		BuildOptim bb = BuildOptim.create(graph);
		load.setBuild(bb);
		load.reset();
		QueryProcess exec = QueryProcess.create(graph);
		//exec.set(SorterImpl.create(graph));
		// for update:
		exec.setLoader(load);
		// default base to interpret from <data.ttl>
		exec.setDefaultBase(defbase);
				
		ArrayList<Result> vec = null;
		Mappings w3result = null;
		Graph gres = null;
		int nbres = -1;
	
		// Load the result
		if (fresult==null && frnamed.length==0){
			if (isEmpty){
				gres = Graph.create();
			}
		}
		else if (fresult!=null && fresult.endsWith(".srx")){
			// XML Result
			vec = new XMLResult().parse(fresult);
			nbres = vec.size();
		}
		else if (fresult!=null && fresult.endsWith(".srj")){
			isJSON = true;
		}
		else if (fresult!=null && fresult.endsWith(".csv")){
			isCSV = true;
		}
		else if (fresult!=null && fresult.endsWith(".tsv")){
			isTSV = true;
		}
		else if (frnamed.length>0 || 
				(fresult != null && (fresult.endsWith(".ttl") ||  fresult.endsWith(".rdf")))){
			if (sparql1 || path.contains("construct")){
				// Graph Result 
				gres = Graph.create();
				Load rl = Load.create(gres);
				rl.reset();
				
				if (fresult != null && ! isBlankResult){
					rl.load(ttl2rdf(fresult));
				}
				
				int i = 0;
				for (String g : frnamed){
					rl.reset();
					rl.load(ttl2rdf(g), frnames[i++]);
				}
				
				gres.index();
			}
			else {
				// SPARQL 1.0 RDF Result Format
				w3result = parseRDFResult(ttl2rdf(fresult));
				nbres = w3result.size();
			}
		}

		
		// Positive & Negative Syntax test
		Query qq = null;
		try {
			qq = exec.compile(query);
			
			if (type == null){}
			else if (type.contains(POSITIVE)){
				
				if (! query.contains("LOAD")){
					// Extra test: exec the query
					exec.query(query);
				}

				// positive syntax test
				if (! qq.isCorrect()){
					System.out.println("** Should be positive: " + fquery);
					names.add(fquery);
					errors.add(query);
				}
				
				earl.define(test, qq.isCorrect());
				return qq.isCorrect();
			}
			// NEGATIVE is tested below, at runtime
		} 
		catch (EngineException e1) {
			if (type!=null && type.contains(NEGATIVE)){
				earl.define(test, true);
				return true;
			}
			System.out.println("** Parser Error: " + e1.getMessage());
			names.add(fquery);
			errors.add(query);
			earl.define(test, false);
			return false;
		}
		

		// LOAD RDF
		ArrayList <String> defaultGraph=null, namedGraph=null;	

		
		// check if query contains from/from named
		if (fdefault.length==0 && qq.getFrom().size()>0){
			fdefault = new String[qq.getFrom().size()];
		}
		int i = 0;
		for (Node node : qq.getFrom()){
			String name = node.getLabel();
			fdefault[i++] = name;
		}

		// get named graphs
		if (fnamed.length==0 && qq.getNamed().size()>0){
			fnamed = new String[qq.getNamed().size()];
		}
		i = 0;
		for (Node node : qq.getNamed()){
			String name = node.getLabel();
			fnamed[i++] = name;
		}		

		if (ep.length>0){
			Provider p = endpoint(ep, ed);
			exec.set(p);
		}
		
		if (fdefault.length>0){
			// Load RDF files for default graph
			defaultGraph = new ArrayList<String>();

			for (String file : fdefault){
				// rdf version
				String ff = ttl2rdf(file);
				String name = file;
				load.load(ff, name);
				defaultGraph.add(name);
			}
		}


		if (fnamed.length>0){				
			// Load RDF files for named graphs
			namedGraph = new ArrayList<String>();

			i = 0;
			for (String file : fnamed){
				String ff = ttl2rdf(file);
				String name = file;
				if (fnames.length>0){
					// the name of a named graph
					name = fnames[i++];
				}
				load.load(ff, name);
				namedGraph.add(name);
			}
		}
				
		
		if (rdfs || rdf){
			// load RDF/S definitions & inference rules
			if (defaultGraph == null) defaultGraph = new ArrayList<String>();

			if (rdfs) {
				entail = QueryProcess.RDFS_ENTAILMENT;
				load.load(more + "rdfs.rdf", RDFS.RDFS);
				ld.load(more + "rdfs.rul");
				defaultGraph.add(RDFS.RDFS);
			}
			else {
				entail = QueryProcess.RDF_ENTAILMENT;
				// exclude rdfs properties when load rdf
				load.exclude(RDFS.RDFS);
			}

			load.load(more + "rdf.rdf", RDFS.RDF);					
			//ld.load(more + "rdf.rul");
			defaultGraph.add(RDFS.RDF);
			
			//re.setDebug(true);
			re.process();								
		}
			

		try {

			// QUERY PROCESSING
			//exec.setDebug(true);
			Mappings res = exec.sparql(query, defaultGraph, namedGraph, entail);
			
//			XMLFormat f = XMLFormat.create(res);
//			System.out.println(f);
			
			//System.out.println(res);
						
			// CHECK RESULT
			boolean result = true;
			
			
			if (type!=null && type.contains(NEGATIVE)){
				// KGRAM should detect an error here
				if (res.getQuery().isCorrect()){
					System.out.println("** Should be false: " + res.getQuery().isCorrect());
					result = false;
				}
			}
			else if (! res.getQuery().isCorrect()){
				System.out.println("** Should be true: " + res.getQuery().isCorrect());
				result = false;
			}
			else if (isJSON){
				// checked by hand
				JSONFormat json = JSONFormat.create(res);
				System.out.println(json);
			}
			else if (isCSV){
				// checked by hand
				CSVFormat json = CSVFormat.create(res);
				System.out.println(json);
			}
			else if (isTSV){
				// checked by hand
				TSVFormat json = TSVFormat.create(res);
				System.out.println(json);
			}
			else if (gres != null){
				// construct where
				Graph kg = exec.getGraph(res);

				if (kg != null && ! kg.compare(gres)){
					if (!sparql1 && path.contains("construct")){
						// ok verified by hand 2011-03-15 because of blanks in graph
					}
					else {
						result = false;
					
//						System.out.println("w3c:");
//						System.out.println( RDFFormat.create(gres));
//						System.out.println("kgram:");
//						System.out.println( RDFFormat.create(kg));
					}
				}
			}			
			else if (nbres != res.size()){
				if (verbose){
					System.out.println("** Failure");
					if (fdefault.length>0)   System.out.println(pp(fdefault[0]) + " ");
					if (fquery!=null)  System.out.println(pp(fquery) + " ");
					if (fresult!=null) System.out.println(pp(fresult));
					System.out.println("kgram result: ");
					System.out.println(res);
					System.out.println("w3c: " + nbres + "; kgram: " + res.size());
				}
				System.out.println(query);
				result = false;
			}
			else if (w3result!=null){
				// old rdf result format
				result = validate(res, w3result);
			}
			else {
				// XML Result Format
				result = validate(res, vec);
			}
			
			if (result == false){
				names.add(fquery);
				errors.add(query);
			}
			
			earl.define(test, result);
			return result;
			
		}
		catch (EngineException e){
			if (type!=null && type.contains(NEGATIVE)){
				earl.define(test, true);
				return true;
			}
			
			System.out.println("** ERROR 2: " + e.getMessage() + " " + nbres + " " + 0);
			if (fdefault.length>0)   System.out.print(pp(fdefault[0]) + " ");
			if (fquery!=null)  System.out.print(pp(fquery) + " ");
			if (fresult!=null) System.out.println(pp(fresult));
			System.out.println(query);
			errors.add(query);
			names.add(fquery);
			earl.define(test, false);
			return false;			
		}
	}
	
	
	
	Provider endpoint(String[] ep, String[] ed){
		ProviderImpl p =  ProviderImpl.create();
		int j = 0;
		for (String nep : ep){
			String name = ed[j++];

			// rdf version
			String ff = ttl2rdf(name);
			Graph g = Graph.create();
			Load load = Load.create(g);
			load.load(ff, name);
			p.add(nep, g);
		}

		return p;
	}

	
	
	
	/**
	 * Blanks may have different ID in test case and in kgram
	 * but same ID should remain the same
	 * Hence store ID in hashtable to compare
	 *
	 */
	class TBN extends Hashtable<IDatatype,IDatatype>{
		
		boolean same(IDatatype dt1, IDatatype dt2){
			if (containsKey(dt1)){
				return get(dt1).sameTerm(dt2);
			}
			else {
				put(dt1, dt2);
				return true;
			}
		}
	}
	
	// target value of a Node
	IDatatype datatype(Node n){
		return (IDatatype) n.getValue();
	}
	
	
	/**
	 * KGRAM vs W3C result
	 */
	boolean validate(Mappings kgram, ArrayList<Result> w3c){
		boolean result = true, printed = false;
		Hashtable<Mapping, Result> table = new Hashtable<Mapping, Result>();
		
		for (Result w3cres : w3c){
			// for each w3c result
			boolean ok = false;
						
			for (Mapping kres : kgram){				
				// find a new kgram result that is equal to w3c
				if (table.contains(kres)) continue;
				
				ok = compare(kres, w3cres);
				
				if (ok){
					//if (kgram.getSelect().size() != w3cres.size()) ok = false;
					
					for (Node qNode : kgram.getSelect()){
						// check that kgram has no additional binding 
						if (kres.getNode(qNode)!=null){ 
							if (w3cres.get(qNode.getLabel()) == null){
								ok = false;
							}
						}
					}
				}

				if (ok){
					table.put(kres, w3cres);
					break;
				}
			}
				
			
			if (! ok){
				result = false;
				
				System.out.println("** Failure");
				if (printed == false){
					System.out.println(kgram);
					printed = true;
				}
				for (String var : w3cres.keySet()){
					// for each w3c variable/value
					Value  val = w3cres.get(var);
					System.out.println(var + " [" + val +"]");
				}
				System.out.println("--");
			}			
			
		}
		return result;
	}

	
	
	// compare two results
	boolean compare (Mapping kres, Result w3cres){
		TBN tbn = new TBN();
		boolean ok = true;
		
		for (String var : w3cres.keySet()){
			if (! ok) break;
			
			// for each w3c variable/value
			Value  w3cval = w3cres.get(var);
			// find same value in kgram
			if (w3cval != null){
				String cvar = "?"+var;
				Node kNode = kres.getNode(cvar);
				if (kNode == null){
					ok = false;
				}
				else {
					IDatatype kdt = datatype(kNode);
					IDatatype wdt = w3cval.getDatatypeValue();
					ok = compare(kdt, wdt, tbn);
				}
			}
		}
		
		return ok;
	}
	
	
	// compare kgram vs w3c values
	boolean compare(IDatatype kdt, IDatatype wdt, TBN tbn){
		boolean ok = true;
		if (kdt.isBlank()){
			if (wdt.isBlank()){
				// blanks may not have same ID but 
				// if repeated they should  both be the same
				ok = tbn.same(kdt, wdt);
			}
			else {
				ok = false;
			}
		}
		else if (wdt.isBlank()){
			ok = false;
		}
		else if (kdt.isNumber() && wdt.isNumber()){
			ok = kdt.sameTerm(wdt);

			if (DatatypeMap.isLong(kdt) && DatatypeMap.isLong(wdt)) {
				// ok
			}
			else {
				if (! ok){
					// compare them at 10^-10
					ok = 
						Math.abs((kdt.getDoubleValue()-wdt.getDoubleValue())) < 10e-10;
					if (ok){
						System.out.println("** Consider as equal: " + kdt.toSparql() + " = " + wdt.toSparql());
					}
				}
			}
			
		}
		else {
			ok = kdt.sameTerm(wdt);
		}
		
		if (ok && strict && wdt.isLiteral()){
			// check same datatypes
			if (kdt.getDatatype()!=null && wdt.getDatatype()!=null)
				 ok = kdt.getDatatype().sameTerm(wdt.getDatatype());
			else ok = kdt.getIDatatype().sameTerm(wdt.getIDatatype());
			if (!ok)
				System.out.println("** Datatype differ: " + kdt.toSparql() + " " + wdt.toSparql());
		}
		
		return ok;
		
	}
	
	
	
	
	
	
	
	// w3c result was rdf format
	// result is 
	// results may be sorted
	boolean validate(Mappings kgram, Mappings w3c){
		boolean result = true, printed = false,
			sorted = kgram.getQuery().getOrderBy().size()>0;
		int j = 0;
		
		for (Mapping w3cres : w3c){
			// for each w3c result
			List<Node> lVar = w3cres.getNodes("?var");
			List<Node> lVal = w3cres.getNodes("?val");
			boolean ok = false;
			
			if (sorted){
				ok = compare(lVar, lVal, kgram.get(j++));
			}
			else {
				for (Mapping kres : kgram){
					if (ok) break;
					ok = compare(lVar, lVal, kres);				
				}
			}
				
			
			if (! ok){
				System.out.println("** Failure");
				if (printed == false){
					System.out.println(kgram);
					printed = true;
				}
				result = false;
				int k = 0;
				for (Node var : lVar){
					// for each w3c variable/value
					Node  vv = lVal.get(k++); //w3cres.getNode(var);
					System.out.println(var + " [" + vv +"]");
				}
				System.out.println("--");
			}			
			
		}
		return result;
	}

	
	boolean compare(List<Node> lVar, List<Node> lVal, Mapping kres){
		boolean ok = true;
		TBN tbn = new TBN();
		int i = 0;
		for (Node var : lVar){
			if (! ok) break;
			
			// for each w3c variable/value
			// find same value in kgram
			Node  w3cval = lVal.get(i++); 

			if (w3cval != null){
				String cvar = "?" + var.getLabel();
				Node kNode = kres.getNode(cvar);
				if (kNode == null){
					ok = false;
				}
				else {
					IDatatype kdt = datatype(kNode);
					IDatatype wdt = datatype(w3cval);
					ok = compare(kdt, wdt, tbn);
				}
			}
		}
		
		return ok;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	String pp(String name){
		String pat = "sparql11/";
		int index = name.indexOf(pat);
		if (index == -1) return name;
		return name.substring(index+pat.length());
	}
	
	String name(String path){
		int index = path.lastIndexOf(File.separator);
		return path.substring(index+1);
	}
	
	String uri(String file){
		return "file://" + file;
	}
	
	
	String ttl2rdf(String name){
		if (name.endsWith(".ttl")){
			name = name.substring(0, name.length()-4);
			name = name + ".rdf";
		}
		return clean(name);
	}
	
	String clean(String name){
		String HEAD = "file://";
		if (name.startsWith(HEAD)){
			name = name.substring(HEAD.length());
		}
		return name;
	}
	
	
	
	
	// read and return query
	public String read(String name){
		//name = clean(name);
		String query = "", str = "";
		try {
			BufferedReader fq = new BufferedReader(new FileReader(name));
			while (true){
				str = fq.readLine();
				if (str == null){
					fq.close();
					break;
				}
				query += str + "\n";
			}
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return query;
	}
	
	
	
	
	
	Mappings parseRDFResult(String fresult){
		String query =
			"prefix rs: <http://www.w3.org/2001/sw/DataAccess/tests/result-set#>" +
			"select ?var ?val where { " +
			"{ ?r rs:solution ?s " +
				"optional {?s rs:index ?i }" +
				"optional { " +
					"?s rs:binding [  rs:variable ?var ; rs:value ?val ] } " +
			"} " +
			"union {?r rs:boolean 'true'^^xsd:boolean}" +
			"}" +
			"order by ?i  " +
			"group by ?s  ";
		
		Graph g = Graph.create();
		Load load = Load.create(g);
		load.load(fresult);
		
		QueryProcess exec = QueryProcess.create(g);
		exec.setListGroup(true);
		Mappings map = null;
		try {
			map = exec.query(query);
			//System.out.println(map);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;

	}


	

	
}
