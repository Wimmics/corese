
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author edemairy
 */
public class SimpleQuery {

	private static String[] queries = {"select * where {?x ?p ?y}"};

	public static void main(String[] args) {
		Graph graph = Graph.create(false);
		QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, true);
		execDQP.setGroupingEnabled(true); // @ToBeDocumented
		execDQP.addRemote(new URL(), WSImplem.REST);
for (String query : queries) {
			Mappings map = execDQP.query(query);
		}
	}
}
