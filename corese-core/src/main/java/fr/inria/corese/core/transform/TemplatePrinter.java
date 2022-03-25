package fr.inria.corese.core.transform;

import java.io.FileWriter;
import java.io.IOException;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import java.util.Date;


/**
 * Generate a file in rule format from a QueryEngine or a directory of templates
 * Olivier Corby, Wimmics INRIA 2013
 */ 
public class TemplatePrinter {
	
	String from, to;
	FileWriter fw;
        StringBuilder sb;
	static final String NL 			= System.getProperty("line.separator");
	
        TemplatePrinter(){
             sb = new StringBuilder();            
        }
        
        TemplatePrinter(String from){
            this();
            this.from = from;
	}
        
	TemplatePrinter(String from, String to){
            this();
            this.from = from;
            this.to = to;
	}
	
	public static TemplatePrinter create(String from, String to){
		return new TemplatePrinter(from, to);
	}
	
	public static TemplatePrinter create(String from){
		return new TemplatePrinter(from);
	}
	public StringBuilder process() throws IOException, LoadException {
		Graph g = Graph.create();
		Load ld = Load.create(g);
		ld.parseDirRec(from);
		QueryEngine qe = ld.getQueryEngine();
                if (qe == null){
                    throw new LoadException(new IOException("No templates"));
                }
                else {
                    header();

                    for (Query q : qe.getNamedTemplates()){
                            process((ASTQuery) q.getAST());
                    }
                    for (Query q : qe.getQueries()){
                            process( q.getAST());
                    }		
                    trailer();
                    result();
                }
                
                return sb;
	}
        
        void result() throws IOException{
            if (to != null){
                fw = new FileWriter(to);
                fw.write(sb.toString());
                fw.flush();
		fw.close();            
            }
        }
	
	void header() throws IOException{
		write("<?xml version='1.0' encoding='UTF-8'?>");
                write("<!--");
                write("SPARQL Template Transformation");
                write("Olivier Corby - Wimmics - Inria UCA I3S");
                write(new Date().toString());
                write("-->");
		write("<rdf:RDF  xmlns:rdf='" + NSManager.RDF + "'");
		write("  xmlns='" + NSManager.STL + "'>");	
	}
	
	void trailer() throws IOException{
		write("</rdf:RDF>");
	}
	
	void process(ASTQuery ast) throws IOException{
		write("<rule>");
		write("<body>");
		write("<![CDATA[");
		write(ast.getText());
		write("]]>");
		write("</body>");
		write("</rule>");
		write("");
	}
	
	void write(String str) throws IOException{
		sb.append(str);
                sb.append(NL);
	}

}
