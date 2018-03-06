package fr.inria.corese.kgraph.query;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.parser.Transformer;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgraph.api.QueryGraphVisitor;
import fr.inria.corese.kgraph.core.EdgeFactory;
import fr.inria.corese.kgraph.core.Graph;

/**
 * Translate a Graph into a Query
 * Use case:
 * let g = eval(construct {} where {})
 * let q = query(g)
 * eval(q)
 * 
 * The target Query may itself be a construct
 * 
 * There may be a Visitor that rewrites the Graph on the Fly, e.g. :
 * - bnode to variable
 * - constant to variable
 * - generalize type
 * - eliminate edge
 *
 */
public class QueryGraph implements QueryGraphVisitor {
	
	boolean isDebug = false,
			isConstruct = false;

	Graph graph;
	QueryGraphVisitor visitor;
        EdgeFactory fac;
		
		
	QueryGraph(Graph g){
		graph = g;
                fac = new EdgeFactory(g);
		visitor = this;
	}
	
	
	public static QueryGraph create (Graph g){
		return new QueryGraph(g);
	}
	
	public void setVisitor(QueryGraphVisitor vis){
		visitor = vis;
	}
	
	/**
	 * Compile Graph into a BGP
	 * Generate a Query
	 */
	public Query getQuery(){
		Transformer t = Transformer.create();
		ASTQuery ast = ASTQuery.create();
                ast.setSelectAll(true);
		ast = visitor.visit(ast);
		graph = visitor.visit(graph);
		
		Exp exp = getExp(graph);
		Query q = Query.create(exp);
		q.setAST(ast);
		q = t.transform(q, ast);
		q.setDebug(isDebug);
		q = visitor.visit(q);
		
		if (isConstruct()){
			// TODO: blanks in construct should be renamed
			q.setConstruct(q.getBody());
			q.setConstruct(true);
		}
		
		return q;
	}
	
	/**
	 * The query is construct {graph} where {graph}
	 */
	public void setConstruct(boolean b){
		isConstruct = b;
	}	
	
	public boolean isConstruct(){
		return isConstruct;
	}

	public void setDebug(boolean b){
		isDebug = b;
	}		
	
	Exp getExp(Graph g){
		Exp exp = Exp.create(Exp.AND);
		
		for (Entity ent : g.getEdges()){
			Entity e = visitor.visit(ent);
			if (e != null){
				init(e);
				exp.add(fac.copy(e).getEdge());
			}
		}
		return exp;
	}
	



	/**
	 * Set the index of Node to -1
	 * Just in case the graph has already been used as a Query
	 */
	void init(Entity ent){
		Edge edge = ent.getEdge();
		
		for (int i=0; i<ent.nbNode(); i++){
			edge.getNode(i).setIndex(-1);
		}
		
		edge.getEdgeNode().setIndex(-1);
		Node var = edge.getEdgeVariable();
		
		if (var != null){
			var.setIndex(-1);
		}
	}



	
	
	/*******************************
	 * Visitor
	 */
	
	public Query visit(Query q) {
		return q;
	}

	public Graph visit(Graph g) {
		return g;
	}
	
	public ASTQuery visit(ASTQuery ast) {
		return ast;
	}
	
	public Entity visit(Entity ent) {
		return ent;
	}
	
	
	
	
	
	
	
	
	
}
