package test.w3c;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLResult {
	
	
	// parse SPARQL XML Result
	ArrayList<Result> parse(String name){
		MyHandler handler = new MyHandler();
		SAXParserFactory factory=SAXParserFactory.newInstance();
		try {
			SAXParser parser=factory.newSAXParser();
			parser.parse(name, handler);    
		}
		catch (SAXException e){
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return handler.getResult();
	}
	
	
	public class MyHandler extends DefaultHandler {
		ArrayList<Result> vec;
		Result res;
		Value value;
		String var;
		 
		// when character is content
		boolean isContent = false, ask = false, isBoolean = false;
		
		MyHandler(){
			
			vec = new ArrayList<Result>();
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
	    	}
	    	else if (qualifiedName.equals("binding")){
	    		 var = atts.getValue("name");
	    	}
	    	else if (qualifiedName.equals("uri")){
	    		isContent = true;
	    		 value = Value.createURI(null);
	    	}
	    	else if (qualifiedName.equals("literal")){
	    		isContent = true;
	    		value = Value.createLiteral(null,
	    				atts.getValue("datatype"),
	    				atts.getValue("xml:lang"));
	    	}
	    	else if (qualifiedName.equals("bnode")){
	    		isContent = true;
	    		 value = Value.createBlank(null);
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
	    			res.put(var, value);
	    		}
	    	}
	    }
	    
	    
	    public void characters (char buf [], int offset, int len){
	    	if (isContent){
	    		isContent = false;
	    		String s=new String(buf, offset, len);
	    		if (isBoolean){
	    			ask = s.equals("true");
	    			// simulate one (true) result:
	    			if (ask) vec.add(new Result());
	    		}
	    		else {
	    			if (value.isLiteral()){
	    				value.setValue(s);
	    			}
	    			else {
	    				value.setURI(s);
	    			}
	    			//System.out.println("** Test " + var + " " + value);
	    			res.put(var, value);
	    		}
	    	}
	    }


	    public void endDocument (){
	        
	    }
	}

}
