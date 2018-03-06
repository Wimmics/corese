package fr.inria.edelweiss.kgengine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.print.JSONFormat;
import fr.inria.corese.kgtool.print.RDFFormat;
import fr.inria.corese.kgtool.print.XMLFormat;

public class QueryResults implements IResults
{
	
	VResult results;
	ArrayList<String> select;
	ASTQuery ast;
	Mappings map;
	
	
	QueryResults(Mappings r, ASTQuery aa){
		map = r;
		ast = aa;
		select = new ArrayList<String>();
		for (Node node : r.getSelect()){
			select.add(node.getLabel());
		}
		results = new VResult();
		for (Mapping m : r){
			results.add(new QueryResult(m, aa));
		}
	}
	
	public static QueryResults create(Mappings r){
		ASTQuery ast = (ASTQuery) r.getQuery().getAST();
		return new QueryResults(r, ast);
	}
	
	public ASTQuery getAST(){
		return ast;
	}
	
	public NSManager getNSM(){
		return ast.getNSM();
	}
	
	public Mappings getMappings(){
		return map;
	}
	
	public Graph getGraph(){
		return (Graph) getMappings().getGraph();
	}
	
	public Iterable<Entity> getEdges(){
		Graph g = getGraph();
		if (g == null) return new ArrayList<Entity>();
		return g.getEdges();
	}
	
	class VResult extends ArrayList<IResult> {}

	
	
	public void add(IResult r) {
		
	}

	
	public void add(int n, IResult r) {
		
		
	}

	
	public void defVariable(String name) {
		
		
	}

	
	public IResult get(int i) {
		
		return results.get(i);
	}

	
	public int getClause() {
		
		return 0;
	}

	
	public Enumeration<IResult> getResults() {
		
		final Iterator<IResult> it = results.iterator();
		
		return new Enumeration<IResult>(){

			
			public boolean hasMoreElements() {
				
				return it.hasNext();
			}

			
			public IResult nextElement() {
				
				return it.next();
			}
			
		};	}

	
	public boolean getSuccess() {
		
		return size()>0;
	}

	
	
	public String[] getVariables() {
				
		String[] aa = new String[select.size()];
		int i = 0;
		for (String s : select){
			aa[i++] = s;
		}
		//select.copyInto(aa);
		return aa;
	}
	

	
	public boolean includes(IResult r) {
		
		return false;
	}

	
	public IResults inter(IResults r) {
		
		return null;
	}

	
	public boolean isAsk() {
		
		if (ast!=null) return ast.isAsk();
		return false;
	}

	
	public boolean isConstruct() {
		
		if (ast!=null){
			// gui do not draw graph with insert
			return ast.isConstruct() && ! ast.isInsert();
		}
		return false;
	}

	
	public boolean isDescribe() {
		
		if (ast!=null) return ast.isDescribe();
		return false;
	}

	
	public boolean isSelect() {
		
		if (ast!=null) return ast.isSelect();
		return true;
	}

	
	public IResults minus(IResults r) {
		
		return null;
	}

	
	public void remove(IResult r) {
		
		
	}

	
	public int size() {
		
		return results.size();
	}

	
	public String toCoreseResult() {
		
		return toString();
	}

	
	public String toJSON() {
		
		JSONFormat xml = JSONFormat.create(map);
		return xml.toString();
		}

	
	public String toSPARQLResult() {
		if (ast.isConstruct()){
			RDFFormat rdf = RDFFormat.create(map);
			return rdf.toString();
		}
		else {
			XMLFormat xml = XMLFormat.create(map);
			return xml.toString();
		}
	}
	
	public String toString(){
		return toSPARQLResult();
	}

	
	public IResults union(IResults r) {
		
		if (r instanceof QueryResults){
			QueryResults qr = (QueryResults) r;
			Mappings res = Mappings.create(map.getQuery());
			
			for (Mapping m : getMappings()){
				res.add(m);
			}
			for (Mapping m : qr.getMappings()){
				res.add(m);
			}
			return create(res);
		}
		return this;
	}

	
	public Iterator<IResult> iterator() {
		
		return results.iterator();
	}

}
