package fr.inria.corese.kgtool.print;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Graphable;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.Serializer;

/**
 * Generate RDF Result Format for Mappings (bindings in RDF)
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class RDFResultFormat implements Graphable {
    static final String RDFRESULT = NSManager.RDFRESULT;
    Mappings map;
    
    RDFResultFormat(Mappings m){
        map = m;
    }
    
    public static RDFResultFormat create(Mappings m){
        return new RDFResultFormat(m);
    }
    
    @Override
    public  String toString(){
        Serializer sb = process();
        return sb.toString();
    }
    
    Serializer process(){
        Serializer sb = new Serializer();
        sb.appendNL("@prefix rs: <" + RDFRESULT + "> .");
        sb.nl();
        sb.appendPNL("[] a rs:ResultSet");       
        Query q = map.getQuery();
        ASTQuery ast = (ASTQuery) q.getAST();
        if (ast != null && ast.isAsk()){
            sb.append("rs:boolean ", map.size() > 0);
            sb.appendNL(" .");
        }
        else {
            body(sb);
        }
        return sb;
    }
    
    void body(Serializer sb) {
        Query q = map.getQuery();
        for (Node n : q.getSelect()) {
            sb.append("rs:resultVariable '", getName(n));
            sb.appendPNL("'");
        }
        int i = 0;
        for (Mapping m : map) {
            process(m, sb, i++);
        }
    }
    
    void process(Mapping m, Serializer sb, int i){
        Query q = map.getQuery();        
        sb.appendNL("rs:solution [");
        sb.appendPNL("rs:index ", i);
        for (Node n : q.getSelect()){
            if (m.getNode(n) != null){
                sb.appendNL("rs:binding [");
                sb.append("rs:variable '", getName(n));
                sb.appendPNL("'");
                sb.appendNL("rs:value ",     m.getValue(n));
                sb.appendPNL("]");
            }
        }
        sb.appendPNL("]");
    }
    
    String getName(Node n){
        return n.getLabel().substring(1);
    }

    @Override
    public String toGraph() {
        return toString();    
    }

    @Override
    public void setGraph(Object obj) {
     }

    @Override
    public Object getGraph() {
        return null;    
    }
    
}
