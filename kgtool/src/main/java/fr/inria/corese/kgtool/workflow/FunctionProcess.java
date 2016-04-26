/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgraph.query.QueryProcess;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class FunctionProcess extends WorkflowProcess {
    
    private String query;
    
    FunctionProcess(String q, String p){
        query = q;
        path = p;
    }
    
     @Override
    void start(Data data){

     }
    
    @Override
    void finish(Data data){
        
    } 
    
    
    @Override
   public Data run(Data data) throws EngineException{
       IDatatype dt = eval(data, getContext(), getDataset());
       Data res = new Data(data.getGraph(), dt);
       res.setProcess(this);
       return res;  
    }
    
    IDatatype eval(Data data, Context c, Dataset ds) throws EngineException{
        QueryProcess exec = QueryProcess.create(data.getGraph());
        if (path != null){
            exec.setDefaultBase(path);
        }
        if (ds == null && c != null){
            ds = new Dataset();
        }
        if (ds != null){
            ds.setContext(c);
        }
        IDatatype res = exec.eval(getQuery(), ds);  
        return res;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    @Override
    public String stringValue(Data data){
        return data.getDatatypeValue().toString();
    }

}
