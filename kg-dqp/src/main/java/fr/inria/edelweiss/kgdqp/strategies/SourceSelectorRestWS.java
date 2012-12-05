/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerRestWSImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.query.Environment;
import java.util.Enumeration;
import javax.xml.ws.WebServiceException;
import org.apache.log4j.Logger;

/**
 * Helper singleton class aiming at enhancing source selection while performing
 * distributed query processing.
 *
 * @author Eric TOGUEM
 */
public class SourceSelectorRestWS {

    private static Logger logger = Logger.getLogger(SourceSelectorRestWS.class);
    private static SourceSelectorRestWS instance = null;
    
    private SourceSelectorRestWS(){
	
    }

    public static SourceSelectorRestWS getInstance() {
        if (instance != null) {
            return instance;
        } else {
            return instance = new SourceSelectorRestWS();
        }
    }

    public static boolean ask(Edge edge, RemoteProducerRestWSImpl rp, Environment env) {
        if (rp.getCacheIndex().containsKey(edge.getLabel())) {
            return rp.getCacheIndex().get(edge.getLabel());
        } else {
            // ASK
            String query = SourceSelectorRestWS.getSparqlAsk(edge, env);
            try {

                String res = rp.getRp().getEdges(query);
                logger.info("Remote ASK for "+edge.getEdgeNode().getLabel());
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
    
    public static boolean ask(String predicate, RemoteProducerRestWSImpl rp, ASTQuery ast) {
        if (rp.getCacheIndex().containsKey(predicate)) {
            return rp.getCacheIndex().get(predicate);
        } else {
            // ASK
            String query = SourceSelectorRestWS.getSparqlAsk(predicate, ast);
            try {

                String res = rp.getRp().getEdges(query);
                logger.info("Remote ASK for "+predicate);
                if ((res == null) || (res.length() == 0)) {
                    //update cache
                    rp.getCacheIndex().put(predicate, false);
                    return false;
                } else {
                    //update cache
                    rp.getCacheIndex().put(predicate, true);
                    return true;
                }
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
        sparql += "ask  { ?_s " + edge.getEdgeNode().toString() + " ?_o } \n";

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
