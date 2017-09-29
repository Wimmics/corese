/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.w3c;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author edemairy
 */
public class SparqlTester {

    private static final Logger logger = LogManager.getLogger(SparqlTester.class);

    public static void main(String... args) throws IOException {
        ClassLoader classLoader = SparqlTester.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("rdf-tests/LICENSE.md");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (reader.ready()) {
            logger.info(reader.readLine());
        }
        reader.close();
        URL urlRessource = classLoader.getResource("rdf-tests/LICENSE.md");
        logger.info("url = {}", urlRessource);
//		FileSystem fs = FileSystems.
    }
}
