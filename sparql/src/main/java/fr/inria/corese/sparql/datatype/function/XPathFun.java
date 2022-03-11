package fr.inria.corese.sparql.datatype.function;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

public class XPathFun {

    private static Logger logger = LoggerFactory.getLogger(XPathFun.class);

    NSManager nsm;
    NamespaceContext context;
    XPathFactory factory;
    XPath xpath;
    XPathExpression xexp;
    VariableResolver resolver;
    boolean bindex = !true;

    boolean isXPathConstant = true;

    static int count = 0;
    
    public XPathFun() {
        
    }
    
    public XPathFun(boolean bindex) {
        setNameIndex(bindex);
    }
    
    // bindex = true :  label of Node is tag name + index
    // bindex = false : label of Node is tag name only    
    public void setNameIndex(boolean b) {
        bindex = b;
    }

    public void init(NSManager nsm, VariableResolver res, boolean constant) {
        init(nsm, constant);
        set(res);
    }

    public void init(NSManager nsm, boolean constant) {
        isXPathConstant = constant;
        this.nsm = nsm;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        setNSM(nsm);
    }

    public void set(VariableResolver res) {
        resolver = res;
        xpath.setXPathVariableResolver(res);
    }

    public VariableResolver getResolver() {
        return resolver;
    }

    void setNSM(final NSManager nsm) {
        if (xpath != null) {
            context = new NamespaceContext() {
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

    public Document parse(IDatatype idoc) throws ParserConfigurationException, SAXException, IOException {
        String name = idoc.getLabel();
        Document doc;
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder builder = fac.newDocumentBuilder();
        switch (idoc.getCode()) {
            case IDatatype.XMLLITERAL:
            case IDatatype.LITERAL:
            case IDatatype.STRING:
                // doc is XML markup String
                InputSource sin = new InputSource(new StringReader(name));
                doc = builder.parse(sin);
                break;

            default:
                doc = builder.parse(name);
        }
        return doc;
    }
    
    public String print(Document doc) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter str = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(str));
        return str.toString();
    }
    
    public String xslt(Document doc, String xsl) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        StreamSource stylesource = new StreamSource(xsl); 
        Transformer transformer = tf.newTransformer(stylesource);
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter str = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(str));
        return str.toString();
    }
  
    /**
     * idoc is URI or rdf:XMLLiteral
     */
    public IDatatype xpath(IDatatype idoc, IDatatype iexp) {
        String exp  = iexp.getLabel();
        try {
            Node doc = getNode(idoc);
            if (doc == null) {
                doc = parse(idoc);
            } 

            // Now process(doc, exp)
            if (resolver != null) {
                resolver.start(doc);
            }

            Object result;
            
            if (isXPathConstant) {
                if (xexp == null) {
                    xexp = xpath.compile(exp);
                }
                try {
                    result = xexp.evaluate(doc, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    // lets try as String
                    result = xexp.evaluate(doc);
                }
            } else {
                result = xpath.evaluate(exp, doc, XPathConstants.NODESET);
            }

           return cast(result);

        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage());
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
            if (e.getCause() != null) {
                logger.error(e.getCause().getMessage() + " " + exp);
            } else {
                logger.error(e.getMessage() + " " + exp);
            }
        }
        return null;
    }
    
    IDatatype cast(Object result) {
        IDatatype adt = null;
        if (result instanceof NodeList) {
            adt = cast((NodeList) result);
        } else if (result instanceof String) {
            adt = DatatypeMap.newInstance((String) result);
        }
        return adt;
    }
    
    IDatatype cast (NodeList list) {
        return DatatypeMap.cast(list);
    }

    Node getNode(IDatatype dt) {
        return (Node) dt.getNodeObject();
    }
    
}
