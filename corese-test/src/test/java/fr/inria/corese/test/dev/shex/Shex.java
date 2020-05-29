/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.test.dev.shex;

import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.*;
import fr.inria.lille.shexjava.schema.concrsynt.Constraint;
import fr.inria.lille.shexjava.schema.concrsynt.DatatypeConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.FacetNumericConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.FacetStringConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.IRIStemConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.LanguageConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.LanguageStemConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.LiteralStemConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.NodeKindConstraint;
import fr.inria.lille.shexjava.schema.concrsynt.ValueSetValueConstraint;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.eclipse.rdf4j.model.Value;
//import org.apache.commons.rdf.api.Graph;
//import org.apache.commons.rdf.api.IRI;

/**
 *
 * @author corby
 */
public class Shex {
    
    StringBuilder sb;
    NSManager nsm =  NSManager.create();
    private Label label;
    
    Shex() {
        sb = new StringBuilder();
    }

    static String data = Shex.class.getClassLoader().getResource("data/").getPath();
    static String shex = Shex.class.getClassLoader().getResource("data/shex/").getPath();

    public static void main(String[] args) throws Exception {
        new Shex().parse(shex + "test1.shex");
    }

    void parse(String path) throws Exception {
        Path schemaFile = Paths.get(path);
        List<Path> importDirectories = Collections.emptyList();

        // load and create the shex schema
        trace("Reading schema");
        ShexSchema schema = GenParser.parseSchema(schemaFile, importDirectories);
        process(schema);
    }

    void process(ShexSchema schema) {
        for (Label label : schema.getRules().keySet()) {
            ShapeExpr exp = schema.getRules().get(label);
            trace("");
            trace("main: " + exp.getClass().getName());
            trace("");
            setLabel(label);
            process(exp);
        }
        System.out.println("");
        finish();
    }
    
    void finish() {
        System.out.println(sb);
    }

    void process(ShapeExpr exp) {
       // trace(exp.getClass().getName());

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
        //trace(exp.getClass().getName());
        if (exp instanceof TripleExprRef) {
            process((TripleExprRef) exp);
        } else if (exp instanceof TripleConstraint) {
            process((TripleConstraint) exp);
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
        TripleExpr tr = sh.getTripleExpression();
        //trace(tr.getClass().getName());
        sb.append(getPrefixURI(label.stringValue())).append(" a sh:Shape [\n");
        process(tr);
        sb.append("] .\n\n");
    }

   

    void process(TripleExprRef exp) {
        //process(exp.getTripleExp());
        trace("Ref: "+ exp);
    }

    // and or
    void process(ShapeAnd exp) {
        //trace(exp.getClass().getName());
        for (ShapeExpr ee : exp.getSubExpressions()) {
            process(ee);
        }
    }

    void process(ShapeOr exp) {
        //trace(exp.getClass().getName());
        for (ShapeExpr ee : exp.getSubExpressions()) {
            process(ee);
        }
    }

    void process(ShapeNot exp) {
        //trace(exp.getClass().getName());
        process(exp.getSubExpression());
    }

    void process(RepeatedTripleExpression exp) {
        //trace(exp.getClass().getName());
        process(exp.getSubExpression());
    }

    void process(EachOf exp) {
        //trace(exp.getClass().getName());
        for (TripleExpr ee : exp.getSubExpressions()) {
            //trace(ee.getClass().getName() + " " + ee);
            process(ee);
        }
    }

    void process(OneOf exp) {
        //trace(exp.getClass().getName());
        for (TripleExpr ee : exp.getSubExpressions()) {
            //trace(ee.getClass().getName() + " " + ee);
            process(ee);
        }
    }

    void process(AbstractNaryTripleExpr abs) {
        //trace(abs.getClass().getName());
        for (TripleExpr ee : abs.getSubExpressions()) {
            //trace(ee.getClass().getName() + " " + ee );
            process(ee);
        }
    }

    void process(ShapeExprRef exp) {
        sb.append("sh:node ").append(getPrefixURI(exp.getLabel().stringValue())).append(";\n");
    }
    
    

    void process(TripleConstraint tc) {
        ShapeExpr exp = tc.getShapeExpr();
        trace("prop: " + tc.getProperty());
        //trace(exp.getClass().getName());
        sb.append("sh:property [\n");
        sb.append("sh:path ").append(getPrefixURI(tc.getProperty().toString())).append(";\n");
        process(exp);
        sb.append("];\n");
    }

    void process(NodeConstraint node) {
        for (Constraint cst : node.getConstraints()) {
            process(cst);
        }
    }

    String getPrefixURI(String name) {
        String prop = nsm.toPrefix(name, true);
        if (name == prop) {
            return "<" + name + ">" ;
        }
        return prop;
    }
    
    /************************************************************************
     * 
     *  Constraint
     * 
     */

    void process(Constraint cst) {
        if (cst instanceof DatatypeConstraint) {
            process((DatatypeConstraint) cst);
        } 
        else if (cst instanceof ValueSetValueConstraint) {
            process((ValueSetValueConstraint) cst);
        } 
        else if (cst instanceof FacetNumericConstraint) {
            process((FacetNumericConstraint) cst);
        } 
        else if (cst instanceof FacetStringConstraint) {
            process((FacetStringConstraint) cst);
        } 
        else if (cst instanceof IRIStemConstraint) {
            process((IRIStemConstraint) cst);
        } 
        else if (cst instanceof LanguageConstraint) {
            process((LanguageConstraint) cst);
        } 
        else if (cst instanceof LanguageStemConstraint) {
            process((LanguageStemConstraint) cst);
        } 
        else if (cst instanceof LiteralStemConstraint) {
            process((LiteralStemConstraint) cst);
        } 
        else if (cst instanceof NodeKindConstraint) {
            process((NodeKindConstraint) cst);
        } 
        
        else {
            trace("undef cst: " + cst.getClass().getName() + " " + cst);
        }
    }

    void process(DatatypeConstraint cst) {
        sb.append("sh:datatype ").append(getPrefixURI(cst.getDatatypeIri().toString())).append(";\n");
    }
    
    String getValue(String value) {
        if (value.startsWith("http://")) {
            return "<" + value + ">";
        }
        return value;
    }

    void process(ValueSetValueConstraint cst) {
        if (cst.getExplicitValues().size() == 1) {            
            for (Value val : cst.getExplicitValues()) {
                sb.append("sh:hasValue ").append(getValue(val.stringValue())).append(";\n");
            }
        } else {
            sb.append("sh:in (");
            for (Value val : cst.getExplicitValues()) {
                sb.append(getValue(val.stringValue())).append(" ");
            }
            sb.append("); \n");
        }
    }
    
     void process(FacetNumericConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        if (cst.getMaxincl()!= null) {
            sb.append("sh:maxInclusive ").append(cst.getMaxincl()).append(";\n");
        }
    }

    void process(FacetStringConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLength();
    }
    
    void process(IRIStemConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getIriStem();
    }

    void process(LanguageConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLangTag();
    }
    
     void process(LanguageStemConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLangStem();
    }

    void process(LiteralStemConstraint cst) {
        trace("cst: " + cst.getClass().getName() + " " + cst);
        cst.getLitStem();
    }
    
    void process(NodeKindConstraint cst) {
        sb.append("sh:nodeKind ").append(getKind(cst)).append(";\n");
    }
    
    String getKind(NodeKindConstraint cst) {
        if (cst == NodeKindConstraint.AllIRI) {
            return "sh:IRI";
        }
        if (cst == NodeKindConstraint.AllLiteral) {
            return "sh:Literal";
        }
        if (cst == NodeKindConstraint.AllNonLiteral) {
            return "sh:IRIOrBlank";
        }
        if (cst == NodeKindConstraint.Blank) {
            return "sh:Blank";
        }
        return "sh:Undef";
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

}
