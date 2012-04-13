package fr.inria.edelweiss.kgraph.query;

import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.edelweiss.kgenv.eval.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;

/**
 * SPARQL 1.1 Update
 * 
 * KGRAM Extensions:
 * 
 * create/drop graph kg:entailment
 * create      graph kg:rule
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class ManagerImpl implements Manager {
	
	// default loader, by meta protocol to preserve modularity
	static final String LOADER = "fr.inria.edelweiss.kgtool.load.Load";
	
	static Logger logger = Logger.getLogger(ManagerImpl.class);
	
	Graph graph;
	Loader load;
	QueryProcess exec;
	// dataset on which query is executed (cf W3C test case)
	Dataset ds;
	
	static final int COPY = 0;
	static final int MOVE = 1;
	static final int ADD  = 2;
	
	ManagerImpl(Graph g, Loader ld){
		graph = g;
		graph.init();
		load = ld;
		if (load == null){
			load = getLoader(LOADER);
			load.init(graph);
		}
	}
	
	ManagerImpl(QueryProcess exec){
		this(exec.getGraph(), exec.getLoader());
		this.exec = exec;
	}
	
	ManagerImpl() {
	}

	static ManagerImpl create(Graph g, Loader ld){
		return new ManagerImpl(g, ld);
	}
	
	static ManagerImpl create(QueryProcess exec){
		return new ManagerImpl(exec);
	}
	
	public static ManagerImpl create(QueryProcess exec, Dataset ds){
		ManagerImpl m = new ManagerImpl(exec);
		m.set(ds);
		return m;
	}
	
	static Loader getLoader(){
		return getLoader(LOADER);
	}
	
	static Loader getLoader(String name){
		try {
			Class<Loader> loadClass = (Class<Loader>) Class.forName(name);
			Loader ld = loadClass.newInstance();
			return ld;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public void set(Dataset ds){
		this.ds = ds;
	}
	
	
	public boolean isDebug (){
		return exec.isDebug();
	}
	
	
	public Mappings query(Query q, ASTQuery ast){
		return exec.update(q, ast, ds);
	}
	
	public boolean process(Query q, Basic ope){
		String uri 			= ope.getGraph();
		boolean isDefault 	= ope.isDefault();
		boolean isNamed 	= ope.isNamed();
		boolean isAll 		= ope.isAll();
		boolean isSilent 	= ope.isSilent();
		
		system(ope);

		switch (ope.type()){
		
		case Update.LOAD: 	return load(q, ope); 
			
		case Update.CREATE: return create(ope);
			
		case Update.CLEAR: 	return clear(ope);

		case Update.DROP: 	return drop(ope);
			
		case Update.ADD:	return add(ope);

		case Update.MOVE: 	return move(ope);

		case Update.COPY: 	return copy(ope);
			
		}
		
		return false;
		
	}
	
	/**
	 * kgraph extension:
	 * 
	 * clear graph kg:entailment suspend entailments
	 * add   graph kg:entailment resume  entailments
	 * 
	 * add graph kg:rule process rules if any
	 *
	 */
	void system(Basic ope){
		String uri = ope.getGraph();
		
		if (! isSystem(uri)){
			return;
		}
		
		RuleEngine rengine = load.getRuleEngine();
		
		

		switch (ope.type()){
		
		case Update.DROP: 
			
			if (isRule(uri) && rengine != null){
				// clear also the rule base
				rengine.clear();
			}
			
		case Update.CLEAR: 
			
			if (isEntailment(uri)){
				graph.setEntailment(false);
			}
			break;
			
			
		case Update.CREATE:
			
			if (isEntailment(uri)){
				graph.setEntailment(true);
				graph.setUpdate(true);
			}
			else if (isRule(uri)){
				if (rengine != null){
					rengine.process();
				}
			}
			break;
		}
	}
	
	boolean isSystem(String uri){
		return uri != null &&  uri.startsWith(Entailment.KGRAPH);
	}
	
	boolean isEntailment(String uri){
		return uri.equals(Entailment.ENTAIL);
	}
	
	boolean isRule(String uri){
		return uri.equals(Entailment.RULE);
	}
	
	private boolean clear(Basic ope) {
		return clear(ope, false);
	}
	
	private boolean drop(Basic ope) {
		return clear(ope, true);
	}
	
	private boolean clear(Basic ope, boolean drop) {
		
		if (ds!=null && ! ds.isEmpty()){
			if (ds.hasNamed() && (ope.isNamed() || ope.isAll())){
				for (String gg : ds.getNamed()){
					clear(gg, ope, drop);
				}
			}

			if (ds.hasFrom() && (ope.isDefault() || ope.isAll())){
				for (String gg : ds.getFrom()){
					clear(gg, ope, drop);
				}
			}
		}
		
		if (ope.getGraph() != null){
			graph.clear(ope.getGraph(), ope.isSilent());
			if (drop) graph.deleteGraph(ope.getGraph());
		}
		else if (ds == null || ds.isEmpty()){
			// no prescribed dataset
			if (ope.isNamed() || ope.isAll()){
				graph.clearNamed();
				if (drop){
					graph.dropGraphNames();
				}
			}
			else if (ope.isDefault()){
				graph.clearDefault();
			}

		}
		return true;
	}
	
	
	void clear(String g, Basic ope, boolean drop){
		graph.clear(ope.expand(g), ope.isSilent());
		if (drop) graph.deleteGraph(ope.expand(g));
	}

	/**
	 * 
	copy graph  | default 
	to   target | default
	 */

	
	private boolean update(Basic ope, int mode) {
		String source = ope.getGraph();
		String target = ope.getTarget();
		
		if (source != null){
			if (target != null){
				update(ope, mode, source, target);
			}
			else if (ds!=null && ds.hasFrom()){
				// copy g to default
				// use from as default specification
				String name = ope.expand(ds.getFrom().get(0));
				update(ope, mode, source, name);
			}
		}
		else if (target != null && ds!=null && ds.hasFrom()) {
			// copy default to g
			// use from as default specification
			for (String gg : ds.getFrom()){
				String name = ope.expand(gg);
				update(ope, mode, name, target);
			}
		}

		return true;
	}
	
	
	private boolean update(Basic ope, int mode, String source, String target) {
		if (source.equals(target)) return true;
		
		switch (mode){
		case ADD:   return graph.add(source, target, ope.isSilent()); 
		case MOVE:  return graph.move(source, target, ope.isSilent());
		case COPY: 	return graph.copy(source, target, ope.isSilent());
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
		String uri = ope.getGraph();
		graph.addGraph(uri);
		return true;
	}

	private boolean load(Query q, Basic ope) {
		if (load == null){
			logger.error("Load " + ope.getURI() + ": Loader is undefined");
			return ope.isSilent();
		}
		String uri = ope.getURI();
		String src = ope.getTarget();
		if (ope.isSilent()){
			load.load(uri, src);
		}
		else 
			try {	
			load.loadWE(uri, src);
		} catch (LoadException e) {
			logger.error("Load error: " + ope.getURI() + "\n" + e);
			q.addError("Load error: ", ope.getURI() + "\n" + e);
			return ope.isSilent();
		}
		
		if (load.isRule(uri) && load.getRuleEngine()!=null && src!=null && src.equals(Entailment.RULE)){
			load.getRuleEngine().entail();
		}
		
		return true;
	}

}
