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
		TRUE = proxy.getValue(true);
		FALSE = proxy.getValue(false);
	}
	
	public void setProducer(Producer p){
		producer = p;
	}
	
	public Proxy getProxy(){
		return proxy;
	}
	
	public Node eval(Filter f, Environment env) {
		Expr exp = f.getExp();
		Object value = eval(exp, env);
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
			Object[] args = evalArguments(exp, env);
			if (args == null) return null;
			Object res = proxy.eval(exp, env, args);
			if (res == null) return new Mappings();
			return producer.map(nodes, res);

		}
	}
	
	

	public boolean test(Filter f, Environment env) {
		Expr exp = f.getExp();
		Object value = eval(exp, env);
		if (value == null) return false;
		return proxy.isTrue(value);
	}
	
	Node getNode(Expr var, Environment env){
		return env.getNode(var);
	}
	
	Object getValue(Expr var, Environment env){
		Node node = getNode(var, env);
		if (node == null) return null;
		return getValue(node);
	}
	
	Object getValue(Node node) {
		//return producer.getNodeValue(node);
		return node.getValue();

	}
	
	public Object eval(Expr exp, Environment env){
		//System.out.println("Interpret: " + exp + " " + env.getClass().getName());
		switch (exp.type()){
		case CONSTANT: 	return proxy.getConstantValue(exp.getValue());
		case VARIABLE: 	return getValue(exp, env);
		case BOOLEAN: 	return connector(exp, env);
		case TERM: 		return term(exp, env);
		case FUNCTION: 	return function(exp, env);
		}
		return null;
	}



	private Object connector(Expr exp, Environment env) {
		switch (exp.oper()){
		case AND: 	return and(exp, env);
		case OR: 	return or(exp, env);
		case NOT: 	return not(exp, env);
		}	
		return null;
	}

	private Object not(Expr exp, Environment env) {
		Object o = eval(exp.getExp(0), env);
		if (o == null) return null;
		if (! proxy.isTrueAble(o)) return null;
		if (proxy.isTrue(o)){
			return FALSE;
		}
		else {
			return TRUE;
		}
	}

	private Object or(Expr exp, Environment env) {
		boolean error = false;
		for (Expr arg : exp.getExpList()){
			Object o = eval(arg, env);
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

	private Object and(Expr exp, Environment env) {
		for (Expr arg : exp.getExpList()){
			Object o = eval(arg, env);
			if (o == null) return null; 
			if (! proxy.isTrueAble(o)) return null;
			if (! proxy.isTrue(o)) return FALSE;
		}
		return TRUE;
	}

	
	Object function(Expr exp, Environment env) {
		
		switch (exp.oper()){
		
		case ENV: return env;
		
		case SKIP: return TRUE;
				
		case BOUND: 
			Node node = getNode(exp.getExp(0), env);
			return proxy.getValue(node != null);
		
		case COALESCE:
			for (Expr arg : exp.getExpList()){
				Object o = eval(arg, env);
				if (o != null) return o;
			}
			return null;
			
		case EXIST:
			return exist(exp, env);
			
		case IF:
			return ifthenelse(exp, env);
			
		case LENGTH:
			Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
			if (qNode == null) return null;
			int value = env.pathLength(qNode);
			return proxy.getValue(value);	
		
		case COUNT:
		case MIN:
		case MAX:
		case SUM:
		case AVG:
		case SAMPLE:
		case GROUPCONCAT:
			return aggregate(exp, env);
		
		}
		
		
		Object[] args = evalArguments(exp, env);
		if (args == null) return null;
		
		switch (exp.oper()){
			case SELF: return args[0];
			
			case SYSTEM:
				return system(exp, env);
		}
		
		Object res = proxy.eval(exp, env, args);
		return res;
	}
	
	
	
	
	
	/**
	 * use case:
	 * exp: max(?count)
	 * iterate all values of ?count to get the max
	 */
	Object aggregate(Expr exp, Environment env){
		
		switch(exp.oper()){
		
		case COUNT:

			if (exp.arity() == 0){
				return proxy.aggregate(exp, env, null);
			}

		default:
			if (exp.arity() == 0) return null;

			Node qNode = null;
			
			if (exp.getExp(0).isVariable()){
				qNode = env.getQueryNode(exp.getExp(0).getLabel());
			}
			
			return proxy.aggregate(exp, env, qNode);
		}

	}

	Object[] evalArguments(Expr exp, Environment env){
		Object[] args = new Object[exp.arity()];
		int i = 0;
		for (Expr arg : exp.getExpList()){
			Object o = eval(arg, env);
			if (o == null) return null;
			args[i++] = o;
		}
		return args;
	}

	Object term(Expr exp, Environment env){
		switch (exp.oper()){
		case IN:
			// ?x in (?y, ?z)
			return in(exp, env);
		}
		
		Object o1 = eval(exp.getExp(0), env);
		if (o1 == null) return null;		
		Object o2 = eval(exp.getExp(1), env);
		if (o2 == null) return null;
		Object res = proxy.eval(exp, env, o1, o2);
		return res;
	}
	
	
	Object in(Expr exp, Environment env){
		Object o1 = eval(exp.getExp(0), env);
		if (o1 == null) return null;	
		boolean error = false;
		
		Expr list = exp.getExp(1);
		for (Expr arg : list.getExpList()){
			Object o2 = eval(arg, env);
			if (o2 == null){
				error = true;
			}
			else {
				Object res = proxy.eval(exp, env, o1, o2);
				if (proxy.isTrue(res)){
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
	Object exist(Expr exp, Environment env){
		if (env instanceof Memory){
			Exp pat = env.getQuery().getPattern(exp);
			Memory memory = (Memory) env;
			Node gNode = memory.getGraphNode();
			Eval kgram = memory.getEval();
			Eval eval = kgram.copy(kgram.getMemory(memory, pat), producer, this);
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
	

	Object ifthenelse(Expr exp, Environment env){
		Object test = eval(exp.getExp(0), env);
		Object value = null;
		if (test == null){
			return null;
		}
		if (proxy.isTrue(test)){
			value = eval(exp.getExp(1), env);
		}
		else if (exp.arity() == 3){
			value = eval(exp.getExp(2), env);
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
