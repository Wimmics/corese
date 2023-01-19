package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.URLParam;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TransformationProcess extends  WorkflowProcess {

    private boolean isDefault = false;
    private boolean template = false;
    private Transformer transfomer;
    static final String TEMPLATE_RESULT = "?templateResult";
    
    public TransformationProcess(String p){
        setPath(p);
    }
    
     public TransformationProcess(String p, boolean b){
        this(p);
        isDefault = b;
    }
    
    @Override
    public boolean isTransformation() {
        return true;
    }
      
    @Override
     void start(Data data){
        if (isDebug()){
            System.out.println("Transformer: " + getPath());
        }  
        data.getEventManager().start(Event.WorkflowTransformation, getPath());
        // focus this event only
        data.getEventManager().show(Event.WorkflowTransformation);
     }
     
     @Override
     void finish(Data data){
         collect(data);
         data.getEventManager().finish(Event.WorkflowTransformation, getPath());
         data.getEventManager().show(Event.WorkflowTransformation, false);
     }  
     
    @Override
    public Data run(Data data) throws EngineException {      
        if (data.getMappings() != null && data.getMappings().getQuery().isTemplate()) {
            if (isDefault) {
                // former SPARQLProcess is a template {} where {}
                // this Transformer is default transformer : return former template result
                return data;
            }
            else if (data.getMappings().getTemplateResult()!=null) {
                setTemplate(true);
            }
        }
        Transformer t = Transformer.create(data.getGraph(), getPath());
        t.setDebug(isDebug());
        if (getContext().hasValue(Context.STL_MODE, URLParam.DEBUG)) {
            t.setDebug(true);
        }
        if (isDebug()) {
            System.out.println("Transformer graph size: " + data.getGraph().size());
        }
        setTransfomer(t);
        init(t, data, getContext());
        Data res = new Data(data.getGraph());
        if (isTemplate()) {
            // set result of previous template query into ldscript global variable ?templateResult
            // use case: in Workflow, transformation st:web return ?templateResult as result 
            // when this variable is bound
            data.getBinding().setGlobalVariable(TEMPLATE_RESULT, 
                    data.getMappings().getTemplateResult().getDatatypeValue());
        }
        IDatatype dt = t.process(data.getBinding());
        if (dt != null){
            res.setTemplateResult(dt.getLabel());
            res.setDatatypeValue(dt);
        }
        res.setProcess(this);
        res.setBinding(t.getBinding());
        complete(t, data, res);
        return res;
    }
    
    @Override
    public String stringValue(Data data){
        return data.getTemplateResult();
    }
    
    void init(Transformer t, Data data, Context c) {
        if (c != null){
            t.setContext(c);
        }
        if (data.getMappings() != null) {
            t.getContext().set(Context.STL_MAPPINGS, DatatypeMap.createObject(data.getMappings()));
        }
//        if (data.getVisitor() != null){
//            t.setVisitor(data.getVisitor());
//        }               
    }
    
    void complete(Transformer t, Data data, Data res){
//        if (t.getVisitor() != null){
//            res.setVisitor(t.getVisitor());
//        }
    }

    /**
     * @return the transfomer
     */
    public Transformer getTransfomer() {
        return transfomer;
    }

    /**
     * @param transfomer the transfomer to set
     */
    public void setTransfomer(Transformer transfomer) {
        this.transfomer = transfomer;
    }
    
    @Override
    public String getTransformation(){
        return getPath();
    } 

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }
   
}
