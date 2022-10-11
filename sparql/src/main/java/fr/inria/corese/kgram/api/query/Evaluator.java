package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.api.Computer;

/**
 * Interface for the connector that evaluates filters
 * 
* @author Olivier Corby, Edelweiss, INRIA 2010
*
*/
public interface Evaluator extends Computer {
	
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
	//boolean test(Filter f, Environment e) throws SparqlException;
//        
	//boolean test(Filter f, Environment e, Producer p) throws SparqlException ;

	/**
	 * Evaluate a filter and return a Node
	 * use case: select fun(?x) as ?y
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	//Node eval(Filter f, Environment e, Producer p) throws SparqlException;
        
        /**
	 * Evaluate an extension function filter and return Mappings
	 * use case: select sql('select from where') as (?x ?y) where {}
	 * TODO: should be an interface instead of Mappings
	 * 	
	 */
//	Mappings eval(Filter f, Environment e, List<Node> nodes) throws SparqlException;
	                             

        // cast Java object into IDatatype
        //Node cast(Object obj, Environment e, Producer p);

        Binding getBinder();
				        
        void setProducer(Producer p);
        
        void setKGRAM(Eval o);
        
        //Eval getEval();

        void addResultListener(ResultListener rl);
        
        void start(Environment env);
        void finish(Environment env);
        void init(Environment env);
        
}
