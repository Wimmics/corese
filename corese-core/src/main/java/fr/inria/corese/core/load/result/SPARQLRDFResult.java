package fr.inria.corese.core.load.result;

import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 *
 * @author corby
 */
public class SPARQLRDFResult {
    
    public Mappings parse(String result) throws LoadException {
        return parse(result, Load.TURTLE_FORMAT);
    }
    
    public Mappings parse(String result, int format) throws LoadException {
        String query
                = "prefix rs: <http://www.w3.org/2001/sw/DataAccess/tests/result-set#>"
                + "select ?var ?val where { "
                + "{ ?r rs:solution ?s "
                + "optional {?s rs:index ?i }"
                + "optional { "
                + "?s rs:binding [  rs:variable ?var ; rs:value ?val ] } "
                + "} "
                + "union {?r rs:boolean true}"
                + "}"
                + "order by ?i  "
                + "group by ?s  ";

        Graph g = Graph.create();
        Load load = Load.create(g);
        load.loadString(result, format);

        QueryProcess exec = QueryProcess.create(g);
        exec.setListGroup(true);
        Mappings map = null;
        try {
            map = exec.query(query);
        } catch (EngineException e) {
            System.out.println(e.getMessage());
        }
        Mappings res = translate(map);
        return res;

    }

    /**
     * W3C map to KGRAM map
     */
    Mappings translate(Mappings ms) {
        Mappings res = new Mappings();
        for (Mapping map : ms) {
            Mapping m = translate(map);
            res.add(m);
        }
        return res;
    }

    Mapping translate(Mapping m) {
        if (m.getMappings() == null) {
            return m;
        }

        Mapping res = Mapping.create();

        for (Mapping map : m.getMappings()) {
            Node var = map.getNode("?var");
            Node val = map.getNode("?val");
            if (var != null && val != null) {
                NodeImpl q = NodeImpl.createVariable("?" + var.getLabel());
                res.addNode(q, val);
            }
        }

        return res;
    }
    
}
