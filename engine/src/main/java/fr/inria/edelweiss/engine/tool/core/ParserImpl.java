package fr.inria.edelweiss.engine.tool.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.engine.tool.api.Parser;

public class ParserImpl implements Parser {
	static String XPATH_RULE = "/rdf:RDF/rule:rule"; ///rule:value/text()";
	static String XPATH_VALUE = "rule:value/text()";
	static String XPATH_QUERY = "rule:query/text()";


	static String RULE  = "http://ns.inria.fr/corese/2008/rule#";
	
	/**
	 * get a document to load and parse the file XML
	 */
	private  Document getDocument(String xmlFileName){
     
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true); 
		DocumentBuilder builder;
		try {
			builder = fac.newDocumentBuilder();
			Document doc = builder.parse(xmlFileName);
			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> extractSPARQLQuery(String file, String xPath, String parameter) {
		
		//create the document to load and parse the file XML
		Document document = getDocument(file);
		if (document == null) return new ArrayList<String>();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(defContext());
		
		//get the list of nodes containing the rule as string
		try {
			NodeList nodes;
			nodes = (NodeList) xpath.evaluate(XPATH_RULE, document, XPathConstants.NODESET);
			//list containing the SPARQLQueries as strings
			List<String> sparqlQueries=new ArrayList<String>();

			//iterate the list of nodes
			for (int i = 0; i < nodes.getLength(); i++) {

				Node rule = nodes.item(i);
				
				Node value = (Node) xpath.evaluate(XPATH_VALUE, rule, XPathConstants.NODE);
				Node query = (Node) xpath.evaluate(XPATH_QUERY, rule, XPathConstants.NODE);

				//get a SPARQLQuery
				String sparqlQuery = value.getNodeValue();

				//add the SPARQLQuery to the list to return
				sparqlQueries.add(sparqlQuery);
			}
			return sparqlQueries;
		}
		catch(XPathExpressionException e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	Hashtable<String, String> defNS(){
		Hashtable<String, String> nsm = new Hashtable<String, String>();
		nsm.put(RDFS.RDFPrefix, RDFS.RDF);
		nsm.put("rule", RULE);
		return nsm;
	}

	NamespaceContext defContext(){
		NamespaceContext context = new NamespaceContext(){
			Hashtable<String, String> nsm = defNS();
			
			public String getNamespaceURI(String prefix) {
				return nsm.get(prefix);
			}

			// This method isn't necessary for XPath processing.
			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}

			// This method isn't necessary for XPath processing either.
			public Iterator getPrefixes(String uri) {
				throw new UnsupportedOperationException();
			}

		};
		return context;
		
	}
	

}
