package fr.inria.corese.inteGraalImpl.dataManager;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.AtomImpl;
import fr.boreal.storage.builder.StorageBuilder;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.storage.inteGraal.ConvertInteGraalCorese;
import fr.inria.corese.storage.inteGraal.InteGraalDataManager;

public class InteGraalDataManagerTest {

        // Factbase
        private FactBase fb_memory;

        // Subjects Graal
        private Term edithPiaf_graal;
        private Term george_brassens_graal;

        // Predicate Graal
        private Predicate isa_graal;
        private Predicate first_name_graal;

        // Objects Corese
        private Term singer_graal;
        private Term edith_literral_graal;

        // Contexts Graal
        private Term default_context_graal;
        private Term context1_graal;
        private Term context2_graal;
        private Term context3_graal;

        // Statements Graal
        private Atom statement_0_graal;
        private Atom statement_1_graal;
        private Atom statement_2_graal;
        private Atom statement_3_graal;
        private Atom statement_bonus_graal;
        private Atom statement_bonus_loop_graal;

        // Subjects Corese
        private Node edith_piaf_corese;
        private Node george_brassens_corese;

        // Predicate Corese
        private Node isa_corese;
        private Node first_name_corese;

        // Objects Corese
        private Node singer_corese;
        private Node edith_literral_corese;

        // Contexts Corese
        private Node default_context_corese;
        private Node context1_corese;
        private Node context2_corese;
        private Node context3_corese;

        // Statements Corese
        private Edge statement_0_corese;
        private Edge statement_1_corese;
        private Edge statement_2_corese;
        private Edge statement_3_corese;
        private Edge statement_bonus_corese;
        private Edge statement_bonus_loop_corese;

        @Before
        public void init() {

                String ex = "http://example.org/";
                String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

                // Factories
                TermFactory termFactory = SameObjectTermFactory.instance();
                PredicateFactory predicateFactory = SameObjectPredicateFactory.instance();

                // Terms
                this.edithPiaf_graal = termFactory.createOrGetConstant(ex + "EdithPiaf");
                this.singer_graal = termFactory.createOrGetConstant(ex + "Singer");
                this.edith_literral_graal = termFactory.createOrGetLiteral("Édith^^" + RDF.xsdstring);
                this.george_brassens_graal = termFactory.createOrGetConstant(ex + "GeorgeBrassens");
                this.default_context_graal = termFactory.createOrGetConstant("default");
                this.context1_graal = termFactory.createOrGetConstant("context1");
                this.context2_graal = termFactory.createOrGetConstant("context2");
                this.context3_graal = termFactory.createOrGetConstant("context3");

                // Predicates
                this.isa_graal = predicateFactory.createOrGetPredicate(rdf + "type", 3);
                this.first_name_graal = predicateFactory.createOrGetPredicate(ex + "firstName", 3);

                // Atoms
                this.statement_0_graal = new AtomImpl(isa_graal, edithPiaf_graal, singer_graal, default_context_graal);
                this.statement_1_graal = new AtomImpl(first_name_graal, edithPiaf_graal, edith_literral_graal,
                                context1_graal);
                this.statement_2_graal = new AtomImpl(first_name_graal, edithPiaf_graal, edith_literral_graal,
                                context2_graal);
                this.statement_3_graal = new AtomImpl(first_name_graal, edithPiaf_graal, edith_literral_graal,
                                context3_graal);
                this.statement_bonus_graal = new AtomImpl(isa_graal, george_brassens_graal, singer_graal,
                                context3_graal);
                this.statement_bonus_loop_graal = new AtomImpl(isa_graal, george_brassens_graal, george_brassens_graal,
                                context3_graal);

                // FactBase
                this.fb_memory = StorageBuilder.getSimpleInMemoryGraphStore();
                fb_memory.addAll(List.of(statement_0_graal, statement_1_graal, statement_2_graal, statement_3_graal));

                // Subjects Corese
                this.edith_piaf_corese = ConvertInteGraalCorese.inteGraalTermToCoreseNode(edithPiaf_graal);
                this.george_brassens_corese = ConvertInteGraalCorese.inteGraalTermToCoreseNode(george_brassens_graal);

                // Predicates Corese
                this.isa_corese = ConvertInteGraalCorese.inteGraalPredicateToCoreseNode(isa_graal);
                this.first_name_corese = ConvertInteGraalCorese.inteGraalPredicateToCoreseNode(first_name_graal);

                // Objects Corese
                this.singer_corese = ConvertInteGraalCorese.inteGraalTermToCoreseNode(singer_graal);
                this.edith_literral_corese = ConvertInteGraalCorese.inteGraalTermToCoreseNode(edith_literral_graal);

                // Contexts Corese
                this.default_context_corese = ConvertInteGraalCorese
                                .inteGraalContextToCoreseContext(default_context_graal);
                this.context1_corese = ConvertInteGraalCorese.inteGraalContextToCoreseContext(context1_graal);
                this.context2_corese = ConvertInteGraalCorese.inteGraalContextToCoreseContext(context2_graal);
                this.context3_corese = ConvertInteGraalCorese.inteGraalContextToCoreseContext(context3_graal);

                // Statements Corese
                this.statement_0_corese = ConvertInteGraalCorese.atomToEdge(statement_0_graal);
                this.statement_1_corese = ConvertInteGraalCorese.atomToEdge(statement_1_graal);
                this.statement_2_corese = ConvertInteGraalCorese.atomToEdge(statement_2_graal);
                this.statement_3_corese = ConvertInteGraalCorese.atomToEdge(statement_3_graal);
                this.statement_bonus_corese = ConvertInteGraalCorese.atomToEdge(statement_bonus_graal);
                this.statement_bonus_loop_corese = ConvertInteGraalCorese.atomToEdge(statement_bonus_loop_graal);
        }

        private boolean containsCompareTriple(Edge edge, List<Edge> results) {

                Edge edge_triple = EdgeImpl.create(this.default_context_corese, edge.getSubjectNode(),
                                edge.getPropertyNode(), edge.getObjectNode());
                List<Edge> results_triples = results.stream().map(el -> {
                        el.setGraph(this.default_context_corese);
                        return el;
                }).collect(Collectors.toList());

                return results_triples.contains(edge_triple);
        }

        @Test
        public void graphSize() {
                InteGraalDataManager igdm;

                igdm = new InteGraalDataManager();
                assertEquals(0, igdm.graphSize());

                igdm = new InteGraalDataManager(this.fb_memory);
                assertEquals(4, igdm.graphSize());
        }

        @Test
        public void countEdges() {
                InteGraalDataManager data_manager;

                data_manager = new InteGraalDataManager();
                assertEquals(0, data_manager.countEdges(this.first_name_corese));
                assertEquals(0, data_manager.countEdges(this.isa_corese));
                assertEquals(0, data_manager.countEdges(null));

                data_manager = new InteGraalDataManager(this.fb_memory);
                assertEquals(3, data_manager.countEdges(this.first_name_corese));
                assertEquals(1, data_manager.countEdges(this.isa_corese));
                assertEquals(4, data_manager.countEdges(null));
        }

        @Test
        public void getEdgesAll() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All edges
                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, null);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);

                assertEquals(2, result.size());
                assertEquals(true, containsCompareTriple(this.statement_0_corese, result));
                assertEquals(true, containsCompareTriple(this.statement_1_corese, result));
        }

        @Test
        public void getEdgesDefault() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All edges of default context
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.default_context_corese);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(1, result.size());
                assertEquals(true, containsCompareTriple(this.statement_0_corese, result));
        }

        @Test
        public void getEdgesIgnoreNull() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All edges (with ignore null values)
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(null);
                contexts.add(null);
                contexts.add(null);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);

                assertEquals(2, result.size());
                assertEquals(true, containsCompareTriple(this.statement_0_corese, result));
                assertEquals(true, containsCompareTriple(this.statement_1_corese, result));
        }

        @Test
        public void getEdgesIgnoreNull2() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All edges of context 1 (with a ignore null value)
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(null);
                contexts.add(this.context1_corese);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);

                assertEquals(1, result.size());
                assertEquals(true, containsCompareTriple(this.statement_1_corese, result));
        }

        @Test
        public void getEdgesOneContext() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All edges of context 1
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);

                assertEquals(1, result.size());
                assertEquals(result.get(0).getGraph(), this.context1_corese);

                // All edges of context 1 (with a ignore null value)
                contexts = new ArrayList<>();
                contexts.add(null);
                contexts.add(this.context1_corese);

                iterable = data_manager.getEdges(null, null, null, contexts);
                result = new ArrayList<>();
                iterable.forEach(result::add);

                assertEquals(1, result.size());
                assertEquals(this.context1_corese, result.get(0).getGraph());
        }

        @Test
        public void predicates() {
                Iterable<Node> iterable;
                List<Node> result;

                this.fb_memory.addAll(List.of(this.statement_bonus_graal));
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All contexts
                iterable = data_manager.predicates(null);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(2, result.size());
                assertEquals(true, result.contains(this.isa_corese));
                assertEquals(true, result.contains(this.first_name_corese));

                // Context 1
                iterable = data_manager.predicates(this.context1_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(1, result.size());
                assertEquals(true, result.contains(this.first_name_corese));

                // Default context
                iterable = data_manager.predicates(this.default_context_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(1, result.size());
                assertEquals(true, result.contains(this.isa_corese));
        }

        @Test
        public void getNodes() {
                Iterable<Node> iterable;
                List<Node> result;

                this.fb_memory.addAll(List.of(this.statement_bonus_graal));
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // All contexts
                iterable = data_manager.getNodes(null);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(4, result.size());
                assertEquals(true, result.contains(this.edith_piaf_corese));
                assertEquals(true, result.contains(this.george_brassens_corese));
                assertEquals(true, result.contains(this.singer_corese));
                assertEquals(true, result.contains(this.edith_literral_corese));

                // Context 1
                iterable = data_manager.getNodes(this.context1_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(2, result.size());
                assertEquals(true, result.contains(this.edith_piaf_corese));
                assertEquals(true, result.contains(this.edith_literral_corese));

                // Element of kg:default
                iterable = data_manager.getNodes(default_context_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(2, result.size());
                assertEquals(true, result.contains(this.edith_piaf_corese));
                assertEquals(true, result.contains(this.singer_corese));

                // No duplication
                this.fb_memory.addAll(List.of(this.statement_bonus_loop_graal));

                iterable = data_manager.getNodes(this.context3_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(4, result.size());
                assertEquals(true, result.contains(this.edith_piaf_corese));
                assertEquals(true, result.contains(this.george_brassens_corese));
                assertEquals(true, result.contains(this.singer_corese));
                assertEquals(true, result.contains(this.edith_literral_corese));
        }

        @Test
        public void contexts() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                Iterable<Node> iterable = data_manager.contexts();

                List<Node> result = new ArrayList<>();
                iterable.forEach(result::add);

                assertEquals(4, result.size());
                assertEquals(true, result.contains(this.context1_corese));
                assertEquals(true, result.contains(this.context2_corese));
                assertEquals(true, result.contains(this.context3_corese));
                assertEquals(true, result.contains(this.default_context_corese));
        }

        @Test
        public void insertSPO() {
                InteGraalDataManager data_manager = new InteGraalDataManager();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);

                data_manager.insert(this.edith_piaf_corese, this.first_name_corese,
                                this.edith_literral_corese, contexts);

                assertEquals(2, data_manager.graphSize());
                assertEquals(false, data_manager.contains(this.statement_0_corese));
                assertEquals(true, data_manager.contains(this.statement_1_corese));
                assertEquals(true, data_manager.contains(this.statement_2_corese));
                assertEquals(false, data_manager.contains(this.statement_3_corese));
        }

        @Test
        public void insertSPODefault() {
                InteGraalDataManager data_manager = new InteGraalDataManager();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.default_context_corese);

                Iterable<Edge> results = data_manager.insert(
                                this.edith_piaf_corese,
                                this.isa_corese,
                                this.singer_corese,
                                contexts);

                assertEquals(1, data_manager.graphSize());
                assertEquals(results.iterator().next().getGraph(), this.default_context_corese);
                assertEquals(true, data_manager.contains(this.edith_piaf_corese, this.isa_corese, this.singer_corese,
                                List.of(this.default_context_corese)));
                assertEquals(false, data_manager.contains(this.edith_piaf_corese, this.isa_corese, this.singer_corese,
                                List.of(this.context1_corese)));
                assertEquals(false, data_manager.contains(this.edith_piaf_corese, this.isa_corese, this.singer_corese,
                                List.of(this.context2_corese)));
                assertEquals(false, data_manager.contains(this.edith_piaf_corese, this.isa_corese, this.singer_corese,
                                List.of(this.context3_corese)));
        }

        @Test(expected = UnsupportedOperationException.class)
        public void insertSPOError1() {
                InteGraalDataManager data_manager = new InteGraalDataManager();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);

                data_manager.insert(null, this.isa_corese, this.singer_corese, contexts);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void insertSPOError2() {
                InteGraalDataManager data_manager = new InteGraalDataManager();
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);
                contexts.add(null);

                data_manager.insert(this.edith_piaf_corese, this.isa_corese, this.singer_corese,
                                contexts);
        }

        @Test
        public void insertEdgeEdge() {
                InteGraalDataManager data_manager = new InteGraalDataManager(this.fb_memory);

                // Insert new statement
                assertEquals(false, data_manager.contains(this.statement_bonus_corese));

                Edge iterable = data_manager.insert(this.statement_bonus_corese);
                assertEquals(iterable, this.statement_bonus_corese);
                assertEquals(true, data_manager.contains(this.statement_bonus_corese));

                // Try insert statement already in graph
                iterable = data_manager.insert(this.statement_bonus_corese);
                assertEquals(null, iterable);
        }
}
