package fr.inria.corese.gui.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;
import org.json.JSONObject;

/**
 * Find in the Graph resource and property URL that are similar to those
 * in the query, according to Levenshtein distance
 * use case: @explain
 */
public class DocumentResult {
    static final String NL = System.getProperty("line.separator");
    
    MainFrame frame;
    private Mappings mappings;
    private ASTQuery ast;
    
    DocumentResult(MainFrame frame) {
        this.frame = frame;
    }
    
    
    
    void process(Mappings map) {
        setMappings(map);
        setAst((ASTQuery) map.getQuery().getAST());
        explain();
    }
    
    void explain() {
        int distance = Math.max(1, getAst().getMetadata().intValue(Metadata.EXPLAIN));
        JSONObject json = getGraph().match(getAst(), distance);
        display(json);
    }
    
    void display(JSONObject json) {
        msg(NL);
        for (String key : json.keySet()) {
            //System.out.println(key + " = " + json.get(key));
            msg(key + " -> " + json.get(key)).msg(NL);
        }
    }
    
    DocumentResult msg(String mes) {
        frame.msg(mes);
        return this;
    }
    
    Graph getGraph() {
        return frame.getMyCorese().getGraph();
    }
    
    public Mappings getMappings() {
        return mappings;
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    public ASTQuery getAst() {
        return ast;
    }

    public void setAst(ASTQuery ast) {
        this.ast = ast;
    }
    
}
