/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class AbstractProcess implements Process {
    private Context context;
    private Dataset dataset;
    private Data data;
    private WorkflowProcess workflow;
    private boolean debug = false;
    
     @Override
    public Data process(Data d) throws EngineException {
        return d;
    }
     
    

    @Override
    public void subscribe(WorkflowProcess w) {
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
    public void setContext(Context context) {
        this.context = context;
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
    public WorkflowProcess getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(WorkflowProcess workflow) {
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
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

}
