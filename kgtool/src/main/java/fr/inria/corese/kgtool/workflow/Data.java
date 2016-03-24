package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.transform.TemplateVisitor;

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
    
    @Override
    public String toString(){
        return stringValue();
    }
    
    public String stringValue(){
        if (process != null){
            return process.stringValue(this);
        }
        return null;
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

    /**
     * @return the datatype
     */
    public IDatatype getDatatype() {
        return datatype;
    }

    /**
     * @param datatype the datatype to set
     */
    public void setDatatype(IDatatype datatype) {
        this.datatype = datatype;
    }

}
