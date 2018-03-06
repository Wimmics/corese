package fr.inria.corese.kgtool.load;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.corese.kgraph.rule.RuleEngine;

/**
 * Rule Loader as construct-where SPARQL Queries Can also load Corese rule
 * format
 *
 * Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class RuleLoad {

    private static Logger logger = LogManager.getLogger(Load.class);
    public static final String NS = "http://ns.inria.fr/edelweiss/2011/rule#";
    static final String STL = NSManager.STL;
    static final String BRUL = "http://ns.inria.fr/corese/2008/rule#";
    static final String COS = "http://www.inria.fr/acacia/corese#";
    static final String BODY = "body";
    static final String RULE = "rule";
    static final String VALUE = "value";
    static final String PREFIX1 = "prefix";
    static final String PREFIX2 = "PREFIX";
    static final String IF = "if";
    static final String THEN = "then";
    static final String CONST = "construct";
    static final String WHERE = "where";
    RuleEngine engine;
    private String base;

    RuleLoad(RuleEngine e) {
        engine = e;
    }

    public static RuleLoad create(RuleEngine e) {
        return new RuleLoad(e);
    }

    public void parse(String file) throws LoadException {
        Document doc = parsing(file);
        load(doc);
    }

    public void parse(InputStream stream) throws LoadException {
        Document doc = parsing(stream);
        load(doc);
    }

    public void parse(Reader stream) throws LoadException {
        Document doc = parsing(stream);
        load(doc);
    }
    
        

    @Deprecated
    public void load(String file) {
        try {
            loadWE(file);
        } catch (LoadException e) {
            logger.error(e);
        }
    }

    @Deprecated
    public void loadWE(String file) throws LoadException {
        Document doc = parsing(file);
        load(doc);
    }

    @Deprecated
    public void loadWE(InputStream stream) throws LoadException {
        Document doc = parsing(stream);
        load(doc);
    }

    @Deprecated
    public void load(InputStream stream) {
        try {
            loadWE(stream);
        } catch (LoadException e) {
            logger.error(e);
        }
    }

    @Deprecated
    public void loadWE(Reader stream) throws LoadException {
        Document doc = parsing(stream);
        load(doc);
    }

    @Deprecated
    public void load(Reader stream) {
        try {
            Document doc = parsing(stream);
            load(doc);
        } catch (LoadException e) {
            logger.error(e);
        }
    }

    void load(Document doc) {

        NodeList list = doc.getElementsByTagNameNS(NS, BODY);
        
        if (list.getLength() == 0) {
            list = doc.getElementsByTagNameNS(STL, BODY);
        }

        if (list.getLength() == 0) {
            list = doc.getElementsByTagNameNS(BRUL, VALUE);
        }

        if (list.getLength() == 0) {
            list = doc.getElementsByTagNameNS(COS, RULE);
            if (list.getLength() == 0) {
                error();
                return;
            }
            loadCorese(list);
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

    /**
     * Corese format
     */
    public void loadCorese(String file) {
        Document doc;
        try {
            doc = parsing(file);
            NodeList list = doc.getElementsByTagNameNS(COS, RULE);
            loadCorese(list);
        } catch (LoadException e) {
            e.printStackTrace();
        }

    }

    void loadCorese(NodeList list) {
        if (getBase() != null){
            engine.setBase(getBase());
            if (engine.getQueryEngine() != null){
                engine.getQueryEngine().setBase(getBase());
            }
        }
        for (int i = 0; i < list.getLength(); i++) {
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

    void error() {
        logger.error("Rule Namespace should be one of:");
        logger.error(NS);
        logger.error(BRUL);
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
}
