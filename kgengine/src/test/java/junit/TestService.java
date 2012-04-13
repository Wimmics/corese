package junit;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;

public class TestService {
	
	@Test
	public void test1() throws ParserConfigurationException, SAXException, IOException{
		Graph g1 = Graph.create(true);
		Load load1 = Load.create(g1);
		
		String init = 
			"load rdfs:";
		
		String query = "select * where {" +
				"?p ?p ?r " +
				"service <http://localhost:8080/corese/sparql>  {?x ?p ?y} " +
				"?x a ?r} ";

		String del = "clear all";
		
		QueryProcess exec = QueryProcess.create(g1);
		
		try {
			exec.query(init);
			
			ProviderImpl p = ProviderImpl.create();
			exec.set(p);
			
			// ~/soft/apache-tomcat-7.0.26/bin/startup.sh 
			//p.doPost("http://localhost:8080/corese/sparql", "load rdfs:");
						
			Mappings map = exec.query(query);
			System.out.println(map.size());
			assertEquals("Result", 29, map.size());

			
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
