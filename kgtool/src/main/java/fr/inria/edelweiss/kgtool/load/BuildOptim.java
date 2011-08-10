package fr.inria.edelweiss.kgtool.load;


import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.rdf.EdgeComment;
import fr.inria.edelweiss.kgraph.rdf.EdgeLabel;
import fr.inria.edelweiss.kgraph.rdf.EdgeSubClass;
import fr.inria.edelweiss.kgraph.rdf.EdgeType;

/**
 * Associate optimized Edge classes to metamodel properties
 * They carry the edge node in a static member in the class
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class BuildOptim extends BuildImpl {	
	
	public BuildOptim(Graph g){
		super(g);
	}
	
	public static BuildOptim create(Graph g){
		BuildOptim b = new BuildOptim(g);
		b.init();
		return b;
	}
	
	public void init(){	
		define(Entailment.RDFTYPE, 			EdgeType.class);
		define(Entailment.RDFSSUBCLASSOF, 	EdgeSubClass.class);
		define(Entailment.RDFSLABEL,		EdgeLabel.class);
		define(Entailment.RDFSCOMMENT, 		EdgeComment.class);
	}
	
}
