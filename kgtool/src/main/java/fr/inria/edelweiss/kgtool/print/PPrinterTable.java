/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.triple.parser.NSManager;
import java.util.HashMap;

/**
 * Map pprinter name to pprinter resource path
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class PPrinterTable extends HashMap<String, String> {

    HashMap<String, Boolean> table;

    PPrinterTable() {
        init();
    }

    void init() {
        // namespace to pprinter
        put(NSManager.OWL,  PPrinter.OWL);
        put(NSManager.SPIN, PPrinter.SPIN);
        put(NSManager.SQL,  PPrinter.SQL);

        table = new HashMap<String, Boolean>();
        // pprinter is optimized ?
        table.put(PPrinter.OWL, false);
        table.put(PPrinter.SPIN, true);
        table.put(PPrinter.SQL, true);

    }
    
    boolean isOptimize(String name){
        Boolean b = table.get(name);
        if (b != null){
            return b;
        }
        return false;
    }
    
    void setOptimize(String name, boolean b){
        table.put(name, b);
    }
    
}