package fr.inria.corese.engine.tool.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import fr.inria.corese.sparql.api.IDatatype;

import fr.inria.corese.sparql.triple.api.ElementClause;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;

import fr.inria.corese.engine.model.api.Bind;
import fr.inria.corese.engine.model.api.Clause;
import fr.inria.corese.engine.model.api.ExpFilter;
import fr.inria.corese.engine.model.api.LBind;
import fr.inria.corese.engine.model.api.Query;
import fr.inria.corese.engine.model.core.ExpFilterImpl;
import fr.inria.corese.engine.model.core.LBindImpl;
import fr.inria.corese.engine.tool.api.EventsTreatment;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.tool.EnvironmentImpl;
import fr.inria.corese.kgenv.eval.QuerySolver;


/**
 * Proxy to IEngine graph match and filter evaluator (e.g. Producer & Evaluator)
 * @author corby
 *
 */
public class EventsTreatmentImpl 
extends EnvironmentImpl
implements EventsTreatment {

	boolean hasEvent = false;
	Bind bind;
	boolean kgram = true;
	Producer prod;
	Evaluator eval;
	QuerySolver exec;
	
	
	public EventsTreatmentImpl(QuerySolver exec){
		this.exec = exec;
		eval = exec.getEvaluator();
		prod = exec.getProducer();
	}

	
	/**
	 * All we need to get the value of a variable for kgram interpreter
	 * from a Bind as a Node 
	 */
	public Node getNode(Expr var){
		IDatatype dt = bind.getDatatypeValue(var.getLabel());
		if (dt == null) return null;
		return prod.getNode(dt);
	}
	
	/**
	 * Eval a filter against a binding
	 */
	public boolean test(Query query, ExpFilter filter, Bind b, int level, int n){		
		bind = b;
		ASTQuery ast = query.getASTQuery();
		filter.getExpression().compile(ast);
		boolean suc = eval.test(filter.getExpression(), this);
		return suc;
	}


	
	/**
	 * query RDF graph using SPARQL to find occurrences of clause
     * clause: ?a father ?b
     * bind: {?a = John}
     * some events : John father Mark . Mark father Pierre . Mark mother Helene .
     * RETURNS :
     * {{?a = John , ?b = Mark}}
     */
	public LBind searchTriples(Query query, Clause clause, Bind bind) {
		List<Clause> list = new ArrayList<Clause>();
		list.add(clause);
		return searchTriples(query, list, bind);
	}
	
	
	
	public LBind searchTriples(Query query, List<Clause> lClause, Bind bind) {
				
		//create the basic graph pattern containing the triples
		BasicGraphPattern pat = BasicGraphPattern.create();

		for (Clause clause : lClause){

			for (ElementClause arg : clause){
				//iterate on the elements of the clause of the query

				if (arg.isVariable()){
					//case the element of the clause is variable

					//get the variable
					Variable var=arg.getVariable();

					if (bind.hasVariable(var.getName())){
						//case the bind contains the element of the clause

						//value of the element of the clause
						Constant value=bind.getValue(var.getName());

						//add the constant to the list of elements of the triple
						//elementsTriple.add(value.getConstant());

						// add filter var = cst
						Triple triple = ExpFilterImpl.createEQ(var, value);

						pat.add(triple);
					}
				}	
			}

			if (! query.isSparql()){
				//create the filters corresponding to the clause
				for (ExpFilter filter: query.getFilters()){

					//for each expression filter to the query
					if (filter.isCorresponding(clause, bind)){

						//the filter concern clause
						Triple triple = filter.createFilter(bind);
						pat.add(triple);
					}
				}
			}
		
			//create the triple of the query
			//Triple triple = create(clause);
			Triple triple = clause.getTriple();
			//add the triple to the basic graph pattern
			pat.add(triple);
		}
		
		if (query.isSparql()){
			//deprecated
			// add all filters because there are all clauses
			for (ExpFilter filter: query.getFilters()){
				pat.add(filter.createFilter(bind));				
			}
		}
		
		//create the astQuery
		ASTQuery ast = ASTQuery.create(pat);
		
		//select all the variables
		ast.setSelectAll(true);
		
		ASTQuery src = query.getASTQuery();
		ast.setDebug(src.isDebug());
		ast.setNSM(src.getNSM());
		
		if (query.isSparql()){
			//deprecated
			// select fun(?x) as ?y
			for (String var : src.getSelectFunctions().keySet()){
				ast.setSelect(new Variable(var), src.getSelectFunctions().get(var));
			}
		}
		
		return query(ast,  bind);
	}
	
	/**
	 * TODO:
	 * fix it
	 * compiler may change triple if:
	 * undefined relation (set a variable)
	 */
	Triple create(Clause clause){
		
		Iterator<ElementClause> it=clause.iterator();
		Triple triple = Triple.create(
				it.next().getAtom(), it.next().getAtom(), it.next().getAtom());
		if (it.hasNext()){
			// there is a source
			triple.setVSource(it.next().getAtom());
		}
		return triple;
	}
	
	
	LBind query(ASTQuery ast, Bind bind){

		Mappings res = exec.basicQuery(ast);

		//System.out.println("** ET: " + res);
		//create the list of binds to register the results
		LBind lBind=new LBindImpl();

		if (res == null){
			return lBind;
		}

		return process(bind, lBind, res);
	}
	
	
	
	/**
	 * Build Binding from IResult
	 * Special case:
	 * when ?x = Array in IResult, generate one binding for each value of ?x in the array
	 * when ?x = Array and ?y = Array
	 * we suppose that they have the same length and we generate one binding for each
	 * ?x[i] ?y[i]
	 * use case: 
	 * construct {?x ex:rel ?y}
	 * select sql('select X, Y from EMP where ...') as (?x, ?y)
	 * where {}
	 */
	
	IDatatype getDatatypeValue(Mapping map, Node qNode){
		Node node = map.getNode(qNode);
		if (node == null) return null;
		return (IDatatype) node.getValue();
	}
	
	LBind process(Bind bind, LBind lBind, Mappings res){

	    for (Mapping map : res){
	    	//set that we have at least a triple in the results
	    	lBind.setTripleFounded(true);
		  	
		  	//create another bind containing the values of the bind in process
	    	Bind bind2=bind.cloneBind();

	    	boolean ok = true;

	    	for (Node var : res.getSelect()){
	    		//iterate the list of variables of the query
	    		//put the value of the variable in the element bind
	    		IDatatype dt1 = getDatatypeValue(map, var);

	    		if (ok &&  dt1 != null){

	    			IDatatype dt0 = bind.getDatatypeValue(var.getLabel());

	    			if (dt0!=null && ! dt0.sameTerm(dt1)){
	    				// skip current res, consider next
	    				ok = false;
	    			}
	    			else {
	    				bind2.put(var.getLabel(), Constant.create(dt1));
	    			}
	    		}
	    	}

	    	if (ok){
	    		//add the bind created to the list of binds to return
	    		lBind.add(bind2);
	    	}

	    }

	    //return the list of binds
		return lBind;
	}
	
	
	

	

}
