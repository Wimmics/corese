package test.w3c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Formatter;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgtool.load.QueryLoad;

public class Earl {
	
	static final String NL 	 = System.getProperty("line.separator");
	static final String data = TestW3C11KGraph.more;

	StringBuilder sb;
	Formatter fmt;
	
	String header, pattern, corese ;

	
	Earl(){
		QueryLoad ql = QueryLoad.create();
		header  = ql.read(data + "prefix.txt");
		corese  = ql.read(data + "corese.txt");
		pattern = ql.read(data + "pattern.txt");

		sb  = new StringBuilder();
		fmt = new Formatter(sb);
		
		fmt.format(header);
		fmt.format(corese);
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
