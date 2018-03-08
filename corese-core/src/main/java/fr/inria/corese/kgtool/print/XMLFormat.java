package fr.inria.corese.kgtool.print;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

/**
 * SPARQL XML Result Format for KGRAM Mappings
 * 
 * Olivier Corby, Edelweiss INRIA 2011
 * 
 */
public class XMLFormat  {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	static final String VAR1= "?";
	static final String VAR2= "$";
	Query query;
	ASTQuery ast;
	Mappings lMap;
	Vector<String> select;
	PrintWriter pw;

	public  static final String SPARQLRES   =  "http://www.w3.org/2005/sparql-results#";
	private static final String XMLDEC ="<?xml version=\"1.0\" ?>";
	private static final String OHEADER ="<sparql xmlns='" + SPARQLRES + "'>";
	private static final String CHEADER="</sparql>";
	private static final String OHEAD="<head>";
	private static final String CHEAD="</head>";
	private static final String OVAR ="<variable name='";
	private static final String CVAR ="'/>";
	private static final String ORESULTS="<results>";
	private static final String CRESULTS="</results>";
	private static final String ORESULT="<result>";
	private static final String CRESULT="</result>";
	private static final String OBOOLEAN="<boolean>";
	private static final String CBOOLEAN="</boolean>";
	private static final String[] XML = {"&", "<"};
	private static final String ODATA = "<![CDATA[";
	private static final String CDATA = "]]>";
	private static final String OCOM = "<!--";
	private static final String CCOM = "-->";
        private long nbResult = Long.MAX_VALUE;

	
	boolean displaySort = false;
	NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);

	
	XMLFormat (Mappings lm){
		lMap = lm;
		nf.setMaximumFractionDigits(4);
	}
	
	XMLFormat (){
	}
	
	public static XMLFormat create(Mappings lm){
		Query q = lm.getQuery();
		return XMLFormat.create(q, (ASTQuery)q.getAST(), lm);
	}
	
	public static XMLFormat create(Query q, ASTQuery ast, Mappings lm){
		XMLFormat res = new XMLFormat(lm);
		res.setQuery(q);
		res.setAST(ast);
		return res;
	}
	
	void setQuery(Query q){
		query = q;
		select = new Vector<String> ();
		for (Node node : q.getSelect()){
			select.add(node.getLabel());
		}
	}
	
	ASTQuery getAST(){
		return ast;
	}
	
	public String toString(){
		StringBuffer sb = toStringBuffer();
		return sb.toString();
	}
	
	public StringBuffer toStringBuffer(){
		StringWriter sw = new StringWriter();
		pw = new PrintWriter(sw);
		print();
		return sw.getBuffer();
	}


	public void setAST(ASTQuery q){
		ast = q;   
	}
	
	Vector<String> getSelect(){
		return select;
	}
	
	void setWriter(PrintWriter p){
		pw=p;
	}
	
	/**
	 * The main printer
	 */
	/**
	 * Print the vector of CG results, grouped and sorted and counted, as an HTML table
	 * Print it in the print writer, prints the concepts using cg2rdf pprinter
	 * The table is/can be processed by a stylesheet
	 */
	public void print() {
		print(false, "");
	}

    /**
     * @return the nbResult
     */
    public long getNbResult() {
        return nbResult;
    }

    /**
     * @param nbResult the nbResult to set
     */
    public void setNbResult(long nbResult) {
        this.nbResult = nbResult;
    }
	
	enum Title  {XMLDEC, OHEADER, CHEADER, OHEAD, CHEAD, OVAR, CVAR, 
		ORESULT, CRESULT, ORESULTS, CRESULTS};
	
	public String getTitle(Title t){
		switch (t){
		case XMLDEC: return XMLDEC;
		case OHEADER: return OHEADER;
		case CHEADER: return CHEADER;
		case OHEAD: return OHEAD;
		case CHEAD: return CHEAD;
		case OVAR: return OVAR;
		case CVAR: return CVAR;
		case ORESULT: return ORESULT;
		case CRESULT: return CRESULT;
		case ORESULTS: return ORESULTS;
		case CRESULTS: return CRESULTS;
		default: return "";
		}
	}
	
	boolean isMore(){
		return ast.isMore();
	}
	
	boolean isQTAsk(){
		return ast.isAsk();
	}
	
	public void print(boolean printInfoInFile, String fileName) {
		println(getTitle(Title.XMLDEC));
		println(getTitle(Title.OHEADER));
		error();
		detail();
		println(getTitle(Title.OHEAD));
		if (isMore()) {
			//printVar(SIMILARITY);
		}

		// print variable or functions selected in the header
		printVariables(getSelect());
		
		if (printInfoInFile && fileName != null){
			printLink(fileName);
		}
		
		println(getTitle(Title.CHEAD));
		
		if (isQTAsk()) {
			printAsk();
		} 
		else {         
			println(getTitle(Title.ORESULTS));
			if (lMap != null) {
				long n=1;
				for (Mapping map : lMap){
                                    if (n > nbResult ){
                                        break;
                                    }
                                    print(map, n++);
				}
			}
			println(getTitle(Title.CRESULTS));
		}
		
		println(getTitle(Title.CHEADER));
	}
	
	
	void detail(){
		
		if (lMap.getInsert() != null){
			println(OCOM);
			println("Insert:");
			for (Entity ent : lMap.getInsert()){
				println(ent);
			}
			println(CCOM);
		}	
		
		if (lMap.getDelete() != null){
			println(OCOM);
			println("Delete:");
			for (Entity ent : lMap.getDelete()){
				println(ent);
			}
			println(CCOM);
		}	
	}
	
	
	void error(){
		boolean b1 = ast!=null     && ast.getErrors()!=null;
		boolean b2 = query != null && query.getErrors()!=null;
		boolean b3 = query != null && query.getInfo()!=null;

		if (b1 || b2 || b3){
			
			println(OCOM);
			if (ast.getText()!=null){
				println(ast.getText());
			}
			println("");
			
			if (b1){
				for (String mes : ast.getErrors()){
					println(mes);
				}
			}
			if (b2){
				for (String mes : query.getErrors()){
					println(mes);
				}
			}
			if (b3){
				for (String mes : query.getInfo()){
					println(mes);
				}
			}
			println(CCOM);
		}
	}
	
	
	/**
	 * Print a cg result as a line of the table each column is one of the select
	 * variables of the query
	 */
	void print(Mapping map, long n){
		newResult();
		if (isDebug()){
			println("<!-- number = '" + n + "' -->");
		}
		println(getTitle(Title.ORESULT));
		for (String var : getSelect()){
			// for each select variable, get its binding and print it	
			if (map.getMappings()!=null){
				List<Node> list = map.getNodes(var, true);
				for (Node node : list){
					print(var, node); 
				}
			}
			else {
				Node value = map.getNode(var);
				print(var, value); 
			}
		}
		println(getTitle(Title.CRESULT));
	}
	
	boolean isDebug(){
		return ast.isDebug();
	}
	
	
	
	// for JSON subclass
	void newResult(){
	}

	
	/**
	 * Print one value using the SPARQL XML markup
	 */
	void print (String var, Node c) {
		if (c == null) {
			// do nothing 
			return;
		}
		display(var, (IDatatype) c.getValue());
	}

	void display(String var, IDatatype dt) {
		if (dt == null) {
			// do nothing 
			return;
		}
		String name = getName(var);
		String open  = "<binding name='" + name + "'>";
		String close = "</binding>";
		print(open);
		String str = dt.getLabel();
		
		if (dt.isLiteral()) {
			String literal = "</literal>";
			str = toXML(str);
			
			if (dt.hasLang()) {
				print("<literal xml:lang='" + dt.getLang() + "'>" + str +
						literal);
			}
			else if (dt.getDatatype() != null && dt.getCode() != IDatatype.LITERAL) {
				if (DatatypeMap.isDouble(dt)){
					//str =  nf.format(dt.doubleValue());
					str = String.format("%g", dt.doubleValue());
				}
				print("<literal datatype='" +  dt.getDatatype().getLabel()  +
						"'>" + str);
                                print(literal);
			}
			else {
				print("<literal>" + str + literal);
			}
		}
		else if (dt.isBlank()) {
			print("<bnode>" + str + "</bnode>");
		}
		else {
			print("<uri>" + StringEscapeUtils.escapeXml(str) + "</uri>");
		}
		println(close);
	}
        
       String display(Object o) {
        if (o instanceof Query) {
            o = ((Query) o).getAST();
        } 
        return o.toString();
    }
	
	void printVariables(Vector<String> select){
		for (String var : select) {
			printVar(var);
		}
	}
	
//	variable in header (<th>)
	private void printVar(String var) {
		println(getTitle(Title.OVAR) + getName(var) + getTitle(Title.CVAR));
	}
	
	void printLink(String name){
		print("<link href=\"" + name + "\"/>\n");
	}
	
	protected  String getName(String var){
		if (var.indexOf(VAR1) == 0)
			return var.substring(1);
		else return var;
	}
	
	// protect & <
	String toXML(String str){
		for (String p : XML){
			if (str.indexOf(p) != -1){
				str = ODATA + str + CDATA;
				return str;
			}
		}
		return str;
	}
	
	
	protected void println(String str){
		pw.println(str);
	}
	
	protected void println(Object obj){
		pw.println(obj.toString());
	}
	
	protected void print(String str){
		pw.print(str);
	}
	
	public void printAsk() {
		String res = "true";
		if (lMap == null || lMap.size() == 0 ){ 
			res = "false";
		} 
		print(OBOOLEAN);
		print(res);
		println(CBOOLEAN);
	}

	
	public void printEmpty(int clause) {
		println(OHEADER);
		println(OHEAD);
		println(CHEAD);	
		if (ast.isSelect()) {
			print(ORESULTS);println(">");
			println(CRESULTS);
		}
		println(CHEADER);
	}
	

	
}