/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.ExprType.XT_ATTRIBUTES;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ELEMENTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_NAME;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_PROPERTY;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_TYPE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TEXT_CONTENT;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseXML;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;

/**
 *
 */
public class XML extends TermEval {
    
    public XML() {
    }

    public XML(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getArg(0).eval(eval, b, env, p);

        if (dt == null) {
            return null;
        }
        
        if (isXML(dt)) {
            CoreseXML node = (CoreseXML) dt;
            switch (oper()) {
                case XT_ATTRIBUTES:
                    return node.getAttributes();

                case XT_NODE_TYPE:
                    return node.getNodeType();
                    
                case XT_ELEMENTS:
                    IDatatype dt2 = getArg(1).eval(eval, b, env, p);
                    if (dt2 == null) {
                        return null;
                    }
                    return node.getElementsByTagName(dt2);
                    
                case XT_TEXT_CONTENT:
                    return node.getTextContent();
                    
                case XT_NODE_NAME:    
                    return node.getNodeName();
                    
                case XT_NODE_PROPERTY:
                    return node.getNodeProperty();

            }
        }
        else {
            switch (oper()) {
                case XT_ATTRIBUTES:
                    return DatatypeMap.map();

                case XT_NODE_TYPE:
                    return CoreseXML.TEXT;
                      
                case XT_ELEMENTS: 
                    return DatatypeMap.newList();
                    
                case XT_TEXT_CONTENT:
                    return dt; 
                    
                case XT_NODE_NAME:    
                case XT_NODE_PROPERTY:
                    return dt;    
            }
        }      
        
        return null;
    }

    boolean isXML(IDatatype dt) {
        return dt.getDatatype() == CoreseXML.singleton.getDatatype();
    }
    
}
