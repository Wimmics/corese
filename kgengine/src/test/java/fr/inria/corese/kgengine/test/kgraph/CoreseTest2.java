package fr.inria.corese.kgengine.test.kgraph;

//package test.kgraph;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Date;
//
//import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//
//
//
//import junit.framework.TestCase;
//
//import fr.inria.corese.sparql.api.*;
//import fr.inria.corese.sparql.exceptions.EngineException;
//
//import fr.inria.corese.sparql.triple.javacc1.ParseException;
//import fr.inria.corese.sparql.triple.parser.Model;
//
//import fr.inria.corese.kgram.api.core.Node;
//import fr.inria.corese.kgram.core.Mappings;
//import fr.inria.corese.kgram.event.StatListener;
//import fr.inria.corese.kgram.filter.Interpreter;
//import fr.inria.edelweiss.kgraph.core.Graph;
//import fr.inria.edelweiss.kgraph.logic.Entailment;
//import fr.inria.edelweiss.kgraph.query.MatcherImpl;
//import fr.inria.edelweiss.kgraph.query.ProducerImpl;
//import fr.inria.edelweiss.kgraph.query.QueryProcess;
//import fr.inria.edelweiss.kgtool.load.Load;
//import junit.textui.TestRunner;
//
//public class CoreseTest2 extends TestCase {
//
//	private String corese;
//	static IEngine engine;
//	
//	//private static final String path = "/u/bego/0/acacia/osavoie/Work/corese/comma/query/";
//	private static final String path =  "/0/user/corby/jbproject/corese/data/comma/query/";
//	//private static final String path = "D:/vbottoll/test/troncagainagain/corese/data/comma/query/";
//	private File file;
//	private BufferedReader buf = null;
//	private String queryFileName;
//	private int expected;
//	private String query, name;
//	public static String squery;
//    private boolean isquery = false;
//	private boolean normal = false; // if false, no print no pprint (to speed up)
//	boolean brelations = false; // test nb relations in result
//	String[] answer;
//    String functionCalled = "";
//	Model model = null; // to test get:gui
//	IResults gres;
//	boolean skip = false;
//	boolean printResult = !true; //CoreseTestSuiteParser1.displayResult;
//    boolean printResult2 = true;
//    private static Logger logger = LogManager.getLogger(CoreseTest2.class);
//    static StatListener el;
//    static int count = 0;
//    
//    static Graph graph;
//    
//	/**
//	 *
//	 * @param Name_
//	 */
//	public CoreseTest2(String Name_) {
//		super(Name_);
//	}
//
//	/**
//	 *
//	 * @param qq: boolean, indicate if the string given (query) is a the name of a file or a direct query
//	 * @param Name_: String, the method to call in CoreseTest2.java
//	 * @param corese: instantiation of Corese class
//	 * @param query: String, the query to perform or the name of the file containing the query to perform
//	 * @param expected: int, the number of results expected
//	 */
//	public CoreseTest2(boolean qq, String Name_, String corese, String query, int expected) {
//		super(Name_);
//		if (qq) // direct query, no file
//			this.query = query;
//		else
//			this.queryFileName = query;
//		this.expected = expected;
//		this.corese = corese;
//        this.functionCalled = Name_;
//		isquery = qq;
//	}
//	
//	public CoreseTest2(String Name_, String corese, String query, String name, int expected) {
//		this(true, Name_, corese, query, expected);
//		this.name = name;
//	}
//
//	/**
//	 *
//	 * @param qq: boolean, indicate if the string given (query) is a the name of a file or a direct query
//	 * @param mod: Model
//	 * @param Name_: String, the method to call in CoreseTest2.java
//	 * @param corese: instantiation of Corese class
//	 * @param query: String, the query to perform or the name of the file containing the query to perform
//	 * @param expected: int, the number of results expected
//	 */
//	public CoreseTest2(boolean qq, Model mod, String Name_, String corese, String query, int expected) {
//		this(qq, Name_, corese, query, expected);
//		model = mod;
//	}
//
//	/**
//	 *
//	 * not used?
//	 * @param qq: boolean, indicate if the string given (query) is a the name of a file or a direct query
//	 * @param Name_: String, the method to call in CoreseTest2.java
//	 * @param corese: instantiation of Corese class
//	 * @param query: String, the query to perform or the name of the file containing the query to perform
//	 * @param expected: int, the number of results expected
//	 */
//	public CoreseTest2(boolean qq, String Name_, String corese, String query, String[] expected) {
//		this(qq, Name_, corese, query, 0);
//		answer = expected;
//	}
//
//	/**
//	 * to call the query from a file (query)
//	 * @param Name_: String, the method to call in CoreseTest2.java
//	 * @param corese: instantiation of Corese class
//	 * @param query: String, the name of the file
//	 * @param expected: int, the number of results expected
//	 */
//	public CoreseTest2(String Name_, String corese, String query, int expected) {
//		this(false, Name_, corese, query, expected);
//	}
//
//	/**
//	 * To get the query from the file, create a BufferedReader
//	 */
//	protected void setUp() {
//		if (isquery)
//			return;
//		file = new File(path + queryFileName);
//		System.out.println("here: " + path + " " + queryFileName);
//		try {
//			buf = new BufferedReader(new FileReader(file));
//		} catch (IOException e) {
//			System.err.println(e.toString());
//		}
//	}
//
//	/**
//	 *
//	 * @return the query contained in the file
//	 */
//	private String readFile() {
//		try {
//			String str = "", cur = "";
//			while ((cur = buf.readLine()) != null)
//				str += cur;
//			return str;
//		} catch (IOException e) {
//			System.err.println(e.toString());
//			return null;
//		}
//	}
//
//	/**
//	 * close the BufferedReader
//	 */
//	protected void tearDown() {
//		try {
//			if (buf != null)
//				buf.close();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//
//	}
//
//
//
//
//	/**
//	 * call: testTriple
//	 * @throws FileNotFoundException
//	 * @throws ParseException
//	 */
//	public void testQuery() throws FileNotFoundException, ParseException {
//		testTriple(false);
//	}
//	
//	
//	public void testValue() throws ParseException {
//		Mappings res = eval(query);
//		
//		if (res == null || res.size() == 0){
//			assertEquals(query + " : ", expected, 0);
//			return;
//		}
//		
//		
//		Node val  = res.get(0).getNode(name);
//		if (val == null){
//			assertEquals(query + " : ", expected, 0);
//			return;
//		}
//		
//		IDatatype dt = (IDatatype) val.getValue();
//		assertEquals(query + " : ", expected, dt.getIntegerValue());
//
//	}
//
//	/**
//	 *
//	 * @throws FileNotFoundException
//	 * @throws ParseException
//	 */
//	public void testRelation() throws FileNotFoundException, ParseException {
//		brelations = true;
//		testTriple(false);
//	}
//
//	/**
//	 *
//	 * @throws FileNotFoundException
//	 * @throws ParseException
//	 */
//	public void testQueryCount() throws FileNotFoundException, ParseException {
//		testTriple(true);
//	}
//
//	/**
//	 *
//	 * @throws FileNotFoundException
//	 * @throws ParseException
//	 */
//	public void testCountConcept() throws FileNotFoundException, ParseException {
//		testTriple(true, true);
//	}
//
//	/**
//	 *
//	 * @param count
//	 * @throws FileNotFoundException
//	 * @throws ParseException
//	 */
//	public void testTriple(boolean count) throws FileNotFoundException, ParseException {
//		testTriple(count, false);
//	}
//
//	/**
//	 *
//	 * @param count
//	 * @param countConcept
//	 * @throws FileNotFoundException
//	 * @throws ParseException
//	 */
//	public void testTriple(boolean count, boolean countConcept) throws FileNotFoundException, ParseException {
//	
//		
//		long d1 = new Date().getTime();
//		int nb = test(query);
//		long d2 = new Date().getTime();
//		double dd = (d2-d1) / 1000.0;
//		if (dd > 1){
//			System.out.println(dd + " " + query);
//		}
//
//		
//		if (count) {
//			assertEquals(query + " : ", "", "");
//
//		} else if (brelations){
//			assertEquals(query + " : ", "", "");
//		}
//		//parse(res);
//		else {
//			if (skip){
//				nb = expected;
//			}
//			
//			if (nb!=expected){
//				//System.out.println(query + " " + nb + " " + expected);
//			}
//					
//			assertEquals(query + " : ", expected, nb);
//
//		}
//	}
//	
//	
//	int test(String query){
//		Mappings res = eval(query);
//		if (res == null) return -1;
//		return res.size();
//	}
//	
//	Mappings eval(String query){
//		//System.out.println(query);
//		if (graph == null) init();
//		QueryProcess exec = QueryProcess.create(graph);
//		try {
//			Mappings lMap = exec.query(query);
//			//System.out.println(lMap);
//			return lMap;
//		} catch (EngineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			System.out.println(query);
//
//		}
//		catch(java.lang.NullPointerException e){
//			e.printStackTrace();
//			System.out.println("Null Pointer");
//			System.out.println(query);
//		}
//		return null;
//	}
//	
//	void init(){
//		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
//		graph = Graph.create(true);
//		//graph.setIndex(true);
//		graph.set(Entailment.DATATYPE_INFERENCE, true);
//		
//		Load load = Load.create(graph);
//
//		long t1 = new Date().getTime();
//		load.load(data + "kgraph/rdf.rdf",  Entailment.RDF);
//		load.load(data + "kgraph/rdfs.rdf", Entailment.RDFS);
//		load.load(data + "comma/comma.rdfs");
//		load.load(data + "comma/commatest.rdfs");
//		load.load(data + "comma/model.rdf");
//		load.load(data + "comma/testrdf.rdf");
//		load.load(data + "comma/data");
//		load.load(data + "comma/data2");
//		long t2 = new Date().getTime();
//		System.out.println((t2-t1) / 1000.0 + "s");
//		System.out.println(graph);
//	}
//	
//	
//
//
//
//	
//	//FATAL : ** Test: null 
//	//select ?p ?max where {
//	//{select cardinality(?p) as ?card max(?card) as ?max where {?p rdf:type rdf:Property }} 
//	//?p rdf:type rdf:Property filter(cardinality(?p) = ?max) } limit 1
//
//	void check(StatListener el, String query){
////		if (el.getStat(Event.START) != el.getStat(Event.END)){
////			System.out.println(query);
////			System.out.println(el.getStat(Event.START) + " " + el.getStat(Event.END));
////		}
////		if (count == el.getStat(Event.START)){
////			System.out.println(query);
////		}
////		count = el.getStat(Event.START);
//		System.out.println(el);
//		System.out.println();
//	}
//	
//
//	/**
//	 * main
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		String[] testCaseName = { CoreseTest2.class.getName() };
//		TestRunner.main(testCaseName);
//
//	}
//
//}
//
