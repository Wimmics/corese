package fr.inria.corese.sparql.datatype;

import static fr.inria.corese.sparql.datatype.CoreseBoolean.FALSE;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.TRUE;
import static fr.inria.corese.sparql.datatype.Cst.jTypeGenericInteger;
import static fr.inria.corese.sparql.datatype.Cst.jTypeInteger;
import static fr.inria.corese.sparql.datatype.RDF.RDF_HTML;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.IDatatypeList;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * The top level class of all the Corese datatypes
 * <br>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class CoreseDatatype
        implements IDatatype {
    public static final String NL = System.getProperty("line.separator");
    public static boolean DISPLAY_AS_PREFIX = true;
    public static Logger logger = LoggerFactory.getLogger(CoreseDatatype.class);
    static final CoreseURI datatype = new CoreseURI(RDF.RDFSRESOURCE);
    static final CoreseString empty = new CoreseString("");
    static final CoreseDatatypeException failure = new CoreseDatatypeException("Datatype Exception, statically created");
    // lang tag string -> IDatatype value
    static final Hashtable<String, IDatatype> lang2dataLang = new Hashtable<String, IDatatype>();
    // datatype string -> IDatatype value
    static final Hashtable<String, IDatatype> hdt = new Hashtable<String, IDatatype>(); 
    // DatatypeMap singleton
    static final DatatypeMap dm = DatatypeMap.create();
    static final int LESSER = -1, GREATER = 1;
    static int code = -1;
    
    static HashMap<Integer, Class> javaClassMap;
    @Deprecated
    private static IDatatype publicDatatypeValue;
    
    // Node getIndex()
    private int index = IDatatype.VALUE;
    // Node getTripleStore()
    private TripleStore graph;
    
    static { 
        init();
    }
    
    static void init() {
        javaClassMap = new HashMap<>();
        define(IDatatype.INTEGER,   int.class);
        define(IDatatype.DOUBLE,    double.class);
        define(IDatatype.DECIMAL,   double.class);
        define(IDatatype.FLOAT,     double.class);
        define(IDatatype.BOOLEAN,   boolean.class);
        define(IDatatype.STRING,    String.class);
        define(IDatatype.LITERAL,   String.class);
        define(IDatatype.URI,       String.class);
    }
    
    public static NSManager nsm() {
        return NSManager.nsm();
    }
    
    static void define(int i, Class c) {
        javaClassMap.put(i, c);
    }
    
    @Override
    public Class getJavaClass() {
        if (getNodeObject() != null) {
            return getNodeObject().getClass();
        }
        return javaClassMap.get(getCode());
    }

    /**
     * Default lang is "" for literals, But for URI which is null (see
     * CoreseURI)
     */
    @Override
    public IDatatype getDataLang() {
        return empty;
    }
    
    // for pointer datatype value
    @Override
    public String getContent(){
        if (getNodeObject() == null){
            return "";
        }
        return getNodeObject().toString();
    }
    
    /**
     * Same as toString() except for Pointer that may display the content
     * Use case: xt:display()
     */
    @Override
    public IDatatype display() {
        return this;
    }

    @Override
    public String toString() {
        return toSparql(true, false);
    }

    @Override
    public String toSparql() {
        return toSparql(true, false);
    }

    @Override
    public String toSparql(boolean prefix) {
        return toSparql(prefix, false);
    }

    @Override
    public String toSparql(boolean prefix, boolean xsd) {
        return toSparql(prefix, xsd, nsm());
    }
    
    /**
     * Overloaded by CoreseExtension
     */
    @Override
    public String toSparql(boolean prefix, boolean xsd, NSManager nsm) {
        String value = getLabel();        
        if (isPointer() && getPointerObject() != null){
            value = getPointerObject().getDatatypeLabel();
        }
        if (getCode() == INTEGER && !xsd && getDatatypeURI().equals(XSD.xsdinteger)
                && (! (value.startsWith("0") && value.length() > 1))) {
            // display integer value as is (without datatype)
        } 
        else if (getCode() == BOOLEAN && !xsd && 
                (getLabel().equals(CoreseBoolean.STRUE) || getLabel().equals(CoreseBoolean.SFALSE))) {
        } 
        else if (getCode() == STRING || (getCode() == LITERAL && !hasLang())) {
            value = protect(value);
        } else if (getDatatype() != null && !getDatatype().getLabel().equals(RDFS.rdflangString)) {
            String datatype = getDatatype().getLabel();

            if (prefix && (datatype.startsWith(RDF.XSD))
                    || datatype.startsWith(RDF.RDF)
                    || datatype.startsWith(NSManager.DT)) {
                datatype = nsm.toPrefix(datatype);
            } else {
                datatype = String.format("<%s>", datatype);
            }
            value = String.format("%s^^%s", protect(value), datatype);
        } else if (getLang() != null && !getLang().isEmpty()) {
            value = String.format("%s@%s", protect(value), getLang());
        } 
//        else if (isExtension()) {
//            value = getContent();
//        }
        else if (isLiteral()) {
            value = protect(value);
        } else if (isURI()) {
            if (DISPLAY_AS_PREFIX) {
                String str = nsm.toPrefix(value, true);
                if (str == value) {
                    value = String.format("<%s>", value);
                } else {
                    value = str;
                }
            }
            else {
                value = String.format("<%s>", value);
            }
        } else if (isBlank()) {
        }

        return value;
    }

    String protect(String label) {
        String str = Constant.addEscapes(label, false);
        return "\"" + str + "\"";
    }

    /**
     * Store the lang as an instance of CoreseString shared by all literals
     */
    public IDatatype intGetDataLang(String lang) {
        IDatatype dtl = lang2dataLang.get(lang);
        if (dtl == null) {
            if (lang.equals("")) {
                dtl = empty;
            } else {
                dtl = new CoreseString(lang);
            }
            lang2dataLang.put(lang, dtl);
        }
        return dtl;
    }
    
    public static IDatatype getGenericDatatype(String uri) {
        IDatatype dt = hdt.get(uri);
        if (dt == null) {
            dt = new CoreseURI(uri);
            hdt.put(uri, dt);
        }
        return dt;
    }

    public CoreseDatatype() {
    }
    
    /**
     * @todo: leverage extension and pointer datatype
     */
    @Override
    public IDatatype copy() {
        return DatatypeMap.copy(this);
    }

    @Deprecated
    public static IDatatype create(String valueJType, String label) throws CoreseDatatypeException {
        return create(valueJType, null, label, null);
    }

    /**
     * Create a datatype. If it is a not well formed number, create a
     * CoreseUndef
     */
    public static IDatatype create(String valueJType,
            String datatype, String label, String lang)
            throws CoreseDatatypeException {
        try {
            return create(valueJType, datatype, label, lang, false);
        } catch (CoreseDatatypeException e) {
            if ((CoreseDatatype.isNumber(datatype, valueJType) || 
                    datatype.equals(XSD.xsddate))
                    && !valueJType.equals(Cst.jTypeUndef)) {
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
        
        if (lang != null) {
            lang = lang.toLowerCase();
        }
        switch (valueJType){
            case Cst.jTypeString:   return new CoreseString(label);
            case Cst.jTypeURI:      return new CoreseURI(label);
            case Cst.jTypeLiteral:  return new CoreseLiteral(label, lang);  
            case Cst.jTypeJSON:     return DatatypeMap.json(label);
        }
       
        String display = (datatype == null) ? valueJType : datatype;

        CoreseDatatype o = null;
        Class valueClass = null;
        try {
            valueClass = Class.forName(valueJType);
        } catch (ClassNotFoundException e) {
            throw new CoreseDatatypeException(e, valueJType, label);
        }

        try {
            Class[] argClass = {label.getClass()};
            String[] arg = {label};
            o = (CoreseDatatype) valueClass.getConstructor(argClass).newInstance((Object[]) arg);
        } catch (ClassCastException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new CoreseDatatypeException(e, valueJType, label);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // CoreseDatatypeException arrives here
            if (cast) {
                // to speed up sparql cast we throw a static exception
                // because it will be transformed to null
                throw failure;
            } else {
                throw new CoreseDatatypeException(e, display, label);
            }
        }

        o.setLang(lang);
        o.setDatatype(datatype);
        return o.typeCheck();
    }

    public IDatatype typeCheck() {
        return this;
    }

//    public boolean isDatatype() {
//        return true;
//    }

    @Override
    public boolean isXMLLiteral() {
        return false;
    }

    @Override
    public IDatatype cast(String datatype) {
        String javaType = DatatypeMap.getJavaType(datatype);
        if (javaType == null) {
            return null;
        }
        return cast(datatype, javaType);
    }
        
    @Override
    public IDatatype cast(IDatatype datatype){
        return cast(datatype.getLabel());
    }
    
    IDatatype cast(String datatype, String javaType) {
        IDatatype dt = caster(javaType);
        // effective for undef only :
        if (dt != null) {
            dt.setDatatype(datatype);
        }
        return dt;
    }

    /**
     * cast above set the datatype uri should have datatype as extra arg
     */
    IDatatype caster(String type) {
        try {
            if (isTripleWithEdge() && type.equals(Cst.jTypeString)) {
                return DatatypeMap.newInstance(toString());
            }
            else if (isBlank() && type.equals(Cst.jTypeString)) {
                return CoreseDatatype.create(type, null, "", null);
            }
            
            return CoreseDatatype.create(type, null, getNormalizedLabel(), null, true);
        } catch (CoreseDatatypeException e) {
            return null;
        } catch (Exception e) {
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
    public boolean isTrueAble() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }
    
    @Override
    public boolean isMap() {
        return false;
    }
    
     @Override
    public boolean isJSON() {
        return false;
    }

    @Override
    public boolean isLoop() {
        return false;
    }

    @Override
    public List<IDatatype> getValues() {
        return null;
    }
    
    @Override
    public IDatatypeList getList() {
        return null;
    }
    
    @Override
    public List<IDatatype> getValueList() {   
        ArrayList<IDatatype> list = new ArrayList();
        if (isLoop()){
            for (IDatatype dt : this){
                if (dt != null){
                    list.add(dt);
                }
            }
        }
        return list;
    }
    
    @Override
    public IDatatype getValue(String var, int n) {
        return get(n);
    }
    
    @Override
    public IDatatype toList(){
        return DatatypeMap.newInstance(getValueList());
    }
    
    
    
    @Override
    public Iterator<IDatatype> iterator() {
        if (isLoop()){
            return loopIterator();
        }
        else {
            return new ArrayList<IDatatype>(0).iterator();
        }
    }
    
    Iterator<IDatatype> loopIterator() {
        return new Iterator<IDatatype>() {
            final Iterator it = getLoop().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public IDatatype next() {
                Object obj = it.next();
                if (obj == null) {
                    return null;
                }
                return DatatypeMap.getValue(obj);
            }
        };
    }
     
    @Override
    public Iterable getLoop() {
        return null;
    }
    
    @Override
    public IDatatype has(IDatatype key) {
        return null;
    }

    @Override
    public IDatatype get(int n) {
        return null;
    }
    
    @Override
    public IDatatype get(IDatatype name) {
        return null;
    }
    
    @Override
    public IDatatype set(IDatatype name, IDatatype value) {
        return value;
    }

    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public IDatatype length() {
        return DatatypeMap.newInstance(size());
    }

    @Override
    public boolean isUndefined() {
        return false;
    }
    
    // isUndefined or isExtension
    @Override
    public boolean isGeneralized() {
        return false;
    }

    @Override
    public boolean isBlank() {
        return false;
    }
    
    @Override
    public IDatatype isBlankNode() {
        return getValue(isBlank());
    }

    @Override
    public boolean isSkolem() {
        return false;
    }

    @Override
    public void setObject(Object obj) {
    }

    @Override
    public Object getNodeObject() {
        return null;
    }
    
    @Override
    public Path getPath() {
        return null;
    }

    // redefined as false by CoreseResource
    @Override
    public boolean isLiteral() {
        return true;
    }
    
    @Override
    public IDatatype isLiteralNode() {
        return getValue(isLiteral());
    }
    
    @Override
    public IDatatype isWellFormed() {
        if (isLiteral() && isUndefined()) {
            if (getDatatypeURI().startsWith(XSD.XSD)) {
                return FALSE;
            } else if (getDatatypeURI().startsWith(RDF.RDF) && !getDatatypeURI().equals(RDF_HTML)) {
                return FALSE;
            }
        }
        return TRUE;
    }

    // redefined by CoreseUndefFuture
    // specific datatype for transformer future value
    @Override
    public boolean isFuture() {
        return false;
    }

    @Override
    public boolean isPointer() {
        return false;
    }
    
    // CoresePointer of LDScript Extension datatype
    @Override
    public boolean isExtension() {
        return false;
    }

    @Override
    public Pointerable getPointerObject() {
        return null;
    }

    @Override
    public PointerType pointerType() {
        return PointerType.UNDEF;
    }

    public boolean isDecimal() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isURI() {
        return false;
    }
    
    @Override
    public IDatatype isURINode() {
        return getValue(isURI());
    }

    @Override
    public void setLang(String str) {
    }

    @Override
    public boolean hasLang() {
        return false;
    }

    @Override
    public String getLang() {
        return null;
    }

    @Override
    public void setDatatype(String uri) {
    }

    @Override
    public IDatatype getDatatype() {
        return datatype;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public boolean startsWith(IDatatype iod) {
        return false;
    }

    @Override
    public boolean contains(IDatatype iod) {
        return false;
    }

    @Override
    public String getLowerCaseLabel() {
        return getLabel().toLowerCase();
    }

    @Override
    public void setValue(String str) throws CoreseDatatypeException {
    }
    
    @Override
    public void setValue(BigDecimal bd) {
    }

    @Override
    public void setValue(int n) {
    }

    @Override
    public void setValue(String str, int nid, IStorage pmgr) {
    }

    @Override
    public String getNormalizedLabel() {
        return "";
    }

    @Override
    public void setValue(IDatatype dt) {
    }

    @Override
    public String getLabel() {
        return getNormalizedLabel();
    }
    
    @Override
    public String getPrettyLabel() {
        return getLabel();
    }

    @Override
    public StringBuilder getStringBuilder() {
        return null;
    }

    @Override
    public void setStringBuilder(StringBuilder s) {
    }

    @Override
    public boolean isNumber() {
        return false;
    }
    
    
    
    @Override
    public boolean isDecimalInteger(){
        switch (getCode()){
            case DECIMAL:
            // cover all integer subtypes (long, short, etc.)
            case INTEGER :return true;
        }
        return false;
    }
    
     @Override
    public boolean isBoolean() {
        return false;
    }
    
    @Override 
    public boolean isDate(){
        return false;
    }
    
    @Override
    public Object objectValue(){
        switch (getCode()){
            case INTEGER: 
                return longValue();
            case DOUBLE:  
                return doubleValue();
            case DECIMAL:
                return decimalValue();
            case FLOAT:   
                return floatValue();
            case BOOLEAN:
                return booleanValue();                
            default: 
                return stringValue();
        }
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public String stringValue() {
        return getLabel();
    }

    @Override
    public long longValue() {
        return -1;
    }

    @Override
    public BigDecimal decimalValue(){
        return new BigDecimal(-1);
    }

    @Override
    public int intValue() {
        return -1;
    }

    @Override
    public double doubleValue() {
        return -1;
    }

    @Override
    public float floatValue() {
        return -1;
    }


    /**
     * Generic comparison between datatypes, used to sort the projection cache
     * and to sort results. It sorts deterministically different datatypes with
     * their natural order (e.g. : numbers, strings, dates) The order is :
     * Literal, URI, BlankNode 
     * string/literal/XMLliteral/boolean undef numbers are sorted by values
     * string/literal/XMLLiteral/undef/boolean are sorted alphabetically then by
     * datatypes (by their code) literals are sorted with their lang if any 
     * @todo: the primary order (Lit URI BN) is inverse of SPARQL !!!
     * used for select distinct, group by (sameTerm semantics)
     * used by Graph Node table to retrieve an existing Node
     * Distinguish 1 01 1.0 '1'^^xsd:long (does not return 0 but -1 or +1)
     * Distinguish true '1'^^xsd:boolean
     * Distinguish date with Z and +00:00
     */
    @Override
    public int compareTo(Object d2) {
        if (d2 instanceof IDatatype) {
            return compareTo((IDatatype) d2);
        }
        return -1;
    }
    
    // to be used when isTriple() == true
    // redefined in CoreseBlankNode
    @Override
    public int compareTriple(IDatatype dt) throws CoreseDatatypeException {
        throw failure;
    }
   
    @Override
    public int compareTo(IDatatype d2) {
        int code = getCode(), other = d2.getCode();
        boolean b = false;

        switch (other) {

            case STRING:
                if (code == other) {
                    // Generic Datatype is also STRING
                    return this.getLabel().compareTo(d2.getLabel());
                }
                break;

            case URI:
            case XMLLITERAL:
            case URI_LITERAL:
                if (code == other) {
                    return this.getLabel().compareTo(d2.getLabel());
                }
                break;

            case BLANK: case TRIPLE:
                if (code == other) {
                    if (isTriple() && d2.isTriple()) {
                        try {
                            return compare(d2);
                        } catch (CoreseDatatypeException ex) {
                            // continue;
                        }
                    }
                    return this.getLabel().compareTo(d2.getLabel());
                }
                break;

            case DOUBLE:
            case FLOAT:
            case DECIMAL:
            case INTEGER:

                switch (code) {
                    case DOUBLE:
                    case FLOAT:
                    case DECIMAL:
                    case INTEGER:
                        try {
                            // 0 if sameTerm
                            return compareNumber(d2);
                        } catch (CoreseDatatypeException e) {
                        }
                }

            case BOOLEAN:              
                 if (code == other) {
                   if (this.booleanValue() == d2.booleanValue()){
                       // sameTerm: compare 0 false and 1 true
                       return this.getLabel().compareTo(d2.getLabel());
                   }
                   else if (this.booleanValue()){
                       return GREATER;
                   }
                   else {
                       return LESSER;
                   }
                }
                break;
                
            case DATE:
            case DATETIME:

                if (code == other) {
                    return compareDate(d2);                    
                }
                break;

            case UNDEF:
                if (code == UNDEF) {
                    int res = getDatatypeURI().compareTo(d2.getDatatypeURI());
                    if (res == 0) {
                        return defaultCompare(d2);
                    } else {
                        return number(res);
                    }
                }


        }

        boolean trace = false;
        IDatatype d1 = this;
        
        // case where CODE are not equal 
        if (isTriple()) {
            return GREATER;
        } else if (d2.isTriple()) {
            return LESSER;
        }

        switch (code) {
            // case where CODE are not equal 
            // SPARQL order:
            // Blank URI Literal

            case BLANK: 
                return LESSER;

            case URI:
                switch (other) {
                    case BLANK: 
                        return GREATER;
                    // other is Literal
                    default:
                        return LESSER;
                }

            default:
                // this is LITERAL
                switch (other) {
                    case BLANK: 
                    case URI:
                        return GREATER;
                }
        }


        // String vs Literal
        switch (code) {

            case STRING:
                if (other == LITERAL) {
                    int res = d1.getLabel().compareTo(d2.getLabel());
                    if (res == 0) { 
                        if (d2.hasLang()) {
                            return LESSER;
                        }
                        else if (DatatypeMap.SPARQLCompliant){ 
                            // string and simple literal are considered distinct
                            return Integer.compare(getCode(), d2.getCode());
                        }
                    }
                    return res;
                }
                break;


            case LITERAL:

                switch (other) {

                    case STRING:
                        int res = d1.getLabel().compareTo(d2.getLabel());
                        if (res == 0) { 
                            if (hasLang()) {
                                return GREATER;
                            }
                            else if (DatatypeMap.SPARQLCompliant){
                               // string and simple literal are considered distinct
                                return Integer.compare(getCode(), d2.getCode()); 
                            }
                        }
                        return res;


                    case LITERAL:

                        res = d1.getLabel().compareTo(d2.getLabel());

                        if (!hasLang() && !d2.hasLang()) {
                            return res;
                        }

                        if (res == 0) {
                            if (!hasLang()) {
                                return LESSER;
                            } else if (!d2.hasLang()) {
                                return GREATER;
                            } else {
                                return getLang().compareTo(d2.getLang());
                            }
                        }

                        return res;

                }
        }


        if (code < other) {
            return LESSER;
        } else {
            return GREATER;
        }
    }
    
    public int defaultCompare(IDatatype d2) {
        return number(getLabel().compareTo(d2.getLabel()));
    }
    
    // for CoreseMap TreeMap
    @Override
    public int mapCompareTo(IDatatype dt) {
        return compareTo(dt);
    }
    
    int number(int val) {
        if (val == 0) return 0;
        if (val < 0) return -1;
        return 1;
    }
    
    int compareDate(IDatatype dt) {
        try {
            int res = compare(dt);
            if (res != 0) {
                return res;
            }
            if (sameTerm(dt)) {
                return 0;
            }
            // else distinguish == that are not sameTerm
        } catch (CoreseDatatypeException ex) {
            // incomparable dates
        }
        return getLabel().compareTo(dt.getLabel());
    }
    
    /**
     * RDF sameTerm semantics:
     * sameTerm = same datatype, same value and same label 
     * 1 != 01 != 1.0 != '1'^^xsd:long
     * use case: order by, group by, select distinct
     * 
     */
    int compareNumber(IDatatype dt) throws CoreseDatatypeException{
        int res = compare(dt);
        if (res == 0){
            res = getDatatypeURI().compareTo(dt.getDatatypeURI());
            if (res == 0){
               res = getLabel().compareTo(dt.getLabel());
            }
        }
        return res;
    }
    
    /**
     * Same datatype or String & Literal
     */
//    boolean equivalentDatatype(IDatatype dt) {
//        return getDatatype() == dt.getDatatype()
//                || getCode() == STRING && dt.getCode() == LITERAL
//                || getCode() == LITERAL && dt.getCode() == STRING;
//    }

    // compare values (e.g. for numbers)
    @Override
    public int compare(IDatatype iod) throws CoreseDatatypeException {
        throw failure();
    }

    @Override
    public boolean less(IDatatype iod) throws CoreseDatatypeException {
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

    static boolean isNumber(String name, String cname) {
        return dm.isNumber(name)
                || cname.equals(jTypeInteger) || cname.equals(jTypeGenericInteger);
    }

    /**
     * SPARQL RDF Term equal =
     * Every datatype has its own type safe equals with specific type error
     */
    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
        throw failure();
    }

    // Java equals (for list membership ...)
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IDatatype) {
            return equals((IDatatype) obj);
        } else if (obj instanceof Node) {
            return equals( ((Node) obj).getValue());
        }
        return false;
    }
    
    public boolean equals(IDatatype iod) {
        try {
            return equalsWE(iod);
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }
    
    // overloaded by CoreseTriple
    @Override
    public boolean sameTerm(IDatatype dt) {
        if (getCode() == dt.getCode()) {
            if (isNumber()){
                try {
                    return compareNumber(dt) == 0;
                }
                catch (CoreseDatatypeException e){
                    // never happens
                    return false;
                }
            }
            else { 
                return equals(dt) && 
                   getLabel().equals(dt.getLabel());
            }
        }
        return false;
    }

    @Override
    public IDatatype eq(IDatatype dt) {
        try {
            return (this.equalsWE(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }

    @Override
    public IDatatype neq(IDatatype dt) {
        return ne(dt);
    }
    
    @Override
    public IDatatype ne(IDatatype dt) {
        try {
            return (!this.equalsWE(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }

    @Override
    public IDatatype le(IDatatype dt) {       
        try {
            return (this.lessOrEqual(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }

    @Override
    public IDatatype lt(IDatatype dt) {
        try {
            return (this.less(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }

    @Override
    public IDatatype ge(IDatatype dt) {
        try {
            return (this.greaterOrEqual(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }

    @Override
    public IDatatype gt(IDatatype dt) {
        try {
            return (this.greater(dt)) ? TRUE : FALSE;
        } catch (CoreseDatatypeException ex) {
            return null;
        }
    }

    IDatatype getValue(boolean b) {
        return (b) ? TRUE : FALSE;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
    public IDatatype minus(long val) {
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
    
    @Override
    public IDatatype divis(IDatatype iod) {
        return div(iod);
    }

    /**
     * Default definitions of datatype operators return type error or false
     */
    CoreseDatatypeException failure() {
        return failure;
    }

    @Override
    public String getDatatypeURI() {
        if (getDatatype() != null) {
            return getDatatype().getLabel();
        } else {
            return null;
        }
    }

    @Override
    public IDatatype getDatatypeValue() {
        return this;
    }
    
    // for extension CoreseXML
    @Override
    public IDatatype getObjectDatatypeValue() {
        return this;
    }

    public boolean isPath() {
        return false;
    }

    /**
     * **************************************************************
     *
     * IDatatype implements Node to be able to get rid of both Node & IDatatype
     * objects IDatatype could be a node in graph directly (not used yet)
     * In addition, SPARQL statements such as bind that require a Node may use IDatatype 
     * see ProducerImpl getNode()
     *
     ***************************************************************
     */  
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
        return sameTerm( n.getValue());
    }
    
    @Override
    public boolean match(Node n) {
        return match(n.getValue());
    }
    
    // for graph match     
    public boolean match(IDatatype dt) {
        if (DatatypeMap.SPARQLCompliant) {
            return sameTerm(dt);
        }
        if (DatatypeMap.DATATYPE_ENTAILMENT) {
            // equal values and compatible datatypes
            return basicMatch(dt);
        }        
        return sameTerm(dt);
    }
    
    /**
     * all kinds of integer match
     * and integer match decimal
     */
    public boolean basicMatch(IDatatype dt) {
        return equals(dt) && compatible(dt);       
    }
    
    
    // they are equal, are the datatypes compatible ?
    public boolean compatible(IDatatype dt) {
        switch (getCode()) {
            case INTEGER:
            case DECIMAL:
                return dt.isDecimalInteger();
            case DOUBLE:
            case FLOAT:
                return dt.getCode() == getCode();
        }
        return true;
    }
    
    @Override
    public boolean conform(IDatatype type) {
        if (getDatatype() == null) {
            return false;
        }
        if (getDatatype().equals(type)) {
            return true;
        }       
        return false;
    }
    
    @Override
    public int compare(Node n) {
        return compareTo( n.getValue());
    }

    @Override
    public boolean isVariable() {
        return false;
    }
    
    @Override
    public void setVariable(boolean b) {
        
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public IDatatype getValue() {
        return this;
    }

//    @Override
//    public Object getProperty(int p) {
//        return null;
//    }
//
//    @Override
//    public void setProperty(int p, Object o) {
//    }

    @Override
    public Node getNode() {
        return this;
    }


    @Override
    public Node getGraph() {
        return null;
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
    public TripleStore getTripleStore() {
        return graph;
    }
    
    @Override
    public void setTripleStore(TripleStore store) {
        graph = store;
    }
    
    
    @Override
    public IDatatype getPublicDatatypeValue() {
        return publicDatatypeValue;
    }

    
    @Override
    public IDatatype setPublicDatatypeValue(IDatatype aGlobalDatatypeValue) {
        publicDatatypeValue = aGlobalDatatypeValue;
        return publicDatatypeValue;
    }
}
