package fr.inria.corese.core.workflow;

import fr.inria.corese.core.Graph;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.IDatatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Domain Specific Workflow to process DataShape on RDF Graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ShapeWorkflow extends SemanticWorkflow {
    private static Logger logger = LoggerFactory.getLogger(ShapeWorkflow.class);
    public static final String SHAPE_NAME           = NSManager.STL + "shape";
    public static final String SHAPE_TRANS_TEST     = "/user/corby/home/AAData/sttl/datashape/main";
    public static final String SHAPE_TRANS_TEST_LDS = "/user/corby/home/AAData/sttl/datashape/main";
    public static final String SHAPE_SHAPE          = "/data/shape4shape.ttl";
    public static final String SHAPE_TRANS          = Transformer.DATASHAPE;
    public static final String FORMAT               = Transformer.TURTLE;
    public static final String FORMAT_HTML          = Transformer.TURTLE_HTML;
    private static final String NL                  = System.getProperty("line.separator");
    
    static final String MAIN = Transformer.STL_MAIN;
    static final String SHAPE_NODE  = NSManager.SHAPE + "shapeNode";
    static final String SHAPE_GRAPH = NSManager.SHAPE + "shapeGraph";
    static final IDatatype dtnode  = DatatypeMap.newResource(SHAPE_NODE);
    static final IDatatype dtgraph = DatatypeMap.newResource(SHAPE_GRAPH);
    
    private String resultFormat = FORMAT;
    private String shape ;
    TransformationProcess transformer;
    LoadProcess load;
    // draft: evaluate shape4shape on the shape
    ShapeWorkflow validator;
    private boolean validate = false;
    private boolean shex = false;
    private PreProcessor processor;
    
    public ShapeWorkflow() {}
    
    public ShapeWorkflow(String shape, String data){
        create(shape, data, resultFormat, false, -1, false);
    }
    
    public ShapeWorkflow(String shape, String data, String trans){
        create(shape, data, trans, false, -1, false);
    }
    
    public ShapeWorkflow(String shape, String data, boolean test){
        create(shape, data, resultFormat, false, -1, test);
    }
    
    public ShapeWorkflow(String shape, String data, boolean test, boolean lds){
        create(shape, data, resultFormat, false, -1, test, lds);
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
            logger.error(ex.getMessage());
        }
    }
    
        private void create(String shape, String data, String trans, boolean text, int format, boolean test){
            create(shape, data, trans, text, format, test, false);
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
    public ShapeWorkflow create(String shape, String data, String trans, boolean text, int format, boolean test, boolean lds){
        this.setShape(shape);
        if (trans != null){
            resultFormat = trans;
        }
        setCollect(true);
        SemanticWorkflow rdfWorkflow   = new SemanticWorkflow();
        SemanticWorkflow shapeWorkflow = new SemanticWorkflow(SHAPE_NAME);
        if (shape != null) {
            shapeWorkflow.add(loadShape(shape, text, format));
        }
        if (data != null) {
            load = (text) ?  LoadProcess.createStringLoader(data, format) :  new LoadProcess(data);
            // arg comes from http: no file access
            rdfWorkflow.add(load);
        }
        ParallelProcess para = new ParallelProcess();
        para.insert(rdfWorkflow);
        para.insert(shapeWorkflow);
        // test = true: use DataShape transformation not compiled
        if (test){
            transformer = new TransformationProcess((lds)?SHAPE_TRANS_TEST_LDS:SHAPE_TRANS_TEST);          
        }
        else {
            transformer = new TransformationProcess(SHAPE_TRANS);            
        }
        this.add(para)
            .add(new DatasetProcess())
            .add(transformer);
        // set  Visitor Report Graph as named graph st:visitor
        this.add(new DatasetProcess(WorkflowParser.VISITOR));
                      
        if (test){
            setContext(new Context().export(Context.STL_TEST, DatatypeMap.TRUE));
        }  
        
        return this;
    }
    
    LoadProcess loadShape(String shape, boolean text, int format) {
        LoadProcess shapeLoader = (text) ? LoadProcess.createStringLoader(shape, format) : new LoadProcess(shape);
        shapeLoader.setProcessor(getProcessor());
        return shapeLoader;
    }
    
    
    Graph graph(IDatatype dt) {
        if (dt.isURI()) {
            return parse(dt.getLabel());
        }
        else if (dt.pointerType() == PointerType.GRAPH){
            return (Graph) dt.getPointerObject();
        }
        logger.warn("Empty graph: " + dt);
        return Graph.create();
    }
    
    Graph parse(String uri) {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.parse(uri);
            g.init();
            return g;
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
        }
        return Graph.create();
    }
    
    public IDatatype process(IDatatype g, IDatatype s, IDatatype... ldt) {
        Graph res = process(graph(g), graph(s), true,  ldt);
        return DatatypeMap.createObject(res);
    }
    
    public IDatatype processGraph(Graph g, IDatatype s, IDatatype... ldt) {
        Graph res = process(g, graph(s), true, ldt);
        return DatatypeMap.createObject(res);
    }
    
    public IDatatype processNode(Graph g, IDatatype s, IDatatype... ldt) {
        Graph res = process(g, graph(s), false, ldt);
        return DatatypeMap.createObject(res);
    }
    
    public Graph process(String g, String s) {
        return process(parse(g), parse(s), true);
    }
    
    public Graph process(Graph g, String shape) {
        Graph s = parse(shape);
        return process(g, s, true);
    }
    
    IDatatype[] array(IDatatype... ldt) {
        return ldt;
    }
    
    /**
     * graph = true means check whole graph, false means check uri
     * param = shape | uri | uri, shape
     */
    public Graph process(Graph g) {
        return process(g, g, true);
    }
    
    public Graph process(Graph g, Graph s, boolean graph, IDatatype... param) {
        Transformer t = Transformer.create(g, SHAPE_TRANS);
        t.getContext().export(SHAPE_NAME, DatatypeMap.createObject(s));
        try {
            if (graph) {
                // check whole graph
                if (param.length == 0) {
                    // whole shape graph
                    t.process();
                } else {
                    // param = {shape}
                    //t.template(MAIN, param[0]);
                    t.process(MAIN, dtgraph, param[0]);
                }
            } else {
                // check node in graph
                switch (param.length) {
                    case 1:
                        // param = {node}
                        t.process(MAIN, dtnode, param[0]);
                        break;
                    case 2:
                        // TBD: param = {node, shape}
                        t.process(MAIN, dtnode, param[0], param[1]);
                        break;
                }
            }
        } catch (EngineException e) {
            logger.error(e.getMessage());
        }

        if (t.getVisitor() == null || t.getVisitor().visitedGraph() == null) {
            return Graph.create();
        }
        Graph res = t.getVisitor().visitedGraph();
        res.init();
        return res;
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
            try {
                //res = data.getTemplateResult();
                Transformer t = Transformer.create(data.getVisitedGraph(), resultFormat);
                res = t.transform();
            } catch (EngineException ex) {
                logger.error(ex.getMessage());
                res = "";
            }
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

    /**
     * @return the shex
     */
    public boolean isShex() {
        return shex;
    }

    /**
     * @param shex the shex to set
     */
    public ShapeWorkflow setShex(boolean shex) {
        this.shex = shex;
        return this;
    }

    /**
     * @return the processor
     */
    public PreProcessor getProcessor() {
        return processor;
    }

    /**
     * @param processor the processor to set
     */
    public ShapeWorkflow setProcessor(PreProcessor processor) {
        this.processor = processor;
        return this;
    }

}
