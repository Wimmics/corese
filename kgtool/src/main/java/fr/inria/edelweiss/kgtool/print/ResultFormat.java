package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Metadata;
import java.io.FileWriter;
import java.io.IOException;

import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.util.MappingsGraph;
import java.util.HashMap;

/**
 * Select Result format according to query form and @display annotation.,
 * Olivier Corby Edelweiss INRIA 2011 - Wimmics 2016
 */
public class ResultFormat {

    static final int UNDEF_FORMAT = -1;
    
    static final int RDF_XML_FORMAT = 1;
    static final int TURTLE_FORMAT  = 2;
    static final int JSON_LD_FORMAT = 3;
    
    static final int XML_FORMAT     = 11;
    static final int RDF_FORMAT     = 12;
    static final int JSON_FORMAT    = 13;
    
    static int DEFAULT_SELECT_FORMAT = XML_FORMAT;
    static int DEFAULT_CONSTRUCT_FORMAT = RDF_XML_FORMAT;
    
    Mappings map;
    
    static HashMap<String, Integer> table;
    
    static {
        init();
    }
    
    static void init(){
        table = new HashMap();
        table.put(Metadata.DISPLAY_TURTLE, TURTLE_FORMAT);
        table.put(Metadata.DISPLAY_RDF_XML, RDF_XML_FORMAT);
        table.put(Metadata.DISPLAY_JSON_LD, JSON_LD_FORMAT);
        
        table.put(Metadata.DISPLAY_RDF, RDF_FORMAT);
        table.put(Metadata.DISPLAY_XML, XML_FORMAT);
        table.put(Metadata.DISPLAY_JSON, JSON_FORMAT);
    }

    ResultFormat(Mappings m) {
        map = m;
    }

    static public ResultFormat create(Mappings m) {
        return new ResultFormat(m);
    }

    public static void setDefaultSelectFormat(int i) {
        DEFAULT_SELECT_FORMAT = i;
    }

    public static void setDefaultConstructFormat(int i) {
        DEFAULT_CONSTRUCT_FORMAT = i;
    }

    @Override
    public String toString() {
        Query q = map.getQuery();
        if (q == null) {
            return "";
        }
        
        ASTQuery ast = (ASTQuery) q.getAST();

        if (q.isTemplate()
                || (q.hasPragma(Pragma.TEMPLATE) && map.getGraph() != null)) {
            return TemplateFormat.create(map).toString();
        } else {
            Integer type = null;
            if (ast != null && ast.hasMetadata(Metadata.DISPLAY)) {
                String val = ast.getMetadata().getValue(Metadata.DISPLAY);
                type = table.get(val);
            }

            if (type == null) {
                if (q.isConstruct()) {
                    type = DEFAULT_CONSTRUCT_FORMAT;
                } 
                else {
                    type = DEFAULT_SELECT_FORMAT;               
                }
            }
            
            return process(map, type);
        }
    }
    
    

    String process(Mappings map, int type) {
        switch (type) {
            case RDF_XML_FORMAT: return RDFFormat.create(map).toString();
            case TURTLE_FORMAT:  return TripleFormat.create(map).toString();
            case JSON_LD_FORMAT: return JSONLDFormat.create(map).toString();

            case XML_FORMAT: return XMLFormat.create(map).toString();
            case RDF_FORMAT: 
                Graph g = MappingsGraph.create(map).getGraph();
                return TripleFormat.create(g).toString();
            case JSON_FORMAT: return JSONFormat.create(map).toString();
        }
        return null;
    }

    public void write(String name) throws IOException {
        FileWriter fw = new FileWriter(name);
        String str = toString();
        fw.write(str);
        fw.flush();
        fw.close();
    }
}
