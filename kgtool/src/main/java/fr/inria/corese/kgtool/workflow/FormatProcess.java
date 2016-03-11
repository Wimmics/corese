/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.print.ResultFormat;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class FormatProcess extends AbstractProcess {
    
    int format;
    Graph graph;
    
    FormatProcess(int f){
        format = f;
    }
    
    
    

}
