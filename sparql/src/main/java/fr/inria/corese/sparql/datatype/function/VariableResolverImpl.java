package fr.inria.corese.sparql.datatype.function;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;

/**
 * Variable resolver for XPath 
 * see Processor and XPathFunction
 * @author corby
 *
 */
public class VariableResolverImpl implements VariableResolver {
	Document document;
	QName var;
	Text value;
	Environment env;
	
	public VariableResolverImpl(Environment e){
		env = e;
	}
			
        @Override
	public void start(org.w3c.dom.Node doc){
		if (doc instanceof Document) {
			document = (Document)doc;
		}
		var = null;
		value = null;
	}
	
        @Override
	public Object resolveVariable(QName name) {
		if (var != null && value != null && name.equals(var)){
			return value;
		}
		var = name;
		if (env == null) return null;
		Node node = env.getNode("?" + name.getLocalPart());
		if (node != null){
			value = document.createTextNode(node.getLabel()); //name.getLocalPart());
			return value;
		}
		else {
			return null;
		}
	}
}

