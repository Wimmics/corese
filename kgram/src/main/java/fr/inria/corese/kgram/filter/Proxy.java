package fr.inria.corese.kgram.filter;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.List;

public interface Proxy {
    
        // create an array of target IDatatype[]
        // facilitate cast in Proxy and Plugin
        Object[] createParam(int n);
    
        void start(Producer p, Environment env);
	
        void finish(Producer p, Environment env);

        void setMode(int mode);
	
	void setEvaluator(Evaluator eval);
        
        void setProducer(Producer p);
	
	Evaluator getEvaluator();

	boolean isTrueAble(Object value);

	boolean isTrue(Object value);

	// Query Node value (e.g. KGRAM IDatatype)  to target proxy value
	Node getConstantValue(Node value);
        
        // return a IDatatype and store obj in it
        Object getValue(Object val, Object obj);

	Object getValue(boolean b);

	Object getValue(int value);
	
	Object getValue(float value);
	
	Object getValue(long value);
	
	Object getValue(double value);
	
	Object getValue(double value, String datatype);
	
	Object getValue(String value);
        
        Object getResultValue(Object obj);
        
        Object getBufferedValue(StringBuilder sb, Environment env);

	//int compare(Environment env, Producer p, Node o1, Node o2);

	// terms = <=
	Object term(Expr exp, Environment env, Producer p, Object o1, Object o2);

	// functions isURI regex
	Object eval(Expr exp, Environment env, Producer p, Object[] args);
	Object eval(Expr exp, Environment env, Producer p, Object[] args, Expr def);
                
        Object cast(Object obj, Environment env, Producer p);
	
	Object function(Expr exp, Environment env, Producer p);

	Object function(Expr exp, Environment env, Producer p, Object o1);

	Object function(Expr exp, Environment env, Producer p, Object o1, Object o2);

	
	// apply sum(?x) over env mappings
	Object aggregate(Expr exp, Environment env, Producer p, Node qNode);
        
        Expr decode(Expr exp, Environment env, Producer p);

	// type operators <=:
	void setPlugin(Proxy p);
        
        Proxy getPlugin();

        Expr createFunction(String name, List<Object> args, Environment env);
        
        Expr getDefine(Expr exp, Environment env, String name, int n);

}
