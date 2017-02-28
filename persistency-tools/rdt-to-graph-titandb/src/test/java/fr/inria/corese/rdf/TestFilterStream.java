package fr.inria.corese.rdf;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.corese.rdftograph.RdfToGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author edemairy
 */
public class TestFilterStream {
	
	@Test
	public void filterTest() throws FileNotFoundException, IOException {
		String expectedResult = "line 2\nline 3\nline 4\n";
		RdfToGraph.AddressesFilterInputStream result = new RdfToGraph.AddressesFilterInputStream(new FileInputStream("/Users/edemairy/Developpement/merge2/my_branch/persistency-tools/rdt-to-graph-titandb/src/test/java/fr/inria/corese/rdf/test.txt"), 2, 5);
		int c;
		StringBuilder actualResult = new StringBuilder();
		while ( (c = result.read()) != -1) {
			actualResult.append((char)c);
		}
		assertSame("Strings should be equal", expectedResult, actualResult);
	}
}
