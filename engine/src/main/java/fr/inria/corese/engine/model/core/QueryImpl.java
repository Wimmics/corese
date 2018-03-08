package fr.inria.corese.engine.model.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.corese.engine.model.api.Clause;
import fr.inria.corese.engine.model.api.ExpFilter;
import fr.inria.corese.engine.model.api.Query;

public class QueryImpl implements Query {
	private static Logger logger = LogManager.getLogger(QueryImpl.class);

	//body
	private List<Clause> clauses;
	private List<ExpFilter> filters;
	
	//head case select
	private List<String> variables;
	
	private String sparqlQueryString;
	
	private boolean ask;
	private boolean select;
	private boolean construct;
	
	boolean isSparql = false;
	
	private ASTQuery ast;
	
	public List<String> getVariables() {
		return variables;
	}
	
	public void setVariables(List<String> list){
		variables = list;
	}

	public String getSparqlQueryString() {
		return sparqlQueryString;
	}
	
	public boolean isSparql(){
		return isSparql || ast.getPragma() != null;
	}

	public boolean isAsk() {
		return ask;
	}

	public boolean isSelect() {
		return select;
	}

	public boolean isConstruct() {
		return construct;
	}

	public List<Clause> getClauses() {
		return clauses;
	}

	public void setClauses(List<Clause> clauses) {
		this.clauses = clauses;
	}
	
	public List<ExpFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<ExpFilter> filters) {
		this.filters = filters;
	}

	/**
	 * constructor which create instances of the objects clauses and filters
	 */
	public QueryImpl() {
		clauses=new ArrayList<Clause>();
		filters=new ArrayList<ExpFilter>();
	}
	
	public ASTQuery getASTQuery(){
		return ast;
	}
	
	// for the initial query
	public static QueryImpl create(ASTQuery ast){
		return new QueryImpl(ast, true);
	}
	

	public QueryImpl(ASTQuery ast) {
		this(ast, false); 
	}
	
	/**
	 * constructor which create instances of the objects clauses and filters
	 */
	public QueryImpl(ASTQuery sparqlQuery, boolean isQuery) {
		ast = sparqlQuery;
		//initialize the elements of the query
		clauses=new ArrayList<Clause>();
		filters=new ArrayList<ExpFilter>();
		variables=new ArrayList<String>();
		
		//set the SparqlQuery
		sparqlQueryString=sparqlQuery.getText();
		
		//set the attributes ask, select and construct
		ask=sparqlQuery.isAsk();
		select=sparqlQuery.isSelect();
		construct=sparqlQuery.isConstruct();
		
		
		Exp expBody = sparqlQuery.getQuery();

		if (expBody.size()==1 && expBody.get(0).isScope()){
			// get the GraphPattern:
			expBody = expBody.get(0).get(0);
			isSparql = true;
		}

		//Iteration of the body of the rule

		for (Exp exp : expBody.getBody()){
			
				process(sparqlQuery, exp, isQuery);
		}

		//case select : set the name of the variables
		if (select){
			for(Variable variable:sparqlQuery.getSelectVar()){
				variables.add(variable.getName());
			}
		}
	}
	
	
	void process(ASTQuery sparqlQuery, Exp exp, boolean isQuery){
		if (exp.isTriple()){

			if (! exp.isFilter()){
				//case the expression is a triple

				//create a new object Clause as an element of the query
				Clause clause = ClauseImpl.condition(sparqlQuery, exp);
				

				//add the clause to the query
				addClause(clause);
			}
			else {
				//case the expression is a filter

				//create a new object expression filter as an element of the query
				ExpFilter expFilter=new ExpFilterImpl(exp);

				//add the filter to the query
				addFilter(expFilter);
			}
		}
		else if (exp.isScope()){
			Exp body = exp.get(0);
			for (Exp ee : body.getBody()){
				Clause clause =  ClauseImpl.condition(sparqlQuery, ee);
				clause.setGround(true);
				addClause(clause);
			}
		}
		else if (exp.isGraph()){
			Source src = (Source) exp;
			// get the BasicGraphPattern inside:
			exp = src.getBody().get(0);
			for (Exp ee : exp.getBody()){
				if (ee.isTriple()){
					Triple t = ee.getTriple();
					t.setVSource(src.getSource());
					process(sparqlQuery, ee, isQuery);
				}
			}
		}
		else if (exp.isAnd()){
			for (Exp ee : exp.getBody()){
				process(sparqlQuery, ee, isQuery);
			}
		}
		else {
			logger.error("Parsing, not a triple: " + exp);
		}
	}
	
	
	
	
	
	
	
	
	
	

	/**
	 * to iterate the set of clauses of the query
	 */
	public Iterator<Clause> iterator() {
		return clauses.iterator();
	}

	/**
     * RETURNS the first clause of the query calling the method 
     */
	public Clause getClause() {
		//the first clause of the query
		return clauses.get(0);
	}

	public boolean isEmpty() {
		return clauses.isEmpty();
	}
	
	public void addClause(Clause clause) {
		clauses.add(clause);
	}
	
	public void addFilter(ExpFilter expFilter) {
		filters.add(expFilter);
	}
	
}
