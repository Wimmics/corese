package fr.inria.corese.compiler.result;

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

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.parser.CompilerFacKgram;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Collection;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL XML Results Format Parser into Mappings
 *
 * @author Olivier Corby, Wimmics, INRIA 2012
 *
 */
public class XMLResult {

    private static Logger logger = LoggerFactory.getLogger(XMLResult.class);
    static final String NL = System.getProperty("line.separator");

    // create target Node
    Producer producer;
    // create query Node
    	private fr.inria.corese.compiler.parser.Compiler compiler;
    HashMap<String, Integer> table;
    ArrayList<Node> varList;
    private static final int UNKNOWN = -1;
    private static final int RESULT = 1;
    private static final int BINDING = 2;
    private static final int URI = 3;
    private static final int LITERAL = 4;
    private static final int BNODE = 5;
    private static final int BOOLEAN = 6;
    private static final int VARIABLE = 7;
    private static final int LINK = 8;
    
    private boolean debug = false;
    private boolean trapError = false;
    private boolean showResult = false;
    private List<String> link;

    public XMLResult() {
        init();
    }

    XMLResult(Producer p) {
        this();
        producer = p;
    }

    /**
     * Producer in order to create Node using p.getNode() method Use case:
     * ProducerImpl.create(Graph.create());
     */
    public static XMLResult create(Producer p) {
        return new XMLResult(p);
    }

    public class VTable extends HashMap<String, Variable> {

        public Variable get(String name) {
            Variable var = super.get(name);
            if (var == null) {
                var = new Variable("?" + name);
                put(name, var);
            }
            return var;
        }
    }

    /**
     * parse SPARQL XML Result as Mappings
     */
    public Mappings parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException 
    {
        
        if (isShowResult()) {
            String str = read(stream);
            System.out.println(str);
            setShowResult(false);
            InputStream inputStream = new ByteArrayInputStream(str.getBytes());
            return parse(inputStream);
        }
        
        if (debug) {
            System.out.println("start parse XML result");
        }
        Mappings map = new Mappings();

        MyHandler handler = new MyHandler(map);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            SAXParser parser = factory.newSAXParser();
            InputStreamReader r = new InputStreamReader(stream, "UTF-8");
            parser.parse(new InputSource(r), handler);
            complete(map);
            map.setLinkList(getLink());
            return map;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            if (isTrapError()) {                
                logger.error(e.toString());
                complete(map);
                map.setError(true);
                System.out.println("Return partial result of size: " + map.size());
                return map;
            }
            else {
                throw e;
            }
        }
    }

    public Collection<Node> getVariables() {
        return getCompiler().getVariables();
    }

    public void complete(Mappings map) {
        try {
            ASTQuery ast = ASTQuery.create();
            ast.setBody(BasicGraphPattern.create());
            for (Node n : varList) { 
                ast.setSelect(new Variable(n.getLabel()));
            }
            QuerySolver qs = QuerySolver.create();
            Query q = qs.compile(ast);
            q.setServiceResult(true);
            map.setQuery(q);
            map.init(q);
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(XMLResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init() {
        link = new ArrayList<>();
        varList = new ArrayList<>();
        setCompiler(new CompilerFacKgram().newInstance());
        table = new HashMap<>();
        table.put("result",  RESULT);
        table.put("binding", BINDING);
        table.put("uri",     URI);
        table.put("bnode",   BNODE);
        table.put("literal", LITERAL);
        table.put("boolean", BOOLEAN);
        table.put("variable",VARIABLE);
        table.put("link",   LINK);
    }

    int type(String name) {
        Integer val = table.get(name);
        if (val != null) {
            return val;
        }
        return UNKNOWN;
    }

    public Mappings parseString(String str) throws ParserConfigurationException, SAXException, IOException {
        return parseString(str, "UTF-8");
    }

    public Mappings parseString(String str, String encoding) throws ParserConfigurationException, SAXException, IOException {
        return parse(new ByteArrayInputStream(str.getBytes(encoding)));
    }

    public Mappings parse(String path) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = getStream(path);
        return parse(stream);
    }

    public Node getURI(String str) {
        IDatatype dt = DatatypeMap.createResource(str);
        Node n = producer.getNode(dt);
        return n;
    }

    public Node getBlank(String str) {
        IDatatype dt = DatatypeMap.createBlank(str);
        Node n = producer.getNode(dt);
        return n;
    }

    public Node getLiteral(String str, String datatype, String lang) {
        IDatatype dt = DatatypeMap.createLiteral(str, datatype, lang);
        Node n = producer.getNode(dt);
        return n;
    }
    
    public void defineVariable(Node var) {
       varList.add(var);
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
        boolean // true for variable binding
                isContent = false,
                // true for ask SPARQL Query
                isBoolean = false,
                isURI = false,
                isLiteral = false,
                isBlank = false,
                isVariable = false;
        String text, datatype, lang;

        MyHandler(Mappings m) {
            maps = m;
            vtable = new VTable();
            lvar = new ArrayList<>();
            lval = new ArrayList<>();
        }

        @Override
        public void startDocument() {
            if (debug) {
                System.out.println("start document");
            }
        }

        // called for each binding
        void clear() {
            isURI = false;
            isLiteral = false;
            isBlank = false;
            text = null;
            datatype = null;
            lang = null;
            isVariable = false;
        }

        /**
         * result is represented by Mapping add one binding to current Mapping
         */
        void add(String var, Node nval) {
            Node nvar = getVariable(var);
            lvar.add(nvar);
            lval.add(nval);
        }
        
        Node getVariable(String var) {
            return getCompiler().createNode(vtable.get(var));
        }
        

        @Override
        public void startElement(String namespaceURI, String simpleName,
                String qualifiedName, Attributes atts) {
            if (debug) {
                System.out.println("open: " + qualifiedName);
            }
            isContent = false;

            switch (type(simpleName)) {
                
                case LINK:
                    addLink(atts.getValue("href"));                    
                    break;

                case RESULT:
                    //map =  Mapping.create();
                    //maps.add(map);
                    lval.clear();
                    lvar.clear();
                    break;
                    
                case VARIABLE:
                   String name = atts.getValue("name");
                   defineVariable(getVariable(name));
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
                    lang = atts.getValue("xml:lang");
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

        @Override
        public void endElement(String namespaceURI, String simpleName, String qualifiedName) {
            if (debug) {
                System.out.println("close: " + qualifiedName);
            }
            if (isContent) {

                isContent = false;

                if (text == null) {
                    // may happen with empty literal 
                    text = "";
                }

                if (isURI) {
                    add(var, getURI(text));
                } else if (isBlank) {
                    // TODO: should we generate a fresh ID ?
                    add(var, getBlank(text));
                } else if (isLiteral) {
                    add(var, getLiteral(text, datatype, lang));
                } else if (isBoolean && text.equals("true")) {
                    maps.add(Mapping.create());
                }
            } else {
                switch (type(simpleName)) {

                    case RESULT:
                        Mapping map = Mapping.create(lvar, lval);
                        if (debug) {
                            System.out.println(map);
                        }
                        maps.add(map);
                }
            }
        }

        /**
         * In some case, there may be several calls to this function in one
         * element.
         */
        @Override
        public void characters(char buf[], int offset, int len) {
            if (isContent) {
                String s = new String(buf, offset, len);
                if (text == null) {
                    text = s;
                } else {
                    text += s;
                }
            }
        }

        @Override
        public void endDocument() {
        }
    }

    InputStream getStream(String path) throws FileNotFoundException {
        try {
            URL uri = new URL(path);
            return uri.openStream();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }

        FileInputStream stream;
        stream = new FileInputStream(path);
        return stream;
    }
    
        /**
     * @return the trapError
     */
    public boolean isTrapError() {
        return trapError;
    }

    /**
     * @param trapError the trapError to set
     */
    public void setTrapError(boolean trapError) {
        this.trapError = trapError;
    }
    
    public String read(InputStream stream) throws IOException  {
       return read(new InputStreamReader(stream));
    }
    
    String read(Reader fr) throws IOException {
        BufferedReader fq = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String str;
        boolean isnl = false;
        while (true) {
            str = fq.readLine();
            if (str == null) {
                fq.close();
                break;
            }
            if (isnl){
                sb.append(NL);
            }
            else {
                isnl = true;
            }
            sb.append(str);
            //sb.append(NL);
        }
        return sb.toString();
    }

    /**
     * @return the showResult
     */
    public boolean isShowResult() {
        return showResult;
    }

    /**
     * @param showResult the showResult to set
     */
    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

    /**
     * @return the compiler
     */
    public fr.inria.corese.compiler.parser.Compiler getCompiler() {
        return compiler;
    }

    /**
     * @param compiler the compiler to set
     */
    public void setCompiler(fr.inria.corese.compiler.parser.Compiler compiler) {
        this.compiler = compiler;
    }

    public List<String> getLink() {
        return link;
    }

    public void setLink(List<String> link) {
        this.link = link;
    }
    
    public void addLink(String link) {
        getLink().add(link);
    }
}
