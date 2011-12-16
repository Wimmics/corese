package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

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
	
	public boolean isOptional(){
		return true;
	}
	
	// draft: sparql compliance
	public boolean isSPARQL(){
		return false;
	}


	public StringBuffer toString(StringBuffer sb) {
		sb.append(KeywordPP.OPTIONAL + KeywordPP.SPACE);
		for (int i=0; i<size(); i++){
			sb.append(eget(i).toString());
		}
		return sb;
	}
	

	
}