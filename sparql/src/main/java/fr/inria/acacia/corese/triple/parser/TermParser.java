package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;


/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class parses Expressions of triples for the 1st parser.<br>
 * bool ::= conj || bool  | conj<br>
 * conj ::= cond && conj | cond<br>
 * cond ::= exp oper exp | exp<br>
 * exp  ::= term + exp  | term - exp  | term<br>
 * term ::= fact * term | fact / term | fact<br>
 * fact ::= const | var | ( bool ) | ! (bool)<br>
 * <br>
 * Examples: <br>
 * (?y -  ?x) + ?y * 2<br>
 * ( ?a && true ) || isbound( ?b)<br>
 * <br>
 * @author Olivier Corby
 */

public class TermParser {
	Parser parser = null;
	static final String XSD = "xsd:";
	static final String BOUND = "bound";
	static final String SIM = "similarity";
	static final String SCORE = "score";
//	static final String COUNT = FunMarker.COUNT;
//	static final String SUM = FunMarker.SUM;
//	static final String AVG = FunMarker.AVG;
//	static final String COUNTITEM = FunMarker.COUNTITEM;
	
	static final String[] TABCAST = {XSD};
	static final String[] FUN=
		
	{"lang", "langMatches", "datatype", "namespace", "regex", BOUND, "str",
		"isBlank", "isLiteral", "isURI", "isIRI",
		"today", "sqrt", "cardinality", "occurrence", "depth", "distance", 
		"isMulti", "distinct", "self", "extension", "coextension", SIM, SCORE,
		"year", "month", "day", "byRule", "isSource", "sameTerm", "differ"};
	static final String SDT = KeywordPP.SDT;
	static final String LANG = KeywordPP.LANG;
	static final String[] operList =
	{"=", "!=", "<=", ">=", ">", "<", "~", "!~", "^", "!^", "==", "!==", "~=", "!~=",
		"<:", "<=:", "=:", ">=:", ">:", "!<:", "!<=:", "!=:", "!>=:", "!>:",
		"in", "!in", "is",
	};
	
	public TermParser() {
		
	}
	
	public TermParser(Parser p) {
		parser = p;
	}
	
	/**
	 * BOOL ::= ANDEXP || BOOL  |  ANDEXP
	 */
	Expression bool(Lexer lex) {
		Expression exp1 = and(lex);
		//System.out.println("** TP1 : " + exp1);
		if (lex.hasMoreElements()){
			String oper = lex.lookAhead();
			//System.out.println("** TP2 : " + oper);
			if (oper.equals(Keyword.SEOR)){
				lex.next();
				Expression exp2=bool(lex);
				Term term =  new Term(oper, exp1, exp2);
				//System.out.println("** TP3 : " + term);
				return term;
			}
		}
		return exp1;
	}
	
	/**
	 * AND ::= COND && ANDEXP | COND
	 */
	Expression and(Lexer lex) {
		Expression exp1 = cond(lex);
		if (lex.hasMoreElements()){
			String oper = lex.lookAhead();
			if (oper.equals(Keyword.SEAND)){
				lex.next();
				Expression exp2=and(lex);
				return new Term(oper, exp1, exp2);
			}
		}
		return exp1;
	}
	
	/**
	 * cond ::= exp | exp = exp | ...
	 */
	Expression cond(Lexer lex) {
		lex.lookAhead();
		Expression exp1 = exp(lex);
		if (lex.hasMoreElements()) {
			String oper = lex.lookAhead();
			if (isCond(oper)) {
				lex.next();
				Expression exp2 = exp(lex);
				return new Term(oper, exp1, exp2);
			}
		}
		return exp1;
	}
	
//	= != >= ...
	boolean isCond(String oper){
		for (int i=0; i<operList.length; i++){
			if (oper.equals(operList[i]))
				return true;
		}
		return false;
	}
	
	/**
	 * Parse   exp  ::= term + exp  | term - exp  | term
	 * @param lex
	 * @return
	 */
	
	Expression exp(Lexer lex) {
		Expression fact1 = term(lex), fact2;
		Expression exp=fact1;
		boolean go=true;
		if (lex.hasMoreElements()){
			String oper = lex.lookAhead();
			while (go && (oper.equals("+") || oper.equals("-"))) {
				if (oper.equals("+")) {
					lex.next(); // eat +
					fact2 = exp(lex);
					exp = new Term(oper, fact1, fact2);
				}
				else if (oper.equals("-")) {
					lex.next(); // eat -
					fact2 = term(lex);
					exp = new Term(oper, fact1, fact2);
				}
				if (lex.hasMoreElements()){
					oper = lex.lookAhead();
					fact1=exp; // in case we continue
				}
				else go=false;
			}
		}
		return exp;
	}
	
	
	
	
	/**
	 *  Parse term ::= fact * term | fact / term | fact
	 * @return
	 */
	Expression term(Lexer lex) {
		Expression fact1 = fact(lex);
		Expression exp=fact1;
		if (lex.hasMoreElements()){
			String oper = lex.lookAhead();
			parser.pget(oper); // is it get:oper ???
			if (isMult(oper)) { // || get != null) {
				lex.next(); // eat *
				Expression fact2=term(lex);
				exp = new Term(oper, fact1, fact2);
			}
		}
		return exp;
	}
	
	/**
	 *  Parse  fact ::= var | cst | ( exp ) | ! fact | - fact
	 */
	Expression fact(Lexer lex) {
		String term=lex.lookAhead();
		Expression exp;
		boolean negation = false;
		if (Term.isNegation(term)) {
			lex.next(); // eat !
			term=lex.lookAhead();
			negation = true;
		}
		if (isOpenParen(term)) {
			lex.next(); // eat "("
			exp = bool(lex);
			lex.next(); // eat ")"
		}
		else if (term.equals("-")) {
			lex.next(); // eat "-"
			exp=fact(lex);
			exp=new Term("-", new Constant("0", "xsd:integer"), exp);
		}
		else {
			//exp =  atom(term);
			exp = funatom(lex);
		}
		if (negation){
			Expression tt =  Term.negation(exp);
			exp = tt;
		}
		return exp;
	}
	
	
	Expression funatom(Lexer lex){ //, String value) {
		String value = lex.next();
		String token = "";
		if (lex.hasMore())
			token = lex.lookAhead();
		Expression exp;
		if (isOpenParen(token) && isFunction(value)){
			exp = function(lex, value);
		}
		else {
			exp = atom(value);
		}
		return exp;
	}
	
	
	boolean isFunction(String name){
		name = name.toLowerCase();
		for (int i=0; i<FUN.length; i++){
			//if (FUN[i].indexOf(name) == 0) {
			if (FUN[i].toLowerCase().indexOf(name) == 0) {
				return true;
			}
		}
		if (castFunIndex(name) == 0) { // xsd:string(?x)
			return true;
		}
		return false;
	}
	
	/**
	 *
	 * @param value : variable,
	 * or literal, may carry a datatype or a lang
	 * or URI
	 * @return
	 */
	public Expression atom(String value) {
		
		String datatype = null;
		String lang = null;
		Atom atom=null;
		String src=value;
		int index = value.indexOf(SDT); // ^^
		boolean literal=false;
		if (index != -1) {
			datatype = value.substring(index + SDT.length());
			value = value.substring(0, index);
			literal=true;
		}
		index = value.indexOf(LANG); // @fr
		if (index != -1) {
			lang = value.substring(index + 1);
			if (parser != null && parser.ispGet(lang)){ // it is @get:lang computed from GUI
				lang=parser.getValue(parser.pget(lang));
				if (parser.isEmpty(lang))
					lang=null;
			}
			value = value.substring(0, index);
			literal=true;
		}
		
		atom = (Atom)ASTQuery.createAtom(value, datatype, lang, literal);
		
		// used in the first parser for group:: count:: ...
		//atom.setSrc(src);
		return atom;
	}
	
	
	int castFunIndex(String name){
		int id = -1;
		for (int i = 0; i < TABCAST.length && id == -1; i++) { // test cast datatype
			id = name.indexOf(TABCAST[i]);
		}
		return id;
	}
	
	
	Expression function(Lexer lex, String name) {
		String str= lex.next(); // eat "("
		str = lex.lookAhead();
		Expression res;
		if (isCloseParen(str) ){
			res = Term.function(name);
		}
		else {
			Expression arg = bool(lex);
			{
				Term term = Term.function(name); 
				term.add(arg);
				str = lex.lookAhead();
				while (!isCloseParen(str)) {
					arg = bool(lex);
					term.add(arg);
					str = lex.lookAhead();
				}
				res = term;
			}
		}
		str = lex.next(); // eat ")"
		return res;
	}
	
	boolean isMult(String str) {
		return str.equals("*") || str.equals("/");
	}
	
	boolean isPlus(String str) {
		return str.equals("+") || str.equals("-");
	}
	
	
	boolean isCloseParen(String fst){
		return fst.equals(")");
	}
	
	boolean isOpenParen(String fst){
		return fst.equals("(");
	}
	
	
}