package fr.inria.corese.kgengine.kgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import fr.inria.corese.kgengine.api.EngineFactory;
import fr.inria.corese.kgengine.api.IEngine;
import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgengine.api.IResults;
import fr.inria.corese.sparql.exceptions.EngineException;



public class Main2 {

	static String data = "/home/corby/workspace/kgengine/src/test/resources/data/";
	
	static EngineFactory fac = new EngineFactory();
	static IEngine engine = fac.newInstance();
	
	public static void main(String [] args) throws IOException, EngineException {
		

	    engine.load(data + "alu/dbpedia_3.7.rdfs");	    
	  
	    engine.load(data + "alu/schema.rdfs.org.rdfs");
	    
		ArrayList<String> OntologyClasses = new ArrayList<String>();		
		HashMap<String, Double> Results = new HashMap<String, Double>();	
		
		String query = String.format("select DISTINCT ?x where {{?x rdf:type owl:Class}}");
		IResults res = engine.SPARQLQuery(query);
		String[] variables = res.getVariables();
		
		for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
			IResult r = en.nextElement();
			for (String var : variables) {
			    if (r.isBound(var)) {
			IResultValue[] values = r.getResultValues(var);
			        for (int j = 0; j < values.length; j++){	   	        			 
			        		OntologyClasses.add(values[j].getStringValue());
			        	}	
			    //    System.out.println(var + " = Not bound");
		    }
		    }	  		
		}
		
		
		//System.out.println("Ontology Classes: "+OntologyClasses);
		
		
		
		
		for ( int i =0 ; i < OntologyClasses.size() ; i++ ) {

	        String element_i = OntologyClasses.get(i);

	        	double similarity = 0;

//		Requete :	        	
//	        	String queryString = 
//	        		String.format("select ?sim (kg:similarity(<%s>, <http://dbpedia.org/ontology/Painting>) as ?sim ) where {}",element_i);
	        	String queryString = 
	        	String.format("select ?sim (kg:similarity(<%s>, <http://schema.org/Painting>) as ?sim ) where {}",element_i);
	        	
	        	
//	        	queryString = 
//	        	"select ?sim (kg:similarity(<http://dbpedia.org/ontology/Actor>, <http://dbpedia.org/ontology/Painting>) as ?sim ) " +
//	        	"where {}";
//	        	
//	        	queryString = 
//		        	"select ?sim (kg:similarity(<http://schema.org/Article>, <http://schema.org/Painting>) as ?sim ) " +
//		        	"where {}";
	        	
//	        	if (true){
//		        	IResults res1 = engine.SPARQLQuery(queryString);
//		        	System.out.println(res1);
//		        	return;
//	        	}

	        	try {
	        	IResults res1 = engine.SPARQLQuery(queryString);
	        	String[] variables1 = res1.getVariables();
	        	
	        	for (IResult r : res1) {
	        		for (String var : variables1) {
	        			if (r.isBound(var)) {
	        				similarity = r.getDatatypeValue(var).doubleValue();
	        			} 
	        		}
	        	}
	        	
	        	} catch (EngineException e) {
	        		e.printStackTrace();
	        	}

	        	Results.put(element_i, similarity);        
		}
		
		for (Iterator<?> i3 = SortByValue(Results).iterator(); i3.hasNext(); ) {
		    String key = (String) i3.next();
		    System.out.println(key+" , "+Results.get(key));
		}
	

	}
	
	public static List<String> SortByValue(final Map<String, Double> m) {
		  List<String> keys = new ArrayList<String>();
		  keys.addAll(m.keySet());
		  Collections.sort(keys, new Comparator<String>() {
		      @SuppressWarnings("unchecked")
				public int compare(String o1, String o2) {
		    	  Double v1 = m.get(o1);
		    	  Double v2 = m.get(o2);
		         
		          int res = v1.compareTo(v2);
		          if (res == 0){
		        	  res = o1.compareTo(o2);
		        	  if (res < 0) {
		        		  res = 1;
		        	  }
		        	  else if (res > 0){
		        		  res = -1;
		        	  }
		          }
		          return res;
		      }
		  });
		  Collections.reverse(keys); 
		  return keys;
		}
	}
	