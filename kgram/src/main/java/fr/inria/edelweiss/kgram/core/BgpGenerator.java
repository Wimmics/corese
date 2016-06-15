package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    

    
    public void sortProducersByEdges(Map<Producer, ArrayList<Edge>> indexProducerEdges) {

       Set<Map.Entry<Producer,ArrayList<Edge>>> indexProducerEdgesEntries = indexProducerEdges.entrySet();

       // used linked list to sort, because insertion of elements in linked list is faster than an array list. 
       List<Map.Entry<Producer,ArrayList<Edge>>> indexProducerEdgesLinkedList = new LinkedList<Map.Entry<Producer,ArrayList<Edge>>>(indexProducerEdgesEntries);

       // sorting the List
       Collections.sort(indexProducerEdgesLinkedList, new Comparator<Map.Entry<Producer,ArrayList<Edge>>>() {

           @Override
           public int compare(Map.Entry<Producer, ArrayList<Edge>> element1,
                   Map.Entry<Producer, ArrayList<Edge>> element2) {        
              return (element1.getValue().size() != element2.getValue().size())? ((element1.getValue().size() < element2.getValue().size())? 1 : -1):0;
           }
       });

       // Storing the list into Linked HashMap to preserve the order of insertion.
       indexProducerEdges.clear();
       for(Map.Entry<Producer,ArrayList<Edge>> entry: indexProducerEdgesLinkedList) {
           indexProducerEdges.put(entry.getKey(), entry.getValue());
       }
   }
}
