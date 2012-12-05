//package test.kgraph;
//
//import fr.inria.acacia.corese.exceptions.EngineException;
//import fr.inria.acacia.corese.triple.cst.RDFS;
//import fr.inria.edelweiss.kgram.api.core.Node;
//import fr.inria.edelweiss.kgram.core.Mappings;
//import fr.inria.edelweiss.kgraph.core.EdgeCore;
//import fr.inria.edelweiss.kgraph.core.Graph;
//import fr.inria.edelweiss.kgraph.logic.Entailment;
//import fr.inria.edelweiss.kgraph.query.QueryProcess;
//
//public class TestGraph {
//	
//	public static void main(String[] args){
//		new TestGraph().test();
//	}
//	
//	void test(){
//		Graph g = create();
//		
//		QueryProcess exec = QueryProcess.create(g);
//		
//		String query = "select * where {" +
//				"?x rdf:type/rdfs:subClassOf* <Animal>" +
//				"?x <property> ?val}";
//		
//		try {
//			Mappings res = exec.query(query);
//			System.out.println(res);
//		} catch (EngineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	
//	Graph create(){
//
//		Graph graph = Graph.create();
//		graph.setEntailment();
//		
//		Node g,s,p,o;
//		
//		g = graph.addGraph("g1");
//		
//		s = graph.addBlank();
//		p = graph.addProperty("name");
//		o = graph.addLiteral("John");
//		graph.addEdge(EdgeCore.create(g, s, p, o, o));
//		
//		p = graph.addProperty("member");
//		o = graph.addResource("cnrs");
//		graph.addEdge(EdgeCore.create(g, s, p, o));
//		
//		p = graph.addProperty("age");
//		o = graph.addLiteral(23);
//		graph.addEdge(EdgeCore.create(g, s, p, o));
//		
//		p = graph.addProperty(Entailment.RDFTYPE);
//		o = graph.addResource("Person");
//		graph.addEdge(EdgeCore.create(g, s, p, o));
//		
//		s = graph.addResource("Person");
//		p = graph.addProperty(Entailment.RDFSSUBCLASSOF);
//		o = graph.addResource("Animal");
//		graph.addEdge(EdgeCore.create(g, s, p, o));
//		
//		s = graph.addResource("name");
//		p = graph.addProperty(Entailment.RDFSSUBPROPERTYOF);
//		o = graph.addResource("property");
//		graph.addEdge(EdgeCore.create(g, s, p, o));
//		
//		
//		s = graph.addResource("cnrs");
//		p = graph.addProperty("name");
//		o = graph.addLiteral("CNRS");
//		graph.addEdge(EdgeCore.create(g, s, p, o));
//
//		
//		return graph;
//
//	}
//
//}
