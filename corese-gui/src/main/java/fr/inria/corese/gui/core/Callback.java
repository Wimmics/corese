package fr.inria.corese.gui.core;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author corby
 */
public class Callback {
    
    public IDatatype display(IDatatype dt) {
        return mdisplay(dt);
    }
    
    public IDatatype display(IDatatype dt1, IDatatype dt2) {
        return mdisplay(dt1, dt2);
    }
    
    public IDatatype display(IDatatype dt1, IDatatype dt2, IDatatype dt3) {
        return mdisplay(dt1, dt2, dt3);
    }
    
    public IDatatype display(IDatatype dt1, IDatatype dt2, IDatatype dt3, IDatatype dt4) {
        return mdisplay(dt1, dt2, dt3, dt4);
    }
    
    public IDatatype display(IDatatype dt1, IDatatype dt2, IDatatype dt3, IDatatype dt4, IDatatype dt5) {
        return mdisplay(dt1, dt2, dt3, dt4, dt5);
    }
    
    public IDatatype mdisplay(IDatatype... param) {
        StringBuilder sb = new StringBuilder();
        for (IDatatype dt : param) {
            sb.append(dt.getLabel()).append(" ");
        }
        MainFrame.display(sb.toString());
        return DatatypeMap.TRUE;
    }
    
    public IDatatype display1(IDatatype name, IDatatype node, IDatatype suc) {
        MainFrame.display(String.format("report: %s %s", name.getLabel(), node.getLabel(), suc));
        return DatatypeMap.TRUE;
    }
    
}
