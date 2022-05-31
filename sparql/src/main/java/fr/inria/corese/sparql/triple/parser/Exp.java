package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.exceptions.QuerySemanticException;
import fr.inria.corese.sparql.triple.api.ASTVisitor;
import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.sparql.triple.api.FederateMerge;
import fr.inria.corese.sparql.triple.api.Walker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
//import static java.util.logging.Level.ALL;

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
 * The root class of the statements of the query language: And,
 * BasicGraphPattern, Score, Source, Option, Or, Triple
 * <br>
 *
 * @author Olivier Corby
 */
public abstract class Exp extends TopExp implements Iterable<Exp> {
   
    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(Exp.class);

    private ArrayList<Exp> body;

    public Exp() {
        body = new ArrayList<>();
    }

    public Exp add(Exp exp) {
        if (exp.isBinary() && exp.size() == 1) {
            BasicGraphPattern bgp;

            if (size() == 1 && get(0).isBGP()) {
                bgp = (BasicGraphPattern) get(0);
            } else {
                bgp = BasicGraphPattern.create();
                for (Exp e : body) {
                    bgp.add(e);
                }
            }

            exp.add(0, bgp);
            body.clear();
            return add(exp);
        }
        body.add(exp);
        return this;
    }
    
    // for parser handler stack.addList()
    public Exp addList(RDFList list) {
        for (Exp exp : list) {
            add(exp);
        }
        return this;
    }

    // exp is a filter
    public Exp add(Expression exp) {
        add(ASTQuery.createFilter(exp));
        return this;
    }

    // for structured exp only
    public void include(Exp exp) {
        for (Exp ee : exp) {
            add(ee);
        }
    }

    public Constant getGraphName() {
        for (Exp exp : this) {
            if (exp.isNamedGraph()) {
                Atom name = exp.getNamedGraph().getSource();
                if (name.isConstant()) {
                    return name.getConstant();
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<Exp> iterator() {
        return getBody().iterator();
    }

    boolean isBinary() {
        return isMinus() || isOptional();
    }

    public boolean isBinaryExp() {
        return false;
    }

    public void add(int n, Exp exp) {
        getBody().add(n, exp);
    }

    public void addAll(Exp exp) {
        getBody().addAll(exp.getBody());
    }
    
    public void addDistinct(Exp exp) {
        for (Exp e : exp) {
            if (! getBody().contains(e)) {
                getBody().add(e);
            }
        }
    }

    public List<Exp> getBody() {
        return body;
    }

    public boolean isConnected(Triple t) {
        for (Exp exp : this) {
            if (exp.isTriple()) {
                if (exp.getTriple().isConnected(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConnected(Exp exp) {
        for (Exp ee : exp) {
            if (ee.isTriple()) {
                if (isConnected(ee.getTriple())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConnect(Exp exp) {
        return intersect(getSubscopeVariables(), exp.getSubscopeVariables());
    }

    boolean intersect(List<Variable> l1, List<Variable> l2) {
        for (Variable var : l1) {
            if (l2.contains(var)) {
                return true;
            }
        }
        return false;
    }
    
    public List<Triple> intersectionTriple(BasicGraphPattern bgp) {
        ArrayList<Triple> list = new ArrayList<>();
        for (Exp exp : this) {
            if (exp.isTriple()) {
                if (bgp.getBody().contains(exp)) {
                    list.add(exp.getTriple());
                }
            }
        }
        return list;
    }
    
    public boolean hasIntersection(List<BasicGraphPattern> list) {
        for (BasicGraphPattern exp : list) {
            if (!intersectionTriple(exp).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // BGP body of service, graph
    public Exp getBodyExp() {
        if (size() > 0) {
            return get(0);
        }
        return this;
    }
    
    public void setBodyExp(Exp exp) {
        set(0, exp);
    }

    /**
     * Variables that are surely bound in this Exp left part of optional &
     * minus, common variables of union branchs It is not the list of all
     * variables
     */
    public List<Variable> getVariables() {
        return getSubscopeVariables();
    }

    void add(Variable var, List<Variable> list) {
        if (!list.contains(var)) {
            list.add(var);
        }
    }

    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        for (Exp exp : this) {
            exp.getVariables(sort, list);
        }
    }

//	public ASTQuery getQuery(){
//		return null;
//	}
    public Exp remove(int n) {
        return body.remove(n);
    }

    public Exp get(int n) {
        return body.get(n);
    }

    public void set(int n, Exp exp) {
        body.set(n, exp);
    }

    public Triple getTriple() {
        return null;
    }

    public Binding getBind() {
        return null;
    }

    public Expression getFilter() {
        return null;
    }

    public ASTQuery getAST() {
        return null;
    }

    public int size() {
        return body.size();
    }

    public boolean validateBlank(ASTQuery ast) {
        return true;
    }

    Bind validate(Bind env, int n) throws QuerySemanticException {
        return env;
    }

    public void append(Exp e) {
        add(e);
    }

    public void append(Expression e) {
        add(e);
    }

    boolean isRegexp(String uri) {
        return uri.indexOf(".*") != -1;
    }

//	void process(ASTQuery aq){
//		aq.setQuery(this);
//	}
    public Exp copy() {
        try {
            Exp exp = getClass().newInstance();
            for (Exp ee : getBody()) {
                exp.add(ee.copy());
            }
            return exp;
        } catch (InstantiationException | IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Exp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }

    void setNegation(boolean b) {
    }

    void setCard(String card) {
    }

    public void setRec(boolean b) {
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        return display(sb);
    }

    Exp myget(int i) {
        if (i >= size()) {
            return null;
        }
        return get(i);
    }

    public ASTBuffer display(ASTBuffer sb) {
        int i = 0;
        for (Exp exp : getBody()) {
            if (exp.isDisplayable() && sb.accept(exp)) {
                // this loop pprint only asserted triples
                // rdf star triples are recursively pprinted by Triple
                // annotations and nested triples are printed by Triple 
                if (i++ > 0) {
                    sb.nl();
                }
                exp.toString(sb);
            }
        }
        return sb;
    }


    // use case: nested rdf star triple are not displayed
    // directly but as subject/object of annotation triple
    // overloaded in Triple
    public boolean isDisplayable() {
        return true;
    }

    boolean displayAsTriple() {
        return DatatypeMap.DISPLAY_AS_TRIPLE;
    }


    public ASTBuffer pretty(ASTBuffer sb) {
        sb.append("{").nlincr();
        display(sb);
        sb.nldecr().append("}");
        return sb;
    }

    public Exp eget(int i) {
        if (this.size() > i) {
            return (Exp) get(i);
        } else {
            return null;
        }
    }

    public boolean isTriple() {
        return false;
    }

    public boolean isRelation() {
        return false;
    }

    public boolean isFilter() {
        return false;
    }

    public boolean isStatement() {
        return isBGP() || isUnion() || isMinus() || isOptional() || isGraph();
    }

    public boolean isOption() {
        return false;
    }

    public boolean isOptional() {
        return false;
    }

    public Optional getOptional() {
        return null;
    }

    public Minus getMinus() {
        return null;
    }

    public Service getService() {
        return null;
    }

    public Union getUnion() {
        return null;
    }
    
    public BasicGraphPattern getBasicGraphPattern(){
        return null;
    }

    public Exist getExist() {
        return null;
    }

    public Values getValuesExp() {
        return null;
    }

    public boolean isAnd() {
        return false;
    }
    
    public boolean isStack() {
        return false;
    }

    public boolean isValues() {
        return false;
    }

    public boolean isBGP() {
        return false;
    }

    public boolean isRDFList() {
        return false;
    }

    public boolean isUnion() {
        return false;
    }

    public boolean isJoin() {
        return false;
    }

    public boolean isMinus() {
        return false;
    }

    public boolean isGraph() {
        return false;
    }

    public boolean isNamedGraph() {
        return isGraph();
    }

    public Source getNamedGraph() {
        return null;
    }

    public boolean isService() {
        return false;
    }

    public boolean isScore() {
        return false;
    }

    public boolean isQuery() {
        return false;
    }

    public boolean isBind() {
        return false;
    }

    public boolean isScope() {
        return false;
    }

    public boolean isNegation() {
        return false;
    }

    public boolean isForall() {
        return false;
    }

    public boolean isIfThenElse() {
        return false;
    }

    public boolean isExist() {
        return false;
    }

    /**
     * This Exp is an option pattern : option (t1 t2 t3) tag t1 as first option
     * triple and t3 as last projection will generate index for these first and
     * last triples for appropriate backtracking
     */
    void setOption(boolean b) {
        Exp exp;
        for (int i = 0; i < size(); i++) {
            exp = eget(i);
            exp.setOption(b);
        }
    }

    public void setFirst(boolean b) {
        if (size() > 0) {
            eget(0).setFirst(b);
        }
    }

    public void setLast(boolean b) {
        if (size() > 0) {
            eget(size() - 1).setLast(b);
        }
    }

    /**
     * validate an AST - collect var for select * - check bind(EXP, VAR) : var
     * is not in scope
     */
    boolean validate(ASTQuery ast) {
        return validate(ast, false);
    }

    /**
     * exist = true means we are in a exists {} or in minus {} In this case, do
     * not collect var for select *
     */
    boolean validate(ASTQuery ast, boolean exist) {
        return true;
    }

    public boolean validateData(ASTQuery ast) {
        for (Exp exp : getBody()) {
            if (!exp.validateData(ast)) {
                return false;
            }
        }
        return true;
    }

    public boolean validateDelete() {
        for (Exp exp : getBody()) {
            if (!exp.validateDelete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Searching filters (localize())
     */
    void visit(ExpressionVisitor v) {
        v.visit(this);
        for (Exp exp : getBody()) {
            exp.visit(v);
        }
    }

    public void walk(Walker walker) {
        //System.out.println("walk stmt: " + this);
        walker.enter(this);
        for (Exp exp : getBody()) {
            exp.walk(walker);
        }
        walker.leave(this);
    }
    
    public boolean hasUndefinedService() {
        for (Exp e : this) {
            if (e.isService() && e.getService().isUndefined()) {
                return  true;
            }
            if (e.hasUndefinedService()) {
                return true;
            }
        }
        return false;
    } 

    Exp expandList() {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        expandList(bgp);
        return bgp;
    }

    void expandList(Exp exp) {
        for (Exp e : this) {
            if (e.isTriple()) {
                exp.add(e);
            } else if (e.isRDFList()) {
                e.expandList(exp);
            }
        }
    }
    
    // return bgp list of RDF list if any
    public List<BasicGraphPattern> getRDFList(List<BasicGraphPattern> list) {
        return new ExtractList().getRDFList(this, list);
    }
    
    public List<BasicGraphPattern> getBGPWithBnodeVariable(List<BasicGraphPattern> list) {
        return new ExtractList().getBGPWithBnodeVariable(this, list);
    }
    
    public List<BasicGraphPattern> getBGPWithBnodeVariable(List<BasicGraphPattern> list, FederateMerge fm) {
        return new ExtractList().getBGPWithBnodeVariable(this, list, fm);
    }
 
}
