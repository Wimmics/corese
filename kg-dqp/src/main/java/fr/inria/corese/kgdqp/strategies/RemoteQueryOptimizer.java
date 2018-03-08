/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.strategies;


//import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import java.util.List;

/**
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public interface RemoteQueryOptimizer {
    
    public abstract String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) ;

    public String getSparqlQueryBGP(Node gNode, List<Node> from, Exp bgp, Environment env);
   
}
