/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.rdf.to.graph.app;

import fr.inria.corese.rdftograph.RdfToGraph;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import fr.inria.corese.rdftograph.RdfToGraph.DbDriver;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
/**
 *
 * @author edemairy
 */
public class App {
	public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
		if (args.length < 2) {
			System.err.println("Usage: rdfToGraph fileName db_path [backend]");
			System.err.println("if the parser cannot guess the format of the input file, NQUADS is used.");
			System.err.print("knwown backends ");
			for (DbDriver driver : DbDriver.values()) {
				System.err.print(driver + " ");
			}
			System.exit(1);
		}
		DbDriver driver = DbDriver.NEO4J;
		if (args.length >= 3) {
			try {
				driver = DbDriver.valueOf(args[2].toUpperCase());
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}
		}
		String rdfFileName = args[0];
		String dbPath = args[1];	
		Optional<RDFFormat> format = Rio.getParserFormatForFileName(rdfFileName);

		new RdfToGraph().setDriver(driver).convertFileToDb(rdfFileName, format.orElse(RDFFormat.NQUADS), dbPath);
	}	
}
