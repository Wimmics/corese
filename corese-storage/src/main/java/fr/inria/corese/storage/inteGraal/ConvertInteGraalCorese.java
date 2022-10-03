package fr.inria.corese.storage.inteGraal;

import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.AtomImpl;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.storage.inteGraal.convertDatatype.CoreseDatatypeToInteGraal;
import fr.inria.corese.storage.inteGraal.convertDatatype.InteGraalToCoreseDatatype;

public class ConvertInteGraalCorese {

    public final Node corese_default_context = DatatypeMap.createResource(ExpType.DEFAULT_GRAPH);
    public final Term integraal_default_context;

    private CoreseDatatypeToInteGraal converter_ci;

    /**
     * Construct a converter Corese InteGraal.
     * 
     * @param tf the term factory.
     * @param pf the predicate factory.
     */
    public ConvertInteGraalCorese(TermFactory tf, PredicateFactory pf) {
        this.converter_ci = new CoreseDatatypeToInteGraal(tf, pf);
        this.integraal_default_context = tf.createOrGetConstant("default");
    }

    public ConvertInteGraalCorese() {
        this(SameObjectTermFactory.instance(), SameObjectPredicateFactory.instance());
    }

    /******************************
     * Node : Corese to InteGraal *
     ******************************/

    /**
     * Convert a Corese node to a InteGraal Term .
     * 
     * @param corese_node Corese node to convert.
     * @return Equivalent InteGraal Term.
     */
    public Term coreseNodeToInteGraalTerm(Node corese_node) {
        if (corese_node == null) {
            return this.converter_ci.convertVariable();
        }
        return this.converter_ci.convert(corese_node.getDatatypeValue());
    }

    /**
     * Convert a Corese node to a InteGraal Predicate .
     * 
     * @param corese_node Corese node to convert, can not be null.
     * @return Equivalent InteGraal Predicate.
     */
    public Predicate coreseNodeToInteGraalPredicate(Node corese_node) {
        return this.converter_ci.convertPredicate(corese_node.getDatatypeValue());
    }

    /*********************************
     * Context : Corese to inteGraal *
     *********************************/

    /**
     * Convert a Corese context to a InteGraal Term.
     * 
     * @param corese_context Corese context to convert.
     * @return Equivalent InteGraal Term.
     */
    public Term coreseContextToIntegraalTerm(Node corese_context) {

        if (corese_context != null && corese_context.equals(corese_default_context)) {
            return integraal_default_context;
        } else {
            return this.coreseNodeToInteGraalTerm(corese_context);
        }
    }

    /******************************
     * Quad : Corese to InteGraal *
     ******************************/

    /**
     * Convert Corese edge to equivalent inteGraal Atom.
     * 
     * @param edge Corese edge to convert.
     * @return Equivalent inteGraal Atom.
     */
    public Atom edgeToAtom(Edge edge) {
        Term subject_graal = this.coreseNodeToInteGraalTerm(edge.getNode(0));
        Predicate predicate_graal = this.coreseNodeToInteGraalPredicate(edge.getEdgeNode());
        Term object_graal = this.coreseNodeToInteGraalTerm(edge.getNode(1));
        Term context_graal = this.coreseContextToIntegraalTerm(edge.getGraph());

        return new AtomImpl(predicate_graal, subject_graal, object_graal, context_graal);
    }

    /******************************
     * Node : InteGraal to Corese *
     ******************************/

    /**
     * Convert a InteGraal Term to Corese node.
     * 
     * @param integraal_term InteGraal Term to convert.
     * @return Equivalent Corese Node.
     */
    public Node inteGraalTermToCoreseNode(Term integraal_term) {
        return InteGraalToCoreseDatatype.convert(integraal_term);
    }

    /**
     * Convert a InteGraal Predicate to a Corese Node .
     * 
     * @param integraal_predicate InteGraal Predicate to convert.
     * @return Equivalent Corese Node.
     */
    public Node inteGraalPredicateToCoreseNode(Predicate integraal_predicate) {
        return InteGraalToCoreseDatatype.convert(integraal_predicate);
    }

    /*********************************
     * Context : InteGraal to Corese *
     *********************************/

    /**
     * Convert a InteGraal context to a Corese context.
     * 
     * @param integraal_context InteGraal context to convert.
     * @return Equivalent Corese context.
     */
    public Node inteGraalContextToCoreseContext(Term integraal_context) {
        if (integraal_context.equals(integraal_default_context)) {
            return corese_default_context;
        } else {
            return inteGraalTermToCoreseNode(integraal_context);
        }
    }

    /******************************
     * Quad : InteGraal to Corese *
     ******************************/

    /**
     * Convert inteGraal Atom to equivalent Corese Edge.
     * 
     * @param atom inteGraal Atom to convert.
     * @return Equivalent Corese Edge.
     */
    public Edge atomToEdge(Atom atom) {
        Node subject_corese = this.inteGraalTermToCoreseNode(atom.getTerm(0));
        Node predicate_corese = this.inteGraalPredicateToCoreseNode(atom.getPredicate());
        Node object_corese = this.inteGraalTermToCoreseNode(atom.getTerm(1));
        Node context_corese = this.inteGraalContextToCoreseContext(atom.getTerm(2));

        return EdgeImpl.create(context_corese, subject_corese, predicate_corese, object_corese);
    }

}
