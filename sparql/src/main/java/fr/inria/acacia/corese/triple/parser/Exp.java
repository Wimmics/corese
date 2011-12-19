package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * The root class of the statements of the query language: 
 * And, BasicGraphPattern, Score, Source, Option, Or, Triple
 * <br>
 * @author Olivier Corby
 */

public abstract class Exp extends Statement {
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(Exp.class); 
	private static String SUBSTATEOF = RDFS.COSSUBSTATEOF ;
	private static final String LEAF = "leaf_";
	
	private Vector<Exp> body;
	
	public Exp() {
		body = new Vector<Exp>();
	}
	
	public  boolean add(Exp exp){
		if (exp.isBinary() && exp.size() == 1){
			BasicGraphPattern bgp = BasicGraphPattern.create();
			for (Exp e : body){
				bgp.add(e);
			}
			exp.add(0, bgp);
			body.clear();
			return add(exp);
		}
		return body.add(exp);
	}
	
	boolean isBinary(){
		return isMinus() || (isOptional() && isSPARQL());
	}
	
	public  void add(int n, Exp exp){
		 body.add(n, exp);
	}
	
	public void addAll(Exp exp){
		body.addAll(exp.getBody());
	}
	
	public Vector<Exp> getBody(){
		return body;
	}
	
	public ASTQuery getQuery(){
		return null;
	}
	
	public Exp remove(int n){
		return body.remove(n);
	}
	
	public Exp get(int n){
		return body.get(n);
	}
	
	public void set(int n, Exp exp){
		body.set(n, exp);
	}
	
	public Triple getTriple(){
		return null;
	}
	
	public void setAST(ASTQuery ast){
		
	}
	
	public ASTQuery getAST(){
		return null;
	}
	
	public int size(){
		return body.size();
	}
	
	public boolean validate(ASTQuery ast){
		return true;
	}
	
	Bind validate(Bind env, int n) throws QuerySemanticException {
		   return env;
	   }

	
	public void append(Exp e){
		add(e);
	}
	
	public void append(Expression e){
		add(Triple.create(e));
	}
	
	/**
	 * Because remove does not work, because triple are all empty vectors
	 * hence are equal
	 */
	void delete(Triple t){
		for (int i=0; i<size(); i++)
			if (get(i) instanceof Triple){
				Triple triple = (Triple) get(i);
				if (triple.getID() == t.getID()){
					remove(i);
					break;
				}
			}
		
	}
	
	/**
	 *
	 * @param uri : from named u1 un
	 * @param src : source ?src1 ?srck
	 * @return for each ?srci, (?srci = u1 OR .. ?srci = un)
	 */
	Exp source(Vector<String> src, Vector<String> uri){
		Exp exp=new And();
		for (int i=0; i < src.size(); i++){
			exp.add( Triple.create(source(src.get(i), uri, 0)));
		}
		return exp;
	}
	
	/**
	 * Process one source, return (?src = u1 OR .. ?src = un)
	 */
	Term source(String src, Vector<String> uri, int i){
		if (i == uri.size() - 1){
			return source(src, uri.get(i));
		}
		else {
			return new Term(Keyword.SEOR, source(src, uri.get(i)),
					source(src, uri, i+1));
		}
	}
	
	/**
	 * Process from
	 * return ?src = uri
	 */
	Term source(String src, String uri){
		if (isRegexp(uri)){
			Term tt = Term.function(Keyword.REGEX, 
					new Variable(src), Constant.create(uri)); //parser.getASTQuery().createConstant(uri));
			//tt = Term.negation(tt);
			return tt;
		}
		else {
			Expression exp;
			if (Triple.isVariable(uri)){
				exp = new Variable(uri);
			}
			else {
				exp = Constant.create(uri); //parser.getASTQuery().createConstant(uri);
			}
			return new Term(Keyword.SEQ, new Variable(src), exp);
		}
	}
	
	boolean isRegexp(String uri){
		return uri.indexOf(".*")!=-1;
	}
	
	void process(ASTQuery aq){
		aq.setQuery(this);
	}
	
	
	
	Exp copy(){
		return this;
	}
	
	void setScore(Vector<String> names){
		Exp exp;
		for (int i=0;  i<size(); i++){
			exp = eget(i);
			exp.setScore(names);
		}
	}
	

	
	void setNegation(boolean b) {
	}
	
	void setCard(String card){
	}
	
	public void setRec(boolean b){
	  }
	
	/**
	 * Generate target Triple from triple
	 */
	
	
	
	
	
	public StringBuffer toString(StringBuffer sb) {
		if (size() == 1) {
			sb.append(get(0).toString());
		} else {
			sb.append(get(0).toString());
			for (int i=1;i<size();i++) {
				sb.append(ASTQuery.NL);
				sb.append(get(i).toString());
			}
		}
		return sb;
	}
	
	
	public Exp eget(int i){
		if (this.size() > i) return (Exp)get(i);
		else return null;
	}
	
	/**
	 * If the triples are all filter
	 * @return
	 */
	boolean isExp(){
		for (int i=0; i<size(); i++){
			if (! eget(i).isExp()) return false;
		}
		return true;
	}

	public boolean isTriple(){
		return false;
	}
	
	public boolean isRelation(){
		return false;
	}
	
	public boolean isFilter(){
		return false;
	}
	
	public boolean isOptional(){
		return false;
	}
	
	// draft: sparql compliance
	public boolean isSPARQL(){
		return false;
	}
	
	
	public boolean isAnd(){
		return false;
	}
	
	public boolean isBGP(){
		return false;
	}
	
	public boolean isUnion(){
		return false;
	}
	
	public boolean isMinus(){
		return false;
	}
	
	public boolean isGraph(){
		return false;
	}
	
	public boolean isService(){
		return false;
	}
	
	public boolean isScore(){
		  return false;
	  }
	
	public boolean isQuery(){
		return false;
	}
	
	public boolean isScope(){
		return false;
	}
	
	public boolean isNegation(){
		return false;
	}
	
	public boolean isForall(){
		return false;
	}
	
	public boolean isIfThenElse(){
		return false;
	}
	
	public boolean isExist(){
		return false;
	}
	
	/**
	 * This Exp is an option pattern : option (t1 t2 t3)
	 * tag t1 as first option triple and t3 as last
	 * projection will generate index for these first and last triples for
	 * appropriate backtracking
	 */
	void setOption(boolean b){
		Exp exp;
		for (int i=0;  i<size(); i++){
			exp = eget(i);
			exp.setOption(b);
		}
	}
	
	
	public void setFirst(boolean b){
		if (size() > 0)
			eget(0).setFirst(b);
	}
	
	public void setLast(boolean b){
		if (size() > 0)
			eget(size() - 1).setLast(b);
	}
	


	public boolean validateData(){
		for (Exp exp : getBody()){
			if (! exp.validateData()){
				return false;
			}
		}
		return true;
	}

	public boolean validateDelete(){
		for (Exp exp : getBody()){
			if (! exp.validateDelete()){
				return false;
			}
		}
		return true;
	}
	
}