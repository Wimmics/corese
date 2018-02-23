package fr.inria.acacia.corese.triple.parser;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import java.util.List;


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
	private static Logger logger = LogManager.getLogger(Option.class);
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
		
	
        @Override
	Bind validate(Bind env, int n) throws QuerySemanticException {
		return get(0).validate(env, n+1);
	}
	
	String getOper() {
		return "option";
	}
	
        // corese option {}
        @Override
	public boolean isOption(){
		return ! isOptional;
	}
        
        // sparql option {}
        @Override
        public boolean isOptional(){
		return isOptional;
	}
        
        @Override
        public Option getOptional(){
            return this;
        }
	
        @Override
	public StringBuffer toString(StringBuffer sb) {
            if (isOptional()){
                toString(eget(0), sb);
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
        
        @Override
        void getVariables(List<Variable> list) {
            if (size()>0) {
                get(0).getVariables(list);
            }
        }
        
        void toString(Exp exp, StringBuffer sb){
            if (exp.isBGP()){
                // skip { } around exp
                exp.display(sb);
            }
            else {
                exp.toString(sb);
            }
        }
	
	public boolean validate(ASTQuery ast, boolean exist){
		boolean ok = true;
		for (Exp exp : getBody()){
			boolean b = exp.validate(ast, exist);
			ok = ok && b;
		}
		return ok;
	}
	
}