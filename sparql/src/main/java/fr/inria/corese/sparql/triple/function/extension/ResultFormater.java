package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.ExprType.XT_JSON;
import static fr.inria.corese.kgram.api.core.ExprType.XT_RDF;
import static fr.inria.corese.kgram.api.core.ExprType.XT_XML;
import static fr.inria.corese.kgram.api.core.PointerType.MAPPINGS;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.function.XPathFun;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ResultFormater extends TermEval {

    public ResultFormater() {
    }

    public ResultFormater(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {

        switch (oper()) {
            case XT_JSON:
                if (arity() == 0) {
                    return DatatypeMap.json();
                }
                else if (arity() > 1) {
                    // xt:json(slot, value, slot, value)
                    IDatatype[] param = evalArguments(eval, b, env, p, 0);
                    if (param == null) {
                        return null;
                    }
                    return DatatypeMap.json(param);
                }
        }

        IDatatype dt = getArg(0).eval(eval, b, env, p); 

        if (dt == null) {
            return null;
        }

        switch (oper()) {
            case XT_JSON:
                if (dt.getCode() == IDatatype.STRING || dt.getCode() == IDatatype.LITERAL) {
                    return DatatypeMap.json(dt.stringValue());
                }
                else if (dt.isXML()) {
                    return dt.json();
                }
                break;

            case XT_XML: 
                switch (dt.getCode()) {
                    case IDatatype.STRING:
                    case IDatatype.LITERAL:
                    case IDatatype.XMLLITERAL:
                    case IDatatype.URI:
                    return parseXML(dt);
                }
                break;
                            
        }

        if (dt.pointerType() != MAPPINGS || dt.getPointerObject() == null) {
            return null;
        } else {
            Mappings map = dt.getPointerObject().getMappings();
            switch (oper()) {
                case XT_XML:
                    return eval.getGraphProcessor().format(map, ResultFormatDef.XML_FORMAT);
                case XT_JSON:
                    return eval.getGraphProcessor().format(map, ResultFormatDef.JSON_FORMAT);
                case XT_RDF:
                    return eval.getGraphProcessor().format(map, ResultFormatDef.RDF_FORMAT);

                default:
                    return dt;
            }
        }
    }
    
    IDatatype parseXML(IDatatype dt) {
        try {
            Document node = new XPathFun().parse(dt);
            IDatatype res = DatatypeMap.newXMLObject(dt.getLabel(), node); 
            return res;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(ResultFormater.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
       

}
