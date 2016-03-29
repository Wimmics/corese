package fr.inria.corese.kgtool.workflow;

import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Generic Process to be programmed
 * Select behavior with sw:mode attribute
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DatasetProcess extends WorkflowProcess {
    
    @Override
    public Data process(Data data) {
        if (getMode() == null) {
            return dataset(data);
        }
        String mode = getMode().getLabel();
        if (mode.equals(WorkflowParser.TEST_VALUE)){
            System.out.println(data);
        }
        return data;
    }
    
    /**
     * Create a structured Dataset from a list of Data Graph Pick a main Graph,
     * set other graphs as named graphs
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
