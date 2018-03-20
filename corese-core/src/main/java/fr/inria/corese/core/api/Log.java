package fr.inria.corese.core.api;

import java.util.List;


public interface Log {
	
	public static final int LOAD 	= 0;
	public static final int QUERY 	= 1;
	public static final int UPDATE  = 2;	
	
	void reset();	
	
	void log(int type, Object obj);
	
	void log(int type, Object obj1, Object obj2);

	List<Object> get(int type);
	
	
}
