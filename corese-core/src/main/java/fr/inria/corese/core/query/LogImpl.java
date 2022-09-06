package fr.inria.corese.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.inria.corese.core.api.Log;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.triple.parser.ASTQuery;

/**
 * Log activity (load, query, etc.)
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class LogImpl implements Log {

	static final String DEFAULT_FILE = "/tmp/kgram_";

	ArrayList<Object> load, query, update;

	String file;

	boolean isLoad = true,
			isQuery = true,
			isUpdate = true,
			isTrace = false;

	LogImpl() {
		load = new ArrayList<Object>();
		query = new ArrayList<Object>();
		update = new ArrayList<Object>();
		file = DEFAULT_FILE + new Date();
	}

	public static LogImpl create() {
		return new LogImpl();
	}

	public void reset() {
		load.clear();
		query.clear();
		update.clear();
	}

	public void setQuery(boolean b) {
		isQuery = b;
	}

	public void setUpdate(boolean b) {
		isUpdate = b;
	}

	public void setLoad(boolean b) {
		isLoad = b;
	}

	public void setTrace(boolean b) {
		isTrace = b;
	}

	public void setActive(boolean b) {
		setLoad(b);
		setQuery(b);
		setUpdate(b);
	}

	public void log(int type, Object obj) {
		switch (type) {
			case LOAD:
				load(obj);
				break;
			case QUERY:
				query((Query) obj);
				break;
			case UPDATE:
				update((Query) obj);
				break;
		}
	}

	public void log(int type, Object obj1, Object obj2) {
		switch (type) {
			case QUERY:
				query((Query) obj1);
				break;
		}
	}

	public List<Object> get(int type) {
		switch (type) {
			case LOAD:
				return load;
			case QUERY:
				return query;
			case UPDATE:
				return update;
		}
		return new ArrayList<Object>();
	}

	void load(Object name) {
		trace("Load: " + load.size() + " " + name);
		if (isLoad) {
			load.add(name);
		}
	}

	void query(Query q) {
		ASTQuery ast = q.getAST();
		trace("Query: " + query.size() + " " + ast.getText());
		if (isQuery) {
			query.add(ast.getText());
		}
	}

	void update(Query q) {
		ASTQuery ast = q.getAST();
		trace("Update: " + update.size() + " " + ast.getText());
		if (isUpdate) {
			update.add(ast.getText());
		}
	}

	void trace(String str) {
		if (isTrace) {
			System.out.println(str);
		}
	}

}
