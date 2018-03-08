package fr.inria.corese.kgimport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.corese.kgimport.RdfSplitter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class RdfSplitterTest {

    public RdfSplitterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void rdfFragment() throws ParseException, IOException, FileNotFoundException, URISyntaxException {
//        String inPath = "/Users/gaignard/Documents/Experiences/ExpeFedBench-2013/FedBench-DS-2013/kegg/KEGG-2010-11";
        
        URL in = JenaLoadingTest.class.getClassLoader().getResource("fromKEGG");
        File f = new File(in.toURI());
        String inPath = f.getAbsolutePath();
        
        String c1 = "-i "+inPath+" "
                + "-o /tmp/frag -n 4";

//        String c2 = "-i "+inPath
//                + "-o /tmp/frag";

//        String c3 = "-i "+inPath
//                + "-o /tmp/frag -n 4 -f 10 -f 20";

        String c4 = "-i "+inPath+" "
                + "-o /tmp/frag -f 10 -f 15";

        String c5 = "-i "+inPath+" "
                + "-o /tmp/frag -f 10 -tdb";

        String c6 = "-i "+inPath+" "
                + "-o /tmp/frag -p http://bio2rdf.org/ns/kegg -p http://bio2rdf.org/ns/bio2rdf";

        ArrayList<String> commandLines = new ArrayList<String>();
        commandLines.add(c1);
//        commandLines.add(c2);
//        commandLines.add(c3);
        commandLines.add(c4);
        commandLines.add(c5);
        commandLines.add(c6);

        for (String c : commandLines) {
            RdfSplitter.main(c.split(" "));
        }
    }
}
