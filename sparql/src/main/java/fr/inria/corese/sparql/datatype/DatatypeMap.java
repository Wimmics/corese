package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.datatype.extension.CoreseIterate;
import fr.inria.corese.sparql.datatype.extension.CoreseUndefFuture;
import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.sparql.datatype.extension.CoreseMap;
import fr.inria.corese.sparql.datatype.extension.CoreseList;
import fr.inria.corese.sparql.datatype.extension.CoreseJSON;
import fr.inria.corese.sparql.datatype.extension.CoreseXML;
import fr.inria.corese.sparql.datatype.extension.CoresePointer;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 * <br>
 * This class is used to map a datatype name to its java type representation
 * ands its marker set.
 * <br>
 *
 * @author Olivier Corby, Olivier Savoie
 */
public class DatatypeMap implements Cst, RDF, DatatypeValueFactory {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(DatatypeMap.class);
    public static final IDatatype ZERO = newInstance(0);
    public static final IDatatype ONE = newInstance(1);
    public static final IDatatype TWO = newInstance(2);
    public static final IDatatype THREE = newInstance(3);
    public static final IDatatype FOUR = newInstance(4);
    public static final IDatatype FIVE = newInstance(5);
    public static final IDatatype SIX = newInstance(6);
    public static final IDatatype SEVEN = newInstance(7);
    public static final IDatatype EIGHT = newInstance(8);
    public static final IDatatype NINE = newInstance(9);

    public static final IDatatype MINUSONE = newInstance(-1);
    public static final IDatatype ERROR = CoreseUndefLiteral.ERROR;
    public static final IDatatype UNBOUND = CoreseUndefLiteral.UNBOUND;

    public static final IDatatype URI_DATATYPE = newResource(IDatatype.URI_DATATYPE);
    public static final IDatatype BNODE_DATATYPE = newResource(IDatatype.BNODE_DATATYPE);
    public static final IDatatype LITERAL_DATATYPE = newResource(IDatatype.LITERAL_DATATYPE);
    static final String alpha = "abcdefghijklmnoprstuvwxyz";
    static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String POINTER = "Pointer_";
    static long nbObject = 0;

    private static Hashtable<String, Mapping> ht;
    private static HashMap<String, Integer> dtCode;
    static DatatypeMap dm;
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
    public static final IDatatype EMPTY_LIST = createList();
    public static final IDatatype EMPTY_STRING = newInstance("");
    static final String LIST = ExpType.EXT + "List";
    private static final int INTMAX = 100;
    static IDatatype[] intCache;
    // if true, restrict datatype match to conform to SPARQL test cases
    public static boolean SPARQLCompliant = false;

    static {
        intCache = new IDatatype[INTMAX];
        dm = DatatypeMap.create();
    }

    private class Mapping {

        private String javatype = "";

        public Mapping(String jtype) {
            javatype = jtype;
        }

        String getJavaType() {
            return javatype;
        }
    }

    public DatatypeMap() {
        if (ht == null) {
            ht = new Hashtable<String, Mapping>();
            dtCode = new HashMap<String, Integer>();
        }

        init();
        init2();
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

    public static String getJavaType(String datatype) {
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
        put(xsdstring,  jTypeString, xsdstring);
        put(xsdboolean, jTypeBoolean, xsdboolean);
        put(xsdanyURI,  jTypeURILiteral, xsdanyURI);
        //put(xsdanyURI, jTypeURI, xsdstring);

        put(xsdnormalizedString, jTypeString, xsdstring);
        put(xsdtoken, jTypeString, xsdstring);
        put(xsdnmtoken, jTypeString, xsdstring);
        put(xsdname, jTypeString, xsdstring);
        put(xsdncname, jTypeString, xsdstring);
        put(xsdlanguage, jTypeString, xsdstring);

        String intSpace = xsdinteger;
        // Integer + store datatype URI ?
        String intJType = jTypeInteger;
        String genericIntJType = jTypeGenericInteger;

        put(xsddouble, jTypeDouble, xsddouble);
        put(xsdfloat, jTypeFloat, xsdfloat);
        put(xsddecimal, jTypeDecimal, xsddecimal);
        put(xsdinteger, jTypeInteger, intSpace);

        //put(xsdlong,    jTypeLong, intSpace);
        put(xsdlong, genericIntJType, intSpace);
        put(xsdshort, genericIntJType, intSpace);
        put(xsdint, genericIntJType, intSpace);
        put(xsdbyte, genericIntJType, intSpace);
        put(xsdnonNegativeInteger, genericIntJType, intSpace);
        put(xsdnonPositiveInteger, genericIntJType, intSpace);
        put(xsdpositiveInteger, genericIntJType, intSpace);
        put(xsdnegativeInteger, genericIntJType, intSpace);
        put(xsdunsignedLong, genericIntJType, intSpace);
        put(xsdunsignedInt, genericIntJType, intSpace);
        put(xsdunsignedShort, genericIntJType, intSpace);
        put(xsdunsignedByte, genericIntJType, intSpace);

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
        dtCode.put(datatype, code);
    }

    void defineString(String datatype) {
        dtCode.put(datatype, IDatatype.STRING);
    }

    void defineInteger(String datatype) {
        dtCode.put(datatype, IDatatype.GENERIC_INTEGER);
    }

    int getType(String datatype) {
        return IDatatype.UNDEF;
    }

    static Integer getCode(String datatype) {
        Integer i = dtCode.get(datatype);
        if (i == null) {
            return IDatatype.UNDEFINED;
        }
        return i;
    }

    public void init2() {

        define(RDFSLITERAL, IDatatype.LITERAL);
        define(rdflangString, IDatatype.LITERAL);
        define(XMLLITERAL, IDatatype.XMLLITERAL);
        define(xsdboolean, IDatatype.BOOLEAN);
        define(xsdanyURI, IDatatype.URI_LITERAL);
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

        defineInteger(xsdlong);
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

        define(xsddate, IDatatype.DATE);
        define(xsddateTime, IDatatype.DATETIME);

        define(xsdday, IDatatype.DAY);
        define(xsdmonth, IDatatype.MONTH);
        define(xsdyear, IDatatype.YEAR);

        define(xsddaytimeduration, IDatatype.DURATION);

    }

    static boolean isNumber(String name) {
        switch (getCode(name)) {
            case IDatatype.INTEGER:
            case IDatatype.DOUBLE:
            case IDatatype.FLOAT:
            case IDatatype.DECIMAL:
            case IDatatype.GENERIC_INTEGER:
                return true;
            default:
                return false;
        }
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
    
    public static IDatatype cast(NodeList list) {
        return CoreseXML.cast(list);
    }

    public static IDatatype cast(Object obj) {
        if (obj instanceof Number) {
            if (obj instanceof Integer) {
                return newInstance((Integer) obj);
            } else if (obj instanceof Float) {
                return newInstance((Float) obj);
            } else if (obj instanceof Double) {
                return newInstance((Double) obj);
            }  else if (obj instanceof Short) {
                return newInstance(Integer.valueOf((Short)obj));
            } 
            else if (obj instanceof Byte) {
                return newInstance(Integer.valueOf((Byte)obj));
            } 
        } else if (obj instanceof Boolean) {
            return newInstance((Boolean) obj);
        } else if (obj instanceof String) {
            return newInstance((String) obj);
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
        switch (getCode(datatype)) {
            case IDatatype.INTEGER:
                return newInstance((int) result);
            case IDatatype.FLOAT:
                return new CoreseFloat(result);
            case IDatatype.DECIMAL:
                return new CoreseDecimal(result);
            case IDatatype.GENERIC_INTEGER:
                return new CoreseGenericInteger((int) result, datatype);
            default:
                return new CoreseDouble(result);
        }
    }

    public static IDatatype newInstance(float result) {
        return new CoreseFloat(result);
    }

    public static IDatatype newInstance(int result) {
        return getValue(result);
    }
    
    public static IDatatype create(int result) {
        return new CoreseInteger(result);
    }

    public static IDatatype newInstance(long result) {
        return getValue(result);
    }

    /**
     * Use case: LDScript Java compiler
     */
    public static IDatatype newLong(long result) {
        return new CoreseGenericInteger(result);
    }

    public static IDatatype newInteger(int result) {
        return getValue(result);
    }

    public static IDatatype newInteger(long result) {
        return getValue(result);
    }

    public static IDatatype newDouble(double result) {
        return newInstance(result);
    }

    public static IDatatype newFloat(float result) {
        return newInstance(result);
    }

    public static IDatatype newFloat(double result) {
        return new CoreseFloat((float) result);
    }

    public static IDatatype newDecimal(double result) {
        return new CoreseDecimal(result);
    }

    static IDatatype getValue(long value) {
        if (value >= 0 && value < INTMAX) {
            return getValueCache((int) value);
        }
        return new CoreseInteger(value);
    }

    static IDatatype getValue(int value) {
        if (value >= 0 && value < INTMAX) {
            return getValueCache(value);
        }
        return new CoreseInteger(value);
    }

    static IDatatype getValueCache(int value) {
        if (intCache == null) {
            intCache = new IDatatype[INTMAX];
        }
        if (intCache[value] == null) {
            intCache[value] = new CoreseInteger(value);
        }
        return intCache[value];
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

    public static IDatatype newResource(String result) {
        return new CoreseURI(result);
    }
    
    public static IDatatype newResourceOrLiteral(String result) {
        if (result.startsWith("http://")) {
            return newResource(result);
        }
        return new CoreseString(result);
    }

    public static IDatatype newResource(String ns, String name) {
        return newResource(ns + name);
    }
    
    public static IDatatype uri(String ns, String name) {
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

    public static IDatatype newDateTime(String date) {
        try {
            return new CoreseDateTime(date);
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
    
    public static IDatatype newXMLLiteral(String label, org.w3c.dom.Node node) {
        IDatatype dt = new CoreseXMLLiteral(label);
        dt.setObject(node);
        return dt;
    }
    
    public static IDatatype newXMLObject(String label, org.w3c.dom.Node node) {
        //return newXMLLiteral(label, node);
        return xml(label, node);
    }
    
    public static IDatatype newLiteral(String label) {
        if (literalAsString) {
            return newInstance(label);
        } else {
            return new CoreseLiteral(label);
        }
    }

//    public static IDatatype createObject(String name) {
//        return createLiteral(name, XMLLITERAL, null);
//    }

    public static IDatatype getValue(Object value) {
        if (value instanceof IDatatype) {
            return (IDatatype) value;
        }
        if (value instanceof Node) {
            return (IDatatype) ((Node) value).getDatatypeValue();
        }
        if (value instanceof List) {
            return getValue((List) value);
        }
        IDatatype dt = DatatypeMap.castObject(value);
        return dt;
    }

    // not for recursively nested same list
    public static IDatatype getValue(List<Object> list) {
        ArrayList<IDatatype> l = new ArrayList<>();
        IDatatype res = createList(l);
        for (Object obj : list) {
            if (obj == list) {
                l.add(res);
            } else {
                IDatatype dt = getValue(obj);
                if (dt != null) {
                    l.add(dt);
                }
            }
        }
        return res;
    }

    public static IDatatype castObject(Object obj) {
        if (obj == null) {
            return null;
        }
        IDatatype dt = cast(obj);
        if (dt != null) {
            return dt;
        }
        return createObject(obj);
    }
    
    public static IDatatype createObject(Object obj) {
        return createObject(null, obj);
    }
      
    static String defaultName(Object obj) {
        return Long.toString(obj.hashCode());
    }
    
    static String defaultName(Pointerable obj) {
        //return obj.getDatatypeLabel();      
        //return Long.toString(obj.hashCode());
        return Long.toString(obj.getDatatypeLabel().hashCode());
    }
    
    public static IDatatype createObject(String name, Object obj) {
       if (obj == null) {
            return null;
        }
        if (obj instanceof Node) {
            return (IDatatype) ((Node) obj).getDatatypeValue();
        }      
        return createObjectBasic(name, obj);
    }
    
    public static IDatatype createObjectBasic(String name, Object obj) { 
        if (obj instanceof Pointerable) {
            Pointerable ptr = (Pointerable) obj;            
            return new CoresePointer(name==null?defaultName(ptr):name, ptr);
        }       
        return genericPointer(name, obj);
    }
    
//    @Deprecated
//    static IDatatype genericPointer1(String name, Object obj){
//        IDatatype dt = createLiteral(name==null?defaultName(obj):name, XMLLITERAL, null);
//        dt.setObject(obj);
//        return dt;
//    }
    
    public static IDatatype genericPointer(Object obj){
        return genericPointer(null, obj);
    }
    
    public static IDatatype genericPointer(String name, Object obj){
        return new CoresePointer(name==null?defaultName(obj):name, new PointerObject(obj));
    }
    
    public static IDatatype createPointer(String name) {
        return new CoresePointer(name, null);
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
    
    void test() {}
    
    public static CoreseMap map() {
        return new CoreseMap();
    }
    
    public static CoreseJSON json(JSONObject obj) {
        return new CoreseJSON(obj);
    }
    
    public static CoreseJSON json(String json) {
        try {
            return new CoreseJSON(json);
        }
        catch(JSONException ex) {
            logger.error(ex.getMessage());
            logger.error(json.substring(0, 100).concat("..."));
            return json();
        }
    }
    
      // param = [slot value slot value]
    public static CoreseJSON json(IDatatype... param) {
        CoreseJSON json = new CoreseJSON(new JSONObject());
        for (int i = 0; i < param.length; i++) {
            json.set(param[i++], param[i]);
        }
        return json;
    }
    
    public static CoreseJSON json() {
        return new CoreseJSON(new JSONObject());
    }
    
    public static CoreseXML xml(org.w3c.dom.Node node) {
        return new CoreseXML(node);
    }
    
    public static CoreseXML xml(String str, org.w3c.dom.Node node) {
        return new CoreseXML(str, node);
    }

    public static IDatatype createList(IDatatype... ldt) {
        return new CoreseList(ldt);
    }
        
    public static IDatatype newList(Enumeration en) {
        IDatatype list = DatatypeMap.list();
        while (en.hasMoreElements()) {
            Object name = en.nextElement();
            list.getList().add(DatatypeMap.castObject(name));
        }
        return list;
    } 
    
    public static CoreseList newList(IDatatype... ldt) {
        return new CoreseList(ldt);
    }
    
   public static IDatatype newList(Object... ldt) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Object obj :  ldt) {
            list.add(getValue(obj));
        }
        return newList(list);
    }
   
   public static List<String> toStringList(IDatatype dt) {
       ArrayList<String> list = new ArrayList<>();
       if (dt.isList()) {
           for (IDatatype val : dt) {
               list.add(val.getLabel());
           }
       }
       else {
           list.add(dt.getLabel());
       }
       return list;
   }
   
   public static IDatatype newStringList(List<String> alist) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (String str : alist) {
            list.add(newInstance(str));
        }
        return newList(list);
    }
    
    public static IDatatype newList(List<IDatatype> l) {
        return new CoreseList(l);
    }
    
    public static IDatatype toList(List<Node> list) {
        ArrayList<IDatatype> l = new ArrayList<>();
        for (Node node : list) {
            l.add((IDatatype) node.getDatatypeValue());
        }
        return newList(l);
    }
          
    public static IDatatype[] toArray(IDatatype dt) {
        List<IDatatype> list = dt.getValueList();
        IDatatype[] args = new IDatatype[list.size()];
        return list.toArray(args);
    }

    public static IDatatype newIterate(int start, int end) {
        return newIterate(start, end, 1);
    }

    public static IDatatype newIterate(int start, int end, int step) {
        return new CoreseIterate(start, end, step);
    }

    public static IDatatype newInstance(IDatatype... ldt) {
        return new CoreseList(ldt);
    }

    public static IDatatype createList() {
        return createList(new ArrayList<>(0));
    }

    public static IDatatype createList(List<IDatatype> ldt) {
        IDatatype dt = CoreseList.create(ldt);
        return dt;
    }

    public static IDatatype newInstance(List<IDatatype> ldt) {
        IDatatype dt = CoreseList.create(ldt);
        return dt;
    }

    public static IDatatype createList(IDatatype dt) {
        ArrayList<IDatatype> ldt = new ArrayList<IDatatype>();
        ldt.add(dt);
        return CoreseList.create(ldt);
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
        CoreseUndefFuture dt = new CoreseUndefFuture();
        dt.setDatatype(xsdstring);
        dt.setObject(obj);
        return dt;
    }

    /**
     * Order of Datatypes & rdfs:Literal vs xsd:string
     */
    public static void setSPARQLCompliant(boolean b) {
        SPARQLCompliant = b;
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

    /**
     * *****************************
     *
     * Accessors
     *
     */
    public static boolean isStringLiteral(IDatatype dt) {
        return isString(dt) || isLiteral(dt);
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
        return isLiteral(dt) && !dt.hasLang();
    }

    public static boolean isInteger(IDatatype dt) {
        return dt.getCode() == IDatatype.INTEGER;
    }

    public static boolean isLong(IDatatype dt) {
        return dt.getCode() == IDatatype.INTEGER && dt.getDatatypeURI().equals(fr.inria.corese.sparql.datatype.XSD.xsdlong);
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
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getTZ();
    }

    static CoreseDate getDate(IDatatype dt) {
        return (CoreseDate) dt;
    }

    public static IDatatype getTimezone(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getTimezone();
    }

    public static IDatatype getYear(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getYear();
    }

    public static IDatatype getMonth(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getMonth();
    }

    public static IDatatype getDay(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getDay();
    }

    public static IDatatype getHour(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getHour();
    }

    public static IDatatype getMinute(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getMinute();
    }

    public static IDatatype getSecond(IDatatype dt) {
        if (!dt.isDate()) {
            return null;
        }
        return getDate(dt).getSecond();
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
    
    
    public static IDatatype kind(IDatatype dt) {
        if (dt.isLiteral()) {
            return dt.getDatatype();
        }
        if (dt.isURI()) {
            return URI_DATATYPE;
        }
        return BNODE_DATATYPE;
    }

    /**
     * ***************************
     */
    public static IDatatype encode_for_uri(IDatatype dt) {
        String str = encodeForUri(dt.getLabel());
        return newLiteral(str);
    }

    public static IDatatype strlen(IDatatype dt) {
        return newInstance(dt.getLabel().length());
    }

    static String encodeForUri(String str) {

        StringBuilder sb = new StringBuilder(2 * str.length());

        for (int i = 0; i < str.length(); i++) {

            char c = str.charAt(i);

            if (stdChar(c)) {
                sb.append(c);
            } else {
                try {
                    byte[] bytes = Character.toString(c).getBytes("UTF-8");

                    for (byte b : bytes) {
                        sb.append("%");

                        char cc = (char) (b & 0xFF);

                        String hexa = Integer.toHexString(cc).toUpperCase();

                        if (hexa.length() == 1) {
                            sb.append("0");
                        }

                        sb.append(hexa);
                    }

                } catch (UnsupportedEncodingException e) {
                }
            }
        }

        return sb.toString();
    }

    static boolean stdChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'
                || c == '-' || c == '.' || c == '_' || c == '~';
    }

    public static boolean isBound(IDatatype dt) {
        return dt != UNBOUND;
    }

    public static IDatatype size(IDatatype dt) {
        if (!dt.isList()) {
            return null;
        }
        return dt.length();
    }

    public static IDatatype first(IDatatype dt) {
        if (!dt.isList()) {
            return null;
        }
        return dt.getList().first();
    }

    public static IDatatype rest(IDatatype dt) {
        if (!dt.isList()) {
            return null;
        }
        return dt.getList().rest();
    }
    
    public static IDatatype rest(IDatatype dt, IDatatype index) {
        if (!dt.isList()) {
            return null;
        }
        return dt.getList().rest(index);
    }
    
    public static IDatatype rest(IDatatype dt, IDatatype index, IDatatype last) {
        if (!dt.isList()) {
            return null;
        }
        return dt.getList().rest(index, last);
    }

    // modify
    public static IDatatype add(IDatatype list, IDatatype elem) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().add(elem);
    }

    // modify
    public static IDatatype add(IDatatype list, IDatatype ind, IDatatype elem) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().add(ind, elem);
    }

    // modify
    public static IDatatype swap(IDatatype list, IDatatype i1, IDatatype i2) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().swap(i1, i2);
    }

    // copy
    public static IDatatype cons(IDatatype elem, IDatatype list) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().cons(elem);
    }

    public static IDatatype append(IDatatype dt1, IDatatype dt2) {
        if (!dt1.isList() || !dt2.isList()) {
            return null;
        }
        return dt1.getList().append(dt2);
    }

    // remove duplicates
    public static IDatatype merge(IDatatype dt1, IDatatype dt2) {
        if (!dt1.isList() || !dt2.isList()) {
            return null;
        }
        return dt1.getList().merge(dt2);
    }

    // dt is a list, possibly list of lists
    // merge lists and remove duplicates
    public static IDatatype merge(IDatatype list) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().merge();
    }

    public static IDatatype get(IDatatype list, IDatatype n) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().get(n);
    }
    
     public static IDatatype last(IDatatype list, IDatatype n) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().last(n);
    }

    public static IDatatype set(IDatatype list, IDatatype n, IDatatype val) {
        if (!list.isList()) {
            return null;
        }
        list.getList().set(n, val);
        return val;
    }
    
    public static IDatatype remove(IDatatype list, IDatatype elem) {
        if (list.isList()) {
            list.getList().remove(elem);
        }
        else if (list.isMap()){
            list.getMap().remove(elem);
        }
        return list;
    }
    
    public static IDatatype remove(IDatatype list, int n) {
        if (!list.isList()) {
            return null;
        }
        list.getList().remove(n);
        return list;
    }
    
    public static IDatatype listResource(List<String> args) {
        ArrayList<IDatatype>  list = new ArrayList<>();
        for (String uri : args) {
            list.add(newResource(uri));
        }
        return newList(list);
    }

    
    public static IDatatype list(IDatatype... args) {
        ArrayList<IDatatype> val = new ArrayList<>(args.length);
        val.addAll(Arrays.asList(args));
        return createList(val);
    }

    public static IDatatype reverse(IDatatype dt) {
        if (!dt.isList()) {
            return dt;
        }
        return dt.getList().reverse();
    }

    // modify list
    public static IDatatype sort(IDatatype dt) {
        if (!dt.isList()) {
            return dt;
        }
        return dt.getList().sort();
    }

    public static IDatatype member(IDatatype elem, IDatatype list) {
        if (!list.isList()) {
            return null;
        }
        return list.getList().member(elem);
    }
    
    public static CoreseXML getXML(IDatatype dt) {
        if (dt instanceof CoreseXML) {
            return (CoreseXML) dt;
        }
        return null;
    }

    public static IDatatype iota(IDatatype... args) {
        if (args.length == 0) {
            return null;
        }
        IDatatype dt = args[0];
        if (dt.isNumber()) {
            return iotaNumber(args);
        }
        return iotaString(args);
    }

    static IDatatype iotaNumber(IDatatype[] args) {
        int start = 1;
        int end = 1;

        if (args.length > 1) {
            start = args[0].intValue();
            end = args[1].intValue();
        } else {
            end = args[0].intValue();
        }
        if (end < start) {
            return DatatypeMap.createList();
        }

        int step = 1;
        if (args.length == 3) {
            step = args[2].intValue();
        }
        int length = (end - start + step) / step;
        ArrayList<IDatatype> ldt = new ArrayList<IDatatype>(length);

        for (int i = 0; i < length; i++) {
            ldt.add(DatatypeMap.newInstance(start));
            start += step;
        }
        IDatatype dt = DatatypeMap.createList(ldt);
        return dt;
    }

    static IDatatype iotaString(IDatatype[] args) {
        String fst = args[0].stringValue();
        String snd = args[1].stringValue();
        int step = 1;
        if (args.length == 3) {
            step = args[2].intValue();
        }
        String str = alpha;
        int start = str.indexOf(fst);
        int end = str.indexOf(snd);
        if (start == -1) {
            str = ALPHA;
            start = str.indexOf(fst);
            end = str.indexOf(snd);
        }
        if (start == -1 || end == -1) {
            return null;
        }

        int length = (end - start + step) / step;
        ArrayList<IDatatype> ldt = new ArrayList<IDatatype>(length);

        for (int i = 0; i < length; i++) {
            ldt.add(DatatypeMap.newInstance(str.substring(start, start + 1)));
            start += step;
        }
        IDatatype dt = DatatypeMap.createList(ldt);
        return dt;
    }
    
    public static IDatatype setPublicDatatypeValue(IDatatype dt) {
        TRUE.setPublicDatatypeValue(dt);
        return dt;
    }
    
    public static IDatatype getPublicDatatypeValue() {
        return TRUE.getPublicDatatypeValue();
    }
    

    // DatatypeValueFactory
    
    public static DatatypeMap getDatatypeMap() {
        return dm;
    }
    
    @Override
    public Node nodeList(List<Node> list) {
        return toList(list);
    }
    
    @Override
    public IDatatype nodeValue(int n) {
        return newInstance(n);
    }
    
    public static boolean isLiteralDatatype(IDatatype type) {
        String label = type.getLabel();
        return label.startsWith(NSManager.DT)
                || label.startsWith(NSManager.XSD)
                || label.equals(fr.inria.corese.sparql.datatype.RDF.XMLLITERAL)
                || label.equals(fr.inria.corese.sparql.datatype.RDF.LANGSTRING)
                || label.equals(fr.inria.corese.sparql.datatype.RDF.HTML);
    }
    
    public static IDatatype URIDomain(IDatatype dt) {
        String dom = NSManager.domain(dt.getLabel());
        if (dom == null) {
            return null;
        }
       return newResource(dom);
    }
    
    public static IDatatype split(IDatatype dt1, IDatatype dt2) {
        return split(dt1, dt2.getLabel());
    }
    
    public static IDatatype split(IDatatype dt1, String sep) {
        String[] split = dt1.stringValue().split(sep);
        return cast(split);
    }

    public static IDatatype cast(String[] arr) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (String str : arr) {
            list.add(newInstance(str));
        }
        return newList(list);
    }

}
