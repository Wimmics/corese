package fr.inria.acacia.corese.triple.parser;

import java.util.Comparator;
import java.util.Vector;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;


/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class implements optional graph pattern, it may be recursive:<br>
 * optional ( A B optional ( C D ) )
 * <br>
 * @author Olivier Corby
 */

public class Option extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(Option.class);
	
	static int num =0;
	
	public Option() {}
	
	// PRAGMA: exp is BGP
	public Option(Exp exp){
		add(exp);
		//exp.setOption(true);
	}
	
	public static Option create(Exp exp){
		return new Option(exp);
	}
	
	/**
	 * (and t1 t2 (or (and t3) (and t4)))
	 */
	
	
	
	/**
	 *  Recursive distribution of AND over OR
	 */
	Exp distrib(){
		Exp exp=eget(0).distrib(), alt;
		// do it again because an inside OR may have build an OR Term and lost the option=true
		setOption(true);
		if (exp instanceof Or){
			// (option (or A B)) -> (or (option A) (option B))
			// (option (or A B)) -> (or A B (and (option A) (option B) filter !A && !B) )
			for (int i = 0; i < exp.size(); i++) {
				alt=exp.eget(i);
				if (! (alt instanceof Option)){
					alt= Option.create(alt);
					// by algorithmic convention, an OR contains a list of AND :
					exp.set(i, new And(alt));
				}
			}
			return exp;
		}
		else if (exp instanceof Option){
			return exp;
		}
		else {
			set(0, exp);
			return this;
		}
	}
	
	Bind validate(Bind env, int n) throws QuerySemanticException {
		return get(0).validate(env, n+1);
	}
	
	String getOper() {
		return "option";
	}
	
	public boolean isOptional(){
		return true;
	}
	
	// draft: sparql compliance
	public boolean isSPARQL(){
		return false;
	}

	
	Exp product(Exp exp){
		return exp.sproduct(this);
	}
	
	/**
	 * sproduct conceptually perfom arg * this (instead of this * arg)
	 * the args are permuted by product because of polymorphism weakness of java
	 */
	
	Exp sproduct(Option exp) {
		return sproduct(new And(exp));
	}
	
	
	/**
	 * A (and B C) -> (and A B C)
	 */
	Exp sproduct(Triple exp) {
		//add(t);
		return sproduct(new And(exp));
	}
	
	
	/**
	 * (and A B) (and C D) -> (and A B C D)
	 */
	Exp sproduct(And exp){
		exp.add(this);
		return exp;
	}
	
	/**
	 * (or A B) option C -> (A option C) or (B option C)
	 */
	Exp sproduct(Or exp){
		for (int i = 0; i < exp.size(); i++) {
			exp.eget(i).add(this);
		}
		return exp;
	}
	
	
	Exp simplify(){
		boolean simple=true;
		for (int i=0; i<size(); i++){
			if (eget(i) instanceof And){
				simple=false;
			}
		}
		if (simple)
			return this;
		And exp=new And();
		for (int i=0; i<size(); i++){
			if (eget(i) instanceof And){
				exp.addAll(eget(i));
			}
			else exp.add(eget(i));
		}
		return exp;
	}
	
	
	/**
	 * Set  source for graph/state ?src 
	 * generate subState locally within optional{} 
	 */
	void setSource(Parser parser,  Env env, String src, boolean b) {
		Exp exp;
		Env nenv = env.fork();
		
		for (int i = 0; i < size(); i++) {
			exp = eget(i);
			exp.setSource(parser,  nenv, src, b);
		}
		if (nenv.state && nenv.vars.size() > 0){
			// we have found state ?src, generate ?src cos:subStateOf ?si
			get(0).defState(parser,  nenv);
		}
	}
	
	/**
	 * FROM NAMED uri
	 * Generate local filter for source var : var = uri
	 * vars : vector of collected source var (redefines that of Exp)
	 * named : from named uri
	 */
	void collectSource(Parser parser, Vector<String> vars, Vector<String> named) {
		Exp exp;
		Vector<String> localVars = new Vector<String>();
		for (int i = 0; i < size(); i++) {
			exp = eget(i);
			exp.collectSource(parser, localVars, named);
		}
		if (localVars.size() > 0){
			// we have found source var, generate var = uri
			Exp expFrom = source(parser, localVars, named);
			Exp body = eget(0); // body of option
			body.add(expFrom);
		}
	}
	
	
	/**
	 * FROM uri
	 * name : root name to generate var names
	 * vars : vector of generated variables
	 * generate  : if true generate a new var for each triple, else use name
	 * the test var = uri must be IN the option
	 * use case : union (graph ?src ?x ?p ?y)
	 */
	void setFromSource(Parser parser, String name, Vector<String> vars, Vector<String> from, boolean generate) {
		Exp exp;
		// allocate a new vector for potential source var in option
		Vector<String> localVars = new Vector<String>();
		name += "o" + num++  ; 
		for (int i = 0; i < size(); i++) {
			exp = eget(i);
			// TODO : allocate a new root name / use parser
			exp.setFromSource(parser, name, localVars, from, generate);
		}
		if (localVars.size() > 0){
			// we have set source var, generate var = uri
			Exp expFrom = source(parser, localVars, from);
			Exp body = eget(0); // body of option
			body.add(expFrom);
		}
	}
	
	
	
	/**
	 * Recursive sort of elements of the option :
	 * sort as {triple filter option} OR {triple, option, fake, filter}
	 * filter is moved at the end if it has still unbound var before inner option
	 * add a fake if no triple at the beginning
	 */
	Exp recsort(Parser parser) {
		// store stack size of bound variable
		int size = parser.getStackSize();
		sort(parser); // sort as {triple filter option} OR {triple option fake filter}
		// the sort binds variables for inner options :
		if (size() > 0) {
			Exp exp = eget(0); // inner and
			for (int i = 0; i < exp.size(); i++) {
				exp.eget(i).recsort(parser); // recsort each member of option
				// each inner option binds its variables locally and then pop the stack
				// for the next option
			}
		}
		// compute the number of variables bound by this option :
		// pop the local variables :
		parser.pop(size);
		return this;
	}
	
	
	/**
	 * Refine Exp sort for option
	 * If there is filter and option inside and a filter has unbound variable
	 * may sort as {triple, option, filter}
	 * then, insert a fake triple between option and filter to carry the filter :
	 * {triple, option, fake, filter}
	 * At projection, this fake will be the lastOption and will declare the success of the option
	 * once the filter has succeeded (after the inner option)
	 * This is done because the filter may contain bound/unbound variables from the
	 * inner option. Hence the filter must be evaluated after the inner option
	 * Furthermore, if first is not a relation, add a fake relation first
	 *
	 */
	Exp sort(Parser parser){
		Exp exp=eget(0);
		exp.sort(parser); // std sort : {triple, filter, option}
		if (exp.size() == 0)
			return this;
		// pragma : sorted as {triple filter option}
		// pragma : parser knows bound variables at this point
		// replace ?x rdf:type URI by ?x <=: URI, may generate ?x rdf:type ?class
		type(parser);
		boolean option=false;
		
		// pragma : sorted as {triple filter option}
		// pragma : parser knows bound variables at this point
		option = exp.eget(exp.size() - 1) instanceof Option; // contains inner option
		Vector<Triple> unboundFilter = new Vector<Triple>(); // to store filters with unbound variables
		for (int i = 0; i < exp.size(); i++) {
			Exp e = exp.eget(i);
			if (e.isTriple()) {
				Triple triple = (Triple) e;
				// if (! triple.isExp()) triple.bind(parser); // done by type()
				if (triple.isRelation()) { // relation : bind its var
					// generate a copy to store its own first last option ID
					// in case it is in OR, index may differ in different branch
					// so we need different copy of the same triple :
					triple = (Triple) triple.copy();
					exp.set(i, triple);
				}
				else if (parser.hasOptionVar(triple.getExp())) { // filter
					// this filter has some variable not bound yet, may be bound by next inner option
					// hence the filter will be moved after the option if any
					if (option) { // there is inner option
						unboundFilter.add(triple); // store the unbound filter for the moment
						exp.remove(i); // remove filter from current place because unbound var
						i--; // go back from one position because of the removal
					}
					else {
						//logger.debug("** WARNING : Option filter has unbound var : " + triple.getExp());
					}
				}
			}
		}
		if (unboundFilter.size() > 0) { // there are unbound filters
			Triple fake = fake(parser); // add a fake relation at the end, before filter
			exp.add(fake); // fake will carry the filter
			exp.getBody().addAll(unboundFilter); // move unbound filters at the end
			if (exp.eget(0) instanceof Option) {
				// no relation first, add a fake first triple because the last fake needs a first
				// relation to declare success or failure (cf projection algo)
				// this is not needed if no unbound filter because there is no such declaration of success
				fake = fake(parser);
				exp.add(0, fake);
			}
		}
		if (exp.eget(0).isTriple()) {
			// if first triple is not a relation, add a fake relation
			Triple first = (Triple) exp.eget(0);
			if (first.isExp()) { //  an expression
				// fake a relation to carry the filter
				first = fake(parser);
				exp.add(0, first);
			}
		}
		return this;
	}
	
	/**
	 * for all triple ?x rdf:type URI
	 * 1. generate filter ?x <=: URI (local type test in option)
	 * 2. if ?x is not bound by a relation, modify triple as ?x rdf:type ?class
	 * TODO : and should attach the filter to this relation (not to the last relation)
	 * this option is sorted {triple filter option}
	 */
	void type(Parser parser) {
		Exp exp=eget(0);
		Vector<Triple> vtype = new Vector<Triple>();
		int i ;
		for (i = 0; i < exp.size(); i++) {
			Exp e = exp.eget(i);
			if (e.isTriple()) {
				Triple triple = (Triple) e;
				if (triple.isRelation()) { // relation : bind its var
					triple.bind(parser);
				}
				else if (triple.isExp()) {
					// first filter : stop
					break;
				}
				else if (triple.istype) {
					// ?x rdf:type URI : store it
					vtype.add(triple);
				}
			}
			else { // option
				break;
			}
		}
		// pragma : i = index of last triple + 1 ; i.e. index of first filter
		for (int j=0; j< vtype.size(); j++){
			// vtype is vector of ?x rdf:type URI
			Triple triple = (Triple) vtype.get(j);
			// generate ?x <=: URI from triple
			Expression e1 = triple.getSubject();
			boolean isURI = e1.isConstant();
			Term term = new Term (Keyword.STLE, e1, triple.getObject());
			// use case: approximate search bypass type test
			//term.setSystem(true);
			Triple nt =  Triple.create(term);
			//nt.setID(parser.getTripleId());
			nt.setOption(true);
			// compiler will have to replace URI by its generated variable :
			if (isURI) nt.setFake(true);
			// add this new filter
			exp.add(i, nt);
			if (parser.hasOptionVar(triple.getSubject()) || isURI) {
				// ?x is unbound var : modify triple as ?x rdf:type ?class
				// trick : the triple is still in the option
				//triple.setExp2(new Variable(ExpParser.SYSVAR + "class_" + triple.id));
				triple.setObject(new Variable(ExpParser.SYSVAR +  triple.id));
				//triple.setOne(true);
				triple.bind(parser);
			}
			else {
				// this triple is bound : remove it from option
				exp.delete(triple);
				// decrease index of first filter
				i--;
			}
		}
	}
	
	
	/**
	 * Compile this option
	 * @return the inner exp (which is an and)
	 * collapse the Option (remove it because it is compiled as ID)
	 */
	Exp option(Parser parser){
		//  process this option
		setOptionIndex(parser);
		Exp exp=eget(0);
		exp=exp.option(parser);
		return exp;
	}
	
	/**
	 * pragma : the exp inside the option is an AND
	 * pragma : the input exp is sorted as
	 * {triple, filter, option} OR {triple, filter, option, fakeTriple, unbound filter}
	 * Compute the ID of first and last triple of this option pattern
	 * option(and( A B  option(C D option (E F))  option(G H)))
	 * A ->last H     A isFirst
	 * B ->first A    B isLast
	 * C ->last F
	 * D ->first C
	 */
	void setOptionIndex(Parser parser){
		Exp exp=eget(0); // exp is an AND
		if (! (exp instanceof And)){
			logger.error("** Query Parser : option without and : " + this);
			return;
		}
		Triple first=null;
		if (exp.size() > 0){
			Exp e = exp.eget(0);
			if (e instanceof Option){// no triple no filter
				return ;
			}
			else {
				// first is a relation (native or fake)
				first = (Triple) e;
				first.setFirst(true);
			}
		}
		
		// last triple of this option where to backtrack if this option fail :
		// computed recursively over inner options : get the very last relation
		// may be a fake relation
		Triple last = exp.lastTriple();
		if (last != null) {
			first.setLastOptionID(last.getID());
		}
		// get the last local triple that is a relation within this option
		// this guy will say : optionSucces[first]=true
		// may be the fake, may be the first (if only one triple)
		for (int i=exp.size() - 1; i >= 0; i--){
			if (exp.eget(i).isTriple()){
				Triple triple=(Triple)exp.eget(i);
				if (triple.isRelation()){
					triple.setLast(true);
					triple.setFirstOptionID(first.getID());
					break;
				}
			}
		}
	}
	
	/**
	 * A fake relation to carry an option filter because the projection algo
	 * manages a stack of relation
	 */
	Triple fake(Parser parser) {
		//int id = parser.getTripleId();
		Triple triple = Triple.fake();
		//triple.setID(id);
		triple.setOption(true);
		triple.setOne(true);
		return triple;
	}
	
	class Compare implements Comparator {
		/**
		 * To put relation first, option then exp at the end
		 * pragma : there is option and filter
		 */
		public int compare(Object o1, Object o2){
			Exp exp1 = (Exp)o1, exp2 = (Exp)o2;
			
			if (exp1.isExp()){
				if (exp2.isExp())
					return -1;
				else
					return 1;
			}
			else if (exp1 instanceof Option){
				if (exp2.isExp())
					return -1;
				else  if (exp2 instanceof Option)
					return -1;
				else return 1;
			}
			else return -1;
		}
		
	}
	
	
	
	/**
	 *
	 * Find the last triple that is a relation
	 */
	Triple lastTriple(){
		if (size() > 0)
			return eget(0).lastTriple();
		else return null;
	}
	

	public StringBuffer toString(StringBuffer sb) {
		sb.append(KeywordPP.OPTIONAL + KeywordPP.SPACE);
		for (int i=0; i<size(); i++){
			sb.append(eget(i).toString());
		}
		return sb;
	}
	

	
}