package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.transform.TemplateVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class Data {
    private Dataset dataset;
    private WorkflowProcess process;
    private Mappings map;
    private IDatatype datatype;
    private Graph graph;
    private Context context;
    private TemplateVisitor visitor;
    private String templateResult;
    private List<Data> dataList;
    private String name;
    private boolean success = true;
          
    public Data(Graph g){
        graph = g;
    }
    
    Data(Graph g, IDatatype dt){
        graph = g;
        datatype = dt;
    }
    
    Data(Mappings m, Graph g){
        map = m;
        graph = g;
    }
      
    Data(WorkflowProcess p, Mappings m, Graph g){
        process = p;
        map = m;
        graph = g;
    }
    
    Data(WorkflowProcess p, Graph g){
        process = p;
        graph = g;
    }
    
    Data(WorkflowProcess p, List<Data> l){
        process = p;
        dataList = l;
    }

    
    @Override
    public String toString(){
        return stringValue();
    }
    
    public String stringValue(){
        if (process != null){
            return process.stringValue(this);
        }
        if (getMappings() != null){
            return getMappings().toString();
        }
        if (getGraph() != null){
            return getGraph().toString();
        }
        return getClass().getName();
    }

    /**
     * @return the process
     */
    public WorkflowProcess getProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(WorkflowProcess process) {
        this.process = process;
    }

    /**
     * @return the map
     */
    public Mappings getMappings() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMappings(Mappings map) {
        this.map = map;
    }

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
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
     * @return the visitor
     */
    public TemplateVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(TemplateVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * @return the templateResult
     */
    public String getTemplateResult() {
        return templateResult;
    }

    /**
     * @param templateResult the templateResult to set
     */
    public void setTemplateResult(String templateResult) {
        this.templateResult = templateResult;
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

    
     public IDatatype getDatatypeValue() {
        return datatype;
    }

    /**
     * @param datatype the datatype to set
     */
    public void setDatatypeValue(IDatatype datatype) {
        this.datatype = datatype;
    }

    /**
     * @return the dataList
     */
    public List<Data> getDataList() {
        return dataList;
    }
    
    public List<Data> getResultList() {
        if  (dataList == null){
            return new ArrayList<Data>();
        }
        return dataList;
    }
    
    IDatatype getGraphList() {
        ArrayList<IDatatype> list = new ArrayList<IDatatype>();
        for (Data d : getDataList()) {
            if (d.getGraph() != null){
                list.add(DatatypeMap.createObject(d.getGraph()));
            }
        }
        return DatatypeMap.createList(list);
    }
    
    
    Data getResult() {
        for (Data d : getResultList()) {
            if (d.getName() == null) {
                return d;
            }
        }
        return null;
    }

    /**
     * @param dataList the dataList to set
     */
    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public IDatatype getValue(String var){
        if (getMappings() == null){
            return null;
        }
        return  (IDatatype) getMappings().getValue(var);
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

}
