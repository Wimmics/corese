package fr.inria.edelweiss.kgtool.load;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;

/**
 * Rule Loader as construct-where SPARQL Queries
 * Can also load Corese rule format
 * 
 * Olivier Corby, Edelweiss INRIA 2011
 * 
 */

public class RuleLoad {
	static final String NS 		= "http://ns.inria.fr/edelweiss/2011/rule#";
	static final String BRUL 	= "http://ns.inria.fr/corese/2008/rule#";
	static final String COS 	= "http://www.inria.fr/acacia/corese#";
	
	static final String BODY 	= "body";
	static final String RULE 	= "rule";
	static final String VALUE 	= "value";
	static final String PREFIX1 	= "prefix";
	static final String PREFIX2 	= "PREFIX";

	static final String IF 		= "if";
	static final String THEN 	= "then";
	static final String CONST 	= "construct";
	static final String WHERE 	= "where";

	

	
	RuleEngine engine;
	
	RuleLoad(RuleEngine e){
		engine = e;
	}
	
	public static RuleLoad create(RuleEngine e){
		return new RuleLoad(e);
	}
	
	public void load(String file){
		Document doc = parse(file);
		
		NodeList list = doc.getElementsByTagNameNS(NS, BODY);
		
		if (list.getLength() == 0){
			list = doc.getElementsByTagNameNS(BRUL, VALUE);
		}
		if (list.getLength() == 0){
			loadCorese(doc);
			return;
		}
		for (int i=0; i<list.getLength(); i++){
			Node node = list.item(i);
			String rule = node.getTextContent();
			try {
				engine.defRule(rule);
			} catch (EngineException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Corese format
	 */
	public void loadCorese(String file){
		Document doc = parse(file);
		loadCorese(doc);
	}
		
	void loadCorese(Document doc){
		NodeList list = doc.getElementsByTagNameNS(COS, RULE);
		for (int i=0; i<list.getLength(); i++){
			Element node = (Element) list.item(i);
			NodeList lconst = node.getElementsByTagNameNS(COS, THEN);
			NodeList lwhere = node.getElementsByTagNameNS(COS, IF);

			String rule = getRule(((Element) lconst.item(0)), ((Element) lwhere.item(0)));

			try {
				engine.defRule(rule);
			} catch (EngineException e) {
				e.printStackTrace();
			}
		}
	}

	
	String getRule(Element econst, Element ewhere){
		String sconst = econst.getTextContent().trim();
		String swhere = ewhere.getTextContent().trim();
		String pref = "";

		if (swhere.startsWith(PREFIX1) || swhere.startsWith(PREFIX2)){
			int ind = swhere.indexOf("{");
			pref = swhere.substring(0, ind) ;
			swhere = swhere.substring(ind);
		}
		
		String rule = pref  + CONST + sconst + "\n" + WHERE + swhere;

		return rule;

	}
	
	
	private  Document parse(String xmlFileName){
	     
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

}
