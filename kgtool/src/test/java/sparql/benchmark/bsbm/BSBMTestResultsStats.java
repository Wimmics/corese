package sparql.benchmark.bsbm;

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

    private final static int QUERY_NO = 17;
    private static String[] rFiles = new String[]{"100", "300", "500", "1000", "2000", "3000", "5000", "7000", "10000", "15000", "20000"};

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        String u = "/Users/fsong/NetBeansProjects/bsbmtools-0.2/benchmark";
        if(args.length>0 && args[0]!=null){
            u = args[0];
        }
        double[][] results = new double[rFiles.length][QUERY_NO];
        double[][] all = new double[rFiles.length][2];
        
        for (int j = 0; j < rFiles.length; j++) {

            File fXmlFile = new File(u + rFiles[j] + ".xml");
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
                    if(eElement.getElementsByTagName("aqet")==null) continue;
                    
                    results[j][index] = Double.valueOf(eElement.getElementsByTagName("aqet").item(0).getTextContent());
                }
            }
            
            all[j][1] = Double.valueOf(doc.getElementsByTagName("cqet").item(0).getTextContent());
            all[j][0] = Double.valueOf(doc.getElementsByTagName("qmph").item(0).getTextContent());
            
        }
        print(results, all);
    }

    public static void print(double[][] r, double[][] all) {
        String s = "Scales\t---qmph---\t---cqet--\t";
        //print title
        for (int i = 0; i < QUERY_NO; i++) {
            s += "---Q" + (i + 1) + "---\t";
        }
        s += "\n";

        for (int i = 0; i < r.length; i++) {
            s += rFiles[i]+"\t"+String.format("%.6f",all[i][0])+"\t"+String.format("%.6f",all[i][1])+"\t";
            for (int j = 0; j < QUERY_NO; j++) {
                s+=String.format("%.6f", r[i][j])+"\t";
            }
            s+="\n";
        }
        System.out.println(s);
    }
}
