package fr.inria.edelweiss.kgraph.query;

import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Pragma specific to kgraph
 * kgram pragma are run later
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class PragmaImpl extends Pragma {
	
	static final String ENTAIL 	= KG + "entail";
	
	QueryProcess exec;
	Graph graph;


	public PragmaImpl(QueryProcess ex, Query q){
		super(q, ex.getAST(q));
		exec = ex;
		graph = exec.getGraph();
	}


	public void triple(Triple t){

		String subject  = t.getSubject().getLongName();
		String property = t.getProperty().getLongName();
		String object   = t.getObject().getLongName();
		if (object == null) object = t.getObject().getName();
		
		
		if (subject.equals(SELF)){
			if (property.equals(ENTAIL)){
				boolean b = value(object);
				graph.setEntailment();
				graph.setEntailment(b);
				// as graph.isUpdate may have been set to false
				// we force entailment
				if (b) graph.setUpdate(true);	
			}
		}
		
				
	}

}
