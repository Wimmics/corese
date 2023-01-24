package fr.inria.corese.jenaImpl.dataManager;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.jena.JenaTdb1DataManager;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.jena.convert.ConvertJenaCorese;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;

public class JenaTdb1DataManagerTest {

        // Subjects Corese
        private Node edith_piaf_node_corese;
        private Node george_brassens_node_corese;

        // Subjects Jena
        private org.apache.jena.graph.Node edith_piaf_node_jena;
        private org.apache.jena.graph.Node george_brassens_node_jena;

        // Properties Corese
        private Node isa_property_corese;
        private Node first_name_property_corese;

        // Properties Jena
        private org.apache.jena.graph.Node isa_property_jena;

        // Objects Corese
        private Node singer_node_corese;
        private Node edith_literal_corese;

        // Objects Jena
        private org.apache.jena.graph.Node singer_node_jena;

        // Contexts Corese
        private Node context1_corese;
        private Node context2_corese;
        private Node context3_corese;
        private Node default_context_corese;

        // Contexts Jena
        private org.apache.jena.graph.Node context1_jena;
        private org.apache.jena.graph.Node context2_jena;
        private org.apache.jena.graph.Node context3_jena;
        private org.apache.jena.graph.Node default_context_jena;

        // Statements Corese
        private Edge statement_0_corese;
        private Edge statement_1_corese;
        private Edge statement_2_corese;
        private Edge statement_3_corese;
        private Edge statement_bonus_corese;

        // Statements Jena
        private Quad statement_0_jena;
        private Quad statement_1_jena;
        private Quad statement_2_jena;
        private Quad statement_3_jena;
        private Quad statement_bonus_jena;

        // Dataset
        private Dataset dataset;

        @Before
        public void init() {

                this.default_context_corese = NodeImpl.create(DatatypeMap.createResource(ExpType.DEFAULT_GRAPH));
                this.default_context_jena = Quad.defaultGraphIRI;

                // Build jenaGraph
                String ex = "http://example.org/";

                // statement zero
                Resource edithPiafNode = ResourceFactory.createResource(ex + "EdithPiaf");
                Property isaProperty = RDF.type;
                Resource singerNode = ResourceFactory.createResource(ex + "Singer");
                Statement statement_0 = ResourceFactory.createStatement(edithPiafNode, isaProperty, singerNode);

                // first, second and third statements
                Property firstNameProperty = ResourceFactory.createProperty(ex, "firstName");
                Literal edithLiteral = ResourceFactory.createStringLiteral("Édith");
                Statement statement_1 = ResourceFactory.createStatement(edithPiafNode, firstNameProperty, edithLiteral);

                // bonus statement
                Resource georgeBrassensNode = ResourceFactory.createResource(ex + "GeorgeBrassens");
                Statement statement_bonus = ResourceFactory.createStatement(georgeBrassensNode, isaProperty,
                                singerNode);

                /////////////////
                // Build graph //
                /////////////////
                this.dataset = DatasetFactory.create();

                Model model_default = dataset.getDefaultModel();
                model_default.add(statement_0);

                String context1 = ex + "context1";
                Model model_context1 = dataset.getNamedModel(context1);
                model_context1.add(statement_1);

                String context2 = ex + "context2";
                Model model_context2 = dataset.getNamedModel(context2);
                model_context2.add(statement_1);

                String context3 = ex + "context3";
                Model model_context3 = dataset.getNamedModel(context3);
                model_context3.add(statement_1);

                /////////////////////////////////////////////////
                // Convert from Jena to Corese format and Save //
                /////////////////////////////////////////////////

                // Subjects Corese
                this.edith_piaf_node_corese = ConvertJenaCorese.JenaNodeToCoreseNode(edithPiafNode.asNode());
                this.george_brassens_node_corese = ConvertJenaCorese.JenaNodeToCoreseNode(georgeBrassensNode.asNode());

                // Subjects Jena
                this.edith_piaf_node_jena = edithPiafNode.asNode();
                this.george_brassens_node_jena = georgeBrassensNode.asNode();

                // Predicates Corese
                this.isa_property_corese = ConvertJenaCorese.JenaNodeToCoreseNode(isaProperty.asNode());
                this.first_name_property_corese = ConvertJenaCorese.JenaNodeToCoreseNode(firstNameProperty.asNode());

                // Predicates Jena
                this.isa_property_jena = isaProperty.asNode();
                firstNameProperty.asNode();

                // Objects Corese
                this.singer_node_corese = ConvertJenaCorese.JenaNodeToCoreseNode(singerNode.asNode());
                this.edith_literal_corese = ConvertJenaCorese.JenaNodeToCoreseNode(edithLiteral.asNode());

                // Objects Jena
                this.singer_node_jena = singerNode.asNode();
                edithLiteral.asNode();

                // Contexts Corese
                this.context1_corese = NodeImpl.create(DatatypeMap.createResource(context1));
                this.context2_corese = NodeImpl.create(DatatypeMap.createResource(context2));
                this.context3_corese = NodeImpl.create(DatatypeMap.createResource(context3));

                // Contexts Jena
                this.context1_jena = NodeFactory.createURI(context1);
                this.context2_jena = NodeFactory.createURI(context2);
                this.context3_jena = NodeFactory.createURI(context3);

                // Statements Corese
                this.statement_0_corese = EdgeImpl.create(
                                this.default_context_corese,
                                this.edith_piaf_node_corese,
                                this.isa_property_corese,
                                this.singer_node_corese);
                this.statement_1_corese = EdgeImpl.create(
                                this.context1_corese,
                                this.edith_piaf_node_corese,
                                this.first_name_property_corese,
                                this.edith_literal_corese);
                this.statement_2_corese = EdgeImpl.create(
                                this.context2_corese,
                                this.edith_piaf_node_corese,
                                this.first_name_property_corese,
                                this.edith_literal_corese);
                this.statement_3_corese = EdgeImpl.create(
                                this.context3_corese,
                                this.edith_piaf_node_corese,
                                this.first_name_property_corese,
                                this.edith_literal_corese);
                this.statement_bonus_corese = EdgeImpl.create(
                                this.context3_corese,
                                this.george_brassens_node_corese,
                                this.isa_property_corese,
                                this.singer_node_corese);

                // Statements Jena
                this.statement_0_jena = new Quad(default_context_jena, statement_0.asTriple());
                this.statement_1_jena = new Quad(NodeFactory.createURI(context1), statement_1.asTriple());
                this.statement_2_jena = new Quad(NodeFactory.createURI(context2), statement_1.asTriple());
                this.statement_3_jena = new Quad(NodeFactory.createURI(context3), statement_1.asTriple());
                this.statement_bonus_jena = new Quad(NodeFactory.createURI(context3), statement_bonus.asTriple());
                ;

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
                JenaTdb1DataManager jt1dm;

                jt1dm = new JenaTdb1DataManagerBuilder().build();
                assertEquals(0, jt1dm.graphSize());
                jt1dm.close();

                jt1dm = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
                System.out.println(this.dataset.asDatasetGraph());
                assertEquals(4, jt1dm.graphSize());
                jt1dm.close();
        }

        @Test
        public void countEdges() {
                JenaTdb1DataManager data_manager;

                data_manager = new JenaTdb1DataManagerBuilder().build();
                assertEquals(0, data_manager.countEdges(this.first_name_property_corese));
                assertEquals(0, data_manager.countEdges(this.isa_property_corese));
                assertEquals(0, data_manager.countEdges(null));
                data_manager.close();

                data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
                assertEquals(3, data_manager.countEdges(this.first_name_property_corese));
                assertEquals(1, data_manager.countEdges(this.isa_property_corese));
                assertEquals(4, data_manager.countEdges(null));
                data_manager.close();
        }

        @Test
        public void getEdgesAll() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All edges
                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, null);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();

                assertEquals(2, result.size());
                assertEquals(true, containsCompareTriple(this.statement_0_corese, result));
                assertEquals(true, containsCompareTriple(this.statement_1_corese, result));
        }

        @Test
        public void getEdgesDefault() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All edges of default context
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.default_context_corese);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();
                assertEquals(1, result.size());
                assertEquals(true, containsCompareTriple(this.statement_0_corese, result));
        }

        @Test
        public void getEdgesIgnoreNull() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All edges (with ignore null values)
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(null);
                contexts.add(null);
                contexts.add(null);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();

                assertEquals(2, result.size());
                assertEquals(true, containsCompareTriple(this.statement_0_corese, result));
                assertEquals(true, containsCompareTriple(this.statement_1_corese, result));
        }

        @Test
        public void getEdgesIgnoreNull2() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All edges of context 1 (with a ignore null value)
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(null);
                contexts.add(this.context1_corese);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();

                assertEquals(1, result.size());
                assertEquals(true, containsCompareTriple(this.statement_1_corese, result));
        }

        @Test
        public void getEdgesOneContext() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All edges of context 1
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);

                Iterable<Edge> iterable = data_manager.getEdges(null, null, null, contexts);
                List<Edge> result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();

                assertEquals(1, result.size());
                assertEquals(result.get(0).getGraph(), this.context1_corese);

                // All edges of context 1 (with a ignore null value)
                contexts = new ArrayList<>();
                contexts.add(null);
                contexts.add(this.context1_corese);

                iterable = data_manager.getEdges(null, null, null, contexts);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();

                assertEquals(1, result.size());
                assertEquals(this.context1_corese, result.get(0).getGraph());
        }

        @Test
        public void getNodes() {
                Iterable<Node> iterable;
                List<Node> result;

                this.dataset.asDatasetGraph().add(this.statement_bonus_jena);
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All contexts
                iterable = data_manager.getNodes(null);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(4, result.size());
                assertEquals(true, result.contains(this.edith_piaf_node_corese));
                assertEquals(true, result.contains(this.george_brassens_node_corese));
                assertEquals(true, result.contains(this.singer_node_corese));
                assertEquals(true, result.contains(this.edith_literal_corese));
                // Context 1
                iterable = data_manager.getNodes(this.context1_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(2, result.size());
                assertEquals(true, result.contains(this.edith_piaf_node_corese));
                assertEquals(true, result.contains(this.edith_literal_corese));

                // Element of kg:default
                iterable = data_manager.getNodes(default_context_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(2, result.size());
                assertEquals(true, result.contains(this.edith_piaf_node_corese));
                assertEquals(true, result.contains(this.singer_node_corese));

                // No duplication
                this.dataset.asDatasetGraph().add(
                                this.context3_jena,
                                this.george_brassens_node_jena,
                                this.isa_property_jena,
                                this.george_brassens_node_jena);

                iterable = data_manager.getNodes(this.context3_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(4, result.size());
                assertEquals(true, result.contains(this.edith_piaf_node_corese));
                assertEquals(true, result.contains(this.edith_literal_corese));
                assertEquals(true, result.contains(this.george_brassens_node_corese));
                assertEquals(true, result.contains(this.singer_node_corese));

                data_manager.close();
        }

        @Test
        public void predicates() {
                Iterable<Node> iterable;
                List<Node> result;

                this.dataset.asDatasetGraph().add(this.statement_bonus_jena);
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // All contexts
                iterable = data_manager.predicates(null);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(2, result.size());
                assertEquals(true, result.contains(this.isa_property_corese));
                assertEquals(true, result.contains(this.first_name_property_corese));

                // Context 1
                iterable = data_manager.predicates(this.context1_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(1, result.size());
                assertEquals(true, result.contains(this.first_name_property_corese));

                // Default context
                iterable = data_manager.predicates(this.default_context_corese);
                result = new ArrayList<>();
                iterable.forEach(result::add);
                assertEquals(1, result.size());
                assertEquals(true, result.contains(this.isa_property_corese));
                data_manager.close();
        }

        @Test
        public void contexts() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
                Iterable<Node> iterable = data_manager.contexts();
                List<Node> result = new ArrayList<>();
                iterable.forEach(result::add);
                data_manager.close();

                assertEquals(4, result.size());
                assertEquals(true, result.contains(this.context1_corese));
                assertEquals(true, result.contains(this.context2_corese));
                assertEquals(true, result.contains(this.context3_corese));
                assertEquals(true, result.contains(this.default_context_corese));
        }

        @Test
        public void insertSPO() {
                Dataset dataset = DatasetFactory.create();
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);

                data_manager.insert(this.edith_piaf_node_corese, this.first_name_property_corese,
                                this.edith_literal_corese, contexts);
                data_manager.close();

                assertEquals(2, dataset.asDatasetGraph().stream().count());
                assertEquals(false, dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(false, dataset.asDatasetGraph().contains(this.statement_3_jena));
        }

        @Test
        public void insertSPODefault() {
                Dataset dataset = DatasetFactory.create();
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.default_context_corese);

                Iterable<Edge> results = data_manager.insert(
                                this.edith_piaf_node_corese,
                                this.isa_property_corese,
                                this.singer_node_corese,
                                contexts);
                data_manager.close();

                assertEquals(1, dataset.asDatasetGraph().stream().count());
                assertEquals(results.iterator().next().getGraph(), this.default_context_corese);
                assertEquals(true,
                                dataset.asDatasetGraph().contains(this.default_context_jena, this.edith_piaf_node_jena,
                                                this.isa_property_jena,
                                                this.singer_node_jena));
                assertEquals(false,
                                dataset.asDatasetGraph().contains(this.context1_jena, this.edith_piaf_node_jena,
                                                this.isa_property_jena,
                                                this.singer_node_jena));
                assertEquals(false,
                                dataset.asDatasetGraph().contains(this.context2_jena, this.edith_piaf_node_jena,
                                                this.isa_property_jena,
                                                this.singer_node_jena));
                assertEquals(false,
                                dataset.asDatasetGraph().contains(this.context3_jena, this.edith_piaf_node_jena,
                                                this.isa_property_jena,
                                                this.singer_node_jena));
        }

        @Test(expected = UnsupportedOperationException.class)
        public void insertSPOError1() {
                Dataset dataset = DatasetFactory.create();
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);

                data_manager.insert(null, this.isa_property_corese, this.singer_node_corese, contexts);
                data_manager.close();
        }

        @Test(expected = UnsupportedOperationException.class)
        public void insertSPOError2() {
                Dataset dataset = DatasetFactory.create();
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);
                contexts.add(null);

                data_manager.insert(this.edith_piaf_node_corese, this.isa_property_corese, this.singer_node_corese,
                                contexts);
                data_manager.close();
        }

        @Test
        public void insertEdgeEdge() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // Insert new statement
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_bonus_jena));

                Edge iterable = data_manager.insert(this.statement_bonus_corese);
                assertEquals(iterable, this.statement_bonus_corese);
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_bonus_jena));

                // Try insert statement already in graph
                iterable = data_manager.insert(this.statement_bonus_corese);
                assertEquals(null, iterable);
                data_manager.close();
        }

        @Test
        public void deleteSPO() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.default_context_corese);
                contexts.add(this.context2_corese);

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(4, dataset.asDatasetGraph().stream().count());

                data_manager.delete(this.edith_piaf_node_corese, this.isa_property_corese, this.singer_node_corese,
                                contexts);
                data_manager.close();

                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(3, dataset.asDatasetGraph().stream().count());
        }

        @Test
        public void deleteContext2() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                ArrayList<Node> contexts = new ArrayList<>();
                contexts.add(this.context2_corese);

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(4, dataset.asDatasetGraph().stream().count());

                data_manager.delete(null, null, null, contexts);
                data_manager.close();

                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(3, dataset.asDatasetGraph().stream().count());
        }

        @Test
        public void deleteFirstName() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_3_jena));
                assertEquals(4, dataset.asDatasetGraph().stream().count());

                data_manager.delete(null, this.first_name_property_corese, null, null);
                data_manager.close();

                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_3_jena));
                assertEquals(1, dataset.asDatasetGraph().stream().count());
        }

        @Test
        public void deleteSPOAll() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(false, this.dataset.asDatasetGraph().isEmpty());

                data_manager.delete(null, null, null, null);
                data_manager.close();

                assertEquals(true, this.dataset.asDatasetGraph().isEmpty());
        }

        @Test
        public void deleteEdgeStatement0() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));

                data_manager.delete(this.statement_0_corese);
                data_manager.close();

                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
        }

        @Test
        public void deleteEdgeStatement1() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));

                data_manager.delete(this.statement_1_corese);
                data_manager.close();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
        }

        @Test
        public void deleteFromContext() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_3_jena));

                List<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);

                Edge edge_1 = this.statement_1_corese;
                Iterable<Edge> removed = data_manager.delete(edge_1.getSubjectNode(), edge_1.getProperty(),
                                edge_1.getObjectNode(), contexts);
                data_manager.close();

                List<Edge> result = new ArrayList<>();
                removed.forEach(result::add);
                assertEquals(false, result.contains(this.statement_0_corese));
                assertEquals(true, result.contains(this.statement_1_corese));
                assertEquals(true, result.contains(this.statement_2_corese));
                assertEquals(false, result.contains(this.statement_3_corese));

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_3_jena));
        }

        @Test
        public void deleteFromContextNoRemove() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_3_jena));

                List<Node> contexts = new ArrayList<>();
                contexts.add(this.context1_corese);
                contexts.add(this.context2_corese);

                Edge edge_0 = this.statement_0_corese;
                Iterable<Edge> removed = data_manager.delete(edge_0.getSubjectNode(), edge_0.getProperty(),
                                edge_0.getObjectNode(), contexts);
                data_manager.close();

                List<Edge> result = new ArrayList<>();
                removed.forEach(result::add);
                assertEquals(true, result.isEmpty());

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_3_jena));
        }

        @Test
        public void clearContext1() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));

                data_manager.clear(List.of(this.context1_corese), false);
                data_manager.close();

                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_0_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
        }

        @Test
        public void addContext() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // build and add a new statement in context_1
                Quad theoretical_old_statement = new Quad(this.context1_jena, this.george_brassens_node_jena,
                                this.isa_property_jena, this.singer_node_jena);
                this.dataset.asDatasetGraph().add(theoretical_old_statement);

                // build theorical result
                Quad theoretical_new_statement = new Quad(this.context2_jena, this.george_brassens_node_jena,
                                this.isa_property_jena, this.singer_node_jena);

                // tests
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_old_statement));
                assertEquals(false, this.dataset.asDatasetGraph().contains(theoretical_new_statement));
                data_manager.addGraph(this.context1_corese, this.context2_corese, false);
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_old_statement));
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_new_statement));
                data_manager.close();
        }

        @Test
        public void moveContext() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // build and add a new statement in context_1
                Quad theoretical_old_statement = new Quad(this.context1_jena, this.george_brassens_node_jena,
                                this.isa_property_jena, this.singer_node_jena);
                this.dataset.asDatasetGraph().add(theoretical_old_statement);
                this.dataset.asDatasetGraph().delete(this.statement_2_jena);

                // build theorical result
                Quad theoretical_new_statement = new Quad(this.context2_jena, this.george_brassens_node_jena,
                                this.isa_property_jena, this.singer_node_jena);
                // statement_2 is the second theorical result

                // tests
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_old_statement));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(theoretical_new_statement));
                data_manager.moveGraph(this.context1_corese, this.context2_corese, false);
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(theoretical_old_statement));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_new_statement));
                data_manager.close();
        }

        @Test
        public void copyContext() {
                JenaTdb1DataManager data_manager = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();

                // build and add a new statement in context_1
                Quad theoretical_old_statement = new Quad(this.context1_jena, this.george_brassens_node_jena,
                                this.isa_property_jena, this.singer_node_jena);
                this.dataset.asDatasetGraph().add(theoretical_old_statement);
                this.dataset.asDatasetGraph().delete(this.statement_2_jena);

                // build theorical result
                Quad theoretical_new_statement = new Quad(this.context2_jena, this.george_brassens_node_jena,
                                this.isa_property_jena, this.singer_node_jena);
                // statement_2 is the second theorical result

                // tests
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_old_statement));
                assertEquals(false, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(false, this.dataset.asDatasetGraph().contains(theoretical_new_statement));
                data_manager.copyGraph(this.context1_corese, this.context2_corese, false);
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_1_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_old_statement));
                assertEquals(true, this.dataset.asDatasetGraph().contains(this.statement_2_jena));
                assertEquals(true, this.dataset.asDatasetGraph().contains(theoretical_new_statement));
                data_manager.close();
        }
}