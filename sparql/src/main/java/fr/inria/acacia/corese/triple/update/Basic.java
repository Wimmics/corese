package fr.inria.acacia.corese.triple.update;

import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.parser.And;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Triple;

/**
 * load clear drop create add move copy
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Basic extends Update {
	static final String SILENT 	= "silent";
	static final String DEFAUT 	= "default";
	static final String ALL 	= "all";
	static final String NAMED 	= "named";
	static final String INTO 	= "into";
	static final String TO 		= "to";
	static final String GRAPH 	= "graph";

	
	boolean 
	defaut 	= false,
	silent 	= false,
	named 	= false,
	all 	= false;
	
	String uri, graph, target;
	Constant auri, agraph, atarget;
	// for additional prefix namespace, only for pretty print
    Exp prolog = new And();
	
	Basic (int t){
		type = t;
	}
	

	
	public static Basic create(int type){
		Basic b = new Basic(type);
		return b;
	}
	
	
	public StringBuffer toString(StringBuffer sb){
		
		switch (type()){
		
		case PROLOG:
			prolog(sb);
			return sb;
		}
		
		
		sb.append(title());
		
		if (silent) 	sb.append(" " + SILENT) ;
		
		switch (type()){
		
		
		
		case LOAD:
			if (uri!=null)    sb.append(" " + uri);
			if (target!=null) sb.append(" " + INTO + " " + GRAPH + " " + target);
			break;
		
		case ADD:
		case MOVE:
		case COPY:
			if (graph!=null){
				sb.append(" " + GRAPH + " " + graph);
			}
			else {
				sb.append(" " + DEFAUT);
			}
			
			sb.append(" " + TO + " ");
			
			if (target!=null){
				sb.append(target);
			}
			else {
				sb.append(DEFAUT);
			}
			break;
		
		
		case CLEAR:
		case DROP:
		case CREATE:
			if (graph!=null)  sb.append(" " + GRAPH + " " + graph);
			if (named) 		sb.append(" " + NAMED) ;
			if (all) 		sb.append(" " + ALL) ;
			if (defaut) 	sb.append(" " + DEFAUT) ;
		}

		return sb;
	}
	
	public boolean hasContent(){
		return prolog.size() > 0;
	}	
	
	void prolog(StringBuffer sb){
		getASTUpdate().getASTQuery().getSparqlPrefix(prolog, sb);
	}
	
	public void defNamespace(String prefix, String ns){
		if (prefix.endsWith(":")){
			prefix = prefix.substring(0, prefix.length() - 1); // remove :
		}
		Triple triple = Triple.createNS(
				Constant.create(KeywordPP.PREFIX),  Constant.create(prefix), 
				Constant.create(ns));
		prolog.add(triple);
	}
	
	public void defBase(String ns){
		Triple triple = Triple.createNS(
				Constant.create(KeywordPP.BASE),  Constant.create(""), 
				Constant.create(ns));
		prolog.add(triple);
	}

	
	public void setSilent(boolean b){
		silent = b;
	}
	
	public void setDefault(boolean b){
		defaut = b;
	}
	
	public void setNamed(boolean b){
		named = b;
	}

	public void setAll(boolean b){
		all = b;
	}
	
	
	public boolean isDefault(){
		return defaut;
	}
	
	public boolean isNamed(){
		return named;
	}

	public boolean isAll(){
		return all;
	}
	
	public boolean isSilent(){
		return silent;
	}
	
	
	public void setGraph(String g){
		graph = g;
	}
	
	public void setGraph(Constant g){
		agraph = g;
	}
	
	
	public void setTarget(String t){
		target = t;
	}
	
	public void setTarget(Constant t){
		atarget = t;
	}
	
	public void setURI(String t){
		uri = t;
	}
	
	public void setURI(Constant t){
		auri = t;
	}
	
	public String getGraph(){
		if (agraph == null) return null;
		return agraph.getLongName();
	}
	
	public String getTarget(){
		if (atarget == null) return null;
		return atarget.getLongName();
	}
	
	public String getURI(){
		if (auri == null) return null;
		return auri.getLongName();
	}
	
}
