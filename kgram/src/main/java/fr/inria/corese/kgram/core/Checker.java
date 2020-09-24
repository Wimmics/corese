package fr.inria.corese.kgram.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.core.Edge;


/**
 * Query Type Checker
 * 
 * Check occurrence of properties in Producer
 * Check if class/property are defined in Producer 
 * - p rdf:type rdf:Property
 * - c rdf:type rdfs:Class
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Checker {
	
	static Logger logger = LoggerFactory.getLogger(Checker.class);
	
	Eval eval;
	Producer producer;
	Matcher matcher;
	Query query;
	
	Checker(Eval e){
		eval = e;
		producer = e.getProducer();
		matcher  = e.getMatcher();
	}
	
	public static Checker create(Eval e){
		return new Checker(e);
	}
	
	void check(Query q){
		query = q;
		check(null, q.getBody(), eval.getMemory());
	}
	
	
	void check(Node gNode, Exp exp, Environment env){
		
		switch (exp.type()){
		
		case Exp.EDGE:
			edge(gNode, exp, env);
			break;
			
			
		case Exp.QUERY:
			check(gNode, exp.getQuery().getBody(), env);
			break;

		default:
			for (Exp ee : exp.getExpList()){
				check(gNode, ee, env);
			}
		}
			
			
	}
	
	/**
	 * Check occurrence of edge
	 * If edge has an associated query, check class/property definition in ontology
	 */
	void edge(Node gNode, Exp exp, Environment env){
		Edge edge = exp.getEdge();
		boolean exist = false, match = false, define = false;

		for (Edge ent : producer.getEdges(gNode, query.getFrom(gNode), edge, env)){

			if (ent != null){
				exist = true;
				if (matcher.match(edge, ent, env)){
					match = true;
					break;
				}
			}
		}
		
		Query q = query.get(edge);
		if (q != null){
			Eval ee = Eval.create(producer, eval.getEvaluator(), matcher);
			Mappings map;
                    try {
                        map = ee.query(q);
                        define = map.size()>0;
			report(edge, exist, match, define);
                    } catch (SparqlException ex) {
                        ex.printStackTrace();
                    }
			
		}
		else {
			report(edge, exist, match);
		}
	}

	
	void report(Edge edge, boolean exist, boolean match, boolean define){
		query.addInfo(edge.toString(),  
				" defined:" + define + " exist: " + exist + " match: " + match );
		logger.info("Edge: " + edge + ": " + exist + " " + match + " " + define);
	}
	
	void report(Edge edge, boolean exist, boolean match){
		query.addInfo(edge.toString(), " exist: " + exist + " match: " + match);
		logger.info("Edge: " + edge + ": " + exist + " " + match);
	}
	
	void report(Edge edge, boolean exist){
		logger.info("Defined: " + edge + ": " + exist);
	}
	
	
	
	
	

}
