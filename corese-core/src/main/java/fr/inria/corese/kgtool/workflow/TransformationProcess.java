package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.corese.kgraph.core.Event;
import fr.inria.corese.kgtool.transform.Transformer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TransformationProcess extends  WorkflowProcess {

    private boolean isDefault = false;
    private Transformer transfomer;
    
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
        if (isDefault && data.getMappings() != null && data.getMappings().getQuery().isTemplate()){
            // former SPARQLProcess is a template {} where {}
            // this Transformer is default transformer : return former template result
            return data;
        }
        Transformer t = Transformer.create(data.getGraph(), getPath());
        setTransfomer(t);
        init(t, data, getContext());
        Data res = new Data(data.getGraph());
        IDatatype dt = t.process();
        if (dt != null){
            res.setTemplateResult(dt.getLabel());
            res.setDatatypeValue(dt);
        }
        res.setProcess(this);       
        complete(t, res);
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
        if (data.getVisitor() != null){
            t.setVisitor(data.getVisitor());
        }               
    }
    
    void complete(Transformer t, Data data){
        if (t.getVisitor() != null){
            data.setVisitor(t.getVisitor());
        }
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
   
}
