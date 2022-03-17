package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.logic.RDF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Index templates by type of focus Used by QueryEngine, PPrinter
 *
 * sql:Query -> ( template {} where { ?in a sql:Query } ) sql:Plus -> ( template
 * {} where { ?in a ?class } values ?class { sql:Plus }
 *
 * Templates with no rdf:type stored in a list
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
class TemplateIndex extends HashMap<String, List<Query>> {

    // templates not indexed because they have no ?in rdf:type
    List<Query> list = new ArrayList<Query>();

    List<Query> getTemplates(String type) {
        if (type == null) {
            return list;
        }
        List<Query> l = get(type);
        if (l != null) {
            return l;
        }
        return list;
    }

    /**
     * Index Query at load time search occurrence of: ?in rdf:type sql:Select
     * ?in rdf:type ?class values ?class { ... }
     */
    void add(Query q) {

        boolean suc = add(q, q.getBody());

        if (!suc) {
            list.add(q);
        }
    }

    boolean add(Query q, Exp body) {
        boolean suc = false;
        for (Exp exp : body) {

            if (exp.isUnion()) {
                for (Exp ee : exp) {
                    suc = add(q, ee) || suc;
                }
            } else if (exp.isQuery()) {
                suc = add(q, exp.getQuery().getBody());
            } else if (exp.isEdge()) {
                Edge edge = exp.getEdge();

                if (isType(edge)) {
                    // ?in rdf:type xxx
                    Node type = edge.getNode(1);

                    if (type.isConstant()) {
                        // ?in rdf:type sql:Select
                        IDatatype dt =  type.getValue();
                        add(dt.getLabel(), q);
                        suc = true;
                    } 
                    else {
                        // ?in rdf:type ?class
                        Mappings map = anyMappings(q, body);
                       if (map != null) {
                            // ?in rdf:type ?class . values ?class { ... }
                            for (Mapping m : map) {
                                Node node = m.getNode(type);
                                if (node != null) {
                                    IDatatype dt =  node.getValue();
                                    add(dt.getLabel(), q);
                                    suc = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return suc;
    }

    Mappings anyMappings(Query q, Exp exp) {
        Mappings map = q.getActualMappings();
        if (map != null) {
            return map;
        }
        return anyMappings(exp);
    }

    Mappings anyMappings(Exp exp) {
        for (Exp ee : exp) {
            if (ee.type() == Exp.VALUES) {
                return ee.getMappings();
            }
        }
        return null;
    }

    void add(String type, Query q) {
        List<Query> list = get(type);
        if (list == null) {
            list = new ArrayList<Query>();
            put(type, list);
        }
        list.add(q);
    }

    /**
     * ?in rdf:type xxx
     */
    boolean isType(Edge edge) {
        return edge.getEdgeLabel().equals(RDF.TYPE)
                && edge.getNode(0).getLabel().equals(ASTQuery.IN);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String dt : keySet()) {
            List<Query> l = get(dt);
            sb.append(dt);
            sb.append(System.getProperty("line.separator"));
            sb.append(l);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    /**
     * Sort templates according to priority
     */
    void sort() {
        sort(list);
        for (List<Query> l : values()) {
            sort(l);
        }
    }

    void sort(List<Query> list) {
        Collections.sort(list, new Comparator<Query>() {
            public int compare(Query q1, Query q2) {
                int p1 = getLevel(q1);
                int p2 = getLevel(q2);
                return compare(p1, p2);
            }

            int compare(int x, int y) {
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });
    }

    int getLevel(Query q) {
        ASTQuery ast =  q.getAST();
        return ast.getPriority();
    }
}