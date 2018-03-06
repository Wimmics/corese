package junit;

import java.util.HashMap;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.api.QueryGraphVisitor;
import fr.inria.corese.kgraph.core.edge.EdgeImpl;
import fr.inria.corese.kgraph.core.Graph;

/**
 * Example of Query Graph Visitor that replace blank nodes by variables
 * and select * 
 *
 */
public class QGVisitor implements QueryGraphVisitor {
	
	static final String VAR = "?_kg_var_";
	int count = 0;
	
	Table table;
	
	class Table extends HashMap<Node, Node> {
		
	}
	
	QGVisitor(){
		table = new Table();
	}
	



	public ASTQuery visit(ASTQuery ast) {
		ast.setSelectAll(true);
		return ast;
	}

	
	public Entity visit(Entity ent) {
		
		if (! (ent.getEdge() instanceof EdgeImpl)){
			return ent;
		}
		
		EdgeImpl edge = (EdgeImpl) ent.getEdge();
	
		for (int i = 0; i<ent.nbNode(); i++){
			Node n = ent.getNode(i);
			if (n.isBlank()){
				Node v = getVariable(n);
				edge.setNode(i, v);
			}
		}
		
		return ent;
	}

	
	private Node getVariable(Node n) {
		Node v = table.get(n);
		if (v == null){
			String name = VAR + count ++;
			v = NodeImpl.createVariable(name);
			table.put(n, v);
		}
		return v;
	}




	public Query visit(Query q) {
		q.setLimit(1);
		return q;
	}




	public Graph visit(Graph g) {
		return g;
	}

}
