package fr.inria.corese.sparql.benchmark.bsbm;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generate the synthese of the BSBM testing results obtained by BSBM tools
 *
 * BSBM.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 24 avr. 2014
 */
public class BSBMTestResultsStats {

    private static String HOME_DATA = "/Users/fsong/NetBeansProjects/bsbm/results/";
    private final static String[] scale = new String[]{"100", "300", "500", "1000", "2000", "3000", "5000", "7000", "10000", "15000", "20000"};

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        if (args.length > 0 && args[0] != null) {
            HOME_DATA = args[0];
        }
        readOneTest("CoreseQPv0/bi/", 8);
        readOneTest("CoreseQPv0/biq4/", 4);
        //readOneTest("CoreseQPv0/explore/", 12);
        //readOneTest("CoreseQPv1/bi/", 8);
        //readOneTest("CoreseQPv1/biq4/", 4);
//        readOneTest("Jena/bi/", 8);
//        readOneTest("Jena/b4/", 8);
//        readOneTest("Jena/b8/", 8);
//        readOneTest("Sesame/bi/", 8);
//        readOneTest("Sesame/biq4/", 4);
    }

    public static void readOneTest(String testcase, int numberOfQueries) throws ParserConfigurationException, SAXException, IOException {

        double[][] results = new double[scale.length][numberOfQueries];
        double[][] all = new double[scale.length][2];

//        String singleQuery ;
//        if (options != null) {
//            int q = Integer.valueOf(options[1].toString());
//        }
        for (int j = 0; j < scale.length; j++) {

            File fXmlFile = new File(HOME_DATA + testcase + "benchmark" + scale[j] + ".xml");
            if (!fXmlFile.exists()) {
                all[j][1] = -1;
                all[j][0] = -1;
                continue;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("query");

            for (int i = 0; i < nList.getLength(); i++) {
                Node n = nList.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) n;
                    int index = Integer.valueOf(eElement.getAttribute("nr")) - 1;
                    if (eElement.getElementsByTagName("aqet") == null) {
                        continue;
                    }

                    results[j][index] = Double.valueOf(eElement.getElementsByTagName("aqet").item(0).getTextContent());
                }
            }

            all[j][1] = Double.valueOf(doc.getElementsByTagName("cqet").item(0).getTextContent());
            all[j][0] = Double.valueOf(doc.getElementsByTagName("qmph").item(0).getTextContent());

        }
        System.out.println("=====" + HOME_DATA + testcase + " ======");
        print(results, all, numberOfQueries);
    }

    public static void print(double[][] r, double[][] all, int numberOfQueries) {
        String s = "Scales\t---qmph---\t---cqet--\t";
        //print title
        for (int i = 0; i < numberOfQueries; i++) {
            s += "---Q" + (i + 1) + "---\t";
        }
        s += "\n";

        for (int i = 0; i < r.length; i++) {
            s += scale[i] + "\t" + String.format("%.6f", all[i][0]) + "\t" + String.format("%.6f", all[i][1]) + "\t";
            for (int j = 0; j < numberOfQueries; j++) {
                s += String.format("%.6f", r[i][j]) + "\t";
            }
            s += "\n";
        }
        System.out.println(s);
    }
}
