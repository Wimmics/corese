package fr.inria.edelweiss.engine.model.core;

import java.util.HashMap;
import java.util.Map;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
//import fr.inria.acacia.corese.api.IResult;
//import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.edelweiss.engine.model.api.Bind;
import fr.inria.edelweiss.engine.model.api.Clause;

public class BindImpl 
implements Bind {
	
	private Map<String, Constant> tuples;

	public Map<String, Constant> getTuples() {
		return tuples;
	}

	public void setTuples(Map<String, Constant> tuples) {
		this.tuples = tuples;
	}

	/**
	 * constructor which create an instance of the attribute tuples
	 */
	public BindImpl(){
		super();
		tuples=new HashMap<String,Constant>();
	}
	
	public int size(){
		return tuples.size();
	}
	
	public String toString(){
		String str = "";
		for (String var : tuples.keySet()){
			str += var + " = " + tuples.get(var) + "; ";
		}
		return str;
	}
	
	public boolean equivalent(Bind bind){
		return contains(bind) && bind.contains(this);
	}
	
	public boolean contains(Bind bind){
		for (String var : tuples.keySet()){
			if (! bind.hasVariable(var)){
				return false;
			}
			if (! getDatatypeValue(var).sameTerm(bind.getDatatypeValue(var))){
				return false;
			}
		}
		return true;
	}

	/**
	 * unify ruleConclusion with clauseQuery
	 * bind variables from rule to values of  clause (or clause bind)
	 * 
     * clauseQuery: John grandFather ?y
     * bind : {}
     * rule : construct {?a grandFather ?c} where {?a father ?b . ?b father ?c} 
     * ruleConclusion : ?a grandFather ?c
     * 
     * RETURNS : {?a = John}
     */
	public Bind unify(Clause freeClause, Clause boundClause) {
//		System.out.println(freeClause);
//		System.out.println(boundClause);
//		System.out.println(this);

		//the bind to return containing the mappings between the rule and the clause
		Bind bind = new BindImpl();
		
		int n = 0;
		
		for (ElementClause argFree : freeClause){
			
			if (n >= freeClause.size()) break;
			
			//next element of the clause of the query
			ElementClause argBound = boundClause.get(n++);
			
			//value of the variable element of the rule of the conclusion to put in the bind
			Constant value=null;
			
			if (argFree.isVariable()){
				
				value = getValue(argBound);
				// TODO: draft
				if (value == null && argBound.getAtom().isBlankNode()){
					// transform blank node variable as blank node constant
					value = argBound.getAtom().getConstant();
					//System.out.println("** BI: " + argFree + " " + value);
				}
			}
			
			if (value != null){
				
				Constant cst = bind.getValue(argFree.getName());

				if (cst != null){
					// argFree already bound : check values are the same
					// same variable must be bound to same value
										
					boolean b = cst.getDatatypeValue().sameTerm(value.getDatatypeValue());
					
					if (! b) return null;
										
				}
				
				
				// check this Bind
//				cst = getValue(argFree.getName());
//				
//				if (cst != null){
//					// argFree already bound in this Bind : check values are the same
//					// same variable must be bound to same value
//										
//					boolean b = cst.getDatatypeValue().sameTerm(value.getDatatypeValue());
//					
//					if (! b) return null;
//				}

				
				bind.put(argFree.getName(), value);
			}
		}
		
		return bind;
	}
	
	public Bind get(Clause ruleConclusion, Clause clauseQuery) {
		return unify(ruleConclusion, clauseQuery);
	}
	
	
	/**
	 * Test whether the two bind share the same bindings 
	 * for the clause variables
	 */
	public boolean match(Clause clause, Bind bind){
		for (ElementClause elem : clause){
			if (elem.isVariable()){
				boolean go = true;
				Constant value1 = getValue(elem.getName());
				Constant value2 = bind.getValue(elem.getName());
				if (value1 == null){ 
					if (value2 != null) go = false;
				}
				else if (value2 == null) go = false;
				else if (! value1.getDatatypeValue().sameTerm(value2.getDatatypeValue()))
					go = false;
				
				if (! go){
					return false;
				}
				//System.out.println("** B: " + value1 + " " + value2);
			}

		}
		return  true;
	}
	
	/**
	 * Test whether rule clause match query clause + bind
	 * bind(query) <= rule 
	 * TODO  check a P b vs x R x
	 */
	public boolean match(Clause clauseRule, Clause clause){
		if (clause.size() > clauseRule.size()){		
			return false;
		}

		//true if it has matching
		boolean match=true;

		int n = 0;

		for (ElementClause argQuery : clause){

			ElementClause argRule = clauseRule.get(n);

			if (argRule.isVariable()){
				// OK
			}
			else {
				// rule has constant
				IDatatype dtRule = argRule.getDatatypeValue();
				IDatatype dtQuery = getDatatypeValue(argQuery);

				if (dtQuery != null){
					if (n == Clause.PROPERTY){ 
						//System.out.println("** RI: " + dtQuery + " " + dtRule);
						// check rdf:type 
					}
					match = dtRule.sameTerm(dtQuery);
					if (! match) return false;
				}
			}

			n++;
		}
		
		return true;
	}
	

	public Constant getValue(String variable) {
		return tuples.get(variable);
	}
	
	public Constant getValue(ElementClause e) {
		if (e.isConstant()){
			return e.getConstant();
		}
		else {
			return getValue(e.getName());
		}
	}
	
	
	public IDatatype getDatatypeValue(ElementClause e){
		Constant cst = getValue(e);
		if (cst == null) return null;
		return cst.getDatatypeValue();
	}
	
	public IDatatype getDatatypeValue(String var){
		Constant cst = getValue(var);
		if (cst == null) return null;
		return cst.getDatatypeValue();
	}

	public boolean hasVariable(String variable) {
		return tuples.containsKey(variable);
	}

	public void put(String key, Constant value) {
		tuples.put(key, value);
	}
	
	public void put(Bind bind) {
		for(String key:bind.getTuples().keySet()){
			tuples.put(key, bind.getValue(key));
		}
	}

	/**
	 * 	RETURNS a new object Bind with a new attribute Map 
	 * 	containing the same values of the first object Bind
	 */
	public Bind cloneBind() {
		// create a new Bind with a new attribute Map
		Bind bind=new BindImpl();
		
		//Iteration of the attribute Map of the object bind calling this method 
		for(String key:tuples.keySet()){
			//put each value of the object bind calling this method in the new bind created above
			bind.put(key, tuples.get(key));
		}
		
		return bind;
	}

	public void remove(String key) {
		tuples.remove(key);
	}
	
	
	/**************** IResult **********************/
	
	
//	public Iterable<String> getVariables() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	public boolean includes(IResult r){
//		return false;
//	}
//	
//	public boolean matches(IResult r){
//		return false;
//	}
//	
//	public  IResultValue getResultValue(String var){
//		if (isBound(var)){
//			return ResultValue.create(getValue(var).getDatatypeValue());
//		}
//		else return null;
//	}
//	
//	
//	public void setResultValue(String variableName, IResultValue value) {
//		// TODO Auto-generated method stub
//	}
//	
//
//	public IResultValue[] getResultValues(String var){
//		if (! isBound(var)) return null;
//		IResultValue[] res = new IResultValue[1];
//		res[0] = getResultValue(var);
//		return res;
//	}
//
//	public String[] getStringValues(String var){
//		if (! isBound(var)) return null;
//		String[] res = new String[1];
//		res[0] = getValue(var).getDatatypeValue().getLabel();
//		return res;
//	}
//
//	public String getStringValue(String var){
//		return getValue(var).getDatatypeValue().getLabel();
//	}
//
//	public String[] getSPARQLValues(String var){
//		return getStringValues(var);
//	}
//
//	public  double getSimilarity(){
//		return -1;
//	}
//
//	public  boolean isBound(String var){
//		return hasVariable(var);
//	}
//	
//	
//	public IResultValue[] getAllResultValues(String variableName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	
}
