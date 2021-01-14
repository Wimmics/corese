/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.ExprType.XT_ATTRIBUTE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ATTRIBUTES;
import static fr.inria.corese.kgram.api.core.ExprType.XT_BASE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_CHILDREN;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ELEMENTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_HAS_ATTRIBUTE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NAMESPACE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_DOCUMENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_ELEMENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_FIRST_CHILD;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_LOCAL_NAME;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_NAME;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_PARENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_PROPERTY;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_TYPE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE_VALUE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TEXT_CONTENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_XSLT;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.extension.CoreseXML;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 */
public class XML extends TermEval {

    public XML() {
    }

    public XML(String name) {
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);

        if (param == null) {
            return null;
        }

        IDatatype dt = param[0], ns = null, label = null;

        switch (arity()) {
            case 2:
                label = param[1];
                break;
            case 3:
                ns = param[1];
                label = param[2];
                break;
        }
        
        if (isXML(dt)) {
            CoreseXML node = DatatypeMap.getXML(dt);
            
            switch (oper()) {
                
                case XT_XSLT:
                    // xslt(xml, xsl)
                    return node.xslt(label);
                    
                case XT_ATTRIBUTES:
                    return node.getAttributes();

                case XT_NODE_TYPE:
                    return node.getNodeType();

                case XT_NODE_VALUE:
                    return node.getNodeValue();

                case XT_NAMESPACE:
                    return node.getNamespaceURI();

                case XT_BASE:
                    return node.getBaseURI();

                case XT_ELEMENTS:
                    switch (arity()) {
                        case 1:
                            return node.getChildElements();
                        case 2:
                            return node.getElementsByTagName(label);
                        default:
                            return node.getElementsByTagNameNS(ns, label);
                    }

                case XT_NODE_ELEMENT:
                    return node.getElementById(label);

                case XT_ATTRIBUTE:
                    if (arity() == 2) {
                        return node.getAttribute(label);
                    } else {
                        return node.getAttributeNS(ns, label);
                    }
                    
                case XT_HAS_ATTRIBUTE:
                    if (arity() == 2) {
                        return node.hasAttribute(label);
                    } else {
                        return node.hasAttributeNS(ns, label);
                    }    

                case XT_CHILDREN:
                    return node.getChildNodes();
                    
                case XT_NODE_FIRST_CHILD:
                    return node.getFirstChild();

                case XT_TEXT_CONTENT:
                    return node.getTextContent();

                case XT_NODE_NAME:
                    return node.getNodeName();
                    
                case XT_NODE_LOCAL_NAME:
                    return node.getLocalName();    

                case XT_NODE_PROPERTY:
                    return node.getNodeProperty();

                case XT_NODE_PARENT:
                    return node.getParentNode();

                case XT_NODE_DOCUMENT:
                    return node.getOwnerDocument();

            }
        }
        else {
            switch (oper()) {
                case XT_NAMESPACE:
                    String str = NSManager.namespace(dt.getLabel());
                    if (str.isEmpty()) {
                        return dt;
                    }
                    return DatatypeMap.newResource(str);
            }
        }

        return null;
    }

    boolean isXML(IDatatype dt) {
        return dt.getDatatype() == CoreseXML.singleton.getDatatype();
    }

}
