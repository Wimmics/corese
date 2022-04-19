package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.Computer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.List;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class Constant extends Atom {
    public static boolean DISPLAY_AS_PREFIX = true;
    public static Constant rootProperty;
    private static Logger logger = LoggerFactory.getLogger(Constant.class);
    static DatatypeMap dm;
    private static boolean stringDatatype = false;
    boolean isQName = false;
    // when there is a datatype in the syntactical form of a literal
    private boolean nativeDatatype = false;
    IDatatype dt;
    String datatype = null;
    int weight = 1;
    private Variable var;
    // draft regexp
    private Expression exp;

    static {
        dm = DatatypeMap.create();
        rootProperty = Constant.createResource(RDFS.RootPropertyURI);
    }

    public Constant() {
    }

    private Constant(String name) {
        super(name);
        // by safety:
        setLongName(name);
        datatype = RDFS.RDFSRESOURCE;
    }

    private Constant(String name, String datatype, String lg) {
        super(name);
        this.datatype = datatype;
        setDatatypeValue(DatatypeMap.createLiteral(name, nsm().toNamespace(datatype), lg));
    }

    private Constant(String name, String dt) {
        this(name, dt, null);
    }

    public static Constant create(String str) {
        Constant cst = new Constant(str);
        cst.setDatatypeValue(DatatypeMap.createResource(cst.getLongName()));
        return cst;
    }

    public static Constant createResource(String str, String longName) {
        Constant cst = new Constant(str);
        cst.setLongName(longName);
        cst.setDatatypeValue(DatatypeMap.createResource(longName));
        return cst;
    }

    public static Constant createBlank(String label) {
        Constant cst = new Constant(label);
        cst.setDatatypeValue(DatatypeMap.createBlank(label));
        return cst;
    }
    
    public static Constant createTripleReference(String label) {
        Constant cst = new Constant(label);
        cst.setDatatypeValue(DatatypeMap.createTripleReference(label));
        return cst;
    }

    public static Constant createResource(String str) {
        Constant cst = new Constant(str);
        cst.setLongName(nsm().toNamespace(str));
        cst.setDatatypeValue(DatatypeMap.createResource(cst.getLongName()));
        return cst;
    }

    public static Constant createString(String str) {
        return create(str, RDFS.qxsdString);
    }

    public static Constant create(int n) {
        return new Constant(Integer.toString(n), RDFS.xsdinteger);
    }

    public static Constant create(long n) {
        return new Constant(Long.toString(n), RDFS.xsdinteger);
    }

    public static Constant create(double d) {
        return new Constant(Double.toString(d), RDFS.xsddouble);
    }

    public static Constant create(float d) {
        return new Constant(Float.toString(d), RDFS.xsdfloat);
    }

    public static Constant create(boolean b) {
        return new Constant((b) ? "true" : "false", RDFS.xsdboolean);
    }

    public static Constant create(String name, String dt) {
        return new Constant(name, dt, null);
    }

    public static Constant create(String name, String dt, String lg) {
        Constant cst = new Constant(name, dt, lg);
        return cst;
    }

    /**
     * Extended datatype where datatype(URI) = rdfs:Resource
     */
    @Override
    public String getDatatype() {
        return datatype;
    }

    void setDatatype(String str) {
        datatype = str;
    }

    boolean hasRealDatatype() {
        if (datatype == null) {
            return false;
        }
        for (String str : RDFS.FAKEDT) {
            if (datatype.equals(str)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getLang() {
        return dt.getLang();
    }

    public boolean hasLang() {
        String lg = dt.getLang();
        return lg != null && lg != "";
    }

    @Override
    public void toJava(JavaCompiler jc, boolean arg) {
        jc.toJava(this, arg);
    }
    
    public static void setString(boolean b) {
        stringDatatype = b;
    }
    
    static boolean isString() {
        return stringDatatype ;
    }
    
    String pretty(String datatype) {
        if (datatype.startsWith(NSManager.XSD)) {
            return String.format("^^%s", nsm().toPrefix(datatype));
        }
        else if (datatype.startsWith("http://")) {
            return String.format("^^<%s>", datatype);
        }
        return String.format("^^%s", datatype);
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        if (isTripleWithTriple() && displayAsTriple()) {
            sb.append(toNestedTriple());
        }
        else if (isLiteral()) {
            if (hasLang()) {
                //return name + "@" + lang;
                toString(name, sb);
                sb.append(KeywordPP.LANG).append(getLang());
            } else if (DatatypeMap.isUndefined(getDatatypeValue())) {
                    toString(name, sb);
                    sb.append(pretty(datatype));
                }
                else if (hasRealDatatype()) {
                if (getDatatypeValue().isList()){
                    sb.append("@").append(getDatatypeValue().getContent());
                }
                else if (datatype.equals(RDF.qxsdInteger)
                        || datatype.equals(RDF.xsdinteger)
                        || datatype.equals(RDF.qxsdBoolean)
                        || datatype.equals(RDF.xsdboolean)
                        || (datatype.equals(RDF.xsddecimal) && ! isNativeDatatype()) ) {
                    // value without datatype
                    sb.append(name);
                } else if (datatype.startsWith("http://")) {
                    toString(name, sb);
                    if (!datatype.equals(RDF.xsdstring) || isString() || isNativeDatatype()) {
                        // xsd: or value with datatype with syntax ^^<http://>
                        sb.append(pretty(datatype));
                    }
                } else {
                    toString(name, sb);
                    if (!datatype.equals(RDF.qxsdString) || isString() || isNativeDatatype()) {
                        // value with datatype with syntax ^^xsd:
                        sb.append(KeywordPP.SDT).append(datatype);
                    }
                }
            } else {
                toString(name, sb);
                return sb;
            }
        } else if (isBlank()) {
            sb.append(name);
        } else if (isQName && DISPLAY_AS_PREFIX) {
            sb.append(name);
        } else {
            sb.append(KeywordPP.OPEN).append(getLongName()).append(KeywordPP.CLOSE);
        }
        if (display) {
            completeDisplay();
        }
        return sb;
    }
    
    void completeDisplay() {
        if (isTriple() && getTriple()!=null) {
            Triple t = getTriple();
            System.out.println(String.format("%s = triple(%s %s %s)", getLabel(), 
                    t.getSubject().getDatatypeValue(), t.getPredicate().getDatatypeValue(), t.getObject().getDatatypeValue()));
            if (t.getObject().isTriple()) {
                t.getObject().getConstant().completeDisplay();
            }
        }
    }

    public StringBuffer toString2(StringBuffer sb) {
        sb.append(getDatatypeValue().toString());
        return sb;
    }

    /**
     * Escape special chars Add surrounding quotes to a string literal
     */
    public static void toString(String str, ASTBuffer sb) {
        String s = addEscapes(str);
        String sep = KeywordPP.QUOTE;

        if (s.contains(KeywordPP.QUOTE)) {
            if (s.contains(KeywordPP.DQUOTE)) {
                sep = KeywordPP.TQUOTE;
            } else {
                sep = KeywordPP.DQUOTE;
            }
        }

        sb.append(sep);
        sb.append(s);
        sb.append(sep);
    }

    /**
     * source: javacc replace special char by escape char for pprint
     */
    public static String addEscapes(String str) {
        return addEscapes(str, true);
    }

    public static String addEscapes(String str, boolean quote) {
        StringBuilder retval = new StringBuilder();
        char ch;
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case 0:
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
                    if (quote) {
                        retval.append("\\\'");
                    } else {
                        retval.append(str.charAt(i));
                    }
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

    @Override
    public Constant getConstant() {
        return this;
    }

    @Override
    public boolean equals(Object c) {
        if (c instanceof Constant) {
            return equals((Constant) c);
        }
        return false;
    }
    
    public boolean equals(Constant c) {
        return getDatatypeValue().sameTerm(c.getDatatypeValue());
    }

    static String getJavaType(String datatypeURI) {
        return dm.getJType(nsm().toNamespace(datatypeURI));
    }

    @Override
    public IDatatype getDatatypeValue() {
        return dt;
    }

    @Override
    public Expression prepare(ASTQuery ast) {
        getDatatypeValue();
        return this;
    }

    /**
     * Create Constant from IDatatype
     */
    public static Constant create(IDatatype dt) {
        Constant cst;
        if (dt.isLiteral()) {
            cst = create(dt.getLabel(), dt.getDatatype().getLabel(), dt.getLang());
        } else if (dt.isURI()) {
            cst = createResource(nsm().toPrefix(dt.getLabel(), true), dt.getLabel());
            //cst.setQName(! cst.getName().equals(cst.getLongName()));
        } else {
            cst = createBlank(dt.getLabel());
        }
        cst.setDatatypeValue(dt);
        return cst;
    }
    
    public static Constant createList(Constant cst) {
        IDatatype dt = cst.getDatatypeValue();
        IDatatype list = DatatypeMap.newList(dt);
        Constant res = Constant.create(list);
        return res;
    }
    
    @Override
     public Constant duplicate() {
        Constant cst;
        if (isLiteral()) {
            cst = create(getName(), getDatatype(), getLang());
        } else if (isResource()) {
            cst = createResource(getName(), getLongName());
            cst.setQName(isQName);
        } else {
            cst = createBlank(getName());
        }
        cst.setDatatypeValue(getDatatypeValue());
        return cst;
    }

    public void setDatatypeValue(IDatatype dd) {
        dt = dd;
    }

    @Override
    public int regLength() {
        return 1;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return dt.isLiteral();
    }

    @Override
    public boolean isBlank() {
        return dt.isBlank();
    }
    
    @Override
    public boolean isURI() {
        return dt.isURI();
    }
    
    @Override
    public boolean isTriple() {
        return getDatatypeValue().isTriple();
    }

    @Override
    public void setWeight(String w) {
        try {
            setWeight(Integer.parseInt(w));
        } catch (Exception e) {
        }
    }

    public void setWeight(int w) {
        weight = w;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public boolean isNumber() {
        return dt.isNumber();
    }

    @Override
    public boolean isResource() {
        return dt.isURI();
    }

    // use when "get:name::?x" or "c:SomeRelation::?p" because we have both a variable and a constant
    public void setVar(Variable s) {
        var = s;
    }

    @Override
    public void setExpression(Expression e) {
        exp = e;
    }

    @Override
    public Expression getExpression() {
        return exp;
    }

    // use when "get:name::?x" or "c:SomeRelation::?p" because we have both a variable and a constant
    @Override
    public Variable getIntVariable() {
        return var;
    }

    public boolean isQName() {
        return isQName;
    }

    public void setQName(boolean isQName) {
        this.isQName = isQName;
    }

    /**
     * KGRAM
     */
    @Override
    public int type() {
        return ExprType.CONSTANT;
    }

    @Override
    public IDatatype getValue() {
        return dt;
    }
    
//    @Override
//    IDatatype getExpressionDatatypeValue() {
//        //return DatatypeMap.createObject(this);
//        return getDatatypeValue();
//    }

    @Override
    public void visit(ExpressionVisitor v) {
        v.visit(this);
    }

    @Override
    public Expression transform(boolean isReverse) {
        Constant cst = this;
        if (isReverse) {
            cst = duplicate();
            cst.setReverse(isReverse);
            cst.setWeight(getWeight());
        }
        cst.setretype(cst.getretype());
        return cst;
    }

    @Override
    void getConstants(List<Constant> l) {
        if (!l.contains(this)) {
            l.add(this);
        }
    }
    
    @Override
    void getPredicateList(List<Constant> list) {
        if (! list.contains(this)) {
            list.add(this);
        }
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException{
        if (isTriple()) {
            return triple(eval, b, env, p);
        }
        return getDatatypeValue();
    }
    
    
     
    @Override
     public IDatatype eval(Computer eval, Environment env, Producer p, IDatatype[] param){
        return getDatatypeValue();
    }
     
    @Override
    public Expression replace(Variable arg, Variable var) {
        return this;
    }

    
    public boolean isNativeDatatype() {
        return nativeDatatype;
    }

    
    public void setNativeDatatype(boolean nativeDatatype) {
        this.nativeDatatype = nativeDatatype;
    }
    
    static NSManager nsm() {
        return NSManager.nsm();
    }
 

}