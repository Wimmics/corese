package fr.inria.corese.kgram.api.query;

import java.util.List;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;
/**
 * Interface for the connector that evaluates filters
 * 
* @author Olivier Corby, Edelweiss, INRIA 2010
*
*/
public interface Evaluator {
	
	static final int KGRAM_MODE 	= 0;
	static final int SPARQL_MODE  	= 1;
        
        public static final int CACHE_MODE = 101;
        public static final int NO_CACHE_MODE = 102;
	
	void setMode(int mode);
	
	int getMode();
        
        void setDebug(boolean b);

	
	/**
	 * Evaluate a filter 
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	boolean test(Filter f, Environment e) throws SparqlException;
        
	boolean test(Filter f, Environment e, Producer p) throws SparqlException ;

	/**
	 * Evaluate a filter and return a Node
	 * use case: select fun(?x) as ?y
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	Node eval(Filter f, Environment e, Producer p) throws SparqlException;
        
        /**
	 * Evaluate an extension function filter and return Mappings
	 * use case: select sql('select from where') as (?x ?y) where {}
	 * TODO: should be an interface instead of Mappings
	 * 	
	 */
	Mappings eval(Filter f, Environment e, List<Node> nodes) throws SparqlException;
	                             
        Expr getDefine(Environment env, String name, int n);
        Expr getDefineMetadata(Environment env, String metadata, int n);       
        Expr getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] values);

        Expr getDefine(String name);
        
        // cast Java object into IDatatype
        Node cast(Object obj, Environment e, Producer p);

        Binder getBinder();
				        
        void setProducer(Producer p);
        
        void setKGRAM(Eval o);
        
        //Eval getEval();

        void addResultListener(ResultListener rl);
        
        void start(Environment env);
        void finish(Environment env);
        void init(Environment env);
        
}
