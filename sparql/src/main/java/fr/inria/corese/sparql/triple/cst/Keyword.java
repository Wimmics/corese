package fr.inria.corese.sparql.triple.cst;

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
	final static public String SHORT = "short";

	final static public String DISTINCT = "distinct";

	final static public String SDEPTH = "d";
	final static public String SBREADTH = "b";
	
	final static public String MATCH = "match";
	final static public String STNOT = "not";
	
	final static public String SFAIL = "fail";


}
