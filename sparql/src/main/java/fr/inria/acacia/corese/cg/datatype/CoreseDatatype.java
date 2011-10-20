package fr.inria.acacia.corese.cg.datatype;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * The top level class of all the Corese datatypes
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public class CoreseDatatype 
	implements IDatatype 
	{
	static final CoreseURI datatype=new CoreseURI(RDF.RDFSRESOURCE);
	static final int LITERAL=0;
	static final int STRING=1;
	static final int XMLLITERAL=2;
	static final int NUMBER=3;
	static final int DATE=4;
	static final int BOOLEAN=5;
	static final int STRINGABLE=6;
	static final int URI=7;
	static final int UNDEF=8;
	static final int BLANK=9;
	final static CoreseString empty=new CoreseString("");
	final static CoreseDatatypeException failure  = new CoreseDatatypeException("statically created");
	static Hashtable<String, CoreseString> lang2dataLang=new Hashtable<String, CoreseString>(); // 'en' -> CoreseString('en')
	//static DatatypeMap dm = new DatatypeMap();
	static final     int LESSER = -1, GREATER = 1;
	static boolean SPARQLCompliant = false; //Corese.SPARQLCompliant;
	static DatatypeMap dm = DatatypeMap.create();
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(CoreseDatatype.class);
		
	/**
	 * Default lang is "" for literals, But for URI which is null (see CoreseURI)
	 */
	public IDatatype getDataLang() {
		return empty;
	}
	
	public String toString(){
		return getNormalizedLabel();
	}
	
	public String toSparql(){
		String value = toString();
		if (getDatatype() != null){
			String datatype = getDatatype().getLabel();
			if (datatype.startsWith(RDF.XSD)){
				datatype = datatype.substring(RDF.XSD.length());
				datatype = "xsd:" + datatype;
			}
			else {
				datatype = "<" + datatype + ">";
			}
			
			value = "\"" + value + "\""+ "^^" + datatype;
		}
		else if (getLang() != null && getLang()!=""){
			value = "\"" + value + "\"" + "@" + getLang();
		}
		else if (isLiteral()){
			value = "\"" + value + "\"";
		}
		else if (isURI()) {
			value = "<" + value + ">";
		}
		else if (isBlank()) {}
		
		return value;
	}
	
	/**
	 * Store the lang as an instance of CoreseString shared by all literals
	 */
	public CoreseString intGetDataLang(String lang){
		CoreseString dtl=lang2dataLang.get(lang);
		if (dtl == null){
			if (lang.equals(""))
				dtl= empty;
			else dtl=new CoreseString(lang);
			lang2dataLang.put(lang, dtl);
		}
		return dtl;
	}
	
	static  int code=-1;
	
	public CoreseDatatype() {
	}
	
	/**
	 * create a literal, normalize its label (for unique markers)
	 * we accept not well formed numbers, created with CoreseUndef datatype 
	 */
	public static IDatatype normalizeCreate(String label, String lang, String valueJType, String conceptTypeLabel)
	throws CoreseDatatypeException{
		try{
			label = DatatypeMap.getDatatypeNormalizedLabel(conceptTypeLabel, label);
		}
		catch (CoreseDatatypeException cde){
			if (valueJType.equals(Cst.jTypeUndef)){
				// toto^^xsd:integer : continue because we create 
				// CoreseUndef with xsd:integer as datatype
			}
			else {
				logger.error(cde.getMessage());
				throw cde;
			}
		}
		
		try {
			IDatatype dt = CoreseDatatype.create(valueJType, conceptTypeLabel, label, lang);
			return dt; 
		} 
		catch (CoreseDatatypeException cde){
			logger.fatal(cde.getMessage());
			throw cde;
		}
	}
	
	public static IDatatype create(String valueJType, String label) throws CoreseDatatypeException {
		return create(valueJType, null, label, null);
	}
	
	
	/**
	 * Create a datatype.
	 * If it is a not well formed number, create a CoreseUndef
	 */
	public static IDatatype createLiteral(String label, String datatype,  String lang)
	throws CoreseDatatypeException {
		return DatatypeMap.createLiteralWE(label, datatype, lang);
	}
	
	public static IDatatype createLiteral(String label)
	throws CoreseDatatypeException {
		return DatatypeMap.createLiteralWE(label);
	}
	
	
	
	public static IDatatype createResource(String label)
	throws CoreseDatatypeException {
		return DatatypeMap.createResource(label);
	}
	
	public static IDatatype createBlank(String label)
	throws CoreseDatatypeException {
		return DatatypeMap.createBlank(label);
	}
	
	

	
	/**
	 * Create a datatype.
	 * If it is a not well formed number, create a CoreseUndef
	 */
	public static IDatatype create(String valueJType,
			String datatype, String label, String lang)
	throws CoreseDatatypeException {
		try {
			return create(valueJType, datatype, label, lang, false);
		}
		catch (CoreseDatatypeException e){
			if (CoreseDatatype.isNumber(datatype) && 
					! valueJType.equals(Cst.jTypeUndef)){
				// toto^^xsd:integer
				// try UndefLiteral with integer datatype
				IDatatype dt = create(Cst.jTypeUndef, datatype, label, lang, false);
				return dt;
			}
			throw e;
		}
	}
	
	
	public static IDatatype create(String valueJType,
			String datatype, String label, String lang, boolean cast)
	throws CoreseDatatypeException {
		if (lang!=null) lang=lang.toLowerCase() ;
		if (valueJType.equals(Cst.jTypeString)){
			return new CoreseString(label);
		}
		else if (valueJType.equals(Cst.jTypeURI)){
			return new CoreseURI(label);
		}
		else if (valueJType.equals(Cst.jTypeLiteral)){
			IDatatype dt = new CoreseLiteral(label);
			dt.setLang(lang);
			return dt;
		}
		String display = (datatype == null) ? valueJType : datatype;
		
		IDatatype o = null;
		Class valueClass = null;
		
		try{
			valueClass = Class.forName(valueJType);
		}
		catch(ClassNotFoundException e){
			throw new CoreseDatatypeException(e,valueJType,label);
		}
		
		try {
			Class[] argClass = {label.getClass()};
			String[] arg = {label};
			o = (IDatatype)valueClass.getConstructor(argClass).newInstance((Object[])arg);
		}
		
		catch (ClassCastException e){
			throw new CoreseDatatypeException(e,valueJType,label);}
		catch (IllegalAccessException e){
			throw new CoreseDatatypeException(e,valueJType,label);}
		catch (InstantiationException e){
			throw new CoreseDatatypeException(e,valueJType,label);}
		catch (NoSuchMethodException e){
			throw new CoreseDatatypeException(e,valueJType,label);}
		catch (java.lang.reflect.InvocationTargetException e){
			// CoreseDatatypeException arrives here
			if (cast){
				// to speed up sparql cast we throw a static exception
				// because it will be transformed to null
				throw failure;
			}
			else {
				throw new CoreseDatatypeException(e, display, label);}
			}
		
		o.setLang(lang);
		o.setDatatype(datatype);
		return o;
	}
	
	/**
	 * Create Constant from SPARQL AST Triple language
	 */
//	public Constant getConstant(){
//		return Constant.create(this);
//	}
	
	public boolean isBindable(){
		if (isURI() || isBlank()) return true;
		
		switch (getCode()){
		case NUMBER: return false;
		default: return true;
		}
	}

	public boolean isDatatype(){
		return true;
	}
	
	public boolean isXMLLiteral(){
		return false;
	}
	
	public IDatatype cast(IDatatype target, IDatatype javaType) {
		String type = javaType.getNormalizedLabel();
		IDatatype dt = cast(type);
		// effective for undef only :
		if (dt != null) dt.setDatatype(target.getLabel());
		return dt;
	}
	
	
	/**
	 * cast above set the datatype uri
	 * should have datatype as extra arg
	 */ 
	IDatatype cast(String type) {
		try {
			if (isBlank() && type.equals(Cst.jTypeString))
				return CoreseDatatype.create(type, null, "", null);
			return CoreseDatatype.create(type, null, getNormalizedLabel(), null, true);
		}
		catch (CoreseDatatypeException e) {
			//e.printStackTrace();
			return null;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Following SPARQL EBV Effective Boolean Value cercion rule, RDF terms
	 * coerce to type error but literals, see number and string
	 */
	public boolean isTrue() throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean isTrueAble()  {
		return false;
	}
	
	public boolean isArray(){
		return false;
	}
	
	public CoreseArray getArray(){
		return null;
	}
	
	public IDatatype[] getValues(){
		return null;
	}
	
	public IDatatype get(int n){
		return null;
	}
	
	public int size(){
		return 0;
	}
	
	
	public boolean isBlank() {
		return false;
	}
	
	public void setObject(Object obj){
	}
	
	public Object getObject(){
		return null;
	}
	
	
	public void setBlank(boolean b) {
	}
	
	public boolean isLiteral() {
		return true;
	}
	
	public boolean isDecimal(){
		return false;
	}
	
	public boolean isFloat(){
		return false;
	}
	
	public boolean isInteger(){
		return false;
	}
	
	public boolean isURI() {
		return false;
	}
	
	public void setLang(String str){
	}
	
	public boolean hasLang(){
		return false;
	}
	
	public String getLang(){
		return null;
	}
	
	public void setDatatype(String uri){
		
	}
	
	public IDatatype getDatatype(){
		return datatype;
	}
	
	// rdfs:Literal has ExtDatatype (See CoreseLiteral)
	public IDatatype getExtDatatype(){
		return getDatatype();
	}
	
	// URI has rdfs:Resource as datatype
	public IDatatype getIDatatype(){
		return getExtDatatype();
	}
	
	public  int getCode(){
		return code;
	}
	
	// comparable at cache time ?
//	public boolean isComparable(VarMarker m){
//		return true;
//	}
//	
//	public IDatatype eval(IMemory memory) {
//		return this;
//	}
//	
//	public int countBind(IMemory mem){
//		return 1;
//	}
	
	public boolean startsWith(IDatatype iod){
		return false;
	}
	
	public boolean contains(IDatatype iod){
		return false;
	}
	
	public String getLowerCaseLabel() {
		return "";
	}
	
	public void setValue(String str) {}
	
	public String getNormalizedLabel() {
		return "";
	}
	
	public void setValue(IDatatype dt){
		
	}
	
	public String getLabel(){
		return getNormalizedLabel();
	}
	
//	public boolean isStringable() {
//	return false;
//	}
	
//	public boolean isOrdered() {
//	return false;
//	}
	
//	public boolean isRegExpable() {
//	return false;
//	}
	
	public boolean isNumber() {
		return false;
	}
	
	public double getdValue(){
		return -1;
	}
	
	public long getlValue(){
		return -1;
	}
	
	public int getiValue(){
		return -1;
	}
	
	
	/**
	 * Generic comparison between datatypes, used to sort the projection cache
	 * and to sort results.
	 * It sorts deterministically different datatypes with their natural order
	 * (e.g. : numbers, strings, dates)
	 * The order is : Literal,  URI,  BlankNode
	 * Literal : number date string/literal/XMLliteral/boolean undef
	 * numbers are sorted by values
	 * string/literal/XMLLiteral/undef/boolean are sorted alphabetically then by datatypes
	 * (by their code)
	 * literals are sorted with their lang if any
	 * TODO : the primary order (Lit URI BN) is inverse of SPARQL !!!
	 */
	
	public int compareTo(IDatatype d2){
		int code2 = d2.getCode();
		boolean b = false;
		
		switch (code2){
		case URI:
		case BLANK:
		case STRING:
			
			if (getCode() == code2){
				return this.getLabel().compareTo(d2.getLabel());
			}
			break;
					
		case NUMBER:
		case BOOLEAN:
			
			if (getCode() == code2){
				try {
					b = this.less(d2);
				}
				catch (CoreseDatatypeException e) {}
				if (b) return LESSER;
				else if (this.sameTerm(d2)) return 0;
				else return GREATER;
			}
		}
		
		boolean trace = false;
		IDatatype d1 = this;
		

		if (SPARQLCompliant){
			// BN uri literal
			// literal last
			if (d2.isLiteral()) {
				if (! d1.isLiteral()) return LESSER;
			}
			else if (d1.isLiteral()) return GREATER;
			// BN first
			if (d1.isBlank()) {
				if (! d2.isBlank()) return LESSER;
			}
			else if (d2.isBlank()) return GREATER;
		}
		else {
			// generic last
			if (d2.isBlank()) {
				if (! d1.isBlank()) return LESSER;
			}
			else if (d1.isBlank()) return GREATER;
			// literal first
			if (d1.isLiteral()) {
				if (! d2.isLiteral()) return LESSER;
			}
			else if (d2.isLiteral()) return GREATER;
		}
		
		//boolean sameDatatype = (d1.getDatatype() == d2.getDatatype());
		boolean sameDatatype = equivalentDatatype(d2);

		if (! sameDatatype){
			//  sort number date string/literal/..
			if   (d1 instanceof CoreseNumber){
				if (d2 instanceof CoreseNumber){
					try {
						b = d1.less(d2);
					}
					catch (CoreseDatatypeException e) {}
					if (b) return LESSER;
					else if (d1.sameTerm(d2)) return 0;
					else return GREATER;
				}
				else return LESSER;
			}
			else if (d2 instanceof CoreseNumber) return GREATER;
			else if (d1 instanceof CoreseDate) return LESSER;
			else if (d2 instanceof CoreseDate) return GREATER;
		}
		
		// compare same datatypes
		// also compare string/literal/XMLLiteral/boolean/undef
		try {b = d1.less(d2);}
		catch (CoreseDatatypeException e){}
		
		if (b)
			return LESSER;
		else if  (d1.semiEquals(d2)){
			// equal (modulo language if any)
			if (d1.getDataLang() == d2.getDataLang()){
				// same lang or no lang
				if (sameDatatype)
					return 0; // same/no lang : are equal
				else {
					// sort them arbitrarily
					// TODO BUG  undef datatypes have same code
					// this discriminates string  XMLLiteral undef :
					if (d1.getCode() < d2.getCode()) return LESSER;
					else if (d1.getCode() > d2.getCode()) return GREATER;
					else return d1.getDatatype().compareTo(d2.getDatatype());
				}
			}
			// equal but different languages :
			else {
				// sort by lang :
				try{
					if (d1.getDataLang().less(d2.getDataLang())) return LESSER;
					else return GREATER;
				}
				catch (CoreseDatatypeException e){
					logger.debug("CoreseDatatype.java ");
					e.printStackTrace(); return LESSER;} // never happens on languages
			}
		}
		else return GREATER;
	}
	
	/**
	 * Same datatype or String & Literal
	 */
	boolean equivalentDatatype(IDatatype dt){
		return 
			getDatatype() == dt.getDatatype() ||
			getCode() == STRING  && dt.getCode() == LITERAL ||
			getCode() == LITERAL && dt.getCode() == STRING ;
	}
	
	// default generic functions :
	
	public int compare(IDatatype iod)  throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean less(IDatatype iod)  throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
	/**
	 * Is this dtvalue comparable to datatype dt ??
	 * dtvalue is CoreseInteger 1 and dt is CoreseURI xsd:integer for example
	 */
	public boolean compatible(IDatatype dt){
		String name  = dt.getLabel();
		if (isNumber(name)) return getCode() == NUMBER;
		else if (name.equals(RDF.xsddate) || name.equals(RDF.xsddateTime))
			return getCode() == DATE;
		else return true;
	}
	
	static boolean isNumber(String name){
		return name.equals(RDF.xsdinteger) || name.equals(RDF.xsddouble) || 
		name.equals(RDF.xsdfloat)   || name.equals(RDF.xsddecimal);
	}
	
	
//	public boolean less(IDatatype iod)  throws CoreseDatatypeException {
//	return compare(iod) < 0;
//	}
//	
//	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
//	return compare(iod) <= 0;
//	}
//	
//	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
//	return compare(iod) > 0;
//	}
//	
//	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
//	return compare(iod) >= 0;
//	}
//	
	
	// never happens because every datatype has its own equals
	public boolean equals(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean sameTerm(IDatatype iod) {
		try {
			return equals(iod);
		}
		catch (CoreseDatatypeException e){
			return false;
		}
	}
	
	
	public boolean semiEquals(IDatatype iod) {
		return sameTerm(iod);
	}
	
	public IDatatype plus(IDatatype iod) {
		return null;
	}
	
	public IDatatype minus(IDatatype iod) {
		return null;
	}
	
	public IDatatype mult(IDatatype iod) {
		return null;
	}
	
	public IDatatype div(IDatatype iod) {
		return null;
	}
	
	
	
	
	public IDatatype polyplus(CoreseDouble iod) {
		return null;
	}
	
	public IDatatype polyminus(CoreseDouble iod) {
		return null;
	}
	
	public IDatatype polymult(CoreseDouble iod) {
		return null;
	}
	
	public IDatatype polydiv(CoreseDouble iod) {
		return null;
	}
	
	public IDatatype polyplus(CoreseLong iod) {
		return null;
	}
	
	public IDatatype polyminus(CoreseLong iod) {
		return null;
	}
	
	public IDatatype polymult(CoreseLong iod) {
		return null;
	}
	
	public IDatatype polydiv(CoreseLong iod) {
		return null;
	}
	
	
	
	/**
	 * Default definitions of datatype operators return type error or false
	 */
	
	CoreseDatatypeException failure() {
		return failure;
	}
	
	public int polyCompare(CoreseDouble icod)  throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreaterOrEqual(CoreseDouble icod) throws CoreseDatatypeException {
		throw failure();  
	}
	
	public boolean polymorphGreater(CoreseDouble icod) throws CoreseDatatypeException {
		throw failure() ;  
	}
	
	public boolean polymorphLessOrEqual(CoreseDouble icod) throws CoreseDatatypeException{
		throw failure() ;  
	}
	
	public boolean polymorphLess(CoreseDouble icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphEquals(CoreseDouble icod)throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	/*******************************/
	
	public int polyCompare(CoreseLong icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreaterOrEqual(CoreseLong icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreater(CoreseLong icod) throws CoreseDatatypeException {
		throw failure() ; //return false;
	}
	
	
	public boolean polymorphLessOrEqual(CoreseLong icod) throws CoreseDatatypeException{
		throw failure();
	}
	
	public boolean polymorphLess(CoreseLong icod) throws CoreseDatatypeException {
		throw failure() ; //return false;
	}
	
	public boolean polymorphEquals(CoreseLong icod)throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	
	/********************************************/
	
	/**
	 * We arrive here when there is a type clash with equals
	 * 1. datatype1  vs datatype2 is an error
	 * 2. URI or BN  vs datatype  return false
	 */
	public boolean defaultEquals(IDatatype dt) throws CoreseDatatypeException {
		if (isLiteral())
			throw failure(); 
		else 
			return false;   
	}
	
	public boolean polymorphEquals(CoreseXMLLiteral icod) throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	public boolean polymorphEquals(CoreseLiteral icod) throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	public boolean polymorphEquals(CoreseString icod) throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	public boolean polymorphEquals(CoreseBoolean icod) throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	public boolean polymorphEquals(CoreseUndefLiteral icod) throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	
	public int polyCompare(CoreseStringLiteral icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreaterOrEqual(CoreseStringLiteral icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreater(CoreseStringLiteral icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLessOrEqual(CoreseStringLiteral icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLess(CoreseStringLiteral icod) throws CoreseDatatypeException{
		throw failure();
	}
	
	public IDatatype polyplus(CoreseStringableImpl iod) {
		return null;
	}
	
	public IDatatype polyminus(CoreseStringableImpl iod) {
		return null;
	}
	
	/************************************/
	
	public int polyCompare(CoreseURI icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreaterOrEqual(CoreseURI icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreater(CoreseURI icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLessOrEqual(CoreseURI icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLess(CoreseURI icod) throws CoreseDatatypeException{
		throw failure();
	}
	
	// comparing with URI is not an error, just false
	public boolean polymorphEquals(CoreseURI icod)throws CoreseDatatypeException {
		return false;
	}
	
	public IDatatype polyplus(CoreseURI iod) {
		return null;
	}
	
	public IDatatype polyminus(CoreseURI iod) {
		return null;
	}
	
	
	/***********************************/
	
	
	public int polyCompare(CoreseBlankNode icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreaterOrEqual(CoreseBlankNode icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreater(CoreseBlankNode icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLessOrEqual(CoreseBlankNode icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLess(CoreseBlankNode icod) throws CoreseDatatypeException{
		throw failure();
	}
	
	// comparing with BN is not an error, just false
	public boolean polymorphEquals(CoreseBlankNode icod)throws CoreseDatatypeException {
		return false;
	}
	
	public IDatatype polyplus(CoreseBlankNode iod) {
		return null;
	}
	
	public IDatatype polyminus(CoreseBlankNode iod) {
		return null;
	} 	    
	
	
	/**********************************************************/
	/***********************DATE**************************/
	/**********************************************************/
	
	public int polyCompare(CoreseDate icod)  throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreaterOrEqual(CoreseDate icod)  throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphGreater(CoreseDate icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphLessOrEqual(CoreseDate icod) throws CoreseDatatypeException {
		throw failure() ; //return false;
	}
	
	public boolean polymorphLess(CoreseDate icod) throws CoreseDatatypeException {
		throw failure();
	}
	
	public boolean polymorphEquals(CoreseDate icod) throws CoreseDatatypeException {
		return defaultEquals(icod);
	}
	
	public String getDatatypeURI() {
		//return getDatatype().getNormalizedLabel();
		if (getDatatype() != null)
			return getDatatype().getNormalizedLabel();
		else return null;
	}
	
	public double getDoubleValue() {
		return getdValue();
	}
	
	public int getIntegerValue() {
		return getiValue();
	}
	
	public IDatatype getDatatypeValue() {
		return this;
	}
	
	public String getStringValue() {
		return getLabel();
	}
	
	public boolean isPath(){
		return false;
	}

//	public IPath getPath(){
//		return null;
//	}
	
}