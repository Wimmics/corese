package fr.inria.corese.kgram.api.core;
import fr.inria.corese.sparql.api.IDatatype;

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
	public static final int PARA	= 8;
	public static final int TEST	= 9;
	public static final int CHECK	= 10;
	public static final int REVERSE	= 11;

	
	String getName();
	
	String getLongName();
        
        IDatatype getDatatypeValue();
	
	String toRegex();
	
	int retype();
	
	int getArity();
	
	boolean isConstant();
	
	boolean isAlt();
	
	boolean isPara();

	boolean isSeq();
	
	boolean isOpt();

	boolean isNot();
		
	boolean isDistinct();

	boolean isShort();

	// @deprecated
	boolean isInverse();

	// @deprecated
	void setInverse(boolean b);
	
	// SPARQL 1.1 reverse ^
	boolean isReverse();
	
	void setReverse(boolean b);

	boolean isStar();
	
	boolean isPlus();
	
	boolean isCounter();

	int getMin();
	
	int getMax();
	
	int getWeight();
	
	Regex getArg(int n);

	Regex reverse();
	
	Regex transform();

	Regex translate();
	
	boolean isNotOrReverse();

	int regLength();
	
	Expr getExpr();

}
