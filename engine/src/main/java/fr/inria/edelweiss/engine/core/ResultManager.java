package fr.inria.edelweiss.engine.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

 
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResults;
//import fr.inria.acacia.corese.cg.JSONBinding;
//import fr.inria.acacia.corese.cg.XMLBinding;
import fr.inria.edelweiss.engine.model.api.Bind;
import fr.inria.edelweiss.engine.model.api.LBind;
import fr.inria.edelweiss.engine.model.api.Query;

public class ResultManager implements IResults {

	
	private LBind lBind;
	private Query query;

	public static ResultManager create(LBind lBind,Query query){
		ResultManager res = new ResultManager(lBind, query);
		return res;
	}
	
	public ResultManager(LBind lBind,Query query) {
		this.lBind = lBind;
		this.query=query;
	}
	
	public void print(){
		printResults();
	}
	
	public LBind getLBind(){
		return lBind;
	}
	
	public Query getQuery(){
		return query;
	}
	
	public void printResults(){
		
		System.out.println(query.getSparqlQueryString());
		
		if (query.isAsk()){
			if (lBind.isTripleFound()){
				System.out.println("OUI");
			}
			else {
				System.out.println("NON");
			}
		}
		else if(query.isSelect()){
			for(Bind bind2:lBind){
				
				for(String variable : query.getVariables()){
    				System.out.print(variable+" = "+
    					bind2.getValue(variable) + "\n");
    			}
				
    			System.out.println("___\n");
    		}
		}
		
	}
	
	public String toString(){
		return lBind.toString();
	}
	
	public String toJSON(){
		return lBind.toString();
	}
	

	/********************** IResults **************************/
	
	
	public boolean getSuccess(){
		return lBind.isTripleFound();
	}

	/**
	 * 
	 * @return enumerations&lt;IResult&gt; of arrays of IResult
	 */
	public Enumeration<IResult> getResults(){
		Vector<IResult> vec = new Vector<IResult>();
		for (Bind bind : lBind){
			vec.add(bind);
		}
		return vec.elements();
	}
	
	public boolean includes(IResult r){
		return false;
	}
	
	public IResults union(IResults r){
		return this;
	}

	public IResults inter(IResults r){
		return this;
	}

	public IResults minus(IResults r){
		return this;
	}
	
	
	public Iterator<IResult> iterator(){
		ArrayList<IResult> vec = new ArrayList<IResult>();
		for (Bind bind : lBind){
			vec.add(bind);
		}
		return vec.iterator();
	}
	
	/**
	 * 
	 * @return an array of String that contains selected variables
	 */
	public String[] getVariables(){
		List<String> vars = query.getVariables();
		String[] array = new String[vars.size()];
		int i = 0;
		for (String var : vars){
			array[i++] = var;
		}
		return array;
	}
	
	public void defVariable(String name){
		
	}

	/**
	 * @return the number of graphs in the result
	 */
	public int size(){
		return lBind.size();
	}

	@Override
	public IResult get(int i) {
		// TODO Auto-generated method stub
		return lBind.get(i);
	}

	@Override
	public void remove(IResult r) {
		// TODO Auto-generated method stub
		lBind.remove(r);
	}
	
	@Override
	public void add(IResult r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(int n, IResult r) {
		// TODO Auto-generated method stub
		
	}
	
	
	public String toCoreseResult(){
		return toString();
	}
	
	
	public String toSPARQLResult(){
		return toString();
	}
		
	/**
	 * Returns the clause of the query.<br />
	 * @return if the query was a SELECT, ASK, CONSTRUCT or DESCRIBE clause<br /> 
	 */
	public int getClause(){
		if (isSelect()) return CL_SELECT;
		else if (isConstruct()) return CL_CONSTRUCT;
		else if (isAsk()) return CL_ASK;
		else if (isDescribe()) return CL_DESCRIBE;
		else return -1;
	}
	
	public boolean isSelect(){
		return query.isSelect();
	}
	
	public boolean isConstruct(){
		return query.isConstruct();
	}
	
	public boolean isDescribe(){
		return false;
	}
	
	public boolean isAsk(){
		return query.isAsk();
	}

	
}
