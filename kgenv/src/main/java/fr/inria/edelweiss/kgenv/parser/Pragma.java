package fr.inria.edelweiss.kgenv.parser;

import java.lang.reflect.InvocationTargetException;

import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.EventListener;
import fr.inria.edelweiss.kgram.tool.Message;

/**
 * Pragma processor
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Pragma  {
	protected static final String KG = ExpType.KGRAM;
	// subject
	protected static final String SELF 	= KG + "kgram";
	static final String MATCH 	= KG + "match";
	static final String PATH 	= KG + "path";

	// kgram
	static final String TEST 	= KG + "test";
	static final String DEBUG 	= KG + "debug";
	static final String SORT	= KG + "sort";
	static final String LISTEN 	= KG + "listen";
	static final String LOOP 	= KG + "loop";
	static final String NODE 	= KG + "node";
	static final String EDGE 	= KG + "edge";
	static final String LOAD 	= KG + "load";
	static final String LIST 	= KG + "list";

	

	// match
	static final String MODE 	= KG + "mode";
	// match mode
	static final String RELAX 		= "relax";
	static final String SUBSUME 	= "subsume";
	static final String STRICT  	= "strict";
	static final String ONTOLOGY  	= "ontology";
	static final String INFERENCE  	= "inference";


	Eval kgram;
	Query query;
	ASTQuery ast;
	
	public Pragma(Eval e, Query q, ASTQuery a){
		kgram = e;
		query = q;
		ast = a;
	}
	
	public Pragma(Query q, ASTQuery a){
		query = q;
		ast = a;
	}
	
	public void parse(){
		//System.out.println("** Pragma1: " + ast.getPragma());

		for (fr.inria.acacia.corese.triple.parser.Exp pragma : ast.getPragma().getBody()){
			if (query.isDebug()) Message.log(Message.PRAGMA, pragma);
			
			if (pragma.isTriple()){
				triple(pragma.getTriple());
			}
			else {
				parse(pragma);
			}
		}
	}
	
	public void parse(fr.inria.acacia.corese.triple.parser.Exp exp){
	}
	
	public void triple(Triple t){
		
		String subject  = t.getSubject().getLongName();
		String property = t.getProperty().getLongName();
		String object   = t.getObject().getLongName();
		if (object == null) object = t.getObject().getName();
		
		if (subject.equals(SELF)){
			if (property.equals(TEST)){
				query.setTest(value(object));
			}
			else if (property.equals(DEBUG)){
				query.setDebug(value(object));	
			}
			else if (property.equals(SORT)){
				query.setSort(value(object));	
			}
			else if (property.equals(LISTEN) && value(object)){
				kgram.addEventListener(EvalListener.create());
			}
			else if (property.equals(LIST) && value(object)){
				query.setListGroup(true);
			}
		}
		else if (subject.equals(MATCH)){
			if (property.equals(MODE)){
				int mode = getMode(object);
				query.setMode(mode);
			}
			else if (property.equals(RDFS.RDFTYPE)){
				// kg:match rdf:type <fr.inria.edelweiss.kgramenv.util.MatcherImpl> 
				Matcher match = (Matcher) create(object);
				if (match != null){
					kgram.setMatcher(match);
				}
			}
		}
		else if (subject.equals(LISTEN)){
			if (property.equals(RDFS.RDFTYPE)){
				// kg:listen rdf:type <fr.inria.edelweiss.kgram.event.StatListener> 
				EventListener el = (EventListener) create(object);
				if (el != null){
					kgram.addEventListener(el);
				}
			}
		}
		else if (subject.equals(PATH)){
			if (property.equals(LIST)){
				query.setListPath(value(object));
			}
		}
	}
	
	public boolean value(String value){
		return value.equals("true");
	}
	
	int getMode(String mode){
		if (mode.equals(STRICT)) 	return Matcher.STRICT;
		if (mode.equals(RELAX)) 	return Matcher.RELAX;
		if (mode.equals(ONTOLOGY)) 	return Matcher.ONTOLOGY;
		if (mode.equals(SUBSUME)) 	return Matcher.SUBSUME;
		if (mode.equals(INFERENCE)) return Matcher.INFERENCE;
		// default
		return Matcher.ONTOLOGY;
	}
	
	
	Object create(String name){
		try {
			//EventListener el ;
			//= (EventListener) Class.forName(object).newInstance();
			Class cname =  Class.forName(name);
			Object object =  cname.getMethod("create").invoke(cname);
			return object;
		} 

		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}
