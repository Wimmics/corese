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



public class Main {
	static String data = "/home/corby/workspace/kgengine/src/test/resources/data/";

	static EngineFactory fac = new EngineFactory();
	static IEngine engine = fac.newInstance();
	
	public static void main(String [] args) throws IOException, EngineException {
		

	    engine.load(data +"alu/dbpedia_3.7.rdfs");	    
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
		
		
		System.out.println("Ontology Classes: "+OntologyClasses);
		
		
		
		
		for ( int i =0 ; i < OntologyClasses.size() ; i++ ) {

	        String element_i = OntologyClasses.get(i);

	        	double similarity = 0;

//		Requete :	        	
	        	String queryString = String.format("select ?sim (kg:similarity(<%s>, <http://dbpedia.org/ontology/Painting>) as ?sim ) where {}",element_i);
	        	//String queryString = String.format("select ?sim (kg:similarity(<%s>, <http://schema.org/Painting>) as ?sim ) where {}",element_i);

	        	try {
	        	IResults res1 = engine.SPARQLQuery(queryString);
	        	String[] variables1 = res1.getVariables();
	        	for (Enumeration<IResult> en = res1.getResults(); en.hasMoreElements();) {
	        	IResult r = en.nextElement();
	        	for (String var : variables1) {
	        	  if (r.isBound(var)) {
	        	IResultValue[] values = r.getResultValues(var);
	        	      for (int j1 = 0; j1 < values.length; j1++)
	        	    similarity=Double.parseDouble(values[j1].getStringValue());
	        	    
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
		  Collections.sort(keys, new Comparator<Object>() {
		      @SuppressWarnings("unchecked")
				public int compare(Object o1, Object o2) {
		          Object v1 = m.get(o1);
		          Object v2 = m.get(o2);
		          if (v1 == null) {
		              return (v2 == null) ? 0 : 1;
		          }
		          else if (v1 instanceof Comparable) {
		              return ((Comparable<Object>) v1).compareTo(v2);
		          }
		          else {
		              return 0;
		          }
		      }
		  });
		  Collections.reverse(keys); 
		  return keys;
		}
	}
	