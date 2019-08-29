package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.CoreseDatatype.getGenericDatatype;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * Datatype to manage XML objects such as Node, document, etc. Implements (part
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
        nodeType.put(Node.DOCUMENT_NODE, DatatypeMap.newInstance("DOCUMENT"));
        nodeType.put(Node.ELEMENT_NODE, DatatypeMap.newInstance("ELEMENT"));
        nodeType.put(Node.ATTRIBUTE_NODE, DatatypeMap.newInstance("ATTRIBUTE"));
        nodeType.put(Node.TEXT_NODE, DatatypeMap.newInstance("TEXT"));
        nodeType.put(Node.PROCESSING_INSTRUCTION_NODE, DatatypeMap.newInstance("INSTRUCTION"));
    }

    @Override
    public IDatatype getDatatype() {
        return dt;
    }

    @Override
    public Node getObject() {
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
        return node.toString();
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

    public IDatatype getTextContent() {
        return getTextContent(node);
    }

    public IDatatype getNodeName() {
        return getNodeName(node);
    }
    
    // generic function name for testing API
    public IDatatype getNodeProperty() {
        return getNodeProperty(node);
    }

    /**
     * ********************************************************************
     *
     * DOM Implementation
     *
     ********************************************************************
     */
    
    IDatatype getNodeProperty(Node node) {
        return DatatypeMap.newInstance(node.getNodeValue());
    }
    
    IDatatype getNodeName(Node node) {
        return DatatypeMap.newInstance(node.getNodeName());
    }

    IDatatype getTextContent(Node node) {
        return DatatypeMap.newInstance(node.getTextContent());
    }

    IDatatype getElementsByTagName(Node node, IDatatype dt) {
        Document doc = node.getOwnerDocument();
        NodeList list = doc.getElementsByTagName(dt.getLabel());
        return cast(list);
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

    List<IDatatype> valueList() {
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) {
            return new ArrayList<>(0);
        }
        IDatatype list = cast(nodeList);
        if (list != null && list.isList()) {
            return list.getValueList();
        }
        return new ArrayList<>(0);
    }

    public IDatatype cast(NodeList nodes) {
        Node node;
        IDatatype dt;
        ArrayList<IDatatype> vec = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            //node.DOCUMENT_NODE;
            dt = cast(node);
            if (dt != null) {
                vec.add(dt);
            }
        }
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

            default:
                // text(), etc
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
        }
        return dt;
    }

    IDatatype dom2dt(Node node, String str) {
        return DatatypeMap.newXMLObject(str, node);
    }

}
