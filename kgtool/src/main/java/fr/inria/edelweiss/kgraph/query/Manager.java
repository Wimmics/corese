package fr.inria.edelweiss.kgraph.query;

import java.util.List;

import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * SPARQL 1.1 Update
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Manager {
	
	Graph graph;
	Loader load;
	List<String> from, named;
	
	static final int COPY = 0;
	static final int MOVE = 1;
	static final int ADD  = 2;
	
	Manager(Graph g, Loader ld){
		graph = g;
		graph.init();
		load = ld;
	}
	
	static Manager create(Graph g, Loader ld){
		return new Manager(g, ld);
	}
	
	void setFrom(List<String> l){
		from = l;
	}
	
	void setNamed(List<String> l){
		named = l;
	}
	
	boolean process(Basic ope){
		String uri 			= ope.expand(ope.getGraph());
		boolean isDefault 	= ope.isDefault();
		boolean isNamed 	= ope.isNamed();
		boolean isAll 		= ope.isAll();
		boolean isSilent 	= ope.isSilent();

		switch (ope.type()){
		
		case Update.LOAD: 	return load(ope); 
			
		case Update.CREATE: return create(ope);
			
		case Update.CLEAR: 	return clear(ope);

		case Update.DROP: 	return drop(ope);
			
		case Update.ADD:	return add(ope);

		case Update.MOVE: 	return move(ope);

		case Update.COPY: 	return copy(ope);
			
		}
		
		return false;
		
	}
	
	private boolean clear(Basic ope) {
		return clear(ope, false);
	}
	
	private boolean drop(Basic ope) {
		return clear(ope, true);
	}
	
	private boolean clear(Basic ope, boolean drop) {
		if (named!=null && (ope.isNamed() || ope.isAll())){
			for (String gg : named){
				graph.clear(ope.expand(gg), ope.isSilent());
				if (drop) graph.deleteGraph(ope.expand(gg));
			}
		}
		if (from != null && (ope.isDefault() || ope.isAll())){
			for (String gg : from){
				graph.clear(ope.expand(gg), ope.isSilent());
				if (drop) graph.deleteGraph(ope.expand(gg));
			}
		}
		if (ope.getGraph()!=null){
			graph.clear(ope.expand(ope.getGraph()), ope.isSilent());
			if (drop) graph.deleteGraph(ope.expand(ope.getGraph()));
		}
		return true;
	}

	/**
	 * 
	copy graph  | default 
	to   target | default
	 */

	
	private boolean update(Basic ope, int mode) {
		String source = ope.expand(ope.getGraph());
		String target = ope.expand(ope.getTarget());
		
		if (source != null){
			if (target != null){
				update(ope, mode, source, target);
			}
			else {
				// skip copy to default
			}
		}
		else if (target != null && from != null) {
			for (String gg : from){
				String name = ope.expand(gg);
				update(ope, mode, name, target);
			}
		}

		return true;
	}
	
	
	private boolean update(Basic ope, int mode, String source, String target) {
		if (source.equals(target)) return true;
		
		switch (mode){
		case ADD:   graph.add(source, target, ope.isSilent()); break;
		case MOVE:  graph.move(source, target, ope.isSilent());break;
		case COPY: 	graph.copy(source, target, ope.isSilent());break;
		}
		return true;
	}

	private boolean copy(Basic ope) {
		return update(ope, COPY);
	}
	
	private boolean move(Basic ope) {
		return update(ope, MOVE);
	}

	private boolean add(Basic ope) {
		return update(ope, ADD);
	}



	private boolean create(Basic ope) {
		String uri = ope.expand(ope.getGraph());
		graph.addGraph(uri);
		return true;
	}

	private boolean load(Basic ope) {
		String uri = ope.expand(ope.getURI());
		String src = ope.expand(ope.getTarget());
		load.load(uri, src);
		return true;
	}

}
