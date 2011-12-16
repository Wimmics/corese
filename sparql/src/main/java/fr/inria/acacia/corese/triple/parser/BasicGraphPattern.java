package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.acacia.corese.exceptions.QuerySemanticException;
  
/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class has been created to manage the scope of a blank node.
 * <br>
 * @author Virginie Bottollier
 */

public class BasicGraphPattern extends And {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
    /** The Hashtable that will contains all the blank nodes of this BasicGraphPattern */
    Hashtable<String, Variable> bnodes = null;

    /** Empty Constructor */
    public BasicGraphPattern() {
        super();
    }
    
    public BasicGraphPattern(Exp exp){
    	super(exp);
    }
     
    /** Creation of a BasicGraphPattern */
    public static BasicGraphPattern create() {
        return new BasicGraphPattern();
    }
    
    public static BasicGraphPattern create(Exp exp) {
        return new BasicGraphPattern(exp);
    }
    
    public void addFilter(Expression e){
    	add(Triple.create(e));
    }
    
    public boolean isBGP(){
		return true;
	}
    
    public StringBuffer toString(StringBuffer sb) {
    	sb.append ("{");
    	super.toString(sb);
    	sb.append("}");
    	return sb;
    }
    
    /**
     * This function add a new blank bode in the hashtable bnodes 
     * @param s1 The name of the blank node in the query
     * @param v The new blank node
     */
    public void addBNodes(String s1, Variable v) {
        if (bnodes==null) bnodes = new Hashtable<String, Variable>(); 
        bnodes.put(s1, v);
    } 
    
    /**
     * This function return the blank node that corresponds to 
     * the string s in the BasicGraphPattern part of the query 
     * @param s The name of the blank node in the query
     * @return
     */
    public Variable getBNode(String s) {
        if (bnodes==null)
            return null;
        else
            return bnodes.get(s);
    }
    

    /**
     * To check that inner optional have their variable bound by their embeding
     * BGP.
     * e.g. this is an error because ?X is not bound in first arg of optional
     * but is bound before 
     * ?X  :name "paul"
     * {?Y :name "george" . OPTIONAL { ?X :email ?Z } }
     * env is variables already bound (before this BGP)
     * return variables bound by this BGP 
     */
 
    Bind validate(Bind env, int n) throws QuerySemanticException {
    	check();
    	// var bound by this BGP (including sub BGP)
    	Bind local = new Bind(),
    	// global env + local:
    		 glocal = new Bind(), curGlocal=null, prevGlocal; 
    	glocal = glocal.merge(env);
    	for (int i=0; i<size(); i++){
    		Exp exp = get(i); 
    		prevGlocal = curGlocal; 
    		// local copy of glocal for check() optional below
    		curGlocal = new Bind(); 
    		curGlocal = curGlocal.merge(glocal);
    		
    		Bind bind = exp.validate(glocal, n);
    		
    		if (exp.isOptional() && prevGlocal!= null){
    			// for all var in exp
    			// if var !in local prev && var in global env : error			 
    			boolean ok = prevGlocal.check(local, bind);
    			if (! ok){
    				// detect an unbound variable 
    				// but if it is in a optional {} it may not be an error
    				// so skip iy by now
//    				Triple triple = 
//    					Triple.create(Term.function(Keyword.SFAIL));
//    				this.add(i, triple);
//    				i++;
    			}
    		}
    		
    		local =   local.merge(bind);
    		glocal = glocal.merge(bind);
    	}
    	return local;
    }
    
    /**
     * Variable in filter should be bound in the BGP
     */
    void check() throws QuerySemanticException {
    	if (size() == 1 && get(0).isTriple() && get(0).isExp())
    		throw new QuerySemanticException("Unbound variable in Filter: " + 
    				this.toString());
    }
    
    /**
     * SPARQL Constraint:
     * Two occurrences of same blank not separated by a pattern
     */
    public boolean validate (ASTQuery ast){
    	boolean isBefore = true;
    	Table table = new Table();
    	
//    	if (size() == 1){
//    		Exp exp = getBody().get(0);
//    		if (exp.isFilter()){
//    			Expression f = exp.getTriple().getExp();
//    			List<String> list = f.getVariables();
//    			if (list.size()>0) {
//    				ast.addError("Illegal filter in BGP: " + exp);
//    				ast.setCorrect(false);
//    				return false;
//    			}
//    		}
//    	}
    	
    	for (Exp exp : getBody()){
    		
    		if (exp.isTriple()){
    			if (! exp.isFilter()){
    				Triple t = exp.getTriple();
    				if (isBefore){
    					table.addBlank(t);
    				}
    				else {
    					Atom b = table.contains(t);
    					if (b != null){
    						ast.addError("Illegal blank references: " + b + " in " + this);
    						ast.setCorrect(false);
    						return false;
    					}
    				}
    			}
    		}
    		else {
    			isBefore = false;
    		}
    	}
    	
    	return true;
    }
    
    class Table extends Hashtable<String, String> {
    	void put(String s){
    		put(s, s);
    	}
    	
    	Atom contains(Triple t){
        	if (t.getSubject().isBlankNode()){
        		if (containsKey(t.getSubject().getLabel())){
        			return t.getSubject();
        		}
        	}
        	if (t.getObject().isBlankNode()){
        		if (containsKey(t.getObject().getLabel())){
        			return t.getObject();
        		}
        	}
        	return null;
        }
    	
    	
    	void addBlank(Triple t){
        	if (t.getSubject().isBlankNode()){
        		put(t.getSubject().getLabel());
        	}
        	if (t.getObject().isBlankNode()){
        		put(t.getObject().getLabel());
        	}
        }
        
    }
    
    
    
    
    public Exp union(){
    	if (getBody().size()<2) return this;
    	
    	Exp exp = null;
    	for (Exp e : getBody()){
    		if (exp == null){
    			exp = e;
    		}
    		else {
    			exp = Or.create(exp, e);
    		}
    	}
    	return exp;
    }
    
}
