/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import java.util.Enumeration;
import java.util.List;

/**
 * A naive optimizer that converts an edge request into 
 * a simple sparql construct query. 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteQueryOptimizerSimple implements RemoteQueryOptimizer {

    @Override
    public String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) {
        String sEdge = edge.toString();
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
        sparql += "construct  { "+sEdge+" } \n where { "+sEdge+" }";

        return sparql;
    }
}
