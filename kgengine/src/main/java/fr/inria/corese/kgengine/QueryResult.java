package fr.inria.corese.kgengine;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.sparql.triple.parser.ASTQuery;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Result;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;

public class QueryResult
implements IResult {
	
	Result answer;
	Mapping map;
	ASTQuery ast;
	
	QueryResult(Result a){
		answer = a;
	}
	
	QueryResult(Mapping m, ASTQuery a){
		map = m;
		answer = m;
		ast = a;
	}
	
	public String toString(){
		return answer.toString();
	}
	
	public Mapping getMapping(){
		return map;
	}

	
	public IResultValue getResultValue(String variableName) {
		return getResultValue(map, variableName);
	}
	
	public IResultValue getResultValue(Mapping map, String variableName) {
		// TODO Auto-generated method stub
		Node node = map.getNode(variableName);
		if (node == null) return null;
		return cast(node);
	}
	
	
	public IDatatype getDatatypeValue(String variableName) {
		// TODO Auto-generated method stub
		Node node = map.getNode(variableName);
		if (node == null || !(node.getValue() instanceof IDatatype)) return null;
		return (IDatatype) node.getValue();
	}
	
	IResultValue cast(Node node){
		if (node instanceof IResultValue)
			return  (IResultValue) node;
		else return new QueryValue(node);
	}

	
	public IResultValue[] getResultValues(String variableName) {
		// TODO Auto-generated method stub
		Mappings lMap = map.getMappings();
		if (lMap != null && lMap.size()>0 && 
				 ! ast.isGroupBy(variableName)){
			return getListResultValues(variableName);
		}
		else {
			IResultValue res = getResultValue(variableName);
			if (res == null) return null;
			IResultValue[] lRes;
			lRes = new IResultValue[1];
			lRes[0] = res;
			return lRes;
		}
	}
	
	/**
	 * Simulate select ?y group by ?x à la Corese
	 */
	public IResultValue[] getListResultValues(String variableName) {
		Mappings lMap = map.getMappings();
		List<Node> lNodes = new ArrayList<Node>();
		
		for (Mapping m : lMap){
			Node node = m.getNode(variableName);
			if (node != null && ! contains(lNodes, node)){
				lNodes.add(node);
			}
		}

		// some values were null
		IResultValue[] lRes = new IResultValue[lNodes.size()];
		int i = 0;
		for (Node node : lNodes){
			lRes[i++] = cast(node);
		}

		return lRes;
	}
	
	boolean contains(List<Node> lNodes, Node node){
		for (Node n : lNodes){
			if (node.same(n)){
				return true;
			}
		}
		return false;
	}

	
	public String[] getSPARQLValues(String variableName) {
		// TODO Auto-generated method stub
		return getStringValues(variableName);
	}

	
	public double getSimilarity() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public String getStringValue(String variableName) {
		// TODO Auto-generated method stub
		IResultValue val = getResultValue(variableName);
		if (val == null) return null;
		return val.getStringValue();
	}

	
	public String[] getStringValues(String variableName) {
		// TODO Auto-generated method stub
		IResultValue[] res = getResultValues(variableName);
		if (res == null) return null;
		String[] lRes = new String[res.length];
		int i = 0;
		for (IResultValue r : res){
			lRes[i++] = r.getStringValue();
		}
		return lRes;
	}

	
	public Iterable<String> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean includes(IResult r) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isBound(String var) {
		// TODO Auto-generated method stub
		Node node = map.getNode(var);
		return node != null;
	}

	
	public boolean matches(IResult r) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void setResultValue(String variableName, IResultValue value) {
		// TODO Auto-generated method stub
		
	}

	
	public IResultValue[] getAllResultValues(String variableName) {
		// TODO Auto-generated method stub
		return getResultValues(variableName);
	}
	
	
	
	

}
