package fr.inria.corese.core.print;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;


/**
 * Exec a sparql query inside XSLT using KGRAM
 * 
 * Add this in xslt header:
 *   xmlns:xalan="http://xml.apache.org/xalan"
 *   xmlns:server="xalan://fr.inria.corese.kgtool.print.XSLTQuery"
 *   extension-element-prefixes="server"
 *   
 * Exec query using this code:
 *   <xsl:variable name='res'  select='server:sparql($engine, $query)' />
 * 
 * Olivier Corby & Fabien Gandon, Edelweiss INRIA 2011
 * 
 */
public class XSLTQuery {
	String xsl;
	QuerySolver exec;
	
	
	XSLTQuery(String x, QuerySolver e){
		xsl = x;
		exec = e;
	}
	
	public static XSLTQuery create(String x, QuerySolver e){
		return new XSLTQuery(x, e);
	}
	
	
	QuerySolver getQuerySolver(){
		return exec;
	}
	
	/**
	 * Call XSLT with sparql query inside
	 */
	public String xslt(String doc){
		TransformerFactory fac = TransformerFactory.newInstance();
		try {
			DOMResult dres = new DOMResult();
			// default string result is xsl name
			Transformer trans = fac.newTransformer(new StreamSource(xsl));
			// transmit the engine as XSL parameter to enable sparql() query
			trans.setParameter("engine", this);
			// result as a DOM object
			//trans.transform(new StreamSource(doc), dres);
			// result as a string in addition to DOM (for trace)
			// use case: xslt(?d, ?xsl, 's')
			StringWriter wrt = new StringWriter();
			trans.transform(new StreamSource(doc), new StreamResult(wrt));
			String str = wrt.toString();
			return str;
		} 
		catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * SPARQL Query called by XSLT
	 * return a DOM Element
	 */
	public static Node sparql(Object server, String query) {
		XSLTQuery xslt = (XSLTQuery) server;
		String str = xslt.query(query);
		if (str == null) return null;
		Document doc =  xslt.parseXML(str);
		return doc.getDocumentElement() ;
	}
	
	String query(String query){
		try {
			Mappings map = exec.query(query);
			Query q = map.getQuery();
			ASTQuery ast =  q.getAST();
			
			String str = null;
			if (q.isConstruct() && map.getGraph()!=null){
				Graph g = (Graph) map.getGraph();
				RDFFormat p = RDFFormat.create(g, ast.getNSM());
				str = p.toString();
			}
			else {
				XMLFormat f = XMLFormat.create(map);
				str = f.toString();
			}
			
			return str;
		} 
		catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	
	
	 /**
	   * creates a new document by parsing a string containing some XML.
	   *@param p_XML XML representation of the document.
	   *@return the document that resulted from the parsing.
	   */
	  public Document parseXML(String p_XML) {
	    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = null;
	    fac.setNamespaceAware(true);
	    try {
	      builder = fac.newDocumentBuilder();
	    }
	    catch (ParserConfigurationException l_pce) {
	    	//l_pce.printStackTrace();
	    	//logger.error(l_pce);
	    }

	    Document l_Doc = null;
	    try {
	      l_Doc = builder.parse(new InputSource(new StringReader(p_XML)));
	      l_Doc.normalize();
	    }
	    catch (Exception l_e) {
	    	//l_e.printStackTrace();
	    	//logger.error(l_e);
	    }
	    return (l_Doc);
	  }
	  

}
