package fr.inria.corese.kgenv.parser;


/**
 * Compiler Factory for Transformer
 * Generate target Edge/Node/Filter

 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class CompilerFacKgram implements CompilerFactory {


	
	public CompilerFacKgram(){
	}
	
	public CompilerFacKgram(boolean b){
	}
	
	public Compiler newInstance(){
		return new CompilerKgram();
	}


}
