package fr.inria.edelweiss.kgenv.eval;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import fr.inria.acacia.corese.cg.datatype.function.VariableResolver;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;

/**
 * Variable resolver for XPath 
 * see Processor and XPathFunction
 * @author corby
 *
 */
class VariableResolverImpl implements VariableResolver {
	Document document;
	QName var;
	Text value;
	Environment env;
	
	VariableResolverImpl(Environment e){
		env = e;
	}
			
	public void start(org.w3c.dom.Node doc){
		if (doc instanceof Document) {
			document = (Document)doc;
		}
		var = null;
		value = null;
	}
	
	public Object resolveVariable(QName name) {
		if (var != null && value != null && name.equals(var)){
			//System.out.println("** XP1: " + name + " " + value);
			return value;
		}
		var = name;
		if (env == null) return null;
		//System.out.println("** Proxy: " + env);
		Node node = env.getNode("?" + name.getLocalPart());
		if (node != null){
			value = document.createTextNode(node.getLabel()); //name.getLocalPart());
			//System.out.println("** XP2: " + name + " " + value);
			return value;
		}
		else {
			//System.out.println("** XP3: " + name + " " + null);
			return null;
		}
	}
}


