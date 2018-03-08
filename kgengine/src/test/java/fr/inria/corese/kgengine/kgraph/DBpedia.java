package fr.inria.corese.kgengine.kgraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class DBpedia {
	
	static final String base = 
		"http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query=";
	
	///  http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query
	
	public static void main(String[] args) {
		new DBpedia().process();
	}
	
	
	/**
	 prefix r: <http://dbpedia.org/resource/>
prefix o: <http://dbpedia.org/ontology/>
prefix f: <http://xmlns.com/foaf/0.1/>
prefix s: <http://schema.org/>
prefix p: <http://dbpedia.org/property/>

select * where {
?r ?p ?y .

?y a f:Person
filter (?r = r:Augustus)

}
	 */
	void process(){
		String query = 
			"prefix r: <http://dbpedia.org/resource/> " +
			"construct {?r ?p ?y} where {?r ?p ?y filter(?r = r:Augustus)}";
		
		String str = "";

		try {
			String qq = "wget -O tmp ";
			qq += "\"" + base + query + "\"";
			
			System.out.println("** Run: " + qq);
			
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(qq);

			
			
		}  catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(str);

		
	}

}
