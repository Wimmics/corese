package fr.inria.corese.compiler.eval;

import java.sql.ResultSet;

public class SQLResult {
	
	ResultSet rs;
	boolean isSort = false;
	
	SQLResult (ResultSet r){
		rs = r;
	}
	
	public SQLResult (ResultSet r, boolean b){
		rs = r;
		isSort = b;
	}
	
	public ResultSet getResultSet(){
		return rs;
	}
	
	public boolean isSort(){
		return isSort;
	}

}
