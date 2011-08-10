package fr.inria.edelweiss.kgenv.result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.parser.CompilerKgram;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;

/**
 *  SPARQL XML Result Format Parser
 *  Used by service to convert XML Result to Mapping
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class XMLResult {
	
	CompilerKgram compiler;
	Producer producer;
	
	XMLResult(Producer p){
		compiler = CompilerKgram.create();
		producer = p;
	}
	
	public static XMLResult create(Producer p){
		return new XMLResult(p);
	}
	
	public void set(Producer p){
		producer = p;
	}
	
	
	public Mappings parse(String xml){
		MyHandler handler = new MyHandler();
		SAXParserFactory factory=SAXParserFactory.newInstance();
		try {
			SAXParser parser=factory.newSAXParser();
			InputStream in = new ByteArrayInputStream(xml.getBytes()); 
			parser.parse(in, handler);    
		}
		catch (SAXException e){
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handler.getMappings();
	}
	
	
	public class MyHandler extends DefaultHandler {
		ArrayList<Result> vec;
		Result res;
		Value value;
		Constant cst;
		String var;
		List<Node> lVar, lValue;
		Mappings lMap;
		Mapping map;
		 
		// when character is content
		boolean isContent = false, ask = false, isBoolean = false;
		
		MyHandler(){
			lMap 		= new Mappings();
			vec 		= new ArrayList<Result>();
		}
		
		Mappings getMappings(){
			return lMap;
		}
		
		Node getVar(String name){
			return compiler.createNode(Variable.create("?" + name));
		}
		
		Node getValue(Constant cst){
			return producer.getNode(cst.getDatatypeValue());
		}
		
		ArrayList<Result> getResult(){
			return vec;
		}
		
		boolean getAsk(){
			return ask;
		}
		
		boolean isAsk(){
			return isBoolean;
		}

	    public void startDocument (){

	    }
	 
	    public void startElement(String namespaceURI, String simpleName, 
	    		String qualifiedName, Attributes atts){
	    	isContent = false;
	    	if (qualifiedName.equals("boolean")){
	    		isBoolean = true;
	    		isContent = true;
	    	}
	    	else if (qualifiedName.equals("result")){
	    		res = new Result();
	    		vec.add(res);
	    		lValue = new ArrayList<Node>();
	    		lVar = new ArrayList<Node>();
	    	}
	    	else if (qualifiedName.equals("binding")){
	    		var = atts.getValue("name");
	    	}
	    	else if (qualifiedName.equals("uri")){
	    		isContent = true;
	    		value = Value.createURI(null);
	    		cst = Constant.createResource();
	    	}
	    	else if (qualifiedName.equals("literal")){
	    		isContent = true;
	    		value = Value.createLiteral(null,
	    				atts.getValue("datatype"),
	    				atts.getValue("xml:lang"));
	    		cst = Constant.create(null, atts.getValue("datatype"), atts.getValue("xml:lang"));
	    	}
	    	else if (qualifiedName.equals("bnode")){
	    		isContent = true;
	    		value = Value.createBlank(null);
	    		cst = Constant.createBlank();
	    	}
	    }

	    public void endElement(String namespaceURI, String simpleName, 
	    		String qualifiedName){
	    	if (qualifiedName.equals("literal")){
	    		if (isContent){
	    			// had no characters, hence boolean still true
	    			// fake empty string
	    			isContent = false;
	    			value.setValue("");
	    			cst.setName("");
	    			if (cst.isResource()){
	    				cst.setLongName("");
	    			}
	    			res.put(var, value);
	    			
	    			Node nvar = getVar(var);
	    			Node nval = getValue(cst);
	    			lVar.add(nvar);
	    			lValue.add(nval);
	    		}
	    	}
	    	else if (qualifiedName.equals("result")){
	    		Mapping map = Mapping.create(lVar, lValue);
	    		lMap.add(map);
	    	}

	    }
	    
	    
	    public void characters (char buf [], int offset, int len){
	    	if (isContent){
	    		isContent = false;
	    		String s=new String(buf, offset, len);
	    		if (isBoolean){
	    			ask = s.equals("true");
	    			// simulate one (true) result:
	    			if (ask){
	    				vec.add(new Result());
	    			}
	    		}
	    		else {
    				cst.setName(s);
	    			if (value.isLiteral()){
	    				value.setValue(s);
	    			}
	    			else {
	    				value.setURI(s);
	    				cst.setLongName(s);
	    			}
	    			//System.out.println("** Test " + var + " " + value);
	    			res.put(var, value);
	    			
	    			Node nvar = getVar(var);
	    			Node nval = getValue(cst);
	    			lVar.add(nvar);
	    			lValue.add(nval);
	    		}
	    	}
	    }


	    public void endDocument (){
	        
	    }
	}

}
