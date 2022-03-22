package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.HashMap;

public interface BgpGenerator {

     HashMap<Edge, ArrayList<Producer>> getIndexEdgeProducers();

     HashMap<Edge, ArrayList<Node>> getIndexEdgeVariables();

     HashMap<Edge, ArrayList<Filter>> getIndexEdgeFilters();

     void setIndexEdgeProducers(HashMap<Edge, ArrayList<Producer>> tmpEdgeProducers);

     void setIndexEdgeVariables(HashMap<Edge, ArrayList<Node>> tmpEdgeVariables);

     void setIndexEdgeFilters(HashMap<Edge, ArrayList<Filter>> tmpEdgeFilters);
    
     Exp process(Exp exp);

    public HashMap<Edge, Exp> getEdgeAndContext();
    
}
