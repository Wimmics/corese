package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.function.XPathFun;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.json.JSONML;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * Datatype to manage XML objects as DOM Node, Document, etc. Implements (part
 * of) DOM CoreseXML is used by xpath() and by xt:xml() parser. CoreseXMLLiteral
 * remains the std datatype for rdf:XMLLiteral.
 *
 * @author Olivier Corby, Inria Wimmics 2019
 */
public class CoreseXML extends CoreseExtension {

    static final String RDFNS = NSManager.RDF;
    static final String XSINS = NSManager.XSI;
    static final String XMLNS = NSManager.XML;
    private static final IDatatype dt = getGenericDatatype(IDatatype.XML_DATATYPE);
    private static int count = 0;
    private static final String SEED = "_xml_";
    public static IDatatype DEFAULT = DatatypeMap.newInstance("DEFAULT");
    public static IDatatype TEXT = DatatypeMap.newInstance("TEXT");

    public static final CoreseXML singleton = new CoreseXML();
    private static final HashMap<Short, IDatatype> nodeType = new HashMap<>();

    static {
        deftype();
    }

    Node node;

    CoreseXML() {
        super(SEED + count++);
    }

    public CoreseXML(Node node) {
        super(SEED + count++);
        setObject(node);
    }

    public CoreseXML(String str, Node node) {
        super(str);
        setObject(node);
    }

    private static void deftype() {
        deftype(Node.DOCUMENT_NODE, "DOCUMENT");
        deftype(Node.DOCUMENT_TYPE_NODE, "DOCTYPE");
        deftype(Node.ELEMENT_NODE, "ELEMENT");
        deftype(Node.ATTRIBUTE_NODE, "ATTRIBUTE");
        deftype(Node.TEXT_NODE, "TEXT");
        deftype(Node.PROCESSING_INSTRUCTION_NODE, "INSTRUCTION");
        deftype(Node.CDATA_SECTION_NODE, "CDATA");
        deftype(Node.COMMENT_NODE, "COMMENT");
    }

    static void deftype(short type, String name) {
        nodeType.put(type, DatatypeMap.newInstance(name));
    }

    @Override
    public IDatatype getDatatype() {
        return dt;
    }

    @Override
    public Node getNodeObject() {
        return node;
    }

    public void setObject(Node n) {
        node = n;
    }

    @Override
    public void setObject(Object obj) {
        if (obj instanceof Node) {
            setObject((Node) obj);
        }
    }

    @Override
    public String getContent() {
        try {
            switch (node.getNodeType()) {
                case Node.DOCUMENT_NODE:
                    return new XPathFun().print(getDocument(node));
            }
            return node.toString();
        } catch (IOException | TransformerException ex) {
            return node.toString();
        }
    }
    
    @Override
    public IDatatype json() {
        JSONObject obj = JSONML.toJSONObject(getContent());
        return DatatypeMap.json(obj);
    }
   

    @Override
    public Iterator<IDatatype> iterator() {
        return valueList().iterator();
    }

    @Override
    public List<IDatatype> getValueList() {
        return valueList();
    }

    @Override
    public boolean isLoop() {
        return true;
    }
    
    @Override
    public boolean isXML() {
        return true;
    }

    @Override
    public Iterable<IDatatype> getLoop() {
        return valueList();
    }

    /**
     * DOM API
     */
    public IDatatype getAttributes() {
        return getAttributes(node);
    }

    public IDatatype getNodeType() {
        return getNodeType(node);
    }

    public IDatatype getElementsByTagName(IDatatype dt) {
        return getElementsByTagName(node, dt);
    }

    public IDatatype getElementsByTagNameNS(IDatatype ns, IDatatype dt) {
        return getElementsByTagNameNS(node, ns, dt);
    }

    public IDatatype getElementById(IDatatype dt) {
        return getElementById(node, dt);
    }

    public IDatatype hasAttribute(IDatatype dt) {
        return hasAttribute(node, dt);
    }

    public IDatatype hasAttributeNS(IDatatype ns, IDatatype dt) {
        return hasAttributeNS(node, ns, dt);
    }

    public IDatatype getAttribute(IDatatype dt) {
        return getAttribute(node, dt);
    }

    public IDatatype getAttributeNS(IDatatype ns, IDatatype dt) {
        return getAttributeNS(node, ns, dt);
    }

    public IDatatype getTextContent() {
        return getTextContent(node);
    }

    public IDatatype getNodeValue() {
        return getNodeValue(node);
    }

    public IDatatype getNodeName() {
        return getNodeName(node);
    }

    public IDatatype getLocalName() {
        return getLocalName(node);
    }

    // generic function name for testing API
    public IDatatype getNodeProperty() {
        return getBaseURI(node);
    }

    Element getElement(Node node) {
        return (Element) node;
    }
    
    Attr getAttribute(Node node) {
        return (Attr) node;
    }
    
    Document getDocument(Node node) {
        return (Document) node;
    }

    public IDatatype getFirstChild() {
        return getFirstChild(node);
    }

    public IDatatype getChildNodes() {
        return DatatypeMap.newList(childNodes());
    }

    public IDatatype getChildElements() {
        return DatatypeMap.newList(childElements());
    }

    public IDatatype getOwnerDocument() {
        return getOwnerDocument(node);
    }

    public IDatatype getParentNode() {
        return getParentNode(node);
    }

    public IDatatype getNamespaceURI() {
        return getNamespaceURI(node);
    }

    public IDatatype getBaseURI() {
        return getBaseURI(node);
    }
    
    public IDatatype xslt(IDatatype dt) {
        try {
            String str = new XPathFun().xslt(getDocument(node), dt.getLabel());
            return DatatypeMap.newInstance(str);
        } catch (IOException | TransformerException ex) {
            CoreseDatatype.logger.error(ex.getMessage());
        }
        return null;
    }

    /**
     * ********************************************************************
     *
     * DOM Implementation
     *
     ********************************************************************
     */
    

    IDatatype getAttribute(Node node, IDatatype dt) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                String val = getElement(node).getAttribute(dt.getLabel());
                if (val == null) {
                    return null;
                }
                return DatatypeMap.newInstance(val);
        }
        return null;
    }

    IDatatype getAttributeNS(Node node, IDatatype ns, IDatatype dt) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                String val = getElement(node).getAttributeNS(ns.getLabel(), dt.getLabel());
                if (val == null) {
                    return null;
                }
                return DatatypeMap.newInstance(val);
        }
        return null;
    }
    
    IDatatype hasAttribute(Node node, IDatatype dt) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return DatatypeMap.newInstance(getElement(node).hasAttribute(dt.getLabel()));
        }
        return DatatypeMap.FALSE;
    }

    IDatatype hasAttributeNS(Node node, IDatatype ns, IDatatype dt) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                boolean b = getElement(node).hasAttributeNS(ns.getLabel(), dt.getLabel());
                return DatatypeMap.newInstance(b);
        }
        return DatatypeMap.FALSE;
    }

    IDatatype getNamespaceURI(Node node) {
        String uri = node.getNamespaceURI();
        if (uri == null) {
            return null;
        }
        return DatatypeMap.newResource(uri);
    }

    IDatatype getBaseURI(Node node) {
        String uri = node.getBaseURI();
        if (uri == null) {
            return null;
        }
        return DatatypeMap.newResource(uri);
    }

    IDatatype getOwnerDocument(Node node) {
        Document doc = node.getOwnerDocument();
        return cast(doc);
    }

    IDatatype getParentNode(Node node) {
        Node n = genericParentNode(node);       
        return cast(n);
    }
    
    Node genericParentNode(Node node) {
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                return getAttribute(node).getOwnerElement();
        }
        return node.getParentNode();
    }

    IDatatype getTagName(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return DatatypeMap.newInstance(getElement(node).getTagName());
        }
        return getNodeName(node);
    }

    IDatatype getNodeName(Node node) {
        return DatatypeMap.newInstance(node.getNodeName());
    }
    
    IDatatype getLocalName(Node node) {
        return DatatypeMap.newInstance(node.getLocalName());
    }

    IDatatype getNodeValue(Node node) {
        String str = node.getNodeValue();
        if (str == null) {
            return null;
        }
        return DatatypeMap.newInstance(node.getNodeValue());
    }

    IDatatype getTextContent(Node node) {
        String str = node.getTextContent();
        if (str == null) {
            return null;
        }
        return DatatypeMap.newInstance(str);
    }
    
    IDatatype getElementsByTagNameNS(Node node, IDatatype ns, IDatatype dt) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return cast(getElement(node).getElementsByTagNameNS(ns.getLabel(), dt.getLabel()));
            case Node.DOCUMENT_NODE:
                return cast(getDocument(node).getElementsByTagNameNS(ns.getLabel(),dt.getLabel()));
        }
        return DatatypeMap.newList();
    }

    IDatatype getElementsByTagName(Node node, IDatatype dt) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return cast(getElement(node).getElementsByTagName(dt.getLabel()));
            case Node.DOCUMENT_NODE:
                return cast(getDocument(node).getElementsByTagName(dt.getLabel()));
        }
        return DatatypeMap.newList();
    }

    IDatatype getElementById(Node node, IDatatype dt) {
        Document doc = genericGetDocument(node);
        if (doc == null) {
            return null;
        }
        return cast(doc.getElementById(dt.getLabel()));
    }
    
    Document genericGetDocument(Node node) {
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                return getDocument(node);
            default:
                return node.getOwnerDocument();
        }
    }

    IDatatype getNodeType(Node node) {
        IDatatype type = nodeType.get(node.getNodeType());
        if (type == null) {
            return DEFAULT;
        }
        return type;
    }

    IDatatype getAttributes(Node node) {
        if (!(node.getNodeType() == Node.ELEMENT_NODE && node.hasAttributes())) {
            return DatatypeMap.map();
        }
        NamedNodeMap map = node.getAttributes();
        CoreseMap dt = new CoreseMap();       
        for (int i = 0; i < map.getLength(); i++) {
            Node att = map.item(i);
            dt.set(DatatypeMap.newInstance(att.getNodeName()), DatatypeMap.newInstance(att.getTextContent()));
        }
        return dt;
    }

    // value for iterators
    List<IDatatype> valueList() {
        return childElements();
    }
    
    IDatatype getFirstChild(Node node) {
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        return cast(nodeList.item(0));
    }

    List<IDatatype> childNodes() {
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return new ArrayList<>(0);
        }
        return asList(nodeList);
    }

    List<IDatatype> childElements() {
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return new ArrayList<>(0);
        }
        return asList(nodeList, true);
    }

    List<IDatatype> asList(NodeList nodes) {
        return asList(nodes, false);
    }

    List<IDatatype> asList(NodeList nodes, boolean elementOnly) {
        Node node;
        IDatatype dt;
        ArrayList<IDatatype> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            if (!elementOnly || node.getNodeType() == Node.ELEMENT_NODE) {
                dt = cast(node);
                if (dt != null) {
                    list.add(dt);
                }
            }
        }
        return list;
    }

    /**
     * NodeList is: 1) child node list 2) result of xpath(exp) cast TEXT Node as
     * IDatatype with datatype and lang if any
     */
    public static IDatatype cast(NodeList nodes) {
        return singleton.castNodeList(nodes);
    }

    IDatatype castNodeList(NodeList nodes) {
        List<IDatatype> vec = asList(nodes);
        IDatatype adt = DatatypeMap.createList(vec);
        return adt;
    }

    /**
     * Generate a unique name for kgram that generates a Node according to the
     * name label
     */
    String getName(Node node) {
        return getNameBasic(node);
    }

    String getNameIndex(Node node) {
        return node.getNodeName() + "_" + count++;
    }

    String getNameBasic(Node node) {
        return node.getNodeName();
    }

    IDatatype cast(Node node) {
        if (node == null) {
            return null;
        }
        IDatatype dt = null;
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
            case Node.DOCUMENT_NODE:
                dt = dom2dt(node, getName(node));
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                dt = dom2dt(node, getName(node) + " "
                        + ((ProcessingInstruction) node).getData());
                break;

            case Node.ATTRIBUTE_NODE:
                dt = dom2dt(node, node.getNodeValue());
                break;

            case Node.TEXT_NODE:
                //dt = text2dt(node);
                dt = dom2dt(node, getName(node));
                break;

            default:
                dt = dom2dt(node, getName(node));

        }
        return dt;
    }

    @Override
    public IDatatype getObjectDatatypeValue() {
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
                return text2dt(node);
            default:
                return this;
        }
    }

    /**
     * text(), etc Return text value as IDatatype and take into account
     * rdf:datatype, xsi:type and xml:lang Hence it may return an integer or a
     * literal with lang tag Use case: xpath(node, exp/text()) iterate child
     * nodes
     *
     */
    IDatatype text2dt(Node node) {
        IDatatype dt = null;
        String value = node.getNodeValue();
        NamedNodeMap map = node.getParentNode().getAttributes();
        Node datatype = null,
                lang = null;

        if (map != null) {
            // rdf:datatype
            datatype = map.getNamedItemNS(RDFNS, "datatype");
            if (datatype == null) {
                // xsi:type
                datatype = map.getNamedItemNS(XSINS, "type");
                if (datatype == null) {
                    // xml:lang
                    lang = map.getNamedItemNS(XMLNS, "lang");
                }
            }
        }
        if (value == null) {
            value = getName(node);
        }
        if (value != null) {
            if (datatype != null) {
                dt = DatatypeMap.createLiteral(value, datatype.getTextContent(), null);
            } else if (lang != null) {
                dt = DatatypeMap.createLiteral(value, null, lang.getTextContent());
            } else {
                dt = DatatypeMap.newInstance(value);
            }
        }
        return dt;
    }

    IDatatype dom2dt(Node node, String str) {
        return DatatypeMap.newXMLObject(str, node);
    }

}
