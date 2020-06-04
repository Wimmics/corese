package fr.inria.corese.shex;

import fr.inria.corese.shex.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.shex.ShexConstraint;
import fr.inria.corese.shex.ShexShacl;
import fr.inria.lille.shexjava.graph.TCProperty;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.*;
import fr.inria.lille.shexjava.schema.concrsynt.*;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import fr.inria.lille.shexjava.util.Interval;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.rdf4j.model.Value;


/**
 *
 * @author corby
 */
public class Shex {
    
    private static final String SH_SHAPE = "sh:NodeShape";    
    private static final String SH_PATH = "sh:path";
    private static final String SH_NODE = "sh:node";
    private static final String SH_PROPERTY = "sh:property";
    private static final String SH_MINCOUNT = "sh:minCount";
    private static final String SH_MAXCOUNT = "sh:maxCount";
    private static final String SH_INVERSEPATH = "sh:inversePath";
    
   
    private StringBuilder sb;
    NSManager nsm =  NSManager.create();
    private Label label;
    ShexConstraint shexConst;
    private ShexShacl shacl;
    
    public Shex() {
        sb = new StringBuilder();
        shacl = new ShexShacl();
        shexConst = new ShexConstraint(this);
    }

    public StringBuilder parse(String path) throws Exception {
        Path schemaFile = Paths.get(path);
        List<Path> importDirectories = Collections.emptyList();

        // load and create the shex schema
        trace("Reading schema");
        ShexSchema schema = GenParser.parseSchema(schemaFile, importDirectories);
        process(schema);
        return getShacl().getStringBuilder();
    }
    
    

    public void process(ShexSchema schema) {
        for (Label label : schema.getRules().keySet()) {
            ShapeExpr exp = schema.getRules().get(label);
            trace("");
            trace("main: " + exp.getClass().getName());
            trace("");
            setLabel(label);
            main(exp);
        }
    }
       
    void main(ShapeExpr exp) {
        if (exp instanceof NodeConstraint) {
           // global NodeConstraint as a shacl shape
           process(null, (NodeConstraint) exp);
        }
        else {
            process(exp);
        }
    }

    void process(ShapeExpr exp) {
        if (exp instanceof Shape) {
            process((Shape) exp);
        } else if (exp instanceof ShapeExprRef) {
            process((ShapeExprRef) exp);
        } else if (exp instanceof ShapeAnd) {
            process((ShapeAnd) exp);
        } else if (exp instanceof ShapeOr) {
            process((ShapeOr) exp);
        } else if (exp instanceof ShapeNot) {
            process((ShapeNot) exp);
        } else if (exp instanceof NodeConstraint) {
            process((NodeConstraint) exp);
        } else {
            other(exp);
        }
    }
    
    void process(TripleExpr exp) {
        process(exp, null);
    }

    void process(TripleExpr exp, Context ct) {
        //trace(exp);
        if (exp instanceof TripleExprRef) {
            process((TripleExprRef) exp);
        } else if (exp instanceof TripleConstraint) {
            process((TripleConstraint) exp, ct);
        } else if (exp instanceof EachOf) {
            process((EachOf) exp);
        } else if (exp instanceof OneOf) {
            process((OneOf) exp);
        } else if (exp instanceof RepeatedTripleExpression) {
            process((RepeatedTripleExpression) exp);
        } else {
            trace("**** undef: " + exp.getClass().getName());
        }
    }

    void trace(Object obj) {
        System.out.println(obj);
    }

    void other(ShapeExpr exp) {
        trace("**** undef: " + exp.getClass().getName());
    }

    void process(Shape sh) {
        process(sh, new ArrayList<>());
    }
  
    void process(Shape sh, NodeConstraint cst) {
        ArrayList<NodeConstraint> list = new ArrayList<>();
        list.add(cst);
        process(sh, list);
    }

    void process(Shape sh, ArrayList<NodeConstraint> list) {
        getShacl().append(getPrefixURI(label.stringValue()));
        define(" a", SH_SHAPE);
        for (NodeConstraint cst : list) {
            process(cst);
        }
        if (sh != null) {
            process(sh.getTripleExpression());
        }
        getShacl().nl().nl();
    }

   

    void process(TripleExprRef exp) {
        trace("Ref: "+ exp);
    }

    /**
     * Use case: mix of NodeConstraint ans Shape
     * Insert node constraint inside the SHACL shape
     * my:EmployeeShape IRI /^http:\/\/hr\.example\/id#[0-9]+/ {
     *     foaf:name LITERAL;  
     *   }
     */
    void process(ShapeAnd exp) {
        ArrayList<NodeConstraint> cstList = new ArrayList<>();
        Shape shape = null;
        
        for (ShapeExpr ee : exp.getSubExpressions()) {
            if (ee instanceof NodeConstraint) {
                cstList.add((NodeConstraint) ee);
            }
            else if (ee instanceof Shape) {
                shape = (Shape) ee;
            }
        }
        
        if (shape != null && ! cstList.isEmpty()) {
            // insert node constraint list inside shape 
            process(shape, cstList);
        }
        else {
            // process and list 
            for (ShapeExpr ee : exp.getSubExpressions()) {
                process(ee);
            }
        }
    }

    void process(ShapeOr exp) {
        for (ShapeExpr ee : exp.getSubExpressions()) {
            process(ee);
        }
    }

    void process(ShapeNot exp) {
        process(exp.getSubExpression());
    }

    void process(RepeatedTripleExpression exp) {
        Interval i = exp.getCardinality();
        process(exp.getSubExpression(), new Context(exp));
    }

    void process(EachOf exp) {
        for (TripleExpr ee : exp.getSubExpressions()) {
            process(ee);
        }
    }

    void process(OneOf exp) {
        for (TripleExpr ee : exp.getSubExpressions()) {
            process(ee);
        }
    }

//    void process(AbstractNaryTripleExpr abs) {
//        trace(abs);
//        for (TripleExpr ee : abs.getSubExpressions()) {
//            process(ee);
//        }
//    }

    void process(ShapeExprRef exp) {
        define(SH_NODE, getPrefixURI(exp.getLabel().stringValue()));
    }
    
    

    void process(TripleConstraint tc, Context ctx) {
        ShapeExpr exp = tc.getShapeExpr();
        getShacl().append(SH_PROPERTY).append(" [").nl();
        process(tc.getProperty());
        tripleConstraint(exp);
        if (ctx != null) {
            process(ctx);
        }
        getShacl().append("];").nl();
    }
    
    void process(TCProperty p) {
        if (p.isForward()) {
           define(SH_PATH, getPrefixURI(p.getIri().toString()));
        }
        else {
           define(SH_PATH, String.format("[%s %s]", SH_INVERSEPATH, getPrefixURI(p.getIri().toString())));
        }
    }
    
    
    void process(Context ct) {
        if (ct.getRepeatedTripleExp() != null) {
            insert(ct.getRepeatedTripleExp());
        }
    }
    
    void insert(RepeatedTripleExpression exp) {
        define(SH_MINCOUNT, exp.getCardinality().min);
        define(SH_MAXCOUNT, exp.getCardinality().max);
    }
    
    
    void tripleConstraint(ShapeExpr exp) {
        if (exp instanceof NodeConstraint) {
            processBasic((NodeConstraint) exp);
        }
        else {
            process(exp);
        }
    }
    
    void processBasic(NodeConstraint node) {
        for (Constraint cst : node.getConstraints()) {
            shexConst.process(cst);
        }
    }

    void process(NodeConstraint node) {
        sb.append(SH_NODE).append(" [\n");
        processBasic(node);
        sb.append("];\n");
    }

    String getPrefixURI(String name) {
        String prop = nsm.toPrefix(name, true);
        if (name == prop) {
            return "<" + name + ">" ;
        }
        return prop;
    }
    
    
    
    /****************************************************************************
     * 
     */
    
    void define(String name, String value) {
        getShacl().define(name, value).nl();
    }
    
    void define(String name, List<String> list) {
       getShacl().define(name, list).nl();
    }
    
    void define(String name, BigDecimal value) {
        getShacl().define(name, value).nl();
    }
    
    void define(String name, int value) {
        getShacl().define(name, value).nl();
    }
    
    
    String getValue(String value) {
        if (value.startsWith("http://")) {
            return "<" + value + ">";
        }
        return value;
    }


    /**
     * @return the label
     */
    public Label getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(Label label) {
        this.label = label;
    }


    /**
     * @return the shacl
     */
    public ShexShacl getShacl() {
        return shacl;
    }

    /**
     * @param shacl the shacl to set
     */
    public void setShacl(ShexShacl shacl) {
        this.shacl = shacl;
    }

}
