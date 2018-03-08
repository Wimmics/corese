package fr.inria.corese.engine.model.api;


import java.util.List;

import fr.inria.corese.sparql.triple.parser.ASTQuery;

//import fr.inria.corese.sparql.api.IResult;


public interface LBind extends Iterable<Bind> {
	
	ASTQuery getAST();
	
	/**
	 * getter and setter of the instance lBindInstance
	 */
	public List<Bind> getLBindInstance();
	public void setLBindInstance(List<Bind> lBindInstance);
	
	/**
	 * getter and setter of the instance tripleFounded
	 */
	public boolean isTripleFound();
	public void setTripleFounded(boolean tripleFounded);
	
	/**
	 * union of the lists of binds
	 */
	public LBind union(LBind lBind);
	
	public LBind union(Bind bind);

	/**
	 * rename the variables of the rule conclusion to the variables of the clause of the query corresponding to them
	 */
	public LBind rename(Clause ruleConclusion,Clause clauseQuery, Bind oldBind);
	
	/**
	 * add an instance of Bind in the list of binds
	 */
	public void add(Bind bind);
	
	public int size();
	
	//public IResult get(int i);
	
	//public void remove(IResult r);
	
	

}
