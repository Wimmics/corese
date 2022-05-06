package fr.inria.corese.sparql.triple.parser;

import java.util.Hashtable;
import java.util.List;

import fr.inria.corese.sparql.exceptions.QuerySemanticException;

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
 * This class has been created to manage the scope of a blank node.
 * <br>
 *
 * @author Virginie Bottollier
 */
public class BasicGraphPattern extends And {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Hashtable that will contains all the blank nodes of this
     * BasicGraphPattern
     */
    Hashtable<String, Variable> bnodes = null;

    /**
     * Empty Constructor
     */
    public BasicGraphPattern() {
        super();
    }

    public BasicGraphPattern(Exp exp) {
        super(exp);
    }

    public BasicGraphPattern(Exp e1, Exp e2) {
        super(e1, e2);
    }

    /**
     * Creation of a BasicGraphPattern
     */
    public static BasicGraphPattern create() {
        return new BasicGraphPattern();
    }

    public static BasicGraphPattern create(Exp exp) {
        return new BasicGraphPattern(exp);
    }

    public static BasicGraphPattern create(Exp e1, Exp e2) {
        return new BasicGraphPattern(e1, e2);
    }
    
    @Override
    public BasicGraphPattern getBasicGraphPattern() {
        return this;
    }
        
    // @pragma: bgp are sorted and contain same physical objects
    // use case: federated query compiler specific hashmap: bgp -> list uri
    public boolean bgpEqual(BasicGraphPattern bgp) {
        if (size() == bgp.size()) {
            int i = 0;
            for (Exp ee : this) {
                if (ee != bgp.get(i++)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    // not same physical triple but triple are equal
    public boolean equalsTriple(BasicGraphPattern bgp) {
        if (size() == bgp.size()) {
            for (Exp e : this) {
                if (!bgp.getBody().contains(e)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    // use case: federate
    // test if two pairs of triple are similar:
    // same predicate, similar subject/object
    public boolean similarPair(BasicGraphPattern bgp) {
        if (size()!=2 || bgp.size()!=2) {
            return false;
        }
        Triple a1 = get(0).getTriple();
        Triple a2 = get(1).getTriple();
        Triple b1 = bgp.get(0).getTriple();
        Triple b2 = bgp.get(1).getTriple();

        return similar(a1, a2, b1, b2) || similar(a1, a2, b2, b1);
    }
    
    boolean similar(Triple a1, Triple a2, Triple b1, Triple b2) {
        boolean res = similar(a1, b1) && similar (a2, b2);
        if (!res) {
            return false;
        }
        List<Atom> al1 = a1.getElements();
        List<Atom> al2 = a2.getElements();
        List<Atom> bl1 = b1.getElements();
        List<Atom> bl2 = b2.getElements();

        for (int i = 0; i < al1.size(); i++) {
            for (int j = 0; j < al2.size(); j++) {
                res &= similar(al1.get(i), al2.get(j), bl1.get(i), bl2.get(j));
                if (! res) {
                    return false;
                }
            }
        }
        
        // check p p o OR s p s
        for (int i = 0; i < al1.size(); i++) {
            for (int j = i+1; j < al1.size(); j++) {
                res &= similar(al1.get(i), al1.get(j), bl1.get(i), bl1.get(j));
                if (! res) {
                    return false;
                }
            }
        }
        
        for (int i = 0; i < al2.size(); i++) {
            for (int j = i+1; j < al2.size(); j++) {
                res &= similar(al2.get(i), al2.get(j), bl2.get(i), bl2.get(j));
                if (! res) {
                    return false;
                }
            }
        }

        return res;
    }
    
    boolean similar(Atom a1, Atom a2, Atom b1, Atom b2) {
        boolean res = a1.equals(a2) == b1.equals(b2);
        //System.out.println(String.format("%s %s %s %s %s ", res, a1, a2, b1, b2));
        return res;
    }
    
    boolean similar(Triple a, Triple b) {
        return similar(a.getElements(), b.getElements());
    }
    
    boolean similar(List<Atom> l1, List<Atom> l2) {
        int i = 0;
        for (Atom at : l1) {
            if (!similar(at, l2.get(i++))) {
                return false;
            }
        }
        return true;
    } 
    
    boolean similar(Atom a, Atom b) {
        if (a.isVariable()) {
            return b.isVariable();
        }
        if (a.isBlankNode()) {
            return b.isBlankNode();
        }
        return a.equals(b);
    }

    public void addFilter(Expression e) {
        add(e);
    }

    public BasicGraphPattern duplicate() {
        BasicGraphPattern bgp = new BasicGraphPattern();
        for (Exp exp : this) {
            bgp.add(exp);
        }
        return bgp;
    }

    @Override
    public boolean isBGP() {
        return true;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        sb.append("{");
        super.toString(sb);
        sb.append("}");
        return sb;
    }
    
    public ASTBuffer toStringBasic() {
        ASTBuffer sb = new ASTBuffer();
        super.toString(sb);
        return sb;
    }
    
    /**
     * This function add a new blank bode in the hashtable bnodes
     *
     * @param s1 The name of the blank node in the query
     * @param v The new blank node
     */
    public void addBNodes(String s1, Variable v) {
        if (bnodes == null) {
            bnodes = new Hashtable<String, Variable>();
        }
        bnodes.put(s1, v);
    }

    /**
     * This function return the blank node that corresponds to the string s in
     * the BasicGraphPattern part of the query
     *
     * @param s The name of the blank node in the query
     * @return
     */
    public Variable getBNode(String s) {
        if (bnodes == null) {
            return null;
        } else {
            return bnodes.get(s);
        }
    }

    /**
     * To check that inner optional have their variable bound by their embeding
     * BGP. e.g. this is an error because ?X is not bound in first arg of
     * optional but is bound before ?X :name "paul" {?Y :name "george" .
     * OPTIONAL { ?X :email ?Z } } env is variables already bound (before this
     * BGP) return variables bound by this BGP
     */
    @Override
    Bind validate(Bind env, int n) throws QuerySemanticException {
        check();
        // var bound by this BGP (including sub BGP)
        Bind local = new Bind(),
                // global env + local:
                glocal = new Bind(), curGlocal = null, prevGlocal;
        glocal = glocal.merge(env);
        for (int i = 0; i < size(); i++) {
            Exp exp = get(i);
            prevGlocal = curGlocal;
            // local copy of glocal for check() optional below
            curGlocal = new Bind();
            curGlocal = curGlocal.merge(glocal);

            Bind bind = exp.validate(glocal, n);

            if (exp.isOption() && prevGlocal != null) {
                // for all var in exp
                // if var !in local prev && var in global env : error			 
                boolean ok = prevGlocal.check(local, bind);
                if (!ok) {
                    // detect an unbound variable 
                    // but if it is in a optional {} it may not be an error
                    // so skip iy by now
//    				Triple triple = 
//    					Triple.create(Term.function(Keyword.SFAIL));
//    				this.add(i, triple);
//    				i++;
                }
            }

            local = local.merge(bind);
            glocal = glocal.merge(bind);
        }
        return local;
    }

    /**
     * Variable in filter should be bound in the BGP
     */
    void check() throws QuerySemanticException {
        if (size() == 1 && get(0).isFilter()) {
            throw new QuerySemanticException("Unbound variable in Filter: "
                    + this.toString());
        }
    }

    /**
     * check bind(EXP, VAR) : var not in scope
     */
    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        boolean ok = true;

        List<Variable> list = ast.getStack();
        // in a new BGP, there is no binding
        ast.newStack();

        for (Exp exp : getBody()) {

            boolean b = exp.validate(ast, exist);
            if (!b) {
                ok = false;
            }

        }

        // old:
        // ast.setStack(list);
        // new: BGP binds its variables at the end
        List<Variable> bgpList = ast.getStack();
        ast.setStack(list);
        ast.addStack(bgpList);

        return ok;
    }

    public boolean validate2(ASTQuery ast, boolean exist) {
        boolean ok = true;
        List<Variable> list = null;

        for (Exp exp : getBody()) {
            if (exp.isBGP()) {
                // in a new BGP, there is no binding
                list = ast.getStack();
                ast.newStack();
            }

            boolean b = exp.validate(ast, exist);
            if (!b) {
                ok = false;
            }

            if (exp.isBGP()) {
                ast.addStack(list);
            }
        }

        return ok;
    }

    /**
     * SPARQL Constraint: Two occurrences of same blank must not be separated by
     * a pattern
     */
    @Override
    public boolean validateBlank(ASTQuery ast) {
        boolean isBefore = true, isTriple = false;
        Table table = new Table();

        for (Exp exp : getBody()) {
            if (exp.isFilter()) {
            } else if (exp.isTriple()) {
                Triple t = exp.getTriple();
                isTriple = true;
                if (isBefore) {
                    table.addBlank(t);
                } else {
                    Atom b = table.contains(t);
                    if (b != null) {
                        ast.addErrorMessage(Message.BNODE_SCOPE, b, this);
                        ast.setCorrect(false);
                        return false;
                    }
                }
            } else if (exp.isRDFList()) {
                // OK
            } else if (isTriple) {
                isBefore = false;
            }
        }

        return true;
    }

    class Table extends Hashtable<String, String> {

        void put(String s) {
            put(s, s);
        }

        Atom contains(Triple t) {
            if (t.getSubject().isBlankNode()) {
                if (containsKey(t.getSubject().getLabel())) {
                    return t.getSubject();
                }
            }
            if (t.getObject().isBlankNode()) {
                if (containsKey(t.getObject().getLabel())) {
                    return t.getObject();
                }
            }
            return null;
        }

        void addBlank(Triple t) {
            if (t.getSubject().isBlankNode()) {
                put(t.getSubject().getLabel());
            }
            if (t.getObject().isBlankNode()) {
                put(t.getObject().getLabel());
            }
        }

    }

    public Exp union() {
        if (getBody().size() < 2) {
            return this;
        }

        Exp exp = null;
        for (Exp e : getBody()) {
            if (exp == null) {
                exp = e;
            } else {
                exp = Union.create(exp, e);
            }
        }
        return exp;
    }

}
