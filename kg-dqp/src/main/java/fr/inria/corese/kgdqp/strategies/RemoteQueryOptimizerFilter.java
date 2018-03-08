/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.corese.kgdqp.core.Util;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import java.util.List;

/**
 * An optimizer that propagates suitable sparql filters 
 * through edge requests. 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteQueryOptimizerFilter implements RemoteQueryOptimizer {

    @Override
    public String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) {
        String sEdge = edge.toString();
        String sparqlPrefixes = "";

        String sparqlfilter = null;

        //Filter pushing filter to the remote producer
        //Add the filter only if it is applicable
        List<Filter> kgFilters = Util.getApplicableFilter(env, edge);

        if (kgFilters.size() > 0) {
            sparqlfilter = "FILTER (\n";
            int j = 0;
            for (Filter kgFilter : kgFilters) {

                if (j == (kgFilters.size() - 1)) {
                    sparqlfilter += "\t\t" + ((Term) kgFilter).toSparql() + "\n";
                } else {
                    sparqlfilter += "\t\t" + ((Term) kgFilter).toSparql() + " && \n";
                }

                j++;
            }
            sparqlfilter += "\t)";
        }

        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            for (String p : namespaceMgr.getPrefixes()) {
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        String sparql = sparqlPrefixes;
        sparql += "construct  { "+edge.getNode(0) + " "+edge.getEdgeNode()+" "+edge.getNode(1) +" } \n where { \n";
        sparql += "\t "+edge.getNode(0) + " "+edge.getEdgeNode()+" "+edge.getNode(1)+" .\n ";
        if (sparqlfilter != null) {
            sparql += "\t" + sparqlfilter + "\n";
        }
        sparql += "}";

        return sparql;
    }

    @Override
    public String getSparqlQueryBGP(Node gNode, List<Node> from, Exp bgp, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}