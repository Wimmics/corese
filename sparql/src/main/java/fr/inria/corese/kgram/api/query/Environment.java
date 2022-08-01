package fr.inria.corese.kgram.api.query;

import java.util.Map;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.EventManager;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.kgram.tool.ApproximateSearchEnv;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;

/**
 * Interface of the binding environment provided by KGRAM
 * e.g. for filter Evaluator
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Environment {
	
	
	/**
	 * Return current query
	 * @return
	 */
	Query getQuery();
        
        Binding getBind();
        void setBind(Binding b);
        
        boolean hasBind();
	
	/**
	 * 
	 * @return current graph node (only for filter interpreter)
	 */
	Node getGraphNode();
        default void setGraphNode(Node n) {
        }
	
	/**
	 * Return the target node of variable var
	 */ 
	Node getNode(Expr var);
	
	/**
	 * Return the target node bound to query node with label 
	 * @param label
	 * @return
	 */
	Node getNode(String label);

	/**
	 * Return the target node bound to query node with label 
	 * @param label
	 * @return
	 */
	Node getNode(Node qNode);
	
	/**
	 * Return the query node at index n
	 * @param n
	 * @return
	 */
	Node getQueryNode(int n);

	/**
	 * Return the query node with label
	 * @param n
	 * @return
	 */
	Node getQueryNode(String label);

	/**
	 * Test whether query node is bound 
	 * @param qNode
	 * @return
	 */
	boolean isBound(Node qNode);
	
		
	/**
	 * Return the path length corresponding to query node
	 * 
	 * @param qNode
	 * @return
	 */
	int pathLength(Node qNode);
        
	Path getPath(Node qNode);        
	
	int pathWeight(Node qNode);
	

	 // aggregates

	/**
	 * Count the number of non null values of query node
	 * count duplicates
	 */
	
	int count();
	
	/**
	 * Run the eval function of the evaluator with filter f on every Mapping 
	 * use case: select sum(?x) as ?sum
	 * 
	 * @param eval
	 * @param f
	 */
	//void aggregate(Evaluator eval, Producer p, Filter f);
	
	EventManager getEventManager();
	
	boolean hasEventManager();
	
	void setObject(Object o);
	
	Object getObject();
	
	void setExp(Exp exp);
	
	Exp getExp();
	
        // id -> bnode
	Map<String, IDatatype> getMap();
        
        Edge[] getEdges();	
        
        Node[] getNodes();
	
        Node[] getQueryNodes();
        
        Mappings getMappings();
        
        Mapping getMapping();
        
        Iterable<Mapping> getAggregate();
        
        void aggregate(Mapping m, int n);
        
        Node get(Expr var);
                
        ASTExtension getExtension();
        
        ApproximateSearchEnv getAppxSearchEnv();
                
        Eval getEval();
                
        void setEval(Eval e);
        
        ProcessVisitor getVisitor();
        
        void setReport(IDatatype dt);
        IDatatype getReport();
        
        int size();

}
