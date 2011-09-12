package fr.inria.edelweiss.kgram.api.core;

/**
 * Interface of Property Path Regex
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Regex {
	
	public static final int UNDEF	= -1;
	public static final int LABEL	= 0;
	public static final int NOT 	= 1;
	public static final int SEQ 	= 2;
	public static final int STAR 	= 3;
	public static final int PLUS 	= 4;
	public static final int OPTION 	= 5;
	public static final int COUNT 	= 6;
	public static final int ALT		= 7;
	public static final int TEST	= 8;

	
	String getName();
	
	String getLongName();
	
	int retype();
	
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
	
	boolean isCounter();

	int getMin();
	
	int getMax();
	
	Regex getArg(int n);

	Regex reverse();
	
	Regex transform();

	Regex translate();
	
	boolean isNotOrReverse();

	int regLength();
	
	Expr getExpr();

}
