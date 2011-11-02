package fr.inria.edelweiss.kgram.event;

import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.path.Path;

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

}
