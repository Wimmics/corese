package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.transform.Transformer;
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
    public static final String SHAPE_TRANS_TEST = "/user/corby/home/AAData/sttl/datashape/main";
    public static final String SHAPE_SHAPE      = "/data/shape4shape.ttl";
    public static final String SHAPE_TRANS = Transformer.DATASHAPE;
    public static final String FORMAT = Transformer.TURTLE;
    public static final String FORMAT_HTML = Transformer.TURTLE_HTML;
    private static final String NL = System.getProperty("line.separator");
    
    private String resultFormat = FORMAT;
    private String shape ;
    TransformationProcess transformer;
    LoadProcess load;
    // draft: evaluate shape4shape on the shape
    ShapeWorkflow validator;
    private boolean validate = false;
    
    public ShapeWorkflow(String shape, String data){
        create(shape, data, resultFormat, false, -1, false);
    }
    
    public ShapeWorkflow(String shape, String data, String trans){
        create(shape, data, trans, false, -1, false);
    }
    
    public ShapeWorkflow(String shape, String data, boolean test){
        create(shape, data, resultFormat, false, -1, test);
    }
    
    public ShapeWorkflow(String shape, String data, String trans, boolean text, int format, boolean test){
        create(shape, data, trans, text, format, test);
    }
    
    
    
    @Override
    boolean isShape(){
        return true;
    }
    
    @Override
    public void start(Data data){
        try {
            if (hasMode() && getMode().isNumber()){
                load.setMode(getMode());
            }
            super.start(data);
            validate();
        } catch (EngineException ex) {
            Logger.getLogger(ShapeWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param shape input shape, may be text or URI (see text)
     * @param data  input rdf,   may be text or URI (see text)
     * @param trans
     * @param text : true if input graph and shape are RDF text (not URI of document)
     * @param format : possible RDF input format (may be UNDEF_FORMAT)
     * @param test : false: use compiled datashape sttl, otherwise use uncompiled sttl
     */
    private void create(String shape, String data, String trans, boolean text, int format, boolean test){
        this.setShape(shape);
        if (trans != null){
            resultFormat = trans;
        }
        setCollect(true);
        load =           (text) ?  LoadProcess.createStringLoader(data, format) :  new LoadProcess(data);
        LoadProcess ls = (text) ?  LoadProcess.createStringLoader(shape, format) : new LoadProcess(shape);
        ParallelProcess para = new ParallelProcess();
        para.insert(new SemanticWorkflow().add(load));
        para.insert(new SemanticWorkflow(SHAPE_NAME).add(ls));
        // test = true: use DataShape transformation not compiled
        transformer = new TransformationProcess((test)?SHAPE_TRANS_TEST:SHAPE_TRANS);
        this.add(para)
            .add(new DatasetProcess())
            .add(transformer);
        // set  Visitor Report Graph as named graph st:visitor
        this.add(new DatasetProcess(WorkflowParser.VISITOR));
                      
        if (test){
            setContext(new Context().export(Context.STL_TEST, DatatypeMap.TRUE));
        }        
    }
    
    /**
     * Draft: validate shape with shape4shape.ttl
     * use case:  sw:param [sw:mode sw:validate]
     */
    void validate() throws EngineException {
        if (pgetWorkflow().getContext().hasValue(WorkflowParser.MODE, WorkflowParser.VALIDATE) 
                && ! isValidate()) {
            validator = new ShapeWorkflow(ShapeWorkflow.class.getResource(SHAPE_SHAPE).toString(), shape);
            validator.setValidate(true);
            Data res = validator.process();
            System.out.println("Validate: " + getShape());
            if (res.getVisitedGraph().size() > 0) {
                System.out.println("Result: \n" + res);
            }
        }
    }
    
//    @Override
//    public Data run(Data data) throws EngineException {
//        return super.run(data);
//    }
    
    public TransformationProcess getTransformer(){
        return transformer;
    }
    
    @Override
    public long getMainTime(){
        return transformer.getTime();
    }
    
    
    @Override
    public void finish(Data data) {
        super.finish(data);
        data.setProcess(this);
    }
    
    
    @Override
    public String stringValue(Data data){
        String res ;
        if (data.getVisitedGraph().size() == 0){
            res = success();
        }
        else {
            //res = data.getTemplateResult();
            Transformer t = Transformer.create(data.getVisitedGraph(), resultFormat);
            res = t.transform();
        }
        return res;
    }
    
    String success(){
        if (resultFormat.equals(FORMAT_HTML)){
            return "<h2>Data Shape Validation</h2><pre>success</pre>";
        }
        else {
            return "Data Shape Validation success";
        }
    }

   @Override
   public String getTransformation(){
       return resultFormat;
   }

    /**
     * @return the shape
     */
    public String getShape() {
        return shape;
    }

    /**
     * @param shape the shape to set
     */
    public void setShape(String shape) {
        this.shape = shape;
    }

    /**
     * @return the validate
     */
    public boolean isValidate() {
        return validate;
    }

    /**
     * @param validate the validate to set
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
   
}
