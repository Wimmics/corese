package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie 
 */

public class Variable extends Atom {
	
	private boolean isBlankNode = false;
	private boolean isPath = false; // use case ?x $path ?y
	private boolean isVisited = false;
	private int index = -1;
	List<Variable> lVar;
	
	public Variable(String str){
		super(str);
	}
	
	public static Variable create(String str){
		return new Variable(str);
	}
	
	public StringBuffer toString(StringBuffer sb){
		if (isBlankNode()) {
			// variable for blank node, replace ?_ by _:
			sb.append( KeywordPP.BN + name.substring(2,name.length()));
		} 
		else { 
			sb.append( name);
		}
		return sb;
	}
	
	public boolean equals(Object o){
		if (o instanceof Variable){
			Variable var = (Variable) o;
			if (getName().equals(var.getName())){
				return true;
			}
		}
		return false;
	}
	
	public void setPath(boolean b){
		isPath = b;
	}
	
	public boolean isPath(){
		return isPath;
	}
	
	void addVariable(Variable var){
		if (lVar == null) lVar = new ArrayList<Variable>();
		lVar.add(var);
	}
	
	public List<Variable> getVariableList(){
		return lVar;
	}
	
	public boolean isVisited(){
		return isVisited;
	}
	
	public void setVisited(boolean b){
		isVisited = b;
	}
	
	public static boolean isBlankVariable(String name){
		return name.startsWith(ASTQuery.BNVAR);
	}
	
	/**
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 */
	public Expression process(ASTQuery ast){
		if (isVisited()){
			setVisited(false);
			return null;
		}
		Expression exp = ast.getExpression(name);
		if (exp != null && (! exp.isFunctional() || ! ast.isKgram())){
			// use case: do not rewrite ?val
			// xpath() as ?val
			// xsd:integer(?val)
			setVisited(true);
			Expression ee = exp.process(ast);
			setVisited(false);
			return ee;
		}
		else {
			return this;
		}
	}
	
	public boolean  isType (int type){
		return false;
	}
	
	public boolean isVariable(){
		return true;
	}
	
	public boolean isSimpleVariable(){
		return ! isBlankNode();
	}
	
	
	Bind validate(Bind env){
		env.bind(getName());
		return env;
	}
	
	public Variable getVariable() {
		return this;
	}
	
	public Variable getOptionVar(Vector<String> stdVar) {
		if (! stdVar.contains(name)){
			return this;
		}
		else return null;
	}
	
	public boolean isBlankNode() {
		return isBlankNode;
	}
	
	public void setBlankNode(boolean isBlankNode) {
		this.isBlankNode = isBlankNode;
	}
	
	
	/**
	 * KGRAM
	 */
	
	public int type(){
		return ExprType.VARIABLE;
	}
	
	public void getVariables(List<String> list) {
		// TODO Auto-generated method stub
		if (! list.contains(getName())){
			list.add(getName());
		}
	}
	
	public int getIndex() {
		return index ;
	}

	@Override
	public void setIndex(int n) {
		index = n;
	}
	
	// fake value in case where a variable node is used as a target value node
	// see ProducerDefault
	// use case: project a query graph on itself
	public Object getValue(){
		return getDatatypeValue();
	}
	
	public IDatatype getDatatypeValue(){
		return Constant.create(name).getDatatypeValue();
	}

	
}