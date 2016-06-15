package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import java.awt.font.TransformAttribute;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final String NL = System.getProperty("line.separator");
    static final String SHAPE_TEMPLATE = "/query/shape.rq";
    
    private String format = FORMAT;
    TransformationProcess transformer;
    
    SPARQLProcess sp;
    
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
    
    @Override
    boolean isShape(){
        return true;
    }
    
    private void create(String shape, String data, String trans, boolean test){
        if (trans != null){
            format = trans;
        }
        setCollect(true);
        LoadProcess ld = new LoadProcess(data);
        LoadProcess ls = new LoadProcess(shape);
        ParallelProcess para = new ParallelProcess();
        para.insert(new SemanticWorkflow().add(ld));
        para.insert(new SemanticWorkflow(SHAPE_NAME).add(ls));
        // test = true: use DataShape transformation not compiled
        this.add(para)
            .add(new DatasetProcess())
            .add(new TransformationProcess((test)?SHAPE_TRANS_TEST:SHAPE_TRANS));
        // set  Visitor Report Graph as named graph st:visitor
        this.add(new DatasetProcess(WorkflowParser.VISITOR));
                      
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
        String res ;
        if (data.getGraph().size() == 0){
            res = success();
        }
        else {
            //res = data.getTemplateResult();
            Transformer t = Transformer.create(data.getVisitedGraph(), format);
            res = t.transform();
        }
        return res;
    }
    
    String success(){
        if (format.equals(FORMAT_HTML)){
            return "<h2>Data Shape Validation</h2><pre>sucess</pre>";
        }
        else {
            return "Data Shape Validation success";
        }
    }

   @Override
   public String getTransformation(){
       return format;
   }
   
}
