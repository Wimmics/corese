package fr.inria.corese.gui.core;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author corby
 */
public class Callback {
    
    public IDatatype display(IDatatype name, IDatatype node, IDatatype suc) {
        MainFrame.display(String.format("report: %s %s", name.getLabel(), node.getLabel(), suc));
        return DatatypeMap.TRUE;
    }
    
}
