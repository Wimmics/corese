package fr.inria.corese.rdf;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.RdfToGraph.AddressesFilterInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author edemairy
 */
public class TestFilterStream {

	static String TEST_FILE;

	@BeforeClass
	static public void init() {
		ClassLoader classLoader = TestFilterStream.class.getClassLoader();
		TEST_FILE = classLoader.getResource("test.txt").getFile();
	}

	@Test
	public void filterTest() throws FileNotFoundException, IOException {

		String expectedResult = "line 2\nline 3\nline 4\n";
		AddressesFilterInputStream result = new AddressesFilterInputStream(new FileInputStream(TEST_FILE), 2, 5);
		int c;
		StringBuilder actualResultBuilder = new StringBuilder();
		while ((c = result.read()) != -1) {
			actualResultBuilder.append((char) c);
		}
		String actualResult = actualResultBuilder.toString();
		assertEquals("Strings should be equal", expectedResult, actualResult);
	}

	@Test
	public void filterAllTest() throws FileNotFoundException, IOException {
		String expectedResult = "line 0\nline 1\nline 2\nline 3\nline 4\nline 5\nline 6\nline 7\nline 8\nline 9\n\n";
		AddressesFilterInputStream result = new AddressesFilterInputStream(new FileInputStream(TEST_FILE), 0, AddressesFilterInputStream.INFINITE);
		int c;
		StringBuilder actualResultBuilder = new StringBuilder();
		while ((c = result.read()) != -1) {
			actualResultBuilder.append((char) c);
		}
		String actualResult = actualResultBuilder.toString();
		assertEquals("Strings should be equal", expectedResult, actualResult);
	}
}
