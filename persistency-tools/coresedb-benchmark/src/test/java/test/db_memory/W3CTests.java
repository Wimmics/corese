/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db_memory;

import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.corese.w3c_tests.Runner.SPARQL11_DIR;
import fr.inria.corese.w3c_tests.TestDescription;
import fr.inria.corese.w3c_tests.W3CIterator;
import fr.inria.wimmics.coresetimer.CoreseAdapter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.openrdf.rio.Rio;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Apply the W3C SPARQL Tests
 *
 * @author edemairy
 */
public class W3CTests implements ITest {

	private String mTestCaseName;

	private int nbTest = 0;
	@Test(dataProvider = "w3ctests", groups = "")
	public void testW3C(fr.inria.corese.w3c_tests.TestDescription test) throws Exception {
		String inputFile = test.getInputURL().get();
		RdfToGraph.build()
			.setDriver(RdfToGraph.DbDriver.NEO4J)
			.convertFileToDb( inputFile, Rio.getParserFormatForFileName(inputFile).get(), String.format("test_%d_db",nbTest));
		nbTest++;
	}

	@DataProvider(name = "w3ctests", parallel = false)
	public Iterator<Object[]> createData() throws IOException {
		ArrayList<Object[]> result = new ArrayList<>();
		Iterator<fr.inria.corese.w3c_tests.TestDescription> tests = W3CIterator.build(SPARQL11_DIR).iterator();
		while (tests.hasNext()) {
			result.add(new Object[]{tests.next()});
		}
		return result.iterator();
	}

	@BeforeMethod(alwaysRun = true)
	public void testData(Method method, Object[] testData) {
		TestDescription test = (TestDescription) testData[0];
		this.mTestCaseName = String.format("%s (%s)", test.getName(), test.getComment());
	}
	@Override
	public String getTestName() {
		return this.mTestCaseName;
	}

}
