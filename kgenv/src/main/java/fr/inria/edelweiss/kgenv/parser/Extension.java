package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * SPARQL Extension in Pragma
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Extension extends Pragma {
	
	Extension(Query q, ASTQuery a){
		super(q, a);
	}
	
	public static Extension create(Query q){
		return new Extension(q, (ASTQuery) q.getAST());
	}
	
	public void parse(fr.inria.acacia.corese.triple.parser.Exp exp){
		if (exp.isQuery()){
			
		}
		else if (exp.isBGP()){
			for (fr.inria.acacia.corese.triple.parser.Exp ee : exp.getBody()){
				parse(ee);
			}
		}
	}

}
