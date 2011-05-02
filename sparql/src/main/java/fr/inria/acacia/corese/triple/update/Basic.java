package fr.inria.acacia.corese.triple.update;

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
	
	Basic (int t){
		type = t;
	}
	

	
	public static Basic create(int type){
		Basic b = new Basic(type);
		return b;
	}
	
	public String toString(){
		String str = "";
		str += title();
		
		if (silent) 	str += " " + SILENT ;
		
		switch (type()){
		
		case LOAD:
			if (uri!=null)    str += " " + uri;
			if (target!=null) str += " " + INTO + " " + GRAPH + " " + target;
			break;
		
		case ADD:
		case MOVE:
		case COPY:
			if (graph!=null){
				str += " " + GRAPH + " " + graph;
			}
			else {
				str += " " + DEFAUT;
			}
			
			str += " " + TO + " ";
			
			if (target!=null){
				str += target;
			}
			else {
				str += DEFAUT;
			}
			break;
		
		
		case CLEAR:
		case DROP:
		case CREATE:
			if (graph!=null)  str += " " + GRAPH + " " + graph;
			if (named) 		str += " " + NAMED ;
			if (all) 		str += " " + ALL ;
			if (defaut) 	str += " " + DEFAUT ;
		}

		return str;
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
	
	
	public void setTarget(String t){
		target = t;
	}
	
	public void setURI(String t){
		uri = t;
	}
	
	public String getGraph(){
		return graph;
	}
	
	public String getTarget(){
		return target;
	}
	
	public String getURI(){
		return uri;
	}
	
}
