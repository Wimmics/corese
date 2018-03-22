package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.RDFList;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.logic.Distance;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.core.util.GraphListenerImpl;

/**
 * Pragma specific to kgraph
 * kgram pragma are run later
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class PragmaImpl extends Pragma {
	
	static final String ENTAIL 	 	= Entailment.ENTAIL;
	static final String RULE 	 	= Entailment.RULE;
	static final String RDFSENTAIL 	= RDFS.RDFS + "entailment";
	
	static final String SIMILARITY 	= KG + "similarity";
	static final String PSTEP 		= KG + "pstep";
	static final String CSTEP 		= KG + "cstep";
	
	QueryProcess exec;
	Graph graph;
	Entailment entail ;



	PragmaImpl(QueryProcess ex, Query q){
		super(q, ex.getAST(q));
		exec = ex;
		graph = exec.getGraph();
		entail = graph.getEntailment();
	}

	public static PragmaImpl create(QueryProcess ex, Query q){
		return new PragmaImpl(ex, q);
	}

	String help (){
		String query = 
			"select where {}\n" +
			"pragma {\n" +
			"kg:entailment rdfs:domain true \n" +
			"kg:entailment rdfs:range true \n" +
			"kg:entailment rdfs:subClassOf true \n" +
			"kg:entailment rdfs:subPropertyOf true \n" +
			"" +
			"kg:similarity kg:cstep 2 \n" +
			"kg:similarity kg:pstep 8 \n" +
			"}" ;
	
		return query;
	}
	
	/**
	 * [a kg:Insert ; kg:triple(?x rdf:type c:Person)]
	 * Triple represented by a list
	 */
	public void list(Atom g, RDFList list){
		System.out.println(list.head() + " " + list.getList());
		
		for (Expression exp : list.getList()){
			System.out.println(exp);
		}

		
		for (fr.inria.corese.sparql.triple.parser.Exp exp : list.getBody()){
			//System.out.println(exp);
		}
	}

	public void triple(Atom g, Triple t, fr.inria.corese.sparql.triple.parser.Exp pragma){
		//System.out.println(t);
		String subject  = t.getSubject().getLabel();
		String property = t.getProperty().getLabel();
		String object   = t.getObject().getLabel();
		
		IDatatype dt = t.getObject().getDatatypeValue();
		
		if (g != null && isListen(g.getLabel())){
			listen(g, t, pragma);
		}
		
		else if (subject.equals(SELF)){
//			if (property.equals(ENTAIL)){
//				boolean b = value(object);
//				graph.setEntailment();
//				graph.setEntailment(b);
//				// as graph.isUpdate may have been set to false
//				// we force entailment
//				if (b) graph.setUpdate(true);	
//			}
//			else 
			if (property.equals(STATUS)){
				new Describe(exec, query).describe(value(object));
			}
			else if (property.equals(ENTAIL)){
				rdfsentail(value(object));
			}	
			else if (property.equals(RULE)){
				rule(value(object));
			}	
		}
		else if (subject.equals(ENTAIL)){
			// kg:entailment rdfs:subClassOf true
			// kg:entailment rdfs:range false
			entail.set(property, value(object));
		}
		else if (subject.equals(SIMILARITY)){
			if (property.equals(PSTEP)){
				// kg:similarity kg:pstep 0.5
				graph.setPropertyDistance(null);
				Distance.setPropertyStep(dt.doubleValue());
			}
			else if (property.equals(CSTEP)){
				// kg:similarity kg:cstep 2
				graph.setClassDistance(null);
				Distance.setClassStep(dt.doubleValue());
			}
		}
		else if (subject.equals(PRAGMA)){
			if (property.equals(HELP) && value(object)){
				query.addInfo(help(), null);
			}
		}
		else if (subject.equals(LISTEN)){
			listen(g, t, pragma);			
		}		
	}
	
	/**
	 * 
	 * (des)activate rule engines
	 */
	private void rule(boolean value) {
		graph.getWorkflow().setActivate(Engine.RULE_ENGINE, value);	
		if (value){
                    //graph.setEntail(true);
                    graph.getEventManager().start(Event.ActivateEntailment);
		}
	}
	

	private void rdfsentail(boolean value) {
		graph.pragmaRDFSentailment(value);
	}
       
        	
	boolean isListen(String label){
		return label.equals(LISTEN) || label.equals(INSERT) || label.equals(DELETE);
	}
	
	
	/**
	 * g = kg:listen
	 * pragma = graph pattern
	 */
	public void listen(Atom g, Triple t, fr.inria.corese.sparql.triple.parser.Exp pragma){

		String subject  = t.getSubject().getLabel();
		String property = t.getProperty().getLabel();
		String object   = t.getObject().getLabel();
		IDatatype dt 	= t.getObject().getDatatypeValue();
		
		GraphListenerImpl gl = (GraphListenerImpl) query.getPragma(LISTEN);
		
		if (gl == null){
			gl = GraphListenerImpl.create();
			query.setPragma(LISTEN, gl);
		}
		
		if (subject.equals(LISTEN)){
			// kg:listen kg:insert true
			gl.setProperty(property, dt);
		}
		else if (g != null && g.getLabel().equals(INSERT)){
			// graph kg:insert { <John> ?p ?y }
			gl.setProperty(LISTEN_INSERT , t);
		}
		
		
	}
	
	
	

}
