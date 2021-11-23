package fr.inria.corese.compiler.parser;

import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Variable;

public class NodeImpl extends fr.inria.corese.kgram.tool.NodeImpl {

   
    public NodeImpl(Atom at) {
        super(at);
    }

    public static NodeImpl createNode(Atom at) {
        return new NodeImpl(at);
    }

    public static NodeImpl createVariable(String name) {
        return new NodeImpl(Variable.create(name));
    }

    public static NodeImpl createResource(String name) {
        return new NodeImpl(Constant.create(name));
    }

    public static NodeImpl createConstant(String name) {
        return new NodeImpl(Constant.create(name, RDFS.xsdstring));
    }

    public static NodeImpl createConstant(String name, String datatype) {
        return new NodeImpl(Constant.create(name, datatype));
    }

    public static NodeImpl createConstant(String name, String datatype, String lang) {
        return new NodeImpl(Constant.create(name, null, lang));
    }

}
