package fr.inria.corese.compiler.parser;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;

/**
 * Type Checking.
 *
 * <p>Generate AST query that checks the definition of a property
 * or a class in the ontology
 * Used by transformer
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Checker {

    static String VAR = "?class";
    static String GRAPH = "?g";

    static String RDFTYPE = "rdf:type";
    static String RDFSSUBCLASSOF = "rdfs:subClassOf";
    static String RDFSCLASS = "rdfs:Class";

    static String RDFPROPERTY = "rdf:Property";
    static String RDFSDOMAIN = "rdfs:domain";
    static String RDFSRANGE = "rdfs:range";
    static String RDFSSUBPROPERTY = "rdfs:subPropertyOf";

    static String OWLINVERSEOF = "owl:inverseOf";
    static String OWLSYMMETRIC = "owl:SymmetricProperty";
    static String OWLTRANSITIVE = "owl:TransitiveProperty";


    // Lousy reference to kgraph entailment graph
    static String ENTAIL = "kg:entailment";
    static String NEQ = "!=";

    // the AST of the query to be type checked
    ASTQuery ast;


    Checker(ASTQuery a) {
        ast = a;
    }


    /**
     * Type check property and class definition of the triple.
     */
    ASTQuery check(Triple triple) {
        if (triple.isType()) {
            // ?x rdf:type foaf:Person
            if (triple.getArg(1).isConstant()) {
                return checkClass(triple);
            }
        } else if (triple.getVariable() == null) {
            // ?x foaf:name ?n
            return checkProperty(triple);
        }

        return null;
    }

    /**
     * ?x rdf:type foaf:Person.
     *
     * <p>check:
     * foaf:Person rdf:type rdfs:Class
     * foaf:Person rdfs:subClassOf ?sup
     * ?sub rdfs:subClassOf foaf:Person
     */
    ASTQuery checkClass(Triple triple) {

        ASTQuery aa = ASTQuery.create();
        aa.setNSM(ast.getNSM());
        BasicGraphPattern bgp = BasicGraphPattern.create();
        aa.setBody(bgp);
        aa.setAsk(true);

        Atom c = triple.getArg(1);
        Variable var = Variable.create(VAR);

        Triple t1 = Triple.create(c, aa.createQName(RDFTYPE), aa.createQName(RDFSCLASS));
        Triple t2 = Triple.create(c, aa.createQName(RDFSSUBCLASSOF), var);
        Triple t3 = Triple.create(var, aa.createQName(RDFSSUBCLASSOF), c);

        Union exp = Union.create(t1, t2);
        exp = Union.create(exp, t3);

        bgp.add(exp);

        return aa;
    }


    ASTQuery checkProperty(Triple triple) {

        ASTQuery aa = ASTQuery.create();
        aa.setNSM(ast.getNSM());
        aa.setAsk(true);

        Atom p = triple.getProperty();

        // check <p> rdf:type rdf:Property
        // PB: it may be infered in kg:entailment, hence not really defined in ontology
        Variable gg = Variable.create(GRAPH);
        Variable var = Variable.create(VAR);

        Constant type = aa.createQName(RDFTYPE);
        Term term = Term.create(NEQ, gg, aa.createQName(ENTAIL));

        Triple t1 = Triple.create(p, type, aa.createQName(RDFPROPERTY));
        BasicGraphPattern pat = BasicGraphPattern.create();
        pat.add(t1);
        pat.addFilter(term);
        Source gp = Source.create(gg, pat);

        Triple t2 = Triple.create(p, aa.createQName(RDFSDOMAIN), var);
        Triple t3 = Triple.create(p, aa.createQName(RDFSRANGE), var);

        Triple t4 = Triple.create(p, type, aa.createQName(OWLSYMMETRIC));
        Triple t5 = Triple.create(p, type, aa.createQName(OWLTRANSITIVE));

        Triple t6 = Triple.create(p, aa.createQName(OWLINVERSEOF), var);
        Triple t7 = Triple.create(var, aa.createQName(OWLINVERSEOF), p);

        Triple t8 = Triple.create(p, aa.createQName(RDFSSUBPROPERTY), var);
        Triple t9 = Triple.create(var, aa.createQName(RDFSSUBPROPERTY), p);

        BasicGraphPattern bgp = BasicGraphPattern.create();
        bgp.add(gp);
        bgp.add(t2);
        bgp.add(t3);
        bgp.add(t4);
        bgp.add(t5);
        bgp.add(t6);
        bgp.add(t7);
        bgp.add(t8);
        bgp.add(t9);

        Exp exp = bgp.union();
        bgp = BasicGraphPattern.create();
        bgp.add(exp);
        aa.setBody(exp);

        return aa;
    }


}
