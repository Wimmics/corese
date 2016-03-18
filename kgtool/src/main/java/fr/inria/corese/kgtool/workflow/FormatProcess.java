/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.edelweiss.kgraph.core.Graph;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class FormatProcess extends SemanticProcess {
    
    int format;
    Graph graph;
    
    FormatProcess(int f){
        format = f;
    }
    
    
    

}
