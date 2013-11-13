package fr.inria.edelweiss.kgram.filter;

import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Stack;

/**
 * A generic filter Evaluator
 * Values are Java Object
 * Target processing is delegated to a proxy and a producer (for Node)
 * 
 * @author Olivier Corby INRIA
 *
 */

public class Interpreter implements Evaluator, ExprType {
	private static Logger logger = Logger.getLogger(Interpreter.class);

	static final String MEMORY 	= Exp.KGRAM + "memory";
	static final String STACK 	= Exp.KGRAM + "stack";

	protected Proxy proxy;
	Producer producer;
	Object TRUE, FALSE;
	
	int mode = KGRAM_MODE;
	
	public Interpreter(Proxy p){
		proxy = p;
		p.setEvaluator(this);
		TRUE  = proxy.getValue(true);
		FALSE = proxy.getValue(false);
	}
	
	public void setProducer(Producer p){
		producer = p;
	}
	
	public Proxy getProxy(){
		return proxy;
	}
	
	public Node eval(Filter f, Environment env, Producer p) {
		Expr exp = f.getExp();
		Object value = eval(exp, env, p);
		if (value == null) return null;
		return producer.getNode(value);
	}

	public List<Node> evalList(Filter f, Environment env) {
		
		Expr exp = f.getExp();
		switch(exp.oper()){
		
		default:
			Object value = eval(exp, env);
			if (value == null) return null;
			List<Node> lNode = producer.toNodeList(value);
			return lNode;
		}
	}
	
	/**
	 * Functions that return several variables as result such as:
	 * sql("select from where") as (?x ?y)
	 */
	public Mappings eval(Filter f, Environment env, List<Node> nodes) {
		Expr exp = f.getExp();
		switch(exp.oper()){
		
		case UNNEST:
			// unnest(sql()) as ()
			exp = exp.getExp(0);
				
		default:			
			Object res = eval(exp, env);
			if (res == null) return new Mappings();
			return producer.map(nodes, res);
		}
	}
	
	

	public boolean test(Filter f, Environment env) {
            return test(f, env, producer);
        }
        
	public boolean test(Filter f, Environment env, Producer p) {
		Expr exp = f.getExp();
		Object value = eval(exp, env, p);
		if (value == null) return false;
		return proxy.isTrue(value);
	}
	
	Node getNode(Expr var, Environment env){
		return env.getNode(var);
	}
	
	Object getValue(Expr var, Environment env){
		Node node = env.getNode(var);
		if (node == null) return null;
		return node.getValue();
	}
	
	Object getValue(Node node) {
		//return producer.getNodeValue(node);
		return node.getValue();

	}
	
	public Object eval(Expr exp, Environment env){
            return eval(exp, env, producer);
        }

        public Object eval(Expr exp, Environment env, Producer p){
		//System.out.println("Interpret: " + exp + " " + env.getClass().getName());
		switch (exp.type()){
		
		case CONSTANT: 	return exp.getValue(); //return proxy.getConstantValue(exp.getValue());

		case VARIABLE: 	
			Node node = env.getNode(exp);
			if (node == null) return null;
			return node.getValue();
			
		case BOOLEAN: 	return connector(exp, env, p);
		case TERM: 	return term(exp, env, p);
		case FUNCTION: 	return function(exp, env, p);
		}
		return null;
	}



	private Object connector(Expr exp, Environment env, Producer p) {
		switch (exp.oper()){
		case AND: 	return and(exp, env, p);
		case OR: 	return or(exp, env,  p);
		case NOT: 	return not(exp, env, p);
		}	
		return null;
	}

	private Object not(Expr exp, Environment env, Producer p) {
		Object o = eval(exp.getExp(0), env, p);
		if (o == null) return null;
		if (! proxy.isTrueAble(o)) return null;
		if (proxy.isTrue(o)){
			return FALSE;
		}
		else {
			return TRUE;
		}
	}

	private Object or(Expr exp, Environment env, Producer p) {
		boolean error = false;
		for (Expr arg : exp.getExpList()){
			Object o = eval(arg, env, p);
			if (o!=null){
				if (! proxy.isTrueAble(o)) error = true;
				else if (proxy.isTrue(o)) return TRUE;
			}
			else {
				error = true;
			}
		}
		if (error) return null;
		return FALSE;	
	}

	private Object and(Expr exp, Environment env, Producer p) {
		for (Expr arg : exp.getExpList()){
			Object o = eval(arg, env, p);
			if (o == null) return null; 
			if (! proxy.isTrueAble(o)) return null;
			if (! proxy.isTrue(o)) return FALSE;
		}
		return TRUE;
	}

	
	Object function(Expr exp, Environment env, Producer p) {
		
		switch (exp.oper()){
		
		case ENV: return env;
		
		case SKIP: 
		case GROUPBY:
			return TRUE;
				
		case BOUND: 
			Node node = env.getNode(exp.getExp(0));
			if (node == null) return FALSE;
			return TRUE;
		
		case COALESCE:
			for (Expr arg : exp.getExpList()){
				Object o = eval(arg, env, p);
				if (o != null) return o;
			}
			return null;
			
		case EXIST:
			return exist(exp, env, p);
			
		case IF:
			return ifthenelse(exp, env, p);
			
		case LENGTH: {
			Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
			if (qNode == null) return null;
			int value = env.pathLength(qNode);
			return proxy.getValue(value);
		}
			
		case PWEIGHT: {
			Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
			if (qNode == null) return null;
			int value = env.pathWeight(qNode);
			return proxy.getValue(value);
		}	
		
		case COUNT:
		case MIN:
		case MAX:
		case SUM:
		case AVG:
		case SAMPLE:
		case GROUPCONCAT:
                case AGGAND:
			return aggregate(exp, env, p);
			
		case SYSTEM:
			return system(exp, env);	
		
		
		case SELF:
			return eval(exp.getExp(0), env, p);
		
		case CONCAT:
		case EXTERNAL:
			// variable number of args: need array
			break;
			

		default:
			switch (exp.getExpList().size()){
			
			case 0:
				return proxy.function(exp, env, p);

			case 1: 
				Object val = eval(exp.getExp(0), env, p);
				if (val == null) return null;
				return proxy.function(exp, env, p, val);

			case 2: 
				Object value1 = eval(exp.getExp(0), env, p);
				if (value1 == null) return null;
				Object value2 = eval(exp.getExp(1), env, p);
				if (value2 == null) return null;
				return proxy.function(exp, env, p, value1, value2);
			}

		}
		
		Object[] args = evalArguments(exp, env, p);
		if (args == null) return null;		
		Object res = proxy.eval(exp, env, p, args);
		return res;

	}
	
	
	
	
	
	/**
	 * use case:
	 * exp: max(?count)
	 * iterate all values of ?count to get the max
	 */
	Object aggregate(Expr exp, Environment env, Producer p){
		
		switch(exp.oper()){
		
		case COUNT:

			if (exp.arity() == 0){
				return proxy.aggregate(exp, env, p, null);
			}

		default:
			if (exp.arity() == 0) return null;

			Node qNode = null;
			
			if (exp.getExp(0).isVariable()){
				qNode = env.getQueryNode(exp.getExp(0).getLabel());
			}
			
			return proxy.aggregate(exp, env, p, qNode);
		}

	}

	Object[] evalArguments(Expr exp, Environment env, Producer p){
		Object[] args = new Object[exp.arity()];
		int i = 0;
		for (Expr arg : exp.getExpList()){
			Object o = eval(arg, env, p);
			if (o == null) return null;
			args[i++] = o;
		}
		return args;
	}

	
	Object term(Expr exp, Environment env, Producer p){
		
		switch (exp.oper()){
		
		case IN:
			return in(exp, env, p);
		}
		
		Object o1 = eval(exp.getExp(0), env, p);
		if (o1 == null) return null;	
		
		Object o2 = eval(exp.getExp(1), env, p);
		if (o2 == null) return null;
		
		Object res = proxy.eval(exp, env, p, o1, o2);
		return res;
	}
	
	
	Object in(Expr exp, Environment env, Producer p){
		Object o1 = eval(exp.getExp(0), env, p);
		if (o1 == null) return null;
		
		boolean error = false;
		Expr list = exp.getExp(1);
		
		for (Expr arg : list.getExpList()){
			Object o2 = eval(arg, env, p);
			if (o2 == null){
				error = true;
			}
			else {
				Object res = proxy.eval(exp, env, p, o1, o2);
				if (res == null){
					error = true;
				}
				else if (proxy.isTrue(res)){
					return res;
				}
			}
		}
		if (error) return null;
		return FALSE;
	}
	
	
	/**
	 * 
	 * filter(! exists {PAT})
	 */
	Object exist(Expr exp, Environment env, Producer p){
		if (env instanceof Memory){
			Exp pat = env.getQuery().getPattern(exp);
			Memory memory = (Memory) env;
			Node gNode = memory.getGraphNode();
			Eval kgram = memory.getEval();
			Eval eval = kgram.copy(kgram.getMemory(memory, pat), p, this);
			eval.setSubEval(true);
			eval.setLimit(1);
			Mappings lMap = eval.subEval(memory.getQuery(), gNode, Stack.create(pat), 0);
			boolean b = lMap.size() > 0;

			if (b) return TRUE;
			else return FALSE;
		}
		else {
			return null;
		}
	}
	

	Object ifthenelse(Expr exp, Environment env, Producer p){
		Object test = eval(exp.getExp(0), env, p);
		Object value = null;
		if (test == null){
			return null;
		}
		if (proxy.isTrue(test)){
			value = eval(exp.getExp(1), env, p);
		}
		else if (exp.arity() == 3){
			value = eval(exp.getExp(2), env, p);
		}
		return value;
	}
	
	/**
	 * exp : system(kg:memory)
	 */
	Object system(Expr exp, Environment env){
		if (exp.arity()>0){
			Expr arg = exp.getExp(0);
			if (arg.type() == CONSTANT){
				String label = arg.getLabel();
				if (label.equals(MEMORY)){
					return env;
				}
				else if (label.equals(STACK)){
					return ((Memory)env).getStack();
				}
			}
		}
		return env;
	}

	@Override
	public void setMode(int m) {
		mode = m;
		proxy.setMode(m);
	}
	
	public int getMode(){
		return mode;
	}

}
