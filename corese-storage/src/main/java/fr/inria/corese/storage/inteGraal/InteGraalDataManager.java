package fr.inria.corese.storage.inteGraal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;

import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.impl.AtomImpl;
import fr.boreal.storage.builder.StorageBuilder;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.Graph.TreeNode;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

public class InteGraalDataManager implements DataManager {

    private FactBase inteGraalFactBase;

    private final int CONTEXT_POS = 2;

    /****************
     * Constructors *
     ****************/

    public InteGraalDataManager() {
        this.inteGraalFactBase = StorageBuilder.defaultStorage();
    }

    public InteGraalDataManager(FactBase inteGraalFactBase) {
        this.inteGraalFactBase = inteGraalFactBase;
    }

    /*********
     * Count *
     *********/

    @Override
    public int graphSize() {
        return (int) this.inteGraalFactBase.size();
    }

    @Override
    public int countEdges(Node corese_predicate) {
        Iterable<Node> predicates;

        if (corese_predicate == null) {
            predicates = this.predicates(null);
        } else {
            predicates = List.of(corese_predicate);
        }

        int size = 0;
        for (Node predicate : predicates) {
            Predicate graal_predicate = ConvertInteGraalCorese.coreseNodeToInteGraalPredicate(predicate);
            Iterator<Atom> it = this.inteGraalFactBase.getAtomsByPredicate(graal_predicate);
            size += Iterators.size(it);
        }
        return size;
    }

    /************
     * GetEdges *
     ************/

    @Override
    public Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {

        // Clear all Null value in contexts
        if (contexts != null) {
            contexts = contexts.stream().filter(x -> x != null).collect(Collectors.toList());
        }

        HashSet<Term> contexts_integraal = new HashSet<>();
        HashSet<Predicate> predicates_integraal = new HashSet<>();

        if (predicate == null) {

            if (contexts == null || contexts.isEmpty()) {
                this.predicates(null).forEach(
                        p -> predicates_integraal.add(ConvertInteGraalCorese.coreseNodeToInteGraalPredicate(p)));
                contexts_integraal.add(ConvertInteGraalCorese.coreseContextToIntegraalTerm(null));
            } else {
                for (Node context : contexts) {
                    this.predicates(context).forEach(
                            p -> predicates_integraal.add(ConvertInteGraalCorese.coreseNodeToInteGraalPredicate(p)));
                    contexts_integraal.add(ConvertInteGraalCorese.coreseContextToIntegraalTerm(context));
                }
            }
        } else {
            predicates_integraal.add(ConvertInteGraalCorese.coreseNodeToInteGraalPredicate(predicate));

            if (contexts == null || contexts.isEmpty()) {
                contexts_integraal.add(ConvertInteGraalCorese.coreseContextToIntegraalTerm(null));
            } else {
                for (Node context : contexts) {
                    contexts_integraal.add(ConvertInteGraalCorese.coreseContextToIntegraalTerm(context));
                }
            }
        }
        HashSet<Atom> atoms_integraal = new HashSet<>();

        Term subject_integraal = ConvertInteGraalCorese.coreseNodeToInteGraalTerm(subject);
        Term object_integraal = ConvertInteGraalCorese.coreseNodeToInteGraalTerm(object);

        for (Predicate predicate_integraal : predicates_integraal) {
            for (Term context_integraal : contexts_integraal) {
                Atom matcher = new AtomImpl(predicate_integraal, subject_integraal, object_integraal,
                        context_integraal);
                Iterator<Atom> matches = this.inteGraalFactBase.match(matcher);

                if (contexts == null || contexts.size() != 1) {
                    Term default_context = ConvertInteGraalCorese.integraal_default_context;
                    while (matches.hasNext()) {
                        Atom a = matches.next();
                        atoms_integraal.add(new AtomImpl(a.getPredicate(), a.getTerm(0), a.getTerm(1),
                                default_context));
                    }

                } else {
                    matches.forEachRemaining(atoms_integraal::add);
                }
            }
        }

        return () -> atoms_integraal.stream().map(a -> ConvertInteGraalCorese.atomToEdge(a)).iterator();
    }

    /*************
     * Get lists *
     *************/

    @Override
    public Iterable<Node> predicates(Node corese_context) {

        // Convert Corese Context to InteGraal Term
        Term integraal_context = ConvertInteGraalCorese.coreseContextToIntegraalTerm(corese_context);

        Function<Predicate, Node> convertIteratorInteGraalPredicateToCoreseNode = new Function<Predicate, Node>() {
            @Override
            public Node apply(Predicate predicate) {
                return ConvertInteGraalCorese.inteGraalPredicateToCoreseNode(predicate);
            }
        };

        com.google.common.base.Predicate<Predicate> filterPredicateByContext = new com.google.common.base.Predicate<Predicate>() {

            @Override
            public boolean apply(Predicate predicate) {
                // Get list of context of a given predicate
                Iterator<Term> contexts = inteGraalFactBase.getTermsByPredicatePosition(predicate, CONTEXT_POS);

                Optional<Term> optional_term = Iterators.tryFind(contexts, (c) -> (c.equals(integraal_context)));

                return optional_term.isPresent();
            }
        };

        Iterator<Predicate> predicates = this.inteGraalFactBase.getPredicates();

        if (corese_context == null) {
            return () -> Iterators.transform(
                    predicates,
                    convertIteratorInteGraalPredicateToCoreseNode);
        } else {

            Iterator<Predicate> filtered_predicate = Iterators.filter(predicates, filterPredicateByContext);
            return () -> Iterators.transform(
                    filtered_predicate,
                    convertIteratorInteGraalPredicateToCoreseNode);
        }
    }

    @Override
    public Iterable<Node> getNodes(Node corese_context) {
        Iterator<Edge> edges = this.getEdges(null, null, null, corese_context == null ? null : List.of(corese_context))
                .iterator();

        TreeNode nodes = new Graph().treeNode();

        while (edges.hasNext()) {
            Edge edge = edges.next();
            nodes.put(edge.getSubjectNode());
            nodes.put(edge.getObjectNode());
        }

        return nodes.values();
    }

    @Override
    public Iterable<Node> contexts() {

        Function<Term, Node> convertIteratorInteGraalContextToCoreseNode = new Function<Term, Node>() {
            @Override
            public Node apply(Term term) {
                return ConvertInteGraalCorese.inteGraalContextToCoreseContext(term);
            }
        };

        Iterator<Predicate> predicates = this.inteGraalFactBase.getPredicates();

        HashSet<Term> result = new HashSet<>();

        while (predicates.hasNext()) {
            Predicate predicate = predicates.next();

            Iterator<Term> integraal_contexts = inteGraalFactBase.getTermsByPredicatePosition(predicate, CONTEXT_POS);
            integraal_contexts.forEachRemaining(result::add);
        }

        return () -> Iterators.transform(result.iterator(), convertIteratorInteGraalContextToCoreseNode);
    }

    /**********
     * Insert *
     **********/

    @Override
    public Iterable<Edge> insert(Node subject, Node predicate, Node object, List<Node> contexts) {
        ArrayList<Edge> added = new ArrayList<>();

        if (subject == null || predicate == null || object == null || contexts == null) {
            throw new UnsupportedOperationException("Incomplete statement");
        }

        for (Node context : contexts) {
            if (context == null) {
                throw new UnsupportedOperationException("Context can't be null");
            }
            added.add(EdgeImpl.create(context, subject, predicate, object));
        }

        boolean changed = this.inteGraalFactBase
                .addAll(added.stream().map(ConvertInteGraalCorese::edgeToAtom).collect(Collectors.toSet()));
        return changed ? added : List.of();
    }

    /*******************
     * Graph operation *
     *******************/

    @Override
    public boolean add(Node source, Node target, boolean silent) {
        long nb_graph_before;
        long nb_graph_after;

        nb_graph_before = this.graphSize();

        // Add Graph
        Iterable<Edge> edges = this.getEdges(null, null, null, List.of(source));

        for (Edge edge : edges) {
            this.insert(edge);
        }

        nb_graph_after = this.graphSize();

        return nb_graph_before != nb_graph_after;
    }

    @Override
    public String toString() {
        return this.inteGraalFactBase.toString();
    }

}
