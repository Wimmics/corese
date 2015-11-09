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
    public String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) {

        // IF (gNode == null) && (from.size <= 0) THEN nothing to do
        // IF (gNode == null) && (from.size > 0) THEN include from clauses ==> FROM
        // IF (gNode != null) && (from.size <= 0) THEN include graph pattern
        // IF (gNode != null) && (from.size > 0) THEN include from clauses  ==> FROM NAMED

        String sparqlPrefixes = "";

        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            for (String p : namespaceMgr.getPrefixes()) {
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }


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
        String fromClauses = "";
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

        String sparql = sparqlPrefixes;

        if (gNode == null) {
            sparql += "CONSTRUCT  { " + subject + " " + predicate + " " + object + " } " + fromClauses + "\n WHERE { \n";
            sparql += "\t " + subject + " " + predicate + " " + object + " .\n ";
        } else {
            sparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{" + subject + " " + predicate + " " + object + " }} " + fromClauses + "\n WHERE { \n";
            sparql += "\t GRAPH " + gNode.toString() + "{" + subject + " " + predicate + " " + object + "} .\n ";
        }

        // TODO CHECK filter.getExp().toString();
        if (filters.size() > 0) {
            sparql += "\t  FILTER (\n";
            int i = 0;
            for (Filter filter : filters) {
                if (i == (filters.size() - 1)) {
                    sparql += "\t\t " + ((Term) filter).toSparql() + "\n";
                } else {
                    sparql += "\t\t " + ((Term) filter).toSparql() + "&&\n";
                }
                i++;
            }
            sparql += "\t  )\n";
        }
        sparql += "}";

        return sparql;
    }

    
    @Override
    public String getSparqlQueryBGP(Node gNode, List<Node> from, Exp bgp, Environment environnement) {
        
        String queryBGP = new String();
        
        //Handling PREFIX
        String prefixes = new String();
        prefixes += getPrefix(environnement);
        
        
         //Handling FROM
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
        
        
        String filterClauses = new String(); 
        //FILTERS
        for (Exp e : bgp.getExpList()){
             if(e.isFilter()){
                 Edge edge = e.getEdge();
                 filterClauses += getFilter(environnement, edge);
             }
        }
         
        
        
        

        
        
        //Final query
        queryBGP += prefixes;
        queryBGP  += fromClauses;
        String body = pprint(bgp,environnement, filterClauses);
        queryBGP += body;
        logger.info("BGP QUERY: "+body);

        return queryBGP;
    }


    //generates pefix for each edge
    private String getPrefix(Environment env){
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
    
    //generates filters for each edge
    private String getFilter(Environment env, Edge edge){
        String filter = new String();
        Edge e = getReqEdge(env, edge);
         //filter handling
        List<Filter> filters = Util.getApplicableFilter(env, e);
        
        // TODO CHECK filter.getExp().toString(); ???
        if (filters.size() > 0) {
            filter += "\t  FILTER (\n";
            int i = 0;
            for (Filter f : filters) {
                if (i == (filters.size() - 1)) {
                    filter += "\t\t " + ((Term) f).toSparql() + "\n";
                } else {
                    filter+= "\t\t " + ((Term) f).toSparql() + "&&\n";
                }
                i++;
            }
            filter += "\t  )\n";
        }
        return filter;
    }

    //generates reqedge for each edge
    private Edge getReqEdge(Environment env, Edge edge){
        
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

        return EdgeImpl.create(null, subject, predicate, object);
    }
    
    
    
    String pprint(Exp exp, Environment env, String filter){
        
        String distinct = "";
        if(env.getQuery().isDistinct())
            distinct = " DISTINCT ";
        
        String sparqlQuery = "\n SELECT"+ distinct +"* WHERE { ";
        
        for (Exp ee : exp.getExpList()){
            if (ee.isEdge()){
                fr.inria.edelweiss.kgenv.parser.EdgeImpl edge = (fr.inria.edelweiss.kgenv.parser.EdgeImpl) ee.getEdge();
                Triple t = edge.getTriple();
                sparqlQuery += t.toString() + "\n ";
            }
//            already handled by getFilter
            else if (ee.isFilter()){
                Filter f = ee.getFilter();
                Expr e = f.getExp();
                sparqlQuery += "\t  FILTER (\n" + e.toString() + ")\n";
            
            }
        }
//        sparqlQuery += " "+filter+" ";
        sparqlQuery += "} ";
        return sparqlQuery;
    }
}
