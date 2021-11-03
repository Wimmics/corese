package fr.inria.corese.core.print;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Graphable;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Serializer;
import static fr.inria.corese.core.print.RDFFormat.NL;

/**
 * Generate Turtle W3C RDF Format for Mappings (bindings in RDF)
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class RDFResultFormat extends QueryResultFormat
        implements Graphable {
    static final String RDFRESULT = NSManager.RDFRESULT;
    private Mappings map;
    
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
        header(sb);
        sb.appendNL("@prefix rs: <" + RDFRESULT + "> .");
        sb.nl();
        sb.appendPNL("[] a rs:ResultSet");       
        Query q = getMappings().getQuery();
        ASTQuery ast =  q.getAST();
        if (ast != null && ast.isAsk()){
            sb.append("rs:boolean ", getMappings().size() > 0);
            sb.appendNL(" .");
        }
        else {
            body(sb);
        }
        return sb;
    }
    
    void header(Serializer sb) {
        link(sb);
    }
    
    void link(Serializer bb) {
        if (!getMappings().getLinkList().isEmpty()) {
            bb.append("#").append(NL);

            for (String link : getMappings().getLinkList()) {
                bb.append("# link href = ").append(link).append(NL);
            }

            bb.append("#").append(NL);
        }
    }
    
    void body(Serializer sb) {
        Query q = getMappings().getQuery();
        for (Node n : q.getSelect()) {
            sb.append("rs:resultVariable '", getName(n));
            sb.appendPNL("'");
        }
        int i = 0;
        for (Mapping m : getMappings()) {
            process(m, sb, i++);
        }
        sb.close();
    }
    
    void process(Mapping m, Serializer sb, int i){
        Query q = getMappings().getQuery(); 
        if (i>0) {
            sb.appendNL(" ;");
        }
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
        sb.append("]");
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

    public Mappings getMappings() {
        return map;
    }

    public void setMappings(Mappings map) {
        this.map = map;
    }
    
}
