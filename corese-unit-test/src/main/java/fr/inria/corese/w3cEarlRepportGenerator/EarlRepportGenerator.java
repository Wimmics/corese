package fr.inria.corese.w3cEarlRepportGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.Logger;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * This class generates an EARL report for the Corese software.
 * 
 * The EARL report is a RDF document that describes the conformance of the
 * Corese software to the W3C standards.
 * 
 * @see <a href=
 *      "https://github.com/w3c/rdf-canon/tree/main/reports">Instructions for
 *      submitting implementation reports</a>
 * 
 */
public class EarlRepportGenerator {

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(EarlRepportGenerator.class);

    private final Graph graph;
    // eg "2023-01-25T10:18:04-08:00"
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private final String authorUri = "https://team.inria.fr/wimmics";
    private final String authorName = "Wimmics Team";

    private final String softwareUri = "https://github.com/Wimmics/corese";
    private final String softwareName = "Corese";
    private final String softwareDescription = "Software platform implementing and extending the standards of the Semantic Web.";
    private final String softwareLicense = "http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html";
    private final String softwareHomepage = "https://project.inria.fr/corese/";
    private final String softwareMailingList = "mailto:corese-users@inria.fr";
    private final String softwareDownload = "https://project.inria.fr/corese/download/";
    private final String softwareBugDatabase = "https://github.com/Wimmics/corese/issues";
    private final String softwareBlog = "https://github.com/Wimmics/corese/discussions/";
    private final String softwareProgrammingLanguage = "Java";

    private final String releaseURI = "fc1825918302fec47852dc1f73ad1175c84fd7d1";
    private final String releaseDate = "2024-04-11";

    private final Path reportDir = Path.of("corese-unit-test/src/test/java/fr/inria/corese/w3c/canonicalRdf");
    private final Path inputReportPath = reportDir.resolve("testReport.csv");
    private final Path outputReportPath = reportDir.resolve("earlReport.ttl");

    /**
     * Constructor for the EarlRepportGenerator class.
     */
    public EarlRepportGenerator() {
        this.graph = Graph.create();
    }

    /**
     * Generates the EARL report and writes it to the specified output directory.
     * 
     * @param outputDir the output directory where to write the EARL report
     */
    public void generate() {

        // Insert the document description in the graph
        execSPARQL(insertQueryDescribeDocument());

        // Insert the developer description in the graph
        execSPARQL(insertQueryDescribeDeveloper());

        // Insert the software description in the graph
        execSPARQL(insertQueryDescribeSoftware());

        // Insert the release description in the graph
        execSPARQL(insertQueryDescribeRelease());

        // Generate the EARL report in turtle format
        TripleFormat format = TripleFormat.create(graph, this.getNSM());
        format.addPrefix = false;

        // Add the test results to the EARL report
        try {
            // read line by line the test report file
            // for each line, add the test result to the EARL report

            for (String line : Files.readAllLines(inputReportPath)) {
                String[] values = line.split(",");
                String testUri = values[0];
                String testTime = values[1];
                String testResult = values[2];

                execSPARQL(insertQueryDescribeTestResult(testUri, testTime, testResult));
            }

        } catch (IOException e) {
            logger.error("Error while reading test report file: " + inputReportPath.toString(), e);
            e.printStackTrace();
        }

        // Write the EARL report to the output directory
        try {
            format.write(outputReportPath.toString());
        } catch (IOException e) {
            logger.error("Error while writing EARL report to file: " + outputReportPath.toString(), e);
            e.printStackTrace();
        }

    }

    /**
     * Returns a NSManager with the prefixes used in the EARL report.
     * 
     * @return a NSManager with the prefixes used in the EARL report
     */
    private NSManager getNSM() {
        NSManager nsm = NSManager.create();
        nsm.setRecord(true);
        nsm.definePrefix("earl", "http://www.w3.org/ns/earl#");
        nsm.definePrefix("dc", "http://purl.org/dc/terms/");
        nsm.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
        nsm.definePrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        nsm.definePrefix("doap", "http://usefulinc.com/ns/doap#");
        return nsm;
    }

    /**
     * Executes a SPARQL query on the graph.
     * 
     * @param query the SPARQL query to execute
     */
    private void execSPARQL(String query) {
        QueryProcess exec = QueryProcess.create(graph);
        try {
            exec.query(query);
        } catch (EngineException e) {
            logger.error("Error while executing SPARQL query: " + query, e);
            e.printStackTrace();
        }
    }

    /**
     * Builds a SPARQL query to insert the document description in the graph.
     * 
     * @return a SPARQL query to insert the document description in the graph
     */
    private String insertQueryDescribeDocument() {

        // Calculate the current date and time
        String now = this.dtf.format(ZonedDateTime.now());

        // Build the SPARQL query
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX earl: <http://www.w3.org/ns/earl#>\n");
        sb.append("PREFIX dc: <http://purl.org/dc/terms/>\n");
        sb.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n");
        sb.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n");
        sb.append("INSERT DATA {\n");
        sb.append("    <> foaf:primaryTopic <").append(softwareUri).append("> ;\n");
        sb.append("        dc:issued \"").append(now).append("\"^^xsd:dateTime ;\n");
        sb.append("        foaf:maker <").append(authorUri).append("> .\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Builds a SPARQL query to insert the developer description in the graph.
     * 
     * @return a SPARQL query to insert the developer description in the graph
     */
    private String insertQueryDescribeDeveloper() {
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX earl: <http://www.w3.org/ns/earl#>\n");
        sb.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n");
        sb.append("INSERT DATA {\n");
        sb.append("    <").append(authorUri).append("> a foaf:Person , earl:Assertor ;\n");
        sb.append("        foaf:name \"").append(authorName).append("\" ;\n");

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Builds a SPARQL query to insert the software description in the graph.
     * 
     * @return a SPARQL query to insert the software description in the graph
     */
    private String insertQueryDescribeSoftware() {
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX earl: <http://www.w3.org/ns/earl#>\n");
        sb.append("PREFIX doap: <http://usefulinc.com/ns/doap#>\n");
        sb.append("INSERT DATA {\n");
        sb.append("    <").append(softwareUri).append("> a doap:Project, earl:Software, earl:TestSubject ;\n");
        sb.append("        doap:name \"").append(softwareName).append("\" ;\n");
        sb.append("        doap:release <").append(softwareUri).append("/commit/").append(releaseURI).append("> ;\n");
        sb.append("        doap:developer <").append(authorUri).append("> ;\n");
        sb.append("        doap:homepage <").append(softwareHomepage).append("> ;\n");
        sb.append("        doap:description \"").append(softwareDescription).append("\"@en ;\n");
        sb.append("        doap:license <").append(softwareLicense).append("> ;\n");
        sb.append("        doap:download-page <").append(softwareDownload).append("> ;\n");
        sb.append("        doap:bug-database <").append(softwareBugDatabase).append("> ;\n");
        sb.append("        doap:mailing-list <").append(softwareMailingList).append("> ;\n");
        sb.append("        doap:blog <").append(softwareBlog).append("> ;\n");
        sb.append("        doap:programming-language \"").append(softwareProgrammingLanguage).append("\" .\n");

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Builds a SPARQL query to insert the release description in the graph.
     * 
     * @return a SPARQL query to insert the release description in the graph
     */
    private String insertQueryDescribeRelease() {
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX doap: <http://usefulinc.com/ns/doap#>\n");
        sb.append("INSERT DATA {\n");
        sb.append("    <").append(softwareUri).append("/commit/").append(releaseURI).append("> doap:name \"")
                .append(softwareName).append(" #").append(releaseURI.substring(0, 7)).append("\" ;\n");
        sb.append("        doap:revision \"#").append(releaseURI.substring(0, 7)).append("\" ;\n");
        sb.append("        doap:created \"").append(releaseDate).append("\"^^xsd:date ;\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Builds a SPARQL query to insert the test result in the graph.
     * 
     * @param testUri    the URI of the test
     * @param testTime   the time when the test was executed
     * @param testResult the result of the test
     * @return a SPARQL query to insert the test result in the graph
     */
    private String insertQueryDescribeTestResult(String testUri, String testTime, String testResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX earl: <http://www.w3.org/ns/earl#>\n");
        sb.append("PREFIX dc: <http://purl.org/dc/terms/>\n");
        sb.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n");
        sb.append("INSERT DATA {\n");
        sb.append("    [ a earl:Assertion ;\n");
        sb.append("        earl:assertedBy <").append(authorUri).append("> ;\n");
        sb.append("        earl:subject <").append(softwareUri).append("> ;\n");
        sb.append("        earl:test <").append(testUri).append("> ;\n");
        sb.append("        earl:result [ a earl:TestResult ;\n");
        sb.append("            earl:outcome ").append("<").append(testResult).append(">").append(" ;\n");
        sb.append("            dc:date \"").append(testTime).append("\"^^xsd:dateTime\n");
        sb.append("        ] ;\n");
        sb.append("        earl:mode earl:automatic\n");
        sb.append("    ] .\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        EarlRepportGenerator earlRepportGenerator = new EarlRepportGenerator();
        earlRepportGenerator.generate();
    }

}