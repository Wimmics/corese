package fr.inria.corese.test.research;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.core.Mappings;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EarlReport {
    
   static final String root = "urn:x-shacl-test:" ;
    
    static final String header = 
            "@prefix doap: <http://usefulinc.com/ns/doap#> .\n"
            + "@prefix earl: <http://www.w3.org/ns/earl#> .\n"
            + "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
            + "\n"
            + "<http://wimmics.inria.fr/corese>\n"
            + "  rdf:type doap:Project ;\n"
            + "  rdf:type earl:Software ;\n"
            + "  rdf:type earl:TestSubject ;\n"
            + "  doap:developer <http://www.inria.fr/sophia/members/Olivier.Corby> ;\n"
            + "  doap:name \"Corese SHACL\" ;\n"
            + "  doap:date %s \n"
            + ".\n\n";

    static final String result =
            "[\n"
            + "  rdf:type earl:Assertion ;\n"
            + "  earl:assertedBy <http://wimmics.inria.fr> ;\n"
            + "  earl:result [\n"
            + "      rdf:type earl:TestResult ;\n"
            + "      earl:mode earl:automatic ;\n"
            + "      earl:outcome %s ;\n"
            + "    ] ;\n"
            + "  earl:subject <http://wimmics.inria.fr/corese> ;\n"
            + "  earl:test <%s> ;\n"
            + "].\n";
    
    StringBuilder sb;
    String path;

    EarlReport(String path) {
        sb = new StringBuilder();
        sb.append(String.format(header, DatatypeMap.newDate()));
        this.path = path;
    }

    void result(Mappings map, boolean b) {
        IDatatype res = (IDatatype) map.getValue("?res");
        sb.append(String.format(result, value(b), clean(res.stringValue())));
    }
    
    String value(boolean b){
        return (b) ? "earl:passed" : "earl:failed" ;
    }
    
    String clean(String uri){
        return root.concat(uri.substring(path.length() - 1));
    }

    void write(String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
    }
}
