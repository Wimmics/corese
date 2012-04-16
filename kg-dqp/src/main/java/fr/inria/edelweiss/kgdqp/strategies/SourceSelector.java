/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerImpl;
import fr.inria.edelweiss.kgenv.parser.EdgeImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Helper singleton class aiming at enhancing source selection while performing
 * distributed query processing.
 *
 * @author Alban Gaignard
 */
public class SourceSelector {

    private static Logger logger = Logger.getLogger(SourceSelector.class);
    private static SourceSelector instance = null;

    public static SourceSelector getInstance() {
        if (instance != null) {
            return instance;
        } else {
            return new SourceSelector();
        }
    }

    public static boolean ask(Edge edge, RemoteProducerImpl rp, Environment env) {
        if (rp.getCacheIndex().containsKey(edge.getLabel())) {
            return rp.getCacheIndex().get(edge.getLabel());
        } else {
            // ASK
            String query = SourceSelector.getSparqlAsk(edge, env);
            try {
//                String res = null;
//                Map<String, Object> reqCtxt = ((BindingProvider) rp.getRp()).getRequestContext();
//                reqCtxt.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//                reqCtxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//                StreamingDataHandler streamingDh = (StreamingDataHandler) rp.getRp().getEdges(query);
//                if (streamingDh != null) {
//                    try {
//                        InputStream is = (InputStream) streamingDh.readOnce();
//                        res = new String(IOUtils.toByteArray(is));
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }

                String res = rp.getRp().getEdges(query);
//                logger.info("Remote ASK for "+edge.getEdgeNode().getLabel());
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
}
