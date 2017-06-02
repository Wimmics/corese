package fr.inria.corese.rdf;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.RdfToGraph.AddressesFilterInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	@Test
	public void testMakeStream_readSimpleFile() throws IOException {
		String expectedResult = "file0:line0\nfile0:line1\nfile1:line0\nfile1:line1\n";
		BufferedReader reader = new BufferedReader(new InputStreamReader(RdfToGraph.makeStream("test.txt")));
		StringBuilder actualResultBuilder = new StringBuilder();
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			actualResultBuilder.append(currentLine);
			actualResultBuilder.append(String.format("%n"));
		}
		String actualResult = actualResultBuilder.toString();	
		assertEquals("Strings should be equal", expectedResult, actualResult);
	}

	@Test
	public void testMakeStream_concatSimpleText() throws IOException {
		String expectedResult = "file0:line0\nfile0:line1\nfile1:line0\nfile1:line1\nfile2:line0\nfile2:line1\n";
		BufferedReader reader = new BufferedReader(new InputStreamReader(RdfToGraph.makeStream("./src/test/resources/file[0-2].txt")));
		StringBuilder actualResultBuilder = new StringBuilder();
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			actualResultBuilder.append(currentLine);
			actualResultBuilder.append(String.format("%n"));
		}
		String actualResult = actualResultBuilder.toString();	
		assertEquals("Strings should be equal", expectedResult, actualResult);
	}
	@Test
	public void testMakeStream_concatGzipText() throws IOException {
		String expectedResult = "file0:line0\nfile0:line1\nfile1:line0\nfile1:line1\nfile2:line0\nfile2:line1\n";
		BufferedReader reader = new BufferedReader(new InputStreamReader(RdfToGraph.makeStream("./src/test/resources/file[0-2].txt.gz")));
		StringBuilder actualResultBuilder = new StringBuilder();
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			actualResultBuilder.append(currentLine);
			actualResultBuilder.append(String.format("%n"));
		}
		String actualResult = actualResultBuilder.toString();	
		assertEquals("Strings should be equal", expectedResult, actualResult);
	}
}
