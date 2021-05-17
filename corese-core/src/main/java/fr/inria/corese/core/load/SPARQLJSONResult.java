package fr.inria.corese.core.load;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
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
    
    public SPARQLJSONResult(Graph g) {
        super(g);
        vtable = new VTable();
        varList = new ArrayList<>();
    }
    
    
    @Override
    public Mappings parse(String str) {
        setJson(new JSONObject(str));
        header();
        Mappings map = body();
        complete(map);
        return map;
    }
    
    
    
    void header() {
        JSONArray vars = json.getJSONObject("head").getJSONArray("vars");
        for (int i=0; i<vars.length(); i++) {
            String var = vars.getString(i);
            defineVariable(getVariable(var));
            getVarList().add(var);
        }               
    }
    
    void link() {
        JSONArray link = json.getJSONObject("head").getJSONArray("link");
        for (int i=0; i<link.length(); i++) {
            String url = link.getString(i);
            addLink(url);
        }               
    }
    
    Mappings body() {
        Mappings map = new Mappings();
        JSONArray results = json.getJSONObject("results").getJSONArray("bindings");
        
        for (int i=0; i<results.length(); i++) {
            Mapping m = processResult(results.getJSONObject(i));
            if (m.getNodes().length > 0) {
                map.add(m);
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
                Node val = processBind(bind);
                if (val != null) {
                    varList.add(getVariable(var));
                    valList.add(processBind(bind));
                }
            }
        }
        Mapping map = Mapping.create(varList, valList);
        return map;
    }
    
    Node processBind(JSONObject bind) {
        String type = bind.getString("type");
        switch (type) {
            case "uri":
                return getURI(bind.getString("value"));
            case "literal":
                return getLiteral(bind.getString("value"), null, bind.getString("xml:lang"));
            case "typed-literal":
                return getLiteral(bind.getString("value"), bind.getString("datatype"), null);
            case "bnode":
                return getBlank(bind.getString("value"));
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
