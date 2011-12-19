package fr.inria.edelweiss.kgraph.query;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/**
 * Pragma specific to kgraph
 * kgram pragma are run later
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class PragmaImpl extends Pragma {
	
	static final String ENTAIL 	 	= KG + "entailment";
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
	
	
	public void triple(Atom g, Triple t){

		String subject  = t.getSubject().getLongName();
		String property = t.getProperty().getLongName();
		String object   = t.getObject().getLongName();
		if (object == null) object = t.getObject().getName();
		IDatatype dt = t.getObject().getDatatypeValue();
		
		if (subject.equals(SELF)){
			if (property.equals(ENTAIL)){
				boolean b = value(object);
				graph.setEntailment();
				graph.setEntailment(b);
				// as graph.isUpdate may have been set to false
				// we force entailment
				if (b) graph.setUpdate(true);	
			}
			else if (property.equals(STATUS)){
				new Describe(exec, query).describe(value(object));
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
				Distance.setPropertyStep(dt.getDoubleValue());
			}
			else if (property.equals(CSTEP)){
				// kg:similarity kg:cstep 2
				graph.setClassDistance(null);
				Distance.setClassStep(dt.getDoubleValue());
			}
		}
		else if (subject.equals(PRAGMA)){
			if (property.equals(HELP) && value(object)){
				query.addInfo(help(), null);
			}
		}
		
				
	}
	
	
	
	
	
	
	

}
