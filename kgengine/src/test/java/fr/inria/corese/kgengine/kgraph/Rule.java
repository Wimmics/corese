package fr.inria.corese.kgengine.kgraph;

import java.util.Date;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.engine.core.Engine;
import fr.inria.corese.engine.model.api.Bind;
import fr.inria.corese.engine.model.api.LBind;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;

public class Rule {
	static String data = "/user/corby/home/workspace/kgengine/src/test/resources/data/";

	public static void main(String[] args) {
		new Rule().process();
	}

	private void process() {
		Graph graph = Graph.create();
		QueryProcess exec = QueryProcess.create(graph);
		Engine rengine = Engine.create(exec);

		rengine.load(data + "rule/tree.brul");
		
		Load ld = Load.create(graph);
		ld.load(data + "rule/tree.brul");
		RuleEngine re = ld.getRuleEngine();
		
		String init = 
			"prefix c: <http://www.inria.fr/term#> " +
			"insert data {" +
			"[a c:Father ; c:term(<John> <Jack>)]" +
			"}" +
			"";
		
		String query = 	"prefix c: <http://www.inria.fr/term#> " +
		"select  * " +
		"where {[a c:Parent; c:term(?x ?y)]}";

		try {
			Mappings map = exec.query(init);
			map = exec.query(query);
			System.out.println(map.size());
			
			//rengine.setDebug(true);	
			
			Date d1 = new Date();
			LBind lb = rengine.query(query);
			Date d2 = new Date();
			
			System.out.println("** Time: " + (d2.getTime()-d1.getTime())/1000.0);
			System.out.println("** Loop: " + rengine.getLoop());
			System.out.println(lb);

			System.out.println("end");

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
