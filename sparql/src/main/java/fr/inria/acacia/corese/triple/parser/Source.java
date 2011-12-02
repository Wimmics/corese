package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import fr.inria.acacia.corese.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class implements graph ?src { PATTERN }
 * <br>
 * @author Olivier Corby
 */

public class Source extends And {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	String source;
	Atom asource;
	boolean state = false;
	boolean leaf = false;
	private boolean isRec = false;

  public Source() {}

  /**
   * Model the scope of graph ?src { pattern }
   *
   */
  public Source(Atom src, Exp exp) {
    super(exp);
    source = src.getName();
    asource = src;
  }
  
  public static Source create(Atom src, Exp exp){
	  if (! exp.isAnd()){
		  exp = new BasicGraphPattern(exp);
	  }
	  Source s = new Source(src, exp);
	  return s;
  }
  
  public Atom getSource(){
	  return asource;
  }
  
  public void setState(boolean s){
	  state = s;
  }
  
  public void setLeaf(boolean s){
	  leaf = s;
  }
  
  boolean isState(){
	  return state;
  }
  
  public boolean isGraph(){
	  return true;
  }
  
  public void setRec(boolean b){
	  isRec = b;
  }
  
  public boolean isRec(){
	  return isRec;
  }
  
  void expand(NSManager nsm){
	  if (asource.isConstant()){
		  source = nsm.toNamespace(source);
		  asource = Constant.create(source);
	  }
	  super.expand(nsm);
  }

  /**
   *
   * if pattern is empty or if it contains only optional,
   * add fake ?src rdf:type ?class in such a way that ?src be bound
   */
  void finalize(Parser parser){
    Exp exp = eget(0);
    boolean option = true;
    for (int i=0; i<exp.size() && option; i++){
      if (! (exp.eget(i).isOptional()))
        option = false;
    }
    if (option){
      // add ?src rdf:type ?class
      Triple triple = createType(parser, asource);
      // generate ?src rdf:type ?class
      exp.add(triple);
    }
    else {
    	// augment ?x rdf:type c:Person 
    	// with ?x rdf:type ?class
    	// to carry the source 
    	finalizeType(parser);
    }
  }


  Exp duplicate(){
    Source exp = new Source();
    exp.asource = asource;
    exp.source = source;
    exp.state = state;
    exp.leaf = leaf;
    exp.isRec = isRec;
    return exp;
  }


  /**
   * FROM NAMED uri
   * collect source ?src variables
   * side effect : complete vars, the list of source var
   * PRAGMA : redefined by option
   * graph ?src { option { pattern } }
   * option { graph ?src { pattern } }
   */
  void collectSource(Parser parser, Vector<String> vars, Vector<String> named) {
	  // use case: graph $path {}
	  // do not collect $path as a source variable for from named <uri>
    if (! state && asource.isVariable() && ! asource.isPath() && ! vars.contains(source)) {
      vars.add(source);
    }
    eget(0).collectSource(parser, vars, named);
  }



  /**
   * FROM uri
   * generates a source ?src variable for triples with no source
   * here by definition the inner triples have a source
   * hence skip this
   */
    void setFromSource(Parser parser, String name, Vector<String> vars, Vector<String> from, 
    		boolean generate) { 
    }

    
    /**
     * Assign the source to inner triples
     */
//    void setSource(String src) {
//    	eget(0).setSource(source);
//    }
    
    
    /**
    * 1. pstate=true  Set inner source for state ?src and return list of inner variables
    * 2. pstate=false Set the source for graph ?src  
    */
    void setSource(Parser parser,  Env env, String src, boolean b) {
    	// generate variables ?s  for inner triples 
    	// generate ?s cos:subStateOf ?state 
    	// refine in option (i.e. local cos:subStateOf)
    	// state ?state {?x p ?y}  ->
    	// source ?s {?x p ?y}  ?state cos:subStateOf ?s 
    	if (env.state && ! state){
    		setStateSource(parser,   env, source, state);
    	}
    	else {
    		if (! (asource.isPath() && parser.isKgram())) {
    			// use case: graph $path {}
    			// do not set source variable for inner triples in KGRAM
    			eget(0).setSource(parser, env, source,  state);
    		}
    		
    		if (env.state && leaf && ! env.leaves.contains(source)){
    			env.leaves.add(source);
    		}
    	}
    	if (isRec){
    		// graph rec ?g {PAT}
    		for (Exp exp : eget(0).getBody()){
    			exp.setRec(true);
    		}
    	}
    }
    
    
    /**
     * This is a graph (not state) and pstate=true i.e. process inner states
     * hence the generated subStateOf triples are within this graph
     */
    void setStateSource(Parser parser,  Env env, String src, boolean b) {
    	Exp exp;
    	Env nenv = env.fork();
    	
    	//name = parser.newVar(name)  ; 
    	eget(0).setSource(parser,  nenv, source, state);
    	if (nenv.vars.size() > 0){
    		// we have found state ?src, generate ?src cos:subStateOf ?si
    		get(0).defState(parser,  nenv);
    	}
    }
    


    public StringBuffer toString(StringBuffer sb) {
        sb.append(KeywordPP.GRAPH + KeywordPP.SPACE);
        if (state) sb.append(KeywordPP.STATE + KeywordPP.SPACE);
        sb.append(asource);
        sb.append(KeywordPP.SPACE);
        for (int i=0; i<size(); i++){
        	sb.append(eget(i).toString());
        }
        return sb;       
    }

 
  public boolean validateData(){
	  if (asource.isVariable()) return false;

	  Exp ee = this;
	  if (size() == 1 && get(0) instanceof And){
		  // dive into {}
		  ee = get(0);
	  }

	  for (Exp exp : ee.getBody()){
		  if (! (exp.isTriple() && exp.validateData())){
			  return false;
		  }
	  }
	  return true;
  }

  

}