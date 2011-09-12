package fr.inria.acacia.corese.triple.update;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.triple.parser.Constant;
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
	
	Constant with;
	List<Constant> using, named;
	
	Composite(int t){
		type = t;
		list  = new ArrayList<Composite>();
		using = new ArrayList<Constant>();
		named = new ArrayList<Constant>();

	}
	
	Composite(int t, Exp d){
		this(t);
		data = d;
	}
	
	public static Composite create(int type){
		return new Composite(type);
	}
	
	public static Composite create(int type, Exp d){
		Composite ope = new Composite(type, d);
		return ope;
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
				str += WITH + " " + getWith().toSparql() + NL;
			}
			for (Composite cc : getUpdates()){
				str += cc.title() + " " ;
				str += cc.getPattern()  + NL;
			}

			for (Constant uri : getUsing()){
				str += USING + " " + uri.toSparql() + NL;
			}
			
			for (Constant uri : getNamed()){
				str += NAMED + " " + uri.toSparql() + NL;
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
	
	public void setWith(Constant uri){
		with = uri;
	}
	
	public Constant getWith(){
		return with;
	}
	
	public void addUsing(Constant uri){
		using.add(uri);
	}
	
	public List<Constant> getUsing(){
		return using;
	}
	
	public void addNamed(Constant uri){
		named.add(uri);
	}
	
	public List<Constant> getNamed(){
		return named;
	}
	
	
}
