package fr.inria.corese.kgtool.workflow;

import fr.inria.edelweiss.kgraph.core.Graph;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class FormatProcess extends WorkflowProcess {
    
    int format;
    Graph graph;
    
    FormatProcess(int f){
        format = f;
    }
    
    
    

}
