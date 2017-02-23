package fr.inria.acacia.corese.cg.datatype;

import java.util.Hashtable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.api.IDatatype;
import static fr.inria.acacia.corese.cg.datatype.Cst.jTypeInteger;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.storage.api.IStorage;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.api.core.TripleStore;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import java.util.List;

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
	private static Logger logger = LogManager.getLogger(CoreseDatatype.class);

	static final CoreseURI datatype=new CoreseURI(RDF.RDFSRESOURCE);
	static final CoreseString empty=new CoreseString("");
	static final CoreseDatatypeException failure  = new CoreseDatatypeException("Datatype Exception, statically created");
	static final Hashtable<String, CoreseString> lang2dataLang = new Hashtable<String, CoreseString>(); // 'en' -> CoreseString('en')
        static final Hashtable<String, CoreseURI>    hdt = new Hashtable<String, CoreseURI>(); // datatype name -> CoreseURI datatype
	static final DatatypeMap dm = DatatypeMap.create();
	static final NSManager nsm = NSManager.create();

	static final     int LESSER = -1, GREATER = 1;
	static boolean SPARQLCompliant = false; 
	
	static int cindex = 0;
	private int index = IDatatype.VALUE;
	
		
	/**
	 * Default lang is "" for literals, But for URI which is null (see CoreseURI)
	 */
        @Override
	public IDatatype getDataLang() {
		return empty;
	}
	
        @Override
	public String toString(){
		return toSparql(true, false);
	}       
	
        @Override
	public String toSparql(){
		return toSparql(true, false);
	}
	

        @Override
	public String toSparql(boolean prefix){          
            return toSparql(prefix, false);
        }
        
        @Override
        public String toSparql(boolean prefix, boolean xsd){          
		String value = getLabel();
		if (getCode() == INTEGER && ! xsd){
			
		}
		else if (getCode() == STRING || (getCode() == LITERAL && ! hasLang()) ){
			value =  protect(value);
		}
		else if (getDatatype() != null && ! getDatatype().getLabel().equals(RDFS.rdflangString)){

			String datatype = getDatatype().getLabel();
			
			if (prefix && (datatype.startsWith(RDF.XSD))
                                || datatype.startsWith(RDF.RDF)){
				datatype = nsm.toPrefix(datatype);						
			}
			else {
				datatype = "<" + datatype + ">";
			}
			
			value =  protect(value) + "^^" + datatype;
		}
		else if (getLang() != null && getLang()!=""){
			value =  protect(value)  + "@" + getLang();
		}
		else if (isLiteral()){
			value =  protect(value) ;
		}
		else if (isURI()) {
			String str = nsm.toPrefix(value, true);
			if (str == value){
				value = "<" + value + ">";
			}
			else {
				value = str;
			}
		}
		else if (isBlank()) {}
		
		return value;
	}
	
	
	String protect(String label){
		String str = Constant.addEscapes(label, false);
		return "\"" + str + "\"";
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
	
	static CoreseURI getGenericDatatype(String uri){
		CoreseURI dt =  hdt.get(uri);
		if (dt == null){
			dt = new CoreseURI(uri);
			hdt.put(uri, dt);
		}
		return dt;
	}
	
	static  int code=-1;
	
	public CoreseDatatype() {
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
			if (CoreseDatatype.isNumber(datatype, valueJType) && 
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
		
		CoreseDatatype o = null;
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
			o = (CoreseDatatype) valueClass.getConstructor(argClass).newInstance((Object[])arg);
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
                return o.typeCheck();
	}
	
	public IDatatype typeCheck(){
            return this;
        }
        
	public boolean isBindable(){
		if (isNumber()) return false;
		return true;
	}

	public boolean isDatatype(){
		return true;
	}
	
        @Override
	public boolean isXMLLiteral(){
		return false;
	}
	
        @Override
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
        @Override
	public boolean isTrue() throws CoreseDatatypeException {
		throw failure();
	}
	
        @Override
	public boolean isTrueAble()  {
		return false;
	}
	
        @Override
	public boolean isArray(){
		return false;
	}
        
        @Override
        public boolean isList(){
            return false;
        }
        
        @Override
        public boolean isLoop(){
            return false;
        }
		
        @Override
	public List<IDatatype> getValues(){
		return null;
	}
        
        @Override
        public Iterable getLoop(){
            return null;
        }
	
        @Override
	public IDatatype get(int n){
		return null;
	}
	
        @Override
	public int size(){
		return 0;
	}
	
	
        @Override
	public boolean isBlank() {
		return false;
	}
        
        @Override
        public boolean isSkolem() {
		return false;
	}
	
        @Override
	public void setObject(Object obj){
	}
	
        @Override
	public Object getObject(){
		return null;
	}
	
	
        @Override
	public void setBlank(boolean b) {
	}
	
        @Override
	public boolean isLiteral() {
		return true;
	}
        
        @Override
        public boolean isFuture() {
		return false;
	}
        
        @Override
        public boolean isPointer(){
            return false;
        }
        
        @Override
        public Pointerable getPointerObject(){
            return null;
        }
        
        @Override
        public int pointerType(){
            return Pointerable.UNDEF_POINTER;
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
	
        @Override
	public boolean isURI() {
		return false;
	}
	
        @Override
	public void setLang(String str){
	}
	
        @Override
	public boolean hasLang(){
		return false;
	}
	
        @Override
	public String getLang(){
		return null;
	}
	
        @Override
	public void setDatatype(String uri){
		
	}
	
        @Override
	public IDatatype getDatatype(){
		return datatype;
	}
	
	@Deprecated
        @Override
	public IDatatype getExtDatatype(){
		return getDatatype();
	}
	
	// URI has rdfs:Resource as datatype
        @Override
	public IDatatype getIDatatype(){
		return getDatatype();
	}
	
        @Override
	public  int getCode(){
		return code;
	}
	
	
        @Override
	public boolean startsWith(IDatatype iod){
		return false;
	}
	
        @Override
	public boolean contains(IDatatype iod){
		return false;
	}
	
        @Override
	public String getLowerCaseLabel() {
		return "";
	}
	
        @Override
	public void setValue(String str) {}
        
        @Override
        public void setValue(String str, int nid, IStorage pmgr) {}
	
        @Override
	public String getNormalizedLabel() {
		return "";
	}
	
        @Override
	public void setValue(IDatatype dt){
		
	}
	
        @Override
	public String getLabel(){
		return getNormalizedLabel();
	}
        
        @Override
	public StringBuilder getStringBuilder(){
		return null;
	}
        
        @Override
        public void setStringBuilder(StringBuilder s){
	}
       	
        @Override
	public boolean isNumber() {
		return false;
	}
	
        @Override
        public boolean booleanValue(){
            return false;
        }
        
        @Override
       public String stringValue(){
            return getLabel();
        }
	
        @Override
	public long longValue(){
		return -1;
	}
	
        @Override
	public int intValue(){
		return -1;
	}
	
        @Override
	public double doubleValue(){
		return  -1;
	}
	
        @Override
	public float floatValue(){
		return  -1;
	}
	
	
	
        @Override
	public double getdValue(){
		return -1;
	}
	
        @Override
	public long getlValue(){
		return -1;
	}
	
        @Override
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
        @Override
        public int compareTo(Object d2){
            return compareTo((IDatatype) d2);
        }

	
        @Override
	public int compareTo(IDatatype d2){
		int code = getCode(), other = d2.getCode();
		boolean b = false;
		
		switch (other){
		
		case STRING:
			if (code == other){
				// Generic Datatype is also STRING
				return this.getLabel().compareTo(d2.getLabel());
			}
			break;
		
		case URI:
		case BLANK:
		case XMLLITERAL:
			
			if (code == other){
				return this.getLabel().compareTo(d2.getLabel());
			}
			break;
						
		//case NUMBER:
		case DOUBLE:
		case FLOAT:
		case DECIMAL:
		case LONG:
		case INTEGER:
	
			switch (code){
			//case NUMBER:
			case DOUBLE:
			case FLOAT:
			case DECIMAL:
			case LONG:
			case INTEGER:
				try {
					return compare(d2);
				}
				catch (CoreseDatatypeException e) {}

			}		
			
		case BOOLEAN:
		case DATE:
			
			if (code == other){
				try {
					b = this.less(d2);
				}
				catch (CoreseDatatypeException e) {}
				if (b) return LESSER;
				else if (this.sameTerm(d2)) return 0;
				else return GREATER;
			}
                    break;
		
		case UNDEF:
			
			if (code == UNDEF){
				int res = getDatatypeURI().compareTo(d2.getDatatypeURI());
				if (res == 0){
					return getLabel().compareTo(d2.getLabel());
				}
				else {
					return res;
				}
			}	
			
			
		}
		
		boolean trace = false;
		IDatatype d1 = this;
		
		
		
		
		switch (code){
		// case where CODE are not equal 
		// SPARQL order:
		// Blank URI Literal

		case BLANK: return LESSER;
		
		case URI:
			switch (other){
				case BLANK: return GREATER;
				// other is Literal
				default: return LESSER;
			}
			
		default:
			// this is LITERAL
			switch (other){
			case BLANK: 
			case URI: return GREATER;
		}
		}
		
		
		// String vs Literal
		switch (code){
		
		case STRING:
			if (other == LITERAL){ 
				int res = d1.getLabel().compareTo(d2.getLabel());
				if (res == 0 && d2.hasLang()){
					return LESSER;
				}
				return res;
			}
			break;
			
			
		case LITERAL:
			
			switch (other){
			
			case STRING:
					int res =  d1.getLabel().compareTo(d2.getLabel());
					if (res == 0 && hasLang()){
						return GREATER;
					}
					return res;
				
				
			case LITERAL:
				
				res =  d1.getLabel().compareTo(d2.getLabel());
				
				if (! hasLang() && ! d2.hasLang()) {
					return res;
				}

				if (res == 0){
					if (! hasLang()){
						return LESSER;
					}
					else if (! d2.hasLang()){
						return GREATER;
					}
					else {
						return getLang().compareTo(d2.getLang());
					}
				}
				
				return res;
				
			}
			
			
		
		}
		
		
		
		
		if (code < other){
			return LESSER;
		}
		else {
			return GREATER;
		}
		
		
	}
	
	
	
	
//	public int compareTo2(IDatatype d2){
//		int other = d2.getCode();
//		boolean b = false;
//		
//		switch (other){
//		case URI:
//		case BLANK:
//		case STRING:
//			
//			if (getCode() == other){
//				return this.getLabel().compareTo(d2.getLabel());
//			}
//			break;
//					
//		case NUMBER:
//		case BOOLEAN:
//			
//			if (getCode() == other){
//				try {
//					b = this.less(d2);
//				}
//				catch (CoreseDatatypeException e) {}
//				if (b) return LESSER;
//				else if (this.sameTerm(d2)) return 0;
//				else return GREATER;
//			}
//		}
//		
//		boolean trace = false;
//		IDatatype d1 = this;
//
//		if (SPARQLCompliant){
//			// BN uri literal
//			// literal last
//			if (d2.isLiteral()) {
//				if (! d1.isLiteral()) return LESSER;
//			}
//			else if (d1.isLiteral()) return GREATER;
//			// BN first
//			if (d1.isBlank()) {
//				if (! d2.isBlank()) return LESSER;
//			}
//			else if (d2.isBlank()) return GREATER;
//			
//		}
//		else {
//			// generic last
//			if (d2.isBlank()) {
//				if (! d1.isBlank()) return LESSER;
//			}
//			else if (d1.isBlank()) return GREATER;
//			// literal first
//			if (d1.isLiteral()) {
//				if (! d2.isLiteral()) return LESSER;
//			}
//			else if (d2.isLiteral()) return GREATER;
//			
//		}
//		
//		//boolean sameDatatype = (d1.getDatatype() == d2.getDatatype());
//		boolean sameDatatype = equivalentDatatype(d2);
//
//		if (! sameDatatype){
//			//  sort number date string/literal/..
//			if   (d1 instanceof CoreseNumber){
//				if (d2 instanceof CoreseNumber){
//					try {
//						b = d1.less(d2);
//					}
//					catch (CoreseDatatypeException e) {}
//					if (b) return LESSER;
//					else if (d1.sameTerm(d2)) return 0;
//					else return GREATER;
//				}
//				else 
//					return LESSER;
//			}
//			else if (d2 instanceof CoreseNumber) return GREATER;
//			else if (d1 instanceof CoreseDate) return LESSER;
//			else if (d2 instanceof CoreseDate) return GREATER;
//		}
//		
//		// compare same datatypes
//		// also compare string/literal/XMLLiteral/boolean/undef
//		try {b = d1.less(d2);}
//		catch (CoreseDatatypeException e){}
//		
//		if (b)
//			return LESSER;
//		else if  (d1.semiEquals(d2)){
//			// equal (modulo language if any)
//			if (d1.getDataLang() == d2.getDataLang()){
//				// same lang or no lang
//				if (sameDatatype)
//					return 0; // same/no lang : are equal
//				else {
//					// sort them arbitrarily
//					// TODO BUG  undef datatypes have same code
//					// this discriminates string  XMLLiteral undef :
//					if (d1.getCode() < d2.getCode()) return LESSER;
//					else if (d1.getCode() > d2.getCode()) return GREATER;
//					else return d1.getDatatype().compareTo(d2.getDatatype());
//				}
//			}
//			// equal but different languages :
//			else {
//				// sort by lang :
//				try{
//					if (d1.getDataLang().less(d2.getDataLang())) return LESSER;
//					else return GREATER;
//				}
//				catch (CoreseDatatypeException e){
//					logger.debug("CoreseDatatype.java ");
//					e.printStackTrace(); return LESSER;} // never happens on languages
//			}
//		}
//		else return GREATER;
//	}
	
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
	
        @Override
	public boolean less(IDatatype iod)  throws CoreseDatatypeException {
		throw failure();
	}
	
        @Override
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
        @Override
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
        @Override
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
	
	
	static boolean isNumber(String name, String cname){
		return name.equals(RDF.xsdlong) ||name.equals(RDF.xsdinteger) ||name.equals(RDF.xsdint) || name.equals(RDF.xsddouble) || 
		name.equals(RDF.xsdfloat)   || name.equals(RDF.xsddecimal) || name.equals(RDF.xsddate) ||
                cname.equals(jTypeInteger);
	}
	
	
	/**
	 * Every datatype has its own type safe equals
	 */
        @Override
	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
		throw failure();
	}
        
	
	// Java equals (for list membership ...)
        @Override  
        public boolean equals(Object obj) {
		if (obj instanceof IDatatype) {
			return sameTerm((IDatatype) obj);
		}
                else if (obj instanceof Node){
                    return sameTerm((IDatatype) ((Node)obj).getValue());
                }
		return false;	
	}

    @Override
    public int hashCode() {        
        return super.hashCode();
    }
	
        @Override
	public boolean sameTerm(IDatatype iod) {
		try {
			return equalsWE(iod);
		}
		catch (CoreseDatatypeException e){
			return false;
		}
	}
	
	
        @Override
	public boolean semiEquals(IDatatype iod) {
		return sameTerm(iod);
	}
	
        @Override
	public IDatatype plus(IDatatype iod) {
		return null;
	}
	
        @Override
	public IDatatype minus(IDatatype iod) {
		return null;
	}
	
        @Override
	public IDatatype mult(IDatatype iod) {
		return null;
	}
	
        @Override
	public IDatatype div(IDatatype iod) {
		return null;
	}
	
	
	
	
	
	
	
	/**
	 * Default definitions of datatype operators return type error or false
	 */
	
	CoreseDatatypeException failure() {
		return failure;
	}
	
	
	
        @Override
	public String getDatatypeURI() {
		if (getDatatype() != null)
			return getDatatype().getNormalizedLabel();
		else return null;
	}
	
        @Override
	public double getDoubleValue() {
		return getdValue();
	}
	
        @Override
	public int getIntegerValue() {
		return getiValue();
	}
	
        @Override
	public IDatatype getDatatypeValue() {
		return this;
	}
	
	public String getStringValue() {
		return getLabel();
	}
	
	public boolean isPath(){
		return false;
	}
	
	
	
	/****************************************************************
	 * 
	 * Draft IDatatype implements Node
	 * To get rid of both Node & IDatatype objects
	 * IDatatype would be a node in graph directly
	 * 
	 ****************************************************************/


        @Override
	public int getIndex() {
            return index;
	}


        @Override
	public void setIndex(int n) {
		index = n;
	}


        @Override
	public boolean same(Node n) {
            return sameTerm((IDatatype) n.getValue());
	}


        @Override
	public int compare(Node n) {
            return compareTo((IDatatype) n.getValue());
	}


        @Override
	public boolean isVariable() {
		return false;
	}


        @Override
	public boolean isConstant() {
		return true;
	}


        @Override
	public Object getValue() {
		return this;
	}


        @Override
	public Object getProperty(int p) {
		return null;
	}


        @Override
	public void setProperty(int p, Object o) {
	}

	@Override
	public Edge getEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Node getNode(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getGraph() {
		// TODO Auto-generated method stub
		return null;
	}
        
	@Override
	public int nbNode() {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public String getKey() {
        return Node.INITKEY;
    }

    @Override
    public void setKey(String str) {
    }

        @Override
    public String getID() {
        if (isLiteral()) {
            return toSparql();
        } else {
            return getLabel();
        }
    }

    @Override
    public Object getProvenance() {
        return null;    
    }
    
        @Override
    public void setProvenance(Object obj){
        
    }

    @Override
    public Mappings getMappings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mapping getMapping() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity getEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue(String var, int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query getQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public TripleStore getTripleStore() {
        // TODO Auto-generated method stub
        return null;
    }

    }
