package fr.inria.edelweiss.kgenv.eval;

import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.core.Group;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.filter.Proxy;

/**
 * Interpreter that perfom aggregate over current group list of Mapping
 * The eval function is called by Mappings
 * The eval() function can only compute aggregates
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 * 
 */
class Walker extends Interpreter {
	static IDatatype ZERO = DatatypeMap.ZERO;
	
	Expr exp;
	Node qNode, tNode;
	//double sum = 0;
	IDatatype dtres;
	int num = 0;
	boolean isError = false;
	String str = "", sep = " ";
	Group group;
	Evaluator eval;
	
	
	
	Walker(Expr exp, Node qNode, Proxy p, Environment env){
		super(p);
		eval = p.getEvaluator();
		this.exp = exp;
		this.qNode = qNode;
		if (exp.getModality() != null){
			sep = exp.getModality();
		}
		
		if (exp.isDistinct()){
			// use case: count(distinct ?name)
			List<Node> nodes;
			if (exp.arity() == 0){
				// count(distinct *)
				nodes = env.getQuery().getNodes();
			}
			else {
				// TODO: count(distinct foo(?x))
				// TODO: store nodes
				nodes = env.getQuery().getAggNodes(exp.getFilter());
			}
			group = Group.create(nodes);
			group.setDistinct(true);
			group.setDuplicate(env.getQuery().isDistribute());
		}
	}
	
		
	Object getResult(){
		
		if (isError) return null;
		
		switch (exp.oper()){
		
		case SAMPLE:
		case MIN:	
		case MAX:
			return dtres;
		
			
		case SUM: 
			if (dtres == null){
				return ZERO;
			}
			else { 
				return dtres;
			}

		case AVG: 
			if (dtres == null){
				return ZERO;
			}
						
			try {
				Object dt = dtres.div(DatatypeMap.newInstance(num));
				return dt;
			}
			catch (java.lang.ArithmeticException e){
				return null;
			}


		case COUNT:
			return proxy.getValue(num);

		case GROUPCONCAT:
			// remove last sep:
			if (str.length()>0){
				str = str.substring(0, str.length()-sep.length());
			}
			return proxy.getValue(str);
		}
		
		return null;
	}
	
	
	/**
	 * if aggregate is distinct, check that map is distinct
	 */
	boolean accept(Filter f, Mapping map){
		if (f.getExp().isDistinct()){
			boolean b = group.add(map);
			return b;
		}
		return true;
	}
	
	
	/**
	 * map is a Mapping
	 */
	public Node eval(Filter f, Environment env){
		Mapping map = (Mapping) env;
		
		switch (exp.oper()){
		
		case GROUPCONCAT:
			
			// concat may have several arguments for one mapping
			// hence loop on args
			if (accept(f, map)){
				int i = 0;
				
				for (Expr arg : exp.getExpList()){
					IDatatype dt = null;			
					dt = (IDatatype) eval.eval(arg, env);				
					if (dt != null){
						str += dt.getLabel() + sep;
					}		
				}
			}
			return null;
			
			
		case COUNT:
			
			if (exp.arity() == 0){
				// count(*)
				if (accept(f, map)){
					num++;
				}
				return null;
			}
		}
		
		
		if (exp.arity() == 0) return null;
		
		Expr arg = exp.getExp(0);
		Node node = null;
		IDatatype dt = null;
		
		if (arg.isVariable()){
			// sum(?x)
			// get value from Node Mapping
			node = map.getTNode(qNode);
			if (node == null) return null;
			dt = (IDatatype) node.getValue();
		}
		else {
			// sum(?x + ?y)
			// eval ?x + ?y
			dt = (IDatatype) eval.eval(arg, env);
		}
		
		if (dt != null){

			switch (exp.oper()){
			
			case MIN:
				if (dtres == null){
					dtres = dt;
				}
				else {					
					try {
						if (dt.less(dtres)){
							dtres = dt;
						}
						
					} catch (CoreseDatatypeException e) {
					}
				}
				break;
				
			case MAX:
				if (dtres == null){
					dtres = dt;
				}
				else {
					try {
						if (dt.greater(dtres)){
							dtres = dt;
						}
					} catch (CoreseDatatypeException e) {
					}
				}
				break;

			case COUNT:
				if (accept(f, map)){
					num++;
				}
				break;

			case SUM:
			case AVG:
				if (! dt.isNumber()){
					isError = true;
				}
				else if (accept(f, map)){
					if (dtres == null){
						dtres = dt;
					}
					else {
						dtres = dtres.plus(dt);
					}
					
					//sum += dt.getDoubleValue();
					num++;
				}
				break;

			case SAMPLE:
				if (dtres == null && dt != null){
					dtres = dt;
				}
				break;

			
			}
		}

		return null;
	}
	

}