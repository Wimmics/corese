package fr.inria.corese.sparql.triple.parser.context;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service logger Used by corese core ProviderService and Service Stored in
 * Binding and federated query AST
 * Designed to be serialized as RDF.
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2021
 */
public class ContextLog implements URLParam, LogKey {

    public static int DISPLAY_RESULT_MAX=10;
    
    static String[] shareable = {LogKey.INDEX};

    // log service exception list
    private List<EngineException> exceptionList;
    // log service SPARQL Results XML format link href=url
    private List<String> linkList;
    // labels such as header name
    private List<String> labelList;
    // federated visitor endpoint selector Mappings
    private Mappings selectMap;
    private Mappings indexMap;
    // subject -> property map
    // subject = service URL + service call counter
    private SubjectMap subjectMap;
    // federated query
    private ASTQuery ast;
    // federated visitor endpoint selector Mappings
    private ASTQuery astSelect;
    private ASTQuery astIndex;
    private StringBuilder trace; 
    private List<String> formatList;

    public ContextLog() {
        init();
    }

    void init() {
        exceptionList = new ArrayList<>();
        linkList = new ArrayList<>();
        setLabelList(new ArrayList<>());
        formatList = new ArrayList<>();
        setSubjectMap(new SubjectMap());
    }
    
    synchronized ContextLog getSynchonizedLog() {
        return this;
    }

    String getSubject() {
        return SUBJECT;
    }
    
    public PropertyMap getPropertyMap() {
        return getSubjectMap().getPropertyMap(getSubject());
    }

    public PropertyMap getPropertyMap(String subject) {
        return getSubjectMap().getPropertyMap(subject);
    }

    public IDatatype get(String subject, String property) {
        return getSubjectMap().get(subject, property);
    }
    
    public IDatatype getLabel(String subject, String property) {
        return getSubjectMap().get(subject, getPredicate(property));
    }
    
    public List<List<String>> getLabelList(String property) {
        ArrayList<List<String>> list = new ArrayList<>();
        
        for (String url : keySet()) {
            IDatatype dt = getLabel(url, property);
            if (dt != null) {
                list.add(List.of(url, property, dt.getLabel()));
            }
        }
        
        return list;
    }
    
    public String getString(String subject, String property) {
        IDatatype dt = get(subject, property);
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }
    
    public List<String> getStringList(String subject, String property) {
        return getSubjectMap().getStringList(subject, property);
    }
    
    public List<String> getStringList(String property) {
        return getSubjectMap().getStringList(getSubject(), property);
    }
    
     public String getStringElement(String property, int n) {
        List<String> list = getSubjectMap().getStringList(getSubject(), property);
        if (n < list.size()) {
            return list.get(n);
        }
        return null;
    }
    
    
    public List<String> getStringListDistinct(String property) {
        return distinct(getSubjectMap().getStringList(getSubject(), property));
    }
    
    public List<String> distinct(List<String> list) {
        ArrayList<String> alist = new ArrayList<>();
        for (String str : list) {
            if (! alist.contains(str)) {
                alist.add(str);
            }
        }
        return alist;
    }
    
    public IDatatype get(String property) {
        return getSubjectMap().get(getSubject(), property);
    }
    
    public Mappings getMappings(String property) {
        return getMappings(getSubject(), property);
    } 
    
    public String getMessage() {       
        for (int i = getLinkList().size()-1; i>=0; i--) {
            String url = getLinkList().get(i);
            if (url.contains(MES)) {
                return url;
            }
        }
        return null;
    }
    
    public Mappings getMappings(String subject, String property) {
        IDatatype dt = getSubjectMap().get(subject, property);
        if (dt == null) {
            return null;
        }
        if (dt.getPointerObject() instanceof Mappings) {
            return dt.getPointerObject().getMappings();
        }
        return null;
    }

    public ASTQuery getAST(String property) {
        return getAST(getSubject(), property);
    }

    public ASTQuery getAST(String subject, String property) {
        IDatatype dt = getSubjectMap().get(subject, property);
        if (dt == null) {
            return null;
        }
        if (dt.getPointerObject() instanceof ASTQuery) {
            return (ASTQuery) dt.getPointerObject();
        }
        return null;
    }
    
    public String getString(String property) {
        return getString(getSubject(), property);
    }

    public void set(String property, String value) {
        getSubjectMap().set(getSubject(), property, value);
    }

    public void set(String property, int value) {
        getSubjectMap().set(getSubject(), property, value);
    }

    public void incr(String subject, String property, int value) {
        getSubjectMap().incr(subject, property, value);
    }
    
    public String getPredicate(String name) {
        if (name.startsWith(LogKey.PREF)) {
            return name;
        }
        return LogKey.HEADER_PREF.concat(name);
    }

    public void set(String subject, String property, String value) {
        getSubjectMap().set(subject, property, value);
    }
    
    public void setLabel(String subject, String property, String value) {
        getSubjectMap().set(subject, getPredicate(property), value);
    }
    
    public void defLabel(String subject, String property, String value) {
        getSubjectMap().set(subject, getPredicate(property), value);
        defLabel(property);
    }
    
    public void defLabel(String property) {
        if (!getLabelList().contains(property)) {
            getLabelList().add(property);
        }
    }

    public void set(String subject, String property, int value) {
        getSubjectMap().set(subject, property, value);
    }
    
    public void set(String subject, String property, double value) {
        getSubjectMap().set(subject, property, value);
    }
    
    public void set(String subject, String property, IDatatype dt) {
        getSubjectMap().set(subject, property, dt);
    }

    public void set(String subject, String property, Object value) {
        getSubjectMap().set(subject, property, value);
    }
    
    public void set(String property, Object value) {
        getSubjectMap().set(getSubject(), property, value);
    }
    
    public boolean hasValue(String subject, String property) {
        IDatatype dt = getSubjectMap().get(subject, property);
        return dt != null;
    }

    public void add(String property, String value) {
        getSubjectMap().add(getSubject(), property, value);
    }
    
    public void add(String property, IDatatype value) {
        getSubjectMap().add(getSubject(), property, value);
    }
    
    public void addDistinct(String property, IDatatype value) {
        addDistinct(getSubject(), property, value);
    }

    // add value to list of value
    public void add(String subject, String property, String value) {
        getSubjectMap().add(subject, property, value);
    }
    
    public void add(String subject, String property, IDatatype value) {
        getSubjectMap().add(subject, property, value);
    }
    
    public void addDistinct(String subject, String property, IDatatype value) {
        getSubjectMap().addDistinct(subject, property, value);
    }

    public void add(String subject, String property, Object value) {
        getSubjectMap().add(subject, property, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Link List: %s\n", getLinkList().toString()));
        sb.append(getSubjectMap());
        if (getASTIndex()!= null) {
            sb.append("Source discovery\n");
            sb.append(getASTIndex()).append(NL);
        }
        if (getASTSelect() != null) {
            sb.append("Source selection\n");
            sb.append(getASTSelect()).append(NL);
        }
        return sb.toString();
    }
    
    // subset of Log
    // subset of header specified by Property SERVICE_HEADER
    public String log() {
        return log(getLabelList());
    }
    
    public String log(List<String> propertyList) {
        StringBuilder sb = new StringBuilder();

        for (String url : getSubjectMap().getKeys()) {
            for (String name : propertyList) {
                if (name.equals(STAR)) {
                    for (String pred : getLabelList()) {
                        IDatatype value = getLabel(url, pred);
                        if (value != null) {
                            sb.append(String.format("%s %s %s\n", url, pred, value));
                        }
                    }
                } 
                else {
                    IDatatype value = getLabel(url, name);
                    if (value != null) {
                        sb.append(String.format("%s %s %s\n", url, name, value));
                    }
                }
            }
        }

        return sb.toString();
    }
    
    public void logToFile(String fileName) throws IOException {
        FileWriter file = new FileWriter(fileName);
        file.write(log());
        file.flush();
        file.close();
    }

    public boolean isEmpty() {
        return getSubjectMap().isEmpty();
    }

    public List<EngineException> getExceptionList() {
        return exceptionList;
    }

    public void setExceptionList(List<EngineException> exceptionList) {
        this.exceptionList = exceptionList;
    }

    public void addException(EngineException e) {
        getExceptionList().add(e);
    }
       
    public String getLink() {
        if (getLinkList().isEmpty()) {
            return null;
        }
        return getLinkList().get(0);
    }

    public List<String> getLinkList() {
        return linkList;
    }

    public void setLink(List<String> linkList) {
        this.linkList = linkList;
    }

    public void addLink(String url) {
        getLinkList().add(url);
    }
    
    public void addLink(List<String> linkList) {
        getLinkList().addAll(linkList);
    }

    void addURLInput(URLServer url, Mappings map) {
        incr(url.getLogURLNumber(), INPUT_CARD, mapSize(map));
        if (map != null && !map.isEmpty()) {
            map.setDisplay(url.intValue(NBINPUT, DISPLAY_RESULT_MAX));
            set(url.getLogURLNumber(), LogKey.INPUT, map);
        }
    }

    void addURLOutput(URLServer url, Mappings map) {
        incr(url.getLogURLNumber(), OUTPUT_CARD, mapSize(map));

        if (map!=null && !map.isEmpty()) {
            if (!url.hasParameter(URLParam.RESULT) || url.getLogURLNumber().contains(url.getParameter(URLParam.RESULT))) {
                // we may filter the endpoint for which we ask results  
                map.setDisplay(url.intValue(NBRESULT, DISPLAY_RESULT_MAX));
                set(url.getLogURLNumber(), LogKey.RESULT, map);
            }
        }
    }

    public void traceInput(URLServer serv, Mappings map) {
        addURLInput(serv, map);
    }

    public void traceOutput(URLServer serv, Mappings map, int nbcall, double time) {
        addURLOutput(serv, map);

        set(serv.getLogURLNumber(), NBCALL, nbcall);
        set(serv.getLogURLNumber(), INPUT_SIZE, map.getQueryLength());
        set(serv.getLogURLNumber(), OUTPUT_SIZE, map.getLength());
        set(serv.getLogURLNumber(), LogKey.TIME, time);
    }
    
    /**
     * Record first occurrence of service ast query
     */
    public void traceAST(URLServer serv, ASTQuery ast) {
        if (! hasValue(serv.getLogURLNumber(), LogKey.AST_SERVICE)) {
            set(serv.getLogURLNumber(), LogKey.AST_SERVICE, ast);
        }
    }
    
    public void traceResult(URLServer serv, String result) {
        if (serv.hasAnyParameter(URLParam.RESULT_TEXT, DETAIL) && result!=null) {
            set(serv.getLogURLNumber(), LogKey.RESULT_TEXT, 
                    result.substring(0, Math.min(result.length(), serv.intValue(URLParam.RESULT_TEXT, 1000))));
        }
    }


    int mapSize(Mappings map) {
        return (map == null) ? 0 : map.size();
    }

    // share data from federated visitor log
    public void share(ContextLog log) {
        if (log == null) {
            return;
        }
        if (getAST() == null) {
            setAST(log.getAST());
        }
        if (getASTSelect() == null) {
            setASTSelect(log.getASTSelect());
        }
        if (getSelectMap() == null) {
            setSelectMap(log.getSelectMap());
        }
        if (getASTIndex() == null) {
            setASTIndex(log.getASTIndex());
        }
        if (getIndexMap() == null) {
            setIndexMap(log.getIndexMap());
        }
        if (getExceptionList().isEmpty()) {
            getExceptionList().addAll(log.getExceptionList());
        }
        
        for (String prop : getShareable())  {
            IDatatype dt = log.get(prop);
            if (dt !=null) {
                set(prop, dt);
            }
        }
    }
    
    
    String[] getShareable() {
        return shareable;
    }
    
    /*****************************
     * 
     * JSON message
     * 
     *****************************/
    
    
    
    /**
     * Copy data from ContextLog into json message
     * Copy endpoint exceptions
     */
    public JSONObject message() {
        return message(new JSONObject());
    }

    public JSONObject message(JSONObject json) {
        messageHeader(json);
        if (getAST()!=null) {
            json.put(REW, getAST().toString());
        }
        // show last service that fail        
        messageFail(json);
        // missing triple in source selection 
        messageSourceSelection(json);
        return json;
    }
    
    void pretty(JSONObject json) {
        for (String key : json.keySet()) {
            System.out.println(key + " = " + json.get(key));
        }
    }
    
    void messageHeader(JSONObject json) {
        // list of distinct endpoint call
        List<String> list = getStringListDistinct(LogKey.ENDPOINT);
        if (!list.isEmpty()) {
            JSONArray arr = new JSONArray(list);
            json.put(URLParam.ENDPOINT, arr);
        }
    }

    /**
     * federated query fail
     * show last service that fail
     */
    void messageFail(JSONObject json) {
        Mappings map = getMappings(LogKey.SERVICE_OUTPUT);
        
        if (map != null && map.isEmpty()) {
            List<String> alist = getStringList(LogKey.ENDPOINT_CALL);
            
            if (!alist.isEmpty()) {
                String serv = alist.get(alist.size() - 1);
                ASTQuery ast = getAST(serv, LogKey.AST_SERVICE);
                Mappings res = getMappings(serv, LogKey.OUTPUT);
                
                if (ast != null && (res == null || res.isEmpty())) {
                    JSONObject obj = new JSONObject();
                    obj.put(URLParam.URL, URLServer.clean(serv));
                    obj.put(URLParam.QUERY, ast);
                    json.put(URLParam.FAIL, obj);
                }
            }
        }
    }
    
    /**
     * Check source selection in federated query
     * Find undefined triples in federation
     */
    void messageSourceSelection(JSONObject json) {
        ASTQuery ast = getASTSelect();
        Mappings map = getSelectMap();
        if (ast == null || map == null) {
            return;
        }
        
        ArrayList<Expression> list = ast.getUndefinedTriple(map);
        
        if (!list.isEmpty()) {
            JSONArray arr = new JSONArray();
            
            for (Expression exp : list) {
                arr.put(exp);
            }
            json.put(URLParam.UNDEF, arr);
        }
    }
    
    
    
    
    
    
    
    
    

    public ASTQuery getAST() {
        return ast;
    }

    public void setAST(ASTQuery ast) {
        this.ast = ast;
    }

    public ASTQuery getASTSelect() {
        return astSelect;
    }

    public void setASTSelect(ASTQuery astSelect) {
        this.astSelect = astSelect;
    }

    public Mappings getSelectMap() {
        return selectMap;
    }

    public void setSelectMap(Mappings selectMap) {
        this.selectMap = selectMap;
    }

    public SubjectMap getSubjectMap() {
        return subjectMap;
    }
    
    public Collection<String> keySet() {
        return getSubjectMap().keySet();
    }

    public void setSubjectMap(SubjectMap subjectMap) {
        this.subjectMap = subjectMap;
    }

    public StringBuilder getTrace() {
        return trace;
    }
    
    public StringBuilder getCreateTrace() {
       if (getTrace() == null) {
           setTrace(new StringBuilder());
       }
       return getTrace();
    }

    public void setTrace(StringBuilder trace) {
        this.trace = trace;
    }
    
    public Mappings getLastInputMappings() {
        String last = getLast(ENDPOINT_CALL);
        if (last != null) {
            IDatatype dt = get(last, LogKey.INPUT);
            if (dt != null) {
                return dt.getPointerObject().getMappings();
            }
        }
        return null;
    }

    public String getLast(String name) {
        IDatatype list = get(name);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(list.size()-1).getLabel();
    }   

    public List<String> getFormatList() {
        return formatList;
    }

    public void setFormatList(List<String> formatList) {
        this.formatList = formatList;
    }

    public ASTQuery getASTIndex() {
        return astIndex;
    }

    public void setASTIndex(ASTQuery astIndex) {
        this.astIndex = astIndex;
    }

    public Mappings getIndexMap() {
        return indexMap;
    }

    public void setIndexMap(Mappings indexMap) {
        this.indexMap = indexMap;
    }

    public List<String> getLabelList() {
        return labelList;
    }

    public void setLabelList(List<String> labelList) {
        this.labelList = labelList;
    }

}
