/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;


//import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import java.util.List;

/**
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public interface RemoteQueryOptimizer {
    
    public abstract String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) ;
   
}
