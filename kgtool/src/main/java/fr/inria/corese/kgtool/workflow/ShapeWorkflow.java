package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgtool.transform.Transformer;

/**
 * Domain Specific Workflow to process DataShape on RDF Graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ShapeWorkflow extends SemanticWorkflow {
    public static final String SHAPE_NAME  = NSManager.STL + "shape";
    public static final String SHAPE_TRANS_TEST = "/home/corby/AAData/sttl/datashape/main";
    public static final String SHAPE_TRANS = Transformer.DATASHAPE;
    public static final String FORMAT = Transformer.TURTLE;
    public static final String FORMAT_HTML = Transformer.TURTLE_HTML;
    
    private String format = FORMAT;
    
    public ShapeWorkflow(String shape, String data){
        create(shape, data, format, false);
    }
    
    public ShapeWorkflow(String shape, String data, String trans){
        create(shape, data, trans, false);
    }
    
    public ShapeWorkflow(String shape, String data, boolean test){
        create(shape, data, format, test);
    }
    
    public ShapeWorkflow(String shape, String data, String trans, boolean test){
        create(shape, data, trans, test);
    }
    
    private void create(String shape, String data, String format, boolean test){
        if (format == null){
            format = FORMAT;
        }
        LoadProcess ld = new LoadProcess(data);
        LoadProcess ls = new LoadProcess(shape);
        ParallelProcess para = new ParallelProcess();
        para.insert(new SemanticWorkflow().add(ld));
        para.insert(new SemanticWorkflow(SHAPE_NAME).add(ls));
        
        this.add(para)
            .add(new DatasetProcess())
            .add(new TransformationProcess((test)?SHAPE_TRANS_TEST:SHAPE_TRANS));
        
        this.add(new DatasetProcess(WorkflowParser.VISITOR));
        
        this.add(new TransformationProcess(format));
        
        if (test){
            setContext(new Context().export(Context.STL_TEST, DatatypeMap.TRUE));
        }       
    }
    
    
    @Override
    public void finish(Data data) {
        super.finish(data);
        data.setProcess(this);
    }
    
    
    @Override
    public String stringValue(Data data){
        if (data.getGraph().size() == 0){
            return "Validation succeeded";
        }
        else {
            return data.getTemplateResult();
        }
    }

   
}
