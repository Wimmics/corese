/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgimport;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import fr.inria.corese.kgram.api.core.Entity;
//import fr.inria.corese.sparql.api.IEngine;
//import fr.inria.corese.kgengine.GraphEngine;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author gaignard
 */
public class JenaGraphFactory {

    public static Logger logger = LogManager.getLogger(JenaGraphFactory.class);

    /**
     * Handling of XSDDateType ?? -> transformed as String into KGRAM See
     * http://openjena.org/javadoc/com/hp/hpl/jena/datatypes/xsd/XSDDatatype.html
     *
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
//            logger.info(st.asTriple().toString());

            //subject
            subject = getNode(st.getSubject());

            //property
            if (st.getPredicate().isProperty()) {
//                logger.debug("Property URI " + st.getPredicate().getURI());
                predicate = g.addProperty(st.getPredicate().getURI());
            } else {
                logger.warn("Predicate " + st.getPredicate().toString() + " not recognized as a JENA property.");
            }

            //object 
            object = getNode(st.getObject());

            if (! ((subject == null) || (predicate == null) || (object == null) || (source == null))) {
                Entity e = g.create(source, subject, predicate, object);
                g.add(e);
            }
        }
    }

    public static Node getNode(RDFNode jenaNode) {
        Graph g = Graph.create();
        Node kgNode = null;

        if (jenaNode.isAnon()) {
            kgNode = g.addBlank(jenaNode.asNode().getBlankNodeId().toString());
        } else if (jenaNode.isLiteral()) {
//                logger.debug("Object as literal " + jenaNode.toString());
            Literal l = jenaNode.asLiteral();
//                logger.debug("lexical form " + l.getLexicalForm());
            Object value = null;
            try {
                value = l.getValue();
            } catch (com.hp.hpl.jena.datatypes.DatatypeFormatException ex) {
                logger.warn(ex.getMessage());
                value = l.getLexicalForm();
            }

            if (value instanceof Integer) {
                kgNode = g.addLiteral(((Integer) value).intValue());
            } else if (value instanceof Long) {
                kgNode = g.addLiteral(((Long) value).longValue());
            } else if (value instanceof Float) {
                kgNode = g.addLiteral(((Float) value).floatValue());
            } else if (value instanceof Double) {
                kgNode = g.addLiteral(((Double) value).doubleValue());
            } else if (value instanceof Boolean) {
                kgNode = g.addLiteral(((Boolean) value).booleanValue());
            } else if (value instanceof String) {
                kgNode = g.addLiteral(((String) value));
            } else if (value instanceof XSDDateTime) {
                kgNode = g.addLiteral(l.getLexicalForm(), l.getDatatypeURI(), null);
            } else if (l.getDatatypeURI() != null) {
                kgNode = g.addLiteral(l.getLexicalForm(), l.getDatatypeURI(), null);
//                    logger.debug("Literal value " + value + " : " + value.getClass().getCanonicalName() + " handled by KGRAM graphs through "+l.getDatatypeURI());
            } else {
                logger.error("Literal value " + value + " : " + value.getClass().getCanonicalName() + " not handled by KGRAM graphs");
            }

        } else if (jenaNode.isResource()) {
//                logger.debug("Object as resource " + jenaNode.toString());
            kgNode = g.addResource(jenaNode.asResource().toString());
        } else if (jenaNode.isURIResource()) {
//                logger.debug("Object as resource " + jenaNode.asResource().getURI());
            kgNode = g.addResource(jenaNode.asResource().getURI());
        } else {
            logger.warn("Object " + jenaNode.toString() + " not recognized by JENA as a blank node, a literal or a resource.");
        }

        return kgNode;
    }

    public static Graph createGraph(Model m) {
        Graph g = Graph.create();
        JenaGraphFactory.updateGraph(m, g);
        return g;
    }

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

    public static boolean isJenaSDBConnection(String sdbUrl, String login, String password) {
        try {
            StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
            JDBC.loadDriverMySQL();
            SDBConnection conn = new SDBConnection(sdbUrl, login, password);
            Store store = SDBFactory.connectStore(conn, storeDesc);
            Model m = SDBFactory.connectDefaultModel(store);
            return (m != null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}