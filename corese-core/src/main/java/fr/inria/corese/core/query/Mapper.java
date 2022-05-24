package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.compiler.eval.SQLResult;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.result.SPARQLResultParser;
import fr.inria.corese.core.producer.DataProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.IDatatypeList;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Implements unnest() statement values ?var { unnest(exp) } bind (unnest(?exp)
 * as ?var) bind (unnest(?exp) as (?x, ?y))
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class Mapper {
    static public Logger logger = LoggerFactory.getLogger(Mapper.class);

    Producer p;
    MapperSQL mapper;

    Mapper(Producer p) {
        this.p = p;
        mapper = new MapperSQL(p);

    }
    
    public Mappings map(List<Node> nodes, Object object, int n) {
        if (object instanceof IDatatype) {
            return map(nodes, (IDatatype)object, n);
        }
        else {
            return map(nodes, object);
        }
    }
    
    public Mappings map(List<Node> nodes, IDatatype list, int n) {
        if (n <= 1) {
            return map(nodes, list);
        }
        else {
            Mappings map = new Mappings();
            for (IDatatype dt : list) {
                Mappings res = map(nodes, dt, n-1);
                map.add(res);
            }
            return map;
        }
    }



    /**
     * values (nodes) { unnest(dt) }
     */
    Mappings map(List<Node> nodes, IDatatype dt) {
        if (dt.isList()) {
            return map(nodes, dt.getValues());
        } else if (dt.isMap() || dt.isJSON() || dt.isXML()) {
            return map(nodes, dt.getValueList());
        } 
        else if (dt.isTripleWithEdge()) {
            return mapEdge(nodes, dt.getEdge());
        }    
        else if (dt.isPointer()) {
            return map(nodes, dt.getPointerObject());
        } else if (dt.getNodeObject() != null) {
            return map(nodes, dt.getNodeObject());
        } else if (dt.isURI()) {
            return mapURI(nodes, dt);
        } 
        
        else {
            return mapDT(nodes, dt);
        }
    }
    
    Mappings mapEdge(List<Node> nodes, Edge edge) {
        ArrayList<IDatatype> list = new ArrayList<>();
        IDatatypeList dt = DatatypeMap.newList();
        dt.add(edge.getSubjectValue());
        dt.add(edge.getPredicateValue());
        dt.add(edge.getObjectValue());
        if (edge.getGraphValue()!=null) {
            dt.add(edge.getGraphValue());
        }
        list.add(dt);
        return map(nodes, list);
    }
    
    public Mappings map(List<Node> nodes, Object object) {
        if (object instanceof IDatatype) {
            return map(nodes, (IDatatype) object);
        } else if (object instanceof Mappings) {
            return map(nodes, (Mappings) object);
        } else if (object instanceof Collection) {
            return map(nodes, (Collection<IDatatype>) object);
        } else if (object instanceof SQLResult) {
            return mapper.sql(nodes, (SQLResult) object);
        }
        return new Mappings();
    }

    Mappings map(List<Node> nodes, Pointerable obj) {
        switch (obj.pointerType()) {
            case GRAPH:
                return map(nodes, (Graph) obj.getTripleStore());

            case MAPPINGS:
                return map(nodes, obj.getMappings());

            case TRIPLE:
                return map(nodes, obj.getEdge());

            case NSMANAGER:
                return map(nodes, (NSManager) obj);

            case QUERY:
                return map(nodes, obj.getQuery());

            case CONTEXT:
                return map(nodes, (Context) obj);

            case METADATA:
                return map(nodes, (Metadata) obj);

            case PRODUCER:
                return map(nodes, (DataProducer) obj);
        }

        return map(nodes, (Object) obj);
    }

    Mappings map(List<Node> varList, Graph g) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Node[] nodes;
        Mappings map = new Mappings();
        int size = varList.size();       
        for (Edge ent : g.getEdges()) {
            nodes = new Node[size];
            if (size == 1) {
                nodes[0] = DatatypeMap.createObject(g.getEdgeFactory().copy(ent));
            } else {
                nodeArray(ent, nodes);
            }
            map.add(Mapping.create(qNodes, nodes));
        }

        return map;
    }

    Mapping getMapping(Edge edge, Node[] qNodes) {
        Node[] nodes = new Node[qNodes.length];
        nodeArray(edge, nodes);
        return Mapping.create(qNodes, nodes);
    }

    void nodeArray(Edge edge, Node[] nodes) {
        switch (nodes.length) {
            case 4:
                nodes[3] = edge.getGraph();
            case 3:
                nodes[2] = edge.getNode(1);
            case 2:
                nodes[1] = edge.getEdgeNode();
            case 1:
                nodes[0] = edge.getNode(0);
        }
    }

    Mappings map(List<Node> varList, Edge e) {
        Mappings map = new Mappings();
        int size = varList.size();
        if (size != 1) {
            return map;
        }
        Node[] qNodes = new Node[1];
        varList.toArray(qNodes);

        for (Object obj : e.getLoop()) {
            Node[] nodes = new Node[1];
            nodes[0] = (Node) obj;
            map.add(Mapping.create(qNodes, nodes));
        }

        return map;
    }

    Mappings map(List<Node> varList, NSManager nsm) {
        return mapListOfList(varList, nsm.getList().getValues());
    }

    Mappings map(List<Node> varList, Context c) {
        return mapListOfList(varList, c.getList().getValues());
    }

    Mappings map(List<Node> varList, Metadata m) {
        return map(varList, m.getList().getValues());
    }

    Mappings map(List<Node> varList, DataProducer d) {
        return map(varList, d.getList().getValues());
    }

    Mappings mapListOfList(List<Node> varList, List<IDatatype> listOfList) {
        Mappings map = new Mappings();
        int size = varList.size();
        if (size > 2) {
            return map;
        }
        Node[] qNodes = new Node[size];
        varList.toArray(qNodes);

        for (IDatatype def : listOfList) {
            Node[] tn = new Node[size];
            for (int i = 0; i < size; i++) {
                tn[i] = def.get(i);
            }
            map.add(Mapping.create(qNodes, tn));
        }
        return map;
    }

    Mappings map(List<Node> varList, Query q) {
        if (varList.size() != 1) {
            return new Mappings();
        }
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge e : q.getEdges()) {
            list.add(DatatypeMap.createObject(e));
        }
        return map(varList, list);
    }

    Mappings map(List<Node> lNodes, Collection<IDatatype> list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list) {
            Mapping m = Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }

    /**
     * bind (unnest(exp) as ?x) bind (unnest(exp) as (?x, ?y)) eval(exp) = list
     *
     *
     */
    Mappings map(List<Node> varList, List<IDatatype> valueList) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Mappings map = new Mappings();
        Mapping m;
        for (IDatatype dt : valueList) {
            if (varList.size() > 1 && dt.isList() ) {
                // bind (((1 2)(3 4)) as (?x, ?y))
                // ?x = 1 ; ?y = 2
                Node[] val = new Node[dt.size()];
                m = Mapping.create(qNodes, dt.getValues().toArray(val));
            } 
            else if (varList.size() > 1 && dt.isPointer() && dt.pointerType() == PointerType.TRIPLE) {
                m = getMapping(dt.getPointerObject().getEdge(), qNodes);
            }
            else {
                // bind (((1 2)(3 4)) as ?x)
                // HINT: bind (((1 2)(3 4)) as (?x))
                // ?x = (1 2)
                m = Mapping.create(varList.get(0), dt);
            }

            map.add(m);
        }
        return map;
    }

    Mappings map(List<Node> lNodes, IDatatype[] list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list) {
            Mapping m = Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }

    /**
     * Binding by name Eval extBind() bind list of Node on this Mappings by name
     */
    Mappings map(List<Node> list, Mappings map) {
        return map;
    }
    
    /**
     * Try dt = URL of SPARQL Query Results XML Format
     */
    Mappings mapURI(List<Node> varList, IDatatype dt) {
        SPARQLResultParser parser = new SPARQLResultParser();
        try {
            return parser.parse(dt.getLabel());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.info("Parse error SPARQL Query Results: " + dt.getLabel());
            return mapDT(varList, dt);
        }
    }

    Mappings mapDT(List<Node> varList, IDatatype dt) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Mappings lMap = new Mappings();
        List<Node> lNode = toNodeList(dt);
        for (Node node : lNode) {
            Node[] tNodes = new Node[1];
            tNodes[0] = node;
            Mapping map = Mapping.create(qNodes, tNodes);
            lMap.add(map);
        }
        return lMap;
    }

    public List<Node> toNodeList(Object obj) {
        return toNodeList((IDatatype) obj);
    }
    public List<Node> toNodeList(IDatatype dt) {
        List<Node> list = new ArrayList<>();
        if (dt.isList()) {
            for (IDatatype dd : dt.getValues()) {
                if (dd.isXMLLiteral() && dd.getLabel().startsWith("http://")) {
                    // try an URI
                    dd = DatatypeMap.newResource(dd.getLabel());

                }
                list.add(p.getNode(dd));
            }
        } else {
            list.add(p.getNode(dt));
        }
        return list;
    }

}
