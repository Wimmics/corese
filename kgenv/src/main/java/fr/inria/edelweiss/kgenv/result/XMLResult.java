package fr.inria.edelweiss.kgenv.result;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.CompilerFacKgram;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import java.util.Collection;

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
	HashMap<String, Integer> table;

	private static final int UNKNOWN 	= -1;
	private static final int RESULT 	= 1;
	private static final int BINDING 	= 2;
	private static final int URI 		= 3;
	private static final int LITERAL 	= 4;
	private static final int BNODE 		= 5;
	private static final int BOOLEAN 	= 6;

	public XMLResult(){
		init();
	}
	
	XMLResult(Producer p){
		this();
		producer = p;
	}
	
	/**
	 * Producer in order to create Node using p.getNode() method
	 * Use case: ProducerImpl.create(Graph.create());
	 */
	public static XMLResult create(Producer p){
		return new XMLResult(p);
	}
	
	
	class VTable extends HashMap<String, Variable> {
		
		public Variable get(String name){
			Variable var = super.get(name);
			if (var == null){
				var = new Variable("?" + name);
				put(name, var);
			}
			return var;
		}
		
	}
	
	/**
	 *  parse SPARQL XML Result as Mappings
	 */
	public Mappings parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException{
		Mappings map = new Mappings();
		
		MyHandler handler = new MyHandler(map);
		SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
		SAXParser parser = factory.newSAXParser();
		InputStreamReader r = new InputStreamReader(stream, "UTF-8");
		parser.parse(new InputSource(r), handler);    
		complete(map);
		return map;
	}
        
        public Collection<Node> getVariables(){
            return compiler.getVariables();
        }
        
     void complete(Mappings map) {
        ASTQuery ast = ASTQuery.create();
        ast.setBody(BasicGraphPattern.create());
        for (Node n : getVariables()) {
            ast.setSelect(new Variable(n.getLabel()));
        }
        QuerySolver qs = QuerySolver.create();
        Query q = qs.compile(ast);
        map.setQuery(q);
        map.init(q);
    }
	
	public void init(){
		compiler = new CompilerFacKgram().newInstance();
		table = new HashMap<String, Integer> ();
		table.put("result", 	RESULT );
		table.put("binding", 	BINDING );
		table.put("uri", 	URI );
		table.put("bnode", 	BNODE );
		table.put("literal", 	LITERAL );
		table.put("boolean", 	BOOLEAN );
	}
	
	int type(String name){
		Integer val = table.get(name);
		if (val != null){
			return val;
		}
		return UNKNOWN;
	}
	
	public Mappings parseString(String str) throws ParserConfigurationException, SAXException, IOException{
		return parseString(str, "UTF-8");
	}
        
        public Mappings parseString(String str, String encoding) throws ParserConfigurationException, SAXException, IOException{
		return parse(new ByteArrayInputStream(str.getBytes(encoding))); 
	}
        
	
	public Mappings parse(String path) throws ParserConfigurationException, SAXException, IOException{
		InputStream stream = getStream(path);
		return parse(stream);
	}

	
	public Node getURI(String str){
		IDatatype dt = DatatypeMap.createResource(str);
		Node n = producer.getNode(dt);
		return n;
	}
	
	
	public Node getBlank(String str){
		IDatatype dt = DatatypeMap.createBlank(str);
		Node n = producer.getNode(dt);
		return n;
	}
	
	public Node getLiteral(String str, String datatype, String lang){
		IDatatype dt = DatatypeMap.createLiteral(str, datatype, lang);
		Node n = producer.getNode(dt);
		return n;
	}
	
	
    /**
     * 
     * SAX Handler 
     */
	public class MyHandler extends DefaultHandler {
		Mappings maps;
		//Mapping map;
		List<Node> lvar, lval;
		String var;
		VTable vtable;
		 
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
			vtable = new VTable();
			lvar = new ArrayList<Node>();
			lval = new ArrayList<Node>();
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
	    void add(String var, Node nval){
	    	Node nvar = compiler.createNode(vtable.get(var));
			lvar.add(nvar);
			lval.add(nval);
	    }
	    
	 
	    public void startElement(String namespaceURI, String simpleName, 
	    		String qualifiedName, Attributes atts){
	    	
	    	isContent = false;
	    	
	    	switch (type(simpleName)){
	    	
	    	case RESULT:
	    		//map =  Mapping.create();
	    		//maps.add(map);
	    		lval.clear();
	    		lvar.clear();
	    		break;
	    		
	    	case BINDING:
	    		var = atts.getValue("name");
	    		clear();
	    		break;
	    		
	    	case URI:
	    		isContent = true;
	    		isURI = true;
	    		break;
	    		
	    	case LITERAL:
	    		isContent = true;
	    		isLiteral = true;
	    		datatype = atts.getValue("datatype");
	    		lang 	 = atts.getValue("xml:lang");
	    		break;
	    		
	    	case BNODE:
	    		isContent = true;
	    		isBlank = true;
	    		break;
	    		
	    	case BOOLEAN:
	    		isBoolean = true;
	    		isContent = true;
	    		break;
	    	
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
	    			add(var, getURI(text));
	    		}
	    		else if (isBlank){
	    			// TODO: should we generate a fresh ID ?
	    			add(var, getBlank(text));
	    		}
	    		else if (isLiteral){
	    			add(var, getLiteral(text, datatype, lang));
	    		}
	    		else if (isBoolean && text.equals("true")){
	    			maps.add(Mapping.create());
	    		}
	    	}
	    	else switch(type(simpleName)){
	    	
	    		case RESULT:
	    			Mapping map = Mapping.create(lvar, lval);
	    			maps.add(map);
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
	
	
	
	
	InputStream getStream(String path) throws FileNotFoundException{
		try {
			URL uri = new URL(path);
			return uri.openStream();
		}  
		catch (MalformedURLException e) {
		} 
		catch (IOException e) {
		}

		FileInputStream stream;
		stream = new FileInputStream(path);
		return stream;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
