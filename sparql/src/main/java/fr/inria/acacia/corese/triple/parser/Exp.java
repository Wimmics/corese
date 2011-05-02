package fr.inria.acacia.corese.triple.parser;

import java.util.Collections;
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

public abstract class Exp extends Statement implements Comparable<Exp> {
	
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
	
	boolean validate(){
		return true;
	}
	
	Bind validate(Bind env, int n) throws QuerySemanticException {
		   return env;
	   }
	
	void expand(NSManager nsm){
		for (Exp exp : getBody()){
			exp.expand(nsm);
		}
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
	Exp source(Parser parser, Vector<String> src, Vector<String> uri){
		Exp exp=new And();
		for (int i=0; i < src.size(); i++){
			exp.add( Triple.create(source(parser, src.get(i), uri, 0)));
		}
		return exp;
	}
	
	/**
	 * Process one source, return (?src = u1 OR .. ?src = un)
	 */
	Term source(Parser parser, String src, Vector<String> uri, int i){
		if (i == uri.size() - 1){
			return source(parser, src, uri.get(i));
		}
		else {
			return new Term(Keyword.SEOR, source(parser, src, uri.get(i)),
					source(parser, src, uri, i+1));
		}
	}
	
	/**
	 * Process from
	 * return ?src = uri
	 */
	Term source(Parser parser, String src, String uri){
		if (isRegexp(uri)){
			Term tt = Term.function(Keyword.REGEX, 
					new Variable(src), parser.getASTQuery().createConstant(uri));
			//tt = Term.negation(tt);
			return tt;
		}
		else {
			Expression exp;
			if (Triple.isVariable(uri)){
				exp = new Variable(uri);
			}
			else {
				exp = parser.getASTQuery().createConstant(uri);
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
	
	/**
	 * There should be at least one relation in a rule condition
	 * Generate a relation if needed
	 */
	Exp validateRule(){
		Vector<Triple> vec = new Vector<Triple>();
		for (Exp exp : getBody()){
			if (exp.isTriple()){
				Triple triple = (Triple) exp;
				if (triple.isRelation()){
					return this;
				}
				else if (triple.isType() && ! triple.isOption()){
					vec.add(triple);
				}
			}
		}
		for (Triple t : vec){
			Triple nt = Triple.create(t.getSubject(), t.getProperty(), 
					t.getVariable(), Variable.create(ExpParser.SYSVAR + "class"));
			add(0, nt);
			break;
		}
		
		return this;
	}
	
	void access(Parser parser, String access){
		
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
	
	
	/**
	 * FROM uri
	 * Assign a source variable to triple with no source
	 * name : root name to generate source var names
	 * vars : collected vector of generated variables
	 * generate  : if true generate a new var for each triple, else use name
	 * PRAGMA : option redefines this function for a local process
	 */
	void setFromSource(Parser parser, String name, Vector<String> vars, Vector<String> from, boolean generate) {
		Exp exp;
		for (int i = 0; i < size(); i++) {
			exp = eget(i);
			exp.setFromSource(parser, name, vars, from, generate);
		}
	}
	
	
	/**
	 * Set the source for graph ?src  
	 * Set inner source for state ?src and return list of inner variables
	 * 1. pstate = true :  process state ?s
	 * isState = true : we are in a state
	 * 2. pstate = false :  process graph ?g
	 */
	void setSource(Parser parser, Env env, String src,  boolean isState) {
		Exp exp;
		for (int i = 0; i < size(); i++) {
			exp = eget(i);
			if (env.state && exp.isState()){
				Env nenv = env.fork();
				//name = parser.newVar(name);
				exp.setSource(parser, nenv,  src, isState);
				if (nenv.vars.size() > 0){
					defState(parser,  nenv);
				}
			}
			else {
				exp.setSource(parser,  env, src, isState);
			}
			
		}
	}
	
	boolean isState(){
		return false;
	}
	
	/**
	 * Generate state SUBSTATEOF var
	 */
	void defState(Parser parser,  Env env){
		for (int i=0; i<env.vars.size(); i++){
			Variable var = new Variable(env.vars.get(i));
			Atom state;
			if (Triple.isVariable(env.states.get(i))){
				state = new Variable(env.states.get(i));
			}
			else {
				state = new Constant(env.states.get(i));
			}
			Triple t;
//			if (true)  t = new Triple(state, SUBSTATEOF, var);
//			else  t = new Triple(var, SUBSTATEOF, state);
			if (true)  t = Triple.create(state, new Constant(SUBSTATEOF), var);
			else       t = Triple.create(var, new Constant(SUBSTATEOF), state);
			//t.setID(parser.getTripleId());
			add(t);
		}
		defLeaves(parser, env);
		
	}
	
	/**
	 * 
	 * leaf states have no substates : generate 
	 * optional {var substate state filter(var != state) } filter ! bound(var)
	 */
	void defLeaves(Parser parser,Env env){
		int count = 0;
		for (String state : env.leaves){
			Atom astate;
			Variable var = new Variable(env.name + LEAF + count++);
			if (Triple.isVariable(state)){
				astate = new Variable(state);
			}
			else {
				astate = new Constant(state);
			}
			//Triple  t = new Triple(var, SUBSTATEOF, astate);
			Triple t = Triple.create(var, new Constant(SUBSTATEOF), astate);
			//t.setID(parser.getTripleId());
			Term term = new Term(Keyword.SNEQ, var, astate);
			Triple bt = Triple.create(term); 
			//bt.setID(parser.getTripleId());
			Option opt =  Option.create(new And(t, bt));
			term =  Term.function(TermParser.BOUND);
			term.add(var);
			term = Term.negation(term);
			bt =  Triple.create(term); 
			//bt.setID(parser.getTripleId());
			//System.out.println("** Exp : " + opt + " " + bt);
			add(opt);
			add(bt);
		}
	}
	
	/**
	 * FROM NAMED uri
	 * @param vars : collect source var that are not optional
	 * @param named : list of from named uri
	 * PRAGMA : option redefines this function for a local process
	 */
	void collectSource(Parser parser, Vector<String> vars, Vector<String> named) {
		Exp exp;
		for (int i = 0; i < size(); i++) {
			exp = eget(i);
			exp.collectSource(parser, vars, named);
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
	
	Exp complete(Parser parser){
		return complete(parser, false, false);
	}
	
//	default : do nothing
	void finalize(Parser parser){}
	
	
	/**
	 * score/source ?s { ?x rdf:type c:Person } ->
	 * score/source ?s { ?x rdf:type c:Person  ?x rdf:type ?class}
	 * generate a relation to carry the score/source
	 */
	void finalizeType(Parser parser){
		Exp exp = eget(0);
		Vector<Triple> vtype = new Vector<Triple>();
		int size = parser.getStackSize();
		for (int i = 0; i < exp.size(); i++) {
			Exp e = exp.eget(i);
			if (e instanceof Triple) {
				Triple triple = (Triple) e;
				if (triple.isRelation()) { // relation : bind its var
					triple.bind(parser);
				}
				else if (triple.isType()) {
					// ?x rdf:type URI : store it
					vtype.add(triple);
				}
			}
		}
		for (int i = 0; i < vtype.size(); i++) {
			// is there ?x rdf:type c:Person 
			// with ?x not bound by a relation
			Triple triple = (Triple) vtype.get(i);
			if (triple.getSubject().isConstant() ||
					parser.hasOptionVar(triple.getSubject())){

				triple.bind(parser); // for not duplicate relation on same variable
				//Exp nt = createType(parser, (Atom) triple.getExp1());
				Exp nt = createExpType(parser, (Atom) triple.getSubject(), (Constant) triple.getObject());
				exp.add(nt);
			}
		}
		parser.pop(size);
	}
	
	
//	create exp rdf:type ?class
	Triple createType(Parser parser, Atom exp){
		int id = Triple.nextID(); //parser.getTripleId();
		Variable var = new Variable(ExpParser.SYSVAR +  id);
		Triple triple =  Triple.create(exp, new Constant(RDFS.RDFTYPE), var);
		//triple.setID(id);
		// cannot get only one relation in case of graph ?src {} 
		// because the first relation may not be in the right ?src
		//triple.setOne(true);
		return triple;
	}
	
	
	/**
	 * Generate ?x rdf:type ?class filter(?class <=: c:Person)
	 */
	Exp  createExpType(Parser parser, Atom exp, Constant cname){
		Triple triple = createType(parser, exp); // ?x rdf:type ?class
		// when more, cannot add a type test (it would fail the relax algo)
		if (parser.getASTQuery().isMore()) 
			return triple;
		Term term = new Term(Keyword.STLEC, triple.getObject(), cname);
		return new And(triple,  Triple.create(term));
	}
	
	
	Exp duplicate() {
		try {
			return  (Exp) getClass().newInstance();
		}
		catch (IllegalAccessException e) {e.printStackTrace(); return null; }
		catch (InstantiationException e) {e.printStackTrace(); return null; }
		catch (Exception e) {e.printStackTrace(); return null; }
	}
	
	Exp complete(Parser parser, boolean option, boolean union){
		Exp exp, nexp=null;
		//if (pragma(parser)) return null;
		nexp= duplicate(); //(Exp) getClass().newInstance();
		if (isOptional()){
			option = true;
		}
		if (isUnion()){
			parser.setAll(true);
		}
		if (option && isUnion()){
			union = true;
			// option(?x p ?z union ?y p ?z) compiled as
			// option(?x p ?z) union option(?y p ?z) is false when filter ! bound(?z)
			// could be option(?x p ?z1)  option(?y p ?z2)
			// must rename free variables
			// currently skiped :
			if (parser.hasBound(this)){
				// there is a bound()
				logger.error(   "** Parser : Statement not evaluable : option / union :\n" + this);
				parser.setError("** Parser : Statement not evaluable : option / union :\n" + this);
			}
		}

		for (int i=0;  i<size(); i++){
			exp=eget(i).complete(parser, option, union);
			if (exp != null)
				nexp.add(exp);

		}

		nexp.finalize(parser); // specific process (see source or score)
		return nexp;
	}
	
	/**
	 * optional { graph cos:display {sem:root rdf:type wiki:Page}} 
	 * is a pragma to rdf display
	 * remove it from query, put it in AST
	 */
	boolean pragma(Parser parser){
		if (isOptional()){
			Exp exp = get(0);
			if (exp.getBody().size()>0 && exp.get(0).isGraph()){
				Source g = (Source) exp.get(0);
				if (g.getSource().isConstant()){
					String src = parser.getNSM().toNamespace(g.getSource().getName());
					if (src.startsWith(RDFS.COSPRAGMANS)){
						parser.getASTQuery().setPragma(src, g.get(0));
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public String toSparql() {
		return toSparql(null);
	}
	
	public String toSparql(NSManager nsm) {
		//System.out.println("Exp.java - "+size()+" "+this+" "+this.getClass());
		String str = "";
//		boolean bgpinstance = false;
//		if (this instanceof BasicGraphPattern) {
//			bgpinstance = true;
//		}
//		if (bgpinstance) str += ASTQuery.OPEN_BRACKET;
		if (size() == 1) {
			str += eget(0).toSparql(nsm);
		} else {
			for (int i=0;i<size();i++) {
				str += eget(i).toSparql(nsm);
//				if (this instanceof Or && i<(size()-1)) {
//					//str += "union ";
//					str += ASTQuery.UNION + ASTQuery.SPACE;
//					i++;
//					str += eget(i).toSparql(nsm);
//				}
			}
		}
//		if (bgpinstance) str += ASTQuery.CLOSE_BRACKET;
		return str;
	}
	
	public String toString(){
		return toSparql(null);
	}
	
	public String toString(NSManager nsm){
		boolean isor = this instanceof Or;
		if (size() == 0)
			return "";
		else if (size() == 1)
			return eget(0).toString(nsm);
		Exp exp; int last = size() -1;
		String str = "(";
		for (int i = 0; i < size(); i++){
			exp=eget(i);
			str += exp.toString(nsm);
			if (exp instanceof Triple && i < last)
				str += " \n";
			else str += " ";
			if (isor && i < last) str += " or ";
		}
		str += " )";
		return str;
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
	
	Expression toTerm() {
		return toTerm(0);
	}
	
	/**
	 * Recursively build a Term from this OR Exp
	 */
	Expression toTerm(int n){
		Exp exp=eget(n);
		if (n == size() - 1){
			return exp.toTerm();
		}
		else {
			return new Term(getOper(), exp.toTerm(), toTerm(n+1));
		}
	}
	
	String getOper() {
		return null;
	}
	
	abstract Exp  product(Exp exp);
	abstract Exp sproduct(Or exp);
	abstract Exp sproduct(And exp);
	abstract Exp sproduct(Triple exp);
	Exp sproduct(Option exp){
		return this;
	}
	
	/**
	 * Process distribution over and and or :  (a or b) and c -> ac or bc
	 */
	Exp distrib(){
		return this;
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
	
	/**
	 * Exp is AND
	 * Find the last triple that is a relation (not a test)
	 */
	Triple lastTriple(){
		Triple triple;
		for (int i=size()-1; i >= 0; i--){
			if (eget(i) instanceof Triple){
				triple=(Triple)eget(i);
				if (triple.isRelation()){
					return triple;
				}
			}
			else {
				return eget(i).lastTriple();
			}
		}
		logger.error("** QueryParser : no triple in option " + this);
		return null;
	}
	
	/**
	 * Compile inner options with IDs
	 * @return this collapsed exp, i.e. the Option exp are removed
	 */
	Exp option(Parser parser){
		for (int i = 0; i < size(); i++) {
			set(i, eget(i).option(parser));
		}
		return distrib(); // simplify the AND if needed because the Option are removed
	}
	
	/**
	 * Put options at the end
	 */
	Exp sort(Parser parser){
		Collections.sort(body);
		return this;
	}
	
	/**
	 * recursive sort : {triple, filter, option}
	 * should generate ?x rdf:type ?class for :
	 * ?x rdf:type c:Engineer option (?x ~ alex ?x c:Designation ?name)
	 * recsort specialized by Option
	 */
	Exp recsort(Parser parser){
		sort(parser); // {triple, filter, option}
		boolean isor = this instanceof Or;
		for (int i=0; i<size(); i++){
			Exp exp = eget(i);
			if (exp instanceof Triple){
				Triple triple = (Triple) exp;
				// declare var binding in case of inner option
				if (! triple.isExp())
					triple.bind(parser);
			}
			else {
				exp.recsort(parser);
			}
			if (isor){
				// each branch of OR has its own variables : clear current branch
				parser.clear();
			}
		}
		return this;
	}
	
	/**
	 * To put relation first, then exp, then option at the end
	 * exp after relation in case of option
	 * option exp can be attached to a relation
	 */
	public int compareTo(Exp exp){
		if (this instanceof Option){
			if (exp instanceof Option)
				return -1;
			else
				return 1;
		}
		else if (isExp()){
			if (exp instanceof Option)
				return -1;
			else  if (exp.isExp())
				return -1;
			else return 1;
		}
		else return -1;
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