package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.api.ElementClause;
import fr.inria.corese.sparql.triple.api.Walker;

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
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class Atom extends Expression implements ElementClause {

    public static boolean display = false;
    boolean isone = false;
    boolean isall = false;
    boolean isdirect = false;
    int star;
    // use case: parser generate triple reference for s p o {| q v |}
    // object o contain triple reference for parsing purpose
    private Atom tripleReference;
    // BGP with annotation triple list (t q v)
    private Exp annotation;
    // this atom is a triple reference:
    private Triple triple;
    private boolean btriple = false;

    public Atom() {
    }

    public Atom(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object at) {
        return false;
    }
    
    public boolean displayAsTriple() {
        return DatatypeMap.DISPLAY_AS_TRIPLE;
    }
    
    public String toNestedTriple() {
        return getTriple().toNestedTriple();
    }
    
    public String toTriple() {
        return getTriple().toTriple();
    }
    
    @Override
    public Variable getVariable() {
        return null;
    }

    @Override
    public Expression getExpression() {
        return null;
    }

    @Override
    boolean isAtom() {
        return true;
    }

    public boolean isTriple() {
        return btriple;
    }
    
    public boolean isTripleWithTriple() {
        return isTriple() && getTriple()!=null;
    }
    

    public void setTriple(boolean b) {
        btriple = b;
    }

    public void setTriple(Triple t) {
        triple = t;
    }

    public Triple getTriple() {
        return triple;
    }

    /**
     * this = Atom(isTriple()==true; triple=triple(?s :p ?o this)) eval quoted
     * triple as function call triple(?s, :p, ?o) this atom is either Constant
     * or Variable
     */
    IDatatype triple(Computer eval, fr.inria.corese.sparql.triple.function.term.Binding b, Environment env, Producer p)
            throws EngineException {
        if (getTriple() == null) {
            return null;
        }
        IDatatype sub = getTriple().getSubject().eval(eval, b, env, p);
        IDatatype pred = getTriple().getPredicate().eval(eval, b, env, p);
        IDatatype obj = getTriple().getObject().eval(eval, b, env, p);

        if (sub == null || pred == null || obj == null) {
            return null;
        }

        return eval.getGraphProcessor().triple(env, p, sub, pred, obj);
    }

    public boolean isResource() {
        return false;
    }

    public boolean isIsall() {
        return isall;
    }

    public void setIsall(boolean isall) {
        this.isall = isall;
    }

    public boolean isIsdirect() {
        return isdirect;
    }

    public void setIsdirect(boolean isdirect) {
        this.isdirect = isdirect;
    }

    public boolean isIsone() {
        return isone;
    }

    public void setIsone(boolean isone) {
        this.isone = isone;
    }

    public int getStar() {
        return star;
    }

    public void setPath(int star) {
        this.star = star;
    }

    @Override
    public Atom getAtom() {
        return this;
    }

    @Override
    public Constant getConstant() {
        return null;
    }

    @Override
    public IDatatype getDatatypeValue() {
        return null;
    }

    public Atom getElement() {
        return this;
    }

    boolean validateData(ASTQuery ast) {
        if (isBlankOrBlankNode()) {
            ast.record(this);
        }
        return true;
    }
    
    // bnode or bnode as variable
    public boolean isBlankOrBlankNode() {
        return isBlank() || isBlankNode();
    }

    @Override
    public void walk(Walker walker) {
        walker.enter(this);
        walker.leave(this);
    }

    public Atom getTripleReference() {
        return tripleReference;
    }

    public void setTripleReference(Atom tripleReference) {
        this.tripleReference = tripleReference;
    }

    public Exp getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Exp annotation) {
        this.annotation = annotation;
    }

}
