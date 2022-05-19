package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;
import static fr.inria.corese.sparql.datatype.RDF.OWL;
import static fr.inria.corese.sparql.datatype.RDF.OWL_SAME_AS;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fr.inria.corese.sparql.triple.cst.KeywordPP.DOT;
import fr.inria.corese.sparql.triple.cst.RDFS;
import java.util.ArrayList;

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
 * Represents a triple (resource property value)
 * <br>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class Triple extends Exp implements Pointerable {
    public static boolean display = false;


    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(Triple.class);

//	final static String SDT = KeywordPP.SDT;
//	final static String LANG = KeywordPP.LANG;
    final static String PREFIX = "PREFIX";
    final static String BASE = "BASE";
    static final String SPACE = " ";
    static int ccid = 0;
    // nb different system variables in a query ...
    static final int MAX = Integer.MAX_VALUE;
    ASTQuery ast;
    //String tproperty;
    Atom subject, object;
    // property variable
    Variable variable;
    // property qname (or uri if written as <>)
    Constant predicate;
    // graph ?src/uri
    Atom source;
    // draft for tuple
    List<Atom> larg;
    // rdf star annotation triples t q v 
    // s p o t {| t q v |}
    private List<Triple> tripleList;
    // path regex
    Expression regex;
    String mode;
    int id = -1; // an unique id for triple (if needed to generate variable/option)
    int star = 0; // for path of length n	
    boolean isexp = false;     // is a filter
    boolean isoption = false;
    boolean isRec = false;   // graph rec ?s {}
    //boolean isall=false;     // all::p{n}
    boolean istype = false;    // rdf:type or <= rdf:type
    boolean isdirect = false;  // direct::rdf:type
    private boolean matchArity = false;
    private boolean nested = false;

    public Triple() {
        setID();
    }

    public Triple(int num) {
        id = num;
    }

    // subject pred::var object
    Triple(Atom sub, Constant pred, Variable var, Atom obj) {
        subject = sub;
        object = obj;
        predicate = pred;
        variable = var;
        if (predicate.getName().equals(RDFS.rdftype) || predicate.getName().equals(RDFS.RDFTYPE)) {
            istype = true;
        }
    }

    public static Triple create(Atom src, Atom sub, Constant pred, Variable var, Atom obj) {
        Triple t = new Triple(sub, pred, var, obj);
        t.source = src;
        t.setID();
        return t;
    }

    public static Triple create(Atom src, Atom sub, Constant pred, Atom obj) {
        Triple t = new Triple(sub, pred, null, obj);
        t.source = src;
        t.setID();
        return t;
    }

    public static Triple create(Atom sub, Constant pred, Variable var, Atom obj) {
        Triple t = new Triple(sub, pred, var, obj);
        t.setID();
        return t;
    }

    public static Triple create(String subject, String property, String value) {
        return create(null, subject, property, value);
    }

    public static Triple create(String source,
            String subject, String property, String value) {
        Constant src = null;
        if (source != null) {
            src = Constant.create(source);
        }
        Triple t = Triple.create(src, Constant.create(subject),
                Constant.create(property), null, Constant.create(value));
        return t;
    }

    // for triples
    public static Triple create(Expression subject, Atom predicate, Expression object) {
        if (!subject.isAtom() && !object.isAtom()) {
            return null;
        }
        Variable var = null;
        Constant pred = null;
        if (predicate.isConstant()) {
            pred = (Constant) predicate;
            var = pred.getIntVariable();
        } else {
            pred = Constant.createResource(getRootPropertyQN());
            var = (Variable) predicate;
        }
        Triple t = new Triple((Atom) subject, pred, var, (Atom) object);
        t.setID();
        t.setTriple(t.subject, predicate, t.object);
        return t;
    }


    public static Triple createNS(Constant type, Constant prefix, Constant uri) {
        return create(type, prefix, uri);
    }

    @Override
    public Triple copy() {
        return this;
    }
    
    public Triple duplicate() {
        Triple t =  create(getSubject(), getPredicate(), getObject());
        t.setRegex(getRegex());
        return t;
    }

    private void setTriple(Atom exp1, Atom atom, Atom exp2) {
        setOne(atom.isIsone());
        setDirect(atom.isIsdirect());
        setPath(atom.getStar());

    }

    @Override
    public Triple getTriple() {
        return this;
    }

    public void setID() {
        id = nextID();
    }

    @Override
    public boolean isTriple() {
        return true;
    }

    @Override
    public boolean isConnected(Triple t) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (getTerm(i).equals(t.getTerm(j))) {
                    return true;
                }
            }
        }
        if (getPredicate().isVariable() && t.getPredicate().isVariable()) {
            return getPredicate().equals(t.getPredicate());
        }
        return false;
    }

    synchronized static int nextID() {
        // nb max de variables systemes differentes ds une requete
        if (ccid == MAX) {
            ccid = 0;
        }
        return ccid++;
    }

    public void setID(int num) {
        id = num;
    }

    /**
     * **********************************************************************
     * 2. Semantic phase expand prefix with namespace uri expand get:gui expand
     * path : p[2] p{2} return uri triple
     */

    /**
     * Util functions
     *
     * @return
     */
    public String getResource() {
        return subject.getName();
    }

    public String getValue() {
        return object.getName();
    }

    public String getSourceName() {
        if (source == null) {
            return null;
        }
        return source.getName();
    }

    public Atom getSource() {
        return source;
    }

    public void setVSource(Atom at) {
        source = at;
    }

    @Override
    public void setRec(boolean b) {
        isRec = b;
    }

    public boolean isRec() {
        return isRec;
    }

    public Atom getExp(int i) {
        switch (i) {
            case 0:
                return getSubject();
            case 1:
                return getObject();
            case 2:
                return getVariable();
            case 3:
                return getSource();
            default:
                return null;
        }
    }

    public Atom getArg(int i) {
        switch (i) {
            case 2:
                return getProperty();
            default:
                return getExp(i);
        }
    }

    public Atom getTerm(int i) {
        switch (i) {
            case 2:
                return getPredicate();
            default:
                return getArg(i);
        }
    }

    public boolean similar(Triple t2) {
        for (int i = 0; i < getArity(); i++) {
            if (getArg(i) == null || t2.getArg(i) == null) {
                if (getArg(i) != t2.getArg(i)) {
                    return false;
                }
            }
            if (!getArg(i).getName().equals(t2.getArg(i).getName())) {
                return false;
            }
        }
        return true;
    }

    public void setArg(int i, Atom exp) {
        setExp(i, exp);
    }

    public void setExp(int i, Atom exp) {
        switch (i) {
            case 0:
                setSubject(exp);
                break;
            case 1:
                setObject(exp);
                break;
            case 2:
                if (exp.isVariable()) {
                    setVariable((Variable) exp);
                } else {
                    setProperty((Constant) exp);
                }
                break;
            case 3:
                source = exp;
                break;
        }
    }

    public int getArity() {
        return 4;
    }

    public List<Atom> getArgs() {
        return larg;
    }

    public void setArgs(List<Atom> l) {
        larg = l;
    }
    
    public List<Atom> getArgList() {
        ArrayList<Atom> list = new ArrayList<>();
        list.add(getSubject());
        list.add(getObject());
        if (getArgs()!=null) {
            for (Atom at : getArgs()) {
                list.add(at);
            }
        }
        return list;
    }
    
    public List<Atom> getElements() {
        ArrayList<Atom> list = new ArrayList<>();
        list.add(getSubject());
        list.add(getPredicate());
        list.add(getObject());
        return list;
    }
    
    public boolean hasTripleReference() {
        for (Atom at : getArgList()) {
            if (at.isTriple()) {
                return true;
            }
        }
        return false;
    }

    public void setSubject(Atom e1) {
        subject = e1;
    }

    public void setObject(Atom e2) {
        object = e2;
    }

    public Expression getRegex() {
        return regex;
    }

    public void setRegex(Expression exp) {
        regex = exp;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String m) {
        mode = m;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int s) {
        star = s;
    }

    public boolean isType() {
        return istype;
    }

    public void setType(boolean b) {
        istype = b;
    }
    
    public boolean isList() {
        return getProperty().getLabel().equals(RDF.FIRST) ||
               getProperty().getLabel().equals(RDF.REST);
    }
    
    public boolean isSameAs() {
        return getProperty().getLabel().equals(OWL_SAME_AS);
    }


    /**
     *
     * @param str
     * @return
     */
    String clean(String str) {
        return str;
    }
    
    public String toNestedTriple() {
        return String.format("<<%s>>", toTriple());
    }
    
    public String toTriple() {
        return toTriple(getSubject(), getPredicate(), getObject());
    }
    
    String toTriple(Atom s, Atom p, Atom o) {
        return String.format("%s %s %s", s, p, o);
    }
    
    String toTriple(String s, String p, String o) {
        return String.format("%s %s %s", s, p, o);
    }

    @Override
    public String toString() {
        return toString(new ASTBuffer()).toString();
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {        
        return toStringWithAnnotation(sb);
    }
      
    
    /**
     * When printing rdf star triple, terms that are nested triples are printed 
     * as <<s p o>> except in one case: s p o {| q v |}
     * <<s p o>> is printed by Atom toNestedTriple() 
     * called by Constant or Variable toString()
     * When printing AST, rdf star triples to be printed are selected by Exp isDisplayable()
     * hence we do not print here every physical triple in a BGP 
     * but a subset of relevant star triples
     * 
     * this = s p o
     * recursively pprint s p o {| q v |} if it has annotation
     * record that annotation are already printed in ASTBuffer
     * in order not to pprint them again in Exp pprint
     */
    public ASTBuffer toStringWithAnnotation(ASTBuffer sb) {
        if (getSource() != null) {
            sb.append(getSource()).append(SPACE);
        }

        String s = getSubject().toString();
        String p = propertyToString();
        String o = getObject().toString();
      
        if (getTripleList()!=null && displayAsTriple()) {
            // this = t q v with t = s p o
            // ::=
            // s p o {| q v |}
            sb.append(toTriple(s, p, o));
            annotation(sb);
        }
        else if (getArgs() == null || (hasReference() && displayAsTriple())) {
            // std triple
            sb.append(String.format("%s .", toTriple(s, p, o)));
        } else {
            tuple(sb, s, p, o);
         }
      return sb;
    }
    
    /**
     */
    void annotation(ASTBuffer sb) {
        sb.append(" {| ");
        int i = 0;
        for (Triple t : getTripleList()) {
            sb.getDone().put(t, t);
            sb.append(String.format("%s%s %s", (i++==0)?"":"; ", t.getPredicate(), t.getObject()));
            if (t.getTripleList()!=null) {
                t.annotation(sb);
            }
        }
        sb.append(" |}");
    }
    
    
    String propertyToString() {
        String p = getProperty().toString();
        if (isPath()) {
            p = getRegex().toRegex();

            if (getVariable() != null && !getVariable().getName().startsWith(ASTQuery.SYSVAR)) {
                p += " :: " + getVariable().toString();
            }
        } else if (getVariable() != null) {
            if (isTopProperty()) {
                p = getVariable().toString();
            } else {
                p += "::" + getVariable().getName();
            }
        }
        return p;
    }
    
    // display additional arg: triple(s p o t1 .. tn)
    ASTBuffer tuple(ASTBuffer sb, String s, String p, String o) {
        // tuple()
        if (isNested()) {
            sb.append("<<");
        }
        sb.append(String.format("triple(%s %s %s", s, p, o));
        for (Atom e : getArgs()) {
            sb.append(SPACE).append(e);
        }
        if (isMatchArity()) {
            sb.append(DOT);
        }
        sb.append(")");
        if (isNested()) {
            sb.append(">>");
        }
        sb.append(DOT);
        return sb;
    }
    
    @Deprecated
    public ASTBuffer toStringBasic(ASTBuffer sb) {
        if (getSource() != null) {
            sb.append(getSource()).append(SPACE);
        }

        String s = getSubject().toString();
        String p = propertyToString();
        String o = getObject().toString();
      
        if (getSubject().isTripleWithTriple() && getSubject().getTriple().isAsserted()
                && displayAsTriple()) {
            // this = t q v with t = s p o
            // ::=
            // s p o {| q v |}
            sb.append(String.format("%s {| %s %s |} .", getSubject().toTriple(), p, o));
        }
        else if (getArgs() == null || (hasReference() && displayAsTriple())) {
            // std triple
            sb.append(String.format("%s %s %s .", s, p, o));
        } else {
            tuple(sb, s, p, o);
         }
      return sb;
    }
    
        // Exp display() check triple.isDisplayable()
    @Override
    public boolean isDisplayable() {
        return isAsserted() || ! displayAsTriple();
    }
    
    
    public boolean hasReference() {
        return getArgs() != null && !getArgs().isEmpty() && getArgs().get(0).isTriple();
    }

    boolean isTopProperty() {
        return predicate.getLongName().equals(getRootPropertyURI())
                || predicate.getName().equals(getRootPropertyQN());
    }

    public void setExp(boolean b) {
        isexp = b;
    }

    /**
     * This triple will generate a relation in the graph not an exp, not an
     * rdf:type with a constant value
     */
    @Override
    public boolean isRelation() {
        if (istype) {
            return (object.isVariable());
        } else {
            return !isexp;
        }
    }

//    public boolean isExp() {
//        return isexp;
//    }

//    @Override
//    public boolean isFilter() {
//        return isexp;
//    }

    @Override
    Bind validate(Bind global, int n) {
        Bind env = new Bind();
//        if (isFilter()) {
//            return getFilter().validate(env);
//        }
        for (int i = 0; i < getArity(); i++) {
            Atom arg = getExp(i);
            if (arg != null && arg.isVariable()) {
                env.bind(arg.getName());
            }
        }
        return env;
    }

    /**
     * Does triple bind this variable ?
     */
    public boolean bind(Variable var) {
        return bind(var.getName());
    }
    
    void getTripleVariables(VariableScope sort, List<Variable> list) {
        getSubject().getVariables(sort, list);
        if (!isPath()) {
            getPredicate().getVariables(sort, list);
        }
        getObject().getVariables(sort, list);
    }
    

    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
          getTripleVariables(sort, list);

    }

    public boolean bind(String name) {
        for (int i = 0; i < getArity(); i++) {
            Expression arg = getExp(i);
            if (arg != null && arg.isVariable() && arg.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Triple rewrite(Variable v1, Variable v2) {
        if (getSubject().equals(v1)) {
            setSubject(v2);
        }
        if (getObject().equals(v1)) {
            setObject(v2);
        }
        if (getVariable() != null && getVariable().equals(v1)) {
            setVariable(v2);
        }
        return this;
    }

    public boolean bind(Expression e) {
        // @todo: use getFilterVariables()
        for (String str : e.getVariables()) {
            if (!bind(str)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isOption() {
        return isoption;
    }

    public int getID() {
        return id;
    }

    Triple lastTriple() {
        return this;
    }

    public void setTOption(boolean b) {
        isoption = b;
    }

    @Override
    public void setOption(boolean b) {
        isoption = b;
    }

    public void setVariable(String var) {
        variable = new Variable(var);
    }

    public void setVariable(Variable var) {
        variable = var;
    }

    public boolean isDirect() {
        return isdirect;
    }

    public boolean isOne() {
        return false;
    }

    public boolean isPath() {
        return variable != null && variable.isPath();
    }

    public boolean isXPath() {
        if (regex == null) {
            return false;
        }
        return regex.getName().equals(Term.XPATH);
    }

    public Expression getXPath() {
        return Term.function(Term.XPATH, subject, regex.getArg(0));
    }

    void setOne(boolean b) {
        //isone = b;
    }

    public String getVariableName() {
        if (variable == null) {
            return null;
        }
        return variable.getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Triple) {
            return equals((Triple) obj);
        }
        return false;
    }
    
    // @todo: path
    public boolean equals(Triple t) {
         if (isPath()) {
            return equalsPath(t);
         }
         else {
             return equalsTriple(t);
         }
    }
    
    boolean equalsTriple(Triple t) {
         return getSubject().equals(t.getSubject()) &&
                 getPredicate().equals(t.getPredicate()) &&
                 getObject().equals(t.getObject());
    }
    
    boolean equalsPath(Triple t) {
        return this == t;
    }

    public Atom getSubject() {
        return subject;
    }

    public boolean isConstantNode() {
        return getSubject().isConstant() || getObject().isConstant();
    }

    public Atom getPredicate() {
        if (variable != null) {
            return variable;
        } else {
            return predicate;
        }
    }

    public void setPredicate(Atom at) {
        if (at.isVariable()) {
            setProperty(Constant.createResource(getRootPropertyQN()));
            setVariable(at.getVariable());
        } else {
            setVariable((Variable) null);
            setProperty(at.getConstant());
        }
    }

    public Variable getVariable() {
        return variable;
    }

    public Constant getProperty() {
        return predicate;
    }

    public void setProperty(Constant prop) {
        predicate = prop;
    }

    public Atom getObject() {
        return object;
    }

    public Atom object() {
        return object;
    }

    public Atom subject() {
        return subject;
    }

    public Atom predicate() {
        return getPredicate();
    }

    public void setDirect(boolean b) {
        isdirect = b;
    }

    public void setPath(int i) {
        star = i;
    }

    private static String getRootPropertyQN() {
        return ASTQuery.getRootPropertyQN();
    }

    private String getRootPropertyURI() {
        return ASTQuery.getRootPropertyURI();
    }

    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
//        if (isFilter()) {
//            // validate exists {}
//            return getFilter().validate(ast);
//        }

        if (getSubject().isVariable()) {
            ast.bind(getSubject().getVariable());
            if (!exist) {
                ast.defSelect(getSubject().getVariable());
            }
        }

        if (getVariable() != null) {
            ast.bind(getVariable());
            if (!exist) {
                ast.defSelect(getVariable());
            }
        }

        if (getObject().isVariable()) {
            ast.bind(getObject().getVariable());
            if (!exist) {
                ast.defSelect(getObject().getVariable());
            }
        }

        if (larg != null) {
            for (Atom at : larg) {
                if (at.isVariable()) {
                    ast.bind(at.getVariable());
                    if (!exist) {
                        ast.defSelect(at.getVariable());
                    }
                }
            }
        }

        return true;
    }

    /**
     * No variable in insert data {}
     */
    @Override
    public boolean validateData(ASTQuery ast) {
        if (subject.isSimpleVariable() || object.isSimpleVariable() || variable != null) {
            return false;
        }
        subject.validateData(ast);
        object.validateData(ast);
        return true;
    }

    @Override
    public boolean validateDelete() {
        if (subject.isBlankNode()||object.isBlankNode()) {
            return false;
        }
        return true;
    }

    public boolean contains(Atom at) {
        return subject.equals(at) || object.equals(at) || predicate.equals(at) || variable.equals(at);
    }

    @Override
    void visit(ExpressionVisitor v) {
        v.visit(this);
    }
    
    @Override
    // let ((s p o) = t)
    public IDatatype getValue(String var, int n){
        switch(n) {
            case 0: return getTerm(0).getDatatypeValue();
            case 1: return getPredicate().getDatatypeValue();
            case 2: return getTerm(1).getDatatypeValue();
        }
        return null;
    }
    
  
    public boolean isMatchArity() {
        return matchArity;
    }

   
    public void setMatchArity(boolean matchArity) {
        this.matchArity = matchArity;
    }
    
    public boolean isAsserted() {
        return ! isNested();
    }

    public boolean isNested() {
        return nested;
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }

    public List<Triple> getCreateTripleList() {
        if (getTripleList()==null) {
            setTripleList(new ArrayList<>());
        }
        return getTripleList();
    }
    
    public List<Triple> getTripleList() {
        return tripleList;
    }

    public void setTripleList(List<Triple> tripleList) {
        this.tripleList = tripleList;
    }
       
}
