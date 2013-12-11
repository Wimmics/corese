package fr.inria.acacia.corese.cg.datatype;

import java.lang.reflect.Method;
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
 * This class is used to map a datatype name to its java type representation ands its marker set.
 * <br>
 * @author Olivier Savoie
 */

public class DatatypeMap implements Cst, RDF {

	/** logger from log4j */
	private static Logger logger = Logger.getLogger(DatatypeMap.class);
	
	public static IDatatype ZERO = newInstance(0);

	private static Hashtable<String, Mapping> ht ;
	static DatatypeMap dm = DatatypeMap.create();
	// if true, number values are equal by = but not match same sparql variable 
	// otherwise same value space, match same sparql variable
	// 
	public static boolean SEVERAL_NUMBER_SPACE = true;
	// corese behaviour:
	static boolean literalAsString = true;
	private static final String DEFAULT = "default";
	private static final String NWFL = "NWFL" ;
	private static final String BLANK = BLANKSEED + "bb" ;
	static long COUNT = 0; 	
	public static CoreseBoolean TRUE  = CoreseBoolean.TRUE;
	public static CoreseBoolean FALSE = CoreseBoolean.FALSE;


	private class Mapping {
		private String javatype = "";
//		private String datatypeMarkerSet = "";
//		private Hashtable<String, String> datatypeHashMarkerSet;
		private Method method = null;
		private Class cl = null;



		public Mapping(String jtype){//, Hashtable<String, String> htms){
			javatype = jtype;
			//datatypeHashMarkerSet = htms;
//			Class str = null;
//			try{
//				cl = Class.forName(jtype);
//				str = Class.forName("java.lang.String");
//			}
//			catch(ClassNotFoundException e){
//				//MUST not occur !!! jtype is always specified correctly
//				//coming from the DatatypeMap
//				logger.fatal(e.getMessage());
//			}
//
//			Class[] methodArg = {str};
//			try{
//				try{
//					//search in the class
//					method = cl.getDeclaredMethod("getNormalizedLabel", methodArg);
//				}
//				catch(NoSuchMethodException e){
//					//else in the superclass
//					method = cl.getMethod("getNormalizedLabel", methodArg);
//				}
//			}
//			catch(NoSuchMethodException e){
//				//MUST not occur !!! getNormalizedLabel is a method of IDatatype
//				//so, each CoreseDatatype implements this method, if not the project
//				//doesn't compile
//				logger.fatal(e.getMessage());
//			}
		}


//		String getLiteralMarkerSet(String lang){
//			return datatypeHashMarkerSet.get(lang);
//		}
//
//
//		String getDataTypeMarkerSet(){
//			return datatypeMarkerSet;
//		}
//		
//		void setDataTypeMarkerSet(String ms){
//			 datatypeMarkerSet = ms;
//		}
//
//		Hashtable<String, String> getDataTypeMarkerSetHash(){
//			return datatypeHashMarkerSet;
//		}
//		
//		void setDataTypeMarkerSetHash(Hashtable<String, String> dtms){
//			 datatypeHashMarkerSet = dtms;
//		}

		String getJavaType(){
			return javatype;
		}

//		Method getNormalizedLabelMethod(){
//			return method;
//		}
//
//		Class getNormalizedLabelClass(){
//			return cl;
//		}
	}


	public DatatypeMap() {
		if(ht == null) //as ht is static, DatatypeMap class is a singleton
			ht = new Hashtable<String, Mapping> ();
		init();
	}

	public static DatatypeMap create(){
		return new DatatypeMap();
	}

	public void put(String dt, String jtype, Hashtable<String, String> htms){
		Mapping map = new Mapping(jtype);//,htms);
		//map.setDataTypeMarkerSetHash(htms);
		ht.put(dt, map);
	}

	public void put(String dt, String jtype, String dtms){
		Mapping map = new Mapping(jtype);//,dtms);
		//map.setDataTypeMarkerSet(dtms);
		ht.put(dt, map);
	}

	

	/**
	 * Return the java class name implementing the datatype
	 */
	public String getJType(String dt){
		if (dt==null)
			return null;
		Mapping map=getMapping(dt);
		if (map == null)
			return null;
		String jDatatype=map.getJavaType();
		return jDatatype;
	}

	Mapping getMapping(String dt){
		Mapping map=ht.get(dt);
		if (map == null){
			// create a new literal space (i.e. dt) value for this unknown datatype
			put(dt, jTypeUndef, dt); // CoreseUndefLiteral
			map=ht.get(dt);
		}
		return map;
	}



	/**
	 * Defines the datatype map between XSD datatypes and the java class
	 * that implements the datatype and the marker set that contain the marker.
	 * We separate number value spaces by giving different marker set to integer and float
	 */
	public void init(){

		//define the hashtable that 1 MS for (LITERAL,lang)
		Hashtable<String, String> htlang = new Hashtable<String, String>();
		htlang.put(DEFAULT,RDFSLITERAL);
		put(RDFSLITERAL,  jTypeLiteral,htlang);
		put(rdflangString,jTypeLiteral,htlang);

		put(XMLLITERAL,	jTypeXMLString,		XMLLITERAL);
		put(xsdstring, 	jTypeString,xsdstring);
		put(xsdboolean,	jTypeBoolean,		xsdboolean);  
		put(xsdanyURI,  jTypeURI,			xsdstring );
		
		put(xsdnormalizedString, jTypeString,	xsdstring);
		put(xsdtoken, 	jTypeString,			xsdstring);
		put(xsdnmtoken, jTypeString,			xsdstring );
		put(xsdname, 	jTypeString,			xsdstring );
		put(xsdncname, 	jTypeString,			xsdstring );
		put(xsdlanguage,jTypeString,			xsdstring );

		String intSpace = xsdinteger;
		// Integer + store datatype URI ?
		String intJType = jTypeInteger;		

		put(xsddouble,	jTypeDouble, xsddouble);
		put(xsdfloat,	jTypeFloat,  xsdfloat);
		put(xsddecimal, jTypeDecimal,xsddecimal);		
		put(xsdinteger,	jTypeInteger,intSpace);
		put(xsdlong,	jTypeLong,intSpace);
		
		put(xsdshort,			intJType,intSpace);
		put(xsdint,			jTypeInt,intSpace);
		put(xsdbyte,			intJType,intSpace);
		put(xsdnonNegativeInteger, 	intJType,intSpace);
		put(xsdnonPositiveInteger, 	intJType,intSpace);
		put(xsdpositiveInteger, 	intJType,intSpace);
		put(xsdnegativeInteger, 	intJType,intSpace);
		put(xsdunsignedLong,    	intJType,intSpace);
		put(xsdunsignedInt,     	intJType,intSpace);
		put(xsdunsignedShort,   	intJType,intSpace);
		put(xsdunsignedByte,    	intJType,intSpace);

		put(xsddate,	jTypeDate,	xsddate);
		put(xsddateTime,jTypeDateTime,	xsddateTime);
		put(xsdday,	jTypeDay,	xsdday);
		put(xsdmonth,	jTypeMonth,	xsdmonth);
		put(xsdyear,	jTypeYear,	xsdyear);
		put(xsddaytimeduration,	jTypeGeneric, xsddaytimeduration);

		//special use case: to get the implementation java type for Resource and Blank
		put(RDFSRESOURCE, jTypeURI, RDFSRESOURCE);


	}
	
	void define(String datatype, int code){
		
	}
	
	void defineString(String datatype){
		
	}
	
	void defineInteger(String datatype){
		
	}
	
	int getType(String datatype){
		return IDatatype.UNDEF;
	}
	
	public void init2(){
		
		define(RDFSLITERAL,	 IDatatype.LITERAL);
		define(rdflangString,IDatatype.LITERAL);
		define(XMLLITERAL,	 IDatatype.XMLLITERAL);
		define(xsdboolean,	 IDatatype.BOOLEAN);  
		define(xsdanyURI,    IDatatype.URI);
		define(xsdstring, 	 IDatatype.STRING);
		define(RDFSRESOURCE, IDatatype.URI);
		
		defineString(xsdnormalizedString);
		defineString(xsdtoken);
		defineString(xsdnmtoken);
		defineString(xsdname);
		defineString(xsdncname);
		defineString(xsdlanguage);

		define(xsddouble, 	IDatatype.DOUBLE);
		define(xsdfloat,	IDatatype.FLOAT);
		define(xsddecimal, 	IDatatype.DECIMAL);
		define(xsdinteger,	IDatatype.INTEGER);
		define(xsdlong,		IDatatype.LONG);
		
		defineInteger(xsdshort);
		defineInteger(xsdint);
		defineInteger(xsdbyte);
		defineInteger(xsdnonNegativeInteger);
		defineInteger(xsdnonPositiveInteger);
		defineInteger(xsdpositiveInteger);
		defineInteger(xsdnegativeInteger);
		defineInteger(xsdunsignedLong);
		defineInteger(xsdunsignedInt);
		defineInteger(xsdunsignedShort);
		defineInteger(xsdunsignedByte);

		define(xsddate,		IDatatype.DATE); //jTypeDate,		xsddate);
		define(xsddateTime, IDatatype.DATETIME); //jTypeDateTime,	xsddateTime);
		
		define(xsdday,		IDatatype.DAY); //jTypeDay,	xsdday);
		define(xsdmonth,	IDatatype.MONTH); //jTypeMonth,	xsdmonth);
		define(xsdyear,		IDatatype.YEAR); //jTypeYear,	xsdyear);
		
		define(xsddaytimeduration,	IDatatype.DURATION); // CoreseGeneric

	}
	
	
	static boolean isNumber(String name){
		return name.equals(xsdlong) ||name.equals(xsdinteger) || name.equals(xsdint) 
                        || name.equals(xsddouble) || 
		name.equals(xsdfloat)   || name.equals(xsddecimal);
	}
        
        /**
         * URI of a literal datatype xsd: rdf:XMLLiteral rdf:PlainLiteral
         */
	public static boolean isDatatype(String range) {
            return range.startsWith(XSD) 
                    || range.equals(XMLLITERAL) 
                    || range.equals(rdflangString);
        }
        
        public static boolean isUndefined(IDatatype dt){
            return dt.getCode() == IDatatype.UNDEF;
        }

	
	IDatatype create(String label, String datatype, String lang){
		switch(getType(datatype)){
		case IDatatype.STRING: return new CoreseString(label);
		}
		
		return null;
	}
	

	public static IDatatype newInstance(double result) {
		return new CoreseDouble(result);
	}
	
	public static IDatatype newInstance(double result, String datatype) {
		if (datatype.equals(xsdinteger)){
			return new CoreseInteger((int)result);
		}
		else if (datatype.equals(xsdlong)){
			return new CoreseLong((long)result);
		}
		else if (datatype.equals(xsddecimal)){
			return new CoreseDecimal(result);
		}
		else if (datatype.equals(xsdfloat)){
			return new CoreseFloat(result);
		}
		return new CoreseDouble(result);
	}

	public static IDatatype newInstance(float result) {
		return new CoreseFloat(result);
	}

	public static IDatatype newInstance(int result) {
		return new CoreseInteger(result);
	}

	public static IDatatype newInstance(long result) {
		return new CoreseLong(result);
	}
	
	public static IDatatype newInstance(String result) {
		return new CoreseString(result);
	}
	
	public static IDatatype newStringBuilder(StringBuilder result) {
		return new CoreseStringBuilder(result);
	}
	
	public static IDatatype newStringBuilder(String result) {
		return new CoreseStringBuilder(new StringBuilder(result));
	}
	
	public static IDatatype newInstance(boolean result) {
            if (result){
                return CoreseBoolean.TRUE;
            }
            return CoreseBoolean.FALSE;
	}
	
	public static IDatatype newResource(String result) {
		return new CoreseURI(result);
	}
	
	public static IDatatype newDate() {
		try {
			return new CoreseDateTime();
		} catch (CoreseDatatypeException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IDatatype newDate(String date) {
		try {
			return new CoreseDate(date);
		} catch (CoreseDatatypeException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Create a datatype.
	 * If it is a not well formed number, create a CoreseUndef
	 */
	public static IDatatype createLiteral(String label, String datatype){
		return createLiteral(label, datatype, null);
	}


	public static IDatatype createLiteral(String label, String datatype,  String lang){
		IDatatype dt = null;
		try {
			dt = createLiteralWE(label, datatype, lang);
		} catch (CoreseDatatypeException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
                        dt = createUndef(label, datatype);
		}
		return dt;
	}

	
	public static IDatatype createLiteralWE(String label, String datatype,  String lang)
	throws CoreseDatatypeException {
		if (datatype == null){
			datatype = datatypeURI(lang);
		}
		String JavaType = dm.getJType(datatype);
		IDatatype dt = CoreseDatatype.create(JavaType, datatype, label, lang);
		return dt;
	}
	
	public static IDatatype createLiteral(String label){
		IDatatype dt = null;
		try {
			dt = createLiteralWE(label);
		} catch (CoreseDatatypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dt;
	}
	
	
	public static IDatatype createLiteralWE(String label)
	throws CoreseDatatypeException {
		String datatype = RDFSLITERAL;
		if (literalAsString){
			datatype = xsdstring;
		}
		String JavaType = dm.getJType(datatype);
		IDatatype dt = CoreseDatatype.create(JavaType, datatype, label, "");
		return dt;
	}
        
        public static IDatatype createObject(String name){
            return createLiteral(name, XMLLITERAL, null);
        }
        
        public static IDatatype createUndef(String label, String datatype){
            IDatatype dt = new CoreseUndefLiteral(label);
            dt.setDatatype(datatype);                  
            return dt;
        }
	

	/**
	 * Order of Datatypes & rdfs:Literal vs xsd:string
	 */
	public static void setSPARQLCompliant(boolean b){
		CoreseDatatype.SPARQLCompliant = b;
		literalAsString = ! b;
	}
	
	public static void setLiteralAsString(boolean b){
		literalAsString = b;
	}
	
	public static boolean isLiteralAsString(){
		return literalAsString;
	}
	
	static String datatypeURI(String lang){
		if (literalAsString && (lang == null || lang == "")){ 
			return  xsdstring;
		}
		else {
			return RDFSLITERAL;
		}
	}
	
	public static String datatype(String lang){
		if (literalAsString && (lang == null || lang == "")){ 
			return  qxsdString;
		}
		else {
			return qrdfsLiteral;
		}
	}
	
	
	public static IDatatype createResource(String label){
		return new CoreseURI(label);
	}
        
        public static IDatatype createSkolem(String label){
		return new CoreseURI(label);
	}
	
	public static IDatatype createBlank(String label){
		return new CoreseBlankNode(label);
	}
	
	public static IDatatype createBlank(){
		return new CoreseBlankNode(BLANK + COUNT++);
	}

//	public  String undefType(){
//		return jTypeUndef;
//	}

	/*******************************
	 * 
	 * Accessors
	 * 
	 */
	
	public static boolean isStringLiteral(IDatatype dt){
		return (dt instanceof CoreseString) || (dt instanceof CoreseLiteral);
	}
	
	// literal with or without lang
	public static boolean isLiteral(IDatatype dt){
		return (dt instanceof CoreseLiteral);
	}

	public static boolean isString(IDatatype dt){
		return (dt instanceof CoreseString);
	}
	
	// literal without lang
	public static boolean isSimpleLiteral(IDatatype dt){
		return (dt instanceof CoreseLiteral) && ! dt.hasLang();
	}
	
	public static boolean isInteger(IDatatype dt){
		return dt.getCode() == IDatatype.INTEGER;
	}
	
	public static boolean isLong(IDatatype dt){
		return dt.getCode() == IDatatype.LONG;
	}
	
	public static boolean isFloat(IDatatype dt){
		return dt.getCode() == IDatatype.FLOAT;
	}
	
	public static boolean isDouble(IDatatype dt){
		return dt.getCode() == IDatatype.DOUBLE;
	}
	
	public static boolean isDecimal(IDatatype dt){
		return dt.getCode() == IDatatype.DECIMAL;
	}
	
	public static boolean isBindable(IDatatype dt){
		return ((CoreseDatatype)dt).isBindable();
	}
	
	public static IDatatype getTZ(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getTZ();
	}
	
	public static IDatatype getTimezone(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getTimezone();
	}

	
	public static IDatatype getYear(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getYear();
	}
	
	public static IDatatype getMonth(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getMonth();
	}
	
	public static IDatatype getDay(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getDay();
	}
	
	public static IDatatype getHour(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getHour();
	}
	
	public static IDatatype getMinute(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getMinute();
	}
	
	public static IDatatype getSecond(IDatatype dt){
		if (!(dt instanceof CoreseDate)) return null;
		CoreseDate date = (CoreseDate) dt;
		return date.getSecond();
	}
		
	// for literal only
	 public static boolean check(IDatatype dt, String range) {
             if (dt.isNumber()){
                 if (! isNumber(range)){
                     return false;
                 }
             }
             else if (isNumber(range)){
                 return false;
             }
             return dt.getDatatypeURI().equals(range);            
         }


}