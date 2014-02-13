/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Helper singleton class aiming at enhancing source selection while performing
 * distributed query processing.
 *
 * @author Alban Gaignard
 */
public class SourceSelectorWS {

    private static Logger logger = Logger.getLogger(SourceSelectorWS.class);
    private static SourceSelectorWS instance = null;

    public static SourceSelectorWS getInstance() {
        if (instance != null) {
            return instance;
        } else {
            return new SourceSelectorWS();
        }
    }

    public static boolean ask(Edge edge, RemoteProducerWSImpl rp, Environment env) {
        if (rp.getCacheIndex().containsKey(edge.getLabel())) {
            return rp.getCacheIndex().get(edge.getLabel());
        } else {
            // ASK
            String query = SourceSelectorWS.getSparqlAsk(edge, env);
            try {

//                String res = rp.getEndpoint().getEdges(query);
                String res = rp.getEndpoint().query(query);
                logger.debug("Remote ASK for "+edge.getEdgeNode().getLabel());
                logger.debug(res);
                if ((res == null) || (res.length() == 0)) {
                    //update cache
                    rp.getCacheIndex().put(edge.getLabel(), false);
                    return false;
                } else {
                    //update cache
                    rp.getCacheIndex().put(edge.getLabel(), true);
                    return true;
                }
            } catch (WebServiceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean ask(String predicate, RemoteProducerWSImpl rp, ASTQuery ast) {
        if (rp.getCacheIndex().containsKey(predicate)) {
            return rp.getCacheIndex().get(predicate);
        } else {
            // ASK
            String query = SourceSelectorWS.getSparqlAsk(predicate, ast);
            try {
//                String res = rp.getEndpoint().getEdges(query);
                String res = rp.getEndpoint().query(query);
                if ((res == null) || (res.length() == 0)) {
                    rp.getCacheIndex().put(predicate, false);
                    return false;
                } else {
                    Graph g = Graph.create();
                    Mappings maps = SPARQLResult.create(g).parseString(res);
                    if (maps.size() == 0) {
                        rp.getCacheIndex().put(predicate, false);
                        return false;
                    } else {
                        rp.getCacheIndex().put(predicate, true);
                        return true;
                    }
                }
            } catch (ParserConfigurationException ex) {
                java.util.logging.Logger.getLogger(SourceSelectorWS.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                java.util.logging.Logger.getLogger(SourceSelectorWS.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SourceSelectorWS.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WebServiceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getSparqlAsk(Edge edge, Environment env) {

        String sparqlPrefixes = "";

        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        String sparql = sparqlPrefixes;
        String ask = "ask  { ?_s " + edge.getEdgeNode().toString() + " ?_o } \n";
        
//        if (edge.getEdgeNode().toString().contains("#type") && edge.getNode(1).isConstant()) {
//            ask = "ask  { ?_s " + edge.getEdgeNode().toString() + " "+ edge.getNode(1).toString()+"} \n";
//        }
        
        sparql += ask;

        return sparql;

    }

    public static String getSparqlAsk(String predicate, ASTQuery ast) {

        String sparqlPrefixes = "";

        //prefix handling
        NSManager namespaceMgr = ast.getNSM();
        Enumeration<String> prefixes = namespaceMgr.getPrefixes();
        while (prefixes.hasMoreElements()) {
            String p = prefixes.nextElement();
            sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
        }

        String sparql = sparqlPrefixes;
        sparql += "ask  { ?_s " + predicate + " ?_o } \n";

        return sparql;

    }
}
