package fr.inria.corese.engine.model.api;

import java.util.Map;

import fr.inria.acacia.corese.api.IDatatype;
//import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.acacia.corese.triple.parser.Constant;


public interface Bind //extends IResult
{
	
	/**
	 * getter and setter of the instance tuples
	 */
	public Map<String, Constant> getTuples();
	public void setTuples(Map<String, Constant> tuples);
	
	/**
	 * returns the bindings between the clause of the query and the conclusion of the rule
	 */
	public Bind get(Clause ruleConclusion, Clause clauseQuery);
	
	/**
	 * returns true if the instance tuples has as key the variable given
	 */
	public boolean hasVariable(String variable);
	
	public int size();
	
	/**
	 * returns the value of the variable given, in the Map tuples
	 */
	public Constant getValue(String variable);
	
	public Constant getValue(ElementClause e) ;

	public IDatatype getDatatypeValue(ElementClause e);
	
	public IDatatype getDatatypeValue(String var);
	
	/**
	 * put the value of the key given, in the Map tuples
	 */
	public void put(String key,Constant value);
	
	/**
	 * put the values of a bind in the object bind in process
	 */
	public void put(Bind bind);
	
	public boolean equivalent(Bind bind);
	
	public boolean contains(Bind bind);
	/**
	 * create a new instance of the object Bind with the same values of the instance Bind calling the method
	 */
	public Bind cloneBind(); 
	
	/**
	 * remove the value of the key given, in the Map tuples
	 */
	public void remove(String key);
	
	public Bind unify(Clause freeClause, Clause boundClause);
	
	public boolean match(Clause clause, Bind bind);
	
	public boolean match(Clause clauseRule, Clause clause);

	
}
