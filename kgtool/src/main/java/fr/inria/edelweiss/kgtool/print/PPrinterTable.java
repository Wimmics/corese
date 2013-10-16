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
class PPrinterTable extends HashMap<String, String> {

    HashMap<String, Boolean> table;

    PPrinterTable() {
        init();
    }

    void init() {
        put(NSManager.SPIN, PPrinter.SPIN);
        put(NSManager.SQL,  PPrinter.SQL);
        put(NSManager.OWL,  PPrinter.OWL);

        table = new HashMap<String, Boolean>();
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