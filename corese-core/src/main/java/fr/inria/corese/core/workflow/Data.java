package fr.inria.corese.core.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;

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
    private DataManager dataManager;
    private Context context;
    private Binding binding;
    // private TemplateVisitor visitor;
    private String templateResult;
    private List<Data> dataList;
    private String name;
    private boolean success = true;

    public Data(Graph g) {
        graph = g;
    }

    public Data(Graph g, DataManager man) {
        graph = g;
        setDataManager(man);
    }

    Data(Graph g, IDatatype dt) {
        graph = g;
        datatype = dt;
    }

    Data(Mappings m, Graph g) {
        map = m;
        graph = g;
    }

    Data(WorkflowProcess p, Mappings m, Graph g) {
        process = p;
        map = m;
        graph = g;
    }

    Data(WorkflowProcess p, Graph g) {
        process = p;
        graph = g;
    }

    Data(WorkflowProcess p, List<Data> l) {
        process = p;
        dataList = l;
    }

    Data copy() {
        Data data = new Data(process, map, graph);
        data.setDatatypeValue(datatype);
        data.setDataList(dataList);
        // data.setVisitor(visitor);
        data.setDataset(dataset);
        data.setContext(context);
        data.setTemplateResult(templateResult);
        data.setBinding(binding);
        return data;
    }

    Data copy(boolean b) {
        Data input = copy();
        if (b && getGraph() != null) {
            input.setGraph(getGraph().copy());
        }
        return input;
    }

    QuerySolverVisitorTransformer createVisitor() {
        try {
            return QuerySolverVisitorTransformer
                    .create(QueryProcess.create(getGraph(), getDataManager()).getCreateEval());
        } catch (EngineException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String toString() {
        return stringValue();
    }

    public String stringValue() {
        if (process != null) {
            return process.stringValue(this);
        }
        if (getMappings() != null) {
            return getMappings().toString();
        }
        if (getGraph() != null) {
            return getGraph().toString();
        }
        return getClass().getName();
    }

    public WorkflowProcess getProcess() {
        return process;
    }

    public void setProcess(WorkflowProcess process) {
        this.process = process;
    }

    public Mappings getMappings() {
        return map;
    }

    public void setMappings(Mappings map) {
        this.map = map;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public EventManager getEventManager() {
        if (getGraph() == null) {
            return null;
        }
        return getGraph().getEventManager();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    // public TemplateVisitor getVisitor() {
    // return visitor;
    // }
    //
    //
    // public void setVisitor(TemplateVisitor visitor) {
    // this.visitor = visitor;
    // }

    public Graph getVisitedGraph() {
        TemplateVisitor vis = getTransformerVisitor();
        if (vis == null) {
            return null;
        }
        return vis.visitedGraph();
    }

    public TemplateVisitor getTransformerVisitor() {
        if (getBinding() == null) {
            return null;
        }
        return (TemplateVisitor) getBinding().getTransformerVisitor();
    }

    // public Graph getVisitedGraph(){
    // if (getVisitor() == null){
    // return null;
    // }
    // return getVisitor().visitedGraph();
    // }

    public String getTemplateResult() {
        return templateResult;
    }

    public void setTemplateResult(String templateResult) {
        this.templateResult = templateResult;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public IDatatype getDatatypeValue() {
        return datatype;
    }

    public void setDatatypeValue(IDatatype datatype) {
        this.datatype = datatype;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public List<Data> getResultList() {
        if (dataList == null) {
            return new ArrayList<Data>();
        }
        return dataList;
    }

    void initContext(Context c) {
        set(c, Context.STL_GRAPH, getGraph());
        set(c, Context.STL_SOLUTION, getMappings());
        if (getDatatypeValue() != null) {
            c.set(Context.STL_VALUE, getDatatypeValue());
        }

        if (getDataList() != null) {
            // use case: former Parallel process generated Data List
            c.set(Context.STL_GRAPH_LIST, getGraphList());
            IDatatype dt = getTransformationList();
            if (dt.size() > 0) {
                c.set(Context.STL_TRANSFORMATION_LIST, dt);
            }
        }
    }

    void set(Context c, String name, Object obj) {
        if (obj != null) {
            c.set(name, DatatypeMap.createObject(obj));
        }
    }

    IDatatype getGraphList() {
        ArrayList<IDatatype> list = new ArrayList<IDatatype>();
        for (Data d : getDataList()) {
            if (d.getGraph() != null) {
                list.add(DatatypeMap.createObject(d.getGraph()));
            }
        }
        return DatatypeMap.createList(list);
    }

    IDatatype getTransformationList() {
        ArrayList<IDatatype> list = new ArrayList<IDatatype>();
        for (Data d : getDataList()) {
            if (d.getTemplateResult() != null) {
                list.add(d.getDatatypeValue());
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

    Dataset dataset(Context c, Dataset ds) {
        if (ds == null && (c != null || getBinding() != null)) { // || getVisitor()!= null
            ds = new Dataset();
        }
        if (c != null) {
            ds.setContext(c);
        }
        // if (getVisitor()!= null) {
        // ds.setTemplateVisitor(getVisitor());
        // }
        if (getBinding() != null) {
            ds.setBinding(getBinding());
        }
        return ds;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    void addData(Data d) {
        if (dataList == null) {
            dataList = new ArrayList<Data>();
        }
        dataList.add(d);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IDatatype getValue(String var) {
        if (getMappings() == null) {
            return null;
        }
        return getMappings().getValue(var);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}
