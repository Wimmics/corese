/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * An optimizer that combines filter and binding optimizations.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 * @author Abdoul Macina, macina@i3s.unice.fr
 */
public class RemoteQueryOptimizerFull implements RemoteQueryOptimizer {

    private static Logger logger = Logger.getLogger(RemoteQueryOptimizerFull.class);

    @Override
    public String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) {

        // IF (gNode == null) && (from.size <= 0) THEN nothing to do
        // IF (gNode == null) && (from.size > 0) THEN include from clauses ==> FROM
        // IF (gNode != null) && (from.size <= 0) THEN include graph pattern
        // IF (gNode != null) && (from.size > 0) THEN include from clauses  ==> FROM NAMED
//        String sparqlPrefixes = "";
//
//        //prefix handling
//        if (env.getQuery().getAST() instanceof ASTQuery) {
//            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
//            NSManager namespaceMgr = ast.getNSM();
//            for (String p : namespaceMgr.getPrefixes()) {
//                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
//            }
//        }

        String sparqlPrefixes = getPrefixes(env);
        
        //binding handling
        Node subject = env.getNode(edge.getNode(0));
        Node object = env.getNode(edge.getNode(1));
        Node predicate = null;
        // 
        if (edge.getEdgeVariable() != null) {
            predicate = env.getNode(edge.getEdgeVariable());
        }

        //  No bindings found ?
        if (subject == null) {
            subject = edge.getNode(0);
        }
        if (object == null) {
            object = edge.getNode(1);
        }
        if (predicate == null) {
            if (edge.getEdgeVariable() != null) {
                predicate = edge.getEdgeVariable();
            } else {
                predicate = edge.getEdgeNode();
            }
        }

        Edge reqEdge = EdgeImpl.create(null, subject, predicate, object);

        //filter handling
        List<Filter> filters = Util.getApplicableFilter(env, reqEdge);

        //from handling
//        String fromClauses = "";
//        if ((from != null) && (!from.isEmpty())) {
//            for (Node f : from) {
//                if (gNode == null) {
//                    fromClauses += "FROM ";
//                } else {
//                    fromClauses += "FROM NAMED ";
//                }
//                fromClauses += f + " ";
//            }
//        }

        String fromClauses = getFroms(from, gNode);
                
        String queryBody = "";

        if (gNode == null) {
            queryBody += "CONSTRUCT  { " + subject + " " + predicate + " " + object + " } " + fromClauses + "\n WHERE { \n";
            queryBody += "\t " + subject + " " + predicate + " " + object + " .\n ";
        } else {
            queryBody += "CONSTRUCT  { GRAPH " + gNode.toString() + "{" + subject + " " + predicate + " " + object + " }} " + fromClauses + "\n WHERE { \n";
            queryBody += "\t GRAPH " + gNode.toString() + "{" + subject + " " + predicate + " " + object + "} .\n ";
        }

        
        if (filters.size() > 0) {
            queryBody += "\t  FILTER (\n";
            int i = 0;
            for (Filter filter : filters) {
                if (i == (filters.size() - 1)) {
                    queryBody += "\t\t " + ((Term) filter).toSparql() + "\n";
                } else {
                    queryBody += "\t\t " + ((Term) filter).toSparql() + "&&\n";
                }
                i++;
            }
            queryBody += "\t  )\n";
        }
        queryBody += "}";

        logger.info("EDGE QUERY: "+queryBody);
        String sparqlQuery =   sparqlPrefixes + queryBody;
        return sparqlQuery;
    }

    @Override
    public String getSparqlQueryBGP(Node gNode, List<Node> from, Exp bgp, Environment environnement) {

        String queryBGP = new String();

        //Handling PREFIX
        String prefixes = getPrefixes(environnement);

        //Handling FROM
        String fromClauses = getFroms(from, gNode);

        //Final query
        queryBGP += prefixes;
        queryBGP += fromClauses;
        String body = bgpBodyQuery(bgp, environnement);
        queryBGP += body;
        logger.info("BGP QUERY: " + body);

        return queryBGP;
    }

    //generates all pefixes
    private String getPrefixes(Environment env) {
        String prefix = new String();

        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            for (String p : namespaceMgr.getPrefixes()) {
                prefix += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        return prefix;
    }

      //generates all Froms
    private String getFroms(List<Node> from, Node gNode) {
        String fromClauses = new String();
        if ((from != null) && (!from.isEmpty())) {
            for (Node f : from) {
                if (gNode == null) {
                    fromClauses += "FROM ";
                } else {
                    fromClauses += "FROM NAMED ";
                }
                fromClauses += f + " ";
            }
        }
        return fromClauses;
    }
    
    
    String bgpBodyQuery(Exp exp, Environment env) {

//        String distinct = "";
//        //TO check other keywords to add???
//        if (env.getQuery().isDistinct()) {
//            distinct = " DISTINCT ";
//        }

        String sparqlQuery = "\n SELECT * WHERE { \n";

        for (Exp ee : exp.getExpList()) {
            //edges
            if (ee.isEdge()) {
                fr.inria.edelweiss.kgenv.parser.EdgeImpl edge = (fr.inria.edelweiss.kgenv.parser.EdgeImpl) ee.getEdge();
                Triple t = edge.getTriple();
                sparqlQuery += "\t " + t.toString() + "\n ";

                //filters  To check: if it is necessary and how to add  binding
                List<Filter> filters = ee.getFilters();
                for (Filter f : filters) {
                    if (f != null) {
                        Expr e = f.getExp();
                        sparqlQuery += "\t  FILTER (" + e.toString() + ")\n";
                    }
                }

            }
        }
        sparqlQuery += "} ";

        return sparqlQuery;
    }
}
