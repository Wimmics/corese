package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.parser.VariableScope;
import java.util.List;

/**
 * Manage criteria to evaluate which bgp deserve to be first bgp
 * criteria: 
 * triple with constant subject/object
 * filter
 * triple with predicate uri vs variable
 * triple with domain predicate vs system predicate (e.g. rdfs:label)
 */
public class SorterResult { 
    // ?s rdfs:label ?l (better than ?s ?p ?l)
    public static  double SYSTEM_PREDICATE = 0.25;
    // ?s rdf:type dbo:Country
    public static  double CONSTANT_TRIPLE_TYPE = 0.5;
    // not rdfs: rdf: owl: skos:, for example: ?s vs:country-name ?o
    public static  double DOMAIN_PREDICATE = 0.75;
    // filter: = regex strstarts contains   -- see SelectorFilter
    public static  double FILTER = 1;
    // ?s ?p "arboriculture"@fr
    public static  double CONSTANT_TRIPLE_VARIABLE = 1.5;
    // ?s rdfs:label "arboriculture"@fr
    public static  double CONSTANT_TRIPLE = 1.5;

    // triple with predicate URI
    private Triple triple;
    private Triple constantTriple;
    private int nbFilter = 0;
    SelectorFilter selector;
    
    SorterResult(SelectorFilter selector) {
        this.selector = selector;
    }

    public Triple getTriple() {
        return triple;
    }

    public void setTriple(Triple triple) {
        this.triple = triple;
    }

    void addTriple(Triple t) {
        if (getTriple() == null) {
            setTriple(t);
        } else if (subScore(t) > subScore(getTriple())) {
            // prefer local predicate vs system predicate
            setTriple(t);
        }
    }

    boolean isSystem(Triple t) {
        return NSManager.nsm().isSystemURI(
                t.getPredicate().getLabel());
    }

    public Triple getConstantTriple() {
        return constantTriple;
    }

    boolean hasConstantTriple() {
        return getConstantTriple() != null;
    }

    public void setConstantTriple(Triple constantTriple) {
        this.constantTriple = constantTriple;
    }

    public void addConstantTriple(Triple t) {
        if (hasConstantTriple()) {
            if (getConstantTriple().getPredicate().isVariable() && ! t.getPredicate().isVariable()) {
                setConstantTriple(t);
            }
        }
        else {
            setConstantTriple(t);
        }
    }
    
    void submit(Triple t) {
        if (t.isConstantNode()) {
            // triple with constant subject/object
            addConstantTriple(t);
        }
        else if (t.getPredicate().isConstant()) {
            // predicate URI (not variable)
            addTriple(t);
        }
    }
    
    void submit(Expression exp) {
        List<Variable> varList = exp.getVariables(VariableScope.filterscope());
        // require filter with constant, not ?x = ?y
        // @todo: isBlank() isURI() are not so good
        if (varList.size() <= 1 && accept(exp)) {
            incrFilter();
        }
    }
    
    // accept specific filter such as: regex, =
    boolean accept(Expression exp) {
        return selector.accept(exp);
    }

    public int nbFilter() {
        return nbFilter;
    }

    public void setNbFilter(int nbFilter) {
        this.nbFilter = nbFilter;
    }

    void incrFilter() {
        nbFilter += 1;
    }

    boolean hasFilter() {
        return nbFilter() > 0;
    }

    double score() {
        double score = 0;

        if (hasConstantTriple()) {
            score += score(getConstantTriple());
        }

        if (hasFilter()) {
            score += FILTER;
        }

        if (score == 0) {
            score += subScore();
        }
        return score;
    }

    // constant triple
    double score(Triple t) {
        if (t.isType()) {
            // s rdf:type cst
            return CONSTANT_TRIPLE_TYPE;
        }
        // s p cst better than filter
        if (t.getPredicate().isVariable()) {
            return CONSTANT_TRIPLE_VARIABLE;
        }
        return CONSTANT_TRIPLE;
    }
    
    // triple with predicate uri (not a variable)
    double subScore() {
        if (getTriple() != null) {
            return subScore(getTriple());
        }
        return 0;
    }

    // triple with predicate uri (better than predicate variable)
    double subScore(Triple t) {
        if (isSystem(t)) {
            // rdfs:label is common
            return SYSTEM_PREDICATE;
        } else {
            // domain specific predicate is better than system predicate such as rdfs:label
            return DOMAIN_PREDICATE;
        }
    }

    void union(SorterResult r1, SorterResult r2) {
        if (r1.getConstantTriple() != null && r2.getConstantTriple() != null) {
            addConstantTriple(min(r1.getConstantTriple(), r2.getConstantTriple()));
        }
        if (r1.getTriple() != null && r2.getTriple() != null) {
            addTriple(subMin(r1.getTriple(), r2.getTriple()));
        }
        setNbFilter(nbFilter() + Math.min(r1.nbFilter(), r2.nbFilter()));
    }
    
    Triple min(Triple t1, Triple t2) {
        if (score(t1) < score(t2)) {
            return t1;
        }
        return t2;
    }
    
    Triple subMin(Triple t1, Triple t2) {
        if (subScore(t1) < subScore(t2)) {
            return t1;
        }
        return t2;
    }

}
