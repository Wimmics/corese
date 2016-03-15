/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.edelweiss.kgtool.transform.Transformer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TemplateProcess extends  AbstractProcess {

    
    private String path;
    private boolean isDefault = false;
    private Transformer transfomer;
    
    public TemplateProcess(String p){
        path = p;
    }
    
     public TemplateProcess(String p, boolean b){
        path = p;
        isDefault = b;
    }
    
    @Override
    public Data process(Data data) throws EngineException {
        if (isDebug()){
            System.out.println("Transformer: " + getPath());
        }
        if (isDefault && data.getMappings() != null && data.getMappings().getQuery().isTemplate()){
            // former SPARQLProcess is a template {} where {}
            // this Transformer is default transformer : return former template result
            return data;
        }
        Transformer t = Transformer.create(data.getGraph(), getPath());
        setTransfomer(t);
        init(t, data, getContext());
        Data res = new Data(data.getGraph());
        res.setTemplateResult(t.toString());
        res.setProcess(this);       
        complete(t, res);
        setData(res);
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
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
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
   
}
