/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgenv.parser.EdgeImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * An optimizer that combines filter and binding optimizations. 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteQueryOptimizerFull implements RemoteQueryOptimizer {

    private static Logger logger = Logger.getLogger(RemoteQueryOptimizerFull.class);

    @Override
    public String getSparqlQuery(Edge edge, Environment env) {
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
        

        //binding handling
        Node subject = env.getNode(edge.getNode(0));
        Node object = env.getNode(edge.getNode(1));
        Node predicate = null;

        ArrayList<String> filters = new ArrayList<String>();

        if (edge.getEdgeVariable() != null) {
            predicate = env.getNode(edge.getEdgeVariable());
        }
        

        //   
        if (subject == null) {
            subject = edge.getNode(0);
        } 
        if (object == null) {
            object = edge.getNode(1);
        } 
        if (predicate == null) {
            predicate = edge.getEdgeNode();
        } 

        Edge reqEdge = EdgeImpl.create(predicate, subject, object);
        
        
        //filter handling
        for (Exp exp : env.getQuery().getBody()) {
            if (exp.isFilter()) {
                Filter kgFilter = exp.getFilter();
                if (bound(reqEdge, kgFilter)) {
                    filters.add(((Term) kgFilter).toSparql());
                }
            }
        }
        
        String sparql = sparqlPrefixes;
        sparql += "construct  { " + reqEdge + " } \n where { \n";
        sparql += "\t " + reqEdge + " .\n ";

        if (filters.size() > 0) {
            sparql += "\t  FILTER (\n";
            int i = 0;
            for (String filter : filters) {
                if (i == (filters.size() - 1)) {
                    sparql += "\t\t " + filter + "\n";
                } else {
                    sparql += "\t\t " + filter + "&&\n";
                }
                i++;
            }
            sparql += "\t  )\n";
        }
        sparql += "}";

        return sparql;
    }
    
    /*
     * ?x p ?y 
     * FILTER ((?x > 10) && (?z > 10))
     * 
     */
    public boolean bound(Edge edge, Filter filter) {
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
