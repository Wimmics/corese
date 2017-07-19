/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.rdf_to_graph.app;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.RdfToGraph.DbDriver;
import fr.inria.corese.rdftograph.driver.GdbDriver;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import java.util.Optional;

/**
 * @author edemairy
 */
public class App {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: rdfToGraph fileName db_dir [backend]");
            System.err.println("if the parser cannot guess the format of the input file, NQUADS is used.");
            System.err.print("known backend");
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
        String rdfPattern = args[0];
        System.err.println("path to rdf files = " + rdfPattern);
        String patternWithoutPath = rdfPattern.substring(rdfPattern.lastIndexOf("/"), rdfPattern.length());
        String dbPath = String.join("/", args[1], GdbDriver.filePatternToDbPath(patternWithoutPath));
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(rdfPattern);

        RdfToGraph.build().setDriver(driver).convertFileToDb(rdfPattern, format.orElse(RDFFormat.NQUADS), dbPath);
    }
}
