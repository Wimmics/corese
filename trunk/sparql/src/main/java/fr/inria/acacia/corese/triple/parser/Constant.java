package fr.inria.acacia.corese.triple.parser;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Constant extends Atom {
	private static Logger logger = Logger.getLogger(Constant.class);
	private static NSManager nsm;
	static DatatypeMap dm;
	IDatatype dt;
	
	String srcDatatype = null; // the source datatype if any (not infered)
	boolean literal = false;
	boolean isBlank = false;
	int weight = 1;
	private Variable var;
	// draft regexp
	private Expression exp;

	public Constant() {}

	 Constant(String name) {
		super(name);
		// by safety:
		setLongName(name);
		datatype = RDFS.RDFSRESOURCE;
	}

	 Constant(String name, String dt, String lg, boolean bliteral) {
		super(name, dt, lg);
		literal = bliteral;
	}

	 Constant(String name, String dt) {
		this(name, dt, null, true);
	}
	
	 Constant(String name, String dt, String lg) {
		this(name, dt, lg, true);		
	}           
	
	public static Constant create(String str){
		return new Constant(str);
	}
	
	public static Constant create(){
		return new Constant();
	}
	
	public static Constant createResource(){
		Constant cst = new Constant();
		cst.setDatatype(RDFS.RDFSRESOURCE);
		return cst;
	}
	
	public static Constant createBlank(){
		Constant cst = new Constant();
		cst.setDatatype(RDFS.RDFSRESOURCE);
		cst.setBlank(true);
		return cst;
	}
	
	public static Constant createBlank(String label){
		Constant cst = new Constant(label);
		cst.setBlank(true);
		return cst;
	}
	
	public static Constant createResource(String str){
		Constant cst =  new Constant(str);
		if (nsm==null){
			nsm = NSManager.create();
		}
		cst.setLongName(nsm.toNamespace(str));
		return cst;
	}
	
	public static Constant create(int n){
		return new Constant(Integer.toString(n), RDFS.xsdinteger);
	}
	
	public static Constant create(boolean b){
		return new Constant((b)?"true":"false", RDFS.xsdboolean);
	}
	
	public static Constant create(String name, String dt) {
		return new Constant(name, dt, null);		
	}
	
	public static Constant create(String name, String dt, String lg) {
		Constant cst = new Constant(name, dt, lg);
		return cst;
	}
	
	public static Constant array(ExpressionList el){
		return new Array(el);
	}
	
	public StringBuffer toString(StringBuffer sb) {
		if (isLiteral()){
			
			
			if (lang != null) {
				//return name + "@" + lang;
				toString(name, sb);
				sb.append(KeywordPP.LANG + lang);
			} 
			else if (hasRealDatatype()) {
				if (datatype.equals(RDF.qxsdInteger) || datatype.equals(RDF.xsdinteger)){
					sb.append(name);
				}
				else if (datatype.startsWith("http://")){
					toString(name, sb);
					sb.append(KeywordPP.SDT + "<"+ datatype +">");
				}
				else {
					toString(name, sb);
					if (! datatype.equals(RDF.qxsdString)){
						sb.append(KeywordPP.SDT + datatype);
					}
				}
			} 
			else {
				toString(name, sb);
				return sb;
			}
		}
		else if (isBlank()) {
			sb.append(name);
		} 
		else if (isQName) {
			sb.append(name);
		} 
		else {
			sb.append(KeywordPP.OPEN + getLongName() + KeywordPP.CLOSE);
		}
		return sb;
	}
	
	public StringBuffer toString2(StringBuffer sb) {
		String str = getDatatypeValue().toString();
		sb.append(str);
		return sb;
	}
	
	/**
	 * Escape special chars
	 * Add surrounding quotes to a string literal
	 */
	public static void toString(String str, StringBuffer sb){
		String s = addEscapes(str);
		String sep = KeywordPP.QUOTE;
		
		if (s.contains(KeywordPP.QUOTE)){
			if (s.contains(KeywordPP.DQUOTE)){
				sep = KeywordPP.TQUOTE;
			}
			else {
				sep = KeywordPP.DQUOTE;
			}
		}
		
		sb.append(sep);
		sb.append(s);
		sb.append(sep);				
	}
	
	
	
	/**
	 *  source: javacc
	 *  replace special char by escape char for pprint
	 */
	 public static String addEscapes(String str) {
	      StringBuffer retval = new StringBuffer();
	      char ch;
	      for (int i = 0; i < str.length(); i++) {
	        switch (str.charAt(i))
	        {
	           case 0 :
	              continue;
	           case '\b':
	              retval.append("\\b");
	              continue;
	           case '\t':
	              retval.append("\\t");
	              continue;
	           case '\n':
	              retval.append("\\n");
	              continue;
	           case '\f':
	              retval.append("\\f");
	              continue;
	           case '\r':
	              retval.append("\\r");
	              continue;
	           case '\"':
	              retval.append("\\\"");
	              continue;
	           case '\'':
	              retval.append("\\\'");
	              continue;
	           case '\\':
	              retval.append("\\\\");
	              continue;
	           default:
	        	   retval.append(str.charAt(i));
	        	   
//	              if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
//	                 String s = "0000" + Integer.toString(ch, 16);
//	                 retval.append("\\u" + s.substring(s.length() - 4, s.length()));
//	              } else {
//	                 retval.append(ch);
//	              }
	              	              
	              continue;
	        }
	      }
	      return retval.toString();
	   }

	
	public Constant getConstant() {
		return this;
	}
	
	public boolean equals(Object c){
		if (c instanceof Constant){
			Constant cc = (Constant) c;
			return getDatatypeValue().sameTerm(cc.getDatatypeValue());
		}
		return false;
	}
	
	/**
	 * 
	 */
	public IDatatype createDatatype(){
		if (dm == null){
			dm = DatatypeMap.create();
			nsm = NSManager.create();
		}
		IDatatype dt = null;
		if (isBlank()){
			//dt = CoreseDatatype.create(Cst.jTypeBlank, null, name, null);
			dt = DatatypeMap.createBlank(name);
		}
		else if (isResource()){
			String str = getLongName();
			if (str == null){
				logger.error("** Constant2Datatype: longName missing: " + this);
				str = name;
			}
			dt = DatatypeMap.createResource(str);

		}
		else {
			String ndt =  nsm.toNamespace(datatype);
			dt = DatatypeMap.createLiteral(name, ndt,  lang);
		}
		return dt;
		
	}
	
	static String getJavaType(String datatypeURI){
		if (dm == null){
			dm = DatatypeMap.create();
			nsm = NSManager.create();
		}
		return dm.getJType(nsm.toNamespace(datatypeURI));
	}
	
	public IDatatype getDatatypeValue(){
		if (dt == null) dt = createDatatype();
		return dt;
	}
	
	public Expression compile(ASTQuery ast){
		getDatatypeValue();
		return this;
	}
	
	/**
	 * Create Constant from IDatatype
	 */
	public static Constant create(IDatatype dt){
		Constant cst;
		if (dt.isLiteral()){
			cst = Constant.create(dt.getLabel(), dt.getDatatype().getLabel(), dt.getLang());
		}
		else {
			// URI & Blank
			cst = Constant.create(dt.getLabel());
			if (dt.isBlank()){
				cst.setBlank(true);
			}
		}
		cst.setDatatypeValue(dt);
		return cst;
	}
	
	public boolean  hasDatatypeValue(){
		return dt != null;
	}
	
	public void setDatatypeValue(IDatatype dd){
		dt = dd;
	}

	public int regLength(){
		return 1;
	}
	
	public int length(){
		return 1;
	}
	
	void setLiteral(boolean b) {
		literal = b;
	}

	public boolean isConstant() {
		return true;
	}

	public boolean isLiteral() {
        return literal;
    }
	
	public boolean isBlank(){
		return isBlank;
	}
	
	public void setBlank(boolean b){
		isBlank = b;
	}
	
	public void setWeight(String w){
		try {
			setWeight(Integer.parseInt(w));
		}
		catch (Exception e){
		}
	}
	
	public void setWeight(int w){
		weight = w;
	}

	
	public int getWeight(){
		return weight;
	}
	
	public boolean isNumber(){
		return false;
	}
	
	public boolean isResource() {
		return ! literal && ! isBlank;
	}

	// use when "get:name::?x" or "c:SomeRelation::?p" because we have both a variable and a constant
    public void setVar(Variable s) {
        var = s;
    }
    
    public void setExpression(Expression e) {
        exp = e;
    }
    
    public Expression getExpression() {
        return exp;
    }

    // use when "get:name::?x" or "c:SomeRelation::?p" because we have both a variable and a constant
    public Variable getIntVariable() {
        return var;
    }

 
    
	/**
	 * KGRAM
	 */
	
	public int type(){
		return ExprType.CONSTANT;
	}
	
	public Object getValue(){
		return dt;
	}
	
	
	public Constant copy(){
		Constant cst;
		if (isLiteral()){
			cst = new Constant(getLabel(), getDatatype(), getLang());
		}
		else {
			cst = new Constant(getLabel());
			cst.setQName(isQName);
		}
		cst.setLongName(getLongName());
		return cst;
	}
	
	public Expression transform(boolean isReverse){
		Constant cst = this;
		if (isReverse){
			cst = copy();
			cst.setReverse(isReverse);
			cst.setWeight(getWeight());
		}
		cst.setretype(cst.getretype());
		return cst;
	}
    

}