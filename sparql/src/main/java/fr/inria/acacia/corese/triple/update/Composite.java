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
	static final String NL = System.getProperty("line.separator");

	
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
	

	public StringBuffer toString(StringBuffer sb){
		
		if (type() != COMPOSITE){
			sb.append(title() + " ");
		}

		if (getData()!=null){
			sb.append(DATA + " " + getData());
		}
		else {
			if (getWith()!=null){
				sb.append(WITH + " " + getWith().toString() + NL);
			}
			for (Composite cc : getUpdates()){
				sb.append(cc.title() + " ") ;
				sb.append(cc.getPattern()  + NL);
			}

			for (Constant uri : getUsing()){
				sb.append(USING + " " + uri.toString() + NL);
			}
			
			for (Constant uri : getNamed()){
				sb.append(NAMED + " " + uri.toString() + NL);
			}

			if (getBody()!=null){
				sb.append(WHERE + " " + getBody());
			}
		}
		return sb;
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
