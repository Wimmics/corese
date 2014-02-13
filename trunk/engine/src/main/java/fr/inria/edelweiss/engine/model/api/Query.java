package fr.inria.edelweiss.engine.model.api;

import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;

public interface Query extends Iterable<Clause> {
	
	/**
	 * getter and setter of the instance clauses
	 */
	public List<Clause> getClauses();
	public void setClauses(List<Clause> clauses);
	
	/**
	 * getter and setter of the instance filters
	 */
	public List<ExpFilter> getFilters();
	public void setFilters(List<ExpFilter> filters);
	
	/**
	 * getters of the instances ask, select and construct
	 */
	public boolean isAsk();
	public boolean isSelect();
	public boolean isConstruct();
	
	// full sparql, not for backward engine
	public boolean isSparql();

	/**
	 * add a clause to the query
	 */
	public void addClause(Clause clause);
	
	/**
	 * add a filter to the query
	 */
	public void addFilter(ExpFilter expFilter);
	
	/**
	 * get the first clause of the query
	 */
	public Clause getClause();
	
	/**
	 * the query is empty ?
	 */
	public boolean isEmpty();
	
	/**
	 * get the String of the SparqlQuery
	 */
	public String getSparqlQueryString();
	
	/**
	 * get the variables of the head of the query
	 */
	public List<String> getVariables();
	
	public ASTQuery getASTQuery();
	
}
