package fr.inria.acacia.corese.cg.datatype;

import java.lang.reflect.Method;
import java.util.Hashtable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class is used to map a datatype name to its java type representation
 * ands its marker set.
 * <br>
 *
 * @author Olivier Savoie
 */
public class DatatypeMap implements Cst, RDF {

    /**
     * logger from log4j
     */
    private static Logger logger = LogManager.getLogger(DatatypeMap.class);
    public static final IDatatype ZERO = newInstance(0);
    public static final IDatatype ONE = newInstance(1);
    public static final IDatatype MINUSONE = newInstance(-1);
    public static final IDatatype ERROR   = CoreseUndefLiteral.ERROR;
    public static final IDatatype UNBOUND = CoreseUndefLiteral.UNBOUND;
    
    private static Hashtable<String, Mapping> ht;
    static DatatypeMap dm = DatatypeMap.create();
    // if true, number values are equal by = but not match same sparql variable 
    // otherwise same value space, match same sparql variable
    // 
    public static boolean SEVERAL_NUMBER_SPACE = true;
    // corese behaviour:
    static boolean literalAsString = true;
    private static final String DEFAULT = "default";
    private static final String NWFL = "NWFL";
    private static final String BLANK = BLANKSEED + "bb";
    static long COUNT = 0;
    public static CoreseBoolean TRUE = CoreseBoolean.TRUE;
    public static CoreseBoolean FALSE = CoreseBoolean.FALSE;
    public static final IDatatype EMPTY_LIST = createList(new IDatatype[0]);
    public static final IDatatype EMPTY_STRING = newInstance("");
    static final String LIST = ExpType.EXT + "List";
    private static final int INTMAX = 100;
    static  IDatatype[] intCache;
    
    static {
        intCache = new IDatatype[INTMAX];
    }

    private class Mapping {

        private String javatype = "";
        private Method method = null;
        private Class cl = null;

        public Mapping(String jtype) {//, Hashtable<String, String> htms){
            javatype = jtype;
        }

        String getJavaType() {
            return javatype;
        }
    }

    public DatatypeMap() {
        if (ht == null) { //as ht is static, DatatypeMap class is a singleton
            ht = new Hashtable<String, Mapping>();
        }
        init();
    }

    public static DatatypeMap create() {
        return new DatatypeMap();
    }

    public void put(String dt, String jtype, Hashtable<String, String> htms) {
        Mapping map = new Mapping(jtype);
        ht.put(dt, map);
    }

    public void put(String dt, String jtype, String dtms) {
        Mapping map = new Mapping(jtype);
        ht.put(dt, map);
    }

    /**
     * Return the java class name implementing the datatype
     */
    public String getJType(String dt) {
        if (dt == null) {
            return null;
        }
        Mapping map = getMapping(dt);
        if (map == null) {
            return null;
        }
        String jDatatype = map.getJavaType();
        return jDatatype;
    }
    
    public static String getJavaType(String datatype){
        return dm.getJType(datatype);
    }

    Mapping getMapping(String dt) {
        Mapping map = ht.get(dt);
        if (map == null) {
            // create a new literal space (i.e. dt) value for this unknown datatype
            put(dt, jTypeUndef, dt); // CoreseUndefLiteral
            map = ht.get(dt);
        }
        return map;
    }

    /**
     * Defines the datatype map between XSD datatypes and the java class that
     * implements the datatype and the marker set that contain the marker. We
     * separate number value spaces by giving different marker set to integer
     * and float
     */
    public void init() {

        //define the hashtable that 1 MS for (LITERAL,lang)
        Hashtable<String, String> htlang = new Hashtable<String, String>();
        htlang.put(DEFAULT, RDFSLITERAL);
        put(RDFSLITERAL, jTypeLiteral, htlang);
        put(rdflangString, jTypeLiteral, htlang);

        put(XMLLITERAL, jTypeXMLString, XMLLITERAL);
        put(xsdstring, jTypeString, xsdstring);
        put(xsdboolean, jTypeBoolean, xsdboolean);
        put(xsdanyURI, jTypeURI, xsdstring);

        put(xsdnormalizedString, jTypeString, xsdstring);
        put(xsdtoken, jTypeString, xsdstring);
        put(xsdnmtoken, jTypeString, xsdstring);
        put(xsdname, jTypeString, xsdstring);
        put(xsdncname, jTypeString, xsdstring);
        put(xsdlanguage, jTypeString, xsdstring);

        String intSpace = xsdinteger;
        // Integer + store datatype URI ?
        String intJType = jTypeInteger;

        put(xsddouble, jTypeDouble, xsddouble);
        put(xsdfloat, jTypeFloat, xsdfloat);
        put(xsddecimal, jTypeDecimal, xsddecimal);
        put(xsdinteger, jTypeInteger, intSpace);
        put(xsdlong, jTypeLong, intSpace);

        put(xsdshort, intJType, intSpace);
        put(xsdint, jTypeInt, intSpace);
        put(xsdbyte, intJType, intSpace);
        put(xsdnonNegativeInteger, intJType, intSpace);
        put(xsdnonPositiveInteger, intJType, intSpace);
        put(xsdpositiveInteger, intJType, intSpace);
        put(xsdnegativeInteger, intJType, intSpace);
        put(xsdunsignedLong, intJType, intSpace);
        put(xsdunsignedInt, intJType, intSpace);
        put(xsdunsignedShort, intJType, intSpace);
        put(xsdunsignedByte, intJType, intSpace);

        put(xsddate, jTypeDate, xsddate);
        put(xsddateTime, jTypeDateTime, xsddateTime);
        put(xsdday, jTypeDay, xsdday);
        put(xsdmonth, jTypeMonth, xsdmonth);
        put(xsdyear, jTypeYear, xsdyear);
        put(xsddaytimeduration, jTypeGeneric, xsddaytimeduration);

        //special use case: to get the implementation java type for Resource and Blank
        put(RDFSRESOURCE, jTypeURI, RDFSRESOURCE);


    }

    void define(String datatype, int code) {
    }

    void defineString(String datatype) {
    }

    void defineInteger(String datatype) {
    }

    int getType(String datatype) {
        return IDatatype.UNDEF;
    }

    public void init2() {

        define(RDFSLITERAL, IDatatype.LITERAL);
        define(rdflangString, IDatatype.LITERAL);
        define(XMLLITERAL, IDatatype.XMLLITERAL);
        define(xsdboolean, IDatatype.BOOLEAN);
        define(xsdanyURI, IDatatype.URI);
        define(xsdstring, IDatatype.STRING);
        define(RDFSRESOURCE, IDatatype.URI);

        defineString(xsdnormalizedString);
        defineString(xsdtoken);
        defineString(xsdnmtoken);
        defineString(xsdname);
        defineString(xsdncname);
        defineString(xsdlanguage);

        define(xsddouble, IDatatype.DOUBLE);
        define(xsdfloat, IDatatype.FLOAT);
        define(xsddecimal, IDatatype.DECIMAL);
        define(xsdinteger, IDatatype.INTEGER);
        define(xsdlong, IDatatype.LONG);

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

        define(xsddate, IDatatype.DATE); //jTypeDate,		xsddate);
        define(xsddateTime, IDatatype.DATETIME); //jTypeDateTime,	xsddateTime);

        define(xsdday, IDatatype.DAY); //jTypeDay,	xsdday);
        define(xsdmonth, IDatatype.MONTH); //jTypeMonth,	xsdmonth);
        define(xsdyear, IDatatype.YEAR); //jTypeYear,	xsdyear);

        define(xsddaytimeduration, IDatatype.DURATION); // CoreseGeneric

    }

    static boolean isNumber(String name) {
        return name.equals(xsdlong) || name.equals(xsdinteger) || name.equals(xsdint)
                || name.equals(xsddouble)
                || name.equals(xsdfloat) || name.equals(xsddecimal);
    }

    /**
     * URI of a literal datatype xsd: rdf:XMLLiteral rdf:PlainLiteral
     */
    public static boolean isDatatype(String range) {
        return range.startsWith(XSD)
                || range.equals(XMLLITERAL)
                || range.equals(rdflangString);
    }

    public static boolean isUndefined(IDatatype dt) {
        return dt.getCode() == IDatatype.UNDEF;
    }

    IDatatype create(String label, String datatype, String lang) {
        switch (getType(datatype)) {
            case IDatatype.STRING:
                return new CoreseString(label);
        }

        return null;
    }

    public static IDatatype cast(Object obj) {
        if (obj instanceof Integer) {
            return newInstance((Integer) obj);
        } 
        else if (obj instanceof Boolean) {
            return newInstance((Boolean) obj);
        } 
        else if (obj instanceof String) {
            return newInstance((String) obj);
        }
        else if (obj instanceof Float) {
            return newInstance((Float) obj);
        }
        else if (obj instanceof Double) {
            return newInstance((Double) obj);
        }
        return null;
    }
    
    public static IDatatype newInstance(String label, String datatype) {
        return createLiteral(label, datatype, null);
    }
    
    public static IDatatype newInstance(String label, String datatype, String lang) {
        return createLiteral(label, datatype, lang);
    }

    public static IDatatype newInstance(double result) {
        return new CoreseDouble(result);
    }

    public static IDatatype newInstance(double result, String datatype) {
        if (datatype.equals(xsdinteger)) {
            return newInstance((int) result);
        } else if (datatype.equals(xsdlong)) {
            return newInstance((long) result);
        } else if (datatype.equals(xsddecimal)) {
            return new CoreseDecimal(result);
        } else if (datatype.equals(xsdfloat)) {
            return new CoreseFloat(result);
        }
        return new CoreseDouble(result);
    }

    public static IDatatype newInstance(float result) {
        return new CoreseFloat(result);
    }

    public static IDatatype newInstance(int result) {
        return getValue(result);
    }

    static IDatatype getValue(int value) {
        if (value >= 0 && value < INTMAX) {
            return getValueCache(value);
        }
        return new CoreseInteger(value);
    }

    static IDatatype getValueCache(int value) {
        if (intCache == null){
            intCache = new IDatatype[INTMAX];
        }
        if (intCache[value] == null) {
            intCache[value] = new CoreseInteger(value);
        }
        return intCache[value];
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
        if (result) {
            return CoreseBoolean.TRUE;
        }
        return CoreseBoolean.FALSE;
    }
    
    public static IDatatype createInstance(boolean result) {
        if (result) {
            return new CoreseBoolean(true);
        }
        return new CoreseBoolean(false);
    }

    public static IDatatype newResource(String result) {
        return new CoreseURI(result);
    }

    public static IDatatype newResource(String ns, String name) {
        return newResource(ns + name);
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
     * Create a datatype. If it is a not well formed number, create a
     * CoreseUndef
     */
    public static IDatatype createLiteral(String label, String datatype) {
        return createLiteral(label, datatype, null);
    }

  public static IDatatype createLiteral(String label, String datatype, String lang) {
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

    public static IDatatype createLiteralWE(String label, String datatype, String lang)
            throws CoreseDatatypeException {
        if (datatype == null) {
            datatype = datatypeURI(lang);
        }
        String JavaType = dm.getJType(datatype);
        IDatatype dt = CoreseDatatype.create(JavaType, datatype, label, lang);
        return dt;
    }

    public static IDatatype createLiteral(String label) {
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
        if (literalAsString) {
            datatype = xsdstring;
        }
        String JavaType = dm.getJType(datatype);
        IDatatype dt = CoreseDatatype.create(JavaType, datatype, label, "");
        return dt;
    }

    public static IDatatype createObject(String name) {
        return createLiteral(name, XMLLITERAL, null);
    }
    
    public static IDatatype createObject(Object obj) {
        if (obj == null){
            return null;
        }
        return createObject(Integer.toString(obj.hashCode()), obj);
    }

    public static IDatatype createObject(String name, Object obj) {      
        if (obj == null){
            return null;
        }
        if (obj instanceof Pointerable){
            return new CoresePointer(name, (Pointerable) obj);
        }
        IDatatype dt = createLiteral(name, XMLLITERAL, null);
        dt.setObject(obj);
        return dt;
    }

    public static IDatatype createObject(String name, Object obj, String datatype) {
        IDatatype dt = createUndef(name, datatype);
        dt.setObject(obj);
        return dt;
    }

    public static IDatatype createUndef(String label, String datatype) {
        IDatatype dt = new CoreseUndefLiteral(label);
        dt.setDatatype(datatype);
        return dt;
    }

    public static IDatatype createList(IDatatype... ldt) {
        return new CoreseList(ldt);
    }
    
     public static IDatatype newList(IDatatype... ldt) {
        return new CoreseList(ldt);
    }
    
     public static IDatatype createList() {
       return createList(new ArrayList<IDatatype>(0));
    }

    public static IDatatype createList(List<IDatatype> ldt) {
        IDatatype dt = CoreseList.create(ldt);
        return dt;
    }
    
    public static IDatatype createList(IDatatype dt) {
        ArrayList<IDatatype> ldt = new ArrayList<IDatatype>();
        ldt.add(dt);
        return  CoreseList.create(ldt);
    }

     public static IDatatype createList(Collection<IDatatype> ldt) {
        IDatatype dt = CoreseList.create(ldt);
        return dt;
    }
    
    /**
     * obj is an Expr to be evaluated later such as concat(str, st:number(),
     * str) use case: template with st:number()
     */
    public static IDatatype createFuture(Object obj) {
        CoreseUndefLiteral dt = new CoreseUndefLiteral();
        dt.setDatatype(xsdstring);
        dt.setObject(obj);
        dt.setFuture(true);
        return dt;
    }

    /**
     * Order of Datatypes & rdfs:Literal vs xsd:string
     */
    public static void setSPARQLCompliant(boolean b) {
        CoreseDatatype.SPARQLCompliant = b;
        literalAsString = !b;
    }

    public static void setLiteralAsString(boolean b) {
        literalAsString = b;
    }

    public static boolean isLiteralAsString() {
        return literalAsString;
    }

    static String datatypeURI(String lang) {
        if (literalAsString && (lang == null || lang == "")) {
            return xsdstring;
        } else {
            return RDFSLITERAL;
        }
    }

    public static String datatype(String lang) {
        if (literalAsString && (lang == null || lang == "")) {
            return qxsdString;
        } else {
            return qrdfsLiteral;
        }
    }

    public static IDatatype createResource(String label) {
        return new CoreseURI(label);
    }

    public static IDatatype createSkolem(String label) {
        return new CoreseURI(label);
    }

    public static IDatatype createBlank(String label) {
        return new CoreseBlankNode(label);
    }

    public static IDatatype createBlank() {
        return new CoreseBlankNode(BLANK + COUNT++);
    }

//	public  String undefType(){
//		return jTypeUndef;
//	}
    /**
     * *****************************
     *
     * Accessors
     *
     */
    public static boolean isStringLiteral(IDatatype dt) {
        return (dt instanceof CoreseString) || (dt instanceof CoreseLiteral);
    }

    // literal with or without lang
    public static boolean isLiteral(IDatatype dt) {
        return (dt instanceof CoreseLiteral);
    }

    public static boolean isString(IDatatype dt) {
        return (dt instanceof CoreseString);
    }

    // literal without lang
    public static boolean isSimpleLiteral(IDatatype dt) {
        return (dt instanceof CoreseLiteral) && !dt.hasLang();
    }

    public static boolean isInteger(IDatatype dt) {
        return dt.getCode() == IDatatype.INTEGER;
    }

    public static boolean isLong(IDatatype dt) {
        return dt.getCode() == IDatatype.LONG;
    }

    public static boolean isFloat(IDatatype dt) {
        return dt.getCode() == IDatatype.FLOAT;
    }

    public static boolean isDouble(IDatatype dt) {
        return dt.getCode() == IDatatype.DOUBLE;
    }

    public static boolean isDecimal(IDatatype dt) {
        return dt.getCode() == IDatatype.DECIMAL;
    }

    public static boolean isBindable(IDatatype dt) {
        return ((CoreseDatatype) dt).isBindable();
    }

    public static IDatatype getTZ(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getTZ();
    }

    public static IDatatype getTimezone(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getTimezone();
    }

    public static IDatatype getYear(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getYear();
    }

    public static IDatatype getMonth(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getMonth();
    }

    public static IDatatype getDay(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getDay();
    }

    public static IDatatype getHour(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getHour();
    }

    public static IDatatype getMinute(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getMinute();
    }

    public static IDatatype getSecond(IDatatype dt) {
        if (!(dt instanceof CoreseDate)) {
            return null;
        }
        CoreseDate date = (CoreseDate) dt;
        return date.getSecond();
    }

    // for literal only
    public static boolean check(IDatatype dt, String range) {
        if (dt.isNumber()) {
            if (!isNumber(range)) {
                return false;
            }
        } else if (isNumber(range)) {
            return false;
        }
        return dt.getDatatypeURI().equals(range);
    }

    public static boolean persistentable(IDatatype dt) {

        return (dt instanceof CoreseStringLiteral
                || dt instanceof CoreseLiteral
                || dt instanceof CoreseUndefLiteral
                || dt instanceof CoreseXMLLiteral
                || dt instanceof CoreseString);
    }
    
    /******************************/
    
    // DRAFT
    public static IDatatype result(IDatatype dt){
        switch (dt.getCode()){
            // return a copy to prevent side effects with cached IDatatype
            // use case: parallel threads
            case IDatatype.INTEGER : 
                if (dt.intValue() < INTMAX){
                    dt = newInstance(dt.intValue());
                }
                break;
                
            case IDatatype.BOOLEAN:
                dt = newInstance(dt.booleanValue());
                break;
        }
        dt.setIndex(IDatatype.RESULT);
        return dt;
    }
    
    public static boolean isResult(IDatatype dt){
        return dt.getIndex() == IDatatype.RESULT;
    }
    
    public static IDatatype getResultValue(IDatatype dt){
        dt.setIndex(IDatatype.VALUE);
        return dt;
    }
    
    public static boolean isBound(IDatatype dt){
        return dt != UNBOUND;
    }
    
     public static IDatatype size(IDatatype dt){
        if (! dt.isList()){
              return null;
          }        
         return newInstance(dt.size());
     }
         
      public static IDatatype first(IDatatype dt){
          if (! dt.isList() || dt.getValues().isEmpty()){
              return null;
          }                 
         return dt.getValues().get(0);
     }
      
      public static IDatatype rest(IDatatype dt) {
        if (!dt.isList()) {
            return null;
        }
        List<IDatatype> val = dt.getValues();
        ArrayList<IDatatype> res = new ArrayList(val.size() - 1);
        for (int i = 1; i < val.size(); i++) {
            res.add(val.get(i));
        }
        return createList(res);
    }
      
      // modify
     public static IDatatype add(IDatatype elem, IDatatype list){
          if (! list.isList()){
              return null;
          }          
          list.getValues().add(elem);
          return list;
      }
     
     // copy
     public static IDatatype cons(IDatatype elem, IDatatype list){
          if (! list.isList()){
              return null;
          }
          List<IDatatype> val = list.getValues();
          ArrayList<IDatatype> res = new ArrayList(val.size()+1);
          res.add(elem);
          res.addAll(val);        
          return createList(res);
      }
      
      public static IDatatype append(IDatatype dt1, IDatatype dt2){
          if (! dt1.isList() || ! dt2.isList()){
              return null;
          }
          List<IDatatype> a1 = dt1.getValues();
          List<IDatatype> a2 = dt2.getValues();
          ArrayList<IDatatype> res = new ArrayList(a1.size() + a2.size());
          res.addAll(a1);
          res.addAll(a2);
          return createList(res);
      }
      
      // remove duplicates
       public static IDatatype merge(IDatatype dt1, IDatatype dt2){
          if (! dt1.isList() || ! dt2.isList()){
              return null;
          }
          ArrayList<IDatatype> res = new ArrayList();
          for (IDatatype dt : dt1.getValues()){
              if (! res.contains(dt)){
                  res.add(dt);
              }
          }
          for (IDatatype dt : dt2.getValues()){
              if (! res.contains(dt)){
                  res.add(dt);
              }
          }
          return createList(res);
      }

      
      public static IDatatype get(IDatatype list, IDatatype n){
          if (! list.isList()){
              return null;
          }
          List<IDatatype> arr = list.getValues();
          if (n.intValue() >= arr.size()){
              return null;
          }
          return arr.get(n.intValue());
      }
      
     public static IDatatype set(IDatatype list, IDatatype n, IDatatype val) {
         if (! list.isList()){
              return null;
          }
          List<IDatatype> arr = list.getValues();
          if (n.intValue() >= arr.size()){
              return null;
          }
          arr.set(n.intValue(), val);
         return val;
     }

      
     public static IDatatype list(IDatatype[] args){ 
        ArrayList<IDatatype> val = new ArrayList<IDatatype>(args.length);
        val.addAll(Arrays.asList(args));
        return  createList(val);
    }
    
    public static IDatatype reverse(IDatatype dt){
        if ( ! dt.isList()){
            return dt;
        }
        List<IDatatype> value = dt.getValues();
        ArrayList<IDatatype> res   = new ArrayList<IDatatype>(value.size());
        int n = value.size() - 1;
        for (int i = 0; i<value.size(); i++){
            res.add(value.get(n - i));
        }        
        return createList(res);
    }
    
    // modify list
     public static IDatatype sort(IDatatype dt){
        if ( ! dt.isList()){
            return dt;
        }
        List<IDatatype> value = dt.getValues();
        Collections.sort(value);
        return dt;
        
     }
     
     public static IDatatype member(IDatatype elem, IDatatype list){
         if (! list.isList()){
             return null;
         }
         return list.getValues().contains(elem) ? TRUE : FALSE;
     }
}