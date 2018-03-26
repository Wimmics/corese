package fr.inria.corese.test.w3c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;

import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Earl {
	
	static final String NL 	 = System.getProperty("line.separator");
	static final String data = TestW3C11KGraphNew.more;

	StringBuilder sb;
	Formatter fmt;
	
	String header, pattern, corese ;

	
	Earl(){
            sb  = new StringBuilder();
            fmt = new Formatter(sb);
            try {
                QueryLoad ql = QueryLoad.create();
                header  = ql.readWE(data + "prefix.txt");
                corese  = ql.readWE(data + "corese.txt");
                pattern = ql.readWE(data + "pattern.txt");                              
                fmt.format(header);
                fmt.format(corese);
            } catch (LoadException ex) {
                LogManager.getLogger(Earl.class.getName()).log(Level.ERROR, "", ex);
            }
	}
	
	void define(String test, boolean res){
		fmt.format(pattern, test, result(res), DatatypeMap.newDate().toSparql());
	}
	
	void  skip(String test){
		fmt.format(pattern, test, "earl:untested", DatatypeMap.newDate().toSparql());
	}
	
	String result(boolean res){
		if (res) return "earl:passed";
		else return "earl:failed";
	}
	
	public String toString(){
		return sb.toString();
	}
	
	public void toFile(String name){
		File f = new File(name);
		try {
			FileWriter w = new FileWriter(f);
			BufferedWriter b = new BufferedWriter(w);
			b.write(sb.toString());
			b.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void process(){
		define("aggregates/test1", true);
		System.out.println(this);
	}
	
	
	public static void main(String[] args){
		new Earl().process();
	}

}
