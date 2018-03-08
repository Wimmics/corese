/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.strategies;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.load.SPARQLResult;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

/**
 * Helper singleton class aiming at enhancing source selection while performing
 * distributed query processing.
 *
 * @author Alban Gaignard
 */
public class SourceSelectorWS {

    private static Logger logger = LogManager.getLogger(SourceSelectorWS.class);
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
                if ((res == null) || (res.length() == 0) || (res.toLowerCase().contains("false"))) {
                    //update cache
                    rp.getCacheIndex().put(edge.getLabel(), false);
                    return false;
                } else {
                    //update cache
                    Graph g = Graph.create();
//                    logger.info(res);
                    Mappings maps;
                    try {
                        maps = SPARQLResult.create(g).parseString(res);
                        if (maps.size() == 0) {
                            rp.getCacheIndex().put(edge.getLabel(), false);
                            return false;
                        } else {
                            rp.getCacheIndex().put(edge.getLabel(), true);
                            return true;
                        }
                    } catch (ParserConfigurationException ex) {
                        logger.error("Parsing error for ASK results");
                        logger.error(ex.getMessage());
                    } catch (SAXException ex) {
                        logger.error("Parsing error for ASK results");
                        logger.error(ex.getMessage());
                    } catch (IOException ex) {
                        logger.error("I/O error for ASK results");
                        logger.error(ex.getMessage());
                    }
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
//            logger.info("source selection for predicate " + predicate + " on " + rp.getEndpoint().getEndpoint());

            String query = SourceSelectorWS.getSparqlAsk(predicate, ast);
//            logger.info("with query " + query);

            try {
//                String res = rp.getEndpoint().getEdges(query);
                String res = rp.getEndpoint().query(query);
                if ((res == null) || (res.length() == 0) || (res.toLowerCase().contains("false"))) {
                    rp.getCacheIndex().put(predicate, false);
                    return false;
                } else {
                    Graph g = Graph.create();
//                    logger.info(res);
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
                logger.error("Parsing error for ASK results");
                logger.error(ex.getMessage());
            } catch (SAXException ex) {
                logger.error("Parsing error for ASK results");
                logger.error(ex.getMessage());
            } catch (IOException ex) {
                logger.error("I/O error for ASK results");
                logger.error(ex.getMessage());
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
            for (String p : namespaceMgr.getPrefixes()) {
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
        for (String p : namespaceMgr.getPrefixes()) {
            sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
        }

        String sparql = sparqlPrefixes;
        sparql += "ask  { ?_s " + predicate + " ?_o } \n";

        return sparql;

    }
    
    
    public static boolean ask(Exp bgp, RemoteProducerWSImpl rp, Environment env) {
        boolean result =true;
        
        for(int i = 0; i<bgp.getExpList().size() && result; i++){
            result = ask(bgp.getExpList().get(i).getEdge(), rp, env) && result;
        }
        return result;
    }
    
}
