package fr.inria.acacia.corese.triple.update;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.triple.parser.Exp;
/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Composite extends Update {
	
	static final String WITH  = "with";
	static final String USING = "using";
	static final String NAMED = "using named";
	static final String WHERE = "where";
	static final String DATA  = "data";

	
	Exp data, 
	// insert delete
	pattern, 
	// where
	body;
	// list insert delete
	List<Composite> list;
	
	String with;
	List<String> using, named;
	
	Composite(int t){
		type = t;
		list  = new ArrayList<Composite>();
		using = new ArrayList<String>();
		named = new ArrayList<String>();

	}
	
	Composite(int t, Exp d){
		this(t);
		data = d;
	}
	
	public static Composite create(int type){
		return new Composite(type);
	}
	
	public static Composite create(int type, Exp d){
		return new Composite(type, d);
	}
	
	public String toString(){
		String str = "";
		String NL = "\n";
		if (type() != COMPOSITE){
			str += title() + " ";
		}

		if (getData()!=null){
			str += DATA + " " + getData();
		}
		else {
			if (getWith()!=null){
				str += WITH + " " + getWith() + NL;
			}
			for (Composite cc : getUpdates()){
				str += cc.title() + " " ;
				str += cc.getPattern()  + NL;
			}

			for (String uri : getUsing()){
				str += USING + " " + uri + NL;
			}
			
			for (String uri : getNamed()){
				str += NAMED + " " + uri + NL;
			}

			if (getBody()!=null){
				str += WHERE + " " + getBody();
			}
		}
		return str;
	}
	
	public void setPattern(Exp d){
		pattern = d;
	}
	
	public Exp getPattern(){
		return pattern;
	}
	
	public void setBody(Exp d){
		body = d;
	}
	
	public Exp getBody(){
		return body;
	}
	
	public Exp getData(){
		return data;
	}
	
	public void add(Composite ope){
		list.add(ope);
	}
	
	public List<Composite> getUpdates(){
		return list;
	}
	
	public void setWith(String uri){
		with = uri;
	}
	
	public String getWith(){
		return with;
	}
	
	public void addUsing(String uri){
		using.add(uri);
	}
	
	public List<String> getUsing(){
		return using;
	}
	
	public void addNamed(String uri){
		named.add(uri);
	}
	
	public List<String> getNamed(){
		return named;
	}
	
	
}
