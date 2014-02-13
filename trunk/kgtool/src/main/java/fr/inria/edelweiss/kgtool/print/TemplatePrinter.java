package fr.inria.edelweiss.kgtool.print;

import java.io.FileWriter;
import java.io.IOException;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;


/**
 * Generate a file in rule format from a QueryEngine or a directory of templates
 * Olivier Corby, Wimmics INRIA 2013
 */ 
public class TemplatePrinter {
	
	String from, to;
	FileWriter fw;
	static final String NL 			= System.getProperty("line.separator");
	
	TemplatePrinter(String from, String to){
		this.from = from;
		this.to = to;
	}
	
	public static TemplatePrinter create(String from, String to){
		return new TemplatePrinter(from, to);
	}
	
	
	public void process() throws IOException, LoadException{
		Graph g = Graph.create();
		Load ld = Load.create(g);
		ld.loadWE(from);
		fw = new FileWriter(to);
		QueryEngine qe = ld.getQueryEngine();
		header();
		
		for (Query q : qe.getNamedTemplates()){
			process((ASTQuery) q.getAST());
		}
		for (Query q : qe.getQueries()){
			process((ASTQuery) q.getAST());
		}
		
		trailer();
		
		fw.flush();
		fw.close();
	}
	
	void header() throws IOException{
		write("<?xml version='1.0' encoding='UTF-8'?>");
                write("<!--");
                write("SPARQL Template Pretty Printer");
                write("Corese/KGRAM - Wimmics Inria I3S - 2013");
                write("-->");
		write("<rdf:RDF  xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' ") ;
		write("  xmlns='http://ns.inria.fr/edelweiss/2011/rule#'>");	
	}
	
	void trailer() throws IOException{
		write("</rdf:RDF>");
	}
	
	void process(ASTQuery ast) throws IOException{
		write("<rule>");
		write("<body>");
		write("<![CDATA[");
		fw.write(ast.getText());
		write("]]>");
		write("</body>");
		write("</rule>");
		write("");
	}
	
	void write(String str) throws IOException{
		fw.write(str + NL);
	}

}
