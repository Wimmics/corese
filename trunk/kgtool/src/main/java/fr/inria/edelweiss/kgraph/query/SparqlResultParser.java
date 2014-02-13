/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author gaignard
 */
public class SparqlResultParser {

    public List<HashMap<String, String>> parse(String s) {
        try {
            List<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(s.getBytes()));
            doc.getDocumentElement().normalize();

            NodeList results = doc.getElementsByTagName("result");
            for (int i = 0; i < results.getLength(); i++) {
                Node result = results.item(i);
                NodeList bindings = result.getChildNodes();
                HashMap<String, String> map = new HashMap<String, String>();
                for (int j = 0; j < bindings.getLength(); j++) {
                    Node binding = bindings.item(j);
                    if ((binding != null) && (binding.getNodeName().equals("binding"))) {
                        String var = "?" + binding.getAttributes().getNamedItem("name").getNodeValue();
                        NodeList values = binding.getChildNodes();
                        for (int k = 0; k < values.getLength(); k++) {
                            Node val = values.item(k);
                            if ((val != null) && (val.getNodeName() != null) && (val.getNodeName().equals("uri"))) {
                                String sVal = val.getTextContent();
                                map.put(var, "<"+sVal+">");
                            } else if ((val != null) && (val.getNodeName() != null) && (val.getNodeName().equals("literal"))) {
                                String sVal = val.getTextContent();
                                map.put(var, "'''"+ sVal +"'''");
                            }
//                            if ((val != null) && (val.getNodeName() != null) ) {
//                                String sVal = val.getTextContent();
//                                map.put(var, sVal);
//                            }
                        }
                    }
                }
                if (!map.isEmpty()) {
                    resultList.add(new HashMap<String, String>(map));
                }
            }

            return resultList;

        } catch (SAXException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
