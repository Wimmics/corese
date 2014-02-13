package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Query;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Keep track of nodes already printed to prevent loop Variant: check the pair
 * (dt, template) do not to loop on the same template ; may use several
 * templates on same node dt
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
class PPrinterStack {

    ArrayList<IDatatype> list;
    HashMap<IDatatype, ArrayList<Query>> map;
    HashMap<IDatatype, IDatatype> visit;
    boolean multi = true;

    PPrinterStack(boolean b) {
        list = new ArrayList<IDatatype>();
        map = new HashMap<IDatatype, ArrayList<Query>>();
        visit = new HashMap<IDatatype, IDatatype>();
        multi = b;
    }

    int size() {
        return list.size();
    }

    void push(IDatatype dt) {
        list.add(dt);
    }

    void push(IDatatype dt, Query q) {
        list.add(dt);
        if (multi) {
            ArrayList<Query> qlist = map.get(dt);
            if (qlist == null) {
                qlist = new ArrayList<Query>();
                map.put(dt, qlist);
            }
            qlist.add(q);
        }
    }

    IDatatype pop() {
        if (list.size() > 0) {
            int last = list.size() - 1;
            IDatatype dt = list.get(last);
            list.remove(last);
            if (multi) {
                ArrayList<Query> qlist = map.get(dt);
                qlist.remove(qlist.size() - 1);
            }
            return dt;
        }
        return null;
    }

    boolean contains(IDatatype dt) {
        return list.contains(dt);
    }

    boolean isVisited(IDatatype dt) {
        if (visit.containsKey(dt)) {
            return true;
        }
        ArrayList<Query> qlist = map.get(dt);
        if (qlist == null) {
            return false;
        }
        return qlist.size() > 1;
    }

    void visit(IDatatype dt) {
        visit.put(dt, dt);
    }

    boolean contains(IDatatype dt, Query q) {
        ArrayList<Query> qlist = map.get(dt);
        return qlist != null && qlist.contains(q);
    }

    boolean contains2(IDatatype dt, Query q) {
        boolean b = list.contains(dt);
        if (b && multi) {
            ArrayList<Query> qlist = map.get(dt);
            return qlist.contains(q);
        }
        return b;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (IDatatype dt : list) {
            sb.append(i++);
            sb.append(" ");
            sb.append(dt);
            sb.append(": ");
            for (Query q : map.get(dt)) {
                sb.append(q.getStringPragma(Pragma.FILE));
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}