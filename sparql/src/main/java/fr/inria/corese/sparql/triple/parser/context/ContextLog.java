package fr.inria.corese.sparql.triple.parser.context;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.DatatypeMap.createObject;
import static fr.inria.corese.sparql.datatype.DatatypeMap.newInstance;
import static fr.inria.corese.sparql.datatype.DatatypeMap.newResource;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.ArrayList;
import java.util.List;

/**
 * Service logger Used by corese core ProviderService and Service Stored in
 * Binding and federated query AST.
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2021
 */
public class ContextLog implements URLParam, LogKey {

    // log service exception list
    private List<EngineException> exceptionList;
    // log service SPARQL Results XML format link href=url
    private List<String> linkList;
    // federated visitor endpoint selector Mappings
    private Mappings selectMap;
    // subject -> property map
    private SubjectMap subjectMap;
    // federated query
    private ASTQuery ast;
    // federated visitor endpoint selector Mappings
    private ASTQuery astSelect;

    public ContextLog() {
        init();
    }

    void init() {
        exceptionList = new ArrayList<>();
        linkList = new ArrayList<>();
        setSubjectMap(new SubjectMap());
    }

    String getSubject() {
        return SUBJECT;
    }

    public PropertyMap getPropertyMap(String subject) {
        return getSubjectMap().getPropertyMap(subject);
    }

    public IDatatype get(String subject, String property) {
        return getSubjectMap().get(subject, property);
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

    public void set(String subject, String property, String value) {
        getSubjectMap().set(subject, property, value);
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

    public void add(String property, String value) {
        getSubjectMap().add(getSubject(), property, value);
    }

    // add value to list of value
    public void add(String subject, String property, String value) {
        getSubjectMap().add(subject, property, value);
    }

    public void add(String subject, String property, Object value) {
        getSubjectMap().add(subject, property, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Link List: %s\n", getLinkList().toString()));
        sb.append(getSubjectMap());
        return sb.toString();
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
        if (url.hasAnyParameter(NBINPUT, DETAIL) && map != null && !map.isEmpty()) {
            map.setDisplay(url.intValue(NBINPUT, 5));
            set(url.getLogURLNumber(), LogKey.INPUT, map);
        }
    }

    void addURLOutput(URLServer url, Mappings map) {
        incr(url.getLogURLNumber(), OUTPUT_CARD, mapSize(map));

        if (url.hasAnyParameter(NBRESULT, DETAIL) && map!=null && !map.isEmpty()) {
            if (!url.hasParameter(URLParam.RESULT) || url.getLogURLNumber().contains(url.getParameter(URLParam.RESULT))) {
                // we may filter the endpoint for which we ask results  
                map.setDisplay(url.intValue(NBRESULT, 5));
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
    
    public void traceAST(URLServer serv, ASTQuery ast) {
        set(serv.getLogURLNumber(), LogKey.AST_SERVICE, ast);
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
        if (getAST() == null) {
            setAST(log.getAST());
        }
        if (getASTSelect() == null) {
            setASTSelect(log.getASTSelect());
        }
        if (getSelectMap() == null) {
            setSelectMap(log.getSelectMap());
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

    public void setSubjectMap(SubjectMap subjectMap) {
        this.subjectMap = subjectMap;
    }
}
