package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.transform.TemplateVisitor;
import fr.inria.edelweiss.kgtool.transform.Transformer;

/**
 * Domain Specific Workflow to process DataShape on RDF Graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ShapeWorkflow extends SemanticWorkflow {
    public static final String SHAPE_NAME  = NSManager.STL + "shape";
    public static final String SHAPE_TRANS = "/user/corby/home/AATest/shape/sttl/scope";
    
    public ShapeWorkflow(String shape, String data){
        create(shape, data);
    }
    
    void create(String shape, String data){
        LoadProcess ld = new LoadProcess(data);
        LoadProcess ls = new LoadProcess(shape);
        ParallelProcess para = new ParallelProcess();
        para.insert(new SemanticWorkflow().add(ld));
        para.insert(new SemanticWorkflow(SHAPE_NAME).add(ls));
        
        this.add(para)
            .add(new DatasetProcess())
            .add(new TransformationProcess(SHAPE_TRANS));
        
    }
    
    
    @Override
    public void finish(Data data) {
        super.finish(data);
        data.setProcess(this);
    }
    
    
    @Override
    public String stringValue(Data data){
        Transformer t = Transformer.create(getValidation(data), Transformer.TURTLE);
        return t.transform();
    }
    
    public Graph getValidation(Data data){
        TemplateVisitor vis = data.getVisitor();
        return (Graph) vis.visitedGraph().getPointerObject();
    }

}
