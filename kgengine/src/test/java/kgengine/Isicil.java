package kgengine;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgengine.GraphEngine;

public class Isicil {
	
	
	private IEngine semengine = null; 
	
	
	public static void main(String[] args){
		new Isicil().process();
	}
	
	void process(){
		EngineFactory ef = new EngineFactory(); 
		IEngine semengine1 = ef.newInstance(); 
		IEngine semengine2 = ef.newInstance(); 

	
		QueryExec queryAgent = QueryExec.create(); 
		queryAgent.setListGroup(false); 
		queryAgent.add( semengine1 ); 
		queryAgent.add( semengine2 ); 
		
		String query = "insert data {<John> <name> 'John'}";
		try {
			queryAgent.SPARQLQuery(query);
			System.out.println(((GraphEngine)semengine1).getGraph());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	

}
