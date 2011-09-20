package fr.inria.edelweiss.kgenv.eval;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.filter.Proxy;
import fr.inria.lang.StringHelper;

/**
 * Implements evaluator of operators & functions of filter language
 * with IDatatype values
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class ProxyImpl implements Proxy, ExprType {
	private static Logger logger = Logger.getLogger(ProxyImpl.class);	

	protected static IDatatype TRUE = DatatypeMap.TRUE;
	protected static IDatatype FALSE = DatatypeMap.FALSE;
	static final String UTF8 = "UTF-8";
	public static final String RDFNS   =  "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFTYPE   = RDFNS + "type";
		
	Proxy plugin;
	SQLFun sql;
	Evaluator eval;
	EvalListener el;
	DatatypeMap dm;
	int number = 0;
	
	// KGRAM is relax wrt to string vs literal vs uri input arg of functions
	// eg regex() concat() strdt()
	// setMode(SPARQL_MODE) 
	boolean SPARQLCompliant = false;
	
	
	public ProxyImpl(){
		dm = DatatypeMap.create();
		sql = new SQLFun();
	}
	
	public void setEvaluator(Evaluator ev){
		eval = ev;
	}
	
	public Evaluator getEvaluator(){
		return eval;
	}
	
	public void setPlugin(Proxy p){
		plugin = p;
	}
	
	public Proxy getPlugin(){
		return plugin;
	}
	
	public void setMode(int mode) {
		switch (mode){
		
		case Evaluator.SPARQL_MODE: 
			SPARQLCompliant = true; break;
			
		case Evaluator.KGRAM_MODE: 
			SPARQLCompliant = false; break;	
		}
	}
	
	public void start(){
		number = 0;
	}
	
	@Override
	public Object eval(Expr exp, Environment env, Object o1, Object o2) {
		IDatatype dt1 = (IDatatype) o1;
		IDatatype dt2 = (IDatatype) o2;		
		boolean b = true;
		
		try {
			switch(exp.oper()){
			
			case NEQ: 	b = ! dt1.equals(dt2); break;
			case IN:	return in(dt1, dt2); 
			case EQ: 	b = dt1.equals(dt2); break;
			case LT: 	b = dt1.less(dt2); break;
			case LE: 	b = dt1.lessOrEqual(dt2); break;
			case GE: 	b = dt1.greaterOrEqual(dt2); break;
			case GT: 	b = dt1.greater(dt2); break;
			case CONT: 	b = dt1.contains(dt2); break;
			case START: b = dt1.startsWith(dt2); break;
			
			case PLUS: 
				if (SPARQLCompliant){
					if (!(dt1.isNumber() && dt2.isNumber())){
						return null;
					}
				}
				return dt1.plus(dt2);
				
			case MINUS: return dt1.minus(dt2);
			case MULT: 	return dt1.mult(dt2);
			case DIV: 
				try {
					return dt1.div(dt2);
				}
				catch (java.lang.ArithmeticException e){
					return null;
				}
			
			default: 
				if (plugin!=null) 
					return (IDatatype) plugin.eval(exp, env, dt1, dt2);
				return null;
			
			}
		}
		catch (CoreseDatatypeException e){
			return null;
		}
		
		return (b) ? TRUE : FALSE;
	}
	
	
	
	
	public Object function(Expr exp, Environment env) {
		
		switch (exp.oper()){
		
		case NUMBER: return getValue(env.count());
			
		case RANDOM: return getValue(Math.random());	
			
		case NOW:	 return DatatypeMap.newDate();	
		
		case BNODE:  return DatatypeMap.createBlank();
		
		case PATHNODE: return pathNode(env);		
			
		default: if (plugin!=null){
				return plugin.function(exp, env);
			}
		}
		
		return null; 
	}
	
	


	public Object function(Expr exp, Environment env, Object o1) {
		
		IDatatype dt = (IDatatype) o1;
		
		switch (exp.oper()){
				
		case ISURI: 	return (dt.isURI()) ? TRUE : FALSE;
		
		case ISLITERAL: return (dt.isLiteral()) ? TRUE : FALSE; 
		
		case ISBLANK: 	return (dt.isBlank()) ? TRUE : FALSE;  
		
		case ISNUMERIC: return (dt.isNumber()) ? TRUE : FALSE; 
		
		case URI:  return uri(exp, dt);
		
		case STR: return str(exp, dt);
		
		case STRLEN: return getValue(dt.getLabel().length());
			
		case UCASE: return ucase(dt);
		
		case LCASE: return lcase(dt);
					
		case ENCODE:  return encode(dt);
			
		case ABS: return abs(dt);
			
		case FLOOR:
			return getValue(Math.floor(dt.getDoubleValue()), dt.getDatatypeURI());
			
		case ROUND:
			return getValue(Math.round(dt.getDoubleValue()), dt.getDatatypeURI());

		case CEILING:
			return getValue(Math.ceil(dt.getDoubleValue()), dt.getDatatypeURI());
			
		case TIMEZONE: return dm.getTimezone(dt);
		
		case TZ: return dm.getTZ(dt);
		
		case YEAR:
		case MONTH:
		case DAY:
		case HOURS:
		case MINUTES:
		case SECONDS: return time(exp, dt);
		
		case HASH: return hash(exp, dt);

		case LANG: 	return dt.getDataLang(); 
		
		case BNODE: return DatatypeMap.createBlank(dt.getLabel());
			
		case DATATYPE: return dt.getDatatype();
		
		case DISPLAY:
			System.out.println(exp + " = " + dt);
			return TRUE;
			
		default:
			if (plugin != null){
				return plugin.function(exp, env, o1); 
			}	
		
		}
		return null;
	}
	
	
	
	public Object function(Expr exp, Environment env, Object o1, Object o2) {
		IDatatype dt  = (IDatatype) o1;
		IDatatype dt1 = (IDatatype) o2;
		boolean b;
		
		switch(exp.oper()){
				
		case CONTAINS: 
			// SPARQL 1.1 distinguishes cases  
			b = dt.getLabel().contains(dt1.getLabel());
			return (b) ? TRUE : FALSE;

		case STARTS: 
			b = dt.startsWith(dt1); 
			return (b) ? TRUE : FALSE;

		case ENDS:
			b = dt.getLabel().endsWith(dt1.getLabel());
			return (b) ? TRUE : FALSE;
			
		case SUBSTR: 			
			return substr(dt, dt1, null);	
			
		case LANGMATCH: 
			return langMatches(dt, dt1); 
			
		case STRDT:
			if (SPARQLCompliant && ! DatatypeMap.isSimpleLiteral(dt)){
				return null;
			}
			return DatatypeMap.createLiteral(dt.getLabel(), dt1.getLabel());

		case STRLANG:
			if (SPARQLCompliant && ! DatatypeMap.isSimpleLiteral(dt)){
				return null;
			}
			return DatatypeMap.createLiteral(dt.getLabel(), null, dt1.getLabel());	
			
		case REGEX: {
			if (SPARQLCompliant && ! DatatypeMap.isStringLiteral(dt)){
				return null;
			}
			Processor proc = getProcessor(exp);
			b = proc.regex(dt.getLabel(), dt1.getLabel());
			return (b) ? TRUE : FALSE;
		}
			
			
		case XPATH: {
			// xpath(?g, '/book/title')
			Processor proc = getProcessor(exp);
			proc.setResolver(new VariableResolverImpl(env));
			IDatatype res = proc.xpath(dt, dt1);				
			return res;
		}	
		
		case EXTEQUAL: {
			boolean bb = StringHelper.equalsIgnoreCaseAndAccent(dt.getLabel(), dt1.getLabel());
			return (bb) ? TRUE : FALSE;
		}
			
		case EXTCONT: {
			boolean bb = StringHelper.containsWordIgnoreCaseAndAccent(dt.getLabel(), dt1.getLabel());
			return (bb) ? TRUE : FALSE;
		}
		
		default:
			if (plugin != null){
				return plugin.function(exp, env, o1, o2); 
			}
			
			
		}
		
		return null;
	}

	@Override
	public Object eval(Expr exp, Environment env, Object[] args) {
		switch (exp.oper()){
					
		case EXTERNAL:
			// user defined function with prefix/namespace
			// function://package.className
			Processor proc = getProcessor(exp);
			return proc.eval(args);
			
		case KGRAM: 
		case EXTERN:
		case PROCESS:
			return plugin.eval(exp, env, args);
			
		case DEBUG:
			if (el == null){
				el = EvalListener.create();
				env.getEventManager().addEventListener(el);
			}
			int i = 0;
			for (Object arg : args){
				Event e = EventImpl.create(Event.FILTER, exp.getExp(i++), arg);
				env.getEventManager().send(e);
			}
			return TRUE;
			
		case CONCAT: 
			return concat(args);			
		}
				
		
		boolean b = true;
		IDatatype dt = null, dt1 = null;
		if (args.length>0){
			dt = (IDatatype) args[0];
		}
		if (args.length>1){
			dt1 = (IDatatype) args[1];
		}
		
		switch (exp.oper()){
		
			case REGEX: 
				// it may have a 3rd argument stored as getModality()
				if (SPARQLCompliant && ! DatatypeMap.isStringLiteral(dt)){
					return null;
				}
				Processor proc = getProcessor(exp);
				b = proc.regex(dt.getLabel(), dt1.getLabel());
				return (b) ? TRUE : FALSE;
																		
			case SUBSTR: 
				IDatatype dt2 = null;
				if (args.length>2){
					dt2 = datatype(args[2]);
				}
				return substr(dt, dt1, dt2);				

			case CAST: // cast(?x, xsd:string, CoreseString)
				return dt.cast(dt1, datatype(args[2]));
																
			case SQL: 
				// return ResultSet
				return sql(exp, env, args);
													
			default:
				if (plugin != null){
					return plugin.eval(exp, env, args); 
				}	
		}
			
		return null;
	}
	
	IDatatype encode(IDatatype dt){
		try {
			String str = URLEncoder.encode(dt.getLabel(), UTF8);
			return dm.createLiteral(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// first index is 1
	IDatatype substr(IDatatype dt, IDatatype ind, IDatatype len){
		String str = dt.getLabel();
		int start = ind.getIntegerValue();
		start = Math.max(start-1, 0);
		int end = str.length();
		if (len!=null){
			end = len.getIntegerValue();
		}
		end = start + end;
		if (end > str.length()){
			end = str.length();
		}
		str = str.substring(start, end);
		return getValue(dt, str);
	}
	
	// return a Literal (not a xsd:string)
	IDatatype str(Expr exp, IDatatype dt){
		return DatatypeMap.createLiteral(dt.getLabel());
	}
	
	IDatatype ucase(IDatatype dt){
		String str = dt.getLabel().toUpperCase();
		return getValue(dt, str);
	}
	
	IDatatype lcase(IDatatype dt){
		String str = dt.getLabel().toLowerCase();
		return getValue(dt, str);
	}		
	
	IDatatype uri(Expr exp, IDatatype dt){
		if (dt.isURI()) return dt;
		String label = dt.getLabel();
		if (exp.getModality()!=null && ! isURI(label)){
			// with base
			return DatatypeMap.newResource(exp.getModality() + label);
		} 
		else return DatatypeMap.newResource(label);
	}
	
	boolean isURI(String str){
		return str.matches("[a-zA-Z0-9]+://.*");
	}

	
	/**
	 * literals with same lang return literal@lang
	 * all strings return string
	 * else return literal
	 * error if not literal or string
	 */
	IDatatype concat(Object[] args){
		String str = "",  lang = null;
		if (args.length==0) return getValue(str);
		
		IDatatype dt = datatype(args[0]);
		boolean ok = true, hasLang = false, isString = true;
		if (dt.hasLang()){
			hasLang = true;
			lang = dt.getLang();
		}
		
		for (Object obj : args){
			
			dt = datatype(obj);
			
			if (SPARQLCompliant && ! DatatypeMap.isStringLiteral(dt)){
				return null;
			}
			
			str += dt.getLabel();
			
			if (ok){
				if (hasLang){
					if (! (dt.hasLang() && dt.getLang().equals(lang))){
						ok = false;
					}
				}
				else if (dt.hasLang()){
					ok = false;
				}
				
				if (! DatatypeMap.isString(dt)){
					isString = false;
				}
			}
		}	
		
		if (ok && lang != null){
			return DatatypeMap.createLiteral(str, null, lang);
		}
		else if (isString){
			return getValue(str);
		}
		else {
			return DatatypeMap.createLiteral(str);
		}
	}
	
	
	IDatatype time(Expr exp, IDatatype dt){
		if (dt.getDatatypeURI().equals(RDF.xsddate) ||
			dt.getDatatypeURI().equals(RDF.xsddateTime)){
			
			switch(exp.oper()){
			
			case YEAR: 	return DatatypeMap.getYear(dt);
			case MONTH:	return DatatypeMap.getMonth(dt);
			case DAY:	return DatatypeMap.getDay(dt);
			
			case HOURS: 	return DatatypeMap.getHour(dt);
			case MINUTES:	return DatatypeMap.getMinute(dt);
			case SECONDS: 	return DatatypeMap.getSecond(dt);
			}
		}
		
		return null;
	}
	
	
	IDatatype hash(Expr exp, IDatatype dt){
		String name = exp.getModality();
		String str = dt.getLabel();
		String res = new Hash(name).hash(str);
		if (res == null) return null;
		return dm.createLiteral(res);
	}
	
	
	IDatatype abs(IDatatype dt){
		if (DatatypeMap.isInteger(dt)){
			return getValue(Math.abs(dt.getIntegerValue()));
		}
		else if (DatatypeMap.isLong(dt)){
			return getValue(Math.abs(dt.getlValue()));
		}
		else {
			return getValue(Math.abs(dt.getDoubleValue()));
		}
	}

	/**
	 * sum(?x)
	 */
	public Object aggregate(Expr exp, Environment env, Node qNode){
		Walker walk = new Walker(exp, qNode, this, env);

		// apply the aggregate on current group Mapping, 
		env.aggregate(walk, exp.getFilter());

		return walk.getResult();
	}
	
	
	
	Processor getProcessor(Expr exp){
		return ((Term)exp).getProcessor();
	}


	// IDatatype KGRAM value to target proxy value 
	public Object getConstantValue(Object value) {
		return value;
	}
	
	IDatatype pathNode(Environment env){
		Query q = env.getQuery();
		int num = q.getGlobalQuery().nbPath();
		IDatatype dt = DatatypeMap.createBlank(Query.BPATH + Integer.toString(num));
		return dt;
	}


	@Override
	public boolean isTrue(Object value) {
		IDatatype dt = (IDatatype) value;
		//if (! dt.isTrueAble()) return false;
		try {
			return dt.isTrue();
		} catch (CoreseDatatypeException e) {
			return false;
		}
	}
	
	public boolean isTrueAble(Object value) {
		IDatatype dt = (IDatatype) value;
		return dt.isTrueAble();
	}

	
	IDatatype datatype(Object o){
		return (IDatatype) o;
	}

	@Override
	public IDatatype getValue(boolean b) {
		// TODO Auto-generated method stub
		if (b) return TRUE;
		else return FALSE;
	}
	
	@Override
	public IDatatype getValue(int value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(long value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(float value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(double value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(double value, String datatype) {
		return dm.newInstance(value, datatype);
	}
	
	// return xsd:string
	public IDatatype getValue(String value) {
		return dm.newInstance(value);
	}
	
	// return rdfs:Literal or xsd:string wrt dt
	public IDatatype getValue(IDatatype dt, String value) {
		if (dt.hasLang()){
			return DatatypeMap.createLiteral(value, null, dt.getLang());
		}
		else if (dt.isLiteral() && dt.getDatatype() == null){
			return dm.createLiteral(value);
		}
		return dm.newInstance(value);
	}
	
	Object langMatches(IDatatype ln1, IDatatype ln2) {
		if (ln2.getLabel().equals("*")) {
			return getValue(ln1.getLabel().length() > 0);
		}
		if (ln2.getLabel().indexOf("-")!=-1){
			// en-us need exact match
			return getValue(ln1.getLowerCaseLabel().equals(ln2.getLowerCaseLabel()));
		}
		return getValue(ln1.getLabel().regionMatches(true, 0, ln2.getLabel(), 0, 2)); 
	}
	
	public Object self(Object obj){
		return obj;
	}

	public Object similarity(Environment env){
		if (! (env instanceof Memory)) return getValue(0);
		Memory memory = (Memory) env;
		int count = 0, total = 0;
		
		for (Edge qEdge : memory.getQueryEdges()){
			
			if (qEdge != null && qEdge.getLabel().equals(RDFTYPE)){
				Edge edge = memory.getEdge(qEdge);
				if (edge != null){
					Node type = qEdge.getNode(1);
					if (type.isConstant()){ 
						total += 1;
						if (type.same(edge.getNode(1))){
							count += 1;
						}
					}
				}
			}
		}
		
		if (total == 0) return getValue(1);
		else return getValue(count/total);
		
	}
	
	/**
	 * sql('db', 'login', 'passwd', 'query')
	 * sql('db', 'driver', 'login', 'passwd', 'query')
	 * sql('db', 'driver', 'login', 'passwd', 'query', true)
	 * 
	 * sort means list of sparql variables (sql() as (var)) must be sorted according to sql variables in result
	 */
	Object sql(Expr exp, Environment env, Object[] args){
		ResultSet rs;
		boolean isSort = false;
		if (args.length == 4){
			// no driver
			rs = sql.sql(datatype(args[0]), datatype(args[1]), datatype(args[2]), datatype(args[3]));
		}
		else {
			if (args.length == 6){
				try {
					isSort = datatype(args[5]).isTrue();
				} catch (CoreseDatatypeException e) {
				}
			}
			// with driver
			rs = sql.sql(datatype(args[0]), datatype(args[1]), datatype(args[2]), datatype(args[3]), datatype(args[4]));
		}
		
		return new SQLResult(rs, isSort);
	}
	
	
	
	/**
	 * ?x in (a b)
	 * ?x in (xpath())
	 */
	Object in(IDatatype dt1, IDatatype dt2){

		boolean error = false;
		
		if (dt2.isArray()){
			
			for (IDatatype dt : dt2.getValues()){
				try {
					if (dt1.equals(dt)){
						return TRUE;
					}
				} catch (CoreseDatatypeException e) {
					error = true;
				}
			}
			
			if (error) return null;
			return FALSE;
		} 
		else
			try {
				if (dt1.equals(dt2)){
					return TRUE;
				}
			} catch (CoreseDatatypeException e) {
				return null;
			}
		
		return FALSE;
	}
	
	


}
