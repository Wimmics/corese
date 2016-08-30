/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.parser.EdgeImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * An optimizer that propagates intermediate results (variable bindings)
 * through edge requests. 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteQueryOptimizerBinding implements RemoteQueryOptimizer {

    private static Logger logger = LogManager.getLogger(RemoteQueryOptimizerBinding.class);

    RemoteQueryOptimizerBinding() {
    }

    @Override
    public String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) {
        String sparqlPrefixes = "";

        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            for (String p : namespaceMgr.getPrefixes()) {
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        //environment handlings
        Node subject = env.getNode(edge.getNode(0));
        Node object = env.getNode(edge.getNode(1));
        Node predicate = null;

        if (edge.getEdgeVariable() != null) {
            predicate = env.getNode(edge.getEdgeVariable());
        }

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

        String sparql = sparqlPrefixes;
        sparql += "construct  { " + reqEdge + " } \n where { \n";
        sparql += "\t " + reqEdge + " .\n ";
        sparql += "}";

        return sparql;
    }

    @Override
    public String getSparqlQueryBGP(Node gNode, List<Node> from, Exp bgp, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
