package fr.inria.corese.core.query;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import java.util.HashMap;

/**
 *
 * Cache: store subsumption between query and target node
 */
public class Cache {
   
    STable table;

    class BTable extends HashMap<String, Boolean> {
    }

    class STable extends HashMap<String, BTable> {
    }

    Cache(Query q) {
        table = new STable();
    }

    BTable getTable(Node q) {
        BTable bt = table.get(q.getLabel());
        if (bt == null) {
            bt = new BTable();
            table.put(q.getLabel(), bt);
        }
        return bt;
    }

    Boolean get(Node q, Node t) {
        BTable bt = getTable(q);
        return bt.get(t.getLabel());
    }

    void put(Node q, Node t, Boolean b) {
        BTable bt = getTable(q);
        bt.put(t.getLabel(), b);
    }
}

