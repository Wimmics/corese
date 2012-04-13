package fr.inria.edelweiss.kgenv.result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.parser.CompilerFacKgram;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;

/**
 * SPARQL XML Results Format Parser into Mappings
 * 
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class XMLResult {
	
	// create target Node
	Producer producer;
	// create query Node
	fr.inria.edelweiss.kgenv.parser.Compiler compiler;
	
	XMLResult(Producer p){
		producer = p;
		compiler = new CompilerFacKgram().newInstance();
	}
	
	/**
	 * Producer in order to create Node using p.getNode() method
	 * Use case: ProducerImpl.create(Graph.create());
	 */
	public static XMLResult create(Producer p){
		return new XMLResult(p);
	}
	
	/**
	 *  parse SPARQL XML Result as Mappings
	 */
	public Mappings parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException{
		Mappings maps = new Mappings();
		
		MyHandler handler = new MyHandler(maps);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(stream, handler);    
		
		return maps;
	}
	
	
	public Mappings parseString(String str) throws ParserConfigurationException, SAXException, IOException{
		return parse(new ByteArrayInputStream(str.getBytes()));
	}

	
	
    /**
     * 
     * SAX Handler 
     */
	public class MyHandler extends DefaultHandler {
		Mappings maps;
		Mapping map;
		String var;
		 
		boolean 
		// true for variable binding
		isContent = false,  
		// true for ask SPARQL Query
		isBoolean = false, 
		isURI = false,
		isLiteral = false,
		isBlank   = false;
		
		String text, datatype, lang;
		
		MyHandler(Mappings m){
			maps = m;
		}
		
	    public void startDocument (){

	    }
	    
	    // called for each binding
	    void clear(){
			isURI     = false;
	    	isLiteral = false;
			isBlank   = false;
			text 	  = null;
			datatype  = null;
			lang 	  = null;
	    }
	    
	    /**
	     *  result is represented by Mapping
	     *  add one binding to current Mapping
	     */
	    void add(Mapping map, String var, IDatatype dt){
	    	Node nvar = compiler.createNode(Variable.create("?" + var));
			Node nval = producer.getNode(dt);
			map.addNode(nvar, nval);
	    }
	    
	 
	    public void startElement(String namespaceURI, String simpleName, 
	    		String qualifiedName, Attributes atts){
	    	
	    	isContent = false;
	    	
	    	if (qualifiedName.equals("result")){
	    		map =  Mapping.create();
	    		maps.add(map);
	    	}
	    	else if (qualifiedName.equals("binding")){
	    		var = atts.getValue("name");
	    		clear();
	    	}
	    	else if (qualifiedName.equals("uri")){
	    		isContent = true;
	    		isURI = true;
	    	}
	    	else if (qualifiedName.equals("literal")){
	    		isContent = true;
	    		isLiteral = true;
	    		datatype = atts.getValue("datatype");
	    		lang 	 = atts.getValue("xml:lang");
	    	}
	    	else if (qualifiedName.equals("bnode")){
	    		isContent = true;
	    		isBlank = true;
	    	}
	    	else if (qualifiedName.equals("boolean")){
	    		isBoolean = true;
	    		isContent = true;
	    	}
	    }
	    
	    
	    public void endElement(String namespaceURI, String simpleName, String qualifiedName){

	    	if (isContent){
	    		
	    		isContent = false;

	    		if (text == null){
	    			// may happen with empty literal 
	    			text = "";
	    		}

	    		if (isURI){
	    			add(map, var, DatatypeMap.createResource(text));
	    		}
	    		else if (isBlank){
	    			add(map, var, DatatypeMap.createBlank(text));
	    		}
	    		else if (isLiteral){
	    			add(map, var, DatatypeMap.createLiteral(text, datatype, lang));
	    		}
	    		else if (isBoolean && text.equals("true")){
	    			maps.add(Mapping.create());
	    		}
	    	}
	    }
	    

	    /**
	     * In some case, there may be several calls to this function
	     * in one element.
	     */
	    public void characters (char buf [], int offset, int len){
	    	if (isContent){
	    		String s = new String(buf, offset, len);
	    		if (text == null){
	    			text = s;
	    		}
	    		else {
	    			text += s;
	    		}
	    	}
	    }


	    public void endDocument (){
	        
	    }
	}

}
