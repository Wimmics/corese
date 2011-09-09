/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgimport;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
//import fr.inria.acacia.corese.api.IEngine;
//import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeCore;
import fr.inria.edelweiss.kgraph.core.Graph;
import org.apache.log4j.Logger;

/**
 *
 * @author gaignard
 */
public class JenaGraphFactory {

    public static Logger logger = Logger.getLogger(JenaGraphFactory.class);

//  source   = graph.addGraph(uri);
//  subject  = graph.addResource(uri);
//  property = graph.addProperty(uri);
//  object   = graph.addBlank(id);
//  object   = graph.addLiteral(value, datatype, lang);
//  edge     = Edge.create(source, subject, property, object);
//  graph.addEdge(edge);
    /**
     *  Handling of XSDDateType ?? -> transformed as String into KGRAM
     *  See http://openjena.org/javadoc/com/hp/hpl/jena/datatypes/xsd/XSDDatatype.html
     * @param m
     * @param g
     * @return 
     */
    public static void updateGraph(Model m, Graph g) {
        Node source = g.addGraph("g1");
        Node subject = null;
        Node predicate = null;
        Node object = null;

//        logger.setLevel(Level.WARN);

        StmtIterator it = m.listStatements();
        while (it.hasNext()) {
            Statement st = it.next();
            logger.info(st.asTriple().toString());

            //subject
            if (st.getSubject().isAnon()) {
            } else if (st.getSubject().isLiteral()) {
                logger.debug("Subject as literal " + st.getSubject().toString());
            } else if (st.getSubject().isResource()) {
                logger.debug("Subject as resource " + st.getSubject().toString());
                subject = g.addResource(st.getSubject().toString());
            } else if (st.getSubject().isURIResource()) {
                logger.debug("Subject as URI resource " + st.getSubject().getURI());
                subject = g.addResource(st.getSubject().getURI());
            } else {
                logger.warn("Subject " + st.getSubject().toString() + " not recognized by JENA as a blank node, a literal or a resource.");
            }

            //property
            if (st.getPredicate().isProperty()) {
                logger.debug("Property URI " + st.getPredicate().getURI());
                predicate = g.addProperty(st.getPredicate().getURI());
            } else {
                logger.warn("Predicate " + st.getPredicate().toString() + " not recognized as a JENA property.");
            }

            //object 
            if (st.getObject().isAnon()) {
                object = g.addBlank(st.getObject().asNode().getBlankNodeId().toString());
            } else if (st.getObject().isLiteral()) {
                logger.debug("Object as literal " + st.getObject().toString());
                Literal l = st.getObject().asLiteral();
                logger.debug("lexical form " + l.getLexicalForm());
                Object value = null;
                try {
                    value = l.getValue();
                } catch (com.hp.hpl.jena.datatypes.DatatypeFormatException ex) {
                    logger.warn(ex.getMessage());
                    value = l.getLexicalForm();
                }

                if (value instanceof Integer) {
                    object = g.addLiteral(((Integer) value).intValue());
                } else if (value instanceof Long) {
                    object = g.addLiteral(((Long) value).longValue());
                } else if (value instanceof Float) {
                    object = g.addLiteral(((Float) value).floatValue());
                } else if (value instanceof Double) {
                    object = g.addLiteral(((Double) value).doubleValue());
                } else if (value instanceof Boolean) {
                    object = g.addLiteral(((Boolean) value).booleanValue());
                } else if (value instanceof String) {
                    object = g.addLiteral(((String) value));
                } else if (value instanceof XSDDateTime) {
                    logger.warn("Literal value " + value + " : " + value.getClass().getCanonicalName() + " transformed as String into KGRAM");
                    object = g.addLiteral(l.getLexicalForm(), l.getDatatypeURI(), null);
                }else {
                    logger.error("Literal value " + value + " : " + value.getClass().getCanonicalName() + " not handled by KGRAM graphs");
                }

            } else if (st.getObject().isResource()) {
                logger.debug("Object as resource " + st.getObject().toString());
                object = g.addResource(st.getObject().asResource().toString());
            } else if (st.getObject().isURIResource()) {
                logger.debug("Object as resource " + st.getObject().asResource().getURI());
                object = g.addResource(st.getObject().asResource().getURI());
            } else {
                logger.warn("Object " + st.getObject().toString() + " not recognized by JENA as a blank node, a literal or a resource.");
            }

            //edge
//            if (source != null || subject != null || predicate != null || object != null) {
            EdgeCore e = EdgeCore.create(source, subject, predicate, object);
            g.addEdge(e);
//            }
//            System.out.println(e.toString());
//            System.out.println("");
        }
    }

    public static Graph createGraph(Model m) {
        Graph g = Graph.create();
        JenaGraphFactory.updateGraph(m, g);
        return g;
    }

//    public static IEngine createEngine(Model m) {
//        GraphEngine gEngine = GraphEngine.create();
//        JenaGraphFactory.updateGraph(m, gEngine.getGraph());
//        return gEngine;
//    }

    public static Graph createGraph(String sdbUrl, String login, String password, String namedModel) {
        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
        JDBC.loadDriverMySQL();
        SDBConnection conn = new SDBConnection(sdbUrl.toString(), login, password);
        Store store = SDBFactory.connectStore(conn, storeDesc);
        Model m = null;
        if (namedModel != null) {
            m = SDBFactory.connectNamedModel(store, namedModel);
        } else {
            m = SDBFactory.connectDefaultModel(store);
        }

        return JenaGraphFactory.createGraph(m);
    }

//    public static IEngine createEngine(String sdbUrl, String login, String password, String namedModel) {
//        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
//        JDBC.loadDriverMySQL();
//        SDBConnection conn = new SDBConnection(sdbUrl.toString(), login, password);
//        Store store = SDBFactory.connectStore(conn, storeDesc);
//        Model m = null;
//        if (namedModel != null) {
//            m = SDBFactory.connectNamedModel(store, namedModel);
//        } else {
//            m = SDBFactory.connectDefaultModel(store);
//        }
//
//        return JenaGraphFactory.createEngine(m);
//    }

    public static boolean isJenaSDBConnection(String sdbUrl, String login, String password) {
        try {
            StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
            JDBC.loadDriverMySQL();
            SDBConnection conn = new SDBConnection(sdbUrl, login, password);
            Store store = SDBFactory.connectStore(conn, storeDesc);
            Model m = SDBFactory.connectDefaultModel(store);
            return (m != null);
        } catch (SDBException e) {
            e.printStackTrace();
            return false;
        }
    }
}