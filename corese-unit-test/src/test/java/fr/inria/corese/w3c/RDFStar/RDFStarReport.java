package fr.inria.corese.w3c.RDFStar;

import java.io.FileWriter;
import java.io.IOException;

import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class RDFStarReport {

    static final String root = "urn:x-shacl-test:";

    static final String header = "@prefix doap: <http://usefulinc.com/ns/doap#> .\n"
            + "@prefix earl: <http://www.w3.org/ns/earl#> .\n"
            + "@prefix owl:  <http://www.w3.org/2002/07/owl#> .\n"
            + "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n"
            + "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"
            + "@prefix dc:   <http://purl.org/dc/terms/> .\n"
            + "\n"

            + "<> foaf:primaryTopic <http://project.inria.fr/corese>;\n"
            + "dc:issued %s ;\n"
            + "foaf:maker <http://team.inria.fr/wimmics> .\n"
            + "\n"

            + "<http://project.inria.fr/corese>\n"
            + "  a doap:Project, earl:Software, earl:TestSubject ;\n"
            + "doap:name \"Corese\" ;\n"
            + "doap:release [\n"
            + "   doap:name \"Corese 4.3.1\" ;\n"
            + "   doap:revision \"4.3.1\" ;\n"
            + "   doap:created \"2022-02-02\"^^xsd:date ;\n"
            + "] ;\n"
            + "doap:developer <http://team.inria.fr/wimmics> ;\n"
            + "doap:homepage  <http://project.inria.fr/corese>;\n"
            + "doap:description \"Corese Semantic Web Factory, Inria, UCA.\"@en ; \n"
            + "doap:programming-language \"Java\" .\n\n"

            + "<http://team.inria.fr/wimmics> a foaf:Organization, earl:Assertor;\n"
            + "foaf:name \"Wimmics team\";\n"
            + "foaf:homepage <http://team.inria.fr/wimmics> .\n\n";

    static final String result = "[\n"
            + "  rdf:type earl:Assertion ;\n"
            + "  earl:assertedBy <http://team.inria.fr/wimmics> ;\n"
            + "  earl:subject    <http://project.inria.fr/corese> ;\n"
            + "  earl:test <%s> ;\n"
            + "  earl:result [\n"
            + "      a earl:TestResult ;\n"
            + "      earl:outcome %s ;\n"
            + "      dc:date %s ;\n"
            + "      earl:mode earl:automatic ;\n"
            + "    ] \n"
            + "].\n";

    StringBuilder sb;
    String path;

    RDFStarReport(String path) {
        this(path, "SHACL");
    }

    RDFStarReport(String path, String title) {
        sb = new StringBuilder();
        sb.append(String.format(header, DatatypeMap.newDate()));
        this.path = path;
    }

    void result(String test, boolean b) {
        sb.append(String.format(result, test, value(b), DatatypeMap.newDate()));
    }

    String value(boolean b) {
        return (b) ? "earl:passed" : "earl:failed";
    }

    String clean(String uri) {
        return root.concat(uri.substring(path.length() - 1));
    }

    void write(String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
    }
}
