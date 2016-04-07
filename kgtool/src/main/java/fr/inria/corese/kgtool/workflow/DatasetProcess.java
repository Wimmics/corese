package fr.inria.corese.kgtool.workflow;

import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Generic Process to be programmed
 * Select behavior with sw:mode attribute
 * Use case: produce a structured dataset from parallel process result graphs
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DatasetProcess extends WorkflowProcess {
    
    @Override
    void start(Data data){
    }
    
    @Override
    void finish(Data data){
        
    }
    
    @Override
    public Data run(Data data) {
        if (getMode() == null) {
            return dataset(data);
        }
        return data;
    }
    
    /**
     * Create a structured Dataset from a list of Data Graph Pick a main Graph,
     * set other graphs as named graphs
     * Process of Parallel may have a name which means that it is a named graph
     */
    Data dataset(Data data) {
         if (data.getDataList() != null) {
            // pick main Graph
            Data res = data.getResult();
            if (res != null) {
                Graph g = res.getGraph();
                for (Data dd : data.getDataList()) {                   
                    if (dd.getName() != null && dd.getGraph() != null && dd != res) {
                        g.setNamedGraph(dd.getName(), dd.getGraph());
                    }
                }
                Data value = new Data(this, g);
                return value;
            }
        }
        return data;
    }

}
