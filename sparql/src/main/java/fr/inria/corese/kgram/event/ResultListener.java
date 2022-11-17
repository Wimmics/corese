package fr.inria.corese.kgram.event;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.path.Path;

/**
 * Result Listener to process KGRAM result on the fly
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public interface ResultListener {
		
	/**
	 * For each solution, kgram call process(env)
	 * If return true:  Mapping created as usual
	 * If return false: Mapping not created
	 */
	boolean process(Environment env);
	
	
	/**
	 * For each path, kgram call process(path)
	 * If return true:  Mapping created as usual
	 * If return false: Mapping not created
	 */
	boolean process(Path path);
	
	boolean enter(Edge ent, Regex exp, int size);
	
	boolean leave(Edge ent, Regex exp, int size);
        
        boolean listen(Exp exp, Edge query, Edge target);
        
        Exp listen(Exp exp, int n);
	
        void listen(Expr exp);
}
