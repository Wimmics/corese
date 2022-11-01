package fr.inria.corese.core.load;

import fr.inria.corese.core.rule.Rule;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import org.w3c.dom.Attr;

/**
 * Rule Loader as construct-where SPARQL Queries Can also load Corese rule
 * format
 *
 * Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class RuleLoad {

    private static Logger logger = LoggerFactory.getLogger(Load.class);
    static final String NS  = NSManager.RULE;
    static final String NS2 = "http://ns.inria.fr/edelweiss/2011/rule#";
    static final String NS1 = "http://ns.inria.fr/corese/2008/rule#";
    static final String STL = NSManager.STL;
    static final String RDF = NSManager.RDF;
    static final String[] NAMESPACE = {NS, STL, NS2, NS1};
    static final String COS = NSManager.COS;
    static final String TYPE = "type";
    static final String MODE = "mode";
    static final String BASIC = "basic";
    static final String DEBUG = "debug";
    static final String BODY = "body";
    static final String RULE = "rule";
    static final String VALUE = "value";
    static final String PREFIX1 = "prefix";
    static final String PREFIX2 = "PREFIX";
    static final String IF = "if";
    static final String THEN = "then";
    static final String CONST = "construct";
    static final String WHERE = "where";
    static final String RESOURCE = "resource";
    static final String ABOUT = "about";
    static final String ID = "ID";
    private Level level = Level.USER_DEFAULT;

    RuleEngine engine;
    private String base;

    RuleLoad(RuleEngine e) {
        engine = e;
    }

    public static RuleLoad create(RuleEngine e) {
        return new RuleLoad(e);
    }

    public void parse(String file) throws LoadException, EngineException {
        Document doc = parsing(file);
        load(doc);
    }

    public void parse(InputStream stream) throws LoadException, EngineException {
        Document doc = parsing(stream);
        load(doc);
    }

    public void parse(Reader stream) throws LoadException, EngineException {
        Document doc = parsing(stream);
        load(doc);
    }
    
        

    @Deprecated
    public void load(String file) {
        try {
            loadWE(file);
        } catch (LoadException e) {
            logger.error(e.getMessage());
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
    }

    @Deprecated
    public void loadWE(String file) throws LoadException, EngineException {
        Document doc = parsing(file);
        load(doc);
    }

    @Deprecated
    public void loadWE(InputStream stream) throws LoadException, EngineException {
        Document doc = parsing(stream);
        load(doc);
    }

    @Deprecated
    public void load(InputStream stream) {
        try {
            loadWE(stream);
        } catch (LoadException e) {
            logger.error(e.getMessage());
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
    }

    @Deprecated
    public void loadWE(Reader stream) throws LoadException, EngineException {
        Document doc = parsing(stream);
        load(doc);
    }

    @Deprecated
    public void load(Reader stream) {
        try {
            Document doc = parsing(stream);
            load(doc);
        } catch (LoadException e) {
            logger.error(e.getMessage());
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    

    void load(Document doc) throws EngineException {
        engine.setLevel(getLevel());
        NodeList list = null;
        
        for (String ns : NAMESPACE) {
            list = doc.getElementsByTagNameNS(ns, RULE);           
            if (list.getLength() != 0) {
                break;
            }
        }

        if (list == null || list.getLength() == 0) {
            error();
            return;
        }

        for (int i = 0; i < list.getLength(); i++) {
            Element rule = (Element) list.item(i); 
            Attr att = rule.getAttributeNodeNS(RDF, ABOUT);
            if (att == null) {
                att = rule.getAttributeNodeNS(RDF, ID);
            }
            String uri = null;
            if (att != null) {
                uri = att.getValue();
            }
            Element body = getElement(rule, BODY);
            if (body == null) {
                body = getElement(rule, VALUE);
            }
            String text = body.getTextContent();
            String type = getType(rule);             
            engine.defRule(uri, text, type);
            if (engine.getRule() != null) {
                tune(engine.getRule(), rule);
            }
        }
    }
    
    void tune(Rule rule, Element xml) {
        NodeList list = xml.getElementsByTagNameNS(NS, MODE);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            String text = node.getTextContent();
            if (text!=null) {
                tune(rule, MODE, text);
            }
        }
    }
    
    void tune(Rule rule, String name, String value) {
        if (value.equals(BASIC)) {
            rule.setOptimize(false);
        }
    }
    
    String getType(Element rule) {
        String type = null;
        Element typeElem = getElement(rule, TYPE);
        if (typeElem != null) {
            Attr typeAttr = typeElem.getAttributeNodeNS(RDF, RESOURCE);
            if (typeAttr != null) {
                type = typeAttr.getValue();
            }
        }
        return type;
    }
    
    Element getElement(Element elem, String name) {
        for (String ns : NAMESPACE) {
           Element e = getElement(elem, ns, name);
           if (e != null) {
               return e;
           }
        }
        return null;
    }
    
    Element getElement(Element elem, String ns, String name) {
        NodeList list = elem.getElementsByTagNameNS(ns, name);
        if (list.getLength() == 0) {
            return null;
        }
        return (Element) list.item(0);
    }
    
    void load1(Document doc) {

        NodeList list = null;
        
        for (String ns : NAMESPACE) {
            list = doc.getElementsByTagNameNS(ns, BODY);           
            if (list.getLength() != 0) {
                break;
            }
        }

        if (list == null || list.getLength() == 0) {
            list = doc.getElementsByTagNameNS(NS1, VALUE);
        }
        

        if (list.getLength() == 0) {
            error();
            return;
        }

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            
            String rule = node.getTextContent();
            try {
                engine.defRule(rule);
            } catch (EngineException e) {
                e.printStackTrace();
            }
        }
    }
  
    void error() {
        logger.error("Rule Namespace should be one of:");
        logger.error(NS);
        logger.error(STL);
    }

    String getRule(Element econst, Element ewhere) {
        String sconst = econst.getTextContent().trim();
        String swhere = ewhere.getTextContent().trim();
        String pref = "";

        if (swhere.startsWith(PREFIX1) || swhere.startsWith(PREFIX2)) {
            int ind = swhere.indexOf("{");
            pref = swhere.substring(0, ind);
            swhere = swhere.substring(ind);
        }

        String rule = pref + CONST + sconst + "\n" + WHERE + swhere;

        return rule;

    }

    private Document parsing(InputStream stream) throws LoadException {
        return parsing(new InputSource(stream));
    }

    private Document parsing(Reader stream) throws LoadException {
        return parsing(new InputSource(stream));
    }

    private Document parsing(InputSource stream) throws LoadException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = fac.newDocumentBuilder();
            Document doc = builder.parse(stream);
            return doc;
        } catch (ParserConfigurationException e) {
            throw LoadException.create(e);
        } catch (SAXException e) {
            throw LoadException.create(e);
        } catch (IOException e) {
            throw LoadException.create(e);
        }
    }

    private Document parsing(String xmlFileName) throws LoadException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = fac.newDocumentBuilder();
            Document doc = builder.parse(xmlFileName);
            return doc;
        } catch (ParserConfigurationException e) {
            throw LoadException.create(e);
        } catch (SAXException e) {
            throw LoadException.create(e);
        } catch (IOException e) {
            throw LoadException.create(e);
        }
    }

    /**
     * @return the base
     */
    public String getBase() {
        return base;
    }

    /**
     * @param base the base to set
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * @return the level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Level level) {
        this.level = level;
    }
}
