package fr.inria.corese.kgram.event;

import fr.inria.corese.kgram.core.Exp;

/**
 * Event to trace KGRAM execution
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Event {
	
	public static final int BEGIN 	= 0;
	public static final int START 	= 1;
	public static final int ENUM 	= 2;
	public static final int MATCH 	= 3;
	public static final int FILTER 	= 4;
	public static final int BIND 	= 5;
	public static final int GRAPH 	= 6;
	public static final int PATH 	= 7;
	public static final int PATHSTEP 	= 8;
	
	
	public static final int AGG 	= 13;

	public static final int FINISH 	= 14;
	public static final int DISTINCT = 15;
	public static final int LIMIT 	= 16;
	
	public static final int RESULT 	= 19;
	public static final int END 	= 20;
	

	// warning events
	
	
	public static final int UNDEF_PROPERTY 	= 30;
	public static final int UNDEF_CLASS 	= 31;
	public static final int UNDEF_GRAPH 	= 32;

	
	
	
	
	public static final int ALL 	= 50;
	
	
	
	// events from user to listener
	
	// next step
	public static final int STEP 	= 101;
	
	// next edge/node/path 
	public static final int NEXT 	= 102;
	
	// skip trace until next expression in stack
	public static final int FORWARD 	= 103;

	// eval current expression silently 
	public static final int COMPLETE 	= 104;
	
	// eval until current expression succeed 
	public static final int SUCCESS 	= 105;
	
	// resume execution silently
	public static final int QUIT 	= 109;
	
	
	
	
	// pprint current Mapping
	public static final int MAP 	= 110;
	public static final int VERBOSE = 111;
	public static final int NONVERBOSE = 113;
	public static final int HELP = 112;


	
	int getSort();
	
	Object getObject();
	
	Object getArg(int n);

	Exp getExp();
	
	boolean isSuccess();


}
