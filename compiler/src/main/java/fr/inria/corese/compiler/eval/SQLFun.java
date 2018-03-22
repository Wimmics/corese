package fr.inria.corese.compiler.eval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.corese.sparql.api.IDatatype;

public class SQLFun {
	
	static final String DERBY_DRIVER = "org.apache.derby.jdbc.ClientDriver";
	static Object driver;

	public ResultSet sql(IDatatype uri, IDatatype dd,
			IDatatype login, IDatatype passwd, IDatatype query){
		if (driver == null){
			// first time
			try {
				// remember driver is loaded
				driver = Class.forName(dd.getLabel()).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sql(uri, login, passwd, query);
	}
	
	public ResultSet sql(IDatatype uri, 
			IDatatype login, IDatatype passwd, IDatatype query){
		try {
			if (driver == null){ 
				try {
					// default is derby
					driver = Class.forName(DERBY_DRIVER).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	Connection con = 
        		DriverManager.getConnection(uri.getLabel(), login.getLabel(), passwd.getLabel());
        	Statement stmt = con.createStatement();
        	ResultSet rs = stmt.executeQuery(query.getLabel());
        	return rs;
        }
        catch (SQLException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }  
        return null;
	}
	
	
	
}
