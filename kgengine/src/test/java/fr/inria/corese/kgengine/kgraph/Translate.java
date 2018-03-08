package fr.inria.corese.kgengine.kgraph;

import java.io.File;
import java.io.IOException;

import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.TripleFormat;



public class Translate {
	
	static final String[] 	SUFFIX 	= {".rdf", ".rdfs", ".owl"};
	static final String 	TTL 	= ".ttl";
	
	String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	String root = "/user/corby/home/workspace/corese/data/geographicalData/annotations";
	
	public static void main(String[] args) {
		new Translate().process();
	}
	
	/*
	mv *ttl ../commattl/
	mv data/*ttl ../commattl/data
	mv data2/*ttl ../commattl/data2
	 */
	
	void process(){
//		translate(data + "comma/comma.rdfs");
//		translate(data + "comma/commatest.rdfs");
//		translate(data + "comma/model.rdf");
//		translate(data + "comma/testrdf.rdf");
//		
//		translate(data + "comma/data");
//		translate(data + "comma/data2");
		
		//translate(data + "comma/data2/f1.rdf");
		
		translate(root);


	}
	
	private void translate(String path){
		File dir=new File(path);
		String name;
		if (dir.isDirectory()){
			path+=File.separator;
			String[] files=dir.list();
			for (int i=0; i<files.length; i++){
				name = path+files[i];
				translate(name);
			}
		}
		else if (check(path)){
			String out = base(path) + TTL ; 
			System.out.println(out);
			process(path, out );
		}
	}

	boolean check(String name){
		for (String suf : SUFFIX){
			if (name.endsWith(suf)){
				return true;
			}
		}
		return false;
	}
	
	String base(String name){
		for (String suf : SUFFIX){
			if (name.endsWith(suf)){
				return name.substring(0, name.length()-suf.length());
			}
		}
		return null;
	}

	
	void process(String in, String out){
		
		Graph g = Graph.create();
		Load ld = Load.create(g);
		NSManager nsm = NSManager.create();
//		nsm.definePrefix("c", "http://www.inria.fr/acacia/comma#");
//		nsm.definePrefix("i", "http://www.inria.fr/acacia/comma/instance#");

		nsm.definePrefix("geo", "http://rdf.insee.fr/geo/");
		nsm.definePrefix("dc", "http://purl.org/dc/elements/1.1/");
		
		try {
			ld.loadWE(in);
			TripleFormat f = TripleFormat.create(g, nsm);
			f.write(out);
		} catch (LoadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
