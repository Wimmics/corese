package fr.inria.acacia.corese.triple.parser;

import java.util.List;

/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class RDFList extends And  {
	
	Expression first;
	List<Expression> list;
	
	public static RDFList create (){
		return new RDFList();
	}
	
	public Expression head(){
		return first;
	}
	
	void setHead(Expression e){
		first = e;
	}
	
	void setList(List<Expression> l){
		list = l;
	}
	
	public List<Expression> getList(){
		return list;
	}

}
