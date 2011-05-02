package fr.inria.acacia.corese.triple.parser;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
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
			//dt = CoreseDatatype.create(Cst.jTypeURI, null, str, null);
			dt = DatatypeMap.createResource(str);

		}
		else {
			String ndt =  nsm.toNamespace(datatype);
//				String JavaType = dm.getJType(ndt);
//				dt = CoreseDatatype.create(JavaType, ndt, name, lang);
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
	
	/**
	 * Create Constant from IDatatype
	 */
	public static Constant create(IDatatype dt){
		Constant cst;
		if (dt.isLiteral()){
			cst = Constant.create(dt.getLabel(), dt.getExtDatatype().getLabel(), dt.getLang());
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
     * Process get:gui filter ?x >= get:gui --> return value of get:gui
     */
    public Expression parseGet(Parser parser) {
    	if (literal)
    		return this;
    	String str = parser.pget(name); // get:gui ?
    	if (str == null)
    		return this;
    	String value = parser.getExtValue(str);
    	if (value == null) {
    		//return null;
    		if (name.startsWith(Parser.EGET)) {
    			this.setEget(true);
    			return this;
    		} else {
    			return null;
    		}
    	}
    	
    	TermParser tp = new TermParser(parser);
    	Lexer lex = new Lexer(value);
    	Expression exp = tp.exp(lex);
    	return exp;
    }
    
    
	/**
	 * KGRAM
	 */
	
	public int type(){
		return ExprType.CONSTANT;
	}
	
	public Object getValue(){
		return getDatatypeValue();
	}
    

}