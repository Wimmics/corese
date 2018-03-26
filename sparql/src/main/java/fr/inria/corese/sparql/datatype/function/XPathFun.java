package fr.inria.corese.sparql.datatype.function;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseInteger;
import fr.inria.corese.sparql.datatype.CoreseString;
import fr.inria.corese.sparql.datatype.CoreseXMLLiteral;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;

public class XPathFun {
	
	private static Logger logger = LoggerFactory.getLogger(XPathFun.class);
	static final String RDFNS   =  "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	NSManager nsm;
	NamespaceContext context;
	XPathFactory factory;
	XPath xpath;
	XPathExpression xexp;
	VariableResolver resolver;
	
	boolean isXPathConstant = true;
	
	static int count = 0;

	
	public void init(NSManager nsm, VariableResolver res, boolean constant){
		init(nsm, constant);
		set(res);
	}
	
	public void init(NSManager nsm, boolean constant){
		isXPathConstant = constant;
		this.nsm = nsm;
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		setNSM(nsm);
	}
	
	public void set(VariableResolver res){
		resolver = res;
		xpath.setXPathVariableResolver(res);
	}
	
	public VariableResolver getResolver(){
		return resolver;
	}
	
	
	void setNSM(final NSManager nsm){
		if (xpath != null){
			context = new NamespaceContext(){
				NSManager nm = nsm;
				
				public String getNamespaceURI(String prefix) {
					return nm.getNamespace(prefix);
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
			xpath.setNamespaceContext(context);
		}
	}
	
	public IDatatype xpath(IDatatype idoc, IDatatype iexp){
		return xpath(idoc, iexp, CoreseInteger.ZERO);
	}

	public IDatatype xpath(IDatatype idoc, IDatatype iexp,  IDatatype n){
		return (IDatatype) xpath(idoc, iexp, n, true);
	}
	
	/**
	 * Return IDatatype or NodeList
	 */
	public Object xpath(IDatatype idoc, IDatatype iexp,  IDatatype n, boolean asIDatatype){
	
		String name = idoc.getLabel(); 
		String exp = iexp.getLabel();
		final Node doc;
		//logger.debug(name + " " + nsm.isValid(name));
		if (! nsm.isValid(name)) return null;
		try {
			// have we already parsed this URI into a DOM:
			Object obj = nsm.get(name);
			if (obj != null){
				// already parsed this document
				doc = (Node) obj;
				if (isXPathConstant && n.getIntegerValue()  == 0){
					// already computed same path on dame doc:
					Object dt = nsm.pop(name, exp);
					if (dt != null){
						// only if there is no variable in the exp
						// because bindings may have changed !
						return dt;
					}
				}
			}
			else if (getNode(idoc) != null) {
				// Contains a DOM (e.g. from xslt)
				doc = getNode(idoc);
				
			}
			else {
				//logger.debug("** XP parse: "  + name + " " + exp);
				DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
				fac.setNamespaceAware(true); 
				DocumentBuilder builder = fac.newDocumentBuilder();
				if (idoc.isXMLLiteral()){
					// doc is given as XML markup
					//InputStream in = new StringBufferInputStream(name);
					InputSource sin = new InputSource(new StringReader(name));
					doc = builder.parse(sin);
				}
				else 
				{
					doc = builder.parse(name);
				}
				// save this DOM in the environment (available for all other xpath() fun)
				nsm.set(name, doc);
			}
			
			// Now process(doc, exp)
			
			if (resolver!=null){
				resolver.start(doc);
			}

			Object result;
			if (isXPathConstant){
				if (xexp == null){
					xexp = xpath.compile(exp);
				}
				try {
					result = xexp.evaluate(doc, XPathConstants.NODESET );
				}
				catch (XPathExpressionException e) {
					// lets try as String
					result = xexp.evaluate(doc);
				}
			}
			else {
				result = xpath.evaluate(exp, doc, XPathConstants.NODESET);
			}
			
			if (asIDatatype){
				IDatatype adt;
				if (result instanceof NodeList){
					adt = dom2dt((NodeList)result);
				}
				else if (result instanceof String){
					adt = DatatypeMap.newInstance((String)result);
				}
				else {
					return null;
				}

				nsm.put(name, exp, adt);

				return adt;
			}
			else {
				nsm.put(name, exp, result);
				return result;
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			//logger.error("Parsing1: " + name + " " + e.getMessage());
			nsm.set(name, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//logger.error("Parsing2: " +name + " " + e.getMessage());
			nsm.set(name, null);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			if (e.getCause() != null)
				logger.error(e.getCause().getMessage() + " " + exp );
			else logger.error(e.getMessage() + " " + exp );

			//e.printStackTrace();
		}
		return null;
	}
	
	
	IDatatype dom2dt(NodeList nodes){
		Node node;
		IDatatype dt;
		Vector<IDatatype> vec = new Vector<IDatatype>();
		for (int i = 0; i < nodes.getLength(); i++) {
			dt = null;
			node = nodes.item(i);
			//node.DOCUMENT_NODE;
			dt = dom2dt(node);
			if (dt != null) vec.add(dt);		
		}
		IDatatype adt =  DatatypeMap.createList(vec);
		return adt;
	}
	
	/**
	 * Generate a unique name for kgram that generates a Node 
	 * according to the name label 
	 * @param node
	 * @return
	 */
	String getName(Node node){
		return node.getNodeName() + "_" + count++ ;
	}
	
	IDatatype dom2dt(Node node){
		IDatatype dt = null;
		switch(node.getNodeType()){
		case Node.ELEMENT_NODE:
		case Node.DOCUMENT_NODE: 
			dt = dom2dt(node, getName(node)); 
			break;
			
		case Node.PROCESSING_INSTRUCTION_NODE:
			dt = dom2dt(node, getName(node) + " " + 
					((ProcessingInstruction)node).getData()); 
			break;
					
		case Node.ATTRIBUTE_NODE:
			dt = dom2dt(node, node.getNodeValue()); 
			break;
			
		default:
			// text(), etc
			String value = node.getNodeValue(); 
			NamedNodeMap map = node.getParentNode().getAttributes();
			Node att = null;
			if (map != null){
				att = map.getNamedItemNS(RDFNS, "datatype");
			}
			if (value == null){
				value = getName(node);
			}
			if (value != null){
				if (att != null){
					dt = DatatypeMap.createLiteral(value, att.getTextContent(), null);
				}
				else {
					dt = new CoreseString(value);								
				}
			}
		}
		return dt;
	}
	
	
	IDatatype dom2dt(Node node, String str){
		IDatatype dt = new CoreseXMLLiteral(str);
		dt.setObject(node);
		return dt;
	}
	
	
	Node getNode(IDatatype dt){
		return (Node) dt.getObject();
	}
	
	
	
	
	
	
	
	
	
	
	

	}
	
	
	
	

