/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;

/**
 * Root class
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class WorkflowProcess implements AbstractProcess {
    private Context context;
    private Dataset dataset;
    private Data data;
    private SemanticWorkflow workflow;
    private boolean debug = false;
    // true means return input graph (use case: select where and return graph as is)
    private boolean probe = false;
    private boolean display = false;
    private String result, uri;
    
    @Override
    public String toString(){
        return getClass().getName();
    }
    
     @Override
    public Data process(Data d) throws EngineException {
        return d;
    }
     
    

    @Override
    public void subscribe(SemanticWorkflow w) {
        setWorkflow(w);
    }

    @Override
    public String stringValue(Data data) {
        return null;
    }
    
    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    
    @Override
    public void inherit(Context context) {
        if (getContext() == null){
           setContext(context); 
        }
        else {
            // inherit exported properties
           getContext().complete(context);
        }
    }

    /**
     * @return the data
     */
    public Data getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Data data) {
        this.data = data;
    }

    /**
     * @return the workflow
     */
    public SemanticWorkflow getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(SemanticWorkflow workflow) {
        this.workflow = workflow;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public void inherit(Dataset dataset) {
        setDataset(dataset);
    }

    @Override
    public boolean isTemplate() {
        return false;
    }

    /**
     * @return the probe
     */
    public boolean isProbe() {
        return probe;
    }

    /**
     * @param probe the probe to set
     */
    @Override
    public void setProbe(boolean probe) {
        this.probe = probe;
    }

    /**
     * @return the display
     */
    @Override
    public boolean isDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    @Override
    public void setDisplay(boolean display) {
        this.display = display;
    }

    @Override
    public void setResult(String r) {
        result = r;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @return the uri
     */
    public String getURI() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    @Override
    public void setURI(String uri) {
        this.uri = uri;
    }

}
