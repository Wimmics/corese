package fr.inria.corese.sparql.datatype.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.corese.sparql.api.IDatatype;

public class SQLFun {
	
	static final String DERBY_DRIVER = "org.apache.derby.jdbc.ClientDriver";

	IDatatype input,  datatype;
	ResultSet output;
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
//		if (input == uri && query == datatype){
//			return output;
//		}
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
        	//stmt.close();
        	//rs.close();
           	//con.close();
        	input = uri;
        	datatype = query;
        	output = rs;
        	return rs;
        }
        catch (SQLException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }  
        return null;
	}
	
	
	
}
