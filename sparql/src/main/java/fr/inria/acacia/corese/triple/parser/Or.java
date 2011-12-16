package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Or extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	static int num = 0;
	
	public Or() {}
	
	public  Or (Exp e1, Exp e2){
		add(e1);
		add(e2);		
	}
	
	public static Or create(){
		return new Or();
	}
	
	public static Or create(Exp e1, Exp e2){
		if (!e1.isAnd()){
			e1 = new BasicGraphPattern(e1);
		}
		if (!e2.isAnd()){
			e2 = new BasicGraphPattern(e2);
		}
		return new Or(e1, e2);
	}
	
	
	public boolean isUnion(){
		return true;
	}
	

	
	String getOper() {
		return Keyword.SEOR;
	}
			
	
	public StringBuffer toString(StringBuffer sb) {
				
		sb.append(get(0).toString());
						
		for (int i=1; i<size(); i++) {
			sb.append(KeywordPP.SPACE + KeywordPP.UNION + KeywordPP.SPACE); 		
			sb.append(get(i).toString());
		}
		
		return sb;
	}
	
	
	
}