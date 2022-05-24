package fr.inria.corese.core.load.result;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class SPARQLJSONResult extends SPARQLResult {
    
    private JSONObject json;
    VTable vtable;
    private List<String> varList;
    
    
    public SPARQLJSONResult() {
        this(Graph.create());
    }
    
    public SPARQLJSONResult(Graph g) {
        super(g);
        vtable = new VTable();
        varList = new ArrayList<>();
    }
    
    public static SPARQLJSONResult create() {
        return new SPARQLJSONResult();
    }
    
    
    @Override
    public Mappings parse(String path) throws IOException {
        try {
            String str = QueryLoad.create().readWE(path);
            return parseString(str);
        } catch (LoadException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    @Override
    public Mappings parseString(String str) {
        setJson(new JSONObject(str));
        header();
        link();
        Mappings map = body();
        complete(map);
        return map;
    }
    
    void header() {
        if (json.getJSONObject("head").has("vars")) {
            JSONArray vars = json.getJSONObject("head").getJSONArray("vars");
            for (int i = 0; i < vars.length(); i++) {
                String var = vars.getString(i);
                defineVariable(getVariable(var));
                getVarList().add(var);
            }
        }
    }
    
    void link() {
        if (json.getJSONObject("head").has("link")) {
            JSONArray link = json.getJSONObject("head").getJSONArray("link");
            for (int i = 0; i < link.length(); i++) {
                String url = link.getString(i);
                addLink(url);
            }
        }
    }
    
    Mappings body() {
        Mappings map = new Mappings();
        if (json.has("results")) {
            JSONArray results = json.getJSONObject("results").getJSONArray("bindings");

            for (int i = 0; i < results.length(); i++) {
                Mapping m = processResult(results.getJSONObject(i));
                if (m.getNodes().length > 0) {
                    map.add(m);
                }
            }
        }
        map.setLinkList(getLink());
        return map;
    }
    
    Mapping processResult(JSONObject result) {
        ArrayList<Node> varList = new ArrayList<>();
        ArrayList<Node> valList = new ArrayList<>();

        for (String var : getVarList()) {
            if (result.has(var)) {
                JSONObject bind = result.getJSONObject(var);
                Node val = process(bind);
                if (val != null) {
                    varList.add(getVariable(var));
                    valList.add(process(bind));
                }
            }
        }
        Mapping map = Mapping.create(varList, valList);
        return map;
    }
    
    Node process(JSONObject bind) {
        String type = bind.getString("type");
        switch (type) {
            case "uri":
                return getURI(bind.getString("value"));
            case "literal":
                return getLiteral(bind.getString("value"), getString(bind, "datatype"), getString(bind, "xml:lang"));
            case "typed-literal":
                return getLiteral(bind.getString("value"), getString(bind, "datatype"), null);
            case "bnode":
                return getBlank(bind.getString("value"));
            case "triple":
                return getTriple(bind.getJSONObject("value"));
            case "list":
                return getList(bind.getJSONArray("value"));    
        }
        return null;
    }
    
    Node getList(JSONArray list) {
        IDatatype dt = DatatypeMap.newList();
        for (int i = 0; i<list.length(); i++) {
            Node n = process(list.getJSONObject(i));
            dt.getList().add(n.getDatatypeValue());
        }
        return NodeImpl.create(dt);
    }
    
    Node getTriple(JSONObject triple) {
        Node subject    = process(triple.getJSONObject("subject"));
        Node predicate  = process(triple.getJSONObject("predicate"));
        Node object     = process(triple.getJSONObject("object"));
        Node reference  = edge(subject, predicate, object);
        return reference;
    }
    
    String getString(JSONObject json, String name) {
        if (json.has(name)) {
            return json.getString(name);
        }
        return null;
    }
    
    Node getVariable(String var) {
        return getCompiler().createNode(vtable.get(var));
    }
    
    
    
    
    
    

    
    public JSONObject getJson() {
        return json;
    }

    
    public void setJson(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the varList
     */
    public List<String> getVarList() {
        return varList;
    }

    /**
     * @param varList the varList to set
     */
    public void setVarList(List<String> varList) {
        this.varList = varList;
    }

   
}
