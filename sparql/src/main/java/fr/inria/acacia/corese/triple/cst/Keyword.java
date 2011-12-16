package fr.inria.acacia.corese.triple.cst;

public interface Keyword {
	
	final static public String REGEX = "regex";
	final static public String SOR = "|";
	final static public String SEOR = "||";
	final static public String SEAND = "&&";
	final static public String SENOT = "!";
	final static public String SBE = "^";
	
	final static public String SPLUS = "+";
	final static public String SMINUS = "-";
	final static public String SMULT = "*";
	final static public String SDIV = "/";
	final static public String SQ = "?";
	final static public String SEQ = "=";
	final static public String SNEQ = "!="; 
	final static public String STLEC = "<=::"; // for classes
	final static public String STLE = "<=:";

	final static public String SINV = "i";
	final static public String SSHORT = "s";
	final static public String SSHORTALL = "sa";

	final static public String SDEPTH = "d";
	final static public String SBREADTH = "b";
	
	final static public String MATCH = "match";
	final static public String STNOT = "not";
	
	
	// WARNING: shared by kgram and corese
	public final static String GENURI = "genURI";
	public final static String SCOUNT = "count";
	public final static String COUNTITEM = "countItem";
	public final static String AVG = "avg";
	public final static String SUM = "sum";
	public final static String TIME = "time";
	public final static String GTIME = "gtime";
	public final static String NBRESULTS = "nbResults"; 
	public final static String ARRAY = "row"; 
	public final static String MIN 	= "min";
	public final static String MAX 	= "max";
	static final String CONCAT = "group_concat";
	static final String SAMPLE = "sample";
	
	public static final String[] aggregate = 
	{GENURI, SCOUNT, AVG, SUM, COUNTITEM, TIME, GTIME, ARRAY, NBRESULTS, MIN, MAX, CONCAT, SAMPLE};
	
	final static public String SFAIL = "fail";


}
