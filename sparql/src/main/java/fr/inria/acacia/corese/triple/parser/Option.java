package fr.inria.acacia.corese.triple.parser;


import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.QuerySemanticException;
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
        // false: OPTION corese (unary)
        // true:  OPTIONAL SPARQL (binary)
	public static boolean isOptional = true;

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
		
	
	Bind validate(Bind env, int n) throws QuerySemanticException {
		return get(0).validate(env, n+1);
	}
	
	String getOper() {
		return "option";
	}
	
        // corese option {}
	public boolean isOption(){
		return ! isOptional;
	}
        
        // sparql option {}
        public boolean isOptional(){
		return isOptional;
	}
	
	public StringBuffer toString(StringBuffer sb) {
            if (isOptional()){
                sb.append(eget(0).toString());
 		sb.append(KeywordPP.SPACE + KeywordPP.OPTIONAL + KeywordPP.SPACE);
                sb.append(eget(1).toString());
           }
            else {
		sb.append(KeywordPP.OPTIONAL + KeywordPP.SPACE);
		for (int i=0; i<size(); i++){
			sb.append(eget(i).toString());
		}
            }
            return sb;
	}
	
	public boolean validate(ASTQuery ast, boolean exist){
		if (getBody().size()>0){
			return getBody().get(0).validate(ast, exist);
		}
		return true;
	}

}