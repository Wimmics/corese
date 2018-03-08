package test.rdfa;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgengine.QueryResults;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Helper class for running RDFa parser testing cases/suites
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Jan 2014 new
 */
public class RdfaTestHelper {

    private static Graph graph;

    private static final boolean EXPECTED_RESULT_TRUE = true;
    private static final boolean EXPECTED_RESULT_FALSE = false;
    private static final String TEST_CASE = RdfaTestHelper.class.getClassLoader().getResource("data").getPath() + "/rdfa1.1/testcase/";
    private static final String BASE = "http://rdfa.info/test-suite/test-cases/rdfa1.1/";
    private static final String EXT_SPARQL = ".sparql";
    private static final int NAME_LENGTH = 16;

    public final static String HTML4 = "html4";
    public final static String HTML5 = "html5";
    public final static String SVG = "svg";
    public final static String XHTML1 = "xhtml1";
    public final static String XHTML5 = "xhtml5";
    public final static String XML = "xml";

    //test that needs to return a false result
    private final static String[] NEGATIVE_TEST = new String[]{
        "html4/0107.html", "html4/0122.html", "html4/0140.html", "html4/0180.html", "html4/0258.html", "html4/0311.html",
        "html5/0107.html", "html5/0122.html", "html5/0140.html", "html5/0311.html",
        "xhtml1/0107.xhtml", "xhtml1/0122.xhtml", "xhtml1/0140.xhtml", "xhtml1/0180.xhtml", "xhtml1/0258.xhtml", "xhtml1/0311.xhtml",
        "xhtml5/0107.xhtml", "xhtml5/0122.xhtml", "xhtml5/0140.xhtml", "xhtml5/0311.xhtml",
        "svg/0311.svg",
        "xml/0107.xml", "xml/0122.xml", "xml/0140.xml", "xml/0180.xml", "xml/0258.xml", "xml/0311.xml"};

    /**
     * Run each test case
     *
     * @param filePath file url
     * @param sparqlPath url of sparql statement file
     * @throws java.io.IOException
     */
    public void process(String filePath, String sparqlPath) throws IOException {

        System.out.println("####Test file:..." + filePath);

        loadSource(filePath);

        try {
            String query = readSparqlStatement(sparqlPath);
            //String query = readSparqlStatementFromFile(sparqlPath);
            //System.out.print("##query##:\n" + query);

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            QueryResults res = QueryResults.create(map);
            //System.out.println("##Ast##:\n" + map.getQuery().toString() + "\n");

            //real result
            boolean realResult = false;
            if (res.isAsk() && map.size() != 0) {
                realResult = true;
            }

            //expected result
            boolean expectedResult = EXPECTED_RESULT_TRUE;
            if (isNegativeTest(filePath)) {
                expectedResult = EXPECTED_RESULT_FALSE;
            }
            assertEquals(filePath.substring(filePath.length() - NAME_LENGTH), expectedResult, realResult);
        } catch (EngineException ex) {
            fail();
        }
    }

    //check if the given test is in the negative test
    private boolean isNegativeTest(String filePath) {
        if (null == filePath || filePath.isEmpty()) {
            return false;
        }

        for (String file : NEGATIVE_TEST) {
            if (filePath.toLowerCase().endsWith(file)) {
                return true;
            }
        }
        return false;
    }

    //get extension of file according to the file format
    private String getFileExt(String fileFormat) {
        if (null == fileFormat || fileFormat.isEmpty()) {
            return null;
        }

        String ext = ".html";
        if (fileFormat.equalsIgnoreCase(HTML4) || fileFormat.equalsIgnoreCase(HTML5)) {
            ext = ".html";
        } else if (fileFormat.equalsIgnoreCase(XHTML1) || fileFormat.equalsIgnoreCase(XHTML5)) {
            ext = ".xhtml";
        } else if (fileFormat.equalsIgnoreCase(SVG)) {
            ext = ".svg";
        } else if (fileFormat.equalsIgnoreCase(XML)) {
            ext = ".xml";
        }

        return ext;
    }

    //Load the parsed triples to corese graph
    private void loadSource(String fileUri) {
        graph = Graph.create(true);
        Load ld = Load.create(graph);

        try {
            ld.load(fileUri, fileUri, fileUri);
        } catch (IllegalMonitorStateException e) {
            //System.out.println("illegal monitor state exception: ignore this error");
        } catch (LoadException ex) {
            System.out.println("load exception:" + fileUri + "\n");
        }
    }

    /**
     * Generate test suites
     *
     * @param format the file format (html4, html5, etc..)
     * @return list of parameters
     */
    public Object[][] generateTestSuite(String format) {

        List<Object[]> list = new ArrayList<Object[]>();
        String testCaseListFilePath = TEST_CASE + format + ".txt";
        String ext = getFileExt(format);

        try {
            String line;
            Object[] o;
            InputStream is = new FileInputStream(testCaseListFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                o = new Object[]{BASE + format + "/" + line + ext, BASE + format + "/" + line + EXT_SPARQL};
                list.add(o);
            }
            reader.close();
            is.close();
        } catch (FileNotFoundException ex) {
            LogManager.getLogger(RdfaTestHelper.class.getName()).log(Level.ERROR, "", ex);
        } catch (IOException ex) {
            LogManager.getLogger(RdfaTestHelper.class.getName()).log(Level.ERROR, "", ex);
        }

        //convert list to object array
        Object[][] suite = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            suite[i] = list.get(i);
        }
        return suite;
    }

    //read sparql statement from local file
    private String readSparqlStatementFromFile(String uri) throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line, sparql = "";
        while ((line = reader.readLine()) != null) {
            sparql += line + "\n";
        }
        reader.close();
        is.close();
        return sparql;
    }

    //read sparql statement from uri
    private String readSparqlStatement(String uri) throws MalformedURLException, IOException {

        URL url = new URL(uri);
        String sparql = "";
        InputStream in = url.openStream();
        HttpURLConnection l_connection = (HttpURLConnection) url.openConnection();
        l_connection.connect();
        BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in, "utf-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            sparql += line + "\n";
        }
        reader.close();
        in.close();

        l_connection.disconnect();
        return sparql;
    }
}
