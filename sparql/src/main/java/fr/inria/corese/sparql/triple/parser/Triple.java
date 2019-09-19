package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.cst.RDFS;

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
public class Triple extends Exp {

    /**
     * @return the matchArity
     */
    public boolean isMatchArity() {
        return matchArity;
    }

    /**
     * @param matchArity the matchArity to set
     */
    public void setMatchArity(boolean matchArity) {
        this.matchArity = matchArity;
    }

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
    // tuple contain a filter
    //Expression exp;
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

//    // for filters
//    public static Triple create(Expression exp) {
//        Triple t = new Triple();
//        t.exp = exp;
//        t.isexp = true;
//        t.setID();
//        return t;
//    }

    public static Triple createNS(Constant type, Constant prefix, Constant uri) {
        return create(type, prefix, uri);
    }

    @Override
    public Triple copy() {
//        if (isFilter()) {
//            Expression exp = getFilter().copy();
//            return create(exp);
//        }
        return this;
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
    @Override
    public void setAST(ASTQuery a) {
        ast = a;
    }

    @Override
    public ASTQuery getAST() {
        if (ast == null) {
            ast = defaultAST();
        }
        return ast;
    }

    ASTQuery defaultAST() {
        ASTQuery ast = ASTQuery.create();
        //ast.setKgram(true);
        ast.setBody(new And());
        return ast;
    }

    /**
     * Translate this exp triple as a Term
     *
     * @return
     */
//    Expression toTerm() {
//        return exp;
//    }

//	boolean isString(String str) {
//		if ((str.startsWith("\"") && str.endsWith("\""))
//				|| (str.startsWith("'") && str.endsWith("'")))
//			return true;
//		else
//			return false;
//	}
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
                source = (Atom) exp;
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

//    public void setExp(Expression e) {
//        exp = e;
//    }

    public void setSubject(Atom e1) {
        subject = e1;
    }

    public void setObject(Atom e2) {
        object = e2;
    }

//    public Expression getExp() {
//        return exp;
//    }

//    @Override
//    public Expression getFilter() {
//        return exp;
//    }

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

    /**
     *
     * @param str
     * @return
     */
    String clean(String str) {
        return str;
    }

//    public ASTBuffer ftoSparql(Expression exp, ASTBuffer sb) {
//        if (exp == null) {
//            return sb;
//        }
//        boolean isAtom = (exp.isAtom());
//        sb.append(KeywordPP.FILTER + KeywordPP.SPACE);
//        if (isAtom) {
//            sb.append("(");
//        }
//        exp.toString(sb);
//        if (isAtom) {
//            sb.append(")");
//        }
//        sb.append(KeywordPP.SPACE);
//        return sb;
//    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {

//        if (isFilter()) {
//            return ftoSparql(getFilter(), sb);
//        }

        String SPACE = " ";

        if (source != null) {
            sb.append(source.toString()).append(" ");
        }

        String r = subject.toString();
        String p = predicate.toString();
        String v = object.toString();

        if (isPath()) {
            p = getRegex().toRegex();

            if (variable != null && !variable.getName().startsWith(ASTQuery.SYSVAR)) {
                p += " :: " + variable.toString();
            }
        } else if (variable != null) {
            if (isTopProperty()) {
                p = variable.toString();
            } else {
                p += "::" + variable.getName();
            }
        }

        if (larg != null) {
            // tuple()
            sb.append("triple" + KeywordPP.OPEN_PAREN);
            sb.append(r).append(SPACE).append(p).append(SPACE).append(v);
            for (Atom e : larg) {
                sb.append(SPACE).append(e.toString());
            }
            if (isMatchArity()) {
                sb.append(KeywordPP.DOT);
            }
            sb.append(KeywordPP.CLOSE_PAREN + KeywordPP.DOT);
        } else {
            sb.append(r).append(SPACE).append(p).append(SPACE).append(v).append(KeywordPP.DOT);
        }

        return sb;
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
//        if (isFilter()) {
//            return false;
//        }
        return bind(var.getName());
    }
    
    void getTripleVariables(VariableScope sort,List<Variable> list) {
        getSubject().getVariables(sort, list);
        if (!isPath()) {
            getPredicate().getVariables(sort, list);
        }
        getObject().getVariables(sort, list);
    }
    
//    void getFilterVariables(VariableScope scope, List<Variable> list) {
//        if (scope.isFilter()) {
//            getFilter().getVariables(scope, list);
//        }
//    }

    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
//        if (isFilter()) {
//            getFilterVariables(sort, list);
//        }
//        else {
//            getTripleVariables(sort, list);
//        }
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

    /**
     * @return a variable on which we attach the evaluable expression
     */
//    public String getExpVariable() {
//        if (exp == null) {
//            return null;
//        }
//        Variable var = exp.getVariable();
//        if (var != null) {
//            return var.getName();
//        } else {
//            return null;
//        }
//    }

    public String getVariableName() {
        if (variable == null) {
            return null;
        }
        return variable.getName();
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
        if (subject.isBlankNode()) {
            return false;
        }
        if (object.isBlankNode()) {
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
//        if (isFilter()) {
//            getFilter().visit(v);
//        }
    }

}
