/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.triple.parser.NSManager;
import static fr.inria.edelweiss.kgtool.print.PPrinter.OWL;
import static fr.inria.edelweiss.kgtool.print.PPrinter.SPIN;
import static fr.inria.edelweiss.kgtool.print.PPrinter.SQL;
import java.util.HashMap;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
class PPrinterTable extends HashMap<String, String> {
                                   
            PPrinterTable(){
                put(NSManager.SPIN, SPIN);
                put(NSManager.SQL, SQL);
                put(NSManager.OWL, OWL);
            }
            
            
}