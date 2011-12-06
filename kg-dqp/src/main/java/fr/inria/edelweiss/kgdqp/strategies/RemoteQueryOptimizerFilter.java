/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * An optimizer that propagates suitable sparql filters 
 * through edge requests. 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteQueryOptimizerFilter implements RemoteQueryOptimizer {

    @Override
    public String getSparqlQuery(Edge edge, Environment env) {
        String sEdge = edge.toString();
        String sparqlPrefixes = "";

        String sparqlfilter = null;

        //Filter pushing filter to the remote producer
        //Add the filter only if it is applicable
        //Filter decomposition in conjunctive / disjunctive clauses ?
        // bug : List<Filter> kgFilters = env.getQuery().getBody().getFilters(); not working as well as env.getQuery().getFilters();
        List<Filter> kgFilters = new ArrayList<Filter>();
        for (Exp exp : env.getQuery().getBody()) {
            if (exp.isFilter()) {
                Filter kgFilter = exp.getFilter();
                if (bound(edge, kgFilter)) {
                    kgFilters.add(kgFilter);
                }
            }
        }

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
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        String sparql = sparqlPrefixes;
        sparql += "construct  { "+sEdge+" } \n where { \n";
        sparql += "\t "+sEdge+" .\n ";
        if (sparqlfilter != null) {
            sparql += "\t" + sparqlfilter + "\n";
        }
        sparql += "}";

        return sparql;
    }

    /*
     * ?x p ?y 
     * FILTER ((?x > 10) && (?z > 10))
     * 
     */
    public static boolean bound(Edge edge, Filter filter) {
        List<String> vars = new ArrayList<String>();
        if (edge.getNode(0).isVariable()) {
            vars.add(edge.getNode(0).toString());
        }
        if (edge.getNode(1).isVariable()) {
            vars.add(edge.getNode(1).toString());
        }

        List<String> varsFilter = filter.getVariables();
        for (String var : varsFilter) {
            if (!vars.contains(var)) {
                return false;
            }
        }
        return true;
    }
}
