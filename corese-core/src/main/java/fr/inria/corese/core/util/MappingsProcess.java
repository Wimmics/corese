/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.core.util;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.print.RDFResultFormat;

import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class MappingsProcess {
    
    Mappings map;
    
    MappingsProcess (Mappings m){
        map = m;
    }
    
    public static MappingsProcess create(Mappings m){
        return new MappingsProcess(m);
    }
    
    public Graph getGraph() {
        RDFResultFormat f = RDFResultFormat.create(map);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadString(f.toString(), "", ld.defaultGraph(), "", Load.TURTLE_FORMAT);
        } catch (LoadException ex) {
            LoggerFactory.getLogger(MappingsProcess.class.getName()).error(  "", ex);
        }
        return g;
    }

}
