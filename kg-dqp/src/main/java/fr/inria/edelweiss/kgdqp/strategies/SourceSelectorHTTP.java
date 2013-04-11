/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerHTTPImpl;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.io.IOException;
import java.util.Enumeration;
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
@Deprecated
public class SourceSelectorHTTP {

    private static Logger logger = Logger.getLogger(SourceSelectorHTTP.class);
    private static SourceSelectorHTTP instance = null;

    public static SourceSelectorHTTP getInstance() {
        if (instance != null) {
            return instance;
        } else {
            return new SourceSelectorHTTP();
        }
    }

    public static boolean ask(String predicate, RemoteProducerHTTPImpl rp, ASTQuery ast) {
        if (rp.getCacheIndex().containsKey(predicate)) {
            return rp.getCacheIndex().get(predicate);
        } else {
            // ASK
            String query = SourceSelectorHTTP.getSparqlAsk(predicate, ast);
            try {
                StringBuffer sb = rp.getRp().doPost(query);
                ProducerImpl p = ProducerImpl.create(Graph.create());
                XMLResult r = XMLResult.create(p);
                Mappings maps = r.parseString(sb.toString());

                if (maps.size() == 0) {
                    //update cache
                    rp.getCacheIndex().put(predicate, false);
//                    System.out.println("FALSE");
                    return false;
                } else {
                    //update cache
                    rp.getCacheIndex().put(predicate, true);
//                    System.out.println("TRUE");
                    return true;
                }
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (WebServiceException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static String getSparqlAsk(String predicate, ASTQuery ast) {

        String sparqlPrefixes = "";

        //What with a new empty env ?
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
