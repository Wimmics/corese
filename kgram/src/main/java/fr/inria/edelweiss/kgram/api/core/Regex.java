package fr.inria.edelweiss.kgram.api.core;

/**
 * Interface of Property Path Regex
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Regex {
	
	String getName();
	
	String getLongName();
	
	int getArity();
	
	boolean isConstant();

	boolean isOr();
	
	boolean isSeq();
	
	boolean isOpt();

	boolean isNot();
	
	boolean isInverse();
	
	void setInverse(boolean b);
	
	// SPARQL 1.1 reverse ^
	boolean isReverse();
	
	void setReverse(boolean b);

	boolean isStar();
	
	boolean isPlus();

	int getMin();
	
	int getMax();
	
	Regex getArg(int n);

	Regex reverse();
	
	Regex translate();

	int regLength();

}
