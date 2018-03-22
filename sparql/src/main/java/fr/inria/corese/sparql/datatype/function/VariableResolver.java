package fr.inria.corese.sparql.datatype.function;

import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Node;

public interface VariableResolver extends XPathVariableResolver {
	
	void start(Node doc);

}
